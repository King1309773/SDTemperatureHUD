package dev.rlcraftceaddons.sdtemperaturehud.client;

import dev.rlcraftceaddons.sdtemperaturehud.temperature.TemperatureBreakdown;
import dev.rlcraftceaddons.sdtemperaturehud.temperature.TemperatureSnapshot;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import net.minecraft.client.gui.ScaledResolution;
import org.junit.Test;

import static org.junit.Assert.assertFalse;

public class TemperatureHudRendererApiTest {
    @Test
    public void exposesSharedMeasurementAndRenderingPathsToTheClientPackage() throws Exception {
        assertPackageVisibleMethod(TemperatureHudRenderer.class.getDeclaredMethod(
            "measureCompact",
            TemperatureSnapshot.class,
            HudSettings.class
        ));
        assertPackageVisibleMethod(TemperatureHudRenderer.class.getDeclaredMethod(
            "measureAnalysis",
            TemperatureBreakdown.class,
            HudSettings.class
        ));
        assertPackageVisibleMethod(TemperatureHudRenderer.class.getDeclaredMethod(
            "renderCompact",
            ScaledResolution.class,
            TemperatureHudRenderer.CompactPanel.class,
            HudSettings.class,
            WarningPanelStyle.class
        ));
        assertPackageVisibleMethod(TemperatureHudRenderer.class.getDeclaredMethod(
            "renderAnalysis",
            ScaledResolution.class,
            TemperatureHudRenderer.AnalysisPanel.class,
            HudSettings.class,
            WarningPanelStyle.class
        ));
    }

    private static void assertPackageVisibleMethod(Method method) {
        assertFalse(method.getName() + " must not be private", Modifier.isPrivate(method.getModifiers()));
    }
}
