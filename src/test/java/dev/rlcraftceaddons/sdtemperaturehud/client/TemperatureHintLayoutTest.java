package dev.rlcraftceaddons.sdtemperaturehud.client;

import dev.rlcraftceaddons.sdtemperaturehud.client.layout.HudBounds;
import org.junit.Test;
import static org.junit.Assert.*;

public class TemperatureHintLayoutTest {
    @Test public void prefersAboveCompactWhenAnalysisIsClosed() {
        HudBounds result = TemperatureHintLayout.place(320, 240,
            new HudBounds(140, 180, 40, 10), null, 90, 24);
        assertEquals(151, result.getY());
        assertEquals(5, 180 - result.getY() - result.getHeight());
    }
    @Test public void avoidsAnalysisAndStaysInsideScreen() {
        HudBounds analysis = new HudBounds(120, 100, 90, 70);
        HudBounds result = TemperatureHintLayout.place(320, 240,
            new HudBounds(140, 180, 40, 10), analysis, 90, 24);
        assertFalse(TemperatureHintLayout.overlaps(result, analysis));
        assertTrue(result.getY() >= 0);
        assertTrue(result.getX() + result.getWidth() <= 320);
    }
    @Test public void nearTopUsesSideInsteadOfCoveringHud() {
        HudBounds compact = new HudBounds(100, 5, 40, 10);
        HudBounds result = TemperatureHintLayout.place(320, 240, compact, null, 90, 24);
        assertFalse(TemperatureHintLayout.overlaps(result, compact));
        assertTrue(result.getY() >= 0);
    }
}

