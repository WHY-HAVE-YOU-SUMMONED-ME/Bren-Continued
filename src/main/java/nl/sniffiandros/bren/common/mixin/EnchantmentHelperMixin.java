package nl.sniffiandros.bren.common.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.item.Item;
import nl.sniffiandros.bren.common.registry.custom.enchantment.BrenEnchantment;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EnchantmentHelper.class)
public abstract class EnchantmentHelperMixin {
    @Redirect(method = "getPossibleEntries", at = @At(value = "INVOKE", target = "Lnet/minecraft/enchantment/EnchantmentTarget;isAcceptableItem(Lnet/minecraft/item/Item;)Z"))
    private boolean modifyTooltipContents(EnchantmentTarget target, Item item, @Local Enchantment enchantment) {
        boolean returnValue = target.isAcceptableItem(item);

        if (enchantment instanceof BrenEnchantment brenEnchantment) {
            returnValue |= brenEnchantment.acceptsItem(item);
        }

        return returnValue;
    }
}
