package nl.sniffiandros.bren.common.registry.custom.enchantment;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import nl.sniffiandros.bren.common.Bren;
import nl.sniffiandros.bren.common.registry.EnchantmentReg;
import nl.sniffiandros.bren.common.registry.ItemReg;
import nl.sniffiandros.bren.common.registry.custom.types.BulletOnlyGun;
import nl.sniffiandros.bren.common.registry.custom.types.MagazineItem;

public class AutofillEnchantment extends MagazineEnchantment {
    public AutofillEnchantment(Rarity weight) {
        super(weight);
    }

    public int getMinPower(int level) {
        return (level - 1) * 5;
    }

    public int getMaxPower(int level) {
        return getMinPower(level) + 50;
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

    public static void insert(ItemStack stack, PlayerEntity player) {
        int cooldown = stack.getItem() instanceof BulletOnlyGun ? 192 : 72;

        if (player.getWorld().isClient() || player.age % (cooldown / EnchantmentHelper.getLevel(EnchantmentReg.AUTOFILL, stack)) > 0) {
            return;
        }
        BulletOnlyGun gun = null;

        if (stack.getItem() instanceof BulletOnlyGun) {
            gun = (BulletOnlyGun)stack.getItem();
            if (gun.getContents(stack) >= gun.getMaxCapacity(stack)) {
                return;
            }
        }

        if (gun == null && !(stack.getItem() instanceof MagazineItem)) return;

        ItemStack bullet = Bren.getItemFromPlayer(player, gun == null ? ItemReg.BULLET : gun.compatibleBullet());
        if (!bullet.isEmpty()) {
            if (gun == null) {
                if (MagazineItem.isFull(stack)) {
                    return;
                }
                MagazineItem.fillMagazine(stack, 1);
            } else {
                gun.addContent(stack);
            }
            bullet.decrement(1);
        }
    }
}
