package nl.sniffiandros.bren.common.registry.custom.enchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import nl.sniffiandros.bren.common.registry.custom.types.BulletOnlyGun;

public class OverflowEnchantment extends MagazineEnchantment {
    public OverflowEnchantment(Enchantment.Rarity weight) {
        super(weight);
    }

    public int getMinPower(int level) {
        return level * 2;
    }

    public int getMaxPower(int level) {
        return 8;
    }

    public boolean isTreasure() {
        return false;
    }

    public int getMaxLevel() {
        return 4;
    }

    @Override
    public boolean acceptsItem(Item item) {
        return super.acceptsItem(item) || item instanceof BulletOnlyGun;
    }
}
