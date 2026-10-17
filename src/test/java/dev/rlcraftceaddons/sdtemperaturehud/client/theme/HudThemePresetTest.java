package dev.rlcraftceaddons.sdtemperaturehud.client.theme;

import java.util.HashSet;
import java.util.Set;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotSame;

public class HudThemePresetTest {
    @Test
    public void exposesExactlyFourBuiltInPresets() {
        assertArrayEquals(
            new HudThemePreset[] {
                HudThemePreset.CLASSIC,
                HudThemePreset.HIGH_CONTRAST,
                HudThemePreset.COLOR_BLIND,
                HudThemePreset.MONOCHROME
            },
            HudThemePreset.values()
        );
    }

    @Test
    public void everyPresetOwnsADistinctPalette() {
        Set<HudColorPalette> palettes = new HashSet<HudColorPalette>();
        for (HudThemePreset preset : HudThemePreset.values()) {
            palettes.add(preset.getPalette());
        }

        assertEquals(4, palettes.size());
        assertNotSame(HudThemePreset.CLASSIC.getPalette(), HudThemePreset.HIGH_CONTRAST.getPalette());
    }

    @Test
    public void classicPalettePreservesRecoveredRendererColors() {
        HudColorPalette palette = HudThemePreset.CLASSIC.getPalette();

        assertEquals(0x48C8FF, palette.getSevereCold());
        assertEquals(0x82DDFF, palette.getCold());
        assertEquals(0x8FF0AD, palette.getComfortable());
        assertEquals(0xFFB34D, palette.getWarm());
        assertEquals(0xFF5945, palette.getSevereHot());
        assertEquals(0xFFD064, palette.getRising());
        assertEquals(0x7FDCFF, palette.getFalling());
        assertEquals(0xD5D9DC, palette.getStable());
        assertEquals(0x0A0D12, palette.getBackground());
        assertEquals(0x4D5662, palette.getBorder());
        assertEquals(0xC3C7CC, palette.getText());
    }

    @Test
    public void colorBlindPaletteUsesBlueSkyYellowOrangeAndVermilionCues() {
        HudColorPalette palette = HudThemePreset.COLOR_BLIND.getPalette();

        assertEquals(0x0072B2, palette.getSevereCold());
        assertEquals(0x56B4E9, palette.getCold());
        assertEquals(0xF0E442, palette.getComfortable());
        assertEquals(0xE69F00, palette.getWarm());
        assertEquals(0xD55E00, palette.getSevereHot());
        assertEquals(0xE69F00, palette.getRising());
        assertEquals(0x56B4E9, palette.getFalling());
    }

    @Test
    public void monochromePaletteContainsOnlyGrayscaleRgbValues() {
        HudColorPalette palette = HudThemePreset.MONOCHROME.getPalette();

        assertGrayscale(palette.getSevereCold());
        assertGrayscale(palette.getCold());
        assertGrayscale(palette.getComfortable());
        assertGrayscale(palette.getWarm());
        assertGrayscale(palette.getSevereHot());
        assertGrayscale(palette.getRising());
        assertGrayscale(palette.getFalling());
        assertGrayscale(palette.getStable());
        assertGrayscale(palette.getBackground());
        assertGrayscale(palette.getBorder());
        assertGrayscale(palette.getText());
    }

    @Test
    public void paletteMasksAlphaAndHasImmutableValueSemantics() {
        HudColorPalette palette = new HudColorPalette(
            0xAA010203,
            0xBB111213,
            0xCC212223,
            0xDD313233,
            0xEE414243,
            0xFF515253,
            0x80616263,
            0x70717273,
            0x60818283,
            0x50919293,
            0x40A1A2A3
        );
        HudColorPalette equalPalette = new HudColorPalette(
            0x010203,
            0x111213,
            0x212223,
            0x313233,
            0x414243,
            0x515253,
            0x616263,
            0x717273,
            0x818283,
            0x919293,
            0xA1A2A3
        );

        assertEquals(0x010203, palette.getSevereCold());
        assertEquals(0x111213, palette.getCold());
        assertEquals(0x212223, palette.getComfortable());
        assertEquals(0x313233, palette.getWarm());
        assertEquals(0x414243, palette.getSevereHot());
        assertEquals(0x515253, palette.getRising());
        assertEquals(0x616263, palette.getFalling());
        assertEquals(0x717273, palette.getStable());
        assertEquals(0x818283, palette.getBackground());
        assertEquals(0x919293, palette.getBorder());
        assertEquals(0xA1A2A3, palette.getText());
        assertEquals(palette, equalPalette);
        assertEquals(palette.hashCode(), equalPalette.hashCode());
        assertEquals(palette.toString(), equalPalette.toString());
        assertNotEquals(palette, HudThemePreset.CLASSIC.getPalette());
    }

    private static void assertGrayscale(int color) {
        int red = color >> 16 & 0xFF;
        int green = color >> 8 & 0xFF;
        int blue = color & 0xFF;
        assertEquals(red, green);
        assertEquals(green, blue);
    }
}
