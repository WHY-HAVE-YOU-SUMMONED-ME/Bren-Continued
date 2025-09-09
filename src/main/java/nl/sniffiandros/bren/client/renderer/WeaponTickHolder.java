package nl.sniffiandros.bren.client.renderer;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.MathHelper;

public class WeaponTickHolder {
    private static int ticksLeft = 0;
    private static int lastTicks = 0;
    private static int ticks = 1;

    public static void tick(MinecraftClient client) {
        if (!client.isPaused()) {
            lastTicks = ticksLeft;
            ticksLeft = Math.max(0, --ticksLeft);
        }
    }

    public static void setTicks(int t) {
        ticks = ticksLeft = t;
    }

    public static float getAnimationTimeLeft(float tickDelta) {
        return MathHelper.lerp(tickDelta, (float)lastTicks, (float)ticksLeft) / ticks;
    }
}
