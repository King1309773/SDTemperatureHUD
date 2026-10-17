package dev.rlcraftceaddons.sdtemperaturehud.config;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class HudConfigNormalizerTest {
    private double originalCompactScale;
    private double originalAnalysisScale;

    @Before
    public void rememberConfig() {
        this.originalCompactScale = HudConfig.layout.compactScale;
        this.originalAnalysisScale = HudConfig.layout.analysisScale;
    }

    @After
    public void restoreConfig() {
        HudConfig.layout.compactScale = this.originalCompactScale;
        HudConfig.layout.analysisScale = this.originalAnalysisScale;
    }

    @Test
    public void snapsForgeSliderFractionsToStableFivePercentSteps() {
        HudConfig.layout.compactScale = 0.700381688773632;
        HudConfig.layout.analysisScale = 0.6941;

        assertTrue(HudConfigNormalizer.normalize());
        assertEquals(0.70, HudConfig.layout.compactScale, 0.0);
        assertEquals(0.70, HudConfig.layout.analysisScale, 0.0);
        assertEquals("0.7", Double.toString(HudConfig.layout.compactScale));
        assertEquals("0.7", Double.toString(HudConfig.layout.analysisScale));
    }

    @Test
    public void leavesAlreadyStableScalesUnchanged() {
        HudConfig.layout.compactScale = 0.70;
        HudConfig.layout.analysisScale = 1.25;

        assertFalse(HudConfigNormalizer.normalize());
    }

    @Test
    public void clampsInvalidScaleValuesBeforeSnapping() {
        HudConfig.layout.compactScale = Double.NaN;
        HudConfig.layout.analysisScale = 2.4;

        assertTrue(HudConfigNormalizer.normalize());
        assertEquals(1.0, HudConfig.layout.compactScale, 0.0);
        assertEquals(2.0, HudConfig.layout.analysisScale, 0.0);
    }
}
