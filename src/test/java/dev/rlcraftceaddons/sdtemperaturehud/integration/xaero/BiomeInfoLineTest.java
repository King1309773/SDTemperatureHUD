package dev.rlcraftceaddons.sdtemperaturehud.integration.xaero;
import dev.rlcraftceaddons.sdtemperaturehud.client.theme.HudColorPalette;
import net.minecraft.util.text.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class BiomeInfoLineTest {
    private HudColorPalette palette() {return new HudColorPalette(0x00AAAA,0x5555FF,0x55FF55,0xFFFF55,0xFF5555,0,0,0,0,0,0);}
    @Test public void usesTheExistingFiveTemperatureBands() {
        assertEquals(0x00AAAA,BiomeInfoLine.rgb(5,palette()));
        assertEquals(0x5555FF,BiomeInfoLine.rgb(6,palette()));assertEquals(0x5555FF,BiomeInfoLine.rgb(10,palette()));
        assertEquals(0x55FF55,BiomeInfoLine.rgb(11,palette()));assertEquals(0x55FF55,BiomeInfoLine.rgb(14,palette()));
        assertEquals(0xFFFF55,BiomeInfoLine.rgb(15,palette()));assertEquals(0xFFFF55,BiomeInfoLine.rgb(19,palette()));
        assertEquals(0xFF5555,BiomeInfoLine.rgb(20,palette()));
    }
    @Test public void approximatesThemeColorsWithActualNativeTextColors() {
        assertEquals(TextFormatting.RED,BiomeInfoLine.nativeColor(0xFF5555));
        assertEquals(TextFormatting.DARK_AQUA,BiomeInfoLine.nativeColor(0x01AAAA));
        assertEquals(TextFormatting.WHITE,BiomeInfoLine.nativeColor(0xFFFFFFFF));
    }
    @Test public void colorsOnlyTheNumberAndKeepsTheRegistryIdStable() {
        assertEquals("sdtemperaturehud_biome_temperature",BiomeInfoLine.DISPLAY_ID);
        TextComponentTranslation line=BiomeInfoLine.line(2,palette());
        assertEquals("sdtemperaturehud.map.info.value",line.getKey());assertNull(line.getStyle().getColor());
        ITextComponent number=(ITextComponent)line.getFormatArgs()[0];
        assertEquals("2",number.getUnformattedComponentText());assertEquals(TextFormatting.DARK_AQUA,number.getStyle().getColor());
    }
}
