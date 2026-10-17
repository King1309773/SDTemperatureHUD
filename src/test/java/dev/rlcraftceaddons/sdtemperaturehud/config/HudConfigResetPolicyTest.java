package dev.rlcraftceaddons.sdtemperaturehud.config;

import dev.rlcraftceaddons.sdtemperaturehud.client.AnalysisDisplayMode;
import dev.rlcraftceaddons.sdtemperaturehud.client.layout.HudBounds;
import dev.rlcraftceaddons.sdtemperaturehud.client.layout.HudLayoutMath;
import dev.rlcraftceaddons.sdtemperaturehud.client.layout.HudLayoutState;
import dev.rlcraftceaddons.sdtemperaturehud.client.theme.HudThemePreset;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class HudConfigResetPolicyTest {
    private static final HudLayoutState COMPACT_DEFAULT =
        new HudLayoutState(0.42, 0.73, 1.0);
    private static final HudLayoutState ANALYSIS_DEFAULT =
        new HudLayoutState(0.31, 0.22, 1.0);

    @Test
    public void compactResetChangesOnlyCompactCoordinatesAndScale() throws Exception {
        HudConfigResetPolicy.State original = HudConfigResetPolicy.capture();
        try {
            installSentinelConfig();
            HudConfigResetPolicy.State before = HudConfigResetPolicy.capture();

            HudConfigResetPolicy.State reset = HudConfigResetPolicy.resetCompact(
                before,
                COMPACT_DEFAULT,
                currentVisualLayouts(before, 320, 200, 64, 12, 112, 32)
            );

            assertEquals(before, HudConfigResetPolicy.capture());
            assertEquals(COMPACT_DEFAULT.getNormalizedX(), reset.compactX, 0.0);
            assertEquals(COMPACT_DEFAULT.getNormalizedY(), reset.compactY, 0.0);
            assertEquals(COMPACT_DEFAULT.getScale(), reset.compactScale, 0.0);
            assertOnlyFieldsChanged(
                before,
                reset,
                "compactX",
                "compactY",
                "compactScale"
            );
        } finally {
            HudConfigResetPolicy.apply(original);
        }
    }

    @Test
    public void analysisResetChangesOnlyAnalysisCoordinatesAndScale() throws Exception {
        HudConfigResetPolicy.State original = HudConfigResetPolicy.capture();
        try {
            installSentinelConfig();
            HudConfigResetPolicy.State before = HudConfigResetPolicy.capture();

            HudConfigResetPolicy.State reset = HudConfigResetPolicy.resetAnalysis(
                before,
                ANALYSIS_DEFAULT,
                currentVisualLayouts(before, 320, 200, 64, 12, 112, 32)
            );

            assertEquals(before, HudConfigResetPolicy.capture());
            assertEquals(ANALYSIS_DEFAULT.getNormalizedX(), reset.analysisX, 0.0);
            assertEquals(ANALYSIS_DEFAULT.getNormalizedY(), reset.analysisY, 0.0);
            assertEquals(ANALYSIS_DEFAULT.getScale(), reset.analysisScale, 0.0);
            assertOnlyFieldsChanged(
                before,
                reset,
                "analysisX",
                "analysisY",
                "analysisScale"
            );
        } finally {
            HudConfigResetPolicy.apply(original);
        }
    }

    @Test
    public void fullAppearanceResetIsCompleteAndPreservesBehaviorAndIntegration() {
        HudConfigResetPolicy.State original = HudConfigResetPolicy.capture();
        try {
            installSentinelConfig();
            HudConfigResetPolicy.State before = HudConfigResetPolicy.capture();

            HudConfigResetPolicy.State reset = HudConfigResetPolicy.resetFullAppearance(
                before,
                COMPACT_DEFAULT,
                ANALYSIS_DEFAULT
            );
            HudConfigResetPolicy.apply(reset);
            HudConfigResetPolicy.State applied = HudConfigResetPolicy.capture();

            assertEquals(reset, applied);
            assertTrue(applied.layoutVersion >= 1);
            assertEquals(COMPACT_DEFAULT.getNormalizedX(), applied.compactX, 0.0);
            assertEquals(COMPACT_DEFAULT.getNormalizedY(), applied.compactY, 0.0);
            assertEquals(1.0, applied.compactScale, 0.0);
            assertEquals(ANALYSIS_DEFAULT.getNormalizedX(), applied.analysisX, 0.0);
            assertEquals(ANALYSIS_DEFAULT.getNormalizedY(), applied.analysisY, 0.0);
            assertEquals(1.0, applied.analysisScale, 0.0);
            assertTrue(applied.allVisibilityEnabled());
            assertEquals(HudThemePreset.CLASSIC, applied.themePreset);
            assertFalse(applied.useCustomColors);
            assertEquals("#48C8FF", applied.severeCold);
            assertEquals("#82DDFF", applied.cold);
            assertEquals("#8FF0AD", applied.comfortable);
            assertEquals("#FFB34D", applied.warm);
            assertEquals("#FF5945", applied.severeHot);
            assertEquals("#FFD064", applied.rising);
            assertEquals("#7FDCFF", applied.falling);
            assertEquals("#D5D9DC", applied.stable);
            assertEquals("#0A0D12", applied.background);
            assertEquals("#4D5662", applied.border);
            assertEquals("#C3C7CC", applied.text);

            assertEquals(before.warningFlashEnabled, applied.warningFlashEnabled);
            assertEquals(before.warningSoundEnabled, applied.warningSoundEnabled);
            assertEquals(before.backgroundAlpha, applied.backgroundAlpha);
            assertEquals(before.analysisDisplayMode, applied.analysisDisplayMode);
            assertEquals(before.analysisEnabled, applied.analysisEnabled);
            assertEquals(before.xOffset, applied.xOffset);
            assertEquals(before.yOffset, applied.yOffset);
            assertEquals(before.legacyScale, applied.legacyScale, 0.0);
        } finally {
            HudConfigResetPolicy.apply(original);
        }
    }

    @Test
    public void legacyCompactResetMigratesUntouchedAnalysisWithoutMovingIt()
        throws Exception {
        HudConfigResetPolicy.State original = HudConfigResetPolicy.capture();
        try {
            installSentinelConfig();
            HudConfig.scale = 1.25;
            HudConfig.layout.version = 0;
            HudConfigResetPolicy.State before = HudConfigResetPolicy.capture();
            LegacyLayoutMigration.PanelLayouts current = currentVisualLayouts(
                before, 320, 200, 64, 12, 112, 32
            );

            HudConfigResetPolicy.State reset = HudConfigResetPolicy.resetCompact(
                before,
                COMPACT_DEFAULT,
                current
            );

            assertTrue(reset.layoutVersion >= 1);
            assertEquals(COMPACT_DEFAULT.getNormalizedX(), reset.compactX, 0.0);
            assertEquals(COMPACT_DEFAULT.getNormalizedY(), reset.compactY, 0.0);
            assertEquals(COMPACT_DEFAULT.getScale(), reset.compactScale, 0.0);
            assertEquals(current.analysis().getNormalizedX(), reset.analysisX, 0.0);
            assertEquals(current.analysis().getNormalizedY(), reset.analysisY, 0.0);
            assertEquals(current.analysis().getScale(), reset.analysisScale, 0.0);
            assertEquals(127.0, reset.analysisX * 320.0, 0.0);
            assertEquals(37.0, reset.analysisY * 200.0, 0.0);
            HudBounds resolvedAnalysis = HudLayoutMath.toBounds(
                new HudLayoutState(reset.analysisX, reset.analysisY, reset.analysisScale),
                320,
                200,
                112,
                32,
                16
            );
            assertEquals(127, resolvedAnalysis.getX());
            assertEquals(37, resolvedAnalysis.getY());
            assertEquals(1.25, reset.analysisScale, 0.0);
            assertOnlyFieldsChanged(
                before,
                reset,
                "layoutVersion",
                "compactX",
                "compactY",
                "compactScale",
                "analysisX",
                "analysisY",
                "analysisScale"
            );
        } finally {
            HudConfigResetPolicy.apply(original);
        }
    }

    @Test
    public void legacyAnalysisResetMigratesUntouchedCompactWithoutMovingIt()
        throws Exception {
        HudConfigResetPolicy.State original = HudConfigResetPolicy.capture();
        try {
            installSentinelConfig();
            HudConfig.scale = 1.25;
            HudConfig.layout.version = 0;
            HudConfigResetPolicy.State before = HudConfigResetPolicy.capture();
            LegacyLayoutMigration.PanelLayouts current = currentVisualLayouts(
                before, 320, 200, 64, 12, 112, 32
            );

            HudConfigResetPolicy.State reset = HudConfigResetPolicy.resetAnalysis(
                before,
                ANALYSIS_DEFAULT,
                current
            );

            assertTrue(reset.layoutVersion >= 1);
            assertEquals(ANALYSIS_DEFAULT.getNormalizedX(), reset.analysisX, 0.0);
            assertEquals(ANALYSIS_DEFAULT.getNormalizedY(), reset.analysisY, 0.0);
            assertEquals(ANALYSIS_DEFAULT.getScale(), reset.analysisScale, 0.0);
            assertEquals(current.compact().getNormalizedX(), reset.compactX, 0.0);
            assertEquals(current.compact().getNormalizedY(), reset.compactY, 0.0);
            assertEquals(current.compact().getScale(), reset.compactScale, 0.0);
            assertEquals(157.0, reset.compactX * 320.0, 0.0);
            assertEquals(79.0, reset.compactY * 200.0, 0.0);
            HudBounds resolvedCompact = HudLayoutMath.toBounds(
                new HudLayoutState(reset.compactX, reset.compactY, reset.compactScale),
                320,
                200,
                64,
                12,
                16
            );
            assertEquals(157, resolvedCompact.getX());
            assertEquals(79, resolvedCompact.getY());
            assertEquals(1.25, reset.compactScale, 0.0);
            assertOnlyFieldsChanged(
                before,
                reset,
                "layoutVersion",
                "compactX",
                "compactY",
                "compactScale",
                "analysisX",
                "analysisY",
                "analysisScale"
            );
        } finally {
            HudConfigResetPolicy.apply(original);
        }
    }

    private static void installSentinelConfig() {
        HudConfig.xOffset = 37;
        HudConfig.yOffset = -41;
        HudConfig.scale = 1.35;
        HudConfig.backgroundAlpha = 123;
        HudConfig.warningFlashEnabled = false;
        HudConfig.warningSoundEnabled = true;
        HudConfig.analysisEnabled = false;
        HudConfig.layout.version = 7;
        HudConfig.layout.compactX = 0.91;
        HudConfig.layout.compactY = -0.12;
        HudConfig.layout.compactScale = 1.65;
        HudConfig.layout.analysisX = 0.08;
        HudConfig.layout.analysisY = 0.62;
        HudConfig.layout.analysisScale = 0.65;
        HudConfig.analysis.displayMode = AnalysisDisplayMode.ALWAYS;
        HudConfig.visibility.showTrend = false;
        HudConfig.visibility.showTarget = false;
        HudConfig.visibility.showAnalysisTitle = false;
        HudConfig.visibility.showBaseEnvironment = false;
        HudConfig.visibility.showEquipment = false;
        HudConfig.visibility.showSeason = false;
        HudConfig.visibility.showNearbyHeatSources = false;
        HudConfig.visibility.showOtherModifiers = false;
        HudConfig.visibility.showAnalysisTarget = false;
        HudConfig.theme.preset = HudThemePreset.MONOCHROME;
        HudConfig.theme.useCustomColors = true;
        HudConfig.theme.severeCold = "#010101";
        HudConfig.theme.cold = "#020202";
        HudConfig.theme.comfortable = "#030303";
        HudConfig.theme.warm = "#040404";
        HudConfig.theme.severeHot = "#050505";
        HudConfig.theme.rising = "#060606";
        HudConfig.theme.falling = "#070707";
        HudConfig.theme.stable = "#080808";
        HudConfig.theme.background = "#090909";
        HudConfig.theme.border = "#101010";
        HudConfig.theme.text = "#111111";
    }

    private static LegacyLayoutMigration.PanelLayouts currentVisualLayouts(
        HudConfigResetPolicy.State state,
        int screenWidth,
        int screenHeight,
        int compactWidth,
        int compactHeight,
        int analysisWidth,
        int analysisHeight
    ) {
        return LegacyLayoutMigration.forPanels(
            state.xOffset,
            state.yOffset,
            state.legacyScale,
            screenWidth,
            screenHeight,
            compactWidth,
            compactHeight,
            analysisWidth,
            analysisHeight
        );
    }

    private static void assertOnlyFieldsChanged(
        HudConfigResetPolicy.State before,
        HudConfigResetPolicy.State after,
        String... allowedChanges
    ) throws Exception {
        Set<String> allowed = new HashSet<String>(Arrays.asList(allowedChanges));
        for (Field field : HudConfigResetPolicy.State.class.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) || allowed.contains(field.getName())) {
                continue;
            }
            field.setAccessible(true);
            assertEquals(
                "unexpected reset of " + field.getName(),
                field.get(before),
                field.get(after)
            );
        }
    }
}
