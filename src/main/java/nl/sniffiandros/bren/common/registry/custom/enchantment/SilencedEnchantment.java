package nl.sniffiandros.bren.common.registry.custom.enchantment;

import net.minecraft.item.Item;
import nl.sniffiandros.bren.common.registry.custom.types.BulletOnlyGun;

public class SilencedEnchantment extends GunEnchantment {
    public SilencedEnchantment(Rarity weight) {
        super(weight);
    }

    public int getMinPower(int level) {
        return 1;
    }

    public int getMaxPower(int level) {
        return 50;
    }

    public boolean isTreasure() {
        return false;
    }

    public int getMaxLevel() {
        return 1;
    }

    @Override
    public boolean acceptsItem(Item item) {
        return super.acceptsItem(item) && !(item instanceof BulletOnlyGun);
    }
}
