package dev.rlcraftceaddons.sdtemperaturehud.client.config;

import dev.rlcraftceaddons.sdtemperaturehud.client.AnalysisDisplayMode;
import dev.rlcraftceaddons.sdtemperaturehud.client.HudSettings;
import dev.rlcraftceaddons.sdtemperaturehud.client.layout.HudLayoutState;
import dev.rlcraftceaddons.sdtemperaturehud.client.theme.HudThemePreset;
import dev.rlcraftceaddons.sdtemperaturehud.config.HudConfig;
import java.util.ArrayList;
import java.util.List;

/** All four pages edit this transaction, never the live settings. */
public final class SettingsWorkingCopy {
    public boolean analysisEnabled;
    public AnalysisDisplayMode analysisDisplayMode;
    public boolean warningFlashEnabled;
    public boolean warningSoundEnabled;
    public boolean changeHintsEnabled;
    public int changeHintThreshold;
    public int changeHintDurationSeconds;
    public boolean minimapEnabled, worldMapEnabled;
    private final AppearanceWorkingCopy appearance;
    private double compactX, compactY, analysisX, analysisY;
    private double savedCompactX, savedCompactY, savedAnalysisX, savedAnalysisY;
    private double savedCompactScale, savedAnalysisScale;
    private boolean positionsReady;
    private List<Object> baseline;

    private SettingsWorkingCopy() {
        HudSettings settings = HudSettings.fromConfig();
        appearance = AppearanceWorkingCopy.fromConfig();
        analysisEnabled = settings.isAnalysisEnabled();
        analysisDisplayMode = settings.getAnalysisDisplayMode();
        warningFlashEnabled = settings.isWarningFlashEnabled();
        warningSoundEnabled = settings.isWarningSoundEnabled();
        changeHintsEnabled = HudConfig.changeHintsEnabled;
        changeHintThreshold = Math.max(1, Math.min(25, HudConfig.changeHintThreshold));
        changeHintDurationSeconds = Math.max(1, Math.min(10, HudConfig.changeHintDurationSeconds));
        minimapEnabled = HudConfig.mapIntegration.minimapEnabled;
        worldMapEnabled = HudConfig.mapIntegration.worldMapEnabled;
        compactX = settings.getCompactLayout().getNormalizedX();
        compactY = settings.getCompactLayout().getNormalizedY();
        analysisX = settings.getAnalysisLayout().getNormalizedX();
        analysisY = settings.getAnalysisLayout().getNormalizedY();
        positionsReady = !settings.isLegacyLayoutPending();
        rememberBaseline();
    }

    public static SettingsWorkingCopy fromConfig() { return new SettingsWorkingCopy(); }
    public AppearanceWorkingCopy appearance() { return appearance; }
    public double getX(boolean compact) { return compact ? compactX : analysisX; }
    public double getY(boolean compact) { return compact ? compactY : analysisY; }
    public boolean hasChanges() { return !values().equals(baseline); }

    public void initializeLegacyPositions(HudLayoutState compact, HudLayoutState analysis) {
        if (positionsReady) return;
        compactX = compact.getNormalizedX(); compactY = compact.getNormalizedY();
        analysisX = analysis.getNormalizedX(); analysisY = analysis.getNormalizedY();
        positionsReady = true;
        rememberBaseline();
    }

    public void setPosition(boolean compact, double x, double y) {
        if (!Double.isFinite(x) || !Double.isFinite(y) || x < -2 || x > 3 || y < -2 || y > 3)
            throw new IllegalArgumentException("position must be finite and within -200% to 300%");
        if (compact) { compactX = x; compactY = y; }
        else { analysisX = x; analysisY = y; }
    }

    public void resetAppearance(int scope, HudLayoutState compactDefault, HudLayoutState analysisDefault) {
        if (scope == 0 || scope == 2) {
            compactX = compactDefault.getNormalizedX(); compactY = compactDefault.getNormalizedY();
            appearance.setCompactScale(compactDefault.getScale());
        }
        if (scope == 1 || scope == 2) {
            analysisX = analysisDefault.getNormalizedX(); analysisY = analysisDefault.getNormalizedY();
            appearance.setAnalysisScale(analysisDefault.getScale());
        }
        if (scope == 2) {
            appearance.setBackgroundAlpha(214);
            appearance.theme().selectPreset(HudThemePreset.CLASSIC);
            for (AppearanceItem item : AppearanceItem.values()) appearance.setVisible(item, true);
        }
    }

    public ThemeApplyResult apply() {
        boolean layoutChanged = compactX != savedCompactX || compactY != savedCompactY
            || analysisX != savedAnalysisX || analysisY != savedAnalysisY
            || appearance.getCompactScale() != savedCompactScale
            || appearance.getAnalysisScale() != savedAnalysisScale;
        if (layoutChanged && !positionsReady)
            throw new IllegalStateException("legacy positions must be measured before applying layout changes");
        boolean changed = hasChanges();
        ThemeApplyResult result = appearance.apply();
        if (!result.isSuccess()) return result;
        HudConfig.analysisEnabled = analysisEnabled;
        HudConfig.analysis.displayMode = analysisDisplayMode;
        HudConfig.warningFlashEnabled = warningFlashEnabled;
        HudConfig.warningSoundEnabled = warningSoundEnabled;
        HudConfig.changeHintsEnabled = changeHintsEnabled;
        changeHintThreshold = Math.max(1, Math.min(25, changeHintThreshold));
        changeHintDurationSeconds = Math.max(1, Math.min(10, changeHintDurationSeconds));
        HudConfig.changeHintThreshold = changeHintThreshold;
        HudConfig.changeHintDurationSeconds = changeHintDurationSeconds;
        HudConfig.mapIntegration.minimapEnabled = minimapEnabled;
        HudConfig.mapIntegration.worldMapEnabled = worldMapEnabled;
        if (layoutChanged) {
            HudConfig.layout.compactX = compactX; HudConfig.layout.compactY = compactY;
            HudConfig.layout.analysisX = analysisX; HudConfig.layout.analysisY = analysisY;
            HudConfig.layout.version = Math.max(1, HudConfig.layout.version);
        }
        rememberBaseline();
        return ThemeApplyResult.success(changed || result.isChanged());
    }

    private void rememberBaseline() {
        savedCompactX = compactX; savedCompactY = compactY;
        savedAnalysisX = analysisX; savedAnalysisY = analysisY;
        savedCompactScale = appearance.getCompactScale();
        savedAnalysisScale = appearance.getAnalysisScale();
        baseline = values();
    }

    private List<Object> values() {
        List<Object> values = new ArrayList<Object>();
        java.util.Collections.addAll(values, analysisEnabled, analysisDisplayMode,
            warningFlashEnabled, warningSoundEnabled, changeHintsEnabled, minimapEnabled, worldMapEnabled,
            changeHintThreshold, changeHintDurationSeconds, compactX, compactY, analysisX, analysisY,
            appearance.getCompactScale(), appearance.getAnalysisScale(), appearance.getBackgroundAlpha(),
            appearance.theme().getSelectedPreset(), appearance.theme().values());
        for (AppearanceItem item : AppearanceItem.values()) values.add(appearance.isVisible(item));
        return values;
    }
}

