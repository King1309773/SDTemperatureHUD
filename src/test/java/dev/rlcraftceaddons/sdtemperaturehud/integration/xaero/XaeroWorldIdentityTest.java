package dev.rlcraftceaddons.sdtemperaturehud.integration.xaero;
import dev.rlcraftceaddons.sdtemperaturehud.map.MapWorldKey;
import org.junit.Test;
import static org.junit.Assert.*;

public class XaeroWorldIdentityTest {
    @org.junit.Test public void refreshesConfirmedSubworldsButDoesNotFlipSourcesWhileBrowsingDimensions() {
        dev.rlcraftceaddons.sdtemperaturehud.map.MapWorldKey a=new dev.rlcraftceaddons.sdtemperaturehud.map.MapWorldKey("worldmap","a",0,1,true);
        dev.rlcraftceaddons.sdtemperaturehud.map.MapWorldKey b=new dev.rlcraftceaddons.sdtemperaturehud.map.MapWorldKey("worldmap","b",0,1,true);
        dev.rlcraftceaddons.sdtemperaturehud.map.MapWorldKey fallback=new dev.rlcraftceaddons.sdtemperaturehud.map.MapWorldKey("minimap","a",0,1,true);
        assertTrue(XaeroWorldIdentity.shouldReplace(a,b));assertFalse(XaeroWorldIdentity.shouldReplace(a,a));
        assertFalse(XaeroWorldIdentity.shouldReplace(a,fallback));
        assertFalse(XaeroWorldIdentity.shouldReplace(a,new dev.rlcraftceaddons.sdtemperaturehud.map.MapWorldKey("connection","token",0,1,false)));
    }
    @Test public void unconfirmedServersUseOnlyConnectionScopedData() {
        MapWorldKey a=XaeroWorldIdentity.choose(null,null,null,0,1,"session-a");
        MapWorldKey b=XaeroWorldIdentity.choose(null,null,null,0,2,"session-b");
        assertFalse(a.persistent);assertNotEquals(a.fingerprint(),b.fingerprint());
    }
    @Test public void localAndConfirmedMapsCanPersistButNeverShareNamespaces() {
        MapWorldKey local=XaeroWorldIdentity.choose("save",null,null,0,1,"session");
        MapWorldKey map=XaeroWorldIdentity.choose(null,"worldmap","save",0,1,"session");
        assertTrue(local.persistent);assertTrue(map.persistent);assertNotEquals(local.fingerprint(),map.fingerprint());
        assertNotEquals(local,XaeroWorldIdentity.choose("save",null,null,-1,1,"session"));
        assertEquals(local.fingerprint(),XaeroWorldIdentity.choose("save",null,null,0,2,"next").fingerprint());
    }
}
