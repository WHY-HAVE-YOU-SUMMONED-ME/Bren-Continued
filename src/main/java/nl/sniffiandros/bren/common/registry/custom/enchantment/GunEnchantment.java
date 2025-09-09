package nl.sniffiandros.bren.common.registry.custom.enchantment;

import net.minecraft.item.Item;
import nl.sniffiandros.bren.common.registry.custom.types.GunItem;

public class GunEnchantment extends BrenEnchantment {
    public GunEnchantment(Rarity weight) {
        super(weight);
    }

    @Override
    public boolean acceptsItem(Item item) {
        return item instanceof GunItem;
    }
}
