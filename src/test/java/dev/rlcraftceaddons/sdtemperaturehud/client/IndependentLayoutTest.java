package dev.rlcraftceaddons.sdtemperaturehud.client;

import dev.rlcraftceaddons.sdtemperaturehud.client.layout.HudBounds;
import dev.rlcraftceaddons.sdtemperaturehud.client.layout.HudLayoutMath;
import dev.rlcraftceaddons.sdtemperaturehud.client.layout.HudLayoutState;
import dev.rlcraftceaddons.sdtemperaturehud.config.HudConfig;
import dev.rlcraftceaddons.sdtemperaturehud.config.HudConfigResetPolicy;
import dev.rlcraftceaddons.sdtemperaturehud.config.LegacyLayoutMigration;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class IndependentLayoutTest {
    @Test
    public void compactAndAnalysisUseIndependentNormalizedPositionsAndScales() {
        HudLayoutState compact = new HudLayoutState(0.4, 0.8, 1.0);
        HudLayoutState analysis = new HudLayoutState(0.1, 0.2, 1.5);

        HudBounds compactAtSmallResolution = HudLayout.panelBounds(compact, 320, 180, 40, 10);
        HudBounds analysisAtSmallResolution = TemperatureAnalysisLayout.panelBounds(
            analysis,
            320,
            180,
            80,
            60
        );
        HudLayoutState compactNormalized = HudLayoutMath.normalized(
            compactAtSmallResolution,
            320,
            180,
            compact.getScale()
        );
        HudBounds compactAtLargeResolution = HudLayout.panelBounds(
            compactNormalized,
            640,
            360,
            40,
            10
        );
        HudLayoutState analysisNormalized = HudLayoutMath.normalized(
            analysisAtSmallResolution,
            320,
            180,
            analysis.getScale()
        );
        HudBounds analysisAtLargeResolution = TemperatureAnalysisLayout.panelBounds(
            analysisNormalized,
            640,
            360,
            80,
            60
        );

        assertEquals(0.4, compactNormalized.getNormalizedX(), 0.5 / 320.0);
        assertEquals(0.8, compactNormalized.getNormalizedY(), 0.5 / 180.0);
        assertEquals(0.4, (double)compactAtLargeResolution.getX() / 640.0, 0.5 / 640.0);
        assertEquals(0.8, (double)compactAtLargeResolution.getY() / 360.0, 0.5 / 360.0);
        assertEquals(0.1, analysisNormalized.getNormalizedX(), 0.5 / 320.0);
        assertEquals(0.2, analysisNormalized.getNormalizedY(), 0.5 / 180.0);
        assertEquals(1.5, analysisNormalized.getScale(), 0.0);
        assertEquals(32, analysisAtSmallResolution.getX());
        assertEquals(36, analysisAtSmallResolution.getY());
        assertEquals(120, analysisAtSmallResolution.getWidth());
        assertEquals(90, analysisAtSmallResolution.getHeight());
        assertEquals(64, analysisAtLargeResolution.getX());
        assertEquals(72, analysisAtLargeResolution.getY());
        assertEquals(120, analysisAtLargeResolution.getWidth());
        assertEquals(90, analysisAtLargeResolution.getHeight());
        assertNotEquals(compactAtSmallResolution.getX(), analysisAtSmallResolution.getX());
        assertNotEquals(compactAtSmallResolution.getY(), analysisAtSmallResolution.getY());
    }

    @Test
    public void legacyAnchoringHelpersRemainAvailableForPendingMigration() {
        assertEquals(80.0, HudLayout.panelX(200, 10, 40, 1.5), 0.000001);
        assertEquals(96.0, HudLayout.panelY(180, -4, 10, 1.5), 0.000001);
    }

    @Test
    public void pendingMigrationKeepsAnalysisAboveTheLegacyCompactAnchor() {
        HudSettings settings = HudSettings.of(12, -8, 1.25, 214);

        TemperatureHudRenderer.PanelTransform compact = TemperatureHudRenderer.compactTransform(
            320,
            180,
            40,
            settings
        );
        TemperatureHudRenderer.PanelTransform analysis = TemperatureHudRenderer.analysisTransform(
            320,
            180,
            80,
            60,
            settings
        );

        assertEquals(147.0, compact.getX(), 0.000001);
        assertEquals(94.5, compact.getY(), 0.000001);
        assertEquals(1.25, compact.getScale(), 0.0);
        assertEquals(122.0, analysis.getX(), 0.000001);
        assertEquals(17.5, analysis.getY(), 0.000001);
        assertEquals(compact.getY() - 2.0 - 60.0 * compact.getScale(), analysis.getY(), 0.000001);
        assertEquals(compact.getScale(), analysis.getScale(), 0.0);
    }

    @Test
    public void pendingMigrationUsesConfiguredIndependentScalesOnceEitherIsCustomized() {
        HudConfigResetPolicy.State original = HudConfigResetPolicy.capture();
        try {
            HudConfig.xOffset = 12;
            HudConfig.yOffset = -8;
            HudConfig.scale = 1.25;
            HudConfig.layout.version = 0;
            HudConfig.layout.compactScale = 0.70;
            HudConfig.layout.analysisScale = 0.70;
            HudSettings settings = HudSettings.fromConfig();

            TemperatureHudRenderer.PanelTransform compact =
                TemperatureHudRenderer.compactTransform(320, 180, 40, settings);
            TemperatureHudRenderer.PanelTransform analysis =
                TemperatureHudRenderer.analysisTransform(320, 180, 80, 60, settings);

            assertEquals(158.0, compact.getX(), 0.000001);
            assertEquals(100.0, compact.getY(), 0.000001);
            assertEquals(0.70, compact.getScale(), 0.0);
            assertEquals(144.0, analysis.getX(), 0.000001);
            assertEquals(56.0, analysis.getY(), 0.000001);
            assertEquals(0.70, analysis.getScale(), 0.0);
        } finally {
            HudConfigResetPolicy.apply(original);
        }
    }

    @Test
    public void migratedAnalysisUsesItsOwnNormalizedPositionAndScale() {
        int originalVersion = HudConfig.layout.version;
        double originalCompactX = HudConfig.layout.compactX;
        double originalCompactY = HudConfig.layout.compactY;
        double originalCompactScale = HudConfig.layout.compactScale;
        double originalAnalysisX = HudConfig.layout.analysisX;
        double originalAnalysisY = HudConfig.layout.analysisY;
        double originalAnalysisScale = HudConfig.layout.analysisScale;
        try {
            HudConfig.layout.version = 1;
            HudConfig.layout.compactX = 0.75;
            HudConfig.layout.compactY = 0.8;
            HudConfig.layout.compactScale = 0.5;
            HudConfig.layout.analysisX = 0.1;
            HudConfig.layout.analysisY = 0.2;
            HudConfig.layout.analysisScale = 1.5;
            HudSettings settings = HudSettings.fromConfig();

            TemperatureHudRenderer.PanelTransform compact = TemperatureHudRenderer.compactTransform(
                320,
                180,
                40,
                settings
            );
            TemperatureHudRenderer.PanelTransform analysis = TemperatureHudRenderer.analysisTransform(
                320,
                180,
                80,
                60,
                settings
            );

            assertEquals(240.0, compact.getX(), 0.000001);
            assertEquals(144.0, compact.getY(), 0.000001);
            assertEquals(0.5, compact.getScale(), 0.0);
            assertEquals(32.0, analysis.getX(), 0.000001);
            assertEquals(36.0, analysis.getY(), 0.000001);
            assertEquals(1.5, analysis.getScale(), 0.0);
        } finally {
            HudConfig.layout.version = originalVersion;
            HudConfig.layout.compactX = originalCompactX;
            HudConfig.layout.compactY = originalCompactY;
            HudConfig.layout.compactScale = originalCompactScale;
            HudConfig.layout.analysisX = originalAnalysisX;
            HudConfig.layout.analysisY = originalAnalysisY;
            HudConfig.layout.analysisScale = originalAnalysisScale;
        }
    }

    @Test
    public void compactResetPreservesFractionalLegacyAnalysisTransform() {
        HudConfigResetPolicy.State original = HudConfigResetPolicy.capture();
        try {
            installFractionalLegacyLayout();
            HudSettings legacySettings = HudSettings.fromConfig();
            TemperatureHudRenderer.PanelTransform before =
                TemperatureHudRenderer.analysisTransform(
                    320,
                    200,
                    65,
                    31,
                    legacySettings
                );
            LegacyLayoutMigration.PanelLayouts current =
                LegacyLayoutMigration.forPanels(
                    37,
                    0,
                    1.25,
                    320,
                    200,
                    65,
                    10,
                    65,
                    31
                );

            HudConfigResetPolicy.apply(HudConfigResetPolicy.resetCompact(
                HudConfigResetPolicy.capture(),
                new HudLayoutState(0.25, 0.25, 0.75),
                current
            ));
            TemperatureHudRenderer.PanelTransform after =
                TemperatureHudRenderer.analysisTransform(
                    320,
                    200,
                    65,
                    31,
                    HudSettings.fromConfig()
                );

            assertTransformEquals(before, after);
            assertEquals(156.375, after.getX(), 0.0000001);
            assertEquals(81.75, after.getY(), 0.0000001);
        } finally {
            HudConfigResetPolicy.apply(original);
        }
    }

    @Test
    public void analysisResetPreservesFractionalLegacyCompactTransform() {
        HudConfigResetPolicy.State original = HudConfigResetPolicy.capture();
        try {
            installFractionalLegacyLayout();
            HudSettings legacySettings = HudSettings.fromConfig();
            TemperatureHudRenderer.PanelTransform before =
                TemperatureHudRenderer.compactTransform(320, 200, 65, legacySettings);
            LegacyLayoutMigration.PanelLayouts current =
                LegacyLayoutMigration.forPanels(
                    37,
                    0,
                    1.25,
                    320,
                    200,
                    65,
                    10,
                    65,
                    31
                );

            HudConfigResetPolicy.apply(HudConfigResetPolicy.resetAnalysis(
                HudConfigResetPolicy.capture(),
                new HudLayoutState(0.75, 0.10, 1.50),
                current
            ));
            TemperatureHudRenderer.PanelTransform after =
                TemperatureHudRenderer.compactTransform(
                    320,
                    200,
                    65,
                    HudSettings.fromConfig()
                );

            assertTransformEquals(before, after);
            assertEquals(156.375, after.getX(), 0.0000001);
            assertEquals(122.5, after.getY(), 0.0000001);
        } finally {
            HudConfigResetPolicy.apply(original);
        }
    }

    @Test
    public void scopedMigrationClampsAnOffscreenPanelToTheVisibilityBoundary() {
        HudConfigResetPolicy.State original = HudConfigResetPolicy.capture();
        try {
            HudConfig.xOffset = -500;
            HudConfig.yOffset = 0;
            HudConfig.scale = 1.0;
            HudConfig.layout.version = 0;
            TemperatureHudRenderer.PanelTransform legacy =
                TemperatureHudRenderer.compactTransform(
                    320,
                    200,
                    65,
                    HudSettings.fromConfig()
                );
            LegacyLayoutMigration.PanelLayouts current =
                LegacyLayoutMigration.forPanels(
                    -500,
                    0,
                    1.0,
                    320,
                    200,
                    65,
                    10,
                    65,
                    31
                );

            HudConfigResetPolicy.apply(HudConfigResetPolicy.resetAnalysis(
                HudConfigResetPolicy.capture(),
                new HudLayoutState(0.75, 0.10, 1.50),
                current
            ));
            TemperatureHudRenderer.PanelTransform migrated =
                TemperatureHudRenderer.compactTransform(
                    320,
                    200,
                    65,
                    HudSettings.fromConfig()
                );

            assertEquals(-372.5, legacy.getX(), 0.0000001);
            assertEquals(-49.0, migrated.getX(), 0.0000001);
            assertEquals(16.0, migrated.getX() + 65.0 * migrated.getScale(), 0.0000001);
        } finally {
            HudConfigResetPolicy.apply(original);
        }
    }

    private static void installFractionalLegacyLayout() {
        HudConfig.xOffset = 37;
        HudConfig.yOffset = 0;
        HudConfig.scale = 1.25;
        HudConfig.layout.version = 0;
    }

    private static void assertTransformEquals(
        TemperatureHudRenderer.PanelTransform expected,
        TemperatureHudRenderer.PanelTransform actual
    ) {
        assertEquals(expected.getX(), actual.getX(), 0.0000001);
        assertEquals(expected.getY(), actual.getY(), 0.0000001);
        assertEquals(expected.getScale(), actual.getScale(), 0.0000001);
    }
}
