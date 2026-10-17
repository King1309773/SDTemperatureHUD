package dev.rlcraftceaddons.sdtemperaturehud.map;

import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class BiomeRegionBuilderTest {
    @Test public void groupsOnlyFourConnectedKnownCells() {
        String[] cells=new String[256];cells[0]="a:one";cells[1]="a:one";cells[17]="a:one";
        cells[34]="a:one";cells[3]="b:one";
        List<BiomeRegion> regions=BiomeRegionBuilder.build(Collections.singletonList(new BiomeTile(0,0,cells)));
        assertEquals(3,regions.size());
        assertEquals(3,regions.get(0).size);assertTrue(regions.get(0).contains(1,1));
        assertFalse(regions.get(0).contains(2,2));
        for(BiomeRegion region:regions) assertTrue(region.contains((int)Math.floor(region.x),(int)Math.floor(region.z)));
    }
    @Test public void connectsAcrossNegativeChunkBoundaries() {
        String[] a=new String[256],b=new String[256];a[15]="a:one";b[0]="a:one";
        List<BiomeRegion> regions=BiomeRegionBuilder.build(Arrays.asList(new BiomeTile(-1,0,a),new BiomeTile(0,0,b)));
        assertEquals(1,regions.size());assertEquals(2,regions.get(0).size);
    }
    @Test public void largeUniformInputUsesNoRecursiveFloodFill() {
        List<BiomeTile> tiles=new ArrayList<BiomeTile>();String[] names=new String[256];Arrays.fill(names,"a:one");
        for(int x=0;x<8;x++)for(int z=0;z<8;z++)tiles.add(new BiomeTile(x,z,names));
        assertEquals(16384,BiomeRegionBuilder.build(tiles).get(0).size);
    }
}
