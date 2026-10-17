package dev.rlcraftceaddons.sdtemperaturehud.client.config;

import dev.rlcraftceaddons.sdtemperaturehud.config.HudConfig;
import org.junit.Test;
import static org.junit.Assert.*;

public class MapIntegrationDraftTest {
    @Test public void mapTogglesShareApplyAndCancelSemantics() {
        boolean mini=HudConfig.mapIntegration.minimapEnabled,world=HudConfig.mapIntegration.worldMapEnabled;
        try {
            SettingsWorkingCopy draft=SettingsWorkingCopy.fromConfig();
            draft.minimapEnabled=!mini;draft.worldMapEnabled=!world;
            assertTrue(draft.hasChanges());assertEquals(mini,HudConfig.mapIntegration.minimapEnabled);
            assertTrue(draft.apply().isSuccess());assertFalse(draft.hasChanges());
            assertEquals(!mini,HudConfig.mapIntegration.minimapEnabled);
            draft.minimapEnabled=mini;
            assertEquals(!mini,HudConfig.mapIntegration.minimapEnabled);
        } finally {HudConfig.mapIntegration.minimapEnabled=mini;HudConfig.mapIntegration.worldMapEnabled=world;}
    }
    @Test public void invalidAppearanceAlsoRejectsMapChanges() {
        boolean original=HudConfig.mapIntegration.minimapEnabled;
        SettingsWorkingCopy draft=SettingsWorkingCopy.fromConfig();draft.minimapEnabled=!original;
        draft.appearance().theme().set(ThemeColorField.TEXT,"invalid");
        assertFalse(draft.apply().isSuccess());assertEquals(original,HudConfig.mapIntegration.minimapEnabled);
    }
}
