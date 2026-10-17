package dev.rlcraftceaddons.sdtemperaturehud.integration;

import com.charles445.simpledifficulty.api.temperature.TemperatureEnum;
import com.charles445.simpledifficulty.api.temperature.TemperatureRegistry;
import com.charles445.simpledifficulty.config.ModConfig;
import com.charles445.simpledifficulty.temperature.ModifierBiome;
import com.charles445.simpledifficulty.temperature.ModifierDefault;
import dev.rlcraftceaddons.sdtemperaturehud.map.BiomeBaseTemperature;
import dev.rlcraftceaddons.sdtemperaturehud.SDTemperatureHud;
import java.util.Map;
import java.util.OptionalInt;
import net.minecraft.world.biome.Biome;

public final class SimpleDifficultyBiomeRules {
    private long lastWarning;
    public OptionalInt temperatureFor(Biome biome) {
        if (biome == null || ModConfig.server == null || ModConfig.server.temperature == null)
            return OptionalInt.empty();
        OptionalInt result=calculate(biome.getDefaultTemperature(), TemperatureRegistry.modifiers,
            ModConfig.server.temperature.biomeMultiplier);
        if(!result.isPresent()) {
            long now=System.currentTimeMillis();
            if(now-lastWarning>=30000) {
                lastWarning=now;SDTemperatureHud.LOGGER.warn("Biome map labels skipped: unsupported active SimpleDifficulty rules or multiplier");
            }
        }
        return result;
    }

    static OptionalInt calculate(float climate, Map<String,?> rules, float multiplier) {
        if (rules == null || !exact(rules.get("Default"), ModifierDefault.class)
            || !exact(rules.get("Biome"), ModifierBiome.class)) return OptionalInt.empty();
        float baseline = (TemperatureEnum.NORMAL.getUpperBound()
            + TemperatureEnum.COLD.getUpperBound()) / 2;
        return BiomeBaseTemperature.calculate(climate, baseline, multiplier);
    }

    private static boolean exact(Object value, Class<?> expected) {
        return value != null && value.getClass() == expected;
    }
}
