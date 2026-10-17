/*
 * Decompiled with CFR 0.152.
 */
package dev.rlcraftceaddons.sdtemperaturehud.temperature;

public final class TemperatureBreakdown {
    private final int environment;
    private final int equipment;
    private final int season;
    private final int heatSource;
    private final int other;
    private final int target;

    public TemperatureBreakdown(int environment, int equipment, int season, int heatSource, int other, int target) {
        this.environment = environment;
        this.equipment = equipment;
        this.season = season;
        this.heatSource = heatSource;
        this.other = other;
        this.target = target;
    }

    public int getEnvironment() {
        return this.environment;
    }

    public int getEquipment() {
        return this.equipment;
    }

    public int getSeason() {
        return this.season;
    }

    public int getHeatSource() {
        return this.heatSource;
    }

    public int getOther() {
        return this.other;
    }

    public int getTarget() {
        return this.target;
    }

    public int sum() {
        return this.environment + this.equipment + this.season + this.heatSource + this.other;
    }
}

