package dev.rlcraftceaddons.sdtemperaturehud.config;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ConfigEventHandlerTest {
    @Test
    public void syncsAndRefreshesOnlyForTheExactModId() {
        assertTrue(ConfigEventHandler.shouldSync("sdtemperaturehud"));
        assertFalse(ConfigEventHandler.shouldSync("SDTemperatureHud"));
        assertFalse(ConfigEventHandler.shouldSync("sdtemperaturehud-extra"));
        assertFalse(ConfigEventHandler.shouldSync(""));
        assertFalse(ConfigEventHandler.shouldSync(null));
    }
}
