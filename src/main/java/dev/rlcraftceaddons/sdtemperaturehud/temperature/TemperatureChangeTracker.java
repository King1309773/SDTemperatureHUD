package dev.rlcraftceaddons.sdtemperaturehud.temperature;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;

/** Tracks changes in modifier contributions, not changes in body temperature. */
public final class TemperatureChangeTracker {
    private static final long MOVEMENT_COOLDOWN_MS = 30_000L;
    private static final long DEFAULT_COOLDOWN_MS = 2_000L;
    public enum Source {
        ENVIRONMENT("environment"), EQUIPMENT("equipment"), SEASON("season"),
        HEAT_SOURCE("heat_source"), MOVEMENT("movement"), OTHER("other");

        private final String key;
        Source(String key) { this.key = "sdtemperaturehud.analysis." + key; }
        public String getLanguageKey() { return key; }
        int read(TemperatureBreakdown value) {
            switch (this) {
                case ENVIRONMENT: return value.getEnvironment();
                case EQUIPMENT: return value.getEquipment();
                case SEASON: return value.getSeason();
                case HEAT_SOURCE: return value.getHeatSource();
                case MOVEMENT: return value.getMovementForHints();
                default: return value.getOtherForHints();
            }
        }
    }

    public static final class Hint {
        private final Source source;
        private final int delta;
        private final long expiresAt;
        private Hint(Source source, int delta, long expiresAt) {
            this.source = source; this.delta = delta; this.expiresAt = expiresAt;
        }
        public Source getSource() { return source; }
        public int getDelta() { return delta; }
    }

    private final int[] baseline = new int[Source.values().length];
    private final long[] nextAllowed = new long[Source.values().length];
    private final EnumMap<Source, Hint> hints = new EnumMap<Source, Hint>(Source.class);
    private boolean initialized;
    private long lastTime;

    public void reset() {
        initialized = false;
        hints.clear();
        java.util.Arrays.fill(nextAllowed, 0L);
    }

    /** Hide transient hints without letting UI visibility bypass the movement cooldown. */
    public void suspend() {
        initialized = false;
        hints.clear();
        for (Source source : Source.values()) {
            if (source != Source.MOVEMENT) nextAllowed[source.ordinal()] = 0L;
        }
    }

    public void update(TemperatureSnapshot snapshot, long now, int threshold, int durationMs) {
        if (now < lastTime) reset();
        lastTime = now;
        if (snapshot == null || !snapshot.isAvailable() || !snapshot.hasBreakdown()) {
            suspend();
            return;
        }
        expire(now);
        TemperatureBreakdown breakdown = snapshot.getBreakdown();
        if (!initialized) {
            for (Source source : Source.values()) baseline[source.ordinal()] = source.read(breakdown);
            initialized = true;
            return;
        }
        int minimum = Math.max(1, threshold);
        for (Source source : Source.values()) {
            int index = source.ordinal();
            int value = source.read(breakdown);
            long difference = (long)value - baseline[index];
            Hint previous = hints.get(source);
            if (source == Source.MOVEMENT && now < nextAllowed[index]) {
                // Observe suppressed transitions, but never queue, merge or refresh a movement hint.
                baseline[index] = value;
                if (previous != null && difference != 0L
                    && Long.signum(difference) != Integer.signum(previous.delta)) {
                    hints.remove(source);
                }
                continue;
            }
            if (Math.abs(difference) < minimum) continue;
            if (previous == null && now < nextAllowed[index]) continue;
            long combined = difference
                + (source == Source.MOVEMENT || previous == null ? 0L : previous.delta);
            baseline[index] = value;
            if (Math.abs(combined) < minimum) {
                hints.remove(source);
                continue;
            }
            int delta = (int)Math.max(Integer.MIN_VALUE, Math.min(Integer.MAX_VALUE, combined));
            hints.put(source, new Hint(source, delta, now + Math.max(500, durationMs)));
            nextAllowed[index] = now + (source == Source.MOVEMENT
                ? MOVEMENT_COOLDOWN_MS : DEFAULT_COOLDOWN_MS);
        }
    }

    public List<Hint> visible(long now) {
        expire(now);
        List<Hint> result = new ArrayList<Hint>(hints.values());
        Collections.sort(result, new Comparator<Hint>() {
            @Override public int compare(Hint a, Hint b) {
                return Long.compare(Math.abs((long)b.delta), Math.abs((long)a.delta));
            }
        });
        if (result.size() > 2) result = new ArrayList<Hint>(result.subList(0, 2));
        return Collections.unmodifiableList(result);
    }

    private void expire(long now) {
        java.util.Iterator<Hint> iterator = hints.values().iterator();
        while (iterator.hasNext()) if (now >= iterator.next().expiresAt) iterator.remove();
    }
}

