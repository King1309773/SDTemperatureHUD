package dev.rlcraftceaddons.sdtemperaturehud.client.config;

import dev.rlcraftceaddons.sdtemperaturehud.client.map.MapIntegrationController;
import java.io.IOException;
import net.minecraft.client.gui.*;
import net.minecraft.client.resources.I18n;

/** A child of the existing transaction: Back/Escape never applies or discards the parent's draft. */
public final class GuiMapIntegration extends GuiScreen {
    private final GuiScreen parent;private final SettingsWorkingCopy draft;
    public GuiMapIntegration(GuiScreen parent,SettingsWorkingCopy draft){this.parent=parent;this.draft=draft;}
    @Override public void initGui() {
        buttonList.clear();int w=Math.min(240,width-32);
        for(int i=0;i<2;i++) {
            boolean mini=i==0,value=mini?draft.minimapEnabled:draft.worldMapEnabled;
            String state=MapIntegrationController.availability(mini);
            GuiButton button=new GuiButton(i,width/2-w/2,62+i*36,w,20,
                I18n.format("sdtemperaturehud.map."+(mini?"minimap":"worldmap"))+": "+
                I18n.format(mini?"sdtemperaturehud.map.managed_short":value?"options.on":"options.off"));
            button.enabled=!mini && "ready".equals(state);buttonList.add(button);
        }
        buttonList.add(new GuiButton(2,width/2-60,height-30,120,20,I18n.format("gui.back")));
    }
    @Override protected void actionPerformed(GuiButton button) {
        if(button.id==2){mc.displayGuiScreen(parent);return;}
        if(button.id==1)draft.worldMapEnabled=!draft.worldMapEnabled;
        initGui();
    }
    @Override protected void keyTyped(char character,int code) throws IOException {
        if(code==1)mc.displayGuiScreen(parent);else super.keyTyped(character,code);
    }
    @Override public void drawScreen(int mouseX,int mouseY,float ticks) {
        drawDefaultBackground();drawCenteredString(fontRenderer,I18n.format("sdtemperaturehud.map.title"),width/2,24,0xffffff);
        for(int i=0;i<2;i++) {
            String state=MapIntegrationController.availability(i==0);
            if(!"ready".equals(state))drawCenteredString(fontRenderer,I18n.format("sdtemperaturehud.map."+state),width/2,85+i*36,0xaaaaaa);
            else if(i==0)drawCenteredString(fontRenderer,I18n.format("sdtemperaturehud.map.managed"),width/2,85,0xaaaaaa);
        }
        int y=140;
        for(String line:fontRenderer.listFormattedStringToWidth(I18n.format("sdtemperaturehud.map.help"),Math.max(100,width-40))) {
            if(y>height-46)break;drawString(fontRenderer,line,20,y,0xaaaaaa);y+=11;
        }
        super.drawScreen(mouseX,mouseY,ticks);
    }
}
