/*
 * Decompiled with CFR 0.152.
 */
package dev.rlcraftceaddons.sdtemperaturehud.client;

import dev.rlcraftceaddons.sdtemperaturehud.client.layout.HudBounds;
import dev.rlcraftceaddons.sdtemperaturehud.client.layout.HudLayoutMath;
import dev.rlcraftceaddons.sdtemperaturehud.client.layout.HudLayoutState;

final class TemperatureAnalysisLayout {
    static final int HORIZONTAL_PADDING = 2;
    static final int VERTICAL_PADDING = 2;
    static final int VALUE_GAP = 4;
    static final int LINE_HEIGHT = 9;
    static final int PANEL_GAP = 2;
    static final int TARGET_GAP = 2;
    private static final int MINIMUM_VISIBLE_PIXELS = 16;

    private TemperatureAnalysisLayout() {
    }

    static int panelWidth(int headerWidth, int ... rowWidths) {
        int contentWidth = headerWidth;
        for (int rowWidth : rowWidths) {
            contentWidth = Math.max(contentWidth, rowWidth);
        }
        return 4 + contentWidth;
    }

    static int lineCount(boolean showOther) {
        return showOther ? 7 : 6;
    }

    static int panelHeight(boolean showOther) {
        return 4 + TemperatureAnalysisLayout.lineCount(showOther) * 9 + 2;
    }

    static int panelHeight(int lineCount, boolean showTarget) {
        if (lineCount <= 0) {
            return 0;
        }
        return VERTICAL_PADDING * 2 + lineCount * LINE_HEIGHT + (showTarget ? TARGET_GAP : 0);
    }

    static int labelX() {
        return 2;
    }

    static int valueX(int panelWidth, int valueWidth) {
        return panelWidth - 2 - valueWidth;
    }

    static int lineY(int lineIndex) {
        return 2 + lineIndex * 9;
    }

    static int targetLineY(boolean showOther) {
        return TemperatureAnalysisLayout.lineY(TemperatureAnalysisLayout.lineCount(showOther) - 1) + 2;
    }

    static double panelX(int scaledWidth, int xOffset, int panelWidth, double scale) {
        return (double)scaledWidth / 2.0 - (double)panelWidth * scale / 2.0 + (double)xOffset;
    }

    static double panelY(double compactPanelY, int panelHeight, double scale) {
        return compactPanelY - 2.0 - (double)panelHeight * scale;
    }

    static HudBounds panelBounds(
        HudLayoutState state,
        int screenWidth,
        int screenHeight,
        int panelWidth,
        int panelHeight
    ) {
        return HudLayoutMath.toBounds(
            state,
            screenWidth,
            screenHeight,
            panelWidth,
            panelHeight,
            MINIMUM_VISIBLE_PIXELS
        );
    }

    static HudLayoutMath.RenderPosition panelRenderPosition(
        HudLayoutState state,
        int screenWidth,
        int screenHeight,
        int panelWidth,
        int panelHeight
    ) {
        return HudLayoutMath.toRenderPosition(
            state,
            screenWidth,
            screenHeight,
            panelWidth,
            panelHeight,
            MINIMUM_VISIBLE_PIXELS
        );
    }
}
