package nl.sniffiandros.bren.common.registry.custom.enchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public abstract class BrenEnchantment extends Enchantment {
    public BrenEnchantment(Rarity weight) {
        super(weight, EnchantmentTarget.VANISHABLE, new EquipmentSlot[] {EquipmentSlot.MAINHAND,EquipmentSlot.OFFHAND});
    }

    @Override
    public boolean isAcceptableItem(ItemStack stack) {
        return acceptsItem(stack.getItem());
    }

    public abstract boolean acceptsItem(Item item);
}
