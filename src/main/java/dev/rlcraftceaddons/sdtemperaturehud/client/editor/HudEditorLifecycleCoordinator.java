package dev.rlcraftceaddons.sdtemperaturehud.client.editor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;

public final class HudEditorLifecycleCoordinator {
    private final ScreenProvider screens;
    private final PendingCancellation pendingCancellation;
    private final ClientExecution clientExecution;

    public HudEditorLifecycleCoordinator(
        final Minecraft minecraft,
        final HudEditorLauncher launcher
    ) {
        if (launcher == null) {
            throw new IllegalArgumentException("launcher must not be null");
        }
        this.screens = new ScreenProvider() {
            @Override
            public GuiScreen currentScreen() {
                return minecraft == null ? null : minecraft.currentScreen;
            }
        };
        this.pendingCancellation = new PendingCancellation() {
            @Override
            public void cancelPending() {
                launcher.cancelPending();
            }
        };
        this.clientExecution = new ClientExecution() {
            @Override
            public boolean isClientThread() {
                return minecraft == null || minecraft.isCallingFromMinecraftThread();
            }

            @Override
            public void schedule(Runnable operation) {
                if (minecraft == null) {
                    operation.run();
                } else {
                    minecraft.addScheduledTask(operation);
                }
            }
        };
    }

    HudEditorLifecycleCoordinator(
        ScreenProvider screens,
        PendingCancellation pendingCancellation
    ) {
        this(screens, pendingCancellation, new ClientExecution() {
            @Override
            public boolean isClientThread() {
                return true;
            }

            @Override
            public void schedule(Runnable operation) {
                operation.run();
            }
        });
    }

    HudEditorLifecycleCoordinator(
        ScreenProvider screens,
        PendingCancellation pendingCancellation,
        ClientExecution clientExecution
    ) {
        if (screens == null || pendingCancellation == null || clientExecution == null) {
            throw new IllegalArgumentException("lifecycle dependencies must not be null");
        }
        this.screens = screens;
        this.pendingCancellation = pendingCancellation;
        this.clientExecution = clientExecution;
    }

    public void onWorldInvalidated() {
        this.closeActiveEditor();
        this.pendingCancellation.cancelPending();
    }

    public void onDisconnected(final Runnable clientCleanup) {
        if (clientCleanup == null) {
            throw new IllegalArgumentException("client cleanup must not be null");
        }
        Runnable disconnect = new Runnable() {
            @Override
            public void run() {
                try {
                    clientCleanup.run();
                } finally {
                    HudEditorLifecycleCoordinator.this.onWorldInvalidated();
                }
            }
        };
        if (this.clientExecution.isClientThread()) {
            disconnect.run();
            return;
        }
        this.clientExecution.schedule(disconnect);
    }

    public void onScreenReplacement(GuiScreen replacement) {
        GuiScreen current = this.screens.currentScreen();
        boolean replacingEditor = current instanceof GuiHudEditor && replacement != current;
        if (replacingEditor) {
            ((GuiHudEditor)current).closeForLifecycle();
            this.pendingCancellation.cancelPending();
        } else if (replacement instanceof GuiMainMenu) {
            this.onMainMenu();
        }
    }

    public void onMainMenu() {
        this.pendingCancellation.cancelPending();
    }

    private void closeActiveEditor() {
        GuiScreen current = this.screens.currentScreen();
        if (current instanceof GuiHudEditor) {
            ((GuiHudEditor)current).closeForLifecycle();
        }
    }

    interface ScreenProvider {
        GuiScreen currentScreen();
    }

    interface PendingCancellation {
        void cancelPending();
    }

    interface ClientExecution {
        boolean isClientThread();

        void schedule(Runnable operation);
    }
}
