package dev.rlcraftceaddons.sdtemperaturehud.map;
import org.junit.Test;
import static org.junit.Assert.*;

public class RegionWindowTest {
    @Test public void includesOnlyRegionsIntersectingTheBoundedChunkViewport() {
        assertTrue(RegionWindow.contains(0,0,0,0,16));
        assertTrue(RegionWindow.contains(-1,-1,0,0,16));
        assertFalse(RegionWindow.contains(1,0,0,0,16));
        assertTrue(RegionWindow.contains(-2,0,-33,0,1));
        assertFalse(RegionWindow.contains(1000,0,0,0,Integer.MAX_VALUE));
    }
    @Test public void regionEvictionKeepsTheWorkingSetBoundedDuringLongTravel() {
        MapWorldKey key=new MapWorldKey("test","world",0,1,false);BiomeTileStore store=new BiomeTileStore();
        for(int step=0;step<500;step++) {
            int chunk=step*32;String[] cells=new String[256];cells[0]="a:b";
            store.put(key,new BiomeTile(chunk,0,cells));
            for(long region:store.regionPositions(key)) {
                int rx=(int)(region>>32),rz=(int)region;
                if(!RegionWindow.contains(rx,rz,chunk,0,1))store.discardRegion(key,rx,rz);
            }
            assertTrue(store.regionPositions(key).size()<=2);
        }
    }
}
