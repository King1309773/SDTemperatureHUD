package dev.rlcraftceaddons.sdtemperaturehud.integration;

import dev.rlcraftceaddons.sdtemperaturehud.temperature.TemperatureSnapshot;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;

public class SimpleDifficultyTemperatureSourceTest {
    @Test
    public void expectedDataAbsenceBecomesUnavailableAndIsLoggedWithContext() {
        RecordingWarnings warnings = new RecordingWarnings();
        MutableClock clock = new MutableClock();
        SimpleDifficultyTemperatureSource source = source(
            sequence(SimpleDifficultyTemperatureSource.DataUnavailableException.missing(
                "temperature capability is absent"
            )),
            clock,
            warnings
        );

        TemperatureSnapshot snapshot = source.read();

        assertFalse(snapshot.isAvailable());
        assertEquals(1, warnings.count);
        assertEquals("temperature capability is absent", warnings.lastContext);
    }

    @Test(expected = IllegalArgumentException.class)
    public void unexpectedProgrammingExceptionIsNotSwallowed() {
        SimpleDifficultyTemperatureSource source = source(
            sequence(new IllegalArgumentException("broken modifier")),
            new MutableClock(),
            new RecordingWarnings()
        );

        source.read();
    }

    @Test
    public void warningsAreRateLimitedUntilIntervalExpires() {
        RecordingWarnings warnings = new RecordingWarnings();
        MutableClock clock = new MutableClock();
        SimpleDifficultyTemperatureSource source = source(
            repeatingFailure("registry unavailable"),
            clock,
            warnings
        );

        source.read();
        clock.now = 29_999L;
        source.read();
        assertEquals(1, warnings.count);

        clock.now = 30_000L;
        source.read();
        assertEquals(2, warnings.count);
    }

    @Test
    public void aSuccessfulPollRestoresValidDataAndResetsSuppression() {
        RecordingWarnings warnings = new RecordingWarnings();
        MutableClock clock = new MutableClock();
        TemperatureSnapshot valid = TemperatureSnapshot.withTarget(11, 12);
        SimpleDifficultyTemperatureSource source = source(
            sequence(
                SimpleDifficultyTemperatureSource.DataUnavailableException.missing("first"),
                valid,
                SimpleDifficultyTemperatureSource.DataUnavailableException.missing("again")
            ),
            clock,
            warnings
        );

        assertFalse(source.read().isAvailable());
        clock.now = 1L;
        assertSame(valid, source.read());
        clock.now = 2L;
        assertFalse(source.read().isAvailable());
        assertEquals(2, warnings.count);
    }

    @Test
    public void concurrentFailuresLogOnceAndRecoveryAllowsTheNextFailureToLog()
        throws Exception {
        int readers = 8;
        ConcurrentReader reader = new ConcurrentReader(readers);
        final AtomicInteger warningCount = new AtomicInteger();
        SimpleDifficultyTemperatureSource source = source(
            reader,
            new MutableClock(),
            new SimpleDifficultyTemperatureSource.WarningSink() {
                @Override
                public void warn(String context, Throwable cause) {
                    warningCount.incrementAndGet();
                }
            }
        );

        runConcurrentReads(source, reader, readers);
        assertEquals(1, warningCount.get());

        reader.recover();
        assertSame(reader.valid, source.read());

        reader.failTogether(readers);
        runConcurrentReads(source, reader, readers);
        assertEquals(2, warningCount.get());
    }

    private static SimpleDifficultyTemperatureSource source(
        SimpleDifficultyTemperatureSource.SnapshotReader reader,
        SimpleDifficultyTemperatureSource.Clock clock,
        SimpleDifficultyTemperatureSource.WarningSink warnings
    ) {
        return new SimpleDifficultyTemperatureSource(reader, clock, warnings, 30_000L);
    }

    private static SimpleDifficultyTemperatureSource.SnapshotReader sequence(Object... results) {
        final Queue<Object> remaining = new ArrayDeque<Object>();
        for (Object result : results) {
            remaining.add(result);
        }
        return new SimpleDifficultyTemperatureSource.SnapshotReader() {
            @Override
            public TemperatureSnapshot read()
                throws SimpleDifficultyTemperatureSource.DataUnavailableException {
                Object next = remaining.remove();
                if (next instanceof SimpleDifficultyTemperatureSource.DataUnavailableException) {
                    throw (SimpleDifficultyTemperatureSource.DataUnavailableException)next;
                }
                if (next instanceof RuntimeException) {
                    throw (RuntimeException)next;
                }
                return (TemperatureSnapshot)next;
            }
        };
    }

    private static SimpleDifficultyTemperatureSource.SnapshotReader repeatingFailure(
        final String context
    ) {
        return new SimpleDifficultyTemperatureSource.SnapshotReader() {
            @Override
            public TemperatureSnapshot read()
                throws SimpleDifficultyTemperatureSource.DataUnavailableException {
                throw SimpleDifficultyTemperatureSource.DataUnavailableException.missing(context);
            }
        };
    }

    private static final class MutableClock implements SimpleDifficultyTemperatureSource.Clock {
        private long now;

        @Override
        public long now() {
            return this.now;
        }
    }

    private static final class RecordingWarnings
        implements SimpleDifficultyTemperatureSource.WarningSink {
        private int count;
        private String lastContext;

        @Override
        public void warn(String context, Throwable cause) {
            ++this.count;
            this.lastContext = context;
        }
    }

    private static void runConcurrentReads(
        final SimpleDifficultyTemperatureSource source,
        ConcurrentReader reader,
        int readers
    ) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(readers);
        try {
            Future<?>[] futures = new Future<?>[readers];
            for (int index = 0; index < readers; ++index) {
                futures[index] = executor.submit(new Runnable() {
                    @Override
                    public void run() {
                        assertFalse(source.read().isAvailable());
                    }
                });
            }
            reader.releaseWhenAllReadersArrive();
            for (Future<?> future : futures) {
                future.get(5L, TimeUnit.SECONDS);
            }
        } finally {
            executor.shutdownNow();
            executor.awaitTermination(5L, TimeUnit.SECONDS);
        }
    }

    private static final class ConcurrentReader
        implements SimpleDifficultyTemperatureSource.SnapshotReader {
        private final TemperatureSnapshot valid = TemperatureSnapshot.withTarget(11, 12);
        private volatile FailurePhase failure;

        private ConcurrentReader(int readers) {
            this.failTogether(readers);
        }

        private void failTogether(int readers) {
            this.failure = new FailurePhase(readers);
        }

        private void recover() {
            this.failure = null;
        }

        private void releaseWhenAllReadersArrive() throws InterruptedException {
            FailurePhase current = this.failure;
            if (!current.arrived.await(5L, TimeUnit.SECONDS)) {
                throw new AssertionError("concurrent readers did not arrive");
            }
            current.release.countDown();
        }

        @Override
        public TemperatureSnapshot read()
            throws SimpleDifficultyTemperatureSource.DataUnavailableException {
            FailurePhase current = this.failure;
            if (current == null) {
                return this.valid;
            }
            current.arrived.countDown();
            try {
                if (!current.release.await(5L, TimeUnit.SECONDS)) {
                    throw new AssertionError("concurrent failure phase was not released");
                }
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new AssertionError("concurrent failure phase interrupted", exception);
            }
            throw SimpleDifficultyTemperatureSource.DataUnavailableException.missing(
                "simultaneous failure"
            );
        }
    }

    private static final class FailurePhase {
        private final CountDownLatch arrived;
        private final CountDownLatch release = new CountDownLatch(1);

        private FailurePhase(int readers) {
            this.arrived = new CountDownLatch(readers);
        }
    }
}
