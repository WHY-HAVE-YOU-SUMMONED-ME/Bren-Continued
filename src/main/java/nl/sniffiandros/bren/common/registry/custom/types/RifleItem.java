package nl.sniffiandros.bren.common.registry.custom.types;

import net.minecraft.item.ToolMaterial;
import nl.sniffiandros.bren.common.registry.SoundReg;
import nl.sniffiandros.bren.common.registry.TagReg;

public class RifleItem extends GunWithMagItem {
    public RifleItem(Settings settings, ToolMaterial material, float damage) {
        super(
            settings,
            material,
            TagReg.SHORT_MAGAZINES,
            new GunProperties()
                .rangedDamage(damage)
                .fireRate(30)
                .recoil(22f)
                .shootSound(SoundReg.ITEM_RIFLE_SHOOT, SoundReg.ITEM_RIFLE_SHOOT_SILENCED)
        );
    }
}
