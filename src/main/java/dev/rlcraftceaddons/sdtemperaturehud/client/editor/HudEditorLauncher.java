package dev.rlcraftceaddons.sdtemperaturehud.client.editor;

import dev.rlcraftceaddons.sdtemperaturehud.client.TemperatureHudRenderer;
import dev.rlcraftceaddons.sdtemperaturehud.temperature.TemperatureSnapshotProvider;
import net.minecraft.client.Minecraft;

public final class HudEditorLauncher {
    private static final PendingRequest PENDING = new PendingRequest();

    private final Minecraft minecraft;
    private final TemperatureSnapshotProvider snapshotProvider;
    private final TemperatureHudRenderer renderer;

    private HudEditorLauncher() {
        this.minecraft = null;
        this.snapshotProvider = null;
        this.renderer = null;
    }

    public HudEditorLauncher(
        Minecraft minecraft,
        TemperatureSnapshotProvider snapshotProvider,
        TemperatureHudRenderer renderer
    ) {
        if (minecraft == null || snapshotProvider == null || renderer == null) {
            throw new IllegalArgumentException("editor launcher dependencies must not be null");
        }
        this.minecraft = minecraft;
        this.snapshotProvider = snapshotProvider;
        this.renderer = renderer;
    }

    public static HudEditorLauncher disabled() {
        return new HudEditorLauncher();
    }

    public static void request() {
        PENDING.request();
    }

    public boolean consumeAndOpen() {
        if (this.minecraft == null) {
            PENDING.consume(false);
            return false;
        }
        Decision decision = PENDING.consume(
            this.minecraft.world != null && this.minecraft.player != null
        );
        if (decision != Decision.OPEN) {
            return false;
        }
        this.minecraft.displayGuiScreen(new GuiHudEditor(this.snapshotProvider, this.renderer));
        return true;
    }

    public void cancelPending() {
        PENDING.cancel();
    }

    enum Decision {
        NONE,
        CANCEL,
        OPEN
    }

    static final class PendingRequest {
        private boolean pending;

        synchronized void request() {
            this.pending = true;
        }

        synchronized Decision consume(boolean clientUsable) {
            if (!this.pending) {
                return Decision.NONE;
            }
            this.pending = false;
            return clientUsable ? Decision.OPEN : Decision.CANCEL;
        }

        synchronized void cancel() {
            this.pending = false;
        }

        synchronized boolean isPending() {
            return this.pending;
        }
    }
}
