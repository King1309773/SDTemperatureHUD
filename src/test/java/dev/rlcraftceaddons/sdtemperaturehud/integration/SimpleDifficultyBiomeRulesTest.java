package dev.rlcraftceaddons.sdtemperaturehud.integration;

import com.charles445.simpledifficulty.temperature.ModifierDefault;
import com.charles445.simpledifficulty.temperature.ModifierBiome;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

public class SimpleDifficultyBiomeRulesTest {
    @Test public void usesOnlyRecognizedActiveRulesAndLiveMultiplier() {
        Map<String,Object> rules = new HashMap<String,Object>();
        rules.put("Default", new ModifierDefault());
        rules.put("Biome", new ModifierBiome());
        assertEquals(22, SimpleDifficultyBiomeRules.calculate(1.35f,rules,10).getAsInt());
        assertEquals(12, SimpleDifficultyBiomeRules.calculate(1.35f,rules,0).getAsInt());
        rules.put("Biome", new Object());
        assertFalse(SimpleDifficultyBiomeRules.calculate(1.35f,rules,10).isPresent());
        rules.put("Biome", new ModifierBiome());
        assertEquals(22, SimpleDifficultyBiomeRules.calculate(1.35f,rules,10).getAsInt());
    }
    @Test public void missingOrRenamedRulesAreUnknownNotNormalTemperature() {
        assertFalse(SimpleDifficultyBiomeRules.calculate(0,null,10).isPresent());
        Map<String,Object> rules = new HashMap<String,Object>();
        rules.put("default",new ModifierDefault()); rules.put("Biome",new ModifierBiome());
        assertFalse(SimpleDifficultyBiomeRules.calculate(0,rules,10).isPresent());
        rules.put("Default",new ModifierDefault() {});
        assertFalse(SimpleDifficultyBiomeRules.calculate(0,rules,10).isPresent());
    }
}
