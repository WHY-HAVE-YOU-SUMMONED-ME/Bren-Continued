package nl.sniffiandros.bren.common.registry.custom.enchantment;

import net.minecraft.item.ItemStack;
import nl.sniffiandros.bren.common.registry.custom.types.ShotgunItem;

public class FireLanceEnchantment extends GunEnchantment {
    public FireLanceEnchantment(Rarity weight) {
        super(weight);
    }

    public int getMinPower(int level) {
        return 0;
    }

    public int getMaxPower(int level) {
        return 4;
    }

    public boolean isTreasure() {
        return true;
    }

    public int getMaxLevel() {
        return 1;
    }

    @Override
    public boolean isAcceptableItem(ItemStack stack) {
        return stack.getItem() instanceof ShotgunItem;
    }
}
