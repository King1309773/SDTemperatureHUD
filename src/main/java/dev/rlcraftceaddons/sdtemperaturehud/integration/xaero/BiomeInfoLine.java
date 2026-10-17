package dev.rlcraftceaddons.sdtemperaturehud.integration.xaero;

import dev.rlcraftceaddons.sdtemperaturehud.client.theme.HudColorPalette;
import net.minecraft.util.text.*;

/** Only standard component colors are available to Xaero's native information renderer. */
public final class BiomeInfoLine {
    public static final String DISPLAY_ID="sdtemperaturehud_biome_temperature";
    private static final int[] COLORS={0x000000,0x0000AA,0x00AA00,0x00AAAA,0xAA0000,0xAA00AA,0xFFAA00,0xAAAAAA,
        0x555555,0x5555FF,0x55FF55,0x55FFFF,0xFF5555,0xFF55FF,0xFFFF55,0xFFFFFF};
    private BiomeInfoLine() {}
    public static int rgb(int value,HudColorPalette p) {
        return value<=5?p.getSevereCold():value<=10?p.getCold():value<=14?p.getComfortable():value<=19?p.getWarm():p.getSevereHot();
    }
    public static TextFormatting nativeColor(int rgb) {
        int best=0,distance=Integer.MAX_VALUE;
        for(int i=0;i<COLORS.length;i++) {
            int dr=((rgb>>16)&255)-((COLORS[i]>>16)&255),dg=((rgb>>8)&255)-((COLORS[i]>>8)&255),db=(rgb&255)-(COLORS[i]&255);
            int candidate=dr*dr+dg*dg+db*db;
            if(candidate<distance){distance=candidate;best=i;}
        }
        return TextFormatting.fromColorIndex(best);
    }
    public static ITextComponent number(int value,HudColorPalette palette) {
        return new TextComponentString(Integer.toString(value)).setStyle(new Style().setColor(nativeColor(rgb(value,palette))));
    }
    public static TextComponentTranslation line(int value,HudColorPalette palette) {
        return new TextComponentTranslation("sdtemperaturehud.map.info.value",number(value,palette));
    }
}
