package dev.rlcraftceaddons.sdtemperaturehud.integration.xaero;

/** Xaero over-map/world-map transforms, kept independent of the optional classes. */
public final class MapProjection {
    private MapProjection() {}
    public static double[] minimap(double dx,double dz,double sin,double cos,double zoom) {
        return new double[]{(sin*dx-cos*dz)*zoom,(cos*dx+sin*dz)*zoom};
    }
    public static double[] world(double dx,double dz,double scale) {
        return new double[]{dx*scale,dz*scale};
    }
}
