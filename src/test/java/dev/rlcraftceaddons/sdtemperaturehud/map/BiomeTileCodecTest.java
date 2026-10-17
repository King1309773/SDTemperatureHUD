package dev.rlcraftceaddons.sdtemperaturehud.map;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;
import static org.junit.Assert.*;

public class BiomeTileCodecTest {
    private final MapWorldKey key=BiomeTileStoreTest.key(0,1);
    @Test public void roundTripsRegistryNamesAndUnknownCellsAcrossConnections() throws Exception {
        BiomeTile tile=BiomeTileStoreTest.tile(-1,-33,"example:one");
        byte[] bytes=BiomeTileCodec.encode(key,-1,-2,Collections.singletonList(tile));
        assertEquals(tile,BiomeTileCodec.decode(bytes,BiomeTileStoreTest.key(0,2),-1,-2).get(0));
        assertNull(BiomeTileCodec.decode(bytes,key,-1,-2).get(0).get(1,0));
    }
    @Test(expected=IOException.class) public void rejectsTruncatedInput() throws Exception {
        byte[] bytes=BiomeTileCodec.encode(key,0,0,Collections.singletonList(BiomeTileStoreTest.tile(0,0,"a:b")));
        BiomeTileCodec.decode(Arrays.copyOf(bytes,bytes.length-1),key,0,0);
    }
    @Test(expected=IOException.class) public void rejectsForeignWorld() throws Exception {
        byte[] bytes=BiomeTileCodec.encode(key,0,0,Collections.<BiomeTile>emptyList());
        BiomeTileCodec.decode(bytes,new MapWorldKey("test","foreign",0,1,true),0,0);
    }
    @Test(expected=IOException.class) public void rejectsWrongFormatVersion() throws Exception {
        byte[] bytes=BiomeTileCodec.encode(key,0,0,Collections.<BiomeTile>emptyList());
        bytes[7]=9; BiomeTileCodec.decode(bytes,key,0,0);
    }
    @Test(expected=IOException.class) public void rejectsTileFromAnotherRegion() throws Exception {
        BiomeTileCodec.encode(key,0,0,Collections.singletonList(BiomeTileStoreTest.tile(-1,0,"a:b")));
    }
    @Test(expected=IOException.class) public void rejectsOversizeBeforeAllocatingTiles() throws Exception {
        BiomeTileCodec.decode(new byte[8*1024*1024+1],key,0,0);
    }
    @Test(expected=IOException.class) public void rejectsDuplicateTiles() throws Exception {
        BiomeTile tile=BiomeTileStoreTest.tile(0,0,"a:b");
        BiomeTileCodec.encode(key,0,0,Arrays.asList(tile,tile));
    }
}
