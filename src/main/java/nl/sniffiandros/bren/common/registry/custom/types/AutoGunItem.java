package nl.sniffiandros.bren.common.registry.custom.types;

import net.minecraft.item.ToolMaterial;
import nl.sniffiandros.bren.common.registry.*;

public class AutoGunItem extends GunWithMagItem {
    public AutoGunItem(Settings settings, ToolMaterial material, float damage) {
        super(
            settings,
            material,
            TagReg.MEDIUM_MAGAZINES,
            new GunProperties()
                .rangedDamage(damage)
                .fireRate(5)
                .recoil(10f)
                .shootSound(SoundReg.ITEM_AUTO_GUN_SHOOT, SoundReg.ITEM_AUTO_GUN_SHOOT_SILENCED)
        );
    }
}
