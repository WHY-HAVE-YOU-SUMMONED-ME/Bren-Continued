package nl.sniffiandros.bren.common.registry.custom;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import net.minecraft.registry.tag.TagKey;

public class RifleItem extends GunWithMagItem {
    public RifleItem(Settings settings, ToolMaterial material, TagKey<Item> compatibleMagazines, GunProperties gunProperties) {
        super(settings, material, compatibleMagazines, gunProperties);
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return 2;
    }
}
