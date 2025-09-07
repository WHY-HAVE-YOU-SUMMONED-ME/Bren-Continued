package nl.sniffiandros.bren.client.renderer;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;

public class RecoilSys {
    private static float recoilAmount = 0;
    private static float sideRecoilDirection = 0;
    private static float targetRecoil = 0;
    private static float recoilProgress = 0;

    public static void shotEvent(PlayerEntity player, float recoil) {
        targetRecoil = recoil;
        recoilAmount = 0f;
        sideRecoilDirection = (player.getRandom().nextFloat() - 0.5f) * 0.5f;
        recoilProgress = 1f;
    }

    public static void tick() {
        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity player = client.player;

        if (player == null) return;

        float pitch = player.getPitch();
        float yaw = player.getYaw();
        
        if ((recoilProgress -= 0.2f) <= 0f) return;

        recoilAmount = MathHelper.lerp(recoilProgress, recoilAmount * client.getTickDelta(), targetRecoil) * recoilProgress;

        sideRecoilDirection *= recoilProgress;

        player.setPitch(pitch - recoilAmount);
        player.setYaw(yaw - recoilAmount * sideRecoilDirection);
        player.prevPitch = pitch;
    }
}
