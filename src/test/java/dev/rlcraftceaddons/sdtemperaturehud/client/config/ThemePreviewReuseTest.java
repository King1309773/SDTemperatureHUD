package dev.rlcraftceaddons.sdtemperaturehud.client.config;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ThemePreviewReuseTest {
    @Test
    public void appearancePreviewUsesProductionMeasureAndRenderPaths() throws IOException {
        Path sourcePath = Paths.get(System.getProperty("user.dir"))
            .resolve("src/main/java/dev/rlcraftceaddons/sdtemperaturehud/client/config/HudPreviewRenderer.java");
        String source = new String(Files.readAllBytes(sourcePath), StandardCharsets.UTF_8);
        String drawPreview = source.substring(
            source.indexOf("void drawPreview("),
            source.indexOf("private static TemperatureSnapshot sample(")
        );

        assertTrue(drawPreview.contains("measureCompact("));
        assertTrue(drawPreview.contains("renderCompact("));
        assertTrue(drawPreview.contains("measureAnalysis("));
        assertTrue(drawPreview.contains("renderAnalysis("));
        assertFalse("preview must not space tokens independently", drawPreview.contains("TemperaturePresentation"));
    }
}
