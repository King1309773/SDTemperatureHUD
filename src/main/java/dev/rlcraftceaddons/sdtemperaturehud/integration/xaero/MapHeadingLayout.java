package dev.rlcraftceaddons.sdtemperaturehud.integration.xaero;

/** Matches GuiMap's current texture/pixel selection and integer-centered heading. */
public final class MapHeadingLayout {
    private MapHeadingLayout() {}
    public static Address at(double mouseX,double mouseZ,int level) {
        if(level<0 || level>3 || !Double.isFinite(mouseX) || !Double.isFinite(mouseZ)
            || mouseX<Integer.MIN_VALUE || mouseX>=Integer.MAX_VALUE
            || mouseZ<Integer.MIN_VALUE || mouseZ>=Integer.MAX_VALUE)
            throw new IllegalArgumentException("Unknown map texture coordinates");
        return new Address((int)Math.floor(mouseX),(int)Math.floor(mouseZ),level);
    }
    public static int suffixX(int screenWidth,int nameWidth,int gap) {
        return screenWidth/2-nameWidth/2+nameWidth+gap;
    }
    public static int rowY(boolean coordinates) {return coordinates?12:2;}
    public static boolean fits(int x,int width,int screenWidth) {
        return x>=1 && width>=0 && (long)x+width+1<screenWidth;
    }
    public static final class Address {
        public final int regionX,regionZ,textureX,textureZ,pixelX,pixelZ;
        private Address(int x,int z,int level) {
            regionX=x>>(9+level);regionZ=z>>(9+level);
            textureX=(x>>(6+level))&7;textureZ=(z>>(6+level))&7;
            pixelX=(x>>level)&63;pixelZ=(z>>level)&63;
        }
    }
}
