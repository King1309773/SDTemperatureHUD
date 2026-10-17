package dev.rlcraftceaddons.sdtemperaturehud.client.map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import static org.junit.Assert.*;

public class RegionRequestsTest {
    @Test public void fastCompletionCannotLeaveAStuckReservation() {
        RegionRequests requests=new RegionRequests(Runnable::run);AtomicInteger calls=new AtomicInteger();
        assertTrue(requests.submit("one",()->{calls.incrementAndGet();requests.forget("one");}));
        assertTrue(requests.submit("one",calls::incrementAndGet));assertEquals(2,calls.get());
        assertFalse(requests.submit("one",calls::incrementAndGet));
    }
    @Test public void rejectedWorkCanBeRetried() {
        AtomicInteger attempts=new AtomicInteger();
        RegionRequests requests=new RegionRequests(task->{if(attempts.getAndIncrement()==0)throw new RejectedExecutionException();task.run();});
        assertFalse(requests.submit("one",()->{}));assertTrue(requests.submit("one",()->{}));
    }
    @Test public void leavingTheWorkingSetAlsoDiscardsNegativeCacheReservations() {
        RegionRequests requests=new RegionRequests(Runnable::run);
        requests.submit("one",()->{});requests.submit("two",()->{});
        requests.retain(id->id.equals("one"));
        assertFalse(requests.submit("one",()->{}));assertTrue(requests.submit("two",()->{}));
    }
}
