package nl.sniffiandros.bren.client.renderer;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;

public class RecoilSys {
    private static float targetRecoil = 0;
    private static float sideRecoil = 0;
    private static int recoilTicks = 0;
    private static int lastRecoilTicks = 0;

    public static void shotEvent(PlayerEntity player, float recoil) {
        targetRecoil = recoil;
        sideRecoil = player.getRandom().nextFloat() - 0.5f;
        recoilTicks = 3;
    }

    public static void render() {
        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity player = client.player;

        if (player == null) return;

        float progress = MathHelper.lerp(client.getTickDelta(), lastRecoilTicks, recoilTicks);

        float pitch = player.getPitch();
        float yaw = player.getYaw();

        float recoil = progress * targetRecoil * client.getLastFrameDuration();

        player.setPitch(pitch - recoil);
        player.setYaw(yaw - (recoil * sideRecoil));
    }

    public static void tick(MinecraftClient client) {
        lastRecoilTicks = recoilTicks;
        recoilTicks = Math.max(0, recoilTicks - 1);
    }
}
