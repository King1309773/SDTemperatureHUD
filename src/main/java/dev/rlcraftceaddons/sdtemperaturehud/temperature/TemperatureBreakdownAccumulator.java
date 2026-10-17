/*
 * Decompiled with CFR 0.152.
 */
package dev.rlcraftceaddons.sdtemperaturehud.temperature;

import dev.rlcraftceaddons.sdtemperaturehud.temperature.TemperatureBreakdown;
import dev.rlcraftceaddons.sdtemperaturehud.temperature.TemperatureModifierCategory;
import java.util.EnumMap;
import java.util.Map;

public final class TemperatureBreakdownAccumulator {
    private final Map<TemperatureModifierCategory, Float> totals = new EnumMap<TemperatureModifierCategory, Float>(TemperatureModifierCategory.class);

    public TemperatureBreakdownAccumulator() {
        for (TemperatureModifierCategory category : TemperatureModifierCategory.values()) {
            this.totals.put(category, Float.valueOf(0.0f));
        }
    }

    public void add(TemperatureModifierCategory category, float influence) {
        TemperatureModifierCategory safeCategory = category == null ? TemperatureModifierCategory.OTHER : category;
        this.totals.put(safeCategory, Float.valueOf(this.totals.get((Object)safeCategory).floatValue() + influence));
    }

    public TemperatureBreakdown finish(int target) {
        int environment;
        float cumulative = this.totals.get((Object)TemperatureModifierCategory.ENVIRONMENT).floatValue();
        int previous = environment = (int)cumulative;
        int current = (int)(cumulative += this.totals.get((Object)TemperatureModifierCategory.EQUIPMENT).floatValue());
        int equipment = current - previous;
        previous = current;
        current = (int)(cumulative += this.totals.get((Object)TemperatureModifierCategory.SEASON).floatValue());
        int season = current - previous;
        previous = current;
        current = (int)(cumulative += this.totals.get((Object)TemperatureModifierCategory.HEAT_SOURCE).floatValue());
        int heatSource = current - previous;
        int other = target - current;
        return new TemperatureBreakdown(environment, equipment, season, heatSource, other, target);
    }
}

