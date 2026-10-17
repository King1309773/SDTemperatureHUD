package dev.rlcraftceaddons.sdtemperaturehud.map;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

/** Client-thread confined, indexed by 32x32 chunk regions for viewport queries. */
public final class BiomeTileStore {
    private final Map<MapWorldKey,WorldTiles> worlds=new HashMap<MapWorldKey,WorldTiles>();
    public static int region(int chunk) { return Math.floorDiv(chunk,32); }
    public static int local(int chunk) { return Math.floorMod(chunk,32); }
    private static long pair(int x,int z) { return ((long)x << 32) ^ (z & 0xffffffffL); }
    public boolean put(MapWorldKey key,BiomeTile tile) {
        WorldTiles world=worlds.get(key);
        if(world==null) {world=new WorldTiles();worlds.put(key,world);}
        long region=pair(region(tile.chunkX),region(tile.chunkZ));
        Map<Long,BiomeTile> tiles=world.regions.get(region);
        if(tiles==null) {tiles=new LinkedHashMap<Long,BiomeTile>();world.regions.put(region,tiles);}
        BiomeTile previous=tiles.put(pair(tile.chunkX,tile.chunkZ),tile);
        if(tile.equals(previous)) return false;
        world.version++; return true;
    }
    public BiomeTile get(MapWorldKey key,int x,int z) {
        WorldTiles world=worlds.get(key); if(world==null) return null;
        Map<Long,BiomeTile> tiles=world.regions.get(pair(region(x),region(z)));
        return tiles==null ? null : tiles.get(pair(x,z));
    }
    public List<BiomeTile> regionTiles(MapWorldKey key,int x,int z) {
        WorldTiles world=worlds.get(key); if(world==null) return Collections.emptyList();
        Map<Long,BiomeTile> tiles=world.regions.get(pair(x,z));
        return tiles==null ? Collections.<BiomeTile>emptyList() : new ArrayList<BiomeTile>(tiles.values());
    }
    public long version(MapWorldKey key) {WorldTiles world=worlds.get(key);return world==null?0:world.version;}
    public Set<MapWorldKey> worldKeys() {return new HashSet<MapWorldKey>(worlds.keySet());}
    public Set<Long> regionPositions(MapWorldKey key) {
        WorldTiles world=worlds.get(key);
        return world==null?Collections.<Long>emptySet():new HashSet<Long>(world.regions.keySet());
    }
    public void discardRegion(MapWorldKey key,int x,int z) {
        WorldTiles world=worlds.get(key);
        if(world!=null && world.regions.remove(pair(x,z))!=null)world.version++;
    }
    public void clear(MapWorldKey key) {worlds.remove(key);}
    public void clear() {worlds.clear();}
    private static final class WorldTiles {
        long version;
        final Map<Long,Map<Long,BiomeTile>> regions=new HashMap<Long,Map<Long,BiomeTile>>();
    }
}
