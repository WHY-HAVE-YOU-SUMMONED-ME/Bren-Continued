package nl.sniffiandros.bren.common.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemModels;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.world.World;
import nl.sniffiandros.bren.client.renderer.WeaponTickHolder;
import nl.sniffiandros.bren.common.Bren;
import nl.sniffiandros.bren.common.entity.IGunUser;
import nl.sniffiandros.bren.common.registry.custom.types.*;
import nl.sniffiandros.bren.common.utils.GunHelper;
import nl.sniffiandros.bren.common.utils.GunUtils;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public class ItemRendererMixin {
    @Shadow @Final private ItemModels models;

    @ModifyVariable(at = @At("HEAD"), method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/render/model/BakedModel;)V", argsOnly = true)
    private BakedModel editGuiModel(BakedModel defaultModel, ItemStack stack, ModelTransformationMode renderMode) {
        if (renderMode == ModelTransformationMode.GUI || renderMode == ModelTransformationMode.FIXED || renderMode == ModelTransformationMode.GROUND) {
            if (stack.getItem() instanceof GunItem gunItem && gunItem.hasGUIModel()) {
                return bakeGuiModel(stack);
            }
        }
        return defaultModel;
    }

    private BakedModel bakeGuiModel(ItemStack stack) {
        Identifier identifier = Registries.ITEM.getId(stack.getItem());
        String itemName = identifier.getPath();
        String formattedName = itemName.toLowerCase().replace(' ', '_');
        BakedModel bakedModel;
        if (!GunWithMagItem.hasMagazine(stack)) {
            bakedModel = this.models.getModelManager().getModel(new ModelIdentifier(Bren.MODID, formattedName + "_gui", "inventory"));
        } else {
            bakedModel = !GunWithMagItem.hasColorableMagazine(stack) ?
                    this.models.getModelManager().getModel(new ModelIdentifier(Bren.MODID, formattedName + "_with_magazine_gui", "inventory")) :
                    this.models.getModelManager().getModel(new ModelIdentifier(Bren.MODID, formattedName + "_with_clothed_magazine_gui", "inventory"));
        }
        return bakedModel;
    }


    @Inject(at = @At("HEAD"), method = "renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/world/World;III)V")
    private void render(LivingEntity entity, ItemStack item, ModelTransformationMode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, World world, int light, int overlay, int seed, CallbackInfo ci) {
        if (entity != null) {
            MinecraftClient minecraftClient = MinecraftClient.getInstance();
            float delta = minecraftClient.getTickDelta();

            if (item.getItem() instanceof GunItem gunItem) {
                if (entity instanceof IGunUser gunUser && entity instanceof PlayerEntity player) {
                    ItemCooldownManager cooldownManager = player.getItemCooldownManager();

                    float f1 = cooldownManager.getCooldownProgress(item.getItem(), delta);
                    f1 = Math.max(f1 - 0.15f, 0);

                    boolean reloading = gunUser.getGunState().equals(GunHelper.GunStates.RELOADING);
                    boolean isRevolver = gunItem instanceof RevolverItem;
                    boolean customMatrix = gunItem.applyCustomMatrix(entity, gunUser.getGunState(), matrices, item, f1, renderMode, leftHanded);

                    float f = 1 - WeaponTickHolder.getAnimationTimeLeft(delta);
                    float kick = !reloading ? (float)GunUtils.getRecoilTicks(GunUtils.getRecoil(player, item)) / 6f : 1f;

                    if (renderMode.isFirstPerson() && !customMatrix) {
                        float sin = (float)Math.sin((f * 2 - 0.5) * Math.PI) * 0.5f + 0.5f;
                        float sin2 = (float)Math.sin((f1 * 2 - 0.5) * Math.PI) * 0.5f + 0.5f;
                        float sin3 = reloading ? sin2 : (float)Math.sin(1 - f);

                        double d = (Math.sin(((float)entity.age + delta) / 2) * (reloading ? sin2 : f1)) * 30;

                        matrices.translate(0f, 0f, reloading ? 0 : (sin / 2f + f1 / 4f) / (kick * (isRevolver ? 2f : 0.5f)));
                        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float)(leftHanded ? -15 + d : 15 + d)));
                        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees((sin3 * 10) * kick * (isRevolver ? 2f : 0.5f)));
                    } else {
                        float z = Math.max((1 - f + f1) / 2, 0);
                        float f2 = reloading ? ((float)Math.sin((f1 * 2 - 0.5) * Math.PI) * 0.5f + 0.5f) / 3 : z;
                        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(f2 * 30 + (isRevolver ? 0 : 45)));

                        if (!isRevolver) {
                            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(leftHanded ? 10 : -10));
                            matrices.translate(0, -f2 / 4 + 0.25f, f2 / 8 - 0.25f);
                        }
                    }
                }
            }
        }
    }
}
