package dev.rlcraftceaddons.sdtemperaturehud.client.config;

import dev.rlcraftceaddons.sdtemperaturehud.client.AnalysisDisplayMode;
import dev.rlcraftceaddons.sdtemperaturehud.client.layout.HudLayoutState;
import dev.rlcraftceaddons.sdtemperaturehud.config.HudConfig;
import dev.rlcraftceaddons.sdtemperaturehud.config.HudConfigResetPolicy;
import org.junit.*;
import static org.junit.Assert.*;

public class SettingsWorkingCopyTest {
    private HudConfigResetPolicy.State original;
    private boolean hints;
    private int threshold, duration;
    @Before public void capture() {
        original = HudConfigResetPolicy.capture();
        hints = HudConfig.changeHintsEnabled;
        threshold = HudConfig.changeHintThreshold;
        duration = HudConfig.changeHintDurationSeconds;
    }
    @After public void restore() {
        HudConfigResetPolicy.apply(original);
        HudConfig.changeHintsEnabled = hints;
        HudConfig.changeHintThreshold = threshold;
        HudConfig.changeHintDurationSeconds = duration;
    }
    private SettingsWorkingCopy draft() {
        SettingsWorkingCopy copy = SettingsWorkingCopy.fromConfig();
        copy.initializeLegacyPositions(new HudLayoutState(.4, .7, 1),
            new HudLayoutState(.35, .4, 1));
        return copy;
    }

    @Test public void allPagesShareAnIsolatedDraft() {
        SettingsWorkingCopy copy = draft();
        assertFalse(copy.hasChanges());
        copy.analysisEnabled = !HudConfig.analysisEnabled;
        copy.changeHintsEnabled = !hints;
        copy.appearance().setCompactScale(.7);
        assertTrue(copy.hasChanges());
        assertEquals(original, HudConfigResetPolicy.capture());
        assertEquals(hints, HudConfig.changeHintsEnabled);
    }
    @Test public void invalidColorRejectsCommonHintAndAppearanceChangesTogether() {
        SettingsWorkingCopy copy = draft();
        copy.warningSoundEnabled = !HudConfig.warningSoundEnabled;
        copy.changeHintThreshold = 5;
        copy.appearance().theme().set(ThemeColorField.TEXT, "bad");
        assertFalse(copy.apply().isSuccess());
        assertEquals(original, HudConfigResetPolicy.capture());
        assertEquals(threshold, HudConfig.changeHintThreshold);
    }
    @Test public void applyingUpdatesBaselineWithoutCommittingLaterEdits() {
        SettingsWorkingCopy copy = draft();
        copy.analysisDisplayMode = AnalysisDisplayMode.ALWAYS;
        copy.changeHintThreshold = 7;
        assertTrue(copy.apply().isSuccess());
        assertFalse(copy.hasChanges());
        copy.analysisDisplayMode = AnalysisDisplayMode.HOLD;
        copy.changeHintThreshold = 9;
        assertTrue(copy.hasChanges());
        assertEquals(AnalysisDisplayMode.ALWAYS, HudConfig.analysis.displayMode);
        assertEquals(7, HudConfig.changeHintThreshold);
    }
    @Test public void commonSettingsDoNotMigrateOrMoveLegacyLayout() {
        HudConfig.layout.version = 0;
        SettingsWorkingCopy copy = draft();
        copy.warningFlashEnabled = !HudConfig.warningFlashEnabled;
        assertTrue(copy.apply().isSuccess());
        assertEquals(0, HudConfig.layout.version);
    }
    @Test public void applyingPositionPreservesOtherPreferencesAndCompletesMigration() {
        HudConfig.layout.version = 0;
        SettingsWorkingCopy copy = draft();
        copy.setPosition(true, .2, .3);
        copy.appearance().setCompactScale(.7);
        assertTrue(copy.apply().isSuccess());
        assertEquals(1, HudConfig.layout.version);
        assertEquals(.2, HudConfig.layout.compactX, 0);
        assertEquals(.3, HudConfig.layout.compactY, 0);
        assertEquals(.7, HudConfig.layout.compactScale, 0);
        assertEquals(hints, HudConfig.changeHintsEnabled);
    }
    @Test public void compactResetDoesNotResetAnalysisOrWarnings() {
        SettingsWorkingCopy copy = draft();
        double analysisX = copy.getX(false);
        boolean sound = copy.warningSoundEnabled;
        copy.resetAppearance(0, new HudLayoutState(.45, .8, 1),
            new HudLayoutState(.4, .5, 1));
        assertEquals(.45, copy.getX(true), 0);
        assertEquals(analysisX, copy.getX(false), 0);
        assertEquals(sound, copy.warningSoundEnabled);
        assertEquals(original, HudConfigResetPolicy.capture());
    }
    @Test public void invalidPositionIsRejectedWithoutChangingDraft() {
        SettingsWorkingCopy copy = draft();
        double x = copy.getX(true);
        try { copy.setPosition(true, Double.NaN, .5); fail(); }
        catch (IllegalArgumentException expected) { assertEquals(x, copy.getX(true), 0); }
    }
    @Test public void fullAppearanceResetKeepsWarningAndAnalysisPreferences() {
        SettingsWorkingCopy copy = draft();
        copy.warningSoundEnabled = true;
        copy.analysisDisplayMode = AnalysisDisplayMode.TOGGLE;
        copy.changeHintThreshold = 8;
        copy.appearance().setBackgroundAlpha(1);
        copy.resetAppearance(2, new HudLayoutState(.45, .8, 1),
            new HudLayoutState(.4, .5, 1));
        assertEquals(214, copy.appearance().getBackgroundAlpha());
        assertTrue(copy.warningSoundEnabled);
        assertEquals(AnalysisDisplayMode.TOGGLE, copy.analysisDisplayMode);
        assertEquals(8, copy.changeHintThreshold);
        assertEquals(original, HudConfigResetPolicy.capture());
    }
}

