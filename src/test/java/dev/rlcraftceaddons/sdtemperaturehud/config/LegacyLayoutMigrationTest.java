package dev.rlcraftceaddons.sdtemperaturehud.config;

import dev.rlcraftceaddons.sdtemperaturehud.client.layout.HudLayoutState;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LegacyLayoutMigrationTest {
    @Test
    public void convertsLegacyOffsetsToNormalizedTopLeftCoordinates() {
        HudLayoutState migrated = LegacyLayoutMigration.fromLegacyOffsets(
            12,
            -8,
            1.25,
            320,
            180,
            140.0,
            158.0
        );

        assertEquals(152.0 / 320.0, migrated.getNormalizedX(), 0.0);
        assertEquals(150.0 / 180.0, migrated.getNormalizedY(), 0.0);
        assertEquals(1.25, migrated.getScale(), 0.0);
    }

    @Test
    public void clampsMigratedScaleThroughLayoutMath() {
        assertEquals(
            0.5,
            LegacyLayoutMigration.fromLegacyOffsets(0, 0, 0.1, 320, 180, 140.0, 158.0).getScale(),
            0.0
        );
        assertEquals(
            2.0,
            LegacyLayoutMigration.fromLegacyOffsets(0, 0, 9.0, 320, 180, 140.0, 158.0).getScale(),
            0.0
        );
    }

    @Test
    public void invalidDimensionsOrAnchorsReturnSafeDefault() {
        HudLayoutState expected = new HudLayoutState(0.5, 0.5, 1.0);

        assertEquals(expected, LegacyLayoutMigration.fromLegacyOffsets(12, -8, 1.5, 0, 180, 140.0, 158.0));
        assertEquals(expected, LegacyLayoutMigration.fromLegacyOffsets(12, -8, 1.5, 320, -1, 140.0, 158.0));
        assertEquals(expected, LegacyLayoutMigration.fromLegacyOffsets(12, -8, 1.5, 320, 180, Double.NaN, 158.0));
        assertEquals(expected, LegacyLayoutMigration.fromLegacyOffsets(12, -8, 1.5, 320, 180, 140.0, Double.POSITIVE_INFINITY));
    }

    @Test
    public void reportsMigrationCapabilityAndPendingVersions() {
        assertTrue(LegacyLayoutMigration.canMigrate(1, 1));
        assertFalse(LegacyLayoutMigration.canMigrate(0, 1));
        assertFalse(LegacyLayoutMigration.canMigrate(1, 0));
        assertFalse(LegacyLayoutMigration.canMigrate(-1, 1));

        assertTrue(LegacyLayoutMigration.isPending(Integer.MIN_VALUE));
        assertTrue(LegacyLayoutMigration.isPending(0));
        assertFalse(LegacyLayoutMigration.isPending(1));
        assertFalse(LegacyLayoutMigration.isPending(2));
    }

    @Test
    public void panelMigrationMatchesTheExactLegacyRendererPlacement() {
        LegacyLayoutMigration.PanelLayouts layouts = LegacyLayoutMigration.forPanels(
            37, -41, 1.25, 320, 200, 64, 12, 112, 32
        );

        assertEquals(new HudLayoutState(157.0 / 320.0, 79.0 / 200.0, 1.25), layouts.compact());
        assertEquals(new HudLayoutState(127.0 / 320.0, 37.0 / 200.0, 1.25), layouts.analysis());
    }

    @Test
    public void panelMigrationSupportsMinimumScreenAndFallsBackForInvalidDimensions() {
        LegacyLayoutMigration.PanelLayouts edge = LegacyLayoutMigration.forPanels(
            0, 0, 1.0, 1, 1, 1, 1, 1, 1
        );
        assertEquals(new HudLayoutState(0.0, -65.0, 1.0), edge.compact());
        assertEquals(new HudLayoutState(0.0, -68.0, 1.0), edge.analysis());

        HudLayoutState safe = new HudLayoutState(0.5, 0.5, 1.0);
        LegacyLayoutMigration.PanelLayouts invalidScreen = LegacyLayoutMigration.forPanels(
            12, -8, 1.5, 0, 180, 64, 12, 112, 32
        );
        LegacyLayoutMigration.PanelLayouts invalidPanel = LegacyLayoutMigration.forPanels(
            12, -8, 1.5, 320, 180, 0, 12, 112, 32
        );
        assertEquals(safe, invalidScreen.compact());
        assertEquals(safe, invalidScreen.analysis());
        assertEquals(safe, invalidPanel.compact());
        assertEquals(safe, invalidPanel.analysis());
    }
}
