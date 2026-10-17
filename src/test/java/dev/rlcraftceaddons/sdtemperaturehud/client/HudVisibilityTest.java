package dev.rlcraftceaddons.sdtemperaturehud.client;

import dev.rlcraftceaddons.sdtemperaturehud.config.HudConfig;
import dev.rlcraftceaddons.sdtemperaturehud.temperature.TemperatureBreakdown;
import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class HudVisibilityTest {
    @After
    public void restoreVisibilityDefaults() {
        setAnalysisVisibility(true, true, true, true, true, true, true);
    }

    @Test
    public void ordinaryScreensAllowTheNormalHud() {
        assertTrue(HudVisibility.shouldRender(
            true, true, true, false, true, true, true, false, false
        ));
    }

    @Test
    public void gameOverScreenSuppressesTheNormalHud() {
        assertFalse(HudVisibility.shouldRender(
            true, true, true, false, true, true, true, true, false
        ));
    }

    @Test
    public void hudEditorScreenSuppressesTheNormalHud() {
        assertFalse(HudVisibility.shouldRender(
            true, true, true, false, true, true, true, false, true
        ));
    }

    @Test
    public void compactTextCollapsesHiddenTokensWithoutSpacing() {
        assertEquals("8", HudVisibility.compactText("8", "\u2191", "20", false, false));
        assertEquals("8\u2191", HudVisibility.compactText("8", "\u2191", "20", true, false));
        assertEquals("820", HudVisibility.compactText("8", "\u2191", "20", false, true));
        assertEquals("8\u219120", HudVisibility.compactText("8", "\u2191", "20", true, true));
    }

    @Test
    public void everyEnabledAnalysisItemContributesOneLine() {
        setAnalysisVisibility(true, true, true, true, true, true, true);
        HudSettings settings = HudSettings.fromConfig();

        assertEquals(7, TemperatureAnalysisPresentation.visibleLineCount(
            settings,
            new TemperatureBreakdown(8, 0, 0, 0, 3, 11)
        ));
        assertTrue(TemperatureAnalysisPresentation.hasVisibleRows(
            settings,
            new TemperatureBreakdown(8, 0, 0, 0, 3, 11)
        ));
    }

    @Test
    public void disabledAnalysisItemsDoNotReserveHeight() {
        setAnalysisVisibility(true, false, false, true, true, true, true);
        HudSettings settings = HudSettings.fromConfig();
        TemperatureBreakdown breakdown = new TemperatureBreakdown(8, 0, 0, 0, 3, 11);

        int lineCount = TemperatureAnalysisPresentation.visibleLineCount(settings, breakdown);

        assertEquals(5, lineCount);
        assertEquals(51, TemperatureAnalysisLayout.panelHeight(
            lineCount,
            TemperatureAnalysisPresentation.hasVisibleTarget(settings)
        ));
    }

    @Test
    public void enabledZeroRowsRemainVisibleButZeroOtherRemainsHidden() {
        setAnalysisVisibility(false, false, true, false, false, true, false);
        HudSettings settings = HudSettings.fromConfig();

        assertEquals(1, TemperatureAnalysisPresentation.visibleLineCount(
            settings,
            new TemperatureBreakdown(8, 0, 0, 0, 0, 8)
        ));
    }

    @Test
    public void noEligibleAnalysisRowsSuppressesThePanel() {
        setAnalysisVisibility(false, false, false, false, false, false, false);
        HudSettings settings = HudSettings.fromConfig();
        TemperatureBreakdown breakdown = new TemperatureBreakdown(8, 1, 2, 3, 4, 18);
        TemperatureHudRenderer renderer = new TemperatureHudRenderer(
            null,
            new TemperatureHudRenderer.TextWidthProvider() {
                @Override
                public int width(String text) {
                    return text.length();
                }
            }
        );

        assertEquals(0, TemperatureAnalysisPresentation.visibleLineCount(settings, breakdown));
        assertFalse(TemperatureAnalysisPresentation.hasVisibleRows(settings, breakdown));
        assertNull(renderer.measureAnalysis(breakdown, settings));
        assertFalse(HudVisibility.shouldRenderAnalysis(true, true, false));
        assertFalse(HudVisibility.shouldRenderAnalysis(true, false, true));
        assertTrue(HudVisibility.shouldRenderAnalysis(true, true, true));
    }

    @Test
    public void titleAloneDoesNotCountAsVisibleAnalysisContent() {
        setAnalysisVisibility(true, false, false, false, false, false, false);
        HudSettings settings = HudSettings.fromConfig();
        TemperatureBreakdown breakdown = new TemperatureBreakdown(8, 1, 2, 3, 4, 18);
        TemperatureHudRenderer renderer = new TemperatureHudRenderer(
            null,
            new TemperatureHudRenderer.TextWidthProvider() {
                @Override
                public int width(String text) {
                    return text.length();
                }
            }
        );

        assertEquals(1, TemperatureAnalysisPresentation.visibleLineCount(settings, breakdown));
        assertFalse(TemperatureAnalysisPresentation.hasVisibleRows(settings, breakdown));
        assertNull(renderer.measureAnalysis(breakdown, settings));
        assertFalse(HudVisibility.shouldRenderAnalysis(true, true, false));
    }

    private static void setAnalysisVisibility(
        boolean title,
        boolean environment,
        boolean equipment,
        boolean season,
        boolean heatSources,
        boolean other,
        boolean target
    ) {
        HudConfig.visibility.showAnalysisTitle = title;
        HudConfig.visibility.showBaseEnvironment = environment;
        HudConfig.visibility.showEquipment = equipment;
        HudConfig.visibility.showSeason = season;
        HudConfig.visibility.showNearbyHeatSources = heatSources;
        HudConfig.visibility.showOtherModifiers = other;
        HudConfig.visibility.showAnalysisTarget = target;
    }
}
