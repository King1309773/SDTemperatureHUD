package dev.rlcraftceaddons.sdtemperaturehud.map;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class BiomeRegionCandidatesTest {
    @Test public void boundsPerFrameWorkAndKeepsTheCurrentRegionFirst() {
        String[] names=new String[256];
        for(int z=0;z<16;z++)for(int x=0;x<16;x++)names[z*16+x]=(x+z)%2==0?"a:one":"a:two";
        List<BiomeRegion> regions=BiomeRegionBuilder.build(Collections.singletonList(new BiomeTile(0,0,names)));
        List<BiomeRegion> selected=BiomeRegionCandidates.nearest(regions,15.5,15.5,64);
        assertEquals(64,selected.size());assertTrue(selected.get(0).contains(15,15));
        assertEquals(256,regions.size());
        assertTrue(BiomeRegionCandidates.nearest(regions,0,0,0).isEmpty());
    }
}
