package nl.sniffiandros.bren.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.sound.SoundCategory;
import nl.sniffiandros.bren.common.registry.SoundReg;

public class CasingParticle extends AscendingParticle {
    protected float bounce = 0.9f;
    protected boolean madeSound = false;

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
        } else {
            this.velocityY += this.bounce;
            this.bounce = 0;
            if (!this.madeSound) {
                this.madeSound = true;

                this.world.playSound(this.x, this.y, this.z, SoundReg.PARTICLE_CASING_BOUNCE, SoundCategory.BLOCKS, 1f, 1f - (this.world.getRandom().nextFloat() - 0.5f) / 8, false);
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
