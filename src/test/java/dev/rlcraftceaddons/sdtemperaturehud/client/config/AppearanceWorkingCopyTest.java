package dev.rlcraftceaddons.sdtemperaturehud.client.config;

import dev.rlcraftceaddons.sdtemperaturehud.client.HudSettings;
import dev.rlcraftceaddons.sdtemperaturehud.config.HudConfig;
import dev.rlcraftceaddons.sdtemperaturehud.config.HudConfigResetPolicy;
import org.junit.*;
import static org.junit.Assert.*;

public class AppearanceWorkingCopyTest {
    private HudConfigResetPolicy.State original;
    @Before public void capture() { original = HudConfigResetPolicy.capture(); }
    @After public void restore() { HudConfigResetPolicy.apply(original); }

    @Test public void previewAndCancelNeverMutateSavedConfig() {
        HudConfigResetPolicy.State before = HudConfigResetPolicy.capture();
        AppearanceWorkingCopy copy = AppearanceWorkingCopy.fromConfig();
        copy.setCompactScale(0.7);
        copy.setAnalysisScale(1.4);
        copy.setBackgroundAlpha(42);
        copy.setVisible(AppearanceItem.TREND, false);
        HudSettings preview = copy.preview(HudSettings.fromConfig());
        assertEquals(0.7, preview.getCompactLayout().getScale(), 0.0);
        assertEquals(1.4, preview.getAnalysisLayout().getScale(), 0.0);
        assertEquals(42, preview.getBackgroundAlpha());
        assertFalse(preview.isShowTrend());
        assertEquals(before, HudConfigResetPolicy.capture());
    }

    @Test public void invalidColorRejectsAllAppearanceChanges() {
        HudConfigResetPolicy.State before = HudConfigResetPolicy.capture();
        AppearanceWorkingCopy copy = AppearanceWorkingCopy.fromConfig();
        copy.setCompactScale(0.7);
        copy.theme().set(ThemeColorField.TEXT, "oops");
        assertFalse(copy.apply().isSuccess());
        assertEquals(before, HudConfigResetPolicy.capture());
    }

    @Test public void applySavesScaleAndVisibilityWithoutMovingPanels() {
        AppearanceWorkingCopy copy = AppearanceWorkingCopy.fromConfig();
        double x = HudConfig.layout.compactX;
        int yOffset = HudConfig.yOffset;
        copy.setCompactScale(0.70038);
        copy.setVisible(AppearanceItem.TARGET, false);
        assertTrue(copy.apply().isSuccess());
        assertEquals(0.7, HudConfig.layout.compactScale, 0.0);
        assertFalse(HudConfig.visibility.showTarget);
        assertEquals(x, HudConfig.layout.compactX, 0.0);
        assertEquals(yOffset, HudConfig.yOffset);
    }

    @Test public void allDisplayItemsSurvivePreviewAndApply() {
        AppearanceWorkingCopy copy = AppearanceWorkingCopy.fromConfig();
        for (AppearanceItem item : AppearanceItem.values()) copy.setVisible(item, false);
        HudSettings preview = copy.preview(HudSettings.fromConfig());
        for (AppearanceItem item : AppearanceItem.values()) assertFalse(item.read(preview));
        assertTrue(copy.apply().isSuccess());
        for (AppearanceItem item : AppearanceItem.values())
            assertFalse(item.read(HudSettings.fromConfig()));
        for (AppearanceItem item : AppearanceItem.values()) copy.setVisible(item, true);
        assertTrue(copy.apply().isSuccess());
        for (AppearanceItem item : AppearanceItem.values())
            assertTrue(item.read(HudSettings.fromConfig()));
    }
}

