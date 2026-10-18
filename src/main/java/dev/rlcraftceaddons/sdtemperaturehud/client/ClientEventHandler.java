/*
 * Decompiled with CFR 0.152.
 */
package dev.rlcraftceaddons.sdtemperaturehud.client;

import com.charles445.simpledifficulty.api.config.ClientConfig;
import com.charles445.simpledifficulty.api.config.ClientOptions;
import com.charles445.simpledifficulty.api.config.IConfigOption;
import com.charles445.simpledifficulty.api.config.QuickConfig;
import dev.rlcraftceaddons.sdtemperaturehud.client.HudSettings;
import dev.rlcraftceaddons.sdtemperaturehud.client.HudVisibility;
import dev.rlcraftceaddons.sdtemperaturehud.client.TemperatureAnalysisKey;
import dev.rlcraftceaddons.sdtemperaturehud.client.TemperatureHudRenderer;
import dev.rlcraftceaddons.sdtemperaturehud.client.WarningSoundController;
import dev.rlcraftceaddons.sdtemperaturehud.client.editor.GuiHudEditor;
import dev.rlcraftceaddons.sdtemperaturehud.client.editor.HudEditorLauncher;
import dev.rlcraftceaddons.sdtemperaturehud.client.editor.HudEditorLifecycleCoordinator;
import dev.rlcraftceaddons.sdtemperaturehud.temperature.TemperatureSnapshot;
import dev.rlcraftceaddons.sdtemperaturehud.temperature.TemperatureSnapshotProvider;
import dev.rlcraftceaddons.sdtemperaturehud.temperature.TemperatureWarning;
import dev.rlcraftceaddons.sdtemperaturehud.temperature.TemperatureWarningLevel;
import dev.rlcraftceaddons.sdtemperaturehud.temperature.TemperatureChangeTracker;
import dev.rlcraftceaddons.sdtemperaturehud.config.HudConfig;
import dev.rlcraftceaddons.sdtemperaturehud.client.config.GuiHudConfig;
import dev.rlcraftceaddons.sdtemperaturehud.client.config.GuiResetSettings;
import dev.rlcraftceaddons.sdtemperaturehud.client.config.GuiMapIntegration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiGameOver;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

public final class ClientEventHandler {
    private final Minecraft minecraft;
    private final TemperatureSnapshotProvider snapshotProvider;
    private final TemperatureHudRenderer renderer;
    private final WarningSoundController warningSoundController;
    private final KeyBinding analysisKey;
    private final TemperatureAnalysisKey analysisKeyState;
    private final HudEditorLauncher editorLauncher;
    private final HudEditorLifecycleCoordinator editorLifecycle;
    private World lastWorld;
    private final TemperatureChangeTracker changeTracker = new TemperatureChangeTracker();
    private long changeClock;

    public ClientEventHandler(Minecraft minecraft, TemperatureSnapshotProvider snapshotProvider, TemperatureHudRenderer renderer) {
        this(minecraft, snapshotProvider, renderer, new WarningSoundController());
    }

    public ClientEventHandler(Minecraft minecraft, TemperatureSnapshotProvider snapshotProvider, TemperatureHudRenderer renderer, WarningSoundController warningSoundController) {
        this(minecraft, snapshotProvider, renderer, warningSoundController, TemperatureAnalysisKey.create());
    }

    public ClientEventHandler(Minecraft minecraft, TemperatureSnapshotProvider snapshotProvider, TemperatureHudRenderer renderer, WarningSoundController warningSoundController, KeyBinding analysisKey) {
        this(
            minecraft,
            snapshotProvider,
            renderer,
            warningSoundController,
            analysisKey,
            editorLauncher(minecraft, snapshotProvider, renderer)
        );
    }

    public ClientEventHandler(
        Minecraft minecraft,
        TemperatureSnapshotProvider snapshotProvider,
        TemperatureHudRenderer renderer,
        WarningSoundController warningSoundController,
        KeyBinding analysisKey,
        HudEditorLauncher editorLauncher
    ) {
        this.minecraft = minecraft;
        this.snapshotProvider = snapshotProvider;
        this.renderer = renderer;
        this.warningSoundController = warningSoundController;
        this.analysisKey = analysisKey;
        this.analysisKeyState = new TemperatureAnalysisKey();
        this.editorLauncher = editorLauncher;
        this.editorLifecycle = new HudEditorLifecycleCoordinator(minecraft, editorLauncher);
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        boolean active;
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        WorldClient currentWorld = this.minecraft.world;
        if (currentWorld != this.lastWorld) {
            this.lastWorld = currentWorld;
            this.snapshotProvider.invalidate();
            this.warningSoundController.reset();
            this.changeTracker.reset();
            resetAnalysisState(this.analysisKeyState);
        }
        boolean bl = active = currentWorld != null && this.minecraft.player != null;
        if (!active) {
            this.snapshotProvider.tick(false);
            this.warningSoundController.reset();
            this.changeTracker.reset();
        } else if (!this.minecraft.isGamePaused()) {
            this.snapshotProvider.tick(true);
            TemperatureSnapshot snapshot = this.snapshotProvider.getSnapshot();
            this.changeClock += 50;
            if (HudConfig.changeHintsEnabled && this.minecraft.currentScreen == null
                && !this.minecraft.player.isDead && QuickConfig.isTemperatureEnabled()) {
                this.changeTracker.update(snapshot, this.changeClock,
                    Math.max(1, HudConfig.changeHintThreshold),
                    Math.max(1, Math.min(10, HudConfig.changeHintDurationSeconds)) * 1000);
            } else {
                this.changeTracker.suspend();
            }
            if (snapshot != null) {
                TemperatureWarningLevel level = warningLevelForSound(snapshot);
                if (level != null) {
                    this.playAlert(this.warningSoundController.update(
                        level,
                        HudSettings.current().isWarningSoundEnabled()
                    ));
                }
            }
        }
        if (this.minecraft.currentScreen != null) this.changeTracker.suspend();
        this.editorLauncher.consumeAndOpen();
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL || !this.shouldRender()) {
            return;
        }
        HudSettings settings = HudSettings.current();
        boolean analysisVisible = this.analysisKeyState.isVisible(settings.getAnalysisDisplayMode(), this.analysisKey.isKeyDown());
        TemperatureSnapshot snapshot = this.snapshotProvider.getSnapshot();
        if (snapshot != null) {
            this.renderer.render(event.getResolution(), snapshot, settings, settings.isAnalysisEnabled() && analysisVisible, System.currentTimeMillis());
            if (HudConfig.changeHintsEnabled && this.minecraft.currentScreen == null) {
                this.renderer.renderChangeHints(event.getResolution(), snapshot, settings,
                    settings.isAnalysisEnabled() && analysisVisible,
                    this.changeTracker.visible(this.changeClock));
            }
        }
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        if (event.getWorld().isRemote) {
            this.changeTracker.reset();
            resetAnalysisState(this.analysisKeyState);
            this.editorLifecycle.onWorldInvalidated();
        }
    }

    @SubscribeEvent
    public void onClientDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        this.editorLifecycle.onDisconnected(new Runnable() {
            @Override
            public void run() {
                ClientEventHandler.this.changeTracker.reset();
                resetAnalysisState(ClientEventHandler.this.analysisKeyState);
            }
        });
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public void onGuiOpen(GuiOpenEvent event) {
        this.editorLifecycle.onScreenReplacement(event.getGui());
        if (event.getGui() instanceof GuiMainMenu) {
            resetAnalysisState(this.analysisKeyState);
        }
    }

    static void resetAnalysisState(TemperatureAnalysisKey analysisKeyState) {
        analysisKeyState.reset();
    }

    static TemperatureWarningLevel warningLevelForSound(TemperatureSnapshot snapshot) {
        if (snapshot == null) {
            throw new IllegalArgumentException("snapshot must not be null");
        }
        return snapshot.isAvailable()
            ? TemperatureWarning.classifyBodyTemperature(snapshot.getBodyTemperature())
            : null;
    }

    private static HudEditorLauncher editorLauncher(
        Minecraft minecraft,
        TemperatureSnapshotProvider snapshotProvider,
        TemperatureHudRenderer renderer
    ) {
        if (minecraft == null || snapshotProvider == null || renderer == null) {
            return HudEditorLauncher.disabled();
        }
        return new HudEditorLauncher(minecraft, snapshotProvider, renderer);
    }

    private void playAlert(WarningSoundController.Alert alert) {
        float pitch;
        if (alert == WarningSoundController.Alert.COLD) {
            pitch = 0.65f;
        } else if (alert == WarningSoundController.Alert.HOT) {
            pitch = 1.25f;
        } else {
            return;
        }
        this.minecraft.getSoundHandler().playSound((ISound)PositionedSoundRecord.getMasterRecord((SoundEvent)SoundEvents.BLOCK_NOTE_PLING, (float)pitch));
    }

    private boolean shouldRender() {
        boolean hasPlayer = this.minecraft.player != null;
        boolean hasWorld = this.minecraft.world != null;
        boolean hasPlayerController = this.minecraft.playerController != null;
        boolean survivalOrAdventure = hasPlayerController && this.minecraft.playerController.gameIsSurvivalOrAdventure();
        boolean alternateTemperatureEnabled = ClientConfig.instance != null && ClientConfig.instance.getBoolean((IConfigOption)ClientOptions.ALTERNATE_TEMP);
        return HudVisibility.shouldRender(hasPlayer, hasWorld, hasPlayerController, this.minecraft.gameSettings.hideGUI, survivalOrAdventure, QuickConfig.isTemperatureEnabled(), alternateTemperatureEnabled, this.minecraft.currentScreen instanceof GuiGameOver, this.minecraft.currentScreen instanceof GuiHudEditor || this.minecraft.currentScreen instanceof GuiHudConfig || this.minecraft.currentScreen instanceof GuiResetSettings || this.minecraft.currentScreen instanceof GuiMapIntegration);
    }
}
