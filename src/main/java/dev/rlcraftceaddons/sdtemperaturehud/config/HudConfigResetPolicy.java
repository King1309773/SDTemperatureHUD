package dev.rlcraftceaddons.sdtemperaturehud.config;

import dev.rlcraftceaddons.sdtemperaturehud.client.AnalysisDisplayMode;
import dev.rlcraftceaddons.sdtemperaturehud.client.layout.HudLayoutState;
import dev.rlcraftceaddons.sdtemperaturehud.client.theme.HexColorParser;
import dev.rlcraftceaddons.sdtemperaturehud.client.theme.HudColorPalette;
import dev.rlcraftceaddons.sdtemperaturehud.client.theme.HudThemePreset;
import java.util.Arrays;

public final class HudConfigResetPolicy {
    private HudConfigResetPolicy() {
    }

    public static synchronized State capture() {
        return new State();
    }

    public static State resetCompact(
        State before,
        HudLayoutState compactDefault,
        LegacyLayoutMigration.PanelLayouts currentVisualLayouts
    ) {
        return new State(
            requireState(before),
            requireLayout(compactDefault),
            null,
            false,
            requireLayouts(currentVisualLayouts)
        );
    }

    public static State resetAnalysis(
        State before,
        HudLayoutState analysisDefault,
        LegacyLayoutMigration.PanelLayouts currentVisualLayouts
    ) {
        return new State(
            requireState(before),
            null,
            requireLayout(analysisDefault),
            false,
            requireLayouts(currentVisualLayouts)
        );
    }

    public static State resetFullAppearance(
        State before,
        HudLayoutState compactDefault,
        HudLayoutState analysisDefault
    ) {
        return new State(
            requireState(before),
            requireLayout(compactDefault),
            requireLayout(analysisDefault),
            true,
            null
        );
    }

    public static synchronized void apply(State state) {
        State value = requireState(state);
        HudConfig.xOffset = value.xOffset;
        HudConfig.yOffset = value.yOffset;
        HudConfig.scale = value.legacyScale;
        HudConfig.backgroundAlpha = value.backgroundAlpha;
        HudConfig.warningFlashEnabled = value.warningFlashEnabled;
        HudConfig.warningSoundEnabled = value.warningSoundEnabled;
        HudConfig.analysisEnabled = value.analysisEnabled;

        HudConfig.layout.version = value.layoutVersion;
        HudConfig.layout.compactX = value.compactX;
        HudConfig.layout.compactY = value.compactY;
        HudConfig.layout.compactScale = value.compactScale;
        HudConfig.layout.analysisX = value.analysisX;
        HudConfig.layout.analysisY = value.analysisY;
        HudConfig.layout.analysisScale = value.analysisScale;
        HudConfig.analysis.displayMode = value.analysisDisplayMode;

        HudConfig.visibility.showTrend = value.showTrend;
        HudConfig.visibility.showTarget = value.showTarget;
        HudConfig.visibility.showAnalysisTitle = value.showAnalysisTitle;
        HudConfig.visibility.showBaseEnvironment = value.showBaseEnvironment;
        HudConfig.visibility.showEquipment = value.showEquipment;
        HudConfig.visibility.showSeason = value.showSeason;
        HudConfig.visibility.showNearbyHeatSources = value.showNearbyHeatSources;
        HudConfig.visibility.showOtherModifiers = value.showOtherModifiers;
        HudConfig.visibility.showAnalysisTarget = value.showAnalysisTarget;

        HudConfig.theme.preset = value.themePreset;
        HudConfig.theme.useCustomColors = value.useCustomColors;
        HudConfig.theme.severeCold = value.severeCold;
        HudConfig.theme.cold = value.cold;
        HudConfig.theme.comfortable = value.comfortable;
        HudConfig.theme.warm = value.warm;
        HudConfig.theme.severeHot = value.severeHot;
        HudConfig.theme.rising = value.rising;
        HudConfig.theme.falling = value.falling;
        HudConfig.theme.stable = value.stable;
        HudConfig.theme.background = value.background;
        HudConfig.theme.border = value.border;
        HudConfig.theme.text = value.text;
    }

    private static State requireState(State state) {
        if (state == null) {
            throw new IllegalArgumentException("config state must not be null");
        }
        return state;
    }

    private static HudLayoutState requireLayout(HudLayoutState layout) {
        if (layout == null) {
            throw new IllegalArgumentException("layout default must not be null");
        }
        return layout;
    }

    private static LegacyLayoutMigration.PanelLayouts requireLayouts(
        LegacyLayoutMigration.PanelLayouts layouts
    ) {
        if (layouts == null) {
            throw new IllegalArgumentException("current visual layouts must not be null");
        }
        return layouts;
    }

    public static final class State {
        final int xOffset;
        final int yOffset;
        final double legacyScale;
        final int backgroundAlpha;
        final boolean warningFlashEnabled;
        final boolean warningSoundEnabled;
        final boolean analysisEnabled;
        final int layoutVersion;
        final double compactX;
        final double compactY;
        final double compactScale;
        final double analysisX;
        final double analysisY;
        final double analysisScale;
        final AnalysisDisplayMode analysisDisplayMode;
        final boolean showTrend;
        final boolean showTarget;
        final boolean showAnalysisTitle;
        final boolean showBaseEnvironment;
        final boolean showEquipment;
        final boolean showSeason;
        final boolean showNearbyHeatSources;
        final boolean showOtherModifiers;
        final boolean showAnalysisTarget;
        final HudThemePreset themePreset;
        final boolean useCustomColors;
        final String severeCold;
        final String cold;
        final String comfortable;
        final String warm;
        final String severeHot;
        final String rising;
        final String falling;
        final String stable;
        final String background;
        final String border;
        final String text;

        private State() {
            this.xOffset = HudConfig.xOffset;
            this.yOffset = HudConfig.yOffset;
            this.legacyScale = HudConfig.scale;
            this.backgroundAlpha = HudConfig.backgroundAlpha;
            this.warningFlashEnabled = HudConfig.warningFlashEnabled;
            this.warningSoundEnabled = HudConfig.warningSoundEnabled;
            this.analysisEnabled = HudConfig.analysisEnabled;
            this.layoutVersion = HudConfig.layout.version;
            this.compactX = HudConfig.layout.compactX;
            this.compactY = HudConfig.layout.compactY;
            this.compactScale = HudConfig.layout.compactScale;
            this.analysisX = HudConfig.layout.analysisX;
            this.analysisY = HudConfig.layout.analysisY;
            this.analysisScale = HudConfig.layout.analysisScale;
            this.analysisDisplayMode = HudConfig.analysis.displayMode;
            this.showTrend = HudConfig.visibility.showTrend;
            this.showTarget = HudConfig.visibility.showTarget;
            this.showAnalysisTitle = HudConfig.visibility.showAnalysisTitle;
            this.showBaseEnvironment = HudConfig.visibility.showBaseEnvironment;
            this.showEquipment = HudConfig.visibility.showEquipment;
            this.showSeason = HudConfig.visibility.showSeason;
            this.showNearbyHeatSources = HudConfig.visibility.showNearbyHeatSources;
            this.showOtherModifiers = HudConfig.visibility.showOtherModifiers;
            this.showAnalysisTarget = HudConfig.visibility.showAnalysisTarget;
            this.themePreset = HudConfig.theme.preset;
            this.useCustomColors = HudConfig.theme.useCustomColors;
            this.severeCold = HudConfig.theme.severeCold;
            this.cold = HudConfig.theme.cold;
            this.comfortable = HudConfig.theme.comfortable;
            this.warm = HudConfig.theme.warm;
            this.severeHot = HudConfig.theme.severeHot;
            this.rising = HudConfig.theme.rising;
            this.falling = HudConfig.theme.falling;
            this.stable = HudConfig.theme.stable;
            this.background = HudConfig.theme.background;
            this.border = HudConfig.theme.border;
            this.text = HudConfig.theme.text;
        }

        private State(
            State before,
            HudLayoutState compactDefault,
            HudLayoutState analysisDefault,
            boolean fullAppearance,
            LegacyLayoutMigration.PanelLayouts currentVisualLayouts
        ) {
            this.xOffset = before.xOffset;
            this.yOffset = before.yOffset;
            this.legacyScale = before.legacyScale;
            this.backgroundAlpha = before.backgroundAlpha;
            this.warningFlashEnabled = before.warningFlashEnabled;
            this.warningSoundEnabled = before.warningSoundEnabled;
            this.analysisEnabled = before.analysisEnabled;
            boolean scopedMigration = !fullAppearance
                && LegacyLayoutMigration.isPending(before.layoutVersion);
            HudLayoutState currentCompact = scopedMigration
                ? currentVisualLayouts.compact()
                : null;
            HudLayoutState currentAnalysis = scopedMigration
                ? currentVisualLayouts.analysis()
                : null;
            this.layoutVersion = fullAppearance || scopedMigration
                ? Math.max(1, before.layoutVersion)
                : before.layoutVersion;
            this.compactX = compactDefault == null
                ? (scopedMigration ? currentCompact.getNormalizedX() : before.compactX)
                : compactDefault.getNormalizedX();
            this.compactY = compactDefault == null
                ? (scopedMigration ? currentCompact.getNormalizedY() : before.compactY)
                : compactDefault.getNormalizedY();
            this.compactScale = compactDefault == null
                ? (scopedMigration ? currentCompact.getScale() : before.compactScale)
                : compactDefault.getScale();
            this.analysisX = analysisDefault == null
                ? (scopedMigration ? currentAnalysis.getNormalizedX() : before.analysisX)
                : analysisDefault.getNormalizedX();
            this.analysisY = analysisDefault == null
                ? (scopedMigration ? currentAnalysis.getNormalizedY() : before.analysisY)
                : analysisDefault.getNormalizedY();
            this.analysisScale = analysisDefault == null
                ? (scopedMigration ? currentAnalysis.getScale() : before.analysisScale)
                : analysisDefault.getScale();
            this.analysisDisplayMode = before.analysisDisplayMode;
            this.showTrend = fullAppearance || before.showTrend;
            this.showTarget = fullAppearance || before.showTarget;
            this.showAnalysisTitle = fullAppearance || before.showAnalysisTitle;
            this.showBaseEnvironment = fullAppearance || before.showBaseEnvironment;
            this.showEquipment = fullAppearance || before.showEquipment;
            this.showSeason = fullAppearance || before.showSeason;
            this.showNearbyHeatSources = fullAppearance || before.showNearbyHeatSources;
            this.showOtherModifiers = fullAppearance || before.showOtherModifiers;
            this.showAnalysisTarget = fullAppearance || before.showAnalysisTarget;

            HudColorPalette classic = HudThemePreset.CLASSIC.getPalette();
            this.themePreset = fullAppearance ? HudThemePreset.CLASSIC : before.themePreset;
            this.useCustomColors = fullAppearance ? false : before.useCustomColors;
            this.severeCold = color(fullAppearance, classic.getSevereCold(), before.severeCold);
            this.cold = color(fullAppearance, classic.getCold(), before.cold);
            this.comfortable = color(fullAppearance, classic.getComfortable(), before.comfortable);
            this.warm = color(fullAppearance, classic.getWarm(), before.warm);
            this.severeHot = color(fullAppearance, classic.getSevereHot(), before.severeHot);
            this.rising = color(fullAppearance, classic.getRising(), before.rising);
            this.falling = color(fullAppearance, classic.getFalling(), before.falling);
            this.stable = color(fullAppearance, classic.getStable(), before.stable);
            this.background = color(fullAppearance, classic.getBackground(), before.background);
            this.border = color(fullAppearance, classic.getBorder(), before.border);
            this.text = color(fullAppearance, classic.getText(), before.text);
        }

        boolean allVisibilityEnabled() {
            return this.showTrend
                && this.showTarget
                && this.showAnalysisTitle
                && this.showBaseEnvironment
                && this.showEquipment
                && this.showSeason
                && this.showNearbyHeatSources
                && this.showOtherModifiers
                && this.showAnalysisTarget;
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof State
                && Arrays.deepEquals(this.values(), ((State)other).values());
        }

        @Override
        public int hashCode() {
            return Arrays.deepHashCode(this.values());
        }

        private Object[] values() {
            return new Object[] {
                this.xOffset, this.yOffset, this.legacyScale, this.backgroundAlpha,
                this.warningFlashEnabled, this.warningSoundEnabled, this.analysisEnabled,
                this.layoutVersion, this.compactX, this.compactY, this.compactScale,
                this.analysisX, this.analysisY, this.analysisScale, this.analysisDisplayMode,
                this.showTrend, this.showTarget, this.showAnalysisTitle,
                this.showBaseEnvironment, this.showEquipment, this.showSeason,
                this.showNearbyHeatSources, this.showOtherModifiers, this.showAnalysisTarget,
                this.themePreset, this.useCustomColors, this.severeCold, this.cold,
                this.comfortable, this.warm, this.severeHot, this.rising, this.falling,
                this.stable, this.background, this.border, this.text
            };
        }

        private static String color(boolean reset, int classic, String previous) {
            return reset ? HexColorParser.format(classic) : previous;
        }
    }
}
