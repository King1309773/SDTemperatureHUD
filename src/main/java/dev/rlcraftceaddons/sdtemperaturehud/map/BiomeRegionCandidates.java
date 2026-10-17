package dev.rlcraftceaddons.sdtemperaturehud.map;

import java.util.*;

/** Prepared on a client tick, not repeatedly sorted/measured in the drawing callback. */
public final class BiomeRegionCandidates {
    private BiomeRegionCandidates() {}
    public static List<BiomeRegion> nearest(List<BiomeRegion> input,double x,double z,int limit) {
        List<BiomeRegion> sorted=new ArrayList<BiomeRegion>(input);
        sorted.sort(Comparator.comparing((BiomeRegion region)->!region.contains((int)Math.floor(x),(int)Math.floor(z)))
            .thenComparingDouble(region->(region.x-x)*(region.x-x)+(region.z-z)*(region.z-z))
            .thenComparing(region->region.registryId).thenComparingDouble(region->region.x).thenComparingDouble(region->region.z));
        return Collections.unmodifiableList(new ArrayList<BiomeRegion>(sorted.subList(0,Math.min(sorted.size(),Math.max(0,Math.min(256,limit))))));
    }
}
