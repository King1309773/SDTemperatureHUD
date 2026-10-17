package dev.rlcraftceaddons.sdtemperaturehud.map;

import java.util.*;

public final class BiomeRegionBuilder {
    private BiomeRegionBuilder() {}
    public static List<BiomeRegion> build(List<BiomeTile> tiles) {
        if(tiles.size()>256) throw new IllegalArgumentException("region work exceeds tile budget");
        Map<Long,String> remaining=new LinkedHashMap<Long,String>();
        for(BiomeTile tile:tiles) for(int z=0;z<16;z++) for(int x=0;x<16;x++) {
            String name=tile.get(x,z);
            if(name!=null) remaining.put(BiomeRegion.pack(tile.chunkX*16+x,tile.chunkZ*16+z),name);
        }
        List<BiomeRegion> result=new ArrayList<BiomeRegion>();
        while(!remaining.isEmpty()) {
            long first=remaining.keySet().iterator().next();String name=remaining.remove(first);
            Set<Long> members=new LinkedHashSet<Long>();Queue<Long> queue=new ArrayDeque<Long>();queue.add(first);
            long sumX=0,sumZ=0;
            while(!queue.isEmpty()) {
                long position=queue.remove();members.add(position);
                int x=(int)(position >> 32),z=(int)position;sumX+=x;sumZ+=z;
                visit(remaining,queue,name,x-1,z);visit(remaining,queue,name,x+1,z);
                visit(remaining,queue,name,x,z-1);visit(remaining,queue,name,x,z+1);
            }
            double centerX=(double)sumX/members.size(),centerZ=(double)sumZ/members.size();
            long best=first;double bestDistance=Double.POSITIVE_INFINITY;
            for(long member:members) {
                int x=(int)(member >> 32),z=(int)member;
                double distance=(x-centerX)*(x-centerX)+(z-centerZ)*(z-centerZ);
                int bestX=(int)(best >> 32),bestZ=(int)best;
                if(distance<bestDistance || (distance==bestDistance && (x<bestX || x==bestX && z<bestZ))) {
                    best=member;bestDistance=distance;
                }
            }
            result.add(new BiomeRegion(name,(int)(best >> 32)+.5,(int)best+.5,members));
        }
        return Collections.unmodifiableList(result);
    }
    private static void visit(Map<Long,String> cells,Queue<Long> queue,String name,int x,int z) {
        long position=BiomeRegion.pack(x,z);
        if(name.equals(cells.get(position))) {cells.remove(position);queue.add(position);}
    }
}
