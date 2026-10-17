/*
 * Decompiled with CFR 0.152.
 */
package dev.rlcraftceaddons.sdtemperaturehud.temperature;

import dev.rlcraftceaddons.sdtemperaturehud.temperature.TemperatureSnapshot;
import dev.rlcraftceaddons.sdtemperaturehud.temperature.TemperatureSource;

public final class TemperatureSnapshotProvider {
    public static final int REFRESH_INTERVAL_TICKS = 10;
    private final TemperatureSource source;
    private TemperatureSnapshot snapshot;
    private int ticksUntilRefresh;

    public TemperatureSnapshotProvider(TemperatureSource source) {
        this.source = source;
    }

    public void tick(boolean active) {
        if (!active) {
            this.invalidate();
            return;
        }
        if (this.ticksUntilRefresh > 0) {
            --this.ticksUntilRefresh;
            return;
        }
        this.snapshot = this.source.read();
        this.ticksUntilRefresh = 9;
    }

    public void invalidate() {
        this.snapshot = null;
        this.ticksUntilRefresh = 0;
    }

    public TemperatureSnapshot getSnapshot() {
        return this.snapshot;
    }
}

