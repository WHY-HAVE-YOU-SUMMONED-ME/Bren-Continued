package nl.sniffiandros.bren.common.registry.custom.enchantment;

public class PenetratingEnchantment extends GunEnchantment {
    public PenetratingEnchantment(Rarity weight) {
        super(weight);
    }

    public int getMinPower(int level) {
        return 0;
    }

    public int getMaxPower(int level) {
        return level * 2;
    }

    public boolean isTreasure() {
        return false;
    }

    public int getMaxLevel() {
        return 3;
    }
}
