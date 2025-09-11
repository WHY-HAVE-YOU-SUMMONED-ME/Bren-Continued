package nl.sniffiandros.bren.common.registry.custom.types;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import nl.sniffiandros.bren.common.registry.ItemReg;
import nl.sniffiandros.bren.common.registry.SoundReg;

public class ShotgunItem extends BulletOnlyGun {
    public ShotgunItem(Settings settings, ToolMaterial material, float damage) {
        super(
            settings,
            material,
            new GunProperties()
                .rangedDamage(damage)
                .fireRate(22)
                .recoil(30f)
                .effectiveDistance(8f)
                .shootSound(SoundReg.ITEM_SHOTGUN_SHOOT, null),
            8
        );
    }

    @Override
    protected void onInsert(ItemStack stack, PlayerEntity player) {
        playSound(player, SoundReg.ITEM_SHOTGUN_SHELL_INSERT);
    }

    @Override
    protected void onFullyLoaded(ItemStack stack, PlayerEntity player) {
        playSound(player, SoundReg.ITEM_SHOTGUN_RACK);
    }

    @Override
    public float bulletTravelDistance() {
        return 8f;
    }

    @Override
    public float spread() {
        return 5f;
    }

    @Override
    public int bulletAmount() {
        return 5;
    }

    @Override
    public Item compatibleBullet() {
        return ItemReg.SHELL;
    }

    @Override
    public int reloadSpeed() {
        return 13;
    }

    @Override
    public CasingType ejectCasingType() {
        return CasingType.SHELL;
    }
}
