package nl.sniffiandros.bren.client.renderer;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;

public class RecoilSys {
    private static float appliedRecoil = 0;
    private static float targetRecoil = 0;
    private static float sideRecoil = 0;

    public static void shotEvent(PlayerEntity player, float recoil) {
        sideRecoil = (player.getRandom().nextFloat() - 0.5f) * 0.5f;
        targetRecoil = recoil;
        appliedRecoil = 0f;
    }

    public static void render() {
        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity player = client.player;

        if (player == null || appliedRecoil >= targetRecoil) return;

        float recoil = targetRecoil * client.getLastFrameDuration() * 0.5f;

        appliedRecoil += recoil;

        recoil += Math.min(0, targetRecoil - appliedRecoil);

        player.setPitch(player.getPitch() - recoil);
        player.setYaw(player.getYaw() - (recoil * sideRecoil));
    }
}
