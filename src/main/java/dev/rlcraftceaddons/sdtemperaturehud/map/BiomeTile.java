package dev.rlcraftceaddons.sdtemperaturehud.map;

import java.util.Arrays;
import java.util.regex.Pattern;

/** Immutable snapshot; null cells mean unknown, never a default biome. */
public final class BiomeTile {
    private static final Pattern NAME = Pattern.compile("[a-z0-9_.-]+:[a-z0-9_./-]+");
    public final int chunkX, chunkZ;
    private final String[] names;

    public BiomeTile(int chunkX, int chunkZ, String[] names) {
        if (names == null || names.length != 256)
            throw new IllegalArgumentException("one tile requires 256 biome columns");
        this.names = names.clone();
        for (String name : this.names) {
            if (name != null && (name.length() > 256 || !NAME.matcher(name).matches()))
                throw new IllegalArgumentException("invalid biome registry name");
        }
        this.chunkX = chunkX; this.chunkZ = chunkZ;
    }
    public String get(int x, int z) {
        if (x < 0 || x > 15 || z < 0 || z > 15) throw new IndexOutOfBoundsException();
        return names[z * 16 + x];
    }
    public boolean isKnown() {
        for (String name : names) if (name != null) return true;
        return false;
    }
    @Override public boolean equals(Object other) {
        if (!(other instanceof BiomeTile)) return false;
        BiomeTile tile = (BiomeTile)other;
        return chunkX == tile.chunkX && chunkZ == tile.chunkZ && Arrays.equals(names,tile.names);
    }
    @Override public int hashCode() { return 31 * (31 * chunkX + chunkZ) + Arrays.hashCode(names); }
}
