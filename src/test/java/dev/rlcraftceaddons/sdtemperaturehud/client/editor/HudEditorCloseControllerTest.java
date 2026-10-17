package dev.rlcraftceaddons.sdtemperaturehud.client.editor;

import dev.rlcraftceaddons.sdtemperaturehud.client.layout.HudLayoutState;
import dev.rlcraftceaddons.sdtemperaturehud.client.layout.HudPanel;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

public class HudEditorCloseControllerTest {
    @Test
    public void forcedReplacementSavesOnceAndClearsDragAndSelection() {
        HudEditorModel model = draggingModel();
        HudEditorCloseController controller = new HudEditorCloseController();
        RecordingSave save = new RecordingSave(false);
        RecordingFailure failure = new RecordingFailure();

        controller.close(model, save, failure);
        controller.close(model, save, failure);

        assertEquals(1, save.calls);
        assertEquals(0, failure.calls);
        assertFalse(model.isDragging());
        assertNull(model.selected());
    }

    @Test
    public void persistenceFailureIsReportedOnceWithoutEscapingAndStillClearsInputState() {
        HudEditorModel model = draggingModel();
        HudEditorCloseController controller = new HudEditorCloseController();
        RecordingSave save = new RecordingSave(true);
        RecordingFailure failure = new RecordingFailure();

        controller.close(model, save, failure);
        controller.close(model, save, failure);

        assertEquals(1, save.calls);
        assertEquals(1, failure.calls);
        assertEquals("disk full", failure.last.getMessage());
        assertFalse(model.isDragging());
        assertNull(model.selected());
    }

    private static HudEditorModel draggingModel() {
        HudEditorModel model = new HudEditorModel(
            new HudLayoutState(0.25, 0.75, 1.0),
            new HudLayoutState(0.25, 0.25, 1.0),
            new HudLayoutState(0.5, 0.75, 1.0),
            new HudLayoutState(0.5, 0.25, 1.0)
        );
        model.select(HudPanel.ANALYSIS);
        model.beginDrag(90, 50, 320, 180, 100, 70);
        return model;
    }

    private static final class RecordingSave
        implements HudEditorCloseController.SaveOperation {
        private final boolean fail;
        private int calls;

        private RecordingSave(boolean fail) {
            this.fail = fail;
        }

        @Override
        public void save(HudEditorModel model) {
            ++this.calls;
            if (this.fail) {
                throw new IllegalStateException("disk full");
            }
            model.validatedStates(320, 180, 60, 10, 100, 70);
        }
    }

    private static final class RecordingFailure
        implements HudEditorCloseController.FailureHandler {
        private int calls;
        private RuntimeException last;

        @Override
        public void onFailure(RuntimeException exception) {
            ++this.calls;
            this.last = exception;
        }
    }
}
