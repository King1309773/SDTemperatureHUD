package dev.rlcraftceaddons.sdtemperaturehud.client.config;

import dev.rlcraftceaddons.sdtemperaturehud.client.HudSettings;
import dev.rlcraftceaddons.sdtemperaturehud.client.layout.HudLayoutMath;
import dev.rlcraftceaddons.sdtemperaturehud.config.HudConfig;
import dev.rlcraftceaddons.sdtemperaturehud.config.LegacyLayoutMigration;
import java.util.EnumSet;

/** A local draft. Reading or previewing it never writes the live configuration. */
public final class AppearanceWorkingCopy {
    private double compactScale;
    private double analysisScale;
    private int backgroundAlpha;
    private final EnumSet<AppearanceItem> visible = EnumSet.noneOf(AppearanceItem.class);
    private final ThemeWorkingCopy theme;

    private AppearanceWorkingCopy(HudSettings settings) {
        compactScale = settings.getCompactLayout().getScale();
        analysisScale = settings.getAnalysisLayout().getScale();
        if (settings.isLegacyLayoutPending()) {
            compactScale = LegacyLayoutMigration.pendingCompactScale(
                settings.getScale(), compactScale, analysisScale);
            analysisScale = LegacyLayoutMigration.pendingAnalysisScale(
                settings.getScale(), settings.getCompactLayout().getScale(), analysisScale);
        }
        backgroundAlpha = settings.getBackgroundAlpha();
        for (AppearanceItem item : AppearanceItem.values()) {
            if (item.read(settings)) visible.add(item);
        }
        theme = ThemeWorkingCopy.from(HudConfig.theme);
    }

    public static AppearanceWorkingCopy fromConfig() {
        return new AppearanceWorkingCopy(HudSettings.fromConfig());
    }

    public double getCompactScale() { return compactScale; }
    public double getAnalysisScale() { return analysisScale; }
    public int getBackgroundAlpha() { return backgroundAlpha; }
    public ThemeWorkingCopy theme() { return theme; }
    public boolean isVisible(AppearanceItem item) { return visible.contains(item); }
    public void setVisible(AppearanceItem item, boolean value) {
        if (value) visible.add(item); else visible.remove(item);
    }
    public void setCompactScale(double value) { compactScale = stableScale(value); }
    public void setAnalysisScale(double value) { analysisScale = stableScale(value); }
    public void setBackgroundAlpha(int value) { backgroundAlpha = Math.max(0, Math.min(255, value)); }

    private static double stableScale(double value) {
        return Math.round(HudLayoutMath.clampScale(value) * 20.0) / 20.0;
    }

    public HudSettings preview(HudSettings base) { return base.withAppearance(this); }

    /** Validate the entire theme before committing any part of the draft. */
    public ThemeApplyResult apply() {
        ThemeApplyResult result = theme.commitTo(HudConfig.theme);
        if (!result.isSuccess()) return result;
        boolean changed = result.isChanged()
            || HudConfig.layout.compactScale != compactScale
            || HudConfig.layout.analysisScale != analysisScale
            || HudConfig.backgroundAlpha != backgroundAlpha;
        HudSettings before = HudSettings.fromConfig();
        for (AppearanceItem item : AppearanceItem.values()) {
            changed |= item.read(before) != isVisible(item);
        }
        HudConfig.layout.compactScale = compactScale;
        HudConfig.layout.analysisScale = analysisScale;
        HudConfig.backgroundAlpha = backgroundAlpha;
        HudConfig.visibility.showTrend = isVisible(AppearanceItem.TREND);
        HudConfig.visibility.showTarget = isVisible(AppearanceItem.TARGET);
        HudConfig.visibility.showAnalysisTitle = isVisible(AppearanceItem.TITLE);
        HudConfig.visibility.showBaseEnvironment = isVisible(AppearanceItem.ENVIRONMENT);
        HudConfig.visibility.showEquipment = isVisible(AppearanceItem.EQUIPMENT);
        HudConfig.visibility.showSeason = isVisible(AppearanceItem.SEASON);
        HudConfig.visibility.showNearbyHeatSources = isVisible(AppearanceItem.HEAT_SOURCE);
        HudConfig.visibility.showOtherModifiers = isVisible(AppearanceItem.OTHER);
        HudConfig.visibility.showAnalysisTarget = isVisible(AppearanceItem.ANALYSIS_TARGET);
        return ThemeApplyResult.success(changed);
    }
}

