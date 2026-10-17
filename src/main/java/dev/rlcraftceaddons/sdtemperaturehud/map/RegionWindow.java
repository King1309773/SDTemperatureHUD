package dev.rlcraftceaddons.sdtemperaturehud.map;

/** A viewport is capped to 32 chunks per side before any region query is performed. */
public final class RegionWindow {
    private RegionWindow() {}
    public static boolean contains(int rx,int rz,int centerX,int centerZ,int requestedRadius) {
        int radius=Math.max(1,Math.min(32,requestedRadius));
        long minX=Math.floorDiv((long)centerX-radius,32),maxX=Math.floorDiv((long)centerX+radius,32);
        long minZ=Math.floorDiv((long)centerZ-radius,32),maxZ=Math.floorDiv((long)centerZ+radius,32);
        return rx>=minX && rx<=maxX && rz>=minZ && rz<=maxZ;
    }
}
