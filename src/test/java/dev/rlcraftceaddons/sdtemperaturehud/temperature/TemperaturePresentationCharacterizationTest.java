package dev.rlcraftceaddons.sdtemperaturehud.temperature;

import dev.rlcraftceaddons.sdtemperaturehud.client.theme.HudColorPalette;
import dev.rlcraftceaddons.sdtemperaturehud.client.theme.HudThemePreset;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class TemperaturePresentationCharacterizationTest {
    private static final HudColorPalette CLASSIC = HudThemePreset.CLASSIC.getPalette();

    @Test
    public void freezesBodyTemperatureColorBands() {
        assertEquals(4770047, TemperaturePresentation.colorFor(5, CLASSIC));
        assertEquals(8576511, TemperaturePresentation.colorFor(6, CLASSIC));
        assertEquals(8576511, TemperaturePresentation.colorFor(10, CLASSIC));
        assertEquals(9433261, TemperaturePresentation.colorFor(11, CLASSIC));
        assertEquals(9433261, TemperaturePresentation.colorFor(14, CLASSIC));
        assertEquals(16757581, TemperaturePresentation.colorFor(15, CLASSIC));
        assertEquals(16757581, TemperaturePresentation.colorFor(19, CLASSIC));
        assertEquals(16734533, TemperaturePresentation.colorFor(20, CLASSIC));
    }

    @Test
    public void freezesTrendDirectionGlyphAndColorRules() {
        assertTrend(TemperaturePresentation.Trend.UP, 12, 11, "\u2191", 16765028);
        assertTrend(TemperaturePresentation.Trend.DOWN, 10, 11, "\u2193", 8379647);
        assertTrend(TemperaturePresentation.Trend.STEADY, 11, 11, "\u2192", 14014940);
    }

    private static void assertTrend(TemperaturePresentation.Trend expected,
                                    int target,
                                    int body,
                                    String glyph,
                                    int color) {
        TemperaturePresentation.Trend trend = TemperaturePresentation.trendFor(target, body);
        assertSame(expected, trend);
        assertEquals(glyph, TemperaturePresentation.glyphFor(trend));
        assertEquals(color, TemperaturePresentation.trendColor(trend, CLASSIC));
    }
}
