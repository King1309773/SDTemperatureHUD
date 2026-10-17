package dev.rlcraftceaddons.sdtemperaturehud.client;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class HudLayoutAndSettingsCharacterizationTest {
    @Test
    public void freezesTokenLayoutAndScaledPanelAnchoring() {
        assertEquals(19, HudLayout.panelWidth(4, 5, 6));
        assertEquals(2, HudLayout.bodyX());
        assertEquals(11, HudLayout.arrowX(9));
        assertEquals(16, HudLayout.targetX(9, 5));
        assertEquals(80.0, HudLayout.panelX(200, 10, 40, 1.5), 0.000001);
        assertEquals(96.0, HudLayout.panelY(180, -4, 10, 1.5), 0.000001);
    }

    @Test
    public void freezesOffsetsScaleAlphaAndFeatureBoundaries() {
        HudSettings low = HudSettings.of(900, -700, 0.25, -1);
        assertEquals(500, low.getXOffset());
        assertEquals(-500, low.getYOffset());
        assertEquals(0.5, low.getScale(), 0.0);
        assertEquals(0, low.getBackgroundAlpha());
        assertTrue(low.isWarningFlashEnabled());
        assertFalse(low.isWarningSoundEnabled());
        assertTrue(low.isAnalysisEnabled());

        HudSettings high = HudSettings.of(-900, 700, 3.0, 999, false, true, false);
        assertEquals(-500, high.getXOffset());
        assertEquals(500, high.getYOffset());
        assertEquals(2.0, high.getScale(), 0.0);
        assertEquals(255, high.getBackgroundAlpha());
        assertFalse(high.isWarningFlashEnabled());
        assertTrue(high.isWarningSoundEnabled());
        assertFalse(high.isAnalysisEnabled());

        assertEquals(1.0, HudSettings.of(0, 0, Double.NaN, 128).getScale(), 0.0);
    }
}
