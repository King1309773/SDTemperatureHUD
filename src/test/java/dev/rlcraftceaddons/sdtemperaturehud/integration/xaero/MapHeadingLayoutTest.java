package dev.rlcraftceaddons.sdtemperaturehud.integration.xaero;

import org.junit.Test;
import static org.junit.Assert.*;

public class MapHeadingLayoutTest {
    @Test public void negativeCoordinatesMatchNativeRegionTextureAndPixelMasks() {
        MapHeadingLayout.Address a=MapHeadingLayout.at(-.5,-64.1,0);
        assertEquals(-1,a.regionX);assertEquals(-1,a.regionZ);
        assertEquals(7,a.textureX);assertEquals(6,a.textureZ);
        assertEquals(63,a.pixelX);assertEquals(63,a.pixelZ);
    }
    @Test public void eachZoomLevelUsesTheSameAlignedPixelAsTheNativeHeading() {
        for(int level=0;level<=3;level++)for(int block:new int[]{-4097,-513,-1,0,63,511,4097}) {
            MapHeadingLayout.Address a=MapHeadingLayout.at(block+.75,block+.25,level);
            int aligned=block>>level;
            assertEquals(aligned>>9,a.regionX);assertEquals(aligned>>9,a.regionZ);
            assertEquals((aligned>>6)&7,a.textureX);assertEquals((aligned>>6)&7,a.textureZ);
            assertEquals(aligned&63,a.pixelX);assertEquals(aligned&63,a.pixelZ);
        }
    }
    @Test public void appendsAfterNativeIntegerCenteredNameAndTracksCoordinateRow() {
        assertEquals(104,MapHeadingLayout.suffixX(200,0,4));
        assertEquals(114,MapHeadingLayout.suffixX(200,20,4));
        assertEquals(115,MapHeadingLayout.suffixX(201,21,4));
        assertEquals(12,MapHeadingLayout.rowY(true));assertEquals(2,MapHeadingLayout.rowY(false));
        assertTrue(MapHeadingLayout.fits(114,12,200));assertFalse(MapHeadingLayout.fits(195,12,200));
    }
    @Test(expected=IllegalArgumentException.class) public void rejectsUnknownTextureLevel() {MapHeadingLayout.at(0,0,4);}
    @Test(expected=IllegalArgumentException.class) public void rejectsNonFiniteMouseCoordinates() {MapHeadingLayout.at(Double.NaN,0,0);}
}
