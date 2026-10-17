package dev.rlcraftceaddons.sdtemperaturehud.integration.xaero;

import dev.rlcraftceaddons.sdtemperaturehud.client.HudSettings;
import dev.rlcraftceaddons.sdtemperaturehud.client.map.MapIntegrationController;
import dev.rlcraftceaddons.sdtemperaturehud.client.theme.HudColorPalette;
import dev.rlcraftceaddons.sdtemperaturehud.config.HudConfig;
import dev.rlcraftceaddons.sdtemperaturehud.integration.LoadedBiomeTemperature;
import java.util.OptionalInt;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.*;
import xaero.common.HudMod;
import xaero.hud.minimap.info.*;
import xaero.hud.minimap.info.render.compile.InfoDisplayCompiler;
import xaero.hud.minimap.info.widget.InfoDisplayCommonWidgetFactories;
import xaero.hud.minimap.module.MinimapSession;
import xaero.lib.common.config.option.value.io.serialization.BuiltInConfigValueIOCodecs;

/** A native info item, not an over-map renderer; Xaero owns its profile and visual placement. */
public final class XaeroMinimapInfoBridge implements MapIntegrationController.Bridge {
    private final MapIntegrationController controller;
    private final RegistrationGate gate=new RegistrationGate();
    private final LoadedBiomeTemperature temperatures=new LoadedBiomeTemperature();
    public XaeroMinimapInfoBridge(MapIntegrationController controller){this.controller=controller;}
    private InfoDisplayManager manager() {
        if(HudMod.INSTANCE==null || HudMod.INSTANCE.getMinimap()==null || HudMod.INSTANCE.getMinimap().getInfoDisplays()==null)return null;
        return HudMod.INSTANCE.getMinimap().getInfoDisplays().getManager();
    }
    @Override public void attach() {
        InfoDisplayManager manager=manager();if(!gate.needsRegistration(manager))return;
        InfoDisplay<Boolean> display=InfoDisplay.Builder.<Boolean>begin().setId(BiomeInfoLine.DISPLAY_ID)
            .setName(new TextComponentTranslation("sdtemperaturehud.map.info.name"))
            .setDefaultState(HudConfig.mapIntegration.minimapEnabled)
            .setCodec(BuiltInConfigValueIOCodecs.BOOLEAN).setWidgetFactory(InfoDisplayCommonWidgetFactories.OFF_ON)
            .setCompiler(this::compile).build();
        ensureRegistered(manager,display);gate.attached(manager);
    }
    static void ensureRegistered(InfoDisplayManager manager,InfoDisplay<?> display) {
        if(manager.get(display.getId())==null)manager.add(display);
        manager.applyLocalConfig();
    }
    @Override public boolean usable(){return !gate.isFailed() && manager()!=null;}
    @Override public String persistentWorld(int physicalDimension){return null;}
    private void compile(InfoDisplay<Boolean> display,InfoDisplayCompiler compiler,MinimapSession session,int width,BlockPos position) {
        try {
            if(!controller.enabled(true) || !Boolean.TRUE.equals(display.getEffectiveState()) || session==null || width<1)return;
            Minecraft mc=session.getMc();if(mc==null || mc.fontRenderer==null)return;
            OptionalInt value=temperatures.at(mc.world,position);if(!value.isPresent())return;
            HudColorPalette palette=HudSettings.current().getColorPalette();
            ITextComponent number=BiomeInfoLine.number(value.getAsInt(),palette);
            if(mc.fontRenderer.getStringWidth(number.getFormattedText())>width)return;
            ITextComponent line=BiomeInfoLine.line(value.getAsInt(),palette);
            compiler.addLine(mc.fontRenderer.getStringWidth(line.getFormattedText())<=width?line:number);
        }catch(RuntimeException | LinkageError problem){controller.disable(true,problem);}
    }
}
