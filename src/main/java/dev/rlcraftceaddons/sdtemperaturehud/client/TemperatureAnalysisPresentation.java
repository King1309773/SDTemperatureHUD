/*
 * Decompiled with CFR 0.152.
 */
package dev.rlcraftceaddons.sdtemperaturehud.client;

import dev.rlcraftceaddons.sdtemperaturehud.client.theme.HudColorPalette;
import dev.rlcraftceaddons.sdtemperaturehud.client.theme.HudThemePreset;
import dev.rlcraftceaddons.sdtemperaturehud.temperature.TemperatureBreakdown;

final class TemperatureAnalysisPresentation {
    static final int TITLE_COLOR = 0xE6E8EA;
    static final int POSITIVE_COLOR = 16757581;
    static final int NEGATIVE_COLOR = 8379647;
    static final int ZERO_COLOR = 11054515;

    private TemperatureAnalysisPresentation() {
    }

    static String signed(int value) {
        if (value > 0) {
            return "+" + value;
        }
        if (value < 0) {
            return "\u2212" + -((long)value);
        }
        return "0";
    }

    static int modifierColor(int value) {
        if (value > 0) {
            return POSITIVE_COLOR;
        }
        if (value < 0) {
            return NEGATIVE_COLOR;
        }
        return ZERO_COLOR;
    }

    static int titleColor(HudColorPalette palette) {
        return isClassic(palette) ? TITLE_COLOR : palette.getText();
    }

    static int modifierColor(int value, HudColorPalette palette) {
        if (isClassic(palette)) {
            return modifierColor(value);
        }
        if (value > 0) {
            return palette.getWarm();
        }
        if (value < 0) {
            return palette.getFalling();
        }
        return palette.getStable();
    }

    private static boolean isClassic(HudColorPalette palette) {
        return HudThemePreset.CLASSIC.getPalette().equals(palette);
    }

    static boolean shouldShowOther(int value) {
        return value != 0;
    }

    static int visibleLineCount(HudSettings settings, TemperatureBreakdown breakdown) {
        int lineCount = 0;
        lineCount += settings.isShowAnalysisTitle() ? 1 : 0;
        lineCount += settings.isShowBaseEnvironment() ? 1 : 0;
        lineCount += settings.isShowEquipment() ? 1 : 0;
        lineCount += settings.isShowSeason() ? 1 : 0;
        lineCount += settings.isShowNearbyHeatSources() ? 1 : 0;
        lineCount += settings.isShowOtherModifiers() && shouldShowOther(breakdown.getOther()) ? 1 : 0;
        lineCount += settings.isShowAnalysisTarget() ? 1 : 0;
        return lineCount;
    }

    static boolean hasVisibleRows(HudSettings settings, TemperatureBreakdown breakdown) {
        return settings.isShowBaseEnvironment()
            || settings.isShowEquipment()
            || settings.isShowSeason()
            || settings.isShowNearbyHeatSources()
            || settings.isShowOtherModifiers() && shouldShowOther(breakdown.getOther())
            || settings.isShowAnalysisTarget();
    }

    static boolean hasVisibleTarget(HudSettings settings) {
        return settings.isShowAnalysisTarget();
    }
}
