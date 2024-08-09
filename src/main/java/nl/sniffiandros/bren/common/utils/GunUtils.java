package nl.sniffiandros.bren.common.utils;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalBlockTags;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import nl.sniffiandros.bren.common.Bren;
import nl.sniffiandros.bren.common.config.MConfig;
import nl.sniffiandros.bren.common.entity.BulletEntity;
import nl.sniffiandros.bren.common.entity.IGunUser;
import nl.sniffiandros.bren.common.network.NetworkUtils;
import nl.sniffiandros.bren.common.registry.AttributeReg;
import nl.sniffiandros.bren.common.registry.DamageTypeReg;
import nl.sniffiandros.bren.common.registry.EnchantmentReg;
import nl.sniffiandros.bren.common.registry.ItemReg;
import nl.sniffiandros.bren.common.registry.NetworkReg;
import nl.sniffiandros.bren.common.registry.ParticleReg;
import nl.sniffiandros.bren.common.registry.custom.GunItem;
import nl.sniffiandros.bren.common.registry.custom.RifleItem;
import nl.sniffiandros.bren.common.registry.custom.MagazineItem;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class GunUtils {
    public static int fire(LivingEntity user) {
        World world = user.getWorld();
        ItemStack stack = user.getMainHandStack();

        if (!(stack.getItem() instanceof GunItem gunItem)) return 0;

        if (!((IGunUser)user).getGunState().equals(GunHelper.GunStates.NORMAL)) return 0;

        boolean silenced = EnchantmentHelper.getLevel(EnchantmentReg.SILENCED, stack) >= 1;

        world.playSound(null,
                user.getX(),
                user.getY(),
                user.getZ(),
                silenced ? gunItem.getSilentShootSound() : gunItem.getShootSound(),
                SoundCategory.PLAYERS, silenced ? 0.5F : 5.0F, 1.0F - (user.getRandom().nextFloat() - 0.5F) / 8);

        if (!silenced) {
            GunUtils.playDistantGunFire(world, user.getPos());
        }

        int fireRate = (int)Math.round(user.getAttributeValue(AttributeReg.FIRE_RATE));

        if (!world.isClient()) {
            if (user.isPlayer()) {
                stack.damage(1, user, (p) -> p.sendToolBreakStatus(Hand.MAIN_HAND));
            }
            List<Vec3d> position = GunUtils.calculatePositionBasedOnAngle(user);
            Vec3d origin = position.get(0);
            Vec3d front = position.get(1);
            Vec3d down = position.get(2);
            Vec3d side = position.get(3);
            for (PlayerEntity p : user.getWorld().getPlayers()) {
                NetworkUtils.sendShotEffect(p, origin.add(side.add(down).multiply(0.15)), front);
            }

            if (!(stack.getItem() instanceof RifleItem)) {
                for (int i = 0; i < gunItem.bulletAmount(); ++i) {
                    float x = (user.getRandom().nextFloat() - 0.5f) * 2 * gunItem.spread();
                    float y = (user.getRandom().nextFloat() - 0.5f) * 2 * gunItem.spread();
                    GunUtils.spawnBullet(user, origin, front, stack, new Vec2f(x,y), false, gunItem.bulletLifespan());
                }
            } else {
                if (user.getWorld() instanceof ServerWorld serverWorld) {
                    Vec3d airRingPos = front.multiply(3);
                    for (ServerPlayerEntity p : serverWorld.getPlayers()) {
                        serverWorld.spawnParticles(p, ParticleReg.AIR_RING_PARTICLE, false, airRingPos.x, airRingPos.y, airRingPos.z, 0, 0, 0, 1, 0);
                    }
                }

                HitResult hit = user.raycast(512f, 1.0f, false);
                switch(hit.getType()) {
                    case ENTITY:
                        EntityHitResult entityHit = (EntityHitResult)hit;
                        if (entityHit.getEntity() instanceof LivingEntity livingEntity) {
                            livingEntity.timeUntilRegen = 0;
                            DamageSource damageSource = DamageTypeReg.shot(world, user, user);
                            livingEntity.damage(damageSource, (float)user.getAttributeValue(AttributeReg.RANGED_DAMAGE));
                        }
                        break;
                    case BLOCK:
                        BlockHitResult blockHit = (BlockHitResult)hit;
                        BlockPos pos = blockHit.getBlockPos();
                        BlockState state = world.getBlockState(pos);
                        Vec3d vec3d = blockHit.getPos();
                    
                        if (!state.isAir() && state.isSolid()) {
                            if ((state.isIn(ConventionalBlockTags.GLASS_BLOCKS) || state.isIn(ConventionalBlockTags.GLASS_PANES)) && MConfig.bulletsBreakGlass.get()) {
                                world.breakBlock(pos, false, user);
                            } else {
                                world.playSound(null,vec3d.x,vec3d.y,vec3d.z,state.getSoundGroup().getBreakSound(), SoundCategory.BLOCKS, 1.0F, 3.0F);

                                if (world instanceof ServerWorld serverWorld) {
                                    for (int i = 0; i < 4; ++i) {
                                        float speed = user.getRandom().nextFloat() - 0.5f;

                                        serverWorld.spawnParticles(new BlockStateParticleEffect(ParticleTypes.BLOCK, state), vec3d.x,vec3d.y,vec3d.z, 0, 0, 0, 1, speed);
                                    }
                                }
                            }
                        }
                        break;
                }
            }
        }

        if (user instanceof PlayerEntity player) {
            PacketByteBuf buf = PacketByteBufs.create();
            double recoil = user.getAttributeValue(AttributeReg.RECOIL);
            recoil *= MConfig.recoilMultiplier.get();
            recoil *= 1 - Math.min(EnchantmentHelper.getLevel(EnchantmentReg.STEADY_HANDS, stack) * 0.2d, 1.0d);
            buf.writeFloat((float)recoil);

            NetworkUtils.sendDataToClient(player, NetworkReg.RECOIL_CLIENT_PACKET_ID, buf);
        }

        gunItem.useBullet(stack);
        return fireRate;
    }

    public static List<Vec3d> calculatePositionBasedOnAngle(LivingEntity entity) {
        Vec3d front = Vec3d.fromPolar(entity.getPitch(), entity.getYaw());
        Arm arm = entity.getMainArm();
        boolean isRightHand = arm == Arm.RIGHT;
        Vec3d side = Vec3d.fromPolar(0, entity.getYaw() + (isRightHand ? 90 : -90));
        Vec3d down = Vec3d.fromPolar(entity.getPitch() + 90, entity.getYaw());

        Vec3d origin = new Vec3d(entity.getX(), entity.getEyeY(), entity.getZ());

        List<Vec3d> positions = new ArrayList<>();
        positions.add(origin);
        positions.add(front);
        positions.add(down);
        positions.add(side);
        return positions;
    }

    public static void spawnBullet(LivingEntity entity, Vec3d origin, Vec3d front, ItemStack stack, Vec2f spread,
                                   boolean fireBullet, int bulletLifespan) {
        World world = entity.getWorld();

        float bulletVelocity = 4f * (1 + Math.min(EnchantmentHelper.getLevel(EnchantmentReg.PENETRATING, stack) * 0.125f, 0.25f));

        bulletLifespan *= 1 - (EnchantmentHelper.getLevel(EnchantmentReg.PENETRATING, stack) * 0.125f);

        Vec3d bulletPos = origin.subtract(new Vec3d(0,0.2,0)).subtract(front.multiply((double)bulletVelocity + 0.3d));
        
        BulletEntity bullet = new BulletEntity(world, (float)entity.getAttributeValue(AttributeReg.RANGED_DAMAGE), bulletLifespan, entity, fireBullet, EnchantmentHelper.getLevel(EnchantmentReg.PENETRATING, stack));
        bullet.setPos(bulletPos.getX(), bulletPos.getY() - 0.1, bulletPos.getZ());
        bullet.setVelocity(entity, entity.getPitch() + spread.y, entity.getHeadYaw() + spread.x, 0.0F, bulletVelocity, 0.0F);

        bullet.velocityModified = true;
        bullet.velocityDirty = true;

        world.spawnEntity(bullet);
    }

    public static void sendAnimationPacket(PlayerEntity player) {
        PacketByteBuf buf = PacketByteBufs.empty();
        NetworkUtils.sendDataToClient(player, NetworkReg.SHOOT_ANIMATION_PACKET_ID, buf);
    }

    public static void playDistantGunFire(World world, Vec3d pos) {
        if (world.isClient()) {
            return;
        }

        world.getPlayers().forEach(player -> {
            double distance = player.squaredDistanceTo(pos);

            if (distance > 60) {
                float volume = (float) Math.max(1.0f - (distance / 400) / 100, 0);
                if (volume > 0) {
                    PacketByteBuf buf = PacketByteBufs.create();
                    buf.writeFloat(volume);
                    NetworkUtils.sendDataToClient(player, NetworkReg.SHOOT_CLIENT_PACKET_ID, buf);
                }
            }
        });
    }

    public static void fillMagazine(ItemStack mag, PlayerEntity player) {
        while (mag.getItem() instanceof MagazineItem) {
            ItemStack bulletStack = Bren.getItemFromPlayer(player, ItemReg.BULLET);

            if (bulletStack.isEmpty()) break;
            if (MagazineItem.getContents(mag) >= MagazineItem.getMaxCapacity(mag)) {
                break;
            } else {
               int i = MagazineItem.fillMagazine(mag, bulletStack.getCount());
               bulletStack.decrement(i);
            }
        }
    }
}
