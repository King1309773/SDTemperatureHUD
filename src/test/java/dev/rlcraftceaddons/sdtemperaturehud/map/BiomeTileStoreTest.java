package dev.rlcraftceaddons.sdtemperaturehud.map;

import org.junit.Test;
import static org.junit.Assert.*;

public class BiomeTileStoreTest {
    static MapWorldKey key(int dimension,long generation) {
        return new MapWorldKey("test","world",dimension,generation,true);
    }
    static BiomeTile tile(int x,int z,String name) {
        String[] names=new String[256]; names[0]=name;
        return new BiomeTile(x,z,names);
    }
    @Test public void copiesInputsAndUsesNegativeCoordinates() {
        String[] names=new String[256]; names[0]="minecraft:plains";
        BiomeTile tile=new BiomeTile(-1,-33,names); names[0]="minecraft:desert";
        assertEquals("minecraft:plains",tile.get(0,0)); assertNull(tile.get(1,0));
        assertEquals(-1,BiomeTileStore.region(-1)); assertEquals(-2,BiomeTileStore.region(-33));
        assertEquals(31,BiomeTileStore.local(-1));
    }
    @Test public void isolatesWorldsDimensionsAndGenerationsAndVersionsOnlyChanges() {
        BiomeTileStore store=new BiomeTileStore(); MapWorldKey a=key(0,1);
        store.put(a,tile(-1,0,"a:one")); long version=store.version(a);
        store.put(a,tile(-1,0,"a:one")); assertEquals(version,store.version(a));
        assertNull(store.get(key(-1,1),-1,0)); assertNull(store.get(key(0,2),-1,0));
        assertNull(store.get(new MapWorldKey("other","world",0,1,true),-1,0));
        store.put(a,tile(-1,0,"b:one")); assertTrue(store.version(a)>version);
        assertEquals("b:one",store.get(a,-1,0).get(0,0)); store.clear(a); assertNull(store.get(a,-1,0));
    }
    @Test(expected=IllegalArgumentException.class) public void rejectsWrongTileLength() {
        new BiomeTile(0,0,new String[1]);
    }
    @Test(expected=IllegalArgumentException.class) public void rejectsInvalidRegistryNames() {
        tile(0,0,"../../outside");
    }
    @Test public void discardsOnlySpecifiedRegionAndInvalidatesPreparedViews() {
        BiomeTileStore store=new BiomeTileStore();MapWorldKey key=key(0,1);
        store.put(key,tile(0,0,"a:b"));store.put(key,tile(64,0,"a:c"));long version=store.version(key);
        assertEquals(2,store.regionPositions(key).size());
        store.discardRegion(key,0,0);assertNull(store.get(key,0,0));
        assertNotNull(store.get(key,64,0));assertTrue(store.version(key)>version);
    }
}
