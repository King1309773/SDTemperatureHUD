package dev.rlcraftceaddons.sdtemperaturehud.config;

import dev.rlcraftceaddons.sdtemperaturehud.client.layout.HudLayoutMath;

/** Keeps values produced by Forge's pixel-based sliders stable across saves. */
public final class HudConfigNormalizer {
    private HudConfigNormalizer() {
    }

    public static boolean normalize() {
        double compactScale = stableScale(HudConfig.layout.compactScale);
        double analysisScale = stableScale(HudConfig.layout.analysisScale);
        boolean changed = Double.compare(compactScale, HudConfig.layout.compactScale) != 0
            || Double.compare(analysisScale, HudConfig.layout.analysisScale) != 0;
        HudConfig.layout.compactScale = compactScale;
        HudConfig.layout.analysisScale = analysisScale;
        return changed;
    }

    static double stableScale(double value) {
        double safe = Double.isFinite(value) ? value : 1.0;
        double clamped = HudLayoutMath.clampScale(safe);
        double snapped = Math.round(clamped / HudLayoutMath.SCALE_STEP)
            * HudLayoutMath.SCALE_STEP;
        return Math.round(snapped * 100.0) / 100.0;
    }
}
