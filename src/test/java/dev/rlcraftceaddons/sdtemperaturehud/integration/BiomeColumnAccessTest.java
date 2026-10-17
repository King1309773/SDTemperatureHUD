package dev.rlcraftceaddons.sdtemperaturehud.integration;
import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.*;

public class BiomeColumnAccessTest {
    public interface Legacy {int[] getIntBiomeArray();}
    public interface Modern {int[] getBiomes();}
    @Test public void vanillaUnsignedBytesHaveAnUnknown255Sentinel() {
        byte[] input=new byte[256];input[0]=(byte)200;input[1]=(byte)255;
        int[] output=BiomeColumnAccess.copyBytes(input);
        assertEquals(200,output[0]);assertEquals(-1,output[1]);
    }
    @Test public void extendedIdsIncluding255AreNeverTruncated() {
        int[] input=new int[256];Arrays.fill(input,-1);input[0]=1024;input[1]=255;
        int[] output=BiomeColumnAccess.copyInts(input);input[0]=2;
        assertEquals(1024,output[0]);assertEquals(255,output[1]);assertEquals(-1,output[2]);
    }
    @Test public void bothVerifiedPublicStorageContractsReturnCopiedColumns() throws Exception {
        int[] values=new int[256];values[0]=4096;
        Legacy old=()->values;Modern modern=()->values;
        assertEquals(4096,BiomeColumnAccess.readPublic(old,Legacy.class,"getIntBiomeArray")[0]);
        assertEquals(4096,BiomeColumnAccess.readPublic(modern,Modern.class,"getBiomes")[0]);
        assertNotSame(values,BiomeColumnAccess.readPublic(old,Legacy.class,"getIntBiomeArray"));
    }
    @Test(expected=IllegalArgumentException.class) public void rejectsUnknownColumnLength() {
        BiomeColumnAccess.copyInts(new int[16]);
    }
}
