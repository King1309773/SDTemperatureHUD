package dev.rlcraftceaddons.sdtemperaturehud;

import dev.rlcraftceaddons.sdtemperaturehud.temperature.TemperatureSnapshot;
import dev.rlcraftceaddons.sdtemperaturehud.temperature.TemperatureSnapshotProvider;
import dev.rlcraftceaddons.sdtemperaturehud.temperature.TemperatureSource;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

public class SDTemperatureHudSnapshotTest {
    @Test
    public void exposesTheSnapshotFromTheProviderUsedByTheClientRuntime() throws Exception {
        Field field = SDTemperatureHud.class.getDeclaredField("snapshotProvider");
        field.setAccessible(true);
        Object previous = field.get(null);
        final TemperatureSnapshot expected = TemperatureSnapshot.withTarget(-123, 4567);
        TemperatureSnapshotProvider provider = new TemperatureSnapshotProvider(
            new TemperatureSource() {
                @Override
                public TemperatureSnapshot read() {
                    return expected;
                }
            }
        );

        try {
            SDTemperatureHud.installSnapshotProvider(provider);
            assertNull(SDTemperatureHud.currentSnapshot());

            provider.tick(true);

            assertSame(expected, SDTemperatureHud.currentSnapshot());
        } finally {
            field.set(null, previous);
        }
    }
}
