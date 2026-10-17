package dev.rlcraftceaddons.sdtemperaturehud.client.config;

import dev.rlcraftceaddons.sdtemperaturehud.client.AnalysisDisplayMode;
import dev.rlcraftceaddons.sdtemperaturehud.client.theme.HudThemePreset;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ConfigEnumLocalizationTest {
    @Test
    public void forgeEnumChoicesReferenceTheirChineseLocalizationKeys() {
        assertEquals("sdtemperaturehud.config.analysis_mode.hold", AnalysisDisplayMode.HOLD.getLanguageKey());
        assertEquals("sdtemperaturehud.config.analysis_mode.toggle", AnalysisDisplayMode.TOGGLE.getLanguageKey());
        assertEquals("sdtemperaturehud.config.analysis_mode.always", AnalysisDisplayMode.ALWAYS.getLanguageKey());
        assertEquals("sdtemperaturehud.config.theme.preset.classic", HudThemePreset.CLASSIC.getLanguageKey());
        assertEquals("sdtemperaturehud.config.theme.preset.high_contrast", HudThemePreset.HIGH_CONTRAST.getLanguageKey());
        assertEquals("sdtemperaturehud.config.theme.preset.color_blind", HudThemePreset.COLOR_BLIND.getLanguageKey());
        assertEquals("sdtemperaturehud.config.theme.preset.monochrome", HudThemePreset.MONOCHROME.getLanguageKey());
    }
}
