package dev.rlcraftceaddons.sdtemperaturehud.integration;

import java.util.OptionalInt;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;

/** Bounded read of the native info position. No generation, provider fallback or world cache. */
public final class LoadedBiomeTemperature {
    private final BiomeColumnAccess columns=new BiomeColumnAccess();
    private final SimpleDifficultyBiomeRules rules=new SimpleDifficultyBiomeRules();
    public OptionalInt at(WorldClient world,BlockPos position) {
        if(world==null || position==null)return OptionalInt.empty();
        int x=position.getX(),z=position.getZ();
        Chunk chunk=world.getChunkProvider().getLoadedChunk(x>>4,z>>4);
        if(chunk==null || !chunk.isLoaded())return OptionalInt.empty();
        int id=biomeIdAt(columns.read(chunk),x,z);if(id<0)return OptionalInt.empty();
        Biome biome=Biome.getBiome(id);
        if(biome==null || Biome.getIdForBiome(biome)!=id || biome.getRegistryName()==null)return OptionalInt.empty();
        return rules.temperatureFor(biome);
    }
    static int biomeIdAt(int[] columns,int x,int z) {
        if(columns==null || columns.length!=256)throw new IllegalArgumentException("Expected 256 biome columns");
        return columns[(z&15)*16+(x&15)];
    }
}
