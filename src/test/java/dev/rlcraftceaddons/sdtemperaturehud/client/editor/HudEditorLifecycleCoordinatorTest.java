package dev.rlcraftceaddons.sdtemperaturehud.client.editor;

import net.minecraft.client.gui.GuiScreen;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HudEditorLifecycleCoordinatorTest {
    @Test
    public void worldUnloadAndShutdownCloseActiveEditorOnceAndCancelPending() {
        GuiHudEditorLifecycleTest.Fixture fixture =
            GuiHudEditorLifecycleTest.fixture(GuiHudEditorLifecycleTest.FailurePoint.NONE);
        MutableScreen screens = new MutableScreen(fixture.editor);
        RecordingCancellation cancellation = new RecordingCancellation();
        HudEditorLifecycleCoordinator coordinator =
            new HudEditorLifecycleCoordinator(screens, cancellation);

        coordinator.onWorldInvalidated();
        coordinator.onWorldInvalidated();

        assertEquals(1, fixture.saveCalls());
        assertEquals(2, cancellation.calls);
        fixture.assertCleared();
    }

    @Test
    public void networkThreadDisconnectQueuesCloseAndCancellationOnTheClientThread() {
        GuiHudEditorLifecycleTest.Fixture fixture =
            GuiHudEditorLifecycleTest.fixture(GuiHudEditorLifecycleTest.FailurePoint.NONE);
        MutableScreen screens = new MutableScreen(fixture.editor);
        RecordingCancellation cancellation = new RecordingCancellation();
        QueuedExecution execution = new QueuedExecution(false);
        HudEditorLifecycleCoordinator coordinator =
            new HudEditorLifecycleCoordinator(screens, cancellation, execution);
        final int[] clientCleanupCalls = {0};

        coordinator.onDisconnected(new Runnable() {
            @Override
            public void run() {
                ++clientCleanupCalls[0];
            }
        });

        assertEquals(0, fixture.saveCalls());
        assertEquals(0, cancellation.calls);
        assertEquals(0, clientCleanupCalls[0]);
        assertEquals(1, execution.scheduledCalls);
        execution.runScheduled();
        assertEquals(1, fixture.saveCalls());
        assertEquals(1, cancellation.calls);
        assertEquals(1, clientCleanupCalls[0]);
        fixture.assertCleared();
    }

    @Test
    public void deathOrReplacementClosesEditorBeforeReplacementAndCancelsPending() {
        GuiHudEditorLifecycleTest.Fixture fixture =
            GuiHudEditorLifecycleTest.fixture(GuiHudEditorLifecycleTest.FailurePoint.NONE);
        MutableScreen screens = new MutableScreen(fixture.editor);
        RecordingCancellation cancellation = new RecordingCancellation();
        HudEditorLifecycleCoordinator coordinator =
            new HudEditorLifecycleCoordinator(screens, cancellation);

        coordinator.onScreenReplacement(new GuiScreen() { });
        coordinator.onScreenReplacement(new GuiScreen() { });

        assertEquals(1, fixture.saveCalls());
        assertEquals(2, cancellation.calls);
        fixture.assertCleared();
    }

    @Test
    public void replacementWithTheSameEditorDoesNotCloseOrCancelIt() {
        GuiHudEditorLifecycleTest.Fixture fixture =
            GuiHudEditorLifecycleTest.fixture(GuiHudEditorLifecycleTest.FailurePoint.NONE);
        MutableScreen screens = new MutableScreen(fixture.editor);
        RecordingCancellation cancellation = new RecordingCancellation();
        HudEditorLifecycleCoordinator coordinator =
            new HudEditorLifecycleCoordinator(screens, cancellation);

        coordinator.onScreenReplacement(fixture.editor);

        assertEquals(0, fixture.saveCalls());
        assertEquals(0, cancellation.calls);
    }

    @Test
    public void unrelatedScreensAreUntouchedWhileMainMenuStillCancelsPending() {
        GuiScreen unrelated = new GuiScreen() { };
        MutableScreen screens = new MutableScreen(unrelated);
        RecordingCancellation cancellation = new RecordingCancellation();
        HudEditorLifecycleCoordinator coordinator =
            new HudEditorLifecycleCoordinator(screens, cancellation);

        coordinator.onScreenReplacement(new GuiScreen() { });
        assertEquals(0, cancellation.calls);
        coordinator.onMainMenu();
        assertEquals(1, cancellation.calls);
    }

    private static final class MutableScreen
        implements HudEditorLifecycleCoordinator.ScreenProvider {
        private GuiScreen current;

        private MutableScreen(GuiScreen current) {
            this.current = current;
        }

        @Override
        public GuiScreen currentScreen() {
            return this.current;
        }
    }

    private static final class RecordingCancellation
        implements HudEditorLifecycleCoordinator.PendingCancellation {
        private int calls;

        @Override
        public void cancelPending() {
            ++this.calls;
        }
    }

    private static final class QueuedExecution
        implements HudEditorLifecycleCoordinator.ClientExecution {
        private final boolean clientThread;
        private int scheduledCalls;
        private Runnable scheduled;

        private QueuedExecution(boolean clientThread) {
            this.clientThread = clientThread;
        }

        @Override
        public boolean isClientThread() {
            return this.clientThread;
        }

        @Override
        public void schedule(Runnable operation) {
            ++this.scheduledCalls;
            this.scheduled = operation;
        }

        private void runScheduled() {
            Runnable operation = this.scheduled;
            this.scheduled = null;
            operation.run();
        }
    }
}
