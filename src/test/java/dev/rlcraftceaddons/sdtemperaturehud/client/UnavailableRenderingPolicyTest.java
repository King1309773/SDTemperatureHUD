package dev.rlcraftceaddons.sdtemperaturehud.client;

import dev.rlcraftceaddons.sdtemperaturehud.temperature.TemperatureSnapshot;
import dev.rlcraftceaddons.sdtemperaturehud.temperature.TemperatureWarningLevel;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class UnavailableRenderingPolicyTest {
    private final HudSettings settings = HudSettings.of(0, 0, 1.0, 214);
    private final TemperatureHudRenderer renderer = new TemperatureHudRenderer(
        null,
        new TemperatureHudRenderer.TextWidthProvider() {
            @Override
            public int width(String text) {
                return text.length();
            }
        }
    );

    @Test
    public void unavailableDataStillMeasuresACompactPlaceholder() {
        TemperatureHudRenderer.CompactPanel panel = this.renderer.measureCompact(
            TemperatureSnapshot.unavailable(),
            this.settings
        );

        assertEquals("--", panel.getBodyText());
        assertNull(panel.getTrendText());
        assertNull(panel.getTargetText());
        assertEquals(HudLayout.panelWidth(2), panel.getWidth());
        assertEquals(this.settings.getColorPalette().getText(), panel.getBodyColor());
        assertEquals(
            TemperatureWarningLevel.NORMAL,
            TemperatureHudRenderer.warningLevelFor(TemperatureSnapshot.unavailable())
        );
    }

    @Test
    public void unavailableSnapshotNeverProducesAnalysisRows() {
        assertNull(this.renderer.measureAnalysis(TemperatureSnapshot.unavailable(), this.settings));
    }
}
