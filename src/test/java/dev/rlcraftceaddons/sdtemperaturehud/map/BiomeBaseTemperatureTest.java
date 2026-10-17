package dev.rlcraftceaddons.sdtemperaturehud.map;

import org.junit.Test;
import static org.junit.Assert.*;

public class BiomeBaseTemperatureTest {
    @Test public void comparesSingleBiomesOnTheGameScale() {
        assertEquals(2, BiomeBaseTemperature.calculate(0f, 12f, 10f).getAsInt());
        assertEquals(12, BiomeBaseTemperature.calculate(.675f, 12f, 10f).getAsInt());
        assertEquals(22, BiomeBaseTemperature.calculate(1.35f, 12f, 10f).getAsInt());
    }
    @Test public void clampsClimateBeforeCalculationAndTruncatesTheFinalSum() {
        assertEquals(2, BiomeBaseTemperature.calculate(-9f, 12f, 10f).getAsInt());
        assertEquals(22, BiomeBaseTemperature.calculate(9f, 12f, 10f).getAsInt());
        assertEquals(12, BiomeBaseTemperature.calculate(.675f, 12.9f, 0f).getAsInt());
        assertEquals(0, BiomeBaseTemperature.calculate(0f, 12f, 100f).getAsInt());
        assertEquals(25, BiomeBaseTemperature.calculate(1.35f, 12f, 100f).getAsInt());
    }
    @Test public void rejectsNonFiniteInputsAndOverflow() {
        assertFalse(BiomeBaseTemperature.calculate(Float.NaN,12,10).isPresent());
        assertFalse(BiomeBaseTemperature.calculate(0,Float.POSITIVE_INFINITY,10).isPresent());
        assertFalse(BiomeBaseTemperature.calculate(0,12,Float.NaN).isPresent());
        assertFalse(BiomeBaseTemperature.calculate(1.35f,Float.MAX_VALUE,Float.MAX_VALUE).isPresent());
    }
    @Test public void preservesSimpleDifficultyFloatOrderingNearAnIntegerBoundary() {
        assertEquals(4,BiomeBaseTemperature.calculate(.2025f,12,10).getAsInt());
    }
}
