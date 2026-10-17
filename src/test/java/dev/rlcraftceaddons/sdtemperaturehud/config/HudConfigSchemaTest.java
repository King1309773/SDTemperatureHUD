package dev.rlcraftceaddons.sdtemperaturehud.config;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import dev.rlcraftceaddons.sdtemperaturehud.client.theme.HudThemePreset;
import net.minecraftforge.common.config.Config;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class HudConfigSchemaTest {
    @Test
    public void preservesLegacyRootKeysDefaultsAndAnnotations() throws Exception {
        assertLegacyInt("xOffset", "XOffset", "sdtemperaturehud.config.x_offset", 0, -500, 500);
        assertLegacyInt("yOffset", "YOffset", "sdtemperaturehud.config.y_offset", 0, -500, 500);
        assertLegacyDouble("scale", "Scale", "sdtemperaturehud.config.scale", 1.0, 0.5, 2.0);
        assertLegacyInt("backgroundAlpha", "BackgroundAlpha", "sdtemperaturehud.config.background_alpha", 214, 0, 255);
        assertLegacyBoolean("warningFlashEnabled", "EnableWarningFlash", "sdtemperaturehud.config.warning_flash", true);
        assertLegacyBoolean("warningSoundEnabled", "EnableWarningSound", "sdtemperaturehud.config.warning_sound", false);
        assertLegacyBoolean("analysisEnabled", "EnableTemperatureAnalysis", "sdtemperaturehud.config.temperature_analysis", true);
    }

    @Test
    public void exposesForgeCompatibleNestedCategoryObjects() throws Exception {
        assertCategory("layout", HudConfig.Layout.class);
        assertCategory("analysis", HudConfig.Analysis.class);
        assertCategory("visibility", HudConfig.Visibility.class);
        assertCategory("theme", HudConfig.Theme.class);

        Field version = HudConfig.Layout.class.getField("version");
        assertTrue(Modifier.isPublic(version.getModifiers()));
        assertFalse(Modifier.isStatic(version.getModifiers()));
        assertEquals("Version", version.getAnnotation(Config.Name.class).value());
        assertEquals(0, version.getInt(HudConfig.layout));
    }

    @Test
    public void declaresExactLayoutRangesAndClassicThemeDefaults() throws Exception {
        assertLayoutCoordinate("compactX", 0.5);
        assertLayoutCoordinate("compactY", 0.5);
        assertLayoutScale("compactScale", 1.0);
        assertLayoutCoordinate("analysisX", 0.5);
        assertLayoutCoordinate("analysisY", 0.5);
        assertLayoutScale("analysisScale", 1.0);

        assertEquals(HudThemePreset.CLASSIC, HudConfig.theme.preset);
        assertFalse(HudConfig.theme.useCustomColors);
        assertEquals("#48C8FF", HudConfig.theme.severeCold);
        assertEquals("#82DDFF", HudConfig.theme.cold);
        assertEquals("#8FF0AD", HudConfig.theme.comfortable);
        assertEquals("#FFB34D", HudConfig.theme.warm);
        assertEquals("#FF5945", HudConfig.theme.severeHot);
        assertEquals("#FFD064", HudConfig.theme.rising);
        assertEquals("#7FDCFF", HudConfig.theme.falling);
        assertEquals("#D5D9DC", HudConfig.theme.stable);
        assertEquals("#0A0D12", HudConfig.theme.background);
        assertEquals("#4D5662", HudConfig.theme.border);
        assertEquals("#C3C7CC", HudConfig.theme.text);
    }

    private static void assertCategory(String fieldName, Class<?> type) throws Exception {
        Field field = HudConfig.class.getField(fieldName);
        assertEquals(type, field.getType());
        assertTrue(Modifier.isPublic(field.getModifiers()));
        assertTrue(Modifier.isStatic(field.getModifiers()));
        assertTrue(Modifier.isFinal(field.getModifiers()));
        assertNotNull(field.get(null));
    }

    private static void assertLegacyInt(
        String fieldName,
        String configName,
        String langKey,
        int defaultValue,
        int minimum,
        int maximum
    ) throws Exception {
        Field field = assertLegacyField(fieldName, configName, langKey);
        assertEquals(defaultValue, field.getInt(null));
        Config.RangeInt range = field.getAnnotation(Config.RangeInt.class);
        assertNotNull(range);
        assertEquals(minimum, range.min());
        assertEquals(maximum, range.max());
    }

    private static void assertLegacyDouble(
        String fieldName,
        String configName,
        String langKey,
        double defaultValue,
        double minimum,
        double maximum
    ) throws Exception {
        Field field = assertLegacyField(fieldName, configName, langKey);
        assertEquals(defaultValue, field.getDouble(null), 0.0);
        Config.RangeDouble range = field.getAnnotation(Config.RangeDouble.class);
        assertNotNull(range);
        assertEquals(minimum, range.min(), 0.0);
        assertEquals(maximum, range.max(), 0.0);
    }

    private static void assertLegacyBoolean(
        String fieldName,
        String configName,
        String langKey,
        boolean defaultValue
    ) throws Exception {
        Field field = assertLegacyField(fieldName, configName, langKey);
        assertEquals(defaultValue, field.getBoolean(null));
    }

    private static Field assertLegacyField(String fieldName, String configName, String langKey) throws Exception {
        Field field = HudConfig.class.getField(fieldName);
        assertTrue(Modifier.isPublic(field.getModifiers()));
        assertTrue(Modifier.isStatic(field.getModifiers()));
        assertFalse(Modifier.isFinal(field.getModifiers()));
        assertEquals(configName, field.getAnnotation(Config.Name.class).value());
        assertEquals(langKey, field.getAnnotation(Config.LangKey.class).value());
        assertNotNull(field.getAnnotation(Config.Comment.class));
        return field;
    }

    private static void assertLayoutCoordinate(String fieldName, double defaultValue) throws Exception {
        Field field = HudConfig.Layout.class.getField(fieldName);
        assertTrue(Modifier.isPublic(field.getModifiers()));
        assertFalse(Modifier.isStatic(field.getModifiers()));
        assertEquals(defaultValue, field.getDouble(HudConfig.layout), 0.0);
        Config.RangeDouble range = field.getAnnotation(Config.RangeDouble.class);
        assertNotNull(range);
        assertTrue(range.min() < 0.0);
        assertTrue(range.max() > 1.0);
    }

    private static void assertLayoutScale(String fieldName, double defaultValue) throws Exception {
        Field field = HudConfig.Layout.class.getField(fieldName);
        assertEquals(defaultValue, field.getDouble(HudConfig.layout), 0.0);
        Config.RangeDouble range = field.getAnnotation(Config.RangeDouble.class);
        assertNotNull(range);
        assertEquals(0.5, range.min(), 0.0);
        assertEquals(2.0, range.max(), 0.0);
        assertNotNull(field.getAnnotation(Config.SlidingOption.class));
    }
}
