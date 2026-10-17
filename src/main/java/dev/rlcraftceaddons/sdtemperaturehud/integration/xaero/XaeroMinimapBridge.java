package dev.rlcraftceaddons.sdtemperaturehud.integration.xaero;

import dev.rlcraftceaddons.sdtemperaturehud.client.map.*;
import dev.rlcraftceaddons.sdtemperaturehud.map.*;
import java.util.*;
import net.minecraft.client.Minecraft;
import xaero.common.HudMod;
import xaero.common.XaeroMinimapSession;
import xaero.common.minimap.MinimapProcessor;
import xaero.common.minimap.render.MinimapRenderer;
import xaero.hud.gui.util.GuiUtils;
import xaero.hud.minimap.Minimap;
import xaero.hud.minimap.common.config.option.MinimapProfiledConfigOptions;
import xaero.hud.minimap.config.util.MinimapConfigClientUtils;
import xaero.hud.minimap.element.render.*;
import xaero.lib.client.config.ClientConfigManager;

/** Loaded only after the optional minimap and its exact version have been checked. */
@SuppressWarnings("deprecation") // Xaero's supported legacy session accessor bridges both target versions.
public final class XaeroMinimapBridge implements MapIntegrationController.Bridge {
    private final RegistrationGate gate=new RegistrationGate();
    private final Renderer renderer;
    public XaeroMinimapBridge(MapIntegrationController controller) {renderer=new Renderer(controller);}
    @Override public void attach() {
        if(HudMod.INSTANCE==null || HudMod.INSTANCE.getMinimap()==null)return;
        Object handler=HudMod.INSTANCE.getMinimap().getOverMapRendererHandler();
        if(gate.needsRegistration(handler)) {
            HudMod.INSTANCE.getMinimap().getOverMapRendererHandler().add(renderer);gate.attached(handler);
        }
    }
    @Override public boolean usable() {return !gate.isFailed() && HudMod.INSTANCE!=null && HudMod.INSTANCE.getMinimap()!=null;}
    @Override public String persistentWorld(int physicalDimension) {
        XaeroMinimapSession session=XaeroMinimapSession.getCurrentSession();if(session==null)return null;
        MinimapProcessor processor=session.getMinimapProcessor();
        if(processor==null || !processor.serverHasMod() || Minecraft.getMinecraft().world==null
            || Minecraft.getMinecraft().world.provider.getDimension()!=physicalDimension)return null;
        Object path=processor.getSession().getWorldState().getCurrentWorldPath();
        return path==null?null:path.toString();
    }
    private static final class Frame {
        final MapIntegrationController controller;final MapLabelPainter painter=new MapLabelPainter();
        List<MapLabel> labels=Collections.emptyList();Iterator<MapLabel> iterator=labels.iterator();float scale=1;
        Frame(MapIntegrationController controller){this.controller=controller;}
    }
    private static final class Renderer extends MinimapElementRenderer<MapLabel,Frame> {
        Renderer(MapIntegrationController controller){super(new Reader(),new Provider(),new Frame(controller));}
        @Override public int getOrder(){return 1000;}
        @Override public boolean shouldRender(MinimapElementRenderLocation location) {
            return location==MinimapElementRenderLocation.OVER_MINIMAP && context.controller.enabled(true);
        }
        @Override public void preRender(MinimapElementRenderInfo info) {
            context.labels=Collections.emptyList();
            context.painter.beginFrame();
            try {
                XaeroMinimapSession session=XaeroMinimapSession.getCurrentSession();if(session==null)return;
                MinimapProcessor processor=session.getMinimapProcessor();if(processor==null)return;
                Minimap minimap=HudMod.INSTANCE.getMinimap();
                ClientConfigManager config=HudMod.INSTANCE.getHudConfigs().getClientConfigManager();
                int shape=config.getEffective(MinimapProfiledConfigOptions.SHAPE);
                float uiScale=GuiUtils.getMinimapScale(config,info.scaledResolution.getScaleFactor());
                int size=processor.getMinimapSize(info.scaledResolution.getScaleFactor());
                double radius=(int)(size*uiScale/4f)-2;
                MinimapRenderer renderer=minimap.usingFBO()?minimap.getMinimapFBORenderer():minimap.getMinimapSafeModeRenderer();
                double angle=Math.PI-Math.toRadians(renderer.getRenderAngle(
                    MinimapConfigClientUtils.getEffectiveNorthLocked(size/2,shape)));
                double sin=Math.sin(angle),cos=Math.cos(angle),zoom=renderer.getZoom()*uiScale/2;
                if(!Double.isFinite(zoom) || zoom<=0 || radius<=0)return;
                context.scale=Math.max(.5f,uiScale*.65f);
                List<BiomeRegion> regions=context.controller.regions(true,info.mapDimension,info.renderPos.x,info.renderPos.z,
                    (int)Math.ceil(radius/zoom/16)+1);
                List<MapLabel> candidates=new ArrayList<MapLabel>();
                for(BiomeRegion region:regions) {
                    double[] point=MapProjection.minimap(region.x-info.renderPos.x,region.z-info.renderPos.z,sin,cos,zoom);
                    MapLabel label=context.painter.measure(region,point[0],point[1],context.scale,
                        info.mapDimension==info.renderEntityDimension && region.contains((int)Math.floor(info.renderPos.x),(int)Math.floor(info.renderPos.z)));
                    if(label!=null)candidates.add(label);
                }
                context.labels=MapLabelLayout.layout(candidates,radius,shape==1,Collections.emptyList(),3);
            }catch(RuntimeException | LinkageError problem){context.controller.disable(true,problem);}
        }
        @Override public void postRender(MinimapElementRenderInfo info) {}
        @Override public boolean renderElement(MapLabel label,boolean highlighted,boolean outside,double depth,
                                                float optional,double x,double y,MinimapElementRenderInfo info) {
            if(outside)return false;
            try {context.painter.paint(label,x,y,context.scale);return true;}
            catch(RuntimeException | LinkageError problem){context.controller.disable(true,problem);return false;}
        }
    }
    private static final class Provider extends MinimapElementRenderProvider<MapLabel,Frame> {
        @Override public void begin(MinimapElementRenderLocation location,Frame frame){frame.iterator=frame.labels.iterator();}
        @Override public boolean hasNext(MinimapElementRenderLocation location,Frame frame){return frame.iterator.hasNext();}
        @Override public MapLabel getNext(MinimapElementRenderLocation location,Frame frame){return frame.iterator.next();}
        @Override public void end(MinimapElementRenderLocation location,Frame frame){}
    }
    private static final class Reader extends MinimapElementReader<MapLabel,Frame> {
        @Override public boolean isHidden(MapLabel e,Frame c){return false;}
        @Override public double getRenderX(MapLabel e,Frame c,float t){return e.worldX;}
        @Override public double getRenderY(MapLabel e,Frame c,float t){return 0;}
        @Override public double getRenderZ(MapLabel e,Frame c,float t){return e.worldZ;}
        @Override public double getCoordinateScale(MapLabel e,Frame c,MinimapElementRenderInfo info){return info.backgroundCoordinateScale;}
        @Override public int getInteractionBoxLeft(MapLabel e,Frame c,float t){return 0;}
        @Override public int getInteractionBoxRight(MapLabel e,Frame c,float t){return 0;}
        @Override public int getInteractionBoxTop(MapLabel e,Frame c,float t){return 0;}
        @Override public int getInteractionBoxBottom(MapLabel e,Frame c,float t){return 0;}
        @Override public int getRenderBoxLeft(MapLabel e,Frame c,float t){return -e.width/2;}
        @Override public int getRenderBoxRight(MapLabel e,Frame c,float t){return e.width/2;}
        @Override public int getRenderBoxTop(MapLabel e,Frame c,float t){return -e.height/2;}
        @Override public int getRenderBoxBottom(MapLabel e,Frame c,float t){return e.height/2;}
        @Override public int getLeftSideLength(MapLabel e,Minecraft mc){return 0;}
        @Override public String getMenuName(MapLabel e){return e.name;}
        @Override public String getFilterName(MapLabel e){return e.registryId;}
        @Override public int getMenuTextFillLeftPadding(MapLabel e){return 0;}
        @Override public int getRightClickTitleBackgroundColor(MapLabel e){return 0;}
        @Override public boolean shouldScaleBoxWithOptionalScale(){return false;}
        @Override public boolean isInteractable(MinimapElementRenderLocation location,MapLabel e){return false;}
    }
}
