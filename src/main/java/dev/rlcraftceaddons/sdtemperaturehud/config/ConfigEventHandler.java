/*
 * Decompiled with CFR 0.152.
 */
package dev.rlcraftceaddons.sdtemperaturehud.config;

import dev.rlcraftceaddons.sdtemperaturehud.client.HudSettings;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(modid="sdtemperaturehud", value={Side.CLIENT})
public final class ConfigEventHandler {
    private ConfigEventHandler() {
    }

    @SubscribeEvent
    public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (ConfigEventHandler.shouldSync(event.getModID())) {
            ConfigManager.sync((String)"sdtemperaturehud", (Config.Type)Config.Type.INSTANCE);
            if (HudConfigNormalizer.normalize()) {
                ConfigManager.sync((String)"sdtemperaturehud", (Config.Type)Config.Type.INSTANCE);
            }
            HudSettings.refreshFromConfig();
        }
    }

    static boolean shouldSync(String changedModId) {
        return "sdtemperaturehud".equals(changedModId);
    }
}
