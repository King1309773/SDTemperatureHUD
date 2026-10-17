/*
 * Decompiled with CFR 0.152.
 */
package dev.rlcraftceaddons.sdtemperaturehud.client;

import dev.rlcraftceaddons.sdtemperaturehud.client.layout.HudBounds;
import dev.rlcraftceaddons.sdtemperaturehud.client.layout.HudLayoutMath;
import dev.rlcraftceaddons.sdtemperaturehud.client.layout.HudLayoutState;

final class HudLayout {
    private static final int HORIZONTAL_PADDING = 2;
    private static final int ORB_TOP_FROM_SCREEN_BOTTOM = 62;
    private static final int PANEL_GAP = 3;
    private static final int MINIMUM_VISIBLE_PIXELS = 16;

    private HudLayout() {
    }

    static int panelWidth(int ... tokenWidths) {
        int width = 4;
        for (int tokenWidth : tokenWidths) {
            width += tokenWidth;
        }
        return width;
    }

    static int bodyX() {
        return 2;
    }

    static int arrowX(int bodyWidth) {
        return 2 + bodyWidth;
    }

    static int targetX(int bodyWidth, int arrowWidth) {
        return 2 + bodyWidth + arrowWidth;
    }

    static double panelX(int scaledWidth, int xOffset, int panelWidth, double scale) {
        return (double)scaledWidth / 2.0 - (double)panelWidth * scale / 2.0 + (double)xOffset;
    }

    static double panelY(int scaledHeight, int yOffset, int panelHeight, double scale) {
        return (double)(scaledHeight - 62 - 3) - (double)panelHeight * scale + (double)yOffset;
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
