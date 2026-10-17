package dev.rlcraftceaddons.sdtemperaturehud.client;

import java.util.Locale;
import net.minecraft.client.resources.I18n;

public enum AnalysisDisplayMode {
    HOLD,
    TOGGLE,
    ALWAYS;

    public String getLanguageKey() {
        return "sdtemperaturehud.config.analysis_mode." + this.name().toLowerCase(Locale.ROOT);
    }

    @Override
    public String toString() {
        return I18n.format(this.getLanguageKey());
    }
}
