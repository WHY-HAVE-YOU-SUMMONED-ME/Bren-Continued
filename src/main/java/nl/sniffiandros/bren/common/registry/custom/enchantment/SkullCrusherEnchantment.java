package nl.sniffiandros.bren.common.registry.custom.enchantment;

import net.minecraft.item.Item;
import nl.sniffiandros.bren.common.registry.custom.types.RevolverItem;

public class SkullCrusherEnchantment extends GunEnchantment {
    public SkullCrusherEnchantment(Rarity weight) {
        super(weight);
    }

    public int getMinPower(int level) {
        return 0;
    }

    public int getMaxPower(int level) {
        return 1;
    }

    public boolean isTreasure() {
        return true;
    }

    public int getMaxLevel() {
        return 1;
    }

    @Override
    public boolean acceptsItem(Item item) {
        return super.acceptsItem(item) && !(item instanceof RevolverItem);
    }
}
