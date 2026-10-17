package dev.rlcraftceaddons.sdtemperaturehud.map;

import java.util.OptionalInt;

/** A reference value for one biome, not the player's environmental target. */
public final class BiomeBaseTemperature {
    private BiomeBaseTemperature() {}

    public static OptionalInt calculate(float climate, float baseline, float multiplier) {
        if (!Float.isFinite(climate) || !Float.isFinite(baseline) || !Float.isFinite(multiplier))
            return OptionalInt.empty();
        float normalized = Math.max(0f, Math.min(1.35f, climate)) / 1.35f;
        float value = baseline + (normalized * 2f - 1f) * multiplier;
        if (!Float.isFinite(value)) return OptionalInt.empty();
        return OptionalInt.of(Math.max(0, Math.min(25, (int)value)));
    }
}
