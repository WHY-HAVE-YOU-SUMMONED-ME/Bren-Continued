package nl.sniffiandros.bren.common.entity;

import com.mojang.datafixers.types.templates.Tag;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalBlockTags;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.EndGatewayBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.passive.ParrotEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import nl.sniffiandros.bren.common.Bren;
import nl.sniffiandros.bren.common.config.MConfig;
import nl.sniffiandros.bren.common.registry.ParticleReg;
import nl.sniffiandros.bren.common.utils.GunUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

public class BulletEntity extends ProjectileEntity {
    private static final TrackedData<Integer> LIFESPAN = DataTracker.registerData(BulletEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private int penetratingLevel;
    private int collisionSteps;
    @Nullable
    private IntOpenHashSet damageBlacklist;

    public BulletEntity(EntityType<? extends BulletEntity> entityType, World world) {
        super(entityType, world);
    }

    public BulletEntity(World world, int lifespan, LivingEntity owner, int penetratingLevel) {
        super(Bren.BULLET, world);
        this.penetratingLevel = penetratingLevel;
        this.collisionSteps = (int)Math.ceil(MConfig.bulletCollisionSteps.get() * (1 + (penetratingLevel / 3f)));
        this.setLifespan(lifespan);
        this.setNoGravity(true);
        this.setOwner(owner);
    }

    protected void setLifespan(int lifespan) {
        this.dataTracker.set(LIFESPAN, lifespan);
    }

    public int getLifespan() {
        return this.dataTracker.get(LIFESPAN);
    }

    @Override
    protected void initDataTracker() {
        this.dataTracker.startTracking(LIFESPAN, 0);
    }

    public void tick() {
        super.tick();
        
        this.stepCollision();
        this.updateRotation();

        float h;

        if (this.isTouchingWater()) {
            h = 0.8f;
        } else {
            if (this.getWorld().isClient()) {
                this.getWorld().addParticle(ParticleReg.AIR_RING_PARTICLE, this.getX(), this.getY() + this.getHeight() / 2, this.getZ(), 0, 0, 0);
            }
            h = 0.99f;
        }

        Vec3d velocity = this.getVelocity().multiply(h);

        if (!this.hasNoGravity()) {
            velocity.subtract(0, (double)this.getGravity(), 0);
        }

        this.setVelocity(velocity);

        if (this.age >= this.getLifespan()) {
            this.discard();
            return;
        }
    }

    protected float getGravity() {
        return 0.03f;
    }

    protected void onEntityHit(EntityHitResult entityHitResult) {
        super.onEntityHit(entityHitResult);
        Entity entity = entityHitResult.getEntity();

        if (entity.equals(this.getOwner())) {
            return;
        }

        if (this.penetratingLevel > 0) {
            if (this.damageBlacklist == null) {
                this.damageBlacklist = new IntOpenHashSet(3);
            }
            if (!this.damageBlacklist.add(entity.getId())) {
                return;
            }
        }

        GunUtils.processBulletImpact(this.getOwner(), new EntityHitResult(entity, this.getPos().add(0d, this.getHeight() / 2, 0d)));
        this.tryDiscarding();
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        super.onBlockHit(blockHitResult);
        if (GunUtils.processBulletImpact(this.getOwner(), blockHitResult)) {
            this.tryDiscarding();
        }
    }

    private void tryDiscarding() {
        if (this.penetratingLevel >= 1) { 
            this.penetratingLevel--;
        } else {
            this.discard();
        }
    }

    private void stepCollision() {
        Vec3d velocityStep = this.getVelocity().multiply(1d / collisionSteps);

        for (int i = 0; i < collisionSteps; i++) {
            if (this.isRemoved()) break;

            HitResult hitResult = ProjectileUtil.getCollision(this, this::canHit);
            boolean bl = false;

            if (hitResult.getType() == HitResult.Type.BLOCK) {
                BlockPos blockPos = ((BlockHitResult)hitResult).getBlockPos();
                BlockState blockState = this.getWorld().getBlockState(blockPos);
                if (blockState.isOf(Blocks.NETHER_PORTAL)) {
                    this.setInNetherPortal(blockPos);
                    bl = true;
                } else if (blockState.isOf(Blocks.END_GATEWAY)) {
                    BlockEntity blockEntity = this.getWorld().getBlockEntity(blockPos);
                    if (blockEntity instanceof EndGatewayBlockEntity && EndGatewayBlockEntity.canTeleport(this)) {
                        EndGatewayBlockEntity.tryTeleportingEntity(this.getWorld(), blockPos, blockState, this, (EndGatewayBlockEntity)blockEntity);
                    }
                    bl = true;
                }
            }

            if (hitResult.getType() != HitResult.Type.MISS && !bl) {
                this.onCollision(hitResult);
                if (hitResult.getType() == HitResult.Type.ENTITY) {
                    break;
                }
            }
            this.checkBlockCollision();

            this.setPosition(this.getPos().add(velocityStep));
        }
    }
}
