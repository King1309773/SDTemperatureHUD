package dev.rlcraftceaddons.sdtemperaturehud.temperature;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TemperatureUnavailableTest {
    @Test
    public void unavailableSnapshotHasAnExplicitNonNumericPresentation() {
        TemperatureSnapshot snapshot = TemperatureSnapshot.unavailable();

        assertFalse(snapshot.isAvailable());
        assertFalse(snapshot.hasTargetTemperature());
        assertFalse(snapshot.hasBreakdown());
        assertEquals("--", TemperaturePresentation.from(snapshot).getBodyText());
    }

    @Test
    public void validSnapshotPresentationAndFactoriesRemainCompatible() {
        TemperatureSnapshot snapshot = TemperatureSnapshot.withTarget(12, 14);

        assertTrue(snapshot.isAvailable());
        assertEquals(12, snapshot.getBodyTemperature());
        assertEquals(14, snapshot.getTargetTemperature());
        assertEquals("12", TemperaturePresentation.from(snapshot).getBodyText());
    }
}
