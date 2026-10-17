package dev.rlcraftceaddons.sdtemperaturehud.client;

import dev.rlcraftceaddons.sdtemperaturehud.client.theme.HudColorPalette;
import dev.rlcraftceaddons.sdtemperaturehud.temperature.TemperatureSnapshot;
import dev.rlcraftceaddons.sdtemperaturehud.temperature.TemperatureWarning;
import dev.rlcraftceaddons.sdtemperaturehud.temperature.TemperatureWarningLevel;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class CustomizationRegressionTest {
    @Test
    public void unavailableDataDoesNotChangeValidCompactPresentationOrThresholds() {
        HudSettings settings = HudSettings.of(0, 0, 1.0, 214);
        HudColorPalette palette = settings.getColorPalette();
        TemperatureHudRenderer renderer = new TemperatureHudRenderer(
            null,
            new TemperatureHudRenderer.TextWidthProvider() {
                @Override
                public int width(String text) {
                    return text.length();
                }
            }
        );

        TemperatureHudRenderer.CompactPanel valid = renderer.measureCompact(
            TemperatureSnapshot.withTarget(10, 12),
            settings
        );
        TemperatureHudRenderer.CompactPanel unavailable = renderer.measureCompact(
            TemperatureSnapshot.unavailable(),
            settings
        );

        assertEquals("10", valid.getBodyText());
        assertEquals("\u2191", valid.getTrendText());
        assertEquals("12", valid.getTargetText());
        assertEquals(palette.getCold(), valid.getBodyColor());
        assertEquals(palette.getRising(), valid.getTrendColor());
        assertEquals(palette.getComfortable(), valid.getTargetColor());
        assertEquals("--", unavailable.getBodyText());
        assertNull(unavailable.getTrendText());
        assertNull(unavailable.getTargetText());
        assertEquals(
            TemperatureWarningLevel.CRITICAL_COLD,
            TemperatureWarning.classifyBodyTemperature(2)
        );
        assertEquals(
            TemperatureWarningLevel.NORMAL,
            TemperatureWarning.classifyBodyTemperature(6)
        );
        assertEquals(
            TemperatureWarningLevel.CRITICAL_HOT,
            TemperatureWarning.classifyBodyTemperature(23)
        );
    }
}
