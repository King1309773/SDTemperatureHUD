package dev.rlcraftceaddons.sdtemperaturehud.temperature;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;

public class TemperatureSnapshotProviderRecoveryTest {
    @Test
    public void laterSuccessfulPollReplacesUnavailableSnapshotWithoutRestart() {
        final TemperatureSnapshot valid = TemperatureSnapshot.withTarget(9, 13);
        TemperatureSnapshotProvider provider = new TemperatureSnapshotProvider(
            new TemperatureSource() {
                private int reads;

                @Override
                public TemperatureSnapshot read() {
                    return this.reads++ == 0 ? TemperatureSnapshot.unavailable() : valid;
                }
            }
        );

        provider.tick(true);
        assertFalse(provider.getSnapshot().isAvailable());
        for (int tick = 0; tick < TemperatureSnapshotProvider.REFRESH_INTERVAL_TICKS; ++tick) {
            provider.tick(true);
        }
        assertSame(valid, provider.getSnapshot());
    }
}
