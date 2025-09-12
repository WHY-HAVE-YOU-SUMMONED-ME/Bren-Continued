package nl.sniffiandros.bren.common.registry.custom.types;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import net.minecraft.world.World;
import nl.sniffiandros.bren.common.Bren;
import nl.sniffiandros.bren.common.config.MConfig;
import nl.sniffiandros.bren.common.entity.IGunUser;
import nl.sniffiandros.bren.common.registry.EnchantmentReg;
import nl.sniffiandros.bren.common.registry.ItemReg;
import nl.sniffiandros.bren.common.registry.custom.enchantment.AutofillEnchantment;

public class BulletOnlyGun extends GunItem {
    public int capacity;

    public BulletOnlyGun(Settings settings, ToolMaterial material, GunProperties gunProperties, int capacity) {
        super(settings, material, gunProperties);
        this.capacity = capacity;
    }

    public int getMaxCapacity(ItemStack stack) {
        return Math.round(capacity * Math.max(1f, 1f + (EnchantmentHelper.getLevel(EnchantmentReg.OVERFLOW, stack)) / 4f) * MConfig.ammoCapacityMultiplier.get());
    }

    @Override
    public int getContents(ItemStack stack) {
        if (stack.getNbt() != null) {
            return stack.getNbt().getInt("Contents");
        }
        return 0;
    }

    public void addContent(ItemStack stack) {
        if (stack.getItem() instanceof BulletOnlyGun) {
            stack.getOrCreateNbt().putInt("Contents", getContents(stack) + 1);
        }
    }

    @Override
    public void useBullet(ItemStack stack) {
        if (stack.getItem() instanceof BulletOnlyGun) {
            stack.getOrCreateNbt().putInt("Contents", getContents(stack) - 1);
        }
    }

    @Override
    public void onReload(PlayerEntity player) {
        ItemStack stack = player.getMainHandStack();
        ItemCooldownManager cooldownManager = player.getItemCooldownManager();

        if (player instanceof IGunUser gunUser && !cooldownManager.isCoolingDown(stack.getItem())) {
            ItemStack bullets = Bren.getItemFromPlayer(player, compatibleBullet());
            if (bullets.isEmpty() || getContents(stack) >= getMaxCapacity(stack)) {
                return;
            }

            if (!gunUser.canReload()) {
                return;
            }

            startCoolingDown(player, this.reloadSpeed(), true, this.getClass());
            onInsert(stack, player);
        }
    }

    protected void onInsert(ItemStack stack, PlayerEntity player) {}

    protected void afterInserted(ItemStack stack, PlayerEntity player) {}

    protected void onFullyLoaded(ItemStack stack, PlayerEntity player) {}

    public Item compatibleBullet() {
        return ItemReg.BULLET;
    }

    @Override
    public void reloadTick(ItemStack stack, World world, PlayerEntity player, IGunUser gunUser) {
        ItemCooldownManager cooldownManager = player.getItemCooldownManager();

        if (!cooldownManager.isCoolingDown(stack.getItem()) && getContents(stack) <= getMaxCapacity(stack)) {
            if (getContents(stack) < getMaxCapacity(stack)) {
                ItemStack bullets = Bren.getItemFromPlayer(player, compatibleBullet());

                bullets.decrement(1);
                addContent(stack);

                afterInserted(stack, player);
            }
        } else if (cooldownManager.getCooldownProgress(stack.getItem(), 1f) == 0f && getContents(stack) == getMaxCapacity(stack) - 1) {
            onFullyLoaded(stack, player);
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);
        if (entity instanceof PlayerEntity player && EnchantmentHelper.getLevel(EnchantmentReg.AUTOFILL, stack) > 0) {
            AutofillEnchantment.insert(stack, player);
        }
    }
}
