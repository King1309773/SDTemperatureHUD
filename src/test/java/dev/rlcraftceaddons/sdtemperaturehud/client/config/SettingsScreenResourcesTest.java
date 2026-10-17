package dev.rlcraftceaddons.sdtemperaturehud.client.config;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Properties;
import org.junit.Test;
import static org.junit.Assert.*;

public class SettingsScreenResourcesTest {
    @Test public void settingsLabelsAreLocalizedAndSafeForMinecraftFormatting() throws Exception {
        for (String locale : new String[] {"zh_cn", "en_us"}) {
            Properties text = new Properties();
            try (InputStream input = getClass().getClassLoader()
                .getResourceAsStream("assets/sdtemperaturehud/lang/" + locale + ".lang")) {
                assertNotNull(input);
                text.load(new InputStreamReader(input, StandardCharsets.UTF_8));
            }
            for (String key : new String[] {"title", "page.0", "page.1", "page.2", "page.3",
                "apply", "done", "cancel", "reset", "invalid_position"}) {
                assertNotNull(locale + " " + key, text.getProperty("sdtemperaturehud.settings." + key));
            }
            for (String key : text.stringPropertyNames())
                if (key.startsWith("sdtemperaturehud.settings."))
                    assertFalse(key, String.format(Locale.ROOT, text.getProperty(key)).isEmpty());
        }
    }
}

