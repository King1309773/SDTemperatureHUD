package dev.rlcraftceaddons.sdtemperaturehud.integration;
import org.junit.Test;
import static org.junit.Assert.*;

public class LoadedBiomeTemperatureTest {
    @Test public void indexesTheNativePositionAndKeepsExtendedIds() {
        int[] columns=new int[256];columns[255]=1024;columns[0]=255;columns[33]=-1;
        assertEquals(1024,LoadedBiomeTemperature.biomeIdAt(columns,-1,-1));
        assertEquals(255,LoadedBiomeTemperature.biomeIdAt(columns,16,32));
        assertEquals(-1,LoadedBiomeTemperature.biomeIdAt(columns,1,2));
    }
    @Test public void missingWorldHasNoInventedTemperature() {
        assertFalse(new LoadedBiomeTemperature().at(null,null).isPresent());
    }
    @Test(expected=IllegalArgumentException.class) public void rejectsUnknownStorageLength() {
        LoadedBiomeTemperature.biomeIdAt(new int[16],0,0);
    }
}
