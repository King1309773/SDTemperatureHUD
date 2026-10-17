package dev.rlcraftceaddons.sdtemperaturehud.client.editor;

import dev.rlcraftceaddons.sdtemperaturehud.client.layout.HudBounds;
import dev.rlcraftceaddons.sdtemperaturehud.client.layout.HudLayoutMath;
import dev.rlcraftceaddons.sdtemperaturehud.client.layout.HudLayoutState;
import dev.rlcraftceaddons.sdtemperaturehud.client.layout.HudPanel;
import java.util.Map;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class HudEditorModelTest {
    private static final int SCREEN_WIDTH = 320;
    private static final int SCREEN_HEIGHT = 180;
    private static final int COMPACT_WIDTH = 60;
    private static final int COMPACT_HEIGHT = 10;
    private static final int ANALYSIS_WIDTH = 100;
    private static final int ANALYSIS_HEIGHT = 70;

    @Test
    public void draggingCompactLeavesAnalysisIdenticalAndKeepsStableOffset() {
        HudLayoutState compact = new HudLayoutState(0.25, 0.50, 1.0);
        HudLayoutState analysis = new HudLayoutState(0.60, 0.20, 1.25);
        HudEditorModel model = model(compact, analysis);

        model.select(HudPanel.COMPACT);
        model.beginDrag(85, 95, SCREEN_WIDTH, SCREEN_HEIGHT, COMPACT_WIDTH, COMPACT_HEIGHT);
        model.dragTo(165, 55, SCREEN_WIDTH, SCREEN_HEIGHT, COMPACT_WIDTH, COMPACT_HEIGHT);

        assertEquals(new HudLayoutState(0.50, 50.0 / SCREEN_HEIGHT, 1.0), model.state(HudPanel.COMPACT));
        assertEquals(analysis, model.state(HudPanel.ANALYSIS));
        assertTrue(model.isDragging());
        model.endDrag();
        assertFalse(model.isDragging());
    }

    @Test
    public void resetSelectedRestoresOnlyThatPanelsDefault() {
        HudLayoutState compactDefault = new HudLayoutState(0.30, 0.70, 0.75);
        HudLayoutState analysisDefault = new HudLayoutState(0.10, 0.15, 1.50);
        HudEditorModel model = new HudEditorModel(
            new HudLayoutState(0.80, 0.20, 1.20),
            new HudLayoutState(0.65, 0.55, 0.90),
            compactDefault,
            analysisDefault
        );
        HudLayoutState analysisBefore = model.state(HudPanel.ANALYSIS);

        model.select(HudPanel.COMPACT);
        model.resetSelected(
            SCREEN_WIDTH,
            SCREEN_HEIGHT,
            COMPACT_WIDTH,
            COMPACT_HEIGHT,
            ANALYSIS_WIDTH,
            ANALYSIS_HEIGHT
        );

        assertEquals(compactDefault, model.state(HudPanel.COMPACT));
        assertEquals(analysisBefore, model.state(HudPanel.ANALYSIS));
        assertEquals(compactDefault, model.defaultState(HudPanel.COMPACT));
        assertEquals(analysisDefault, model.defaultState(HudPanel.ANALYSIS));
    }

    @Test
    public void wheelStepsSelectedScaleInExactFivePercentStepsAndClamps() {
        HudEditorModel model = model(
            new HudLayoutState(0.20, 0.20, 1.03),
            new HudLayoutState(0.70, 0.70, 1.50)
        );
        model.select(HudPanel.COMPACT);

        model.wheel(120, SCREEN_WIDTH, SCREEN_HEIGHT, COMPACT_WIDTH, COMPACT_HEIGHT);
        assertEquals(1.08, model.state(HudPanel.COMPACT).getScale(), 0.000000001);
        model.wheel(-120, SCREEN_WIDTH, SCREEN_HEIGHT, COMPACT_WIDTH, COMPACT_HEIGHT);
        assertEquals(1.03, model.state(HudPanel.COMPACT).getScale(), 0.000000001);
        assertEquals(1.50, model.state(HudPanel.ANALYSIS).getScale(), 0.0);

        for (int i = 0; i < 100; ++i) {
            model.wheel(1, SCREEN_WIDTH, SCREEN_HEIGHT, COMPACT_WIDTH, COMPACT_HEIGHT);
        }
        assertEquals(HudLayoutMath.MAX_SCALE, model.state(HudPanel.COMPACT).getScale(), 0.0);
        for (int i = 0; i < 100; ++i) {
            model.wheel(-1, SCREEN_WIDTH, SCREEN_HEIGHT, COMPACT_WIDTH, COMPACT_HEIGHT);
        }
        assertEquals(HudLayoutMath.MIN_SCALE, model.state(HudPanel.COMPACT).getScale(), 0.0);
    }

    @Test
    public void dragAndWheelClampPanelSoSixteenPixelsRemainVisible() {
        HudEditorModel model = model(
            new HudLayoutState(0.50, 0.50, 1.0),
            new HudLayoutState(0.25, 0.25, 1.0)
        );
        model.select(HudPanel.ANALYSIS);
        model.beginDrag(85, 55, SCREEN_WIDTH, SCREEN_HEIGHT, ANALYSIS_WIDTH, ANALYSIS_HEIGHT);
        model.dragTo(-1000, 1000, SCREEN_WIDTH, SCREEN_HEIGHT, ANALYSIS_WIDTH, ANALYSIS_HEIGHT);

        HudBounds dragged = HudLayoutMath.toBounds(
            model.state(HudPanel.ANALYSIS),
            SCREEN_WIDTH,
            SCREEN_HEIGHT,
            ANALYSIS_WIDTH,
            ANALYSIS_HEIGHT,
            16
        );
        assertEquals(16 - ANALYSIS_WIDTH, dragged.getX());
        assertEquals(SCREEN_HEIGHT - 16, dragged.getY());

        model.wheel(1, SCREEN_WIDTH, SCREEN_HEIGHT, ANALYSIS_WIDTH, ANALYSIS_HEIGHT);
        HudBounds scaled = HudLayoutMath.toBounds(
            model.state(HudPanel.ANALYSIS),
            SCREEN_WIDTH,
            SCREEN_HEIGHT,
            ANALYSIS_WIDTH,
            ANALYSIS_HEIGHT,
            16
        );
        assertTrue(scaled.getX() + scaled.getWidth() >= 16);
        assertEquals(SCREEN_HEIGHT - 16, scaled.getY());
    }

    @Test
    public void overlappingHitTestSelectsAnalysisBeforeCompactAndMissKeepsSelection() {
        HudEditorModel model = model(
            new HudLayoutState(0.25, 0.25, 1.0),
            new HudLayoutState(0.25, 0.25, 1.0)
        );

        assertEquals(
            HudPanel.ANALYSIS,
            model.selectAt(
                85,
                50,
                SCREEN_WIDTH,
                SCREEN_HEIGHT,
                COMPACT_WIDTH,
                COMPACT_HEIGHT,
                ANALYSIS_WIDTH,
                ANALYSIS_HEIGHT
            )
        );
        assertEquals(HudPanel.ANALYSIS, model.selected());
        assertNull(model.selectAt(
            319,
            179,
            SCREEN_WIDTH,
            SCREEN_HEIGHT,
            COMPACT_WIDTH,
            COMPACT_HEIGHT,
            ANALYSIS_WIDTH,
            ANALYSIS_HEIGHT
        ));
        assertEquals(HudPanel.ANALYSIS, model.selected());
    }

    @Test
    public void validatedStatesAreFiniteScaleClampedAndEdgeConstrained() {
        HudEditorModel model = model(
            new HudLayoutState(Double.NaN, Double.POSITIVE_INFINITY, Double.NaN),
            new HudLayoutState(Double.NEGATIVE_INFINITY, 99.0, 9.0)
        );

        Map<HudPanel, HudLayoutState> states = model.validatedStates(
            SCREEN_WIDTH,
            SCREEN_HEIGHT,
            COMPACT_WIDTH,
            COMPACT_HEIGHT,
            ANALYSIS_WIDTH,
            ANALYSIS_HEIGHT
        );

        for (HudLayoutState state : states.values()) {
            assertTrue(Double.isFinite(state.getNormalizedX()));
            assertTrue(Double.isFinite(state.getNormalizedY()));
            assertTrue(Double.isFinite(state.getScale()));
            assertTrue(state.getScale() >= HudLayoutMath.MIN_SCALE);
            assertTrue(state.getScale() <= HudLayoutMath.MAX_SCALE);
        }
        assertEquals(1.0, states.get(HudPanel.COMPACT).getScale(), 0.0);
        assertEquals(2.0, states.get(HudPanel.ANALYSIS).getScale(), 0.0);
    }

    private static HudEditorModel model(HudLayoutState compact, HudLayoutState analysis) {
        return new HudEditorModel(
            compact,
            analysis,
            new HudLayoutState(0.50, 0.75, 1.0),
            new HudLayoutState(0.50, 0.25, 1.0)
        );
    }
}
