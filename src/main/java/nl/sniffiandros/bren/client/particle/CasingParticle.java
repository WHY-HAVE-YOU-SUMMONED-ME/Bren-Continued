package nl.sniffiandros.bren.client.particle;

import java.util.Optional;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import nl.sniffiandros.bren.common.registry.SoundReg;

public class CasingParticle extends AscendingParticle {
    protected boolean madeSound = false;
    protected boolean touchedGround = false;

    protected CasingParticle(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, float scaleMultiplier, SpriteProvider spriteProvider) {
        super(world, x, y, z, 0.1f, 0.1f, 0.1f, velocityX, velocityY, velocityZ, scaleMultiplier, spriteProvider, 1f, 32, 2f, true);
        this.angle = (float)(Math.PI * world.getRandom().nextFloat());
        this.setColor(2f, 2f, 2f);
    }

    @Override
    public void tick() {
        super.tick();
        
        this.prevAngle = this.angle;
        if (!this.onGround) {
            this.angle += 0.3f;
        }
        
        for (VoxelShape shape : this.world.getBlockCollisions(null, this.getBoundingBox().stretch(this.velocityX, this.velocityY, this.velocityZ))) {
            if (!shape.isEmpty()) {
                Vec3d nextPosition = new Vec3d(this.x + this.velocityX, this.y + this.velocityY, this.z + this.velocityZ);
                Optional<Vec3d> pointOfCollision = shape.getClosestPointTo(nextPosition);
                pointOfCollision.ifPresent(
                    pos -> {
                        Vec3d delta = pos.subtract(nextPosition);
                        if (delta.getX() > 0d) {
                            this.velocityX = -0.3d;
                        } else if (delta.getX() < 0d) {
                            this.velocityX = 0.3d;
                        }
                        if (delta.getZ() > 0d) {
                            this.velocityZ = -0.3d;
                        } else if (delta.getZ() < 0d) {
                            this.velocityZ = 0.3d;
                        }
                        if (delta.getY() > 0d) {
                            this.velocityY = -0.1d;
                        } else if (delta.getY() <= 0d) {
                            if (this.touchedGround) {
                                this.velocityX = 0d;
                                this.velocityY = 0d;
                                this.velocityZ = 0d;
                            } else {
                                this.velocityY = 0.3d;
                            }
                            if (delta.getY() == 0d) this.touchedGround = true;
                        }
                    }
                );
                if (!this.madeSound) {
                    this.world.playSound(this.x, this.y, this.z, SoundReg.PARTICLE_CASING_BOUNCE, SoundCategory.BLOCKS, 1f, 1f - (this.world.getRandom().nextFloat() - 0.5f) / 8, false);
                    this.madeSound = true;
                }
            }
        }
    }

    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Environment(EnvType.CLIENT)
    public static class Factory implements ParticleFactory<DefaultParticleType> {
        private final SpriteProvider spriteProvider;

        public Factory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        public Particle createParticle(DefaultParticleType defaultParticleType, ClientWorld clientWorld, double d, double e, double f, double g, double h, double i) {
            return new CasingParticle(clientWorld, d, e, f, g, h, i, 0.75f, this.spriteProvider);
        }
    }
}
