package dev.rlcraftceaddons.sdtemperaturehud.client.config;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.rlcraftceaddons.sdtemperaturehud.config.HudConfig;
import net.minecraftforge.common.config.Config;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ConfigGuiResourcesTest {
    @Test
    public void metadataRegistersExactGuiFactoryWithoutChangingIdentity() throws IOException {
        String json = readResource("mcmod.info");
        JsonObject metadata = new JsonParser().parse(json).getAsJsonArray().get(0).getAsJsonObject();

        assertEquals(
            "dev.rlcraftceaddons.sdtemperaturehud.client.config.SDTemperatureHudGuiFactory",
            metadata.get("guiFactory").getAsString()
        );
        assertEquals("KingJJ", metadata.getAsJsonArray("authorList").get(0).getAsString());
        assertTrue(metadata.getAsJsonArray("dependencies").toString().contains("simpledifficulty"));
    }

    @Test
    public void everyAnnotatedConfigEntryHasChineseNameAndTooltip() throws Exception {
        Properties language = language();
        assertAnnotatedKeys(HudConfig.class, language);
        assertAnnotatedKeys(HudConfig.Layout.class, language);
        assertAnnotatedKeys(HudConfig.Analysis.class, language);
        assertAnnotatedKeys(HudConfig.Visibility.class, language);
        assertAnnotatedKeys(HudConfig.Theme.class, language);
    }

    @Test
    public void configActionsEditorHintsEnumsAndThemeControlsAreLocalized() throws IOException {
        Properties language = language();
        Set<String> required = new HashSet<String>(Arrays.asList(
            "sdtemperaturehud.config.title",
            "sdtemperaturehud.config.action.open_editor",
            "sdtemperaturehud.config.action.open_theme",
            "sdtemperaturehud.config.action.reset_compact",
            "sdtemperaturehud.config.action.reset_analysis",
            "sdtemperaturehud.config.action.reset_full_appearance",
            "sdtemperaturehud.config.reset.confirm.title",
            "sdtemperaturehud.config.reset.confirm.compact",
            "sdtemperaturehud.config.reset.confirm.analysis",
            "sdtemperaturehud.config.reset.confirm.full_appearance",
            "sdtemperaturehud.config.editor.unavailable",
            "sdtemperaturehud.editor.hint",
            "sdtemperaturehud.editor.reset",
            "sdtemperaturehud.editor.save_close",
            "sdtemperaturehud.editor.empty_analysis",
            "sdtemperaturehud.config.analysis_mode.hold",
            "sdtemperaturehud.config.analysis_mode.toggle",
            "sdtemperaturehud.config.analysis_mode.always",
            "sdtemperaturehud.config.theme.preset.classic",
            "sdtemperaturehud.config.theme.preset.high_contrast",
            "sdtemperaturehud.config.theme.preset.color_blind",
            "sdtemperaturehud.config.theme.preset.monochrome",
            "sdtemperaturehud.theme.title",
            "sdtemperaturehud.theme.preset",
            "sdtemperaturehud.theme.custom",
            "sdtemperaturehud.theme.preview",
            "sdtemperaturehud.theme.done",
            "sdtemperaturehud.theme.cancel",
            "sdtemperaturehud.theme.invalid_color"
        ));
        for (ThemeColorField field : ThemeColorField.values()) {
            required.add(field.getLanguageKey());
        }
        for (String key : required) {
            assertNotNull("missing localization key " + key, language.getProperty(key));
        }
        assertEquals("\u6253\u5f00 HUD \u7f16\u8f91\u5668", language.getProperty("sdtemperaturehud.config.action.open_editor"));
        assertEquals("\u989c\u8272\u4e3b\u9898\u4e0e\u9ad8\u7ea7\u989c\u8272", language.getProperty("sdtemperaturehud.config.action.open_theme"));
        assertEquals("\u6062\u590d\u7d27\u51d1 HUD", language.getProperty("sdtemperaturehud.config.action.reset_compact"));
        assertEquals("\u6062\u590d\u5206\u6790\u9762\u677f", language.getProperty("sdtemperaturehud.config.action.reset_analysis"));
        assertEquals("\u6062\u590d\u5168\u90e8\u5916\u89c2\u4e0e\u5e03\u5c40", language.getProperty("sdtemperaturehud.config.action.reset_full_appearance"));
        assertEquals("\u8bf7\u8fdb\u5165\u4e16\u754c\u540e\u4f7f\u7528", language.getProperty("sdtemperaturehud.config.editor.unavailable"));
        assertEquals("\u81ea\u5b9a\u4e49", language.getProperty("sdtemperaturehud.theme.custom"));
    }

    private static void assertAnnotatedKeys(Class<?> type, Properties language) throws Exception {
        for (Field field : type.getFields()) {
            Config.LangKey annotation = field.getAnnotation(Config.LangKey.class);
            if (annotation == null) {
                continue;
            }
            String key = annotation.value();
            assertNotNull("missing display name " + key, language.getProperty(key));
            assertNotNull("missing tooltip " + key, language.getProperty(key + ".tooltip"));
        }
    }

    private static Properties language() throws IOException {
        Properties properties = new Properties();
        try (InputStream input = ConfigGuiResourcesTest.class.getClassLoader()
            .getResourceAsStream("assets/sdtemperaturehud/lang/zh_cn.lang")) {
            assertNotNull(input);
            properties.load(new java.io.InputStreamReader(input, StandardCharsets.UTF_8));
        }
        return properties;
    }

    private static String readResource(String name) throws IOException {
        InputStream input = ConfigGuiResourcesTest.class.getClassLoader().getResourceAsStream(name);
        assertNotNull(input);
        try (InputStream stream = input; ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int read;
            while ((read = stream.read(buffer)) >= 0) {
                output.write(buffer, 0, read);
            }
            return new String(output.toByteArray(), StandardCharsets.UTF_8);
        }
    }
}
