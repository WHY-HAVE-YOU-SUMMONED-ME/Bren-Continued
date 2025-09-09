package nl.sniffiandros.bren.common.registry.custom.enchantment;

import net.minecraft.item.Item;
import nl.sniffiandros.bren.common.registry.custom.types.MagazineItem;

public class MagazineEnchantment extends BrenEnchantment {
    public MagazineEnchantment(Rarity weight) {
        super(weight);
    }

    @Override
    public boolean acceptsItem(Item item) {
        return item instanceof MagazineItem;
    }
}
