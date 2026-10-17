package dev.rlcraftceaddons.sdtemperaturehud.config;

import dev.rlcraftceaddons.sdtemperaturehud.client.layout.HudLayoutMath;
import dev.rlcraftceaddons.sdtemperaturehud.client.layout.HudLayoutState;

public final class LegacyLayoutMigration {
    private static final int COMPACT_BOTTOM_MARGIN = 65;
    private static final int PANEL_GAP = 2;
    private static final HudLayoutState SAFE_DEFAULT = new HudLayoutState(0.5, 0.5, 1.0);

    private LegacyLayoutMigration() {
    }

    public static boolean canMigrate(int screenWidth, int screenHeight) {
        return screenWidth > 0 && screenHeight > 0;
    }

    public static HudLayoutState fromLegacyOffsets(
        int xOffset,
        int yOffset,
        double scale,
        int screenWidth,
        int screenHeight,
        double defaultTopLeftX,
        double defaultTopLeftY
    ) {
        if (!canMigrate(screenWidth, screenHeight)
            || !Double.isFinite(defaultTopLeftX)
            || !Double.isFinite(defaultTopLeftY)) {
            return SAFE_DEFAULT;
        }
        return new HudLayoutState(
            (defaultTopLeftX + xOffset) / screenWidth,
            (defaultTopLeftY + yOffset) / screenHeight,
            HudLayoutMath.clampScale(scale)
        );
    }

    public static HudLayoutState compactFromLegacy(
        int xOffset,
        int yOffset,
        double scale,
        int screenWidth,
        int screenHeight,
        int panelWidth,
        int panelHeight
    ) {
        if (!validPanelDimensions(screenWidth, screenHeight, panelWidth, panelHeight)) {
            return SAFE_DEFAULT;
        }
        double safeScale = HudLayoutMath.clampScale(scale);
        return fromLegacyOffsets(
            xOffset,
            yOffset,
            safeScale,
            screenWidth,
            screenHeight,
            (double)screenWidth / 2.0 - (double)panelWidth * safeScale / 2.0,
            screenHeight - COMPACT_BOTTOM_MARGIN - panelHeight * safeScale
        );
    }

    public static HudLayoutState analysisFromLegacy(
        int xOffset,
        int yOffset,
        double scale,
        int screenWidth,
        int screenHeight,
        int compactHeight,
        int analysisWidth,
        int analysisHeight
    ) {
        if (!validPanelDimensions(screenWidth, screenHeight, analysisWidth, analysisHeight)
            || compactHeight <= 0) {
            return SAFE_DEFAULT;
        }
        double safeScale = HudLayoutMath.clampScale(scale);
        double compactTop = screenHeight
            - COMPACT_BOTTOM_MARGIN
            - compactHeight * safeScale;
        return fromLegacyOffsets(
            xOffset,
            yOffset,
            safeScale,
            screenWidth,
            screenHeight,
            (double)screenWidth / 2.0 - (double)analysisWidth * safeScale / 2.0,
            compactTop - PANEL_GAP - analysisHeight * safeScale
        );
    }

    public static HudLayoutState analysisFromLegacy(
        int xOffset,
        int yOffset,
        double compactScale,
        double analysisScale,
        int screenWidth,
        int screenHeight,
        int compactHeight,
        int analysisWidth,
        int analysisHeight
    ) {
        if (!validPanelDimensions(screenWidth, screenHeight, analysisWidth, analysisHeight)
            || compactHeight <= 0) {
            return SAFE_DEFAULT;
        }
        double safeCompactScale = HudLayoutMath.clampScale(compactScale);
        double safeAnalysisScale = HudLayoutMath.clampScale(analysisScale);
        double compactTop = screenHeight
            - COMPACT_BOTTOM_MARGIN
            - compactHeight * safeCompactScale;
        return fromLegacyOffsets(
            xOffset,
            yOffset,
            safeAnalysisScale,
            screenWidth,
            screenHeight,
            (double)screenWidth / 2.0 - (double)analysisWidth * safeAnalysisScale / 2.0,
            compactTop - PANEL_GAP - analysisHeight * safeAnalysisScale
        );
    }

    public static double pendingCompactScale(
        double legacyScale,
        double compactScale,
        double analysisScale
    ) {
        return usesIndependentScales(compactScale, analysisScale)
            ? HudLayoutMath.clampScale(compactScale)
            : HudLayoutMath.clampScale(legacyScale);
    }

    public static double pendingAnalysisScale(
        double legacyScale,
        double compactScale,
        double analysisScale
    ) {
        return usesIndependentScales(compactScale, analysisScale)
            ? HudLayoutMath.clampScale(analysisScale)
            : HudLayoutMath.clampScale(legacyScale);
    }

    private static boolean usesIndependentScales(double compactScale, double analysisScale) {
        return Double.compare(HudLayoutMath.clampScale(compactScale), 1.0) != 0
            || Double.compare(HudLayoutMath.clampScale(analysisScale), 1.0) != 0;
    }

    public static PanelLayouts forPanels(
        int xOffset,
        int yOffset,
        double scale,
        int screenWidth,
        int screenHeight,
        int compactWidth,
        int compactHeight,
        int analysisWidth,
        int analysisHeight
    ) {
        if (!validPanelDimensions(screenWidth, screenHeight, compactWidth, compactHeight)
            || !validPanelDimensions(
                screenWidth,
                screenHeight,
                analysisWidth,
                analysisHeight
            )) {
            return new PanelLayouts(SAFE_DEFAULT, SAFE_DEFAULT);
        }
        return new PanelLayouts(
            compactFromLegacy(
                xOffset,
                yOffset,
                scale,
                screenWidth,
                screenHeight,
                compactWidth,
                compactHeight
            ),
            analysisFromLegacy(
                xOffset,
                yOffset,
                scale,
                screenWidth,
                screenHeight,
                compactHeight,
                analysisWidth,
                analysisHeight
            )
        );
    }

    public static PanelLayouts forPanels(
        int xOffset,
        int yOffset,
        double legacyScale,
        double compactScale,
        double analysisScale,
        int screenWidth,
        int screenHeight,
        int compactWidth,
        int compactHeight,
        int analysisWidth,
        int analysisHeight
    ) {
        double effectiveCompactScale = pendingCompactScale(
            legacyScale,
            compactScale,
            analysisScale
        );
        double effectiveAnalysisScale = pendingAnalysisScale(
            legacyScale,
            compactScale,
            analysisScale
        );
        return new PanelLayouts(
            compactFromLegacy(
                xOffset,
                yOffset,
                effectiveCompactScale,
                screenWidth,
                screenHeight,
                compactWidth,
                compactHeight
            ),
            analysisFromLegacy(
                xOffset,
                yOffset,
                effectiveCompactScale,
                effectiveAnalysisScale,
                screenWidth,
                screenHeight,
                compactHeight,
                analysisWidth,
                analysisHeight
            )
        );
    }

    public static boolean isPending(int layoutVersion) {
        return layoutVersion < 1;
    }

    private static boolean validPanelDimensions(
        int screenWidth,
        int screenHeight,
        int panelWidth,
        int panelHeight
    ) {
        return canMigrate(screenWidth, screenHeight) && panelWidth > 0 && panelHeight > 0;
    }

    public static final class PanelLayouts {
        private final HudLayoutState compact;
        private final HudLayoutState analysis;

        private PanelLayouts(HudLayoutState compact, HudLayoutState analysis) {
            this.compact = compact;
            this.analysis = analysis;
        }

        public HudLayoutState compact() {
            return this.compact;
        }

        public HudLayoutState analysis() {
            return this.analysis;
        }
    }
}
