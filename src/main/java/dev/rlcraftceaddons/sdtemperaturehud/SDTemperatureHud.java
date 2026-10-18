/*
 * Decompiled with CFR 0.152.
 */
package dev.rlcraftceaddons.sdtemperaturehud;

import dev.rlcraftceaddons.sdtemperaturehud.client.ClientEventHandler;
import dev.rlcraftceaddons.sdtemperaturehud.client.map.MapIntegrationController;
import dev.rlcraftceaddons.sdtemperaturehud.client.HudSettings;
import dev.rlcraftceaddons.sdtemperaturehud.client.TemperatureAnalysisKey;
import dev.rlcraftceaddons.sdtemperaturehud.client.TemperatureHudRenderer;
import dev.rlcraftceaddons.sdtemperaturehud.client.WarningSoundController;
import dev.rlcraftceaddons.sdtemperaturehud.config.HudConfigNormalizer;
import dev.rlcraftceaddons.sdtemperaturehud.integration.SimpleDifficultyTemperatureSource;
import dev.rlcraftceaddons.sdtemperaturehud.temperature.TemperatureSnapshot;
import dev.rlcraftceaddons.sdtemperaturehud.temperature.TemperatureSnapshotProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid="sdtemperaturehud", name="SimpleDifficulty Temperature HUD", version="1.6.1", dependencies="required-after:simpledifficulty@[0.3.9]", acceptableRemoteVersions="*", clientSideOnly=true, guiFactory="dev.rlcraftceaddons.sdtemperaturehud.client.config.SDTemperatureHudGuiFactory")
@SideOnly(value=Side.CLIENT)
public final class SDTemperatureHud {
    public static final String MODID = "sdtemperaturehud";
    public static final String NAME = "SimpleDifficulty Temperature HUD";
    public static final String VERSION = "1.6.1";
    public static final Logger LOGGER = LogManager.getLogger((String)"sdtemperaturehud");
    private static volatile TemperatureSnapshotProvider snapshotProvider;

    @Mod.EventHandler
    public void initialize(FMLInitializationEvent event) {
        if (HudConfigNormalizer.normalize()) {
            ConfigManager.sync(MODID, Config.Type.INSTANCE);
        }
        HudSettings.refreshFromConfig();
        Minecraft minecraft = Minecraft.getMinecraft();
        TemperatureSnapshotProvider snapshotProvider = new TemperatureSnapshotProvider(new SimpleDifficultyTemperatureSource(minecraft));
        installSnapshotProvider(snapshotProvider);
        TemperatureHudRenderer renderer = new TemperatureHudRenderer(minecraft);
        WarningSoundController warningSoundController = new WarningSoundController();
        KeyBinding analysisKey = TemperatureAnalysisKey.create();
        ClientRegistry.registerKeyBinding((KeyBinding)analysisKey);
        MinecraftForge.EVENT_BUS.register((Object)new ClientEventHandler(minecraft, snapshotProvider, renderer, warningSoundController, analysisKey));
        MinecraftForge.EVENT_BUS.register(new MapIntegrationController(minecraft));
    }

    static void installSnapshotProvider(TemperatureSnapshotProvider provider) {
        if (provider == null) {
            throw new IllegalArgumentException("snapshot provider must not be null");
        }
        snapshotProvider = provider;
    }

    public static TemperatureSnapshot currentSnapshot() {
        TemperatureSnapshotProvider provider = snapshotProvider;
        return provider == null ? null : provider.getSnapshot();
    }
}
