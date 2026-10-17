package dev.rlcraftceaddons.sdtemperaturehud.client.map;

import dev.rlcraftceaddons.sdtemperaturehud.map.*;
import java.io.IOException;
import java.nio.file.*;
import java.util.Collections;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.*;

public class BiomeCacheIOTest {
    @Rule public TemporaryFolder temp=new TemporaryFolder();
    private MapWorldKey key(boolean persistent) {return new MapWorldKey("test","../../outside",0,1,persistent);}
    private BiomeTile tile(String name) {String[] cells=new String[256];cells[0]=name;return new BiomeTile(0,0,cells);}
    @Test public void pathsAreHashedAndFilesRoundTrip() throws Exception {
        BiomeCacheIO io=new BiomeCacheIO(temp.getRoot().toPath());
        assertTrue(io.fileFor(key(true),0,0).startsWith(temp.getRoot().toPath()));
        assertFalse(io.fileFor(key(true),0,0).toString().contains("outside"));
        io.saveNow(key(true),0,0,Collections.singletonList(tile("a:b")));
        assertEquals(tile("a:b"),io.loadNow(key(true),0,0).get(0));
        assertTrue(io.loadNow(key(true),1,0).isEmpty());
    }
    @Test public void failedAtomicMovePreservesPreviouslySavedFile() throws Exception {
        BiomeCacheIO io=new BiomeCacheIO(temp.getRoot().toPath());
        io.saveNow(key(true),0,0,Collections.singletonList(tile("a:b")));
        byte[] original=Files.readAllBytes(io.fileFor(key(true),0,0));
        BiomeCacheIO broken=new BiomeCacheIO(temp.getRoot().toPath(),(source,target)->{
            throw new IOException("simulated move failure");
        });
        try {broken.saveNow(key(true),0,0,Collections.singletonList(tile("a:c")));fail();}
        catch(IOException expected) {assertArrayEquals(original,Files.readAllBytes(io.fileFor(key(true),0,0)));}
    }
    @Test public void transientWorldNeverTouchesDisk() throws Exception {
        BiomeCacheIO io=new BiomeCacheIO(temp.getRoot().toPath());
        assertTrue(io.loadNow(key(false),0,0).isEmpty());
        try {io.saveNow(key(false),0,0,Collections.singletonList(tile("a:b")));fail();}
        catch(IOException expected) {assertEquals(0,temp.getRoot().list().length);}
    }
    @Test public void incrementalSavePreservesOtherPreviouslyKnownChunks() throws Exception {
        BiomeCacheIO io=new BiomeCacheIO(temp.getRoot().toPath());
        io.saveNow(key(true),0,0,Collections.singletonList(tile("a:b")));
        String[] cells=new String[256];cells[0]="a:c";
        io.saveNow(key(true),0,0,Collections.singletonList(new BiomeTile(1,0,cells)));
        assertEquals(2,io.loadNow(key(true),0,0).size());
    }
}
