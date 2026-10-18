package dev.rlcraftceaddons.sdtemperaturehud.temperature;

import org.junit.Test;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import static org.junit.Assert.*;

public class MovementHintCooldownTest {
    private TemperatureSnapshot snapshot(int movement) {
        return snapshot(movement, 0, 0, 0);
    }

    private TemperatureSnapshot snapshot(int movement, int other, int heat, int equipment) {
        TemperatureBreakdownAccumulator accumulator = new TemperatureBreakdownAccumulator();
        accumulator.add(TemperatureModifierCategory.ENVIRONMENT, 10);
        accumulator.add(TemperatureModifierCategory.forName("Sprint"), movement);
        accumulator.add(TemperatureModifierCategory.OTHER, other);
        accumulator.add(TemperatureModifierCategory.HEAT_SOURCE, heat);
        accumulator.add(TemperatureModifierCategory.EQUIPMENT, equipment);
        return TemperatureSnapshot.withAnalysis(12, accumulator.finish(10 + movement + other + heat + equipment));
    }

    private void update(TemperatureChangeTracker tracker, int movement, long time) {
        tracker.update(snapshot(movement), time, 2, 4000);
    }

    private void assertMovement(TemperatureChangeTracker tracker, long time, int delta) {
        List<TemperatureChangeTracker.Hint> hints = tracker.visible(time);
        assertEquals(1, hints.size());
        assertEquals(TemperatureChangeTracker.Source.MOVEMENT, hints.get(0).getSource());
        assertEquals(delta, hints.get(0).getDelta());
    }

    @Test public void repeatedStopStartCannotReopenTheHintAfterTwoSeconds() {
        TemperatureChangeTracker tracker = new TemperatureChangeTracker();
        update(tracker, 0, 0);
        update(tracker, 3, 500);
        assertMovement(tracker, 500, 3);
        update(tracker, 0, 1000);
        assertTrue(tracker.visible(1000).isEmpty());
        update(tracker, 3, 2500);
        assertTrue(tracker.visible(2500).isEmpty());
        update(tracker, 0, 4500);
        update(tracker, 3, 6500);
        assertTrue(tracker.visible(6500).isEmpty());
    }

    @Test public void bothDirectionsShareTheExactThirtySecondBoundary() {
        TemperatureChangeTracker tracker = new TemperatureChangeTracker();
        update(tracker, 0, 0);
        update(tracker, 3, 500);
        update(tracker, 0, 1000);
        update(tracker, 3, 30499);
        assertTrue(tracker.visible(30499).isEmpty());
        update(tracker, 0, 30500);
        assertMovement(tracker, 30500, -3);
        update(tracker, 3, 32500);
        assertTrue(tracker.visible(32500).isEmpty());
    }

    @Test public void visibleHintDoesNotBypassCooldownMergeOrRefreshExpiry() {
        TemperatureChangeTracker tracker = new TemperatureChangeTracker();
        update(tracker, 0, 0);
        update(tracker, 3, 500);
        update(tracker, 6, 1000);
        assertMovement(tracker, 1000, 3);
        update(tracker, 9, 2000);
        assertMovement(tracker, 2000, 3);
        assertTrue(tracker.visible(4500).isEmpty());
    }

    @Test public void suppressedChangesAreNotQueuedForTheEndOfCooldown() {
        TemperatureChangeTracker tracker = new TemperatureChangeTracker();
        update(tracker, 0, 0);
        update(tracker, 3, 500);
        update(tracker, 0, 1000);
        update(tracker, 3, 2500);
        update(tracker, 3, 30500);
        assertTrue(tracker.visible(30500).isEmpty());
        update(tracker, 3, 45000);
        assertTrue(tracker.visible(45000).isEmpty());
        update(tracker, 0, 45050);
        assertMovement(tracker, 45050, -3);
    }

    @Test public void nextAllowedHintUsesANewDeltaEvenWhenDurationIsLongerThanCooldown() {
        TemperatureChangeTracker tracker = new TemperatureChangeTracker();
        tracker.update(snapshot(0), 0, 2, 60000);
        tracker.update(snapshot(3), 500, 2, 60000);
        tracker.update(snapshot(6), 30500, 2, 60000);
        assertMovement(tracker, 30500, 3);
    }

    @Test public void otherHeatAndEquipmentAreIndependentOfMovementCooldown() {
        TemperatureChangeTracker tracker = new TemperatureChangeTracker();
        update(tracker, 0, 0);
        update(tracker, 3, 500);
        update(tracker, 0, 1000);
        tracker.update(snapshot(3, 7, 6, 0), 2500, 2, 4000);
        List<TemperatureChangeTracker.Hint> hints = tracker.visible(2500);
        assertEquals(2, hints.size());
        assertEquals(TemperatureChangeTracker.Source.OTHER, hints.get(0).getSource());
        assertEquals(7, hints.get(0).getDelta());
        assertEquals(TemperatureChangeTracker.Source.HEAT_SOURCE, hints.get(1).getSource());
        tracker.update(snapshot(3, 7, 6, -8), 3000, 2, 4000);
        assertEquals(TemperatureChangeTracker.Source.EQUIPMENT, tracker.visible(3000).get(0).getSource());
    }

    @Test public void menusHideHintsWithoutClearingTheMovementCooldown() {
        TemperatureChangeTracker tracker = new TemperatureChangeTracker();
        update(tracker, 0, 0);
        update(tracker, 3, 500);
        tracker.suspend();
        assertTrue(tracker.visible(600).isEmpty());
        update(tracker, 0, 1000);
        update(tracker, 3, 2500);
        assertTrue(tracker.visible(2500).isEmpty());
        update(tracker, 0, 30500);
        assertMovement(tracker, 30500, -3);
    }

    @Test public void unavailableDataDoesNotBypassCooldown() {
        TemperatureChangeTracker tracker = new TemperatureChangeTracker();
        update(tracker, 0, 0);
        update(tracker, 3, 500);
        tracker.update(TemperatureSnapshot.unavailable(), 1000, 2, 4000);
        update(tracker, 0, 1500);
        update(tracker, 3, 2500);
        assertTrue(tracker.visible(2500).isEmpty());
    }

    @Test public void worldResetAllowsANewBaselineAndMovementHint() {
        TemperatureChangeTracker tracker = new TemperatureChangeTracker();
        update(tracker, 0, 0);
        update(tracker, 3, 500);
        tracker.reset();
        update(tracker, 0, 1000);
        update(tracker, 3, 1500);
        assertMovement(tracker, 1500, 3);
    }

    @Test public void backwardClockResetsCooldown() {
        TemperatureChangeTracker tracker = new TemperatureChangeTracker();
        update(tracker, 0, 100000);
        update(tracker, 3, 100500);
        update(tracker, 0, 0);
        update(tracker, 3, 500);
        assertMovement(tracker, 500, 3);
    }

    @Test public void clampingResidualDoesNotProduceASecondOtherHint() {
        TemperatureChangeTracker tracker = new TemperatureChangeTracker();
        for (int movement = 0; movement <= 3; movement += 3) {
            TemperatureBreakdownAccumulator accumulator = new TemperatureBreakdownAccumulator();
            accumulator.add(TemperatureModifierCategory.ENVIRONMENT, 30);
            accumulator.add(TemperatureModifierCategory.forName("Sprint"), movement);
            tracker.update(TemperatureSnapshot.withAnalysis(25, accumulator.finish(25)), movement * 100, 2, 4000);
        }
        assertMovement(tracker, 300, 3);
    }

    @Test public void uiCallSitesSuspendButWorldChangesStillReset() throws Exception {
        String source = new String(Files.readAllBytes(Paths.get(
            "src/main/java/dev/rlcraftceaddons/sdtemperaturehud/client/ClientEventHandler.java")), StandardCharsets.UTF_8)
            .replace("\r\n", "\n");
        assertTrue(source.contains("if (this.minecraft.currentScreen != null) this.changeTracker.suspend();"));
        assertTrue(source.contains("this.warningSoundController.reset();\n            this.changeTracker.reset();"));
    }

    @Test public void bothLanguagesDescribeTheIndependentMovementCooldown() throws Exception {
        for (String language : new String[] {"zh_cn", "en_us"}) {
            String text = new String(Files.readAllBytes(Paths.get(
                "src/main/resources/assets/sdtemperaturehud/lang/" + language + ".lang")), StandardCharsets.UTF_8);
            assertTrue(text.contains("sdtemperaturehud.analysis.movement="));
            String tooltip = "";
            for (String line : text.split("\n")) {
                if (line.startsWith("sdtemperaturehud.config.change_hints.tooltip=")) tooltip = line;
            }
            assertTrue("movement cooldown must be explained in " + language, tooltip.contains("30"));
        }
    }
}
