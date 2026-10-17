package dev.rlcraftceaddons.sdtemperaturehud.integration.xaero;

import dev.rlcraftceaddons.sdtemperaturehud.client.map.*;
import dev.rlcraftceaddons.sdtemperaturehud.integration.SimpleDifficultyBiomeRules;
import dev.rlcraftceaddons.sdtemperaturehud.map.MapLabel;
import java.util.OptionalInt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.client.event.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import xaero.map.WorldMap;
import xaero.map.WorldMapSession;
import xaero.map.MapProcessor;
import xaero.map.gui.GuiMap;
import xaero.map.element.render.*;
import xaero.map.region.LeveledRegion;
import xaero.map.region.texture.RegionTexture;
import xaero.map.common.config.option.WorldMapProfiledConfigOptions;
import xaero.lib.client.config.ClientConfigManager;

/** Append a read-only value to Xaero's own hovered-biome heading; no region elements. */
public final class XaeroWorldMapBridge implements MapIntegrationController.Bridge {
    private final RegistrationGate gate=new RegistrationGate();
    private final Renderer renderer;
    public XaeroWorldMapBridge(MapIntegrationController controller) {
        renderer=new Renderer(controller);MinecraftForge.EVENT_BUS.register(this);
    }
    @Override public void attach() {
        if(gate.needsRegistration(WorldMap.mapElementRenderHandler)) {
            WorldMap.mapElementRenderHandler.add(renderer);gate.attached(WorldMap.mapElementRenderHandler);
        }
    }
    @Override public boolean usable(){return !gate.isFailed() && WorldMap.mapElementRenderHandler!=null;}
    @Override public String persistentWorld(int physicalDimension) {
        // Retained for the dormant sampling engine; heading rendering does not persist any data.
        WorldMapSession session=WorldMapSession.getCurrentSession();if(session==null || !session.isUsable())return null;
        MapProcessor processor=session.getMapProcessor();
        if(processor==null || processor.getWorld()!=Minecraft.getMinecraft().world || !processor.serverHasMod()
            || processor.isWaitingForWorldUpdate() || !processor.isMapWorldUsable())return null;
        if(processor.getMapWorld().getCurrentDimension()==null
            || processor.getMapWorld().getCurrentDimension().getDimId()!=physicalDimension
            || !processor.getMapWorld().isCurrentMultiworldTypeConfirmed()
            || !processor.getMapWorld().getCurrentDimension().hasConfirmedMultiworld()
            || !processor.getMapWorld().getCurrentDimension().isAutoSelected())return null;
        String root=processor.getCurrentWorldId(),sub=processor.getCurrentMWId();
        return root==null || sub==null?null:root+"\u0000"+sub;
    }
    @SubscribeEvent public void opened(GuiOpenEvent event) {
        renderer.context.heading=null;renderer.context.gui=null;
    }
    @SubscribeEvent public void before(GuiScreenEvent.DrawScreenEvent.Pre event) {
        renderer.context.heading=null;
    }
    @SubscribeEvent public void heading(GuiScreenEvent.DrawScreenEvent.Post event) {
        Frame frame=renderer.context;Heading heading=frame.heading;frame.heading=null;
        if(event.getGui()!=frame.gui || heading==null || !frame.controller.enabled(false) || GuiMap.hiddenUI)return;
        try {
            Minecraft mc=Minecraft.getMinecraft();
            int x=MapHeadingLayout.suffixX(event.getGui().width,mc.fontRenderer.getStringWidth(heading.name),
                mc.fontRenderer.getStringWidth(" "));
            int numberWidth=mc.fontRenderer.getStringWidth(Integer.toString(heading.temperature));
            if(MapHeadingLayout.fits(x,numberWidth,event.getGui().width))
                frame.painter.paintHeading(heading.temperature,x,heading.y);
        }catch(RuntimeException | LinkageError exception){frame.controller.disable(false,exception);}
    }
    private static final class Heading {
        final String name;final int temperature,y;
        Heading(String name,int temperature,int y){this.name=name;this.temperature=temperature;this.y=y;}
    }
    private static final class Frame {
        final MapIntegrationController controller;final MapLabelPainter painter=new MapLabelPainter();
        final SimpleDifficultyBiomeRules rules=new SimpleDifficultyBiomeRules();
        final NativeBiomeHeadingName headingNames=new NativeBiomeHeadingName();
        GuiScreen gui;Heading heading;
        Frame(MapIntegrationController controller){this.controller=controller;}
    }
    private static final class Renderer extends ElementRenderer<MapLabel,Frame,Renderer> {
        final Frame context;
        Renderer(MapIntegrationController controller){this(new Frame(controller));}
        private Renderer(Frame frame){super(frame,new Provider(),new Reader());this.context=frame;}
        @Override public int getOrder(){return 1000;}
        @Override public boolean shouldBeDimScaled(){return false;}
        @Override public boolean shouldRenderHovered(boolean menu){return false;}
        @Override public boolean shouldRender(ElementRenderLocation location,boolean shadows) {
            return !shadows && location==ElementRenderLocation.WORLD_MAP && context.controller.enabled(false);
        }
        @Override public void preRender(ElementRenderInfo info,boolean shadows) {
            context.heading=null;context.gui=null;
            try {
                Minecraft mc=Minecraft.getMinecraft();
                if(!(mc.currentScreen instanceof GuiMap) || GuiMap.hiddenUI || WorldMap.INSTANCE==null)return;
                MapProcessor processor=((GuiMap)mc.currentScreen).getMapProcessor();
                if(processor==null || processor.getWorld()!=mc.world || !processor.isMapWorldUsable()
                    || processor.isWaitingForWorldUpdate() || processor.getMapWorld().getCurrentDimension()==null)return;
                ClientConfigManager config=WorldMap.INSTANCE.getConfigs().getClientConfigManager();
                if(!Boolean.TRUE.equals(config.getEffective(WorldMapProfiledConfigOptions.DISPLAY_HOVERED_BIOME)))return;
                int level=processor.getMapSaveLoad().mainTextureLevel;
                MapHeadingLayout.Address a=MapHeadingLayout.at(info.mouseX,info.mouseZ,level);
                // getLeveledRegion only looks up an existing region; it does not request loading.
                LeveledRegion<?> region=processor.getLeveledRegion(processor.getCurrentCaveLayer(),a.regionX,a.regionZ,level);
                if(region==null || !region.hasTextures())return;
                RegionTexture<?> texture=region.getTexture(a.textureX,a.textureZ);if(texture==null)return;
                int id=texture.getBiome(a.pixelX,a.pixelZ);if(id<0)return;
                Biome biome=Biome.getBiome(id);
                if(biome==null || Biome.getIdForBiome(biome)!=id || biome.getRegistryName()==null)return;
                OptionalInt value=context.rules.temperatureFor(biome);if(!value.isPresent())return;
                String name=context.headingNames.name(biome);if(name==null || name.isEmpty())return;
                int y=MapHeadingLayout.rowY(Boolean.TRUE.equals(config.getEffective(WorldMapProfiledConfigOptions.COORDINATES)));
                context.gui=mc.currentScreen;context.heading=new Heading(name,value.getAsInt(),y);
            }catch(RuntimeException | LinkageError problem){context.controller.disable(false,problem);}
        }
        @Override public void postRender(ElementRenderInfo info,boolean shadows){}
        @Override public void renderElementShadow(MapLabel e,boolean hovered,float optional,double x,double y,ElementRenderInfo info){}
        @Override public boolean renderElement(MapLabel e,boolean hovered,double depth,float optional,double x,double y,ElementRenderInfo info){return false;}
    }
    /** No positioned labels, hovering targets or click handling remain on the map. */
    private static final class Provider extends ElementRenderProvider<MapLabel,Frame> {
        @Override public void begin(ElementRenderLocation location,Frame frame){}
        @Override public boolean hasNext(ElementRenderLocation location,Frame frame){return false;}
        @Override public MapLabel getNext(ElementRenderLocation location,Frame frame){return null;}
        @Override public void end(ElementRenderLocation location,Frame frame){}
    }
    private static final class Reader extends ElementReader<MapLabel,Frame,Renderer> {
        @Override public boolean isHidden(MapLabel e,Frame c){return true;}
        @Override public double getRenderX(MapLabel e,Frame c,float t){return 0;}
        @Override public double getRenderZ(MapLabel e,Frame c,float t){return 0;}
        @Override public int getInteractionBoxLeft(MapLabel e,Frame c,float t){return 0;}
        @Override public int getInteractionBoxRight(MapLabel e,Frame c,float t){return 0;}
        @Override public int getInteractionBoxTop(MapLabel e,Frame c,float t){return 0;}
        @Override public int getInteractionBoxBottom(MapLabel e,Frame c,float t){return 0;}
        @Override public int getRenderBoxLeft(MapLabel e,Frame c,float t){return 0;}
        @Override public int getRenderBoxRight(MapLabel e,Frame c,float t){return 0;}
        @Override public int getRenderBoxTop(MapLabel e,Frame c,float t){return 0;}
        @Override public int getRenderBoxBottom(MapLabel e,Frame c,float t){return 0;}
        @Override public int getLeftSideLength(MapLabel e,Minecraft mc){return 0;}
        @Override public String getMenuName(MapLabel e){return "";}
        @Override public String getFilterName(MapLabel e){return "";}
        @Override public int getMenuTextFillLeftPadding(MapLabel e){return 0;}
        @Override public int getRightClickTitleBackgroundColor(MapLabel e){return 0;}
        @Override public boolean shouldScaleBoxWithOptionalScale(){return false;}
        @Override public boolean isInteractable(ElementRenderLocation location,MapLabel e){return false;}
        @Override public boolean isRightClickValid(MapLabel e){return false;}
    }
}
