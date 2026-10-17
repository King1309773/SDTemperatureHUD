package dev.rlcraftceaddons.sdtemperaturehud.client.editor;

import dev.rlcraftceaddons.sdtemperaturehud.client.layout.HudLayoutState;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HudLayoutDefaultsTest {
    @Test
    public void computesCenteredNonOverlappingDefaultsForBothPanels() {
        HudLayoutDefaults defaults = HudLayoutDefaults.forScreen(
            320,
            180,
            60,
            10,
            100,
            70
        );

        assertEquals(new HudLayoutState(130.0 / 320.0, 105.0 / 180.0, 1.0), defaults.compact());
        assertEquals(new HudLayoutState(110.0 / 320.0, 33.0 / 180.0, 1.0), defaults.analysis());
    }
}
