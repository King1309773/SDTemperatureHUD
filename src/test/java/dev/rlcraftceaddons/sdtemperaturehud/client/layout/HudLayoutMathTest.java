package dev.rlcraftceaddons.sdtemperaturehud.client.layout;

import dev.rlcraftceaddons.sdtemperaturehud.client.AnalysisDisplayMode;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class HudLayoutMathTest {
    @Test
    public void exposesTheTwoHudPanelsAndThreeAnalysisDisplayModes() {
        assertArrayEquals(new HudPanel[] { HudPanel.COMPACT, HudPanel.ANALYSIS }, HudPanel.values());
        assertArrayEquals(
            new AnalysisDisplayMode[] {
                AnalysisDisplayMode.HOLD,
                AnalysisDisplayMode.TOGGLE,
                AnalysisDisplayMode.ALWAYS
            },
            AnalysisDisplayMode.values()
        );
    }

    @Test
    public void layoutStateHasValueSemantics() {
        HudLayoutState state = new HudLayoutState(0.25, 0.75, 1.25);
        HudLayoutState equalState = new HudLayoutState(0.25, 0.75, 1.25);

        assertEquals(0.25, state.getNormalizedX(), 0.0);
        assertEquals(0.75, state.getNormalizedY(), 0.0);
        assertEquals(1.25, state.getScale(), 0.0);
        assertEquals(state, equalState);
        assertEquals(state.hashCode(), equalState.hashCode());
        assertNotEquals(state, new HudLayoutState(0.25, 0.75, 1.30));
        assertEquals(state.toString(), equalState.toString());
    }

    @Test
    public void boundsHaveValueSemanticsAndExclusiveRightAndBottomEdges() {
        HudBounds bounds = new HudBounds(10, 20, 30, 40);
        HudBounds equalBounds = new HudBounds(10, 20, 30, 40);

        assertEquals(10, bounds.getX());
        assertEquals(20, bounds.getY());
        assertEquals(30, bounds.getWidth());
        assertEquals(40, bounds.getHeight());
        assertTrue(bounds.contains(10, 20));
        assertTrue(bounds.contains(39, 59));
        assertFalse(bounds.contains(40, 59));
        assertFalse(bounds.contains(39, 60));
        assertFalse(bounds.contains(9, 20));
        assertEquals(bounds, equalBounds);
        assertEquals(bounds.hashCode(), equalBounds.hashCode());
        assertNotEquals(bounds, new HudBounds(10, 20, 31, 40));
    }

    @Test
    public void boundsRejectNonpositiveDimensions() {
        assertInvalidBounds(0, 1);
        assertInvalidBounds(-1, 1);
        assertInvalidBounds(1, 0);
        assertInvalidBounds(1, -1);
    }

    @Test
    public void boundsHitTestingDoesNotOverflowAtIntegerLimits() {
        HudBounds minimum = new HudBounds(Integer.MIN_VALUE, Integer.MIN_VALUE, 2, 2);
        HudBounds maximum = new HudBounds(Integer.MAX_VALUE - 1, Integer.MAX_VALUE - 1, 2, 2);

        assertTrue(minimum.contains(Integer.MIN_VALUE, Integer.MIN_VALUE));
        assertTrue(minimum.contains(Integer.MIN_VALUE + 1, Integer.MIN_VALUE + 1));
        assertFalse(minimum.contains(Integer.MIN_VALUE + 2, Integer.MIN_VALUE + 1));
        assertTrue(maximum.contains(Integer.MAX_VALUE, Integer.MAX_VALUE));
        assertFalse(maximum.contains(Integer.MAX_VALUE - 2, Integer.MAX_VALUE));
    }

    @Test
    public void clampsScaleToSupportedRangeAndDefaultsNonFiniteValues() {
        assertEquals(0.50, HudLayoutMath.clampScale(0.10), 0.0);
        assertEquals(2.00, HudLayoutMath.clampScale(3.00), 0.0);
        assertEquals(1.25, HudLayoutMath.clampScale(1.25), 0.0);
        assertEquals(1.00, HudLayoutMath.clampScale(Double.NaN), 0.0);
        assertEquals(1.00, HudLayoutMath.clampScale(Double.POSITIVE_INFINITY), 0.0);
        assertEquals(1.00, HudLayoutMath.clampScale(Double.NEGATIVE_INFINITY), 0.0);
    }

    @Test
    public void exposesStableScaleLimitsAndStep() {
        assertEquals(0.50, HudLayoutMath.MIN_SCALE, 0.0);
        assertEquals(2.00, HudLayoutMath.MAX_SCALE, 0.0);
        assertEquals(0.05, HudLayoutMath.SCALE_STEP, 0.0);
    }

    @Test
    public void wheelAddsAnExactFivePercentDeltaWithoutGridSnapping() {
        assertEquals(1.08, HudLayoutMath.stepScale(1.03, 1), 0.000000001);
        assertEquals(0.98, HudLayoutMath.stepScale(1.03, -1), 0.000000001);
        assertEquals(1.03, HudLayoutMath.stepScale(1.03, 0), 0.0);
        assertEquals(2.00, HudLayoutMath.stepScale(2.00, 120), 0.0);
        assertEquals(0.50, HudLayoutMath.stepScale(0.50, -120), 0.0);
        assertEquals(1.05, HudLayoutMath.stepScale(Double.NaN, 1), 0.0);
    }

    @Test
    public void convertsNormalizedPositionToScaledClampedBounds() {
        HudBounds bounds = HudLayoutMath.toBounds(
            new HudLayoutState(-2.0, 3.0, 1.0),
            320,
            180,
            100,
            40,
            16
        );

        assertEquals(new HudBounds(-84, 164, 100, 40), bounds);
    }

    @Test
    public void renderPositionKeepsFractionsWhileHitBoundsRemainIntegral() {
        HudLayoutState state = new HudLayoutState(
            156.375 / 320.0,
            122.5 / 200.0,
            1.25
        );

        HudLayoutMath.RenderPosition position = HudLayoutMath.toRenderPosition(
            state,
            320,
            200,
            65,
            10,
            16
        );
        HudBounds bounds = HudLayoutMath.toBounds(state, 320, 200, 65, 10, 16);

        assertEquals(156.375, position.getX(), 0.0000001);
        assertEquals(122.5, position.getY(), 0.0000001);
        assertEquals(1.25, position.getScale(), 0.0);
        assertEquals(156, bounds.getX());
        assertEquals(123, bounds.getY());
        assertEquals(82, bounds.getWidth());
        assertEquals(13, bounds.getHeight());
    }

    @Test
    public void fractionalScaleClampKeepsExactlySixteenPixelsVisibleAtLeftAndTop() {
        HudLayoutState state = new HudLayoutState(-10.0, -10.0, 1.25);

        HudLayoutMath.RenderPosition position = HudLayoutMath.toRenderPosition(
            state,
            320,
            200,
            65,
            33,
            16
        );
        HudBounds bounds = HudLayoutMath.toBounds(state, 320, 200, 65, 33, 16);

        assertEquals(16.0, position.getX() + 65.0 * position.getScale(), 0.0000001);
        assertEquals(16.0, position.getY() + 33.0 * position.getScale(), 0.0000001);
        assertEquals(-65.25, position.getX(), 0.0000001);
        assertEquals(-25.25, position.getY(), 0.0000001);
        assertEquals(new HudBounds(-66, -26, 82, 42), bounds);
        assertTrue(bounds.contains(15, 15));
    }

    @Test
    public void fractionalScaleClampKeepsSixteenPixelsVisibleAtRightAndBottom() {
        HudLayoutState state = new HudLayoutState(10.0, 10.0, 1.25);

        HudLayoutMath.RenderPosition position = HudLayoutMath.toRenderPosition(
            state,
            320,
            200,
            65,
            33,
            16
        );
        HudBounds bounds = HudLayoutMath.toBounds(state, 320, 200, 65, 33, 16);

        assertEquals(16.0, 320.0 - position.getX(), 0.0000001);
        assertEquals(16.0, 200.0 - position.getY(), 0.0000001);
        assertEquals(304.0, position.getX(), 0.0000001);
        assertEquals(184.0, position.getY(), 0.0000001);
        assertEquals(new HudBounds(304, 184, 82, 42), bounds);
        assertTrue(bounds.contains(304, 184));
        assertTrue(bounds.contains(319, 199));
    }

    @Test
    public void roundsScaledBoundsUpAndSafelyClampsVisiblePixels() {
        HudBounds scaled = HudLayoutMath.toBounds(
            new HudLayoutState(0.25, 0.50, 1.01),
            320,
            180,
            101,
            41,
            -5
        );
        HudBounds excessiveVisibility = HudLayoutMath.toBounds(
            new HudLayoutState(-10.0, -10.0, 1.0),
            20,
            10,
            100,
            40,
            Integer.MAX_VALUE
        );

        assertEquals(new HudBounds(80, 90, 103, 42), scaled);
        assertEquals(new HudBounds(-80, -30, 100, 40), excessiveVisibility);
    }

    @Test
    public void normalizedPositionRoundTripsWithinPixelRoundingTolerance() {
        HudLayoutState original = new HudLayoutState(0.3125, 0.625, 1.25);
        HudBounds bounds = HudLayoutMath.toBounds(original, 320, 180, 100, 40, 16);
        HudLayoutState normalized = HudLayoutMath.normalized(bounds, 320, 180, original.getScale());

        assertEquals(original.getNormalizedX(), normalized.getNormalizedX(), 0.5 / 320.0);
        assertEquals(original.getNormalizedY(), normalized.getNormalizedY(), 0.5 / 180.0);
        assertEquals(1.25, normalized.getScale(), 0.0);
    }

    @Test
    public void invalidScreenSizeProducesSafeNormalizedDefaults() {
        HudLayoutState invalidWidth = HudLayoutMath.normalized(new HudBounds(10, 20, 30, 40), 0, 180, 3.0);
        HudLayoutState invalidHeight = HudLayoutMath.normalized(new HudBounds(10, 20, 30, 40), 320, -1, Double.NaN);

        assertEquals(new HudLayoutState(0.0, 0.0, 2.0), invalidWidth);
        assertEquals(new HudLayoutState(0.0, 0.0, 1.0), invalidHeight);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsNonpositiveScreenDimensionsForBounds() {
        HudLayoutMath.toBounds(new HudLayoutState(0.0, 0.0, 1.0), 0, 180, 100, 40, 16);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsNonpositiveBaseDimensionsForBounds() {
        HudLayoutMath.toBounds(new HudLayoutState(0.0, 0.0, 1.0), 320, 180, 100, -1, 16);
    }

    private static void assertInvalidBounds(int width, int height) {
        try {
            new HudBounds(0, 0, width, height);
        } catch (IllegalArgumentException expected) {
            return;
        }
        throw new AssertionError("Expected invalid dimensions: " + width + "x" + height);
    }
}
