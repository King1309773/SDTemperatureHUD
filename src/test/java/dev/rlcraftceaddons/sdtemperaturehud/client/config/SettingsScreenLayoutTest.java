package dev.rlcraftceaddons.sdtemperaturehud.client.config;

import org.junit.Test;
import static org.junit.Assert.*;

public class SettingsScreenLayoutTest {
    @Test public void controlsFitMinimumGuiWithoutCrossingFooter() {
        SettingsScreenLayout layout = SettingsScreenLayout.forScreen(320, 240);
        assertTrue(layout.left >= 0);
        assertTrue(layout.left + layout.width <= 320);
        assertTrue(layout.rows() >= 4);
        assertTrue(layout.rowY(layout.rows() - 1) + 20 < layout.footerY);
        assertTrue(layout.controlWidth >= 140);
        assertTrue(layout.previewWidth >= 130);
    }
    @Test public void largerScreensReducePaginationInsteadOfMakingControlsHuge() {
        SettingsScreenLayout small = SettingsScreenLayout.forScreen(320, 240);
        SettingsScreenLayout large = SettingsScreenLayout.forScreen(1280, 720);
        assertTrue(large.width <= 620);
        assertTrue(large.rows() > small.rows());
        assertEquals(0, large.clampPage(5, 3));
        assertEquals(2, small.clampPage(10, 9));
    }
}

