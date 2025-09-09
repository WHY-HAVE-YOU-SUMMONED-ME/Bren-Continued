package nl.sniffiandros.bren.common.utils;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalBlockTags;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.EntityAnimationS2CPacket;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;
import net.minecraft.world.World;
import nl.sniffiandros.bren.common.Bren;
import nl.sniffiandros.bren.common.config.MConfig;
import nl.sniffiandros.bren.common.entity.BulletEntity;
import nl.sniffiandros.bren.common.entity.IGunUser;
import nl.sniffiandros.bren.common.network.NetworkUtils;
import nl.sniffiandros.bren.common.registry.*;
import nl.sniffiandros.bren.common.registry.custom.types.*;

import java.util.ArrayList;
import java.util.List;


public class GunUtils {
    public static int fire(LivingEntity user) {
        World world = user.getWorld();
        ItemStack stack = user.getMainHandStack();

        if (!(stack.getItem() instanceof GunItem gunItem) || !(user instanceof IGunUser gunUser)) return 0;

        if (!gunUser.getGunState().equals(GunHelper.GunStates.NORMAL)) return 0;

        boolean silenced = EnchantmentHelper.getLevel(EnchantmentReg.SILENCED, stack) >= 1;
        boolean isRevolver = gunItem instanceof RevolverItem;

        world.playSound(null,
            user.getX(),
            user.getY(),
            user.getZ(),
            silenced ? gunItem.getSilentShootSound() : gunItem.getShootSound(),
            SoundCategory.PLAYERS, silenced ? 1f : 10f, 1f - (user.getRandom().nextFloat() - 0.5f) / 8
        );

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
            for (PlayerEntity p : world.getPlayers()) {
                NetworkUtils.sendShotEffect(
                    p,
                    origin.add(
                        side.multiply(isRevolver ? 0.7d : 0.4d)
                        .add(down.multiply(isRevolver ? 0.25d : 0.1d))
                    ),
                    front,
                    gunItem.ejectCasing()
                );
            }

            if (stack.getItem() instanceof RifleItem || MConfig.instantlyHit.get() && gunItem.bulletAmount() == 1) {
                if (world instanceof ServerWorld serverWorld) {
                    Vec3d airRingPos = front.multiply(8.0);
                    for (int i = 1; i < 3; i++) {
			            Vec3d temp = origin.add(airRingPos);
                        for (ServerPlayerEntity p : serverWorld.getPlayers()) {
                            serverWorld.spawnParticles(p, ParticleReg.AIR_RING_PARTICLE, false, temp.x, temp.y, temp.z, 0, 0, 0, 1, 0);
                        }
			            float f = 1 + (i * 0.6f);
                        airRingPos = airRingPos.multiply(f, f, f);
                    }
                }
                GunUtils.raytraceGunshot(user, EnchantmentHelper.getLevel(EnchantmentReg.PENETRATING, stack), getHeadshotDamageMultiplier(stack));
            } else {
                for (int i = 0; i < gunItem.bulletAmount(); ++i) {
                    float x = (user.getRandom().nextFloat() - 0.5f) * 2 * gunItem.spread();
                    float y = (user.getRandom().nextFloat() - 0.5f) * 2 * gunItem.spread();
                    GunUtils.spawnBullet(user, origin, front, stack, new Vec2f(x,y), gunItem.bulletLifespan());
                }
            }
        }

        if (user instanceof PlayerEntity player) {
            double recoil = GunUtils.getRecoil(player, stack);

            NetworkUtils.sendRecoil(player, (float)recoil);

            int recoilTicks = GunUtils.getRecoilTicks(recoil * (isRevolver ? 0.75d : 1d));
            
            NetworkUtils.sendShootAnimation(player, (byte)recoilTicks);
            gunUser.setGunTicks(recoilTicks);
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

    public static void spawnBullet(LivingEntity entity, Vec3d origin, Vec3d front, ItemStack stack, Vec2f spread, int bulletLifespan) {
        World world = entity.getWorld();

        int penetratingLevel = EnchantmentHelper.getLevel(EnchantmentReg.PENETRATING, stack);

        float bulletVelocity = 4f * (1 + (penetratingLevel / 3f));
        bulletLifespan *= (4f / bulletVelocity);

        Vec3d bulletPos = origin.subtract(new Vec3d(0d, 0.1d, 0d)).subtract(front.multiply(0.3f));
        
        BulletEntity bullet = new BulletEntity(world, bulletLifespan, entity, penetratingLevel, getHeadshotDamageMultiplier(stack));
        
        bullet.setPos(bulletPos.getX(), bulletPos.getY(), bulletPos.getZ());
        bullet.setVelocity(entity, entity.getPitch() + spread.y, entity.getHeadYaw() + spread.x, 0.0f, bulletVelocity, 0.0f);

        bullet.velocityModified = true;
        bullet.velocityDirty = true;

        world.spawnEntity(bullet);
    }

    public static void playDistantGunFire(World world, Vec3d pos) {
        if (world.isClient()) {
            return;
        }

        world.getPlayers().forEach(player -> {
            double distance = player.squaredDistanceTo(pos);

            if (distance > 128) {
                float volume = (float) Math.max(1.0f - (distance / 2000), 0);
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

    public static boolean processBulletImpact(LivingEntity user, HitResult hit, float headshotMultiplier) {
        World world = user.getWorld();

        if (hit instanceof EntityHitResult entityHit) {
            if (entityHit.getEntity() instanceof LivingEntity livingEntity) {
                if (headshotMultiplier >= 1f) {
                    double eyeHeight = livingEntity.getEyeY();
                    double headshotRadius = livingEntity.getBoundingBox().maxY - eyeHeight;
                    double hitY = entityHit.getPos().getY();

                    if (hitY >= (eyeHeight - headshotRadius) && hitY <= (eyeHeight + headshotRadius)) {
                        world.playSound(null, livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(), SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, user.getSoundCategory(), 2f, 1f);
                        if (world instanceof ServerWorld serverWorld) {
                            serverWorld.getChunkManager().sendToNearbyPlayers(user, new EntityAnimationS2CPacket(livingEntity, EntityAnimationS2CPacket.CRIT));
                        }
                    } else {
                        headshotMultiplier = 1f;
                    }
                } else {
                    headshotMultiplier = 1f;
                }

                livingEntity.damage(livingEntity.getDamageSources().create(DamageTypes.ARROW, user), ((float)user.getAttributeValue(AttributeReg.RANGED_DAMAGE)) * headshotMultiplier);
                livingEntity.timeUntilRegen = 0;
            }
            return true;
        } else if (hit instanceof BlockHitResult blockHit) {
            BlockPos pos = blockHit.getBlockPos();
            BlockState state = world.getBlockState(pos);
            Vec3d vec3d = blockHit.getPos();
            
            if (!state.isAir()) {
                if ((state.isIn(ConventionalBlockTags.GLASS_BLOCKS) || state.isIn(ConventionalBlockTags.GLASS_PANES)) && MConfig.bulletsBreakGlass.get()) {
                    world.breakBlock(pos, false, user);
                } else {
                    world.playSound(null, vec3d.x, vec3d.y, vec3d.z, state.getSoundGroup().getBreakSound(), SoundCategory.BLOCKS, 1.0F, 3.0F);

                    if (world instanceof ServerWorld serverWorld) {
                        for (int i = 0; i < 4; ++i) {
                            float speed = user.getRandom().nextFloat() - 0.5f;

                            serverWorld.spawnParticles(new BlockStateParticleEffect(ParticleTypes.BLOCK, state), vec3d.x,vec3d.y,vec3d.z, 0, 0, 0, 1, speed);
                        }
                    }
                }
                return true;
            }
        }
        return false;
    }

    public static boolean processBulletImpact(Entity user, HitResult hit, float headshotMultiplier) {
        if (user instanceof LivingEntity livingEntity) {
            return GunUtils.processBulletImpact(livingEntity, hit, headshotMultiplier);
        }
        return false;
    }

    public static void raytraceGunshot(Entity user, int penetratingLevel, float headshotMultiplier) {
        Vec3d rayStart = user.getCameraPosVec(1f);
        Vec3d rayDirection = user.getRotationVec(1f);
        Box box = user.getBoundingBox().stretch(rayDirection.multiply(128)).expand(1d, 1d, 1d);

        for (; penetratingLevel >= 0;) {
            Vec3d rayEnd = rayStart.add(rayDirection.multiply(128));
            Vec3d hitOffset = null;
        
            EntityHitResult entityHit = ProjectileUtil.raycast(user, rayStart, rayEnd, box, entity -> !entity.isSpectator() && entity.canHit(), 16384);

            if (GunUtils.processBulletImpact(user, entityHit, headshotMultiplier)) {
                hitOffset = entityHit.getPos().subtract(rayStart.add(rayDirection));
            } else {
                BlockHitResult blockHit = user.getWorld().raycast(new RaycastContext(rayStart, rayEnd, ShapeType.OUTLINE, FluidHandling.NONE, user));

                if (GunUtils.processBulletImpact(user, blockHit, headshotMultiplier)) {
                    hitOffset = blockHit.getPos().subtract(rayStart.add(rayDirection));
                }
            }

            if (hitOffset != null) {
                penetratingLevel--;
                rayStart = rayStart.add(hitOffset);
                box = box.offset(hitOffset);
            } else {
                break;
            }
        }
    }

    public static float getHeadshotDamageMultiplier(ItemStack stack) {
        if (stack.getItem() instanceof RevolverItem || EnchantmentHelper.getLevel(EnchantmentReg.SKULL_CRUSHER, stack) > 0) {
            return MConfig.strongHeadshotMultiplier.get();
        }
        return MConfig.headshotMultiplier.get();
    }

    public static double getRecoil(PlayerEntity player, ItemStack stack) {
        double recoil = player.getAttributeValue(AttributeReg.RECOIL);
            
        recoil *= 1 - Math.min(EnchantmentHelper.getLevel(EnchantmentReg.STEADY_HANDS, stack) * 0.125d, 1.0d);
        
        if (player.isInSneakingPose()) {
            recoil *= MConfig.sneakingRecoilMultiplier.get();
        }

        return recoil;
    }

    public static int getRecoilTicks(double recoil) {
        return (int)Math.ceil(12f * (1f - (1f / recoil)));
    }
}
