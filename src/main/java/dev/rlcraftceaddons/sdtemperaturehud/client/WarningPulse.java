/*
 * Decompiled with CFR 0.152.
 */
package dev.rlcraftceaddons.sdtemperaturehud.client;

import dev.rlcraftceaddons.sdtemperaturehud.temperature.TemperatureWarningLevel;

public final class WarningPulse {
    private static final long CAUTION_PERIOD_MS = 1600L;
    private static final long CRITICAL_PERIOD_MS = 500L;
    private static final double CAUTION_MIN_OPACITY = 0.7;
    private static final double CRITICAL_MIN_OPACITY = 0.35;

    private WarningPulse() {
    }

    public static double opacity(TemperatureWarningLevel level, long timeMs, boolean enabled) {
        if (!enabled || level == TemperatureWarningLevel.NORMAL) {
            return 1.0;
        }
        long periodMs = level.isCritical() ? 500L : 1600L;
        double minimum = level.isCritical() ? 0.35 : 0.7;
        long elapsedMs = Math.floorMod(timeMs, periodMs);
        double wave = (1.0 - Math.cos(Math.PI * 2 * (double)elapsedMs / (double)periodMs)) / 2.0;
        return minimum + (1.0 - minimum) * wave;
    }

    public static int scaleAlpha(int alpha, double opacity) {
        long scaled = Math.round((double)alpha * opacity);
        return (int)Math.max(0L, Math.min(255L, scaled));
    }

    public static int withOpacity(int color, double opacity) {
        return withOpacity(color, 255, opacity);
    }

    public static int withOpacity(int color, int baseAlpha, double opacity) {
        return WarningPulse.scaleAlpha(baseAlpha, opacity) << 24 | color & 0xFFFFFF;
    }
}
