package nl.sniffiandros.bren.common.registry.custom.types;

import net.minecraft.item.ToolMaterial;
import nl.sniffiandros.bren.common.registry.*;

public class MachineGunItem extends GunWithMagItem {
    public MachineGunItem(Settings settings, ToolMaterial material, float damage) {
        super(
            settings,
            material,
            TagReg.MEDIUM_MAGAZINES,
            new GunProperties()
                .rangedDamage(damage)
                .fireRate(3)
                .recoil(10f)
                .effectiveDistance(60f)
                .shootSound(SoundReg.ITEM_MACHINE_GUN_SHOOT, SoundReg.ITEM_MACHINE_GUN_SHOOT_SILENCED)
        );
    }
}
