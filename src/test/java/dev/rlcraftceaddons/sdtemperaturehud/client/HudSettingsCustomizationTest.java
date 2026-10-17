package dev.rlcraftceaddons.sdtemperaturehud.client;

import dev.rlcraftceaddons.sdtemperaturehud.client.layout.HudLayoutState;
import dev.rlcraftceaddons.sdtemperaturehud.client.theme.HudColorPalette;
import dev.rlcraftceaddons.sdtemperaturehud.client.theme.HudThemePreset;
import dev.rlcraftceaddons.sdtemperaturehud.config.HudConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class HudSettingsCustomizationTest {
    @Before
    public void setUp() {
        restoreConfigDefaults();
        HudSettings.refreshFromConfig();
    }

    @After
    public void tearDown() {
        restoreConfigDefaults();
        HudSettings.refreshFromConfig();
    }

    @Test
    public void legacyFactoriesPopulateCompatibleCustomizationDefaults() {
        HudSettings settings = HudSettings.of(12, -8, 1.25, 200, false, true, false);

        assertEquals(12, settings.getXOffset());
        assertEquals(-8, settings.getYOffset());
        assertEquals(1.25, settings.getScale(), 0.0);
        assertEquals(200, settings.getBackgroundAlpha());
        assertFalse(settings.isWarningFlashEnabled());
        assertTrue(settings.isWarningSoundEnabled());
        assertFalse(settings.isAnalysisEnabled());
        assertEquals(new HudLayoutState(0.5, 0.5, 1.0), settings.getCompactLayout());
        assertEquals(new HudLayoutState(0.5, 0.5, 1.0), settings.getAnalysisLayout());
        assertEquals(0, settings.getLayoutVersion());
        assertTrue(settings.isLegacyLayoutPending());
        assertEquals(AnalysisDisplayMode.HOLD, settings.getAnalysisDisplayMode());
        assertAllVisibility(settings, true);
        assertEquals(HudThemePreset.CLASSIC, settings.getThemePreset());
        assertEquals(HudThemePreset.CLASSIC.getPalette(), settings.getColorPalette());
        assertFalse(settings.isCustomPalette());
    }

    @Test
    public void sanitizesLegacyValuesLayoutsAndNullEnums() {
        HudConfig.xOffset = 900;
        HudConfig.yOffset = -700;
        HudConfig.scale = Double.POSITIVE_INFINITY;
        HudConfig.backgroundAlpha = 999;
        HudConfig.layout.version = -4;
        HudConfig.layout.compactX = Double.NaN;
        HudConfig.layout.compactY = 0.75;
        HudConfig.layout.compactScale = 0.2;
        HudConfig.layout.analysisX = -0.25;
        HudConfig.layout.analysisY = Double.NEGATIVE_INFINITY;
        HudConfig.layout.analysisScale = 8.0;
        HudConfig.analysis.displayMode = null;
        HudConfig.theme.preset = null;

        HudSettings settings = HudSettings.fromConfig();

        assertEquals(500, settings.getXOffset());
        assertEquals(-500, settings.getYOffset());
        assertEquals(1.0, settings.getScale(), 0.0);
        assertEquals(255, settings.getBackgroundAlpha());
        assertEquals(new HudLayoutState(0.5, 0.75, 0.5), settings.getCompactLayout());
        assertEquals(new HudLayoutState(-0.25, 0.5, 2.0), settings.getAnalysisLayout());
        assertEquals(-4, settings.getLayoutVersion());
        assertTrue(settings.isLegacyLayoutPending());
        assertEquals(AnalysisDisplayMode.HOLD, settings.getAnalysisDisplayMode());
        assertEquals(HudThemePreset.CLASSIC, settings.getThemePreset());
    }

    @Test
    public void customColorsFallBackFieldByFieldToSelectedPreset() {
        HudConfig.theme.preset = HudThemePreset.HIGH_CONTRAST;
        HudConfig.theme.useCustomColors = true;
        HudConfig.theme.severeCold = "#123456";
        HudConfig.theme.cold = "not-a-color";
        HudConfig.theme.comfortable = "ABCDEF";
        HudConfig.theme.warm = "also-not-a-color";

        HudSettings settings = HudSettings.fromConfig();
        HudColorPalette palette = settings.getColorPalette();
        HudColorPalette fallback = HudThemePreset.HIGH_CONTRAST.getPalette();

        assertEquals(0x123456, palette.getSevereCold());
        assertEquals(fallback.getCold(), palette.getCold());
        assertEquals(0xABCDEF, palette.getComfortable());
        assertEquals(fallback.getWarm(), palette.getWarm());
        assertEquals(HudThemePreset.HIGH_CONTRAST, settings.getThemePreset());
        assertTrue(settings.isCustomPalette());
    }

    @Test
    public void presetModeIgnoresStaleCustomStrings() {
        HudConfig.theme.preset = HudThemePreset.MONOCHROME;
        HudConfig.theme.useCustomColors = false;
        HudConfig.theme.severeCold = "#123456";
        HudConfig.theme.text = "invalid";

        HudSettings settings = HudSettings.fromConfig();

        assertSame(HudThemePreset.MONOCHROME.getPalette(), settings.getColorPalette());
        assertFalse(settings.isCustomPalette());
    }

    @Test
    public void customFlagOnlyMarksAPaletteThatDiffersFromPreset() {
        HudConfig.theme.preset = HudThemePreset.CLASSIC;
        HudConfig.theme.useCustomColors = true;
        HudConfig.theme.severeCold = "invalid";

        HudSettings sameAsPreset = HudSettings.fromConfig();
        assertEquals(HudThemePreset.CLASSIC.getPalette(), sameAsPreset.getColorPalette());
        assertFalse(sameAsPreset.isCustomPalette());

        HudConfig.theme.border = "#010203";
        HudSettings changed = HudSettings.fromConfig();
        assertEquals(0x010203, changed.getColorPalette().getBorder());
        assertTrue(changed.isCustomPalette());
    }

    @Test
    public void copiesEveryVisibilityFlag() {
        HudConfig.visibility.showTrend = false;
        HudConfig.visibility.showTarget = true;
        HudConfig.visibility.showAnalysisTitle = false;
        HudConfig.visibility.showBaseEnvironment = true;
        HudConfig.visibility.showEquipment = false;
        HudConfig.visibility.showSeason = true;
        HudConfig.visibility.showNearbyHeatSources = false;
        HudConfig.visibility.showOtherModifiers = true;
        HudConfig.visibility.showAnalysisTarget = false;

        HudSettings settings = HudSettings.fromConfig();

        assertFalse(settings.isShowTrend());
        assertTrue(settings.isShowTarget());
        assertFalse(settings.isShowAnalysisTitle());
        assertTrue(settings.isShowBaseEnvironment());
        assertFalse(settings.isShowEquipment());
        assertTrue(settings.isShowSeason());
        assertFalse(settings.isShowNearbyHeatSources());
        assertTrue(settings.isShowOtherModifiers());
        assertFalse(settings.isShowAnalysisTarget());
    }

    @Test
    public void currentSnapshotStaysStableUntilExplicitRefresh() {
        HudConfig.xOffset = 10;
        HudConfig.layout.compactX = 0.25;
        HudConfig.visibility.showTrend = true;
        HudSettings.refreshFromConfig();
        HudSettings beforeMutation = HudSettings.current();

        HudConfig.xOffset = 77;
        HudConfig.layout.compactX = 0.8;
        HudConfig.visibility.showTrend = false;

        assertSame(beforeMutation, HudSettings.current());
        assertEquals(10, HudSettings.current().getXOffset());
        assertEquals(0.25, HudSettings.current().getCompactLayout().getNormalizedX(), 0.0);
        assertTrue(HudSettings.current().isShowTrend());

        HudSettings.refreshFromConfig();
        HudSettings afterRefresh = HudSettings.current();
        assertNotSame(beforeMutation, afterRefresh);
        assertEquals(77, afterRefresh.getXOffset());
        assertEquals(0.8, afterRefresh.getCompactLayout().getNormalizedX(), 0.0);
        assertFalse(afterRefresh.isShowTrend());
    }

    @Test
    public void editorLayoutCopyChangesOnlyLayoutsAndMarksMigrationComplete() {
        HudConfig.xOffset = 17;
        HudConfig.warningSoundEnabled = true;
        HudConfig.visibility.showTrend = false;
        HudSettings settings = HudSettings.fromConfig();
        HudLayoutState compact = new HudLayoutState(0.2, 0.3, 0.75);
        HudLayoutState analysis = new HudLayoutState(0.6, 0.1, 1.4);

        HudSettings editor = settings.withLayouts(compact, analysis);

        assertEquals(compact, editor.getCompactLayout());
        assertEquals(analysis, editor.getAnalysisLayout());
        assertEquals(1, editor.getLayoutVersion());
        assertFalse(editor.isLegacyLayoutPending());
        assertEquals(settings.getXOffset(), editor.getXOffset());
        assertEquals(settings.getBackgroundAlpha(), editor.getBackgroundAlpha());
        assertEquals(settings.isWarningSoundEnabled(), editor.isWarningSoundEnabled());
        assertEquals(settings.isShowTrend(), editor.isShowTrend());
        assertEquals(settings.getColorPalette(), editor.getColorPalette());
    }

    private static void assertAllVisibility(HudSettings settings, boolean expected) {
        assertEquals(expected, settings.isShowTrend());
        assertEquals(expected, settings.isShowTarget());
        assertEquals(expected, settings.isShowAnalysisTitle());
        assertEquals(expected, settings.isShowBaseEnvironment());
        assertEquals(expected, settings.isShowEquipment());
        assertEquals(expected, settings.isShowSeason());
        assertEquals(expected, settings.isShowNearbyHeatSources());
        assertEquals(expected, settings.isShowOtherModifiers());
        assertEquals(expected, settings.isShowAnalysisTarget());
    }

    private static void restoreConfigDefaults() {
        HudConfig.xOffset = 0;
        HudConfig.yOffset = 0;
        HudConfig.scale = 1.0;
        HudConfig.backgroundAlpha = 214;
        HudConfig.warningFlashEnabled = true;
        HudConfig.warningSoundEnabled = false;
        HudConfig.analysisEnabled = true;

        HudConfig.layout.version = 0;
        HudConfig.layout.compactX = 0.5;
        HudConfig.layout.compactY = 0.5;
        HudConfig.layout.compactScale = 1.0;
        HudConfig.layout.analysisX = 0.5;
        HudConfig.layout.analysisY = 0.5;
        HudConfig.layout.analysisScale = 1.0;

        HudConfig.analysis.displayMode = AnalysisDisplayMode.HOLD;

        HudConfig.visibility.showTrend = true;
        HudConfig.visibility.showTarget = true;
        HudConfig.visibility.showAnalysisTitle = true;
        HudConfig.visibility.showBaseEnvironment = true;
        HudConfig.visibility.showEquipment = true;
        HudConfig.visibility.showSeason = true;
        HudConfig.visibility.showNearbyHeatSources = true;
        HudConfig.visibility.showOtherModifiers = true;
        HudConfig.visibility.showAnalysisTarget = true;

        HudConfig.theme.preset = HudThemePreset.CLASSIC;
        HudConfig.theme.useCustomColors = false;
        HudConfig.theme.severeCold = "#48C8FF";
        HudConfig.theme.cold = "#82DDFF";
        HudConfig.theme.comfortable = "#8FF0AD";
        HudConfig.theme.warm = "#FFB34D";
        HudConfig.theme.severeHot = "#FF5945";
        HudConfig.theme.rising = "#FFD064";
        HudConfig.theme.falling = "#7FDCFF";
        HudConfig.theme.stable = "#D5D9DC";
        HudConfig.theme.background = "#0A0D12";
        HudConfig.theme.border = "#4D5662";
        HudConfig.theme.text = "#C3C7CC";
    }
}
