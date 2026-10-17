/*
 * Decompiled with CFR 0.152.
 */
package dev.rlcraftceaddons.sdtemperaturehud.temperature;

import dev.rlcraftceaddons.sdtemperaturehud.temperature.TemperatureWarningLevel;

public final class TemperatureWarning {
    private TemperatureWarning() {
    }

    public static TemperatureWarningLevel classifyBodyTemperature(int temperature) {
        return levelFor(temperature);
    }

    private static TemperatureWarningLevel levelFor(int temperature) {
        if (temperature <= 2) {
            return TemperatureWarningLevel.CRITICAL_COLD;
        }
        if (temperature <= 5) {
            return TemperatureWarningLevel.CAUTION_COLD;
        }
        if (temperature <= 19) {
            return TemperatureWarningLevel.NORMAL;
        }
        if (temperature <= 22) {
            return TemperatureWarningLevel.CAUTION_HOT;
        }
        return TemperatureWarningLevel.CRITICAL_HOT;
    }
}
