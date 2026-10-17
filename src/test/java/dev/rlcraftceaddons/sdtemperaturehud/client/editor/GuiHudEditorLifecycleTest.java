package dev.rlcraftceaddons.sdtemperaturehud.client.editor;

import dev.rlcraftceaddons.sdtemperaturehud.client.HudSettings;
import dev.rlcraftceaddons.sdtemperaturehud.client.RendererTestFactory;
import dev.rlcraftceaddons.sdtemperaturehud.client.TemperatureHudRenderer;
import dev.rlcraftceaddons.sdtemperaturehud.client.layout.HudLayoutState;
import dev.rlcraftceaddons.sdtemperaturehud.client.layout.HudPanel;
import dev.rlcraftceaddons.sdtemperaturehud.temperature.TemperatureBreakdown;
import dev.rlcraftceaddons.sdtemperaturehud.temperature.TemperatureSnapshot;
import dev.rlcraftceaddons.sdtemperaturehud.temperature.TemperatureSnapshotProvider;
import dev.rlcraftceaddons.sdtemperaturehud.temperature.TemperatureSource;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.lwjgl.input.Keyboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class GuiHudEditorLifecycleTest {
    @Test
    public void actualEditorCloseSavesValidatedStateOnceAndClearsEveryTransientReference() {
        Fixture fixture = fixture(FailurePoint.NONE);

        fixture.editor.onGuiClosed();
        fixture.editor.onGuiClosed();

        assertEquals(1, fixture.persistence.applyCalls);
        assertEquals(1, fixture.persistence.syncCalls);
        assertEquals(1, fixture.persistence.refreshCalls);
        assertEquals(0, fixture.failures.calls);
        assertNotNull(fixture.persistence.saved);
        assertNotEquals(fixture.originalCompact, fixture.persistence.saved.get(HudPanel.COMPACT));
        assertNotEquals(fixture.originalAnalysis, fixture.persistence.saved.get(HudPanel.ANALYSIS));
        assertFalse(fixture.model.isDragging());
        assertNull(fixture.model.selected());
        fixture.assertCleared();
    }

    @Test
    public void syncFailureStillRefreshesAndRepeatedCloseIsHarmless() {
        Fixture fixture = fixture(FailurePoint.SYNC);

        fixture.editor.closeForLifecycle();
        fixture.editor.onGuiClosed();

        assertEquals(1, fixture.persistence.applyCalls);
        assertEquals(1, fixture.persistence.syncCalls);
        assertEquals(1, fixture.persistence.refreshCalls);
        assertEquals(1, fixture.failures.calls);
        assertEquals("sync failed", fixture.failures.last.getMessage());
        fixture.assertCleared();
    }

    @Test
    public void applyFailureStillRefreshesAndDoesNotEscapeTheCloseTransaction() {
        Fixture fixture = fixture(FailurePoint.APPLY);

        fixture.editor.closeForLifecycle();

        assertEquals(1, fixture.persistence.applyCalls);
        assertEquals(0, fixture.persistence.syncCalls);
        assertEquals(1, fixture.persistence.refreshCalls);
        assertEquals(1, fixture.failures.calls);
        assertEquals("apply failed", fixture.failures.last.getMessage());
        fixture.assertCleared();
    }

    @Test
    public void escapeUsesTheSameCloseTransactionBeforeRequestingScreenClose()
        throws IOException {
        Fixture fixture = fixture(FailurePoint.NONE);

        fixture.editor.keyTyped('\0', Keyboard.KEY_ESCAPE);

        assertEquals(1, fixture.persistence.applyCalls);
        assertEquals(1, fixture.persistence.refreshCalls);
        assertEquals(1, fixture.screenCloser.calls);
        fixture.assertCleared();
        fixture.editor.onGuiClosed();
        assertEquals(1, fixture.persistence.applyCalls);
    }

    static Fixture fixture(FailurePoint failurePoint) {
        HudSettings settings = HudSettings.of(0, 0, 1.0, 214);
        TemperatureSnapshot snapshot = TemperatureSnapshot.withAnalysis(
            11,
            new TemperatureBreakdown(11, 1, -1, 2, 0, 13)
        );
        TemperatureSnapshotProvider provider = new TemperatureSnapshotProvider(
            new TemperatureSource() {
                @Override
                public TemperatureSnapshot read() {
                    return TemperatureSnapshot.unavailable();
                }
            }
        );
        TemperatureHudRenderer renderer = RendererTestFactory.fixedWidth(1);
        TemperatureHudRenderer.CompactPanel compact = renderer.measureCompact(snapshot, settings);
        TemperatureHudRenderer.AnalysisPanel analysis = analysisPanel(110, 72);
        HudLayoutState originalCompact = new HudLayoutState(-4.0, 3.0, 1.15);
        HudLayoutState originalAnalysis = new HudLayoutState(5.0, -2.0, 0.85);
        HudEditorModel model = new HudEditorModel(
            originalCompact,
            originalAnalysis,
            new HudLayoutState(0.40, 0.70, 1.0),
            new HudLayoutState(0.30, 0.20, 1.0)
        );
        model.select(HudPanel.ANALYSIS);
        model.beginDrag(70, 45, 320, 180, analysis.getWidth(), analysis.getHeight());
        RecordingPersistence persistence = new RecordingPersistence(failurePoint);
        RecordingScreenCloser screenCloser = new RecordingScreenCloser();
        RecordingFailure failures = new RecordingFailure();
        GuiHudEditor editor = new GuiHudEditor(
            provider,
            renderer,
            settings,
            persistence,
            screenCloser,
            failures
        );
        editor.width = 320;
        editor.height = 180;
        setField(editor, "model", model);
        setField(editor, "previewSnapshot", snapshot);
        setField(editor, "compactPanel", compact);
        setField(editor, "analysisPanel", analysis);
        setField(editor, "compactWidth", compact.getWidth());
        setField(editor, "compactHeight", compact.getHeight());
        setField(editor, "analysisWidth", analysis.getWidth());
        setField(editor, "analysisHeight", analysis.getHeight());
        setField(editor, "mouseHandled", true);
        return new Fixture(
            editor,
            model,
            persistence,
            screenCloser,
            failures,
            originalCompact,
            originalAnalysis
        );
    }

    private static void setField(Object target, String name, Object value) {
        try {
            Field field = field(target.getClass(), name);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError(exception);
        }
    }

    private static TemperatureHudRenderer.AnalysisPanel analysisPanel(int width, int height) {
        try {
            Constructor<TemperatureHudRenderer.AnalysisPanel> constructor =
                TemperatureHudRenderer.AnalysisPanel.class.getDeclaredConstructor(
                    String.class,
                    List.class,
                    Integer.TYPE,
                    Integer.TYPE
                );
            constructor.setAccessible(true);
            return constructor.newInstance(null, Collections.emptyList(), width, height);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError(exception);
        }
    }

    private static Object getField(Object target, String name) {
        try {
            Field field = field(target.getClass(), name);
            field.setAccessible(true);
            return field.get(target);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError(exception);
        }
    }

    private static Field field(Class<?> type, String name) throws NoSuchFieldException {
        Class<?> current = type;
        while (current != null) {
            try {
                return current.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            }
        }
        throw new NoSuchFieldException(name);
    }

    enum FailurePoint {
        NONE,
        APPLY,
        SYNC
    }

    private static final class RecordingPersistence
        implements GuiHudEditor.LayoutPersistence {
        private final FailurePoint failurePoint;
        private int applyCalls;
        private int syncCalls;
        private int refreshCalls;
        private Map<HudPanel, HudLayoutState> saved;

        private RecordingPersistence(FailurePoint failurePoint) {
            this.failurePoint = failurePoint;
        }

        @Override
        public void apply(Map<HudPanel, HudLayoutState> states) {
            ++this.applyCalls;
            this.saved = states;
            if (this.failurePoint == FailurePoint.APPLY) {
                throw new IllegalStateException("apply failed");
            }
        }

        @Override
        public void sync() {
            ++this.syncCalls;
            if (this.failurePoint == FailurePoint.SYNC) {
                throw new IllegalStateException("sync failed");
            }
        }

        @Override
        public void refresh() {
            ++this.refreshCalls;
        }
    }

    private static final class RecordingScreenCloser
        implements GuiHudEditor.ScreenCloser {
        private int calls;

        @Override
        public void close(GuiHudEditor editor) {
            ++this.calls;
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

    static final class Fixture {
        final GuiHudEditor editor;
        final HudEditorModel model;
        final RecordingPersistence persistence;
        final RecordingScreenCloser screenCloser;
        final RecordingFailure failures;
        final HudLayoutState originalCompact;
        final HudLayoutState originalAnalysis;

        private Fixture(
            GuiHudEditor editor,
            HudEditorModel model,
            RecordingPersistence persistence,
            RecordingScreenCloser screenCloser,
            RecordingFailure failures,
            HudLayoutState originalCompact,
            HudLayoutState originalAnalysis
        ) {
            this.editor = editor;
            this.model = model;
            this.persistence = persistence;
            this.screenCloser = screenCloser;
            this.failures = failures;
            this.originalCompact = originalCompact;
            this.originalAnalysis = originalAnalysis;
        }

        int saveCalls() {
            return this.persistence.applyCalls;
        }

        void assertCleared() {
            assertNull(getField(this.editor, "model"));
            assertNull(getField(this.editor, "previewSnapshot"));
            assertNull(getField(this.editor, "compactPanel"));
            assertNull(getField(this.editor, "analysisPanel"));
            assertEquals(0, ((Integer)getField(this.editor, "compactWidth")).intValue());
            assertEquals(0, ((Integer)getField(this.editor, "compactHeight")).intValue());
            assertEquals(0, ((Integer)getField(this.editor, "analysisWidth")).intValue());
            assertEquals(0, ((Integer)getField(this.editor, "analysisHeight")).intValue());
            assertFalse(((Boolean)getField(this.editor, "mouseHandled")).booleanValue());
            assertFalse(this.model.isDragging());
            assertNull(this.model.selected());
        }
    }
}
