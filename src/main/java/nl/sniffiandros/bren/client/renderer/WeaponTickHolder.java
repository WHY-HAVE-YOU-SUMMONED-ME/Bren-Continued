package nl.sniffiandros.bren.client.renderer;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.MathHelper;

public class WeaponTickHolder {
    private static int ticks;
    private static int lastTicks;

    public static void tick(MinecraftClient client) {
        if (!client.isPaused()) {
            lastTicks = ticks;
            ticks = Math.max(0, --ticks);
        }
    }

    public static void setTicks(int t) {
        ticks = t;
    }

    public static float getAnimationTicks(float tickDelta) {
        return MathHelper.lerp(tickDelta, (float)lastTicks, (float)ticks);
    }

}
