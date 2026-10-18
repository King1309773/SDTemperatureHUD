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
    private final int movementForHints;
    private final int otherForHints;

    public TemperatureBreakdown(int environment, int equipment, int season, int heatSource, int other, int target) {
        this(environment, equipment, season, heatSource, other, target, 0, other);
    }

    TemperatureBreakdown(int environment, int equipment, int season, int heatSource, int other, int target,
                         int movementForHints, int otherForHints) {
        this.environment = environment;
        this.equipment = equipment;
        this.season = season;
        this.heatSource = heatSource;
        this.other = other;
        this.target = target;
        this.movementForHints = movementForHints;
        this.otherForHints = otherForHints;
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

    /** Raw modifier contributions for hints; not the clamping/rounding residual shown by getOther(). */
    public int getMovementForHints() {
        return this.movementForHints;
    }

    public int getOtherForHints() {
        return this.otherForHints;
    }

    public int sum() {
        return this.environment + this.equipment + this.season + this.heatSource + this.other;
    }
}

