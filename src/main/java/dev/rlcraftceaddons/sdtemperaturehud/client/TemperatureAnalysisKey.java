/*
 * Decompiled with CFR 0.152.
 */
package dev.rlcraftceaddons.sdtemperaturehud.client;

import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;

public final class TemperatureAnalysisKey {
    public static final int DEFAULT_KEY_CODE = Keyboard.KEY_TAB;
    public static final String DESCRIPTION_KEY = "key.sdtemperaturehud.analysis";
    public static final String CATEGORY_KEY = "key.categories.sdtemperaturehud";
    private boolean previousDown;
    private boolean toggleVisible;

    public TemperatureAnalysisKey() {
    }

    public static KeyBinding create() {
        return new KeyBinding(DESCRIPTION_KEY, DEFAULT_KEY_CODE, CATEGORY_KEY);
    }

    public boolean isVisible(AnalysisDisplayMode mode, boolean down) {
        boolean pressed = down && !this.previousDown;
        this.previousDown = down;

        AnalysisDisplayMode safeMode = mode == null ? AnalysisDisplayMode.HOLD : mode;
        if (safeMode == AnalysisDisplayMode.ALWAYS) {
            return true;
        }
        if (safeMode == AnalysisDisplayMode.TOGGLE) {
            if (pressed) {
                this.toggleVisible = !this.toggleVisible;
            }
            return this.toggleVisible;
        }
        return down;
    }

    public void reset() {
        this.previousDown = false;
        this.toggleVisible = false;
    }
}
