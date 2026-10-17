/*
 * Decompiled with CFR 0.152.
 */
package dev.rlcraftceaddons.sdtemperaturehud.client;

import dev.rlcraftceaddons.sdtemperaturehud.client.layout.HudLayoutMath;
import dev.rlcraftceaddons.sdtemperaturehud.client.config.AppearanceWorkingCopy;
import dev.rlcraftceaddons.sdtemperaturehud.client.config.AppearanceItem;
import dev.rlcraftceaddons.sdtemperaturehud.client.layout.HudLayoutState;
import dev.rlcraftceaddons.sdtemperaturehud.client.theme.HexColorParser;
import dev.rlcraftceaddons.sdtemperaturehud.client.theme.HudColorPalette;
import dev.rlcraftceaddons.sdtemperaturehud.client.theme.HudThemePreset;
import dev.rlcraftceaddons.sdtemperaturehud.config.HudConfig;
import dev.rlcraftceaddons.sdtemperaturehud.config.LegacyLayoutMigration;

public final class HudSettings {
    private static final double DEFAULT_NORMALIZED_POSITION = 0.5;
    private static final HudLayoutState DEFAULT_LAYOUT = new HudLayoutState(
        DEFAULT_NORMALIZED_POSITION,
        DEFAULT_NORMALIZED_POSITION,
        1.0
    );
    private static volatile HudSettings snapshot = createLegacyDefaults(0, 0, 1.0, 214, true, false, true);

    private final int xOffset;
    private final int yOffset;
    private final double scale;
    private final int backgroundAlpha;
    private final boolean warningFlashEnabled;
    private final boolean warningSoundEnabled;
    private final boolean analysisEnabled;
    private final HudLayoutState compactLayout;
    private final HudLayoutState analysisLayout;
    private final int layoutVersion;
    private final boolean legacyLayoutPending;
    private final AnalysisDisplayMode analysisDisplayMode;
    private final boolean showTrend;
    private final boolean showTarget;
    private final boolean showAnalysisTitle;
    private final boolean showBaseEnvironment;
    private final boolean showEquipment;
    private final boolean showSeason;
    private final boolean showNearbyHeatSources;
    private final boolean showOtherModifiers;
    private final boolean showAnalysisTarget;
    private final HudColorPalette colorPalette;
    private final HudThemePreset themePreset;
    private final boolean customPalette;

    private HudSettings(
        int xOffset,
        int yOffset,
        double scale,
        int backgroundAlpha,
        boolean warningFlashEnabled,
        boolean warningSoundEnabled,
        boolean analysisEnabled,
        HudLayoutState compactLayout,
        HudLayoutState analysisLayout,
        int layoutVersion,
        AnalysisDisplayMode analysisDisplayMode,
        boolean showTrend,
        boolean showTarget,
        boolean showAnalysisTitle,
        boolean showBaseEnvironment,
        boolean showEquipment,
        boolean showSeason,
        boolean showNearbyHeatSources,
        boolean showOtherModifiers,
        boolean showAnalysisTarget,
        HudColorPalette colorPalette,
        HudThemePreset themePreset,
        boolean customPalette
    ) {
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.scale = scale;
        this.backgroundAlpha = backgroundAlpha;
        this.warningFlashEnabled = warningFlashEnabled;
        this.warningSoundEnabled = warningSoundEnabled;
        this.analysisEnabled = analysisEnabled;
        this.compactLayout = compactLayout;
        this.analysisLayout = analysisLayout;
        this.layoutVersion = layoutVersion;
        this.legacyLayoutPending = LegacyLayoutMigration.isPending(layoutVersion);
        this.analysisDisplayMode = analysisDisplayMode;
        this.showTrend = showTrend;
        this.showTarget = showTarget;
        this.showAnalysisTitle = showAnalysisTitle;
        this.showBaseEnvironment = showBaseEnvironment;
        this.showEquipment = showEquipment;
        this.showSeason = showSeason;
        this.showNearbyHeatSources = showNearbyHeatSources;
        this.showOtherModifiers = showOtherModifiers;
        this.showAnalysisTarget = showAnalysisTarget;
        this.colorPalette = colorPalette;
        this.themePreset = themePreset;
        this.customPalette = customPalette;
    }

    public static HudSettings of(int xOffset, int yOffset, double scale, int backgroundAlpha) {
        return HudSettings.of(xOffset, yOffset, scale, backgroundAlpha, true, false, true);
    }

    public static HudSettings of(int xOffset, int yOffset, double scale, int backgroundAlpha, boolean warningFlashEnabled, boolean warningSoundEnabled) {
        return HudSettings.of(xOffset, yOffset, scale, backgroundAlpha, warningFlashEnabled, warningSoundEnabled, true);
    }

    public static HudSettings of(int xOffset, int yOffset, double scale, int backgroundAlpha, boolean warningFlashEnabled, boolean warningSoundEnabled, boolean analysisEnabled) {
        return createLegacyDefaults(
            xOffset,
            yOffset,
            scale,
            backgroundAlpha,
            warningFlashEnabled,
            warningSoundEnabled,
            analysisEnabled
        );
    }

    public static HudSettings fromConfig() {
        HudThemePreset preset = HudConfig.theme.preset == null
            ? HudThemePreset.CLASSIC
            : HudConfig.theme.preset;
        HudColorPalette presetPalette = preset.getPalette();
        HudColorPalette palette = HudConfig.theme.useCustomColors
            ? customPalette(presetPalette)
            : presetPalette;

        return new HudSettings(
            clamp(HudConfig.xOffset, -500, 500),
            clamp(HudConfig.yOffset, -500, 500),
            HudLayoutMath.clampScale(HudConfig.scale),
            clamp(HudConfig.backgroundAlpha, 0, 255),
            HudConfig.warningFlashEnabled,
            HudConfig.warningSoundEnabled,
            HudConfig.analysisEnabled,
            layoutState(HudConfig.layout.compactX, HudConfig.layout.compactY, HudConfig.layout.compactScale),
            layoutState(HudConfig.layout.analysisX, HudConfig.layout.analysisY, HudConfig.layout.analysisScale),
            HudConfig.layout.version,
            HudConfig.analysis.displayMode == null ? AnalysisDisplayMode.HOLD : HudConfig.analysis.displayMode,
            HudConfig.visibility.showTrend,
            HudConfig.visibility.showTarget,
            HudConfig.visibility.showAnalysisTitle,
            HudConfig.visibility.showBaseEnvironment,
            HudConfig.visibility.showEquipment,
            HudConfig.visibility.showSeason,
            HudConfig.visibility.showNearbyHeatSources,
            HudConfig.visibility.showOtherModifiers,
            HudConfig.visibility.showAnalysisTarget,
            palette,
            preset,
            HudConfig.theme.useCustomColors && !palette.equals(presetPalette)
        );
    }

    public static void refreshFromConfig() {
        snapshot = fromConfig();
    }

    public static HudSettings current() {
        return snapshot;
    }

    public HudSettings withLayouts(HudLayoutState compactLayout, HudLayoutState analysisLayout) {
        if (compactLayout == null || analysisLayout == null) {
            throw new IllegalArgumentException("layout states must not be null");
        }
        return new HudSettings(
            this.xOffset,
            this.yOffset,
            this.scale,
            this.backgroundAlpha,
            this.warningFlashEnabled,
            this.warningSoundEnabled,
            this.analysisEnabled,
            compactLayout,
            analysisLayout,
            Math.max(1, this.layoutVersion),
            this.analysisDisplayMode,
            this.showTrend,
            this.showTarget,
            this.showAnalysisTitle,
            this.showBaseEnvironment,
            this.showEquipment,
            this.showSeason,
            this.showNearbyHeatSources,
            this.showOtherModifiers,
            this.showAnalysisTarget,
            this.colorPalette,
            this.themePreset,
            this.customPalette
        );
    }

    public HudSettings withColorPalette(HudColorPalette colorPalette) {
        if (colorPalette == null) {
            throw new IllegalArgumentException("color palette must not be null");
        }
        return new HudSettings(
            this.xOffset,
            this.yOffset,
            this.scale,
            this.backgroundAlpha,
            this.warningFlashEnabled,
            this.warningSoundEnabled,
            this.analysisEnabled,
            this.compactLayout,
            this.analysisLayout,
            this.layoutVersion,
            this.analysisDisplayMode,
            this.showTrend,
            this.showTarget,
            this.showAnalysisTitle,
            this.showBaseEnvironment,
            this.showEquipment,
            this.showSeason,
            this.showNearbyHeatSources,
            this.showOtherModifiers,
            this.showAnalysisTarget,
            colorPalette,
            this.themePreset,
            !colorPalette.equals(this.themePreset.getPalette())
        );
    }

    public HudSettings withAppearance(AppearanceWorkingCopy draft) {
        return new HudSettings(
            this.xOffset, this.yOffset, this.scale, draft.getBackgroundAlpha(),
            this.warningFlashEnabled, this.warningSoundEnabled, this.analysisEnabled,
            new HudLayoutState(this.compactLayout.getNormalizedX(),
                this.compactLayout.getNormalizedY(), draft.getCompactScale()),
            new HudLayoutState(this.analysisLayout.getNormalizedX(),
                this.analysisLayout.getNormalizedY(), draft.getAnalysisScale()),
            this.layoutVersion, this.analysisDisplayMode,
            draft.isVisible(AppearanceItem.TREND), draft.isVisible(AppearanceItem.TARGET),
            draft.isVisible(AppearanceItem.TITLE), draft.isVisible(AppearanceItem.ENVIRONMENT),
            draft.isVisible(AppearanceItem.EQUIPMENT), draft.isVisible(AppearanceItem.SEASON),
            draft.isVisible(AppearanceItem.HEAT_SOURCE), draft.isVisible(AppearanceItem.OTHER),
            draft.isVisible(AppearanceItem.ANALYSIS_TARGET), draft.theme().previewPalette(),
            draft.theme().getSelectedPreset(), draft.theme().isCustom()
        );
    }

    public int getXOffset() {
        return this.xOffset;
    }

    public int getYOffset() {
        return this.yOffset;
    }

    public double getScale() {
        return this.scale;
    }

    public int getBackgroundAlpha() {
        return this.backgroundAlpha;
    }

    public boolean isWarningFlashEnabled() {
        return this.warningFlashEnabled;
    }

    public boolean isWarningSoundEnabled() {
        return this.warningSoundEnabled;
    }

    public boolean isAnalysisEnabled() {
        return this.analysisEnabled;
    }

    public HudLayoutState getCompactLayout() {
        return this.compactLayout;
    }

    public HudLayoutState getAnalysisLayout() {
        return this.analysisLayout;
    }

    public int getLayoutVersion() {
        return this.layoutVersion;
    }

    public boolean isLegacyLayoutPending() {
        return this.legacyLayoutPending;
    }

    public AnalysisDisplayMode getAnalysisDisplayMode() {
        return this.analysisDisplayMode;
    }

    public boolean isShowTrend() {
        return this.showTrend;
    }

    public boolean isShowTarget() {
        return this.showTarget;
    }

    public boolean isShowAnalysisTitle() {
        return this.showAnalysisTitle;
    }

    public boolean isShowBaseEnvironment() {
        return this.showBaseEnvironment;
    }

    public boolean isShowEquipment() {
        return this.showEquipment;
    }

    public boolean isShowSeason() {
        return this.showSeason;
    }

    public boolean isShowNearbyHeatSources() {
        return this.showNearbyHeatSources;
    }

    public boolean isShowOtherModifiers() {
        return this.showOtherModifiers;
    }

    public boolean isShowAnalysisTarget() {
        return this.showAnalysisTarget;
    }

    public HudColorPalette getColorPalette() {
        return this.colorPalette;
    }

    public HudThemePreset getThemePreset() {
        return this.themePreset;
    }

    public boolean isCustomPalette() {
        return this.customPalette;
    }

    private static HudSettings createLegacyDefaults(
        int xOffset,
        int yOffset,
        double scale,
        int backgroundAlpha,
        boolean warningFlashEnabled,
        boolean warningSoundEnabled,
        boolean analysisEnabled
    ) {
        return new HudSettings(
            clamp(xOffset, -500, 500),
            clamp(yOffset, -500, 500),
            HudLayoutMath.clampScale(scale),
            clamp(backgroundAlpha, 0, 255),
            warningFlashEnabled,
            warningSoundEnabled,
            analysisEnabled,
            DEFAULT_LAYOUT,
            DEFAULT_LAYOUT,
            0,
            AnalysisDisplayMode.HOLD,
            true,
            true,
            true,
            true,
            true,
            true,
            true,
            true,
            true,
            HudThemePreset.CLASSIC.getPalette(),
            HudThemePreset.CLASSIC,
            false
        );
    }

    private static HudLayoutState layoutState(double x, double y, double scale) {
        return new HudLayoutState(
            finiteOrDefault(x, DEFAULT_NORMALIZED_POSITION),
            finiteOrDefault(y, DEFAULT_NORMALIZED_POSITION),
            HudLayoutMath.clampScale(scale)
        );
    }

    private static double finiteOrDefault(double value, double defaultValue) {
        return Double.isFinite(value) ? value : defaultValue;
    }

    private static HudColorPalette customPalette(HudColorPalette fallback) {
        return new HudColorPalette(
            parseColorOrFallback(HudConfig.theme.severeCold, fallback.getSevereCold()),
            parseColorOrFallback(HudConfig.theme.cold, fallback.getCold()),
            parseColorOrFallback(HudConfig.theme.comfortable, fallback.getComfortable()),
            parseColorOrFallback(HudConfig.theme.warm, fallback.getWarm()),
            parseColorOrFallback(HudConfig.theme.severeHot, fallback.getSevereHot()),
            parseColorOrFallback(HudConfig.theme.rising, fallback.getRising()),
            parseColorOrFallback(HudConfig.theme.falling, fallback.getFalling()),
            parseColorOrFallback(HudConfig.theme.stable, fallback.getStable()),
            parseColorOrFallback(HudConfig.theme.background, fallback.getBackground()),
            parseColorOrFallback(HudConfig.theme.border, fallback.getBorder()),
            parseColorOrFallback(HudConfig.theme.text, fallback.getText())
        );
    }

    private static int parseColorOrFallback(String value, int fallback) {
        try {
            return HexColorParser.parse(value);
        }
        catch (IllegalArgumentException ignored) {
            return fallback;
        }
    }

    private static int clamp(int value, int minimum, int maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }
}
