package dev.rlcraftceaddons.sdtemperaturehud.client;

import dev.rlcraftceaddons.sdtemperaturehud.client.theme.HudColorPalette;
import dev.rlcraftceaddons.sdtemperaturehud.client.theme.HudThemePreset;
import dev.rlcraftceaddons.sdtemperaturehud.config.HudConfig;
import dev.rlcraftceaddons.sdtemperaturehud.temperature.TemperaturePresentation;
import dev.rlcraftceaddons.sdtemperaturehud.temperature.TemperatureSnapshot;
import dev.rlcraftceaddons.sdtemperaturehud.temperature.TemperatureWarningLevel;
import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class ThemeRenderingPolicyTest {
    private static final HudColorPalette DISTINCT_PALETTE = new HudColorPalette(
        0x010101,
        0x020202,
        0x030303,
        0x040404,
        0x050505,
        0x060606,
        0x070707,
        0x080808,
        0x091011,
        0x121314,
        0x151617
    );

    @After
    public void restoreThemeConfig() {
        HudConfig.theme.preset = HudThemePreset.CLASSIC;
        HudConfig.theme.useCustomColors = false;
        HudConfig.backgroundAlpha = 214;
    }

    @Test
    public void mapsAllFiveExistingTemperatureBandsToThePalette() {
        assertEquals(0x010101, TemperaturePresentation.colorFor(5, DISTINCT_PALETTE));
        assertEquals(0x020202, TemperaturePresentation.colorFor(6, DISTINCT_PALETTE));
        assertEquals(0x020202, TemperaturePresentation.colorFor(10, DISTINCT_PALETTE));
        assertEquals(0x030303, TemperaturePresentation.colorFor(11, DISTINCT_PALETTE));
        assertEquals(0x030303, TemperaturePresentation.colorFor(14, DISTINCT_PALETTE));
        assertEquals(0x040404, TemperaturePresentation.colorFor(15, DISTINCT_PALETTE));
        assertEquals(0x040404, TemperaturePresentation.colorFor(19, DISTINCT_PALETTE));
        assertEquals(0x050505, TemperaturePresentation.colorFor(20, DISTINCT_PALETTE));
    }

    @Test
    public void mapsAllThreeTrendDirectionsToThePalette() {
        assertEquals(
            0x060606,
            TemperaturePresentation.trendColor(TemperaturePresentation.Trend.UP, DISTINCT_PALETTE)
        );
        assertEquals(
            0x070707,
            TemperaturePresentation.trendColor(TemperaturePresentation.Trend.DOWN, DISTINCT_PALETTE)
        );
        assertEquals(
            0x080808,
            TemperaturePresentation.trendColor(TemperaturePresentation.Trend.STEADY, DISTINCT_PALETTE)
        );
    }

    @Test
    public void classicAnalysisTitleKeepsTheRecoveredHighlight() {
        assertEquals(
            0xE6E8EA,
            TemperatureAnalysisPresentation.titleColor(HudThemePreset.CLASSIC.getPalette())
        );
    }

    @Test
    public void classicAnalysisModifiersKeepRecoveredPositiveNegativeAndZeroColors() {
        HudColorPalette classic = HudThemePreset.CLASSIC.getPalette();

        assertEquals(0xFFB34D, TemperatureAnalysisPresentation.modifierColor(1, classic));
        assertEquals(0x7FDCFF, TemperatureAnalysisPresentation.modifierColor(-1, classic));
        assertEquals(0xA8ADB3, TemperatureAnalysisPresentation.modifierColor(0, classic));
    }

    @Test
    public void nonClassicAnalysisTitleUsesTheSelectedPaletteText() {
        assertEquals(
            DISTINCT_PALETTE.getText(),
            TemperatureAnalysisPresentation.titleColor(DISTINCT_PALETTE)
        );
    }

    @Test
    public void nonClassicAnalysisModifiersUseSelectedPaletteSemantics() {
        assertEquals(
            DISTINCT_PALETTE.getWarm(),
            TemperatureAnalysisPresentation.modifierColor(1, DISTINCT_PALETTE)
        );
        assertEquals(
            DISTINCT_PALETTE.getFalling(),
            TemperatureAnalysisPresentation.modifierColor(-1, DISTINCT_PALETTE)
        );
        assertEquals(
            DISTINCT_PALETTE.getStable(),
            TemperatureAnalysisPresentation.modifierColor(0, DISTINCT_PALETTE)
        );
    }

    @Test
    public void composesBackgroundAlphaWithoutReplacingSelectedRgb() {
        assertEquals(0x80091011, WarningPulse.withOpacity(0x091011, 128, 1.0));
        assertEquals(0x40091011, WarningPulse.withOpacity(0x091011, 128, 0.5));

        double cautionPulse = WarningPulse.opacity(TemperatureWarningLevel.CAUTION_COLD, 0L, true);
        int pulsed = WarningPulse.withOpacity(0x091011, 200, cautionPulse);
        assertEquals(140, pulsed >>> 24);
        assertEquals(0x091011, pulsed & 0xFFFFFF);
    }

    @Test
    public void rendererMeasurementUsesTheImmutablePaletteFromSettings() {
        HudConfig.theme.preset = HudThemePreset.COLOR_BLIND;
        HudConfig.theme.useCustomColors = false;
        HudSettings settings = HudSettings.fromConfig();
        HudColorPalette palette = settings.getColorPalette();
        TemperatureHudRenderer renderer = new TemperatureHudRenderer(
            null,
            new TemperatureHudRenderer.TextWidthProvider() {
                @Override
                public int width(String text) {
                    return text.length();
                }
            }
        );

        TemperatureHudRenderer.CompactPanel panel = renderer.measureCompact(
            TemperatureSnapshot.withTarget(4, 17),
            settings
        );

        assertEquals(palette.getSevereCold(), panel.getBodyColor());
        assertEquals(palette.getRising(), panel.getTrendColor());
        assertEquals(palette.getWarm(), panel.getTargetColor());
    }

    @Test
    public void rendererBackgroundPolicyUsesSettingsRgbAndValidatedAlpha() {
        HudConfig.theme.preset = HudThemePreset.COLOR_BLIND;
        HudConfig.theme.useCustomColors = false;
        HudConfig.backgroundAlpha = 128;
        HudSettings settings = HudSettings.fromConfig();

        WarningPanelStyle staticStyle = WarningPanelStyle.forLevel(
            TemperatureWarningLevel.NORMAL,
            0L,
            true
        );
        WarningPanelStyle pulsedStyle = WarningPanelStyle.forLevel(
            TemperatureWarningLevel.CRITICAL_HOT,
            0L,
            true
        );

        assertEquals(0x80181818, TemperatureHudRenderer.backgroundColor(settings, staticStyle));
        assertEquals(0x2D181818, TemperatureHudRenderer.backgroundColor(settings, pulsedStyle));
    }

    @Test
    public void workingPaletteSettingsPreserveLiveCompactPresentationOptions() {
        HudConfig.theme.preset = HudThemePreset.CLASSIC;
        HudConfig.theme.useCustomColors = false;
        HudSettings base = HudSettings.fromConfig();

        HudSettings preview = base.withColorPalette(DISTINCT_PALETTE);

        assertEquals(DISTINCT_PALETTE, preview.getColorPalette());
        assertEquals(base.getCompactLayout(), preview.getCompactLayout());
        assertEquals(base.getBackgroundAlpha(), preview.getBackgroundAlpha());
        assertEquals(base.isShowTrend(), preview.isShowTrend());
        assertEquals(base.isShowTarget(), preview.isShowTarget());
        assertFalse(preview.getColorPalette().equals(base.getColorPalette()));
    }
}
