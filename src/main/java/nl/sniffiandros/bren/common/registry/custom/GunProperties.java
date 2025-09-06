package nl.sniffiandros.bren.common.registry.custom;

import net.minecraft.sound.SoundEvent;
import nl.sniffiandros.bren.common.config.MConfig;

public class GunProperties {
    float recoil;
    float rangedDamage;
    int fireRate;
    SoundEvent sound;
    SoundEvent silentSound;

    public GunProperties() {}

    public GunProperties rangedDamage(float damage) {
        this.rangedDamage = damage * MConfig.damageMultiplier.get();
        return this;
    }

    public GunProperties fireRate(int rate) {
        this.fireRate = rate;
        return this;
    }

    public GunProperties recoil(float recoil) {
        this.recoil = recoil * MConfig.recoilMultiplier.get();
        return this;
    }
    
    public GunProperties shootSound(SoundEvent sound, SoundEvent silent) {
        this.sound = sound;
        this.silentSound = silent;
        return this;
    }
}
