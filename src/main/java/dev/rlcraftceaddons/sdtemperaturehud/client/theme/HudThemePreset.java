package dev.rlcraftceaddons.sdtemperaturehud.client.theme;

import java.util.Locale;
import net.minecraft.client.resources.I18n;

public enum HudThemePreset {
    // Recovered 1.2.1: border <- ANALYSIS_SEPARATOR_RGB (0x4D5662),
    // text <- ANALYSIS_LABEL_RGB (0xC3C7CC).
    CLASSIC(new HudColorPalette(
        0x48C8FF,
        0x82DDFF,
        0x8FF0AD,
        0xFFB34D,
        0xFF5945,
        0xFFD064,
        0x7FDCFF,
        0xD5D9DC,
        0x0A0D12,
        0x4D5662,
        0xC3C7CC
    )),
    HIGH_CONTRAST(new HudColorPalette(
        0x00B7FF,
        0x7FDBFF,
        0xFFFFFF,
        0xFFD700,
        0xFF4D00,
        0xFFFF00,
        0x00FFFF,
        0xFFFFFF,
        0x000000,
        0xFFFFFF,
        0xFFFFFF
    )),
    COLOR_BLIND(new HudColorPalette(
        0x0072B2,
        0x56B4E9,
        0xF0E442,
        0xE69F00,
        0xD55E00,
        0xE69F00,
        0x56B4E9,
        0xCC79A7,
        0x181818,
        0xB3B3B3,
        0xFFFFFF
    )),
    MONOCHROME(new HudColorPalette(
        0x202020,
        0x606060,
        0xB0B0B0,
        0xD0D0D0,
        0xF4F4F4,
        0xDADADA,
        0x707070,
        0xA0A0A0,
        0x101010,
        0xB8B8B8,
        0xFFFFFF
    ));

    private final HudColorPalette palette;

    HudThemePreset(HudColorPalette palette) {
        this.palette = palette;
    }

    public HudColorPalette getPalette() {
        return this.palette;
    }

    public String getLanguageKey() {
        return "sdtemperaturehud.config.theme.preset."
            + this.name().toLowerCase(Locale.ROOT);
    }

    @Override
    public String toString() {
        return I18n.format(this.getLanguageKey());
    }
}
