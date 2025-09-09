package nl.sniffiandros.bren.common.registry.custom.types;

import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.world.World;
import nl.sniffiandros.bren.common.entity.IGunUser;
import nl.sniffiandros.bren.common.registry.SoundReg;
import nl.sniffiandros.bren.common.registry.custom.PoseType;
import nl.sniffiandros.bren.common.utils.GunHelper;

public class RevolverItem extends BulletOnlyGun {
    public RevolverItem(Settings settings, ToolMaterial material, float damage) {
        super(
            settings,
            material,
            new GunProperties()
                .rangedDamage(damage)
                .fireRate(6)
                .recoil(6f)
                .shootSound(SoundReg.ITEM_REVOLVER_SHOOT, null),
            6
        );
    }

    @Override
    public PoseType holdingPose() {
        return PoseType.REVOLVER;
    }

    @Override
    public int reloadSpeed() {
        return 16;
    }

    @Override
    public boolean renderOnBack() {
        return false;
    }

    @Override
    public boolean applyCustomMatrix(LivingEntity entity, GunHelper.GunStates state, MatrixStack matrices, ItemStack stack, float cooldownProgress, ModelTransformationMode renderMode, boolean leftHanded) {
        if (entity instanceof IGunUser gunUser && cooldownProgress > 0 && gunUser.getGunState().equals(GunHelper.GunStates.RELOADING)) {
            float sin = (float)Math.sin(((cooldownProgress * 2) - 0.5) * Math.PI) * 0.5f + 0.5f;
            
            if (renderMode.isFirstPerson()) {
                matrices.multiply(RotationAxis.POSITIVE_X.rotation(cooldownProgress * 15f));
                return true;
            } else {
                matrices.translate(0, sin / 2, 0);

                matrices.multiply(RotationAxis.NEGATIVE_Z.rotationDegrees((leftHanded ? 90 + 25 : 65) + (sin * 180)));
                matrices.multiply(RotationAxis.NEGATIVE_X.rotation(cooldownProgress * 15));
            }
        }

        return false;
    }

    @Override
    public boolean hasGUIModel() {
        return false;
    }

    @Override
    public boolean ejectCasing() {
        return false;
    }

    @Override
    protected void afterInserted(ItemStack stack, PlayerEntity player) {
        playSound(player, SoundReg.ITEM_REVOLVER_RELOAD);
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);

        if (entity instanceof IGunUser gunUser && entity instanceof PlayerEntity player) {
            ItemCooldownManager cooldownManager = player.getItemCooldownManager();
            if (selected) {
                float f = cooldownManager.getCooldownProgress(stack.getItem(), 1);

                if (gunUser.getGunState() == GunHelper.GunStates.RELOADING) {
                    if (f == 0.5f) {
                        playSound(player, SoundReg.ITEM_REVOLVER_BULLET_INSERT);
                    }
                    if (player.age % 5 == 0) {
                        playSound(player, SoundReg.ITEM_REVOLVER_SPINNING);
                    }
                }
            }
        }
    }
}
