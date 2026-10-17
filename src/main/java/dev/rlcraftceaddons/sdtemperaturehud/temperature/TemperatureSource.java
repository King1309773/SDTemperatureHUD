/*
 * Decompiled with CFR 0.152.
 */
package dev.rlcraftceaddons.sdtemperaturehud.temperature;

import dev.rlcraftceaddons.sdtemperaturehud.temperature.TemperatureSnapshot;

public interface TemperatureSource {
    public TemperatureSnapshot read();
}

