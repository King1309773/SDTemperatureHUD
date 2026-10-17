package dev.rlcraftceaddons.sdtemperaturehud.client.editor;

import dev.rlcraftceaddons.sdtemperaturehud.client.HudSettings;
import dev.rlcraftceaddons.sdtemperaturehud.client.TemperatureHudRenderer;
import dev.rlcraftceaddons.sdtemperaturehud.client.WarningPanelStyle;
import dev.rlcraftceaddons.sdtemperaturehud.temperature.TemperatureBreakdown;
import dev.rlcraftceaddons.sdtemperaturehud.temperature.TemperatureSnapshot;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import net.minecraft.client.gui.ScaledResolution;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class TemperatureHudRendererAccessTest {
    @Test
    public void editorSubpackageCanUseTheSharedRendererApi() throws Exception {
        assertTrue(Modifier.isPublic(TemperatureHudRenderer.CompactPanel.class.getModifiers()));
        assertTrue(Modifier.isPublic(TemperatureHudRenderer.AnalysisPanel.class.getModifiers()));
        assertPublic(TemperatureHudRenderer.class.getMethod(
            "measureCompact",
            TemperatureSnapshot.class,
            HudSettings.class
        ));
        assertPublic(TemperatureHudRenderer.class.getMethod(
            "measureAnalysis",
            TemperatureBreakdown.class,
            HudSettings.class
        ));
        assertPublic(TemperatureHudRenderer.class.getMethod(
            "renderCompact",
            ScaledResolution.class,
            TemperatureHudRenderer.CompactPanel.class,
            HudSettings.class,
            WarningPanelStyle.class
        ));
        assertPublic(TemperatureHudRenderer.class.getMethod(
            "renderAnalysis",
            ScaledResolution.class,
            TemperatureHudRenderer.AnalysisPanel.class,
            HudSettings.class,
            WarningPanelStyle.class
        ));
        assertPublic(TemperatureHudRenderer.CompactPanel.class.getMethod("getWidth"));
        assertPublic(TemperatureHudRenderer.CompactPanel.class.getMethod("getHeight"));
        assertPublic(TemperatureHudRenderer.AnalysisPanel.class.getMethod("getWidth"));
        assertPublic(TemperatureHudRenderer.AnalysisPanel.class.getMethod("getHeight"));
    }

    private static void assertPublic(Method method) {
        assertTrue(method.getName() + " must be public", Modifier.isPublic(method.getModifiers()));
    }

    @SuppressWarnings("unused")
    private static int compileTimeSharedRendererUsage(
        TemperatureHudRenderer renderer,
        TemperatureSnapshot snapshot,
        TemperatureBreakdown breakdown,
        HudSettings settings,
        ScaledResolution resolution,
        WarningPanelStyle style
    ) {
        TemperatureHudRenderer.CompactPanel compact = renderer.measureCompact(snapshot, settings);
        TemperatureHudRenderer.AnalysisPanel analysis = renderer.measureAnalysis(breakdown, settings);
        renderer.renderCompact(resolution, compact, settings, style);
        renderer.renderAnalysis(resolution, analysis, settings, style);
        return compact.getWidth()
            + compact.getHeight()
            + analysis.getWidth()
            + analysis.getHeight();
    }
}
