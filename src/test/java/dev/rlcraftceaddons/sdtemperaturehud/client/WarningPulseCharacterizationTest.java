package dev.rlcraftceaddons.sdtemperaturehud.client;

import dev.rlcraftceaddons.sdtemperaturehud.temperature.TemperatureWarningLevel;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class WarningPulseCharacterizationTest {
    @Test
    public void freezesSlowCautionAndFastCriticalPulsePeriods() {
        assertEquals(0.70, WarningPulse.opacity(TemperatureWarningLevel.CAUTION_COLD, 0L, true), 0.000001);
        assertEquals(1.00, WarningPulse.opacity(TemperatureWarningLevel.CAUTION_HOT, 800L, true), 0.000001);
        assertEquals(0.70, WarningPulse.opacity(TemperatureWarningLevel.CAUTION_HOT, 1600L, true), 0.000001);

        assertEquals(0.35, WarningPulse.opacity(TemperatureWarningLevel.CRITICAL_COLD, 0L, true), 0.000001);
        assertEquals(1.00, WarningPulse.opacity(TemperatureWarningLevel.CRITICAL_HOT, 250L, true), 0.000001);
        assertEquals(0.35, WarningPulse.opacity(TemperatureWarningLevel.CRITICAL_HOT, 500L, true), 0.000001);
    }

    @Test
    public void freezesStaticDisabledAndNormalStates() {
        assertEquals(1.0, WarningPulse.opacity(TemperatureWarningLevel.CRITICAL_HOT, 125L, false), 0.0);
        assertEquals(1.0, WarningPulse.opacity(TemperatureWarningLevel.NORMAL, 125L, true), 0.0);
        assertEquals(128, WarningPulse.scaleAlpha(255, 0.5));
        assertEquals(0, WarningPulse.scaleAlpha(-10, 1.0));
        assertEquals(255, WarningPulse.scaleAlpha(300, 1.0));
        assertEquals(0x8048C8FF, WarningPulse.withOpacity(0x48C8FF, 0.5));
    }
}
