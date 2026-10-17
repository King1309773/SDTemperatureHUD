/*
 * Decompiled with CFR 0.152.
 */
package dev.rlcraftceaddons.sdtemperaturehud.client;

import dev.rlcraftceaddons.sdtemperaturehud.temperature.TemperatureWarningLevel;

public final class WarningSoundController {
    private TemperatureWarningLevel previous = TemperatureWarningLevel.NORMAL;

    public Alert update(TemperatureWarningLevel current, boolean enabled) {
        boolean newlyEntered = current.isCritical() && current != this.previous;
        this.previous = current;
        if (!enabled || !newlyEntered) {
            return Alert.NONE;
        }
        return current == TemperatureWarningLevel.CRITICAL_COLD ? Alert.COLD : Alert.HOT;
    }

    public void reset() {
        this.previous = TemperatureWarningLevel.NORMAL;
    }

    public static enum Alert {
        NONE,
        COLD,
        HOT;

    }
}

