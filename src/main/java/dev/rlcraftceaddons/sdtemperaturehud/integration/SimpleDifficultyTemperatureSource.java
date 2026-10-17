package dev.rlcraftceaddons.sdtemperaturehud.integration;

import com.charles445.simpledifficulty.api.SDCapabilities;
import com.charles445.simpledifficulty.api.temperature.ITemperatureCapability;
import com.charles445.simpledifficulty.api.temperature.ITemperatureDynamicModifier;
import com.charles445.simpledifficulty.api.temperature.ITemperatureModifier;
import com.charles445.simpledifficulty.api.temperature.TemperatureRegistry;
import com.charles445.simpledifficulty.api.temperature.TemperatureUtil;
import com.charles445.simpledifficulty.util.WorldUtil;
import dev.rlcraftceaddons.sdtemperaturehud.SDTemperatureHud;
import dev.rlcraftceaddons.sdtemperaturehud.temperature.TemperatureBreakdown;
import dev.rlcraftceaddons.sdtemperaturehud.temperature.TemperatureBreakdownAccumulator;
import dev.rlcraftceaddons.sdtemperaturehud.temperature.TemperatureModifierCategory;
import dev.rlcraftceaddons.sdtemperaturehud.temperature.TemperatureSnapshot;
import dev.rlcraftceaddons.sdtemperaturehud.temperature.TemperatureSource;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class SimpleDifficultyTemperatureSource implements TemperatureSource {
    private static final long ERROR_LOG_INTERVAL_MILLIS = 30_000L;

    private final SnapshotReader reader;
    private final Clock clock;
    private final WarningSink warnings;
    private final WarningRateLimiter warningRateLimiter;

    public SimpleDifficultyTemperatureSource(Minecraft minecraft) {
        this(
            new MinecraftSnapshotReader(minecraft),
            new Clock() {
                @Override
                public long now() {
                    return System.currentTimeMillis();
                }
            },
            new WarningSink() {
                @Override
                public void warn(String context, Throwable cause) {
                    String message = "SimpleDifficulty temperature data unavailable ("
                        + context
                        + "); compact HUD will show --";
                    if (cause == null) {
                        SDTemperatureHud.LOGGER.warn(message);
                    } else {
                        SDTemperatureHud.LOGGER.warn(message, cause);
                    }
                }
            },
            ERROR_LOG_INTERVAL_MILLIS
        );
    }

    SimpleDifficultyTemperatureSource(
        SnapshotReader reader,
        Clock clock,
        WarningSink warnings,
        long warningIntervalMillis
    ) {
        if (reader == null || clock == null || warnings == null) {
            throw new IllegalArgumentException("temperature source dependencies must not be null");
        }
        this.reader = reader;
        this.clock = clock;
        this.warnings = warnings;
        this.warningRateLimiter = new WarningRateLimiter(warningIntervalMillis);
    }

    @Override
    public TemperatureSnapshot read() {
        try {
            TemperatureSnapshot snapshot = this.reader.read();
            if (snapshot == null) {
                throw DataUnavailableException.missing("integration returned no snapshot");
            }
            this.warningRateLimiter.recovered();
            return snapshot;
        } catch (DataUnavailableException exception) {
            if (this.warningRateLimiter.shouldLog(this.clock.now())) {
                this.warnings.warn(exception.getContext(), exception.getCause());
            }
            return TemperatureSnapshot.unavailable();
        }
    }

    interface SnapshotReader {
        TemperatureSnapshot read() throws DataUnavailableException;
    }

    interface Clock {
        long now();
    }

    interface WarningSink {
        void warn(String context, Throwable cause);
    }

    static final class DataUnavailableException extends Exception {
        private final String context;

        private DataUnavailableException(String context, Throwable cause) {
            super(context, cause);
            this.context = context;
        }

        static DataUnavailableException missing(String context) {
            return new DataUnavailableException(context, null);
        }

        String getContext() {
            return this.context;
        }
    }

    private static final class WarningRateLimiter {
        private final long intervalMillis;
        private long nextLogTime;

        private WarningRateLimiter(long intervalMillis) {
            if (intervalMillis < 0L) {
                throw new IllegalArgumentException("warning interval must not be negative");
            }
            this.intervalMillis = intervalMillis;
        }

        private synchronized boolean shouldLog(long now) {
            if (now < this.nextLogTime) {
                return false;
            }
            this.nextLogTime = now > Long.MAX_VALUE - this.intervalMillis
                ? Long.MAX_VALUE
                : now + this.intervalMillis;
            return true;
        }

        private synchronized void recovered() {
            this.nextLogTime = 0L;
        }
    }

    private static final class MinecraftSnapshotReader implements SnapshotReader {
        private final Minecraft minecraft;

        private MinecraftSnapshotReader(Minecraft minecraft) {
            this.minecraft = minecraft;
        }

        @Override
        public TemperatureSnapshot read() throws DataUnavailableException {
            if (this.minecraft == null) {
                throw DataUnavailableException.missing("Minecraft client is absent");
            }
            EntityPlayerSP player = this.minecraft.player;
            if (player == null) {
                throw DataUnavailableException.missing("client player is absent");
            }
            if (SDCapabilities.TEMPERATURE == null) {
                throw DataUnavailableException.missing(
                    "SimpleDifficulty temperature capability API is not initialized"
                );
            }
            ITemperatureCapability capability = SDCapabilities.getTemperatureData((EntityPlayer)player);
            if (capability == null) {
                throw DataUnavailableException.missing(
                    "player has no SimpleDifficulty temperature capability"
                );
            }
            if (TemperatureUtil.internal == null) {
                throw DataUnavailableException.missing(
                    "SimpleDifficulty temperature utility API is not initialized"
                );
            }
            int bodyTemperature = TemperatureUtil.clampTemperature(
                capability.getTemperatureLevel()
            );
            return TemperatureSnapshot.withAnalysis(
                bodyTemperature,
                this.readBreakdown(player)
            );
        }

        private TemperatureBreakdown readBreakdown(EntityPlayerSP player)
            throws DataUnavailableException {
            World world = player.getEntityWorld();
            if (world == null) {
                throw DataUnavailableException.missing("client world is absent");
            }
            BlockPos position = WorldUtil.getSidedBlockPos((World)world, (Entity)player);
            if (position == null) {
                throw DataUnavailableException.missing(
                    "SimpleDifficulty did not provide a player block position"
                );
            }
            if (TemperatureRegistry.modifiers == null
                || TemperatureRegistry.dynamicModifiers == null) {
                throw DataUnavailableException.missing(
                    "SimpleDifficulty temperature modifier registries are not initialized"
                );
            }

            TemperatureBreakdownAccumulator accumulator = new TemperatureBreakdownAccumulator();
            float target = 0.0f;
            for (Map.Entry<String, ITemperatureModifier> entry
                : TemperatureRegistry.modifiers.entrySet()) {
                ITemperatureModifier modifier = entry.getValue();
                if (modifier == null) {
                    throw DataUnavailableException.missing(
                        "temperature modifier is absent for registry key " + entry.getKey()
                    );
                }
                TemperatureModifierCategory category =
                    TemperatureModifierCategory.forName(modifier.getName());
                float worldInfluence = modifier.getWorldInfluence(world, position);
                target += worldInfluence;
                accumulator.add(category, worldInfluence);
                float playerInfluence = modifier.getPlayerInfluence((EntityPlayer)player);
                target += playerInfluence;
                accumulator.add(category, playerInfluence);
            }
            for (Map.Entry<String, ITemperatureDynamicModifier> entry
                : TemperatureRegistry.dynamicModifiers.entrySet()) {
                ITemperatureDynamicModifier modifier = entry.getValue();
                if (modifier == null) {
                    throw DataUnavailableException.missing(
                        "dynamic temperature modifier is absent for registry key "
                            + entry.getKey()
                    );
                }
                float before = target;
                target = modifier.applyDynamicWorldInfluence(world, position, target);
                target = modifier.applyDynamicPlayerInfluence((EntityPlayer)player, target);
                accumulator.add(
                    TemperatureModifierCategory.forName(modifier.getName()),
                    target - before
                );
            }
            int clampedTarget = TemperatureUtil.clampTemperature((int)target);
            return accumulator.finish(clampedTarget);
        }
    }
}
