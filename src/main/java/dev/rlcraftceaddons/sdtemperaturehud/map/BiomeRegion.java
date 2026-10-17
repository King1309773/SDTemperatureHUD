package dev.rlcraftceaddons.sdtemperaturehud.map;

import java.util.*;

public final class BiomeRegion {
    public final String registryId;
    public final double x,z;
    public final int size;
    private final Set<Long> members;
    BiomeRegion(String registryId,double x,double z,Set<Long> members) {
        this.registryId=registryId;this.x=x;this.z=z;this.size=members.size();
        this.members=Collections.unmodifiableSet(new HashSet<Long>(members));
    }
    public boolean contains(int x,int z) {return members.contains(pack(x,z));}
    static long pack(int x,int z) {return ((long)x << 32) ^ (z & 0xffffffffL);}
}
