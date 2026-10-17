/*
 * Decompiled with CFR 0.152.
 */
package dev.rlcraftceaddons.sdtemperaturehud.temperature;

public enum TemperatureWarningLevel {
    CRITICAL_COLD,
    CAUTION_COLD,
    NORMAL,
    CAUTION_HOT,
    CRITICAL_HOT;


    public boolean isCritical() {
        return this == CRITICAL_COLD || this == CRITICAL_HOT;
    }
}

