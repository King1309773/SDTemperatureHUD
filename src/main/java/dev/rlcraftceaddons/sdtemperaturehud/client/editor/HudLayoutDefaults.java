package dev.rlcraftceaddons.sdtemperaturehud.client.editor;

import dev.rlcraftceaddons.sdtemperaturehud.client.layout.HudBounds;
import dev.rlcraftceaddons.sdtemperaturehud.client.layout.HudLayoutMath;
import dev.rlcraftceaddons.sdtemperaturehud.client.layout.HudLayoutState;

public final class HudLayoutDefaults {
    private static final int COMPACT_BOTTOM_MARGIN = 65;
    private static final int PANEL_GAP = 2;

    private final HudLayoutState compact;
    private final HudLayoutState analysis;

    private HudLayoutDefaults(HudLayoutState compact, HudLayoutState analysis) {
        this.compact = compact;
        this.analysis = analysis;
    }

    public static HudLayoutDefaults forScreen(
        int screenWidth,
        int screenHeight,
        int compactWidth,
        int compactHeight,
        int analysisWidth,
        int analysisHeight
    ) {
        if (screenWidth <= 0 || screenHeight <= 0) {
            throw new IllegalArgumentException("screen dimensions must be positive");
        }
        HudBounds compactBounds = HudLayoutMath.toBounds(
            new HudLayoutState(
                (double)(screenWidth - compactWidth) / 2.0 / screenWidth,
                (double)(screenHeight - COMPACT_BOTTOM_MARGIN - compactHeight) / screenHeight,
                1.0
            ),
            screenWidth,
            screenHeight,
            compactWidth,
            compactHeight,
            HudEditorModel.MINIMUM_VISIBLE_PIXELS
        );
        HudLayoutState compact = HudLayoutMath.normalized(
            compactBounds,
            screenWidth,
            screenHeight,
            1.0
        );
        HudBounds analysisBounds = HudLayoutMath.toBounds(
            new HudLayoutState(
                (double)(screenWidth - analysisWidth) / 2.0 / screenWidth,
                (double)(compactBounds.getY() - PANEL_GAP - analysisHeight) / screenHeight,
                1.0
            ),
            screenWidth,
            screenHeight,
            analysisWidth,
            analysisHeight,
            HudEditorModel.MINIMUM_VISIBLE_PIXELS
        );
        HudLayoutState analysis = HudLayoutMath.normalized(
            analysisBounds,
            screenWidth,
            screenHeight,
            1.0
        );
        return new HudLayoutDefaults(compact, analysis);
    }

    public HudLayoutState compact() {
        return this.compact;
    }

    public HudLayoutState analysis() {
        return this.analysis;
    }
}
