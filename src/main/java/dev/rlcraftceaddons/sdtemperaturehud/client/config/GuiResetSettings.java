package dev.rlcraftceaddons.sdtemperaturehud.client.config;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.client.resources.I18n;
import org.lwjgl.input.Keyboard;

public final class GuiResetSettings extends GuiScreen implements GuiYesNoCallback {
    private final GuiHudConfig parent;
    GuiResetSettings(GuiHudConfig parent) { this.parent = parent; }

    @Override public void initGui() {
        buttonList.clear();
        int size = Math.min(240, width - 32);
        int left = (width - size) / 2;
        for (int index = 0; index < 3; index++)
            buttonList.add(new GuiButton(index, left, 68 + index * 28, size, 20,
                I18n.format("sdtemperaturehud.settings.reset." + index)));
        buttonList.add(new GuiButton(3, left, height - 27, size, 20, I18n.format("gui.back")));
    }
    @Override protected void actionPerformed(GuiButton button) {
        if (button.id == 3) { mc.displayGuiScreen(parent); return; }
        if (button.id >= 0 && button.id < 3)
            mc.displayGuiScreen(new GuiYesNo(this,
                I18n.format("sdtemperaturehud.config.reset.confirm.title"),
                I18n.format("sdtemperaturehud.settings.reset.confirm." + button.id), button.id));
    }
    @Override public void confirmClicked(boolean confirmed, int id) {
        if (confirmed) {
            parent.resetDraft(id);
            mc.displayGuiScreen(parent);
        } else mc.displayGuiScreen(this);
    }
    @Override public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRenderer, I18n.format("sdtemperaturehud.settings.reset"),
            width / 2, 14, 0xFFFFFF);
        drawCenteredString(fontRenderer, I18n.format("sdtemperaturehud.settings.reset.notice"),
            width / 2, 36, 0xBBBBBB);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
    @Override protected void keyTyped(char typedChar, int keyCode) throws java.io.IOException {
        if (keyCode == Keyboard.KEY_ESCAPE) { mc.displayGuiScreen(parent); return; }
        super.keyTyped(typedChar, keyCode);
    }
}

