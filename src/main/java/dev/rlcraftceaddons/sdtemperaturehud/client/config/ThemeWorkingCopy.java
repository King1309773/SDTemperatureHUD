package dev.rlcraftceaddons.sdtemperaturehud.client.config;

import dev.rlcraftceaddons.sdtemperaturehud.client.theme.HexColorParser;
import dev.rlcraftceaddons.sdtemperaturehud.client.theme.HudColorPalette;
import dev.rlcraftceaddons.sdtemperaturehud.client.theme.HudThemePreset;
import dev.rlcraftceaddons.sdtemperaturehud.config.HudConfig;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public final class ThemeWorkingCopy {
    private final EnumMap<ThemeColorField, String> values =
        new EnumMap<ThemeColorField, String>(ThemeColorField.class);
    private HudThemePreset selectedPreset;

    private ThemeWorkingCopy(HudThemePreset selectedPreset) {
        this.selectedPreset = selectedPreset;
    }

    public static ThemeWorkingCopy from(HudConfig.Theme theme) {
        HudThemePreset preset = theme.preset == null ? HudThemePreset.CLASSIC : theme.preset;
        ThemeWorkingCopy copy = new ThemeWorkingCopy(preset);
        if (!theme.useCustomColors) {
            copy.fillFromPalette(preset.getPalette());
            return copy;
        }
        copy.values.put(ThemeColorField.SEVERE_COLD, theme.severeCold);
        copy.values.put(ThemeColorField.COLD, theme.cold);
        copy.values.put(ThemeColorField.COMFORTABLE, theme.comfortable);
        copy.values.put(ThemeColorField.WARM, theme.warm);
        copy.values.put(ThemeColorField.SEVERE_HOT, theme.severeHot);
        copy.values.put(ThemeColorField.RISING, theme.rising);
        copy.values.put(ThemeColorField.FALLING, theme.falling);
        copy.values.put(ThemeColorField.STABLE, theme.stable);
        copy.values.put(ThemeColorField.BACKGROUND, theme.background);
        copy.values.put(ThemeColorField.BORDER, theme.border);
        copy.values.put(ThemeColorField.TEXT, theme.text);
        return copy;
    }

    public HudThemePreset getSelectedPreset() {
        return this.selectedPreset;
    }

    public Map<ThemeColorField, String> values() {
        return Collections.unmodifiableMap(
            new EnumMap<ThemeColorField, String>(this.values)
        );
    }

    public String get(ThemeColorField field) {
        return this.values.get(field);
    }

    public void set(ThemeColorField field, String value) {
        this.values.put(field, value);
    }

    public boolean isCustom() {
        ParsedTheme parsed = this.parseAll();
        return parsed.invalidField != null
            || !parsed.palette.equals(this.selectedPreset.getPalette());
    }

    public void selectNextPreset() {
        HudThemePreset[] presets = HudThemePreset.values();
        this.selectPreset(presets[(this.selectedPreset.ordinal() + 1) % presets.length]);
    }

    public void selectPreset(HudThemePreset preset) {
        if (preset == null) {
            throw new IllegalArgumentException("preset must not be null");
        }
        this.selectedPreset = preset;
        this.fillFromPalette(preset.getPalette());
    }

    public ThemeApplyResult commitTo(HudConfig.Theme target) {
        ParsedTheme parsed = this.parseAll();
        if (parsed.invalidField != null) {
            return ThemeApplyResult.invalid(parsed.invalidField);
        }

        boolean custom = !parsed.palette.equals(this.selectedPreset.getPalette());
        boolean changed = target.preset != this.selectedPreset
            || target.useCustomColors != custom
            || !Objects.equals(target.severeCold, parsed.normalized[0])
            || !Objects.equals(target.cold, parsed.normalized[1])
            || !Objects.equals(target.comfortable, parsed.normalized[2])
            || !Objects.equals(target.warm, parsed.normalized[3])
            || !Objects.equals(target.severeHot, parsed.normalized[4])
            || !Objects.equals(target.rising, parsed.normalized[5])
            || !Objects.equals(target.falling, parsed.normalized[6])
            || !Objects.equals(target.stable, parsed.normalized[7])
            || !Objects.equals(target.background, parsed.normalized[8])
            || !Objects.equals(target.border, parsed.normalized[9])
            || !Objects.equals(target.text, parsed.normalized[10]);

        target.preset = this.selectedPreset;
        target.useCustomColors = custom;
        target.severeCold = parsed.normalized[0];
        target.cold = parsed.normalized[1];
        target.comfortable = parsed.normalized[2];
        target.warm = parsed.normalized[3];
        target.severeHot = parsed.normalized[4];
        target.rising = parsed.normalized[5];
        target.falling = parsed.normalized[6];
        target.stable = parsed.normalized[7];
        target.background = parsed.normalized[8];
        target.border = parsed.normalized[9];
        target.text = parsed.normalized[10];
        return ThemeApplyResult.success(changed);
    }

    public HudColorPalette previewPalette() {
        int[] colors = colors(this.selectedPreset.getPalette());
        ThemeColorField[] fields = ThemeColorField.values();
        for (int index = 0; index < fields.length; index++) {
            try {
                colors[index] = HexColorParser.parse(this.values.get(fields[index]));
            } catch (IllegalArgumentException ignored) {
                // Keep the selected preset's value while the user repairs this field.
            }
        }
        return palette(colors);
    }

    public void cancel() {
        // All edits live in this working copy until commitTo is called.
    }

    private void fillFromPalette(HudColorPalette palette) {
        int[] colors = colors(palette);
        ThemeColorField[] fields = ThemeColorField.values();
        for (int index = 0; index < fields.length; index++) {
            this.values.put(fields[index], HexColorParser.format(colors[index]));
        }
    }

    private ParsedTheme parseAll() {
        ThemeColorField[] fields = ThemeColorField.values();
        int[] colors = new int[fields.length];
        String[] normalized = new String[fields.length];
        for (int index = 0; index < fields.length; index++) {
            try {
                colors[index] = HexColorParser.parse(this.values.get(fields[index]));
                normalized[index] = HexColorParser.format(colors[index]);
            } catch (IllegalArgumentException exception) {
                return ParsedTheme.invalid(fields[index]);
            }
        }
        return ParsedTheme.valid(palette(colors), normalized);
    }

    private static int[] colors(HudColorPalette palette) {
        return new int[] {
            palette.getSevereCold(),
            palette.getCold(),
            palette.getComfortable(),
            palette.getWarm(),
            palette.getSevereHot(),
            palette.getRising(),
            palette.getFalling(),
            palette.getStable(),
            palette.getBackground(),
            palette.getBorder(),
            palette.getText()
        };
    }

    private static HudColorPalette palette(int[] colors) {
        return new HudColorPalette(
            colors[0], colors[1], colors[2], colors[3], colors[4], colors[5],
            colors[6], colors[7], colors[8], colors[9], colors[10]
        );
    }

    private static final class ParsedTheme {
        private final ThemeColorField invalidField;
        private final HudColorPalette palette;
        private final String[] normalized;

        private ParsedTheme(
            ThemeColorField invalidField,
            HudColorPalette palette,
            String[] normalized
        ) {
            this.invalidField = invalidField;
            this.palette = palette;
            this.normalized = normalized;
        }

        private static ParsedTheme invalid(ThemeColorField field) {
            return new ParsedTheme(field, null, null);
        }

        private static ParsedTheme valid(HudColorPalette palette, String[] normalized) {
            return new ParsedTheme(null, palette, normalized);
        }
    }
}
