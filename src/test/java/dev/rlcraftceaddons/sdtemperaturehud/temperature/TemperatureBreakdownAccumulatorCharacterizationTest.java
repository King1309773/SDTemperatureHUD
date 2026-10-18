package dev.rlcraftceaddons.sdtemperaturehud.temperature;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class TemperatureBreakdownAccumulatorCharacterizationTest {
    @Test
    public void freezesCumulativeRoundingAndOtherRemainderRules() {
        TemperatureBreakdownAccumulator accumulator = new TemperatureBreakdownAccumulator();
        accumulator.add(TemperatureModifierCategory.ENVIRONMENT, 1.4f);
        accumulator.add(TemperatureModifierCategory.ENVIRONMENT, 1.4f);
        accumulator.add(TemperatureModifierCategory.EQUIPMENT, 1.4f);
        accumulator.add(TemperatureModifierCategory.SEASON, -0.6f);
        accumulator.add(TemperatureModifierCategory.HEAT_SOURCE, 2.9f);
        accumulator.add(null, 100.0f);

        TemperatureBreakdown breakdown = accumulator.finish(9);

        assertEquals(2, breakdown.getEnvironment());
        assertEquals(2, breakdown.getEquipment());
        assertEquals(-1, breakdown.getSeason());
        assertEquals(3, breakdown.getHeatSource());
        assertEquals(3, breakdown.getOther());
        assertEquals(9, breakdown.getTarget());
        assertEquals(9, breakdown.sum());
    }

    @Test
    public void freezesSimpleDifficultyModifierCategoryMerging() {
        assertSame(TemperatureModifierCategory.ENVIRONMENT, TemperatureModifierCategory.forName("Biome"));
        assertSame(TemperatureModifierCategory.EQUIPMENT, TemperatureModifierCategory.forName("AUWDYNAMIC"));
        assertSame(TemperatureModifierCategory.SEASON, TemperatureModifierCategory.forName("sereneseasons"));
        assertSame(TemperatureModifierCategory.HEAT_SOURCE, TemperatureModifierCategory.forName("blockstiles"));
        assertSame(TemperatureModifierCategory.OTHER, TemperatureModifierCategory.forName("unknown"));
        assertSame(TemperatureModifierCategory.OTHER, TemperatureModifierCategory.forName(null));
    }

    @Test
    public void sprintHasItsOwnHintSourceButKeepsTheOriginalAnalysisTotal() {
        assertSame(TemperatureModifierCategory.MOVEMENT, TemperatureModifierCategory.forName("sPrInT"));
        TemperatureBreakdownAccumulator accumulator = new TemperatureBreakdownAccumulator();
        accumulator.add(TemperatureModifierCategory.ENVIRONMENT, 10);
        accumulator.add(TemperatureModifierCategory.forName("Sprint"), 3);
        accumulator.add(TemperatureModifierCategory.OTHER, -7);
        TemperatureBreakdown result = accumulator.finish(6);
        assertEquals(-4, result.getOther());
        assertEquals(6, result.sum());
        assertEquals(3, result.getMovementForHints());
        assertEquals(-7, result.getOtherForHints());
    }

    @Test
    public void targetClampingDoesNotLeakMovementIntoOtherHints() {
        TemperatureBreakdownAccumulator accumulator = new TemperatureBreakdownAccumulator();
        accumulator.add(TemperatureModifierCategory.ENVIRONMENT, 30);
        accumulator.add(TemperatureModifierCategory.MOVEMENT, 3);
        TemperatureBreakdown result = accumulator.finish(25);
        assertEquals(-5, result.getOther());
        assertEquals(25, result.sum());
        assertEquals(3, result.getMovementForHints());
        assertEquals(0, result.getOtherForHints());
    }

    @Test
    public void oldBreakdownConstructorKeepsItsHintContributions() {
        TemperatureBreakdown result = new TemperatureBreakdown(10, 0, 0, 0, -2, 8);
        assertEquals(0, result.getMovementForHints());
        assertEquals(-2, result.getOtherForHints());
    }
}
