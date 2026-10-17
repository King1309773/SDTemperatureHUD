package dev.rlcraftceaddons.sdtemperaturehud.client.map;

import dev.rlcraftceaddons.sdtemperaturehud.client.HudSettings;
import dev.rlcraftceaddons.sdtemperaturehud.client.theme.HudColorPalette;
import dev.rlcraftceaddons.sdtemperaturehud.integration.SimpleDifficultyBiomeRules;
import dev.rlcraftceaddons.sdtemperaturehud.map.*;
import java.nio.FloatBuffer;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

public final class MapLabelPainter {
    private final Minecraft mc=Minecraft.getMinecraft();
    private final SimpleDifficultyBiomeRules rules=new SimpleDifficultyBiomeRules();
    private final FrameStateCache<GlState> frame=new FrameStateCache<GlState>();
    private final FloatBuffer colorBuffer=BufferUtils.createFloatBuffer(16);
    public void beginFrame(){frame.beginFrame();}
    public MapLabel measure(BiomeRegion region,double x,double y,float scale,boolean priority) {
        ResourceLocation id=new ResourceLocation(region.registryId);
        if(!ForgeRegistries.BIOMES.containsKey(id))return null;
        Biome biome=ForgeRegistries.BIOMES.getValue(id);
        java.util.OptionalInt value=rules.temperatureFor(biome);if(!value.isPresent())return null;
        String name=biome.getBiomeName();String shortName=shortName(name);
        int width=(int)Math.ceil(mc.fontRenderer.getStringWidth(shortName+" "+value.getAsInt())*scale)+4;
        return new MapLabel(region.registryId,name,value.getAsInt(),region.x,region.z,x,y,width,
            (int)Math.ceil(mc.fontRenderer.FONT_HEIGHT*scale)+4,priority);
    }
    private String shortName(String name) {
        return mc.fontRenderer.getStringWidth(name)<=72?name:mc.fontRenderer.trimStringToWidth(name,64)+"…";
    }
    public void paint(MapLabel label,double partialX,double partialY,float scale) {
        GlState state=frame.get(this::capture);
        GlStateManager.pushMatrix();
        try {
            GlStateManager.disableDepth();GlStateManager.enableAlpha();
            GlStateManager.translate(partialX,partialY,0);
            HudSettings settings=HudSettings.current();HudColorPalette palette=settings.getColorPalette();
            int left=-label.width/2,top=-label.height/2;
            Gui.drawRect(left,top,left+label.width,top+label.height,
                (settings.getBackgroundAlpha()<<24)|palette.getBackground());
            GlStateManager.scale(scale,scale,1);
            String name=shortName(label.name)+" ";
            float x=(left+2)/scale,y=(top+2)/scale;
            mc.fontRenderer.drawString(name,x,y,0xff000000|palette.getText(),true);
            mc.fontRenderer.drawString(Integer.toString(label.temperature),x+mc.fontRenderer.getStringWidth(name),y,
                0xff000000|temperatureColor(label.temperature,palette),true);
        } finally {
            GlStateManager.popMatrix();
            restore(state);
        }
    }
    /** GUI-sized suffix; matches the native heading background without repainting its name. */
    public void paintHeading(int temperature,int x,int y) {
        GlState state=capture();GlStateManager.pushMatrix();
        try {
            GlStateManager.disableDepth();GlStateManager.enableAlpha();GlStateManager.enableTexture2D();
            String number=Integer.toString(temperature);int width=mc.fontRenderer.getStringWidth(number);
            GlStateManager.translate(0,0,-1);
            Gui.drawRect(x-1,y-1,x+width+1,y+9,0x66000000);
            GlStateManager.translate(0,0,1);
            mc.fontRenderer.drawString(number,x,y,
                0xff000000|temperatureColor(temperature,HudSettings.current().getColorPalette()),true);
        }finally {GlStateManager.popMatrix();restore(state);}
    }
    private void restore(GlState state) {
        if(state.blend)GlStateManager.enableBlend();else GlStateManager.disableBlend();
        if(state.depth)GlStateManager.enableDepth();else GlStateManager.disableDepth();
        if(state.texture)GlStateManager.enableTexture2D();else GlStateManager.disableTexture2D();
        if(state.alpha)GlStateManager.enableAlpha();else GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(state.srcRGB,state.dstRGB,state.srcAlpha,state.dstAlpha);
        GlStateManager.bindTexture(state.binding);GlStateManager.color(state.r,state.g,state.b,state.a);
    }
    private GlState capture() {
        colorBuffer.clear();GL11.glGetFloat(GL11.GL_CURRENT_COLOR,colorBuffer);
        return new GlState(colorBuffer);
    }
    private static final class GlState {
        final boolean blend=GL11.glIsEnabled(GL11.GL_BLEND),depth=GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
        final boolean texture=GL11.glIsEnabled(GL11.GL_TEXTURE_2D),alpha=GL11.glIsEnabled(GL11.GL_ALPHA_TEST);
        final int srcRGB=GL11.glGetInteger(0x80C9),dstRGB=GL11.glGetInteger(0x80C8);
        final int srcAlpha=GL11.glGetInteger(0x80CB),dstAlpha=GL11.glGetInteger(0x80CA);
        final int binding=GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
        final float r,g,b,a;
        GlState(FloatBuffer color){r=color.get(0);g=color.get(1);b=color.get(2);a=color.get(3);}
    }
    private static int temperatureColor(int value,HudColorPalette p) {
        return value<=5?p.getSevereCold():value<=10?p.getCold():value<=14?p.getComfortable():value<=19?p.getWarm():p.getSevereHot();
    }
    public List<String> tooltip(MapLabel label) {
        List<String> lines=new ArrayList<String>();lines.add(label.name+"  "+label.temperature);
        lines.add("§7"+label.registryId);lines.add(I18n.format("sdtemperaturehud.map.reference"));
        lines.add(I18n.format("sdtemperaturehud.map.exclusions"));
        if(!mc.isSingleplayer())lines.add(I18n.format("sdtemperaturehud.map.client_rules"));
        return lines;
    }
}
