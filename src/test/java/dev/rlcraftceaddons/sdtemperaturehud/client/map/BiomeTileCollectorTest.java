package dev.rlcraftceaddons.sdtemperaturehud.client.map;

import dev.rlcraftceaddons.sdtemperaturehud.map.*;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class BiomeTileCollectorTest {
    @Test public void boundsWorkAndDropsOldWorldRequests() {
        BiomeTileCollector collector=new BiomeTileCollector();
        collector.enqueue(0,0);collector.enqueue(0,0);collector.enqueue(1,0);collector.enqueue(2,0);
        List<Integer> calls=new ArrayList<Integer>();
        collector.tick(2,(x,z)->{calls.add(x);return null;},tile->fail());
        assertEquals(Arrays.asList(0,1),calls);assertEquals(1,collector.pending());
        collector.reset();collector.tick(2,(x,z)->{fail();return null;},tile->fail());
        assertEquals(0,collector.pending());
    }
    @Test public void samplesOnlyDataSuppliedByLoadedChunkSource() {
        BiomeTileCollector collector=new BiomeTileCollector();collector.enqueue(-1,0);
        String[] names=new String[256];names[0]="minecraft:plains";
        List<BiomeTile> output=new ArrayList<BiomeTile>();
        collector.tick(2,(x,z)->new BiomeTile(x,z,names),output::add);
        assertEquals(-1,output.get(0).chunkX);assertNull(output.get(0).get(1,0));
    }
    @Test public void unsupportedChunkFormatsAreReportedWithoutAbortingTheTick() {
        BiomeTileCollector collector=new BiomeTileCollector();collector.enqueue(0,0);collector.enqueue(1,0);
        List<RuntimeException> failures=new ArrayList<RuntimeException>();List<BiomeTile> output=new ArrayList<BiomeTile>();
        collector.tick(2,(x,z)->{
            if(x==0)throw new UnsupportedOperationException("extended biome data");
            String[] names=new String[256];names[0]="a:b";return new BiomeTile(x,z,names);
        },output::add,failures::add);
        assertEquals(1,failures.size());assertEquals(1,output.size());assertEquals(0,collector.pending());
    }
}
