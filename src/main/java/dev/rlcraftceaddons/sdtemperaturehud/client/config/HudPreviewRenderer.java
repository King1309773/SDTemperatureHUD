package dev.rlcraftceaddons.sdtemperaturehud.client.config;

import dev.rlcraftceaddons.sdtemperaturehud.SDTemperatureHud;
import dev.rlcraftceaddons.sdtemperaturehud.client.HudSettings;
import dev.rlcraftceaddons.sdtemperaturehud.client.TemperatureHudRenderer;
import dev.rlcraftceaddons.sdtemperaturehud.client.WarningPanelStyle;
import dev.rlcraftceaddons.sdtemperaturehud.client.editor.HudLayoutDefaults;
import dev.rlcraftceaddons.sdtemperaturehud.client.layout.HudLayoutState;
import dev.rlcraftceaddons.sdtemperaturehud.config.LegacyLayoutMigration;
import dev.rlcraftceaddons.sdtemperaturehud.temperature.TemperatureBreakdown;
import dev.rlcraftceaddons.sdtemperaturehud.temperature.TemperatureSnapshot;
import dev.rlcraftceaddons.sdtemperaturehud.temperature.TemperatureWarningLevel;
import java.nio.IntBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.resources.I18n;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

/** The same measurements, colors and draw calls used by the in-world HUD. */
final class HudPreviewRenderer {
    private final Minecraft mc;
    private final TemperatureHudRenderer renderer;
    private final IntBuffer scissorBox = BufferUtils.createIntBuffer(16);

    HudPreviewRenderer(Minecraft mc) {
        this.mc = mc;
        renderer = new TemperatureHudRenderer(mc);
    }

    void initializePositions(SettingsWorkingCopy draft, int width, int height) {
        HudSettings settings = HudSettings.fromConfig();
        if (!settings.isLegacyLayoutPending()) return;
        TemperatureSnapshot snapshot = SDTemperatureHud.currentSnapshot();
        if (snapshot == null || !snapshot.isAvailable() || !snapshot.hasBreakdown()) snapshot = sample(1);
        TemperatureHudRenderer.CompactPanel compact = renderer.measureCompact(snapshot, settings);
        TemperatureHudRenderer.AnalysisPanel analysis = renderer.measureAnalysis(snapshot, settings);
        if (analysis == null) analysis = renderer.measureAnalysis(snapshot, HudSettings.of(0, 0, 1, 214));
        LegacyLayoutMigration.PanelLayouts positions = LegacyLayoutMigration.forPanels(
            settings.getXOffset(), settings.getYOffset(), settings.getScale(),
            settings.getCompactLayout().getScale(), settings.getAnalysisLayout().getScale(),
            width, height, compact.getWidth(), compact.getHeight(), analysis.getWidth(), analysis.getHeight());
        draft.initializeLegacyPositions(positions.compact(), positions.analysis());
    }

    HudLayoutDefaults defaults(int width, int height) {
        HudSettings settings = HudSettings.of(0, 0, 1, 214);
        TemperatureSnapshot snapshot = sample(1);
        TemperatureHudRenderer.CompactPanel compact = renderer.measureCompact(snapshot, settings);
        TemperatureHudRenderer.AnalysisPanel analysis = renderer.measureAnalysis(snapshot, settings);
        return HudLayoutDefaults.forScreen(width, height, compact.getWidth(), compact.getHeight(),
            analysis.getWidth(), analysis.getHeight());
    }

    void drawPreview(SettingsScreenLayout box, int width, int height,
        SettingsWorkingCopy draft, int scene) {
        int left = box.previewLeft, top = 96, right = left + box.previewWidth, bottom = height - 41;
        for (int y = top; y < bottom; y += 8)
            for (int x = left; x < right; x += 8)
                Gui.drawRect(x, y, Math.min(x + 8, right), Math.min(y + 8, bottom),
                    (((x - left) / 8 + (y - top) / 8) & 1) == 0 ? 0xFF455047 : 0xFF344239);
        AppearanceWorkingCopy appearance = draft.appearance();
        HudSettings settings = appearance.preview(HudSettings.fromConfig());
        TemperatureSnapshot snapshot = sample(scene);
        TemperatureHudRenderer.CompactPanel compact = renderer.measureCompact(snapshot, settings);
        TemperatureHudRenderer.AnalysisPanel analysis = renderer.measureAnalysis(snapshot, settings);
        double compactScale = appearance.getCompactScale(), analysisScale = appearance.getAnalysisScale();
        double analysisHeight = analysis == null ? 0 : analysis.getHeight() * analysisScale;
        double totalHeight = compact.getHeight() * compactScale + analysisHeight + 6;
        double y = top + Math.max(4, (bottom - top - totalHeight) / 2);
        double compactX = left + Math.max(3, (box.previewWidth - compact.getWidth() * compactScale) / 2);
        double analysisX = analysis == null ? left + 3 :
            left + Math.max(3, (box.previewWidth - analysis.getWidth() * analysisScale) / 2);
        HudSettings positioned = settings.withLayouts(
            new HudLayoutState(compactX / width, (y + analysisHeight + 6) / height, compactScale),
            new HudLayoutState(analysisX / width, y / height, analysisScale));
        ScaledResolution resolution = new ScaledResolution(mc);
        boolean scissor = GL11.glIsEnabled(GL11.GL_SCISSOR_TEST);
        scissorBox.clear();
        GL11.glGetInteger(GL11.GL_SCISSOR_BOX, scissorBox);
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        int factor = resolution.getScaleFactor();
        GL11.glScissor(left * factor, mc.displayHeight - bottom * factor,
            box.previewWidth * factor, Math.max(1, bottom - top) * factor);
        try {
            WarningPanelStyle style = WarningPanelStyle.forLevel(TemperatureWarningLevel.NORMAL, 0, false);
            renderer.renderAnalysis(resolution, analysis, positioned, style);
            renderer.renderCompact(resolution, compact, positioned, style);
        } finally {
            GL11.glScissor(scissorBox.get(0), scissorBox.get(1), scissorBox.get(2), scissorBox.get(3));
            if (!scissor) GL11.glDisable(GL11.GL_SCISSOR_TEST);
        }
        boolean clipped = totalHeight + 8 > bottom - top
            || compact.getWidth() * compactScale + 6 > box.previewWidth
            || analysis != null && analysis.getWidth() * analysisScale + 6 > box.previewWidth;
        String caption = I18n.format("sdtemperaturehud.preview." + (clipped ? "clipped" : "sample"));
        mc.fontRenderer.drawString(mc.fontRenderer.trimStringToWidth(caption, box.previewWidth),
            left, bottom + 2, 0xBBBBBB);
    }

    private static TemperatureSnapshot sample(int scene) {
        int body = scene == 0 ? 4 : scene == 1 ? 12 : 22;
        int environment = scene == 0 ? 8 : scene == 1 ? 14 : 19;
        int season = scene == 0 ? -3 : scene == 1 ? -2 : 2;
        int heat = scene == 2 ? 5 : 2;
        return TemperatureSnapshot.withAnalysis(body, new TemperatureBreakdown(environment,
            -1, season, heat, 1, Math.min(25, environment + season + heat)));
    }
}

