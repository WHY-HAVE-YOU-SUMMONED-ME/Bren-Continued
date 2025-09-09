package nl.sniffiandros.bren.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Arm;
import nl.sniffiandros.bren.common.entity.IGunUser;
import nl.sniffiandros.bren.common.utils.GunHelper;

public class GunEntityModelAnimator {
    public static void oneArm(LivingEntity livingEntity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch,
                              ModelPart leftArm, ModelPart rightArm, ModelPart head, float gunAmount) {
        if (livingEntity instanceof IGunUser && !livingEntity.isSleeping()) {
            boolean isLeftHanded = livingEntity.getMainArm().equals(Arm.LEFT);

            float h_pi = 1.570796f;
            float p = headPitch * 0.01745329f;
            float y = netHeadYaw * 0.01745329f;

            ModelPart arm = isLeftHanded ? leftArm : rightArm;

            arm.yaw = y;
            arm.pitch = p - h_pi;
        }
    }

    public static void revolver(LivingEntity livingEntity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch,
                              ModelPart leftArm, ModelPart rightArm, ModelPart head, float gunAmount) {
        if (livingEntity instanceof IGunUser gunUser && !livingEntity.isSleeping()) {
            boolean reloading = gunUser.getGunState().equals(GunHelper.GunStates.RELOADING);

            boolean isLeftHanded = livingEntity.getMainArm().equals(Arm.LEFT);
            ModelPart arm = isLeftHanded ? leftArm : rightArm;

            float f = 0;
            float f1 = 1.570796f;

            if (livingEntity instanceof PlayerEntity player) {
                MinecraftClient client = MinecraftClient.getInstance();

                f = player.getItemCooldownManager().getCooldownProgress(player.getMainHandStack().getItem(), client.getTickDelta());
            }

            float p = headPitch * 0.01745329f;
            float y = netHeadYaw * 0.01745329f;

            arm.pitch = p - f1;
            arm.yaw = y;

            if (reloading) {
                float sin = (float)Math.sin((f * 2 - 0.5) * Math.PI) * 0.5f + 0.5f;

                arm.pitch += (float)(Math.sin(f * 15f) * 0.08726646d) - sin;
                arm.yaw += (float)(Math.sin(f * 15f) * 0.08726646d);
            }
        }
    }

    public static void angles(LivingEntity livingEntity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch,
    ModelPart leftArm, ModelPart rightArm, ModelPart head, float gunAmount) {

        if (livingEntity instanceof IGunUser gunUser && !livingEntity.isSleeping()) {
            boolean reloading = gunUser.getGunState().equals(GunHelper.GunStates.RELOADING);
            float kick = 2.5f;

            gunAmount = Math.max(gunAmount - 0.15f, 0);
            float f = (((float)gunUser.getGunTicks() / 16) + gunAmount) / 2;
            float f1 = (float) (Math.sin(f) / Math.PI) * (kick / 2);

            boolean isLeftHanded = livingEntity.getMainArm().equals(Arm.LEFT);
            ModelPart arm = isLeftHanded ? leftArm : rightArm;
            ModelPart otherArm = !isLeftHanded ? leftArm : rightArm;

            float l = isLeftHanded ? -1 : 1;

            float p = headPitch * 0.01745329f;
            float y = netHeadYaw * 0.01745329f;

            float f2 = f1 * kick / 2;

            float fr = ((float) Math.sin((gunAmount * 2 - 0.5) * Math.PI) * 0.5f + 0.5f);
            float f3 = reloading ? fr / 4 : f2;
            float f4 = reloading ? (isLeftHanded ? -fr / 4 : fr / 4) : f2 * l;

            arm.yaw = isLeftHanded ? y + 0.7853982f : y - 0.7853982f;
            arm.pitch = 0.2181662f + p + f3 / 2;
            arm.roll += f4;

            otherArm.pitch = -0.6981317f + p / 3 - f3 / 2 - (reloading ? fr : 0);
            otherArm.yaw = (isLeftHanded ? -1.090831f - y : 1.090831f + y) + (p / 2) * l + f3 / 3;

            head.yaw = y - 0.7853982f * l;
        }
    }
}
