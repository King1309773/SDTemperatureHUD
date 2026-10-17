package dev.rlcraftceaddons.sdtemperaturehud.temperature;

import org.junit.Test;
import static org.junit.Assert.*;

public class TemperatureChangeTrackerTest {
    private TemperatureSnapshot snapshot(int heat) {
        return TemperatureSnapshot.withAnalysis(12,
            new TemperatureBreakdown(10, 0, 0, heat, 0, 10 + heat));
    }

    @Test public void initialSampleAndOnePointNoiseAreSilentButCumulativeChangeCounts() {
        TemperatureChangeTracker tracker = new TemperatureChangeTracker();
        tracker.update(snapshot(0), 0, 2, 4000);
        assertTrue(tracker.visible(0).isEmpty());
        tracker.update(snapshot(1), 500, 2, 4000);
        assertTrue(tracker.visible(500).isEmpty());
        tracker.update(snapshot(2), 1000, 2, 4000);
        assertEquals(1, tracker.visible(1000).size());
        assertEquals(2, tracker.visible(1000).get(0).getDelta());
    }

    @Test public void sameSourceMergesInsteadOfAddingRowsAndExpires() {
        TemperatureChangeTracker tracker = new TemperatureChangeTracker();
        tracker.update(snapshot(0), 0, 2, 4000);
        tracker.update(snapshot(6), 500, 2, 4000);
        tracker.update(snapshot(8), 1000, 2, 4000);
        assertEquals(1, tracker.visible(1000).size());
        assertEquals(8, tracker.visible(1000).get(0).getDelta());
        assertTrue(tracker.visible(5001).isEmpty());
    }

    @Test public void lostDataAndResetRequireNewBaseline() {
        TemperatureChangeTracker tracker = new TemperatureChangeTracker();
        tracker.update(snapshot(0), 0, 2, 4000);
        tracker.update(snapshot(6), 500, 2, 4000);
        tracker.update(TemperatureSnapshot.unavailable(), 1000, 2, 4000);
        tracker.update(snapshot(15), 1500, 2, 4000);
        assertTrue(tracker.visible(1500).isEmpty());
        tracker.reset();
        tracker.update(snapshot(0), 2000, 2, 4000);
        assertTrue(tracker.visible(2000).isEmpty());
    }

    @Test public void keepsOnlyTwoStrongestSimultaneousChanges() {
        TemperatureChangeTracker tracker = new TemperatureChangeTracker();
        tracker.update(snapshot(0), 0, 2, 4000);
        tracker.update(TemperatureSnapshot.withAnalysis(12,
            new TemperatureBreakdown(14, -7, 3, 8, 2, 20)), 500, 2, 4000);
        assertEquals(2, tracker.visible(500).size());
        assertEquals(TemperatureChangeTracker.Source.HEAT_SOURCE, tracker.visible(500).get(0).getSource());
        assertEquals(TemperatureChangeTracker.Source.EQUIPMENT, tracker.visible(500).get(1).getSource());
    }

    @Test public void reversalDuringVisibleHintCancelsTheHint() {
        TemperatureChangeTracker tracker = new TemperatureChangeTracker();
        tracker.update(snapshot(0), 0, 2, 4000);
        tracker.update(snapshot(6), 500, 2, 4000);
        tracker.update(snapshot(0), 1000, 2, 4000);
        assertTrue(tracker.visible(1000).isEmpty());
    }
}

