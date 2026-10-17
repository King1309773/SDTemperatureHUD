package dev.rlcraftceaddons.sdtemperaturehud.client;

import dev.rlcraftceaddons.sdtemperaturehud.temperature.TemperatureSnapshot;
import dev.rlcraftceaddons.sdtemperaturehud.temperature.TemperatureWarningLevel;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class UnavailableWarningPolicyTest {
    @Test
    public void unavailableDataDoesNotProduceAWarningSoundLevel() {
        assertNull(ClientEventHandler.warningLevelForSound(TemperatureSnapshot.unavailable()));
        assertEquals(
            TemperatureWarningLevel.CRITICAL_COLD,
            ClientEventHandler.warningLevelForSound(TemperatureSnapshot.bodyOnly(2))
        );
    }
}
