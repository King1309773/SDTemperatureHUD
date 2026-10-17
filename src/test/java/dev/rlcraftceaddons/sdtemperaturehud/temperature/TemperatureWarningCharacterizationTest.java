package dev.rlcraftceaddons.sdtemperaturehud.temperature;

import org.junit.Test;

import static org.junit.Assert.assertSame;

public class TemperatureWarningCharacterizationTest {
    @Test
    public void freezesColdAndHotWarningBoundaries() {
        assertSame(TemperatureWarningLevel.CRITICAL_COLD, TemperatureWarning.classifyBodyTemperature(Integer.MIN_VALUE));
        assertSame(TemperatureWarningLevel.CRITICAL_COLD, TemperatureWarning.classifyBodyTemperature(2));
        assertSame(TemperatureWarningLevel.CAUTION_COLD, TemperatureWarning.classifyBodyTemperature(3));
        assertSame(TemperatureWarningLevel.CAUTION_COLD, TemperatureWarning.classifyBodyTemperature(5));
        assertSame(TemperatureWarningLevel.NORMAL, TemperatureWarning.classifyBodyTemperature(6));
        assertSame(TemperatureWarningLevel.NORMAL, TemperatureWarning.classifyBodyTemperature(19));
        assertSame(TemperatureWarningLevel.CAUTION_HOT, TemperatureWarning.classifyBodyTemperature(20));
        assertSame(TemperatureWarningLevel.CAUTION_HOT, TemperatureWarning.classifyBodyTemperature(22));
        assertSame(TemperatureWarningLevel.CRITICAL_HOT, TemperatureWarning.classifyBodyTemperature(23));
        assertSame(TemperatureWarningLevel.CRITICAL_HOT, TemperatureWarning.classifyBodyTemperature(Integer.MAX_VALUE));
    }
}
