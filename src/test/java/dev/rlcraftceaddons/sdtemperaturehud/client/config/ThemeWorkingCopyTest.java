package dev.rlcraftceaddons.sdtemperaturehud.client.config;

import dev.rlcraftceaddons.sdtemperaturehud.client.theme.HexColorParser;
import dev.rlcraftceaddons.sdtemperaturehud.client.theme.HudColorPalette;
import dev.rlcraftceaddons.sdtemperaturehud.client.theme.HudThemePreset;
import dev.rlcraftceaddons.sdtemperaturehud.config.HudConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ThemeWorkingCopyTest {
    private ThemeConfigSnapshot original;
    private int originalXOffset;
    private boolean originalShowTrend;

    @Before
    public void rememberConfig() {
        this.original = ThemeConfigSnapshot.capture(HudConfig.theme);
        this.originalXOffset = HudConfig.xOffset;
        this.originalShowTrend = HudConfig.visibility.showTrend;
    }

    @After
    public void restoreConfig() {
        this.original.restore(HudConfig.theme);
        HudConfig.xOffset = this.originalXOffset;
        HudConfig.visibility.showTrend = this.originalShowTrend;
    }

    @Test
    public void everyPresetFillsAllElevenFieldsWithExactNormalizedColors() {
        ThemeWorkingCopy copy = ThemeWorkingCopy.from(HudConfig.theme);

        for (HudThemePreset preset : HudThemePreset.values()) {
            copy.selectPreset(preset);

            assertEquals(preset, copy.getSelectedPreset());
            assertEquals(expected(preset.getPalette()), copy.values());
            assertEquals(11, copy.values().size());
        }
    }

    @Test
    public void nonCustomConfigStartsFromSelectedPresetInsteadOfStaleStoredColors() {
        HudConfig.theme.preset = HudThemePreset.COLOR_BLIND;
        HudConfig.theme.useCustomColors = false;
        HudConfig.theme.severeCold = "#010203";
        HudConfig.theme.text = "#040506";

        ThemeWorkingCopy copy = ThemeWorkingCopy.from(HudConfig.theme);

        assertEquals(HudThemePreset.COLOR_BLIND, copy.getSelectedPreset());
        assertEquals(expected(HudThemePreset.COLOR_BLIND.getPalette()), copy.values());
    }

    @Test
    public void openingExistingCustomThemeReportsCustomState() {
        HudConfig.theme.preset = HudThemePreset.CLASSIC;
        HudConfig.theme.useCustomColors = true;
        HudConfig.theme.border = "#010203";

        ThemeWorkingCopy copy = ThemeWorkingCopy.from(HudConfig.theme);

        assertTrue(copy.isCustom());
    }

    @Test
    public void editingPresetColorBecomesCustomAndExactRevertRestoresPresetState() {
        ThemeWorkingCopy copy = ThemeWorkingCopy.from(HudConfig.theme);
        copy.selectPreset(HudThemePreset.HIGH_CONTRAST);
        assertFalse(copy.isCustom());

        copy.set(ThemeColorField.BORDER, "#010203");
        assertTrue(copy.isCustom());

        copy.set(
            ThemeColorField.BORDER,
            HexColorParser.format(HudThemePreset.HIGH_CONTRAST.getPalette().getBorder())
        );
        assertFalse(copy.isCustom());
    }

    @Test
    public void invalidWorkingColorReportsSafeCustomStateWithoutBreakingPreview() {
        ThemeWorkingCopy copy = ThemeWorkingCopy.from(HudConfig.theme);
        copy.selectPreset(HudThemePreset.CLASSIC);
        copy.set(ThemeColorField.WARM, "invalid");

        assertTrue(copy.isCustom());
        assertEquals(
            HudThemePreset.CLASSIC.getPalette().getWarm(),
            copy.previewPalette().getWarm()
        );
    }

    @Test
    public void oneInvalidFieldRejectsWithoutMutatingAnyConfigValue() {
        ThemeConfigSnapshot before = ThemeConfigSnapshot.capture(HudConfig.theme);
        ThemeWorkingCopy copy = ThemeWorkingCopy.from(HudConfig.theme);
        copy.set(ThemeColorField.WARM, "not-a-color");
        copy.set(ThemeColorField.TEXT, "#010203");

        ThemeApplyResult result = copy.commitTo(HudConfig.theme);

        assertFalse(result.isSuccess());
        assertEquals(ThemeColorField.WARM, result.getInvalidField());
        assertEquals(before, ThemeConfigSnapshot.capture(HudConfig.theme));
    }

    @Test
    public void cancelingAWorkingCopyLeavesFullConfigSnapshotUnchanged() {
        HudConfig.xOffset = 37;
        HudConfig.visibility.showTrend = false;
        Map<String, Object> before = fullConfigSnapshot();
        ThemeWorkingCopy copy = ThemeWorkingCopy.from(HudConfig.theme);
        copy.selectPreset(HudThemePreset.MONOCHROME);
        copy.set(ThemeColorField.BORDER, "#010203");

        copy.cancel();

        assertEquals(before, fullConfigSnapshot());
    }

    @Test
    public void validApplyNormalizesAllValuesAndComputesCustomFlagAtomically() {
        ThemeWorkingCopy presetCopy = ThemeWorkingCopy.from(HudConfig.theme);
        presetCopy.selectPreset(HudThemePreset.HIGH_CONTRAST);
        presetCopy.set(ThemeColorField.TEXT, "ffffff");

        ThemeApplyResult presetResult = presetCopy.commitTo(HudConfig.theme);

        assertTrue(presetResult.isSuccess());
        assertTrue(presetResult.isChanged());
        assertEquals(HudThemePreset.HIGH_CONTRAST, HudConfig.theme.preset);
        assertFalse(HudConfig.theme.useCustomColors);
        assertEquals("#FFFFFF", HudConfig.theme.text);

        ThemeApplyResult unchangedResult = ThemeWorkingCopy.from(HudConfig.theme)
            .commitTo(HudConfig.theme);
        assertTrue(unchangedResult.isSuccess());
        assertFalse(unchangedResult.isChanged());

        ThemeWorkingCopy customCopy = ThemeWorkingCopy.from(HudConfig.theme);
        customCopy.set(ThemeColorField.BORDER, "0a0b0c");

        ThemeApplyResult customResult = customCopy.commitTo(HudConfig.theme);

        assertTrue(customResult.isSuccess());
        assertTrue(customResult.isChanged());
        assertEquals(HudThemePreset.HIGH_CONTRAST, HudConfig.theme.preset);
        assertTrue(HudConfig.theme.useCustomColors);
        assertEquals("#0A0B0C", HudConfig.theme.border);
        assertEquals("#FFFFFF", HudConfig.theme.text);
    }

    private static Map<ThemeColorField, String> expected(HudColorPalette palette) {
        Map<ThemeColorField, String> values = new EnumMap<ThemeColorField, String>(ThemeColorField.class);
        values.put(ThemeColorField.SEVERE_COLD, HexColorParser.format(palette.getSevereCold()));
        values.put(ThemeColorField.COLD, HexColorParser.format(palette.getCold()));
        values.put(ThemeColorField.COMFORTABLE, HexColorParser.format(palette.getComfortable()));
        values.put(ThemeColorField.WARM, HexColorParser.format(palette.getWarm()));
        values.put(ThemeColorField.SEVERE_HOT, HexColorParser.format(palette.getSevereHot()));
        values.put(ThemeColorField.RISING, HexColorParser.format(palette.getRising()));
        values.put(ThemeColorField.FALLING, HexColorParser.format(palette.getFalling()));
        values.put(ThemeColorField.STABLE, HexColorParser.format(palette.getStable()));
        values.put(ThemeColorField.BACKGROUND, HexColorParser.format(palette.getBackground()));
        values.put(ThemeColorField.BORDER, HexColorParser.format(palette.getBorder()));
        values.put(ThemeColorField.TEXT, HexColorParser.format(palette.getText()));
        return values;
    }

    private static Map<String, Object> fullConfigSnapshot() {
        Map<String, Object> values = new LinkedHashMap<String, Object>();
        captureFields("", HudConfig.class, null, values);
        return values;
    }

    private static void captureFields(
        String prefix,
        Class<?> type,
        Object owner,
        Map<String, Object> values
    ) {
        try {
            for (Field field : type.getFields()) {
                if (owner == null && !Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                if (owner != null && Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                Object value = field.get(owner);
                String path = prefix + field.getName();
                if (value != null && value.getClass().getName().startsWith(HudConfig.class.getName() + "$")) {
                    captureFields(path + ".", value.getClass(), value, values);
                } else {
                    values.put(path, value);
                }
            }
        } catch (IllegalAccessException exception) {
            throw new AssertionError(exception);
        }
    }

    private static final class ThemeConfigSnapshot {
        private final HudThemePreset preset;
        private final boolean custom;
        private final String[] values;

        private ThemeConfigSnapshot(HudThemePreset preset, boolean custom, String[] values) {
            this.preset = preset;
            this.custom = custom;
            this.values = values;
        }

        private static ThemeConfigSnapshot capture(HudConfig.Theme theme) {
            return new ThemeConfigSnapshot(
                theme.preset,
                theme.useCustomColors,
                new String[] {
                    theme.severeCold, theme.cold, theme.comfortable, theme.warm,
                    theme.severeHot, theme.rising, theme.falling, theme.stable,
                    theme.background, theme.border, theme.text
                }
            );
        }

        private void restore(HudConfig.Theme theme) {
            theme.preset = this.preset;
            theme.useCustomColors = this.custom;
            theme.severeCold = this.values[0];
            theme.cold = this.values[1];
            theme.comfortable = this.values[2];
            theme.warm = this.values[3];
            theme.severeHot = this.values[4];
            theme.rising = this.values[5];
            theme.falling = this.values[6];
            theme.stable = this.values[7];
            theme.background = this.values[8];
            theme.border = this.values[9];
            theme.text = this.values[10];
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof ThemeConfigSnapshot)) {
                return false;
            }
            ThemeConfigSnapshot that = (ThemeConfigSnapshot) other;
            return this.custom == that.custom
                && this.preset == that.preset
                && Arrays.equals(this.values, that.values);
        }

        @Override
        public int hashCode() {
            int result = this.preset == null ? 0 : this.preset.hashCode();
            result = 31 * result + (this.custom ? 1 : 0);
            result = 31 * result + Arrays.hashCode(this.values);
            return result;
        }
    }
}
