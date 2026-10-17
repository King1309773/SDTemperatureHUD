/*
 * Decompiled with CFR 0.152.
 */
package dev.rlcraftceaddons.sdtemperaturehud.client;

import dev.rlcraftceaddons.sdtemperaturehud.client.WarningPulse;
import dev.rlcraftceaddons.sdtemperaturehud.temperature.TemperatureWarningLevel;

public final class WarningPanelStyle {
    private final double opacity;

    private WarningPanelStyle(double opacity) {
        this.opacity = opacity;
    }

    public static WarningPanelStyle forLevel(TemperatureWarningLevel level, long timeMs, boolean enabled) {
        return new WarningPanelStyle(WarningPulse.opacity(level, timeMs, enabled));
    }

    public int backgroundColor(int baseAlpha, int rgb) {
        return WarningPulse.withOpacity(rgb, baseAlpha, this.opacity);
    }

    public int textColor(int rgb) {
        return WarningPulse.withOpacity(rgb, this.opacity);
    }
}
