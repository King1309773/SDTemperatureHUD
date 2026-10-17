package dev.rlcraftceaddons.sdtemperaturehud.client.map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import static org.junit.Assert.*;

public class FrameStateCacheTest {
    @Test public void capturesOncePerFrameRatherThanOncePerLabel() {
        FrameStateCache<Object> cache=new FrameStateCache<Object>();AtomicInteger calls=new AtomicInteger();
        Object first=cache.get(()->{calls.incrementAndGet();return new Object();});
        for(int i=0;i<40;i++)assertSame(first,cache.get(()->{calls.incrementAndGet();return new Object();}));
        assertEquals(1,calls.get());cache.beginFrame();
        assertNotSame(first,cache.get(()->{calls.incrementAndGet();return new Object();}));assertEquals(2,calls.get());
    }
}
