package dev.rlcraftceaddons.sdtemperaturehud.client.editor;

import dev.rlcraftceaddons.sdtemperaturehud.SDTemperatureHud;
import dev.rlcraftceaddons.sdtemperaturehud.client.HudSettings;
import dev.rlcraftceaddons.sdtemperaturehud.client.TemperatureHudRenderer;
import dev.rlcraftceaddons.sdtemperaturehud.client.WarningPanelStyle;
import dev.rlcraftceaddons.sdtemperaturehud.client.layout.HudBounds;
import dev.rlcraftceaddons.sdtemperaturehud.client.layout.HudLayoutState;
import dev.rlcraftceaddons.sdtemperaturehud.client.layout.HudPanel;
import dev.rlcraftceaddons.sdtemperaturehud.config.HudConfig;
import dev.rlcraftceaddons.sdtemperaturehud.config.LegacyLayoutMigration;
import dev.rlcraftceaddons.sdtemperaturehud.temperature.TemperatureBreakdown;
import dev.rlcraftceaddons.sdtemperaturehud.temperature.TemperatureSnapshot;
import dev.rlcraftceaddons.sdtemperaturehud.temperature.TemperatureSnapshotProvider;
import dev.rlcraftceaddons.sdtemperaturehud.temperature.TemperatureWarning;
import dev.rlcraftceaddons.sdtemperaturehud.temperature.TemperatureWarningLevel;
import java.io.IOException;
import java.util.Map;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public final class GuiHudEditor extends GuiScreen {
    private static final int OVERLAY_COLOR = 0x88000000;
    private static final int SELECTED_COLOR = 0xFFFFFFFF;
    private static final int UNSELECTED_COLOR = 0x77999999;
    private static final int PLACEHOLDER_BACKGROUND = 0xCC20242A;
    private static final int PLACEHOLDER_TEXT = 0xFFE0E0E0;
    private static final int PLACEHOLDER_HEIGHT = 16;
    private static final int PLACEHOLDER_MINIMUM_WIDTH = 96;

    private final TemperatureSnapshotProvider snapshotProvider;
    private final TemperatureHudRenderer renderer;
    private final HudSettings baseSettings;
    private final LayoutPersistence layoutPersistence;
    private final ScreenCloser screenCloser;
    private final HudEditorCloseController.FailureHandler failureHandler;
    private final HudEditorCloseController closeController = new HudEditorCloseController();

    private HudEditorModel model;
    private TemperatureSnapshot previewSnapshot;
    private TemperatureHudRenderer.CompactPanel compactPanel;
    private TemperatureHudRenderer.AnalysisPanel analysisPanel;
    private int compactWidth;
    private int compactHeight;
    private int analysisWidth;
    private int analysisHeight;

    public GuiHudEditor(
        TemperatureSnapshotProvider snapshotProvider,
        TemperatureHudRenderer renderer
    ) {
        this(
            snapshotProvider,
            renderer,
            HudSettings.current(),
            new ConfigLayoutPersistence(),
            new MinecraftScreenCloser(),
            new LoggingFailureHandler()
        );
    }

    GuiHudEditor(
        TemperatureSnapshotProvider snapshotProvider,
        TemperatureHudRenderer renderer,
        HudSettings baseSettings,
        LayoutPersistence layoutPersistence,
        ScreenCloser screenCloser,
        HudEditorCloseController.FailureHandler failureHandler
    ) {
        if (snapshotProvider == null || renderer == null || baseSettings == null) {
            throw new IllegalArgumentException("editor dependencies must not be null");
        }
        if (layoutPersistence == null || screenCloser == null || failureHandler == null) {
            throw new IllegalArgumentException("editor lifecycle dependencies must not be null");
        }
        this.snapshotProvider = snapshotProvider;
        this.renderer = renderer;
        this.baseSettings = baseSettings;
        this.layoutPersistence = layoutPersistence;
        this.screenCloser = screenCloser;
        this.failureHandler = failureHandler;
    }

    @Override
    public void initGui() {
        this.refreshPanels();
        if (this.model == null) {
            HudLayoutDefaults defaults = HudLayoutDefaults.forScreen(
                this.width,
                this.height,
                this.compactWidth,
                this.compactHeight,
                this.analysisWidth,
                this.analysisHeight
            );
            HudLayoutState compactDefault = defaults.compact();
            HudLayoutState analysisDefault = defaults.analysis();
            HudLayoutState compact = this.baseSettings.getCompactLayout();
            HudLayoutState analysis = this.baseSettings.getAnalysisLayout();
            if (this.baseSettings.isLegacyLayoutPending()) {
                LegacyLayoutMigration.PanelLayouts legacy = this.legacyLayouts();
                compact = legacy.compact();
                analysis = legacy.analysis();
            }
            this.model = new HudEditorModel(
                compact,
                analysis,
                compactDefault,
                analysisDefault
            );
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        Gui.drawRect(0, 0, this.width, this.height, OVERLAY_COLOR);
        this.refreshPanels();
        if (this.model == null) {
            this.initGui();
        }

        ScaledResolution resolution = new ScaledResolution(this.mc);
        HudSettings previewSettings = this.baseSettings.withLayouts(
            this.model.state(HudPanel.COMPACT),
            this.model.state(HudPanel.ANALYSIS)
        );
        TemperatureWarningLevel warningLevel = this.previewSnapshot.isAvailable()
            ? TemperatureWarning.classifyBodyTemperature(
                this.previewSnapshot.getBodyTemperature()
            )
            : TemperatureWarningLevel.NORMAL;
        WarningPanelStyle style = WarningPanelStyle.forLevel(
            warningLevel,
            System.currentTimeMillis(),
            previewSettings.isWarningFlashEnabled()
        );

        this.renderer.renderAnalysis(resolution, this.analysisPanel, previewSettings, style);
        if (this.analysisPanel == null) {
            this.drawEmptyAnalysisPreview();
        }
        this.renderer.renderCompact(resolution, this.compactPanel, previewSettings, style);

        this.drawPanelOutline(HudPanel.ANALYSIS, this.analysisWidth, this.analysisHeight);
        this.drawPanelOutline(HudPanel.COMPACT, this.compactWidth, this.compactHeight);
        drawCenteredString(
            this.fontRenderer,
            controlHint(),
            this.width / 2,
            this.height - this.fontRenderer.FONT_HEIGHT - 6,
            0xFFE5E5E5
        );
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (mouseButton == 0 && this.model != null) {
            HudPanel hit = this.model.selectAt(
                mouseX,
                mouseY,
                this.width,
                this.height,
                this.compactWidth,
                this.compactHeight,
                this.analysisWidth,
                this.analysisHeight
            );
            if (hit != null) {
                this.model.beginDrag(
                    mouseX,
                    mouseY,
                    this.width,
                    this.height,
                    this.panelWidth(hit),
                    this.panelHeight(hit)
                );
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseClickMove(
        int mouseX,
        int mouseY,
        int clickedMouseButton,
        long timeSinceLastClick
    ) {
        if (clickedMouseButton == 0 && this.model != null) {
            HudPanel selected = this.model.selected();
            this.model.dragTo(
                mouseX,
                mouseY,
                this.width,
                this.height,
                this.panelWidth(selected),
                this.panelHeight(selected)
            );
        }
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        if (this.model != null) {
            this.model.endDrag();
        }
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheelDelta = Mouse.getEventDWheel();
        if (wheelDelta != 0 && this.model != null) {
            this.mouseHandled = true;
            HudPanel selected = this.model.selected();
            this.model.wheel(
                wheelDelta,
                this.width,
                this.height,
                this.panelWidth(selected),
                this.panelHeight(selected)
            );
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_R && this.model != null) {
            this.model.resetSelected(
                this.width,
                this.height,
                this.compactWidth,
                this.compactHeight,
                this.analysisWidth,
                this.analysisHeight
            );
            return;
        }
        if (keyCode == Keyboard.KEY_ESCAPE) {
            this.closeForLifecycle();
            this.screenCloser.close(this);
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return this.mc != null && this.mc.isSingleplayer();
    }

    @Override
    public void onGuiClosed() {
        this.closeForLifecycle();
        super.onGuiClosed();
    }

    void closeForLifecycle() {
        try {
            this.closeController.close(
                this.model,
                new HudEditorCloseController.SaveOperation() {
                    @Override
                    public void save(HudEditorModel closingModel) {
                        GuiHudEditor.this.saveValidatedStates(closingModel);
                    }
                },
                this.failureHandler
            );
        } finally {
            this.clearTransientReferences();
        }
    }

    private void saveValidatedStates(HudEditorModel closingModel) {
        Map<HudPanel, HudLayoutState> states = closingModel.validatedStates(
            this.width,
            this.height,
            this.compactWidth,
            this.compactHeight,
            this.analysisWidth,
            this.analysisHeight
        );
        try {
            this.layoutPersistence.apply(states);
            this.layoutPersistence.sync();
        } finally {
            this.layoutPersistence.refresh();
        }
    }

    private void clearTransientReferences() {
        this.model = null;
        this.previewSnapshot = null;
        this.compactPanel = null;
        this.analysisPanel = null;
        this.compactWidth = 0;
        this.compactHeight = 0;
        this.analysisWidth = 0;
        this.analysisHeight = 0;
        this.mouseHandled = false;
    }

    private void refreshPanels() {
        this.previewSnapshot = this.createPreviewSnapshot();
        this.compactPanel = this.renderer.measureCompact(this.previewSnapshot, this.baseSettings);
        this.analysisPanel = this.renderer.measureAnalysis(this.previewSnapshot, this.baseSettings);
        this.compactWidth = this.compactPanel.getWidth();
        this.compactHeight = this.compactPanel.getHeight();
        if (this.analysisPanel == null) {
            this.analysisWidth = Math.max(
                PLACEHOLDER_MINIMUM_WIDTH,
                this.fontRenderer.getStringWidth(emptyAnalysisText()) + 8
            );
            this.analysisHeight = PLACEHOLDER_HEIGHT;
        } else {
            this.analysisWidth = this.analysisPanel.getWidth();
            this.analysisHeight = this.analysisPanel.getHeight();
        }
    }

    private TemperatureSnapshot createPreviewSnapshot() {
        TemperatureSnapshot live = this.snapshotProvider.getSnapshot();
        if (live != null && !live.isAvailable()) {
            return live;
        }
        if (live != null && live.hasBreakdown()) {
            return live;
        }
        int bodyTemperature = live == null ? 0 : live.getBodyTemperature();
        int targetTemperature = live != null && live.hasTargetTemperature()
            ? live.getTargetTemperature()
            : bodyTemperature;
        return TemperatureSnapshot.withAnalysis(
            bodyTemperature,
            new TemperatureBreakdown(bodyTemperature, 0, 0, 0, 0, targetTemperature)
        );
    }

    private LegacyLayoutMigration.PanelLayouts legacyLayouts() {
        return LegacyLayoutMigration.forPanels(
            this.baseSettings.getXOffset(),
            this.baseSettings.getYOffset(),
            this.baseSettings.getScale(),
            this.baseSettings.getCompactLayout().getScale(),
            this.baseSettings.getAnalysisLayout().getScale(),
            this.width,
            this.height,
            this.compactWidth,
            this.compactHeight,
            this.analysisWidth,
            this.analysisHeight
        );
    }

    private void drawEmptyAnalysisPreview() {
        HudBounds bounds = this.model.bounds(
            HudPanel.ANALYSIS,
            this.width,
            this.height,
            this.analysisWidth,
            this.analysisHeight
        );
        double scale = this.model.state(HudPanel.ANALYSIS).getScale();
        GlStateManager.pushMatrix();
        try {
            GlStateManager.translate(bounds.getX(), bounds.getY(), 0.0);
            GlStateManager.scale(scale, scale, 1.0);
            Gui.drawRect(0, 0, this.analysisWidth, this.analysisHeight, PLACEHOLDER_BACKGROUND);
            this.fontRenderer.drawStringWithShadow(
                emptyAnalysisText(),
                4.0f,
                4.0f,
                PLACEHOLDER_TEXT
            );
        } finally {
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            GlStateManager.popMatrix();
        }
    }

    private void drawPanelOutline(HudPanel panel, int panelWidth, int panelHeight) {
        HudBounds bounds = this.model.bounds(
            panel,
            this.width,
            this.height,
            panelWidth,
            panelHeight
        );
        int color = this.model.selected() == panel ? SELECTED_COLOR : UNSELECTED_COLOR;
        int left = bounds.getX() - 1;
        int top = bounds.getY() - 1;
        int right = bounds.getX() + bounds.getWidth() + 1;
        int bottom = bounds.getY() + bounds.getHeight() + 1;
        Gui.drawRect(left, top, right, top + 1, color);
        Gui.drawRect(left, bottom - 1, right, bottom, color);
        Gui.drawRect(left, top, left + 1, bottom, color);
        Gui.drawRect(right - 1, top, right, bottom, color);
    }

    private int panelWidth(HudPanel panel) {
        return panel == HudPanel.ANALYSIS ? this.analysisWidth : this.compactWidth;
    }

    private int panelHeight(HudPanel panel) {
        return panel == HudPanel.ANALYSIS ? this.analysisHeight : this.compactHeight;
    }

    private static String emptyAnalysisText() {
        return I18n.format("sdtemperaturehud.editor.empty_analysis");
    }

    private static String controlHint() {
        return I18n.format(
            "sdtemperaturehud.editor.hint",
            I18n.format("sdtemperaturehud.editor.reset"),
            I18n.format("sdtemperaturehud.editor.save_close")
        );
    }

    interface LayoutPersistence {
        void apply(Map<HudPanel, HudLayoutState> states);

        void sync();

        void refresh();
    }

    interface ScreenCloser {
        void close(GuiHudEditor editor);
    }

    private static final class ConfigLayoutPersistence implements LayoutPersistence {
        @Override
        public void apply(Map<HudPanel, HudLayoutState> states) {
            HudLayoutState compact = states.get(HudPanel.COMPACT);
            HudLayoutState analysis = states.get(HudPanel.ANALYSIS);
            HudConfig.layout.compactX = compact.getNormalizedX();
            HudConfig.layout.compactY = compact.getNormalizedY();
            HudConfig.layout.compactScale = compact.getScale();
            HudConfig.layout.analysisX = analysis.getNormalizedX();
            HudConfig.layout.analysisY = analysis.getNormalizedY();
            HudConfig.layout.analysisScale = analysis.getScale();
            HudConfig.layout.version = Math.max(1, HudConfig.layout.version);
        }

        @Override
        public void sync() {
            ConfigManager.sync(SDTemperatureHud.MODID, Config.Type.INSTANCE);
        }

        @Override
        public void refresh() {
            HudSettings.refreshFromConfig();
        }
    }

    private static final class MinecraftScreenCloser implements ScreenCloser {
        @Override
        public void close(GuiHudEditor editor) {
            editor.mc.displayGuiScreen(null);
        }
    }

    private static final class LoggingFailureHandler
        implements HudEditorCloseController.FailureHandler {
        @Override
        public void onFailure(RuntimeException exception) {
            SDTemperatureHud.LOGGER.error(
                "Failed to persist HUD editor layout; keeping edited session values in memory",
                exception
            );
        }
    }

}
