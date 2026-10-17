package dev.rlcraftceaddons.sdtemperaturehud.client.config;

import dev.rlcraftceaddons.sdtemperaturehud.client.AnalysisDisplayMode;
import dev.rlcraftceaddons.sdtemperaturehud.client.editor.HudEditorLauncher;
import dev.rlcraftceaddons.sdtemperaturehud.client.editor.HudLayoutDefaults;
import java.io.IOException;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.client.config.GuiSlider;
import org.lwjgl.input.Keyboard;

/** A single, draft-based settings surface with four predictable pages. */
public final class GuiHudConfig extends GuiScreen {
    private final GuiScreen parent;
    private final SettingsWorkingCopy draft = SettingsWorkingCopy.fromConfig();
    private final Map<Integer, GuiSlider> sliders = new LinkedHashMap<Integer, GuiSlider>();
    private final Map<ThemeColorField, GuiTextField> colorFields =
        new EnumMap<ThemeColorField, GuiTextField>(ThemeColorField.class);
    private final Map<Integer, GuiTextField> positionFields = new LinkedHashMap<Integer, GuiTextField>();
    private final Map<GuiButton, String> tooltips = new LinkedHashMap<GuiButton, String>();
    private final String[] positionText = new String[4];
    private final boolean[] positionEdited = new boolean[4];
    private SettingsScreenLayout layout;
    private HudPreviewRenderer preview;
    private int page, section, listPage, scene = 1;
    private ThemeColorField invalidColor;
    private int invalidPosition = -1;
    private String error;
    private boolean applied;

    public GuiHudConfig(GuiScreen parentScreen) { this.parent = parentScreen; }

    @Override public void initGui() {
        syncControls();
        clearBindings();
        Keyboard.enableRepeatEvents(true);
        layout = SettingsScreenLayout.forScreen(width, height);
        preview = new HudPreviewRenderer(mc);
        preview.initializePositions(draft, width, height);
        if (positionText[0] == null) refreshPositionText(-1);
        int tabWidth = (layout.width - 6) / 4;
        for (int index = 0; index < 4; index++) {
            GuiButton tab = button(100 + index, layout.left + index * (tabWidth + 2),
                42, tabWidth, tr("page." + index), null);
            tab.enabled = index != page;
        }
        if (page == 0) buildCommon();
        else if (page == 1) buildAppearance();
        else if (page == 2) buildHints();
        else buildAdvanced();
        int size = Math.min(100, (layout.width - 16) / 3);
        int left = width / 2 - (size * 3 + 16) / 2;
        button(1, left, layout.footerY, size, tr("apply"), null);
        button(2, left + size + 8, layout.footerY, size, tr("done"), null);
        button(3, left + (size + 8) * 2, layout.footerY, size, tr("cancel"), null);
    }

    private void buildCommon() {
        toggle(300, 96, "temperature_analysis", draft.analysisEnabled);
        GuiButton mode = button(301, layout.left, 126, layout.controlWidth,
            tr("mode") + ": " + I18n.format(draft.analysisDisplayMode.getLanguageKey()),
            "sdtemperaturehud.config.analysis_display_mode.tooltip");
        mode.enabled = draft.analysisEnabled;
        button(302, layout.left, 156, layout.controlWidth,
            I18n.format("sdtemperaturehud.map.title"), null);
    }

    private void buildAppearance() {
        subTabs(3, "appearance.");
        button(900, layout.previewLeft, 72, layout.previewWidth,
            I18n.format("sdtemperaturehud.preview.scene." + scene), null);
        if (section == 0) {
            slider(400, 96, "size.compact", draft.appearance().getCompactScale(), .5, 2, true, "compact_scale");
            slider(401, 126, "size.analysis", draft.appearance().getAnalysisScale(), .5, 2, true, "analysis_scale");
            slider(402, 156, "size.alpha", draft.appearance().getBackgroundAlpha(), 0, 255, false, "background_alpha");
            GuiButton editor = button(403, layout.left, 186, layout.controlWidth,
                tr("edit_position"), editorAvailable() ? "sdtemperaturehud.settings.edit_position.tooltip" :
                "sdtemperaturehud.config.editor.unavailable");
            editor.enabled = editorAvailable();
        } else if (section == 1) {
            AppearanceItem[] items = AppearanceItem.values();
            listPage = layout.clampPage(listPage, items.length);
            for (int index = listPage * layout.rows();
                index < Math.min(items.length, (listPage + 1) * layout.rows()); index++) {
                AppearanceItem item = items[index];
                button(500 + index, layout.left, layout.rowY(index % layout.rows()), layout.controlWidth,
                    I18n.format(item.getLanguageKey()) + ": " + onOff(draft.appearance().isVisible(item)),
                    item.getLanguageKey() + ".tooltip");
            }
            pagination(items.length);
        } else {
            button(600, layout.left, 96, layout.controlWidth, presetText(),
                "sdtemperaturehud.config.theme_preset.tooltip");
            button(601, layout.left, 126, layout.controlWidth, tr("custom_colors"),
                "sdtemperaturehud.settings.custom_colors.tooltip");
        }
    }

    private void buildHints() {
        toggle(700, 96, "warning_flash", draft.warningFlashEnabled);
        toggle(701, 118, "warning_sound", draft.warningSoundEnabled);
        toggle(702, 140, "change_hints", draft.changeHintsEnabled);
        slider(703, 162, "hint.threshold", draft.changeHintThreshold, 1, 25, false, "change_hint_threshold");
        slider(704, 184, "hint.duration", draft.changeHintDurationSeconds, 1, 10, false, "change_hint_duration");
        sliders.get(703).enabled = sliders.get(704).enabled = draft.changeHintsEnabled;
    }

    private void buildAdvanced() {
        subTabs(2, "advanced.");
        if (section == 0) {
            for (int index = 0; index < 4; index++) {
                GuiTextField field = new GuiTextField(index, fontRenderer,
                    layout.left + 74, layout.rowY(index) + 1, layout.controlWidth - 76, 18);
                field.setMaxStringLength(18);
                field.setText(positionText[index]);
                field.setFocused(index == invalidPosition);
                positionFields.put(index, field);
            }
            button(800, layout.left, 186, layout.controlWidth, tr("reset"),
                "sdtemperaturehud.settings.reset.tooltip");
        } else {
            ThemeColorField[] fields = ThemeColorField.values();
            listPage = layout.clampPage(listPage, fields.length);
            for (int index = listPage * layout.rows();
                index < Math.min(fields.length, (listPage + 1) * layout.rows()); index++) {
                ThemeColorField color = fields[index];
                GuiTextField field = new GuiTextField(index, fontRenderer, layout.left + 74,
                    layout.rowY(index % layout.rows()) + 1, layout.controlWidth - 76, 18);
                field.setMaxStringLength(7);
                field.setText(draft.appearance().theme().get(color));
                field.setFocused(color == invalidColor);
                colorFields.put(color, field);
            }
            pagination(fields.length);
        }
    }

    private void subTabs(int count, String prefix) {
        int size = (layout.controlWidth - (count - 1) * 2) / count;
        for (int index = 0; index < count; index++) {
            GuiButton button = button(200 + index, layout.left + index * (size + 2), 72,
                size, tr(prefix + index), null);
            button.enabled = section != index;
        }
    }

    private void pagination(int count) {
        if (layout.pageCount(count) <= 1) return;
        button(901, layout.left, layout.pagerY, 28, "<", null).enabled = listPage > 0;
        button(902, layout.left + layout.controlWidth - 28, layout.pagerY, 28, ">", null)
            .enabled = listPage + 1 < layout.pageCount(count);
    }

    private void toggle(int id, int y, String key, boolean value) {
        button(id, layout.left, y, layout.controlWidth,
            I18n.format("sdtemperaturehud.config." + key) + ": " + onOff(value),
            "sdtemperaturehud.config." + key + ".tooltip");
    }

    private void slider(int id, int y, String label, double value, double min, double max,
        boolean decimal, String tooltip) {
        GuiSlider slider = new GuiSlider(id, layout.left + 74, y, layout.controlWidth - 74, 20,
            "", "", min, max, value, decimal, true) {
            @Override public void updateSlider() {
                if (showDecimal) setValue(Math.round(getValue() * 20) / 20.0);
                super.updateSlider();
            }
        };
        slider.precision = decimal ? 2 : 0;
        slider.updateSlider();
        sliders.put(id, slider);
        buttonList.add(slider);
        tooltips.put(slider, "sdtemperaturehud.config." + tooltip + ".tooltip");
    }

    private GuiButton button(int id, int x, int y, int size, String text, String tooltip) {
        GuiButton button = new GuiButton(id, x, y, size, 20, text);
        buttonList.add(button);
        if (tooltip != null) tooltips.put(button, tooltip);
        return button;
    }

    @Override protected void actionPerformed(GuiButton button) {
        syncControls();
        int id = button.id;
        if (id == 3) { mc.displayGuiScreen(parent); return; }
        if (id == 1 || id == 2) { save(id == 2); return; }
        if (id >= 100 && id < 104) { page = id - 100; section = listPage = 0; }
        else if (id >= 200 && id < 203) { section = id - 200; listPage = 0; }
        else if (id == 300) draft.analysisEnabled = !draft.analysisEnabled;
        else if (id == 302) { mc.displayGuiScreen(new GuiMapIntegration(this, draft)); return; }
        else if (id == 301) {
            AnalysisDisplayMode[] modes = AnalysisDisplayMode.values();
            draft.analysisDisplayMode = modes[(draft.analysisDisplayMode.ordinal() + 1) % modes.length];
        } else if (id >= 500 && id < 509) {
            AppearanceItem item = AppearanceItem.values()[id - 500];
            draft.appearance().setVisible(item, !draft.appearance().isVisible(item));
        } else if (id == 600) { draft.appearance().theme().selectNextPreset(); invalidColor = null; error = null; }
        else if (id == 601) { page = 3; section = 1; listPage = 0; }
        else if (id == 700) draft.warningFlashEnabled = !draft.warningFlashEnabled;
        else if (id == 701) draft.warningSoundEnabled = !draft.warningSoundEnabled;
        else if (id == 702) draft.changeHintsEnabled = !draft.changeHintsEnabled;
        else if (id == 403) {
            if (editorAvailable() && save(false)) {
                HudEditorLauncher.request();
                mc.displayGuiScreen(null);
            }
            return;
        } else if (id == 800) { mc.displayGuiScreen(new GuiResetSettings(this)); return; }
        else if (id == 900) scene = (scene + 1) % 3;
        else if (id == 901) listPage--;
        else if (id == 902) listPage++;
        else return;
        rebuild();
    }

    private boolean save(boolean close) {
        if (!parsePositions()) {
            page = 3; section = 0; rebuild(); return false;
        }
        if (dirty()) {
            ThemeApplyResult result = draft.apply();
            if (!result.isSuccess()) {
                invalidColor = result.getInvalidField();
                error = I18n.format("sdtemperaturehud.theme.invalid_color",
                    I18n.format(invalidColor.getLanguageKey()));
                page = 3; section = 1; listPage = invalidColor.ordinal() / layout.rows();
                rebuild();
                return false;
            }
            ConfigChangeNotifier.finish(result.isChanged(), mc.world != null, false);
            Arrays.fill(positionEdited, false);
            refreshPositionText(-1);
            invalidColor = null;
            error = null;
            applied = true;
        }
        if (close) mc.displayGuiScreen(parent);
        else rebuild();
        return true;
    }

    void resetDraft(int scope) {
        HudLayoutDefaults defaults = preview.defaults(width, height);
        draft.resetAppearance(scope, defaults.compact(), defaults.analysis());
        refreshPositionText(scope);
        invalidPosition = -1;
        invalidColor = null;
        error = null;
        rebuild();
    }

    private boolean parsePositions() {
        double[] values = {draft.getX(true), draft.getY(true), draft.getX(false), draft.getY(false)};
        for (int index = 0; index < 4; index++) {
            if (!positionEdited[index]) continue;
            try {
                double value = Double.parseDouble(positionText[index]) / 100;
                if (!Double.isFinite(value) || value < -2 || value > 3) throw new NumberFormatException();
                values[index] = value;
            } catch (NumberFormatException failure) {
                invalidPosition = index; error = tr("invalid_position"); return false;
            }
        }
        draft.setPosition(true, values[0], values[1]);
        draft.setPosition(false, values[2], values[3]);
        invalidPosition = -1;
        return true;
    }

    private void refreshPositionText(int scope) {
        for (int index = 0; index < 4; index++) {
            if (scope == 0 && index >= 2 || scope == 1 && index < 2) continue;
            boolean compact = index < 2;
            positionText[index] = String.format(java.util.Locale.ROOT, "%.3f",
                (index % 2 == 0 ? draft.getX(compact) : draft.getY(compact)) * 100);
            positionEdited[index] = false;
        }
    }

    private void syncControls() {
        for (Map.Entry<ThemeColorField, GuiTextField> entry : colorFields.entrySet())
            draft.appearance().theme().set(entry.getKey(), entry.getValue().getText());
        for (Map.Entry<Integer, GuiTextField> entry : positionFields.entrySet())
            positionText[entry.getKey()] = entry.getValue().getText();
        for (Map.Entry<Integer, GuiSlider> entry : sliders.entrySet()) {
            double value = entry.getValue().getValue();
            switch (entry.getKey()) {
                case 400: draft.appearance().setCompactScale(value); break;
                case 401: draft.appearance().setAnalysisScale(value); break;
                case 402: draft.appearance().setBackgroundAlpha((int)Math.round(value)); break;
                case 703: draft.changeHintThreshold = (int)Math.round(value); break;
                case 704: draft.changeHintDurationSeconds = (int)Math.round(value); break;
                default: break;
            }
        }
    }

    private void clearBindings() {
        sliders.clear(); colorFields.clear(); positionFields.clear(); tooltips.clear(); buttonList.clear();
    }
    private void rebuild() { clearBindings(); initGui(); }
    private boolean dirty() {
        for (boolean value : positionEdited) if (value) return true;
        return draft.hasChanges();
    }
    private boolean editorAvailable() {
        return mc != null && EditorAvailabilityPolicy.isAvailable(mc.world != null, mc.player != null);
    }
    private String presetText() {
        return tr("preset") + ": " + I18n.format(draft.appearance().theme().isCustom()
            ? "sdtemperaturehud.theme.custom" : draft.appearance().theme().getSelectedPreset().getLanguageKey());
    }

    @Override public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        syncControls();
        drawDefaultBackground();
        drawCenteredString(fontRenderer, tr("title"), width / 2, 10, 0xFFFFFF);
        fontRenderer.drawString(fontRenderer.trimStringToWidth(tr("intro." + page), layout.width - 65),
            layout.left, 26, 0xBBBBBB);
        String status = tr(dirty() ? "unsaved" : applied ? "saved" : "unchanged");
        fontRenderer.drawString(status, layout.left + layout.width - fontRenderer.getStringWidth(status),
            26, dirty() ? 0xFFD064 : 0x8FF0AD);
        if (page == 1) preview.drawPreview(layout, width, height, draft, scene);
        else drawHelp();
        if (page == 1 && section == 0) {
            sliderLabel("size.compact", 96, true);
            sliderLabel("size.analysis", 126, true);
            sliderLabel("size.alpha", 156, true);
        } else if (page == 2) {
            sliderLabel("hint.threshold", 162, draft.changeHintsEnabled);
            sliderLabel("hint.duration", 184, draft.changeHintsEnabled);
        }
        for (GuiButton button : buttonList) if (button.id == 1) button.enabled = dirty();
        super.drawScreen(mouseX, mouseY, partialTicks);
        for (Map.Entry<ThemeColorField, GuiTextField> entry : colorFields.entrySet()) {
            GuiTextField field = entry.getValue();
            fontRenderer.drawString(I18n.format(entry.getKey().getLanguageKey()),
                layout.left, field.y + 5, entry.getKey() == invalidColor ? 0xFF6666 : 0xDDDDDD);
            field.drawTextBox();
        }
        for (Map.Entry<Integer, GuiTextField> entry : positionFields.entrySet()) {
            GuiTextField field = entry.getValue();
            fontRenderer.drawString(tr("position." + entry.getKey()),
                layout.left, field.y + 5, entry.getKey() == invalidPosition ? 0xFF6666 : 0xDDDDDD);
            field.drawTextBox();
        }
        int count = page == 1 && section == 1 ? AppearanceItem.values().length :
            page == 3 && section == 1 ? ThemeColorField.values().length : 0;
        if (count > 0 && layout.pageCount(count) > 1)
            drawCenteredString(fontRenderer, (listPage + 1) + "/" + layout.pageCount(count),
                layout.left + layout.controlWidth / 2, layout.pagerY + 6, 0xCCCCCC);
        for (Map.Entry<GuiButton, String> entry : tooltips.entrySet()) {
            GuiButton button = entry.getKey();
            if (mouseX >= button.x && mouseX < button.x + button.width
                && mouseY >= button.y && mouseY < button.y + button.height)
                drawHoveringText(fontRenderer.listFormattedStringToWidth(I18n.format(entry.getValue()),
                    Math.min(240, width - 24)), mouseX, mouseY);
        }
    }

    private void drawHelp() {
        int left = layout.previewLeft, size = layout.previewWidth;
        drawRect(left, 72, left + size, height - 41, 0x55101418);
        fontRenderer.drawSplitString(tr("help." + page), left + 8, 82, size - 16, 0xC3C7CC);
        if (error != null) fontRenderer.drawSplitString(error, left + 8, 142, size - 16, 0xFF7777);
    }
    private void sliderLabel(String key, int y, boolean enabled) {
        fontRenderer.drawString(tr(key), layout.left, y + 6, enabled ? 0xDDDDDD : 0x777777);
    }
    private static String tr(String key) {
        return I18n.format("sdtemperaturehud.settings." + key).replace("\\n", "\n");
    }
    private static String onOff(boolean on) { return I18n.format(on ? "options.on" : "options.off"); }

    @Override protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_ESCAPE) { mc.displayGuiScreen(parent); return; }
        if (keyCode == Keyboard.KEY_TAB) {
            java.util.List<GuiTextField> all = new java.util.ArrayList<GuiTextField>(colorFields.values());
            all.addAll(positionFields.values());
            if (!all.isEmpty()) {
                int next = 0;
                for (int index = 0; index < all.size(); index++)
                    if (all.get(index).isFocused()) next = (index + 1) % all.size();
                for (int index = 0; index < all.size(); index++) all.get(index).setFocused(index == next);
            }
            return;
        }
        for (Map.Entry<ThemeColorField, GuiTextField> entry : colorFields.entrySet())
            if (entry.getValue().textboxKeyTyped(typedChar, keyCode)) {
                invalidColor = null; error = null; return;
            }
        for (Map.Entry<Integer, GuiTextField> entry : positionFields.entrySet()) {
            String before = entry.getValue().getText();
            if (entry.getValue().textboxKeyTyped(typedChar, keyCode)) {
                if (!before.equals(entry.getValue().getText())) positionEdited[entry.getKey()] = true;
                invalidPosition = -1; error = null; return;
            }
        }
        super.keyTyped(typedChar, keyCode);
    }
    @Override protected void mouseClicked(int x, int y, int button) throws IOException {
        for (GuiTextField field : colorFields.values()) field.mouseClicked(x, y, button);
        for (GuiTextField field : positionFields.values()) field.mouseClicked(x, y, button);
        super.mouseClicked(x, y, button);
    }
    @Override public void updateScreen() {
        for (GuiTextField field : colorFields.values()) field.updateCursorCounter();
        for (GuiTextField field : positionFields.values()) field.updateCursorCounter();
    }
    @Override public void onGuiClosed() { Keyboard.enableRepeatEvents(false); }
}
