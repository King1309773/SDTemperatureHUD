package dev.rlcraftceaddons.sdtemperaturehud.client.config;

import dev.rlcraftceaddons.sdtemperaturehud.SDTemperatureHud;
import dev.rlcraftceaddons.sdtemperaturehud.client.HudSettings;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.Event;

final class ConfigChangeNotifier {
    private static final Operations FORGE_OPERATIONS = new Operations() {
        @Override
        public boolean isModLoaded() {
            return Loader.isModLoaded(SDTemperatureHud.MODID);
        }

        @Override
        public boolean postOnChanged(boolean worldRunning, boolean requiresMcRestart) {
            ConfigChangedEvent.OnConfigChangedEvent event =
                new ConfigChangedEvent.OnConfigChangedEvent(
                    SDTemperatureHud.MODID,
                    null,
                    worldRunning,
                    requiresMcRestart
                );
            MinecraftForge.EVENT_BUS.post(event);
            return event.getResult() != Event.Result.DENY;
        }

        @Override
        public void postPostChanged(boolean worldRunning, boolean requiresMcRestart) {
            MinecraftForge.EVENT_BUS.post(
                new ConfigChangedEvent.PostConfigChangedEvent(
                    SDTemperatureHud.MODID,
                    null,
                    worldRunning,
                    requiresMcRestart
                )
            );
        }

        @Override
        public void sync() {
            ConfigManager.sync(SDTemperatureHud.MODID, Config.Type.INSTANCE);
        }

        @Override
        public void refresh() {
            HudSettings.refreshFromConfig();
        }
    };

    private ConfigChangeNotifier() {
    }

    static void finish(
        boolean changed,
        boolean worldRunning,
        boolean requiresMcRestart
    ) {
        finish(changed, worldRunning, requiresMcRestart, FORGE_OPERATIONS);
    }

    static void finish(
        boolean changed,
        boolean worldRunning,
        boolean requiresMcRestart,
        Operations operations
    ) {
        if (changed && operations.isModLoaded()) {
            boolean accepted = operations.postOnChanged(worldRunning, requiresMcRestart);
            if (accepted) {
                operations.postPostChanged(worldRunning, requiresMcRestart);
            }
        }
        operations.sync();
        operations.refresh();
    }

    interface Operations {
        boolean isModLoaded();

        boolean postOnChanged(boolean worldRunning, boolean requiresMcRestart);

        void postPostChanged(boolean worldRunning, boolean requiresMcRestart);

        void sync();

        void refresh();
    }
}
