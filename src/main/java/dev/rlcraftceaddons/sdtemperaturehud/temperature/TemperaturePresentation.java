/*
 * Decompiled with CFR 0.152.
 */
package dev.rlcraftceaddons.sdtemperaturehud.temperature;

import dev.rlcraftceaddons.sdtemperaturehud.client.theme.HudColorPalette;

public final class TemperaturePresentation {
    private TemperaturePresentation() {
    }

    public static SnapshotPresentation from(TemperatureSnapshot snapshot) {
        if (snapshot == null) {
            throw new IllegalArgumentException("snapshot must not be null");
        }
        return new SnapshotPresentation(
            snapshot.isAvailable() ? Integer.toString(snapshot.getBodyTemperature()) : "--"
        );
    }

    public static int colorFor(int temperature, HudColorPalette palette) {
        if (temperature <= 5) {
            return palette.getSevereCold();
        }
        if (temperature <= 10) {
            return palette.getCold();
        }
        if (temperature <= 14) {
            return palette.getComfortable();
        }
        if (temperature <= 19) {
            return palette.getWarm();
        }
        return palette.getSevereHot();
    }

    public static Trend trendFor(int target, int body) {
        if (target > body) {
            return Trend.UP;
        }
        if (target < body) {
            return Trend.DOWN;
        }
        return Trend.STEADY;
    }

    public static int trendColor(Trend trend, HudColorPalette palette) {
        switch (trend) {
            case UP: {
                return palette.getRising();
            }
            case DOWN: {
                return palette.getFalling();
            }
            case STEADY: {
                return palette.getStable();
            }
        }
        throw new IllegalArgumentException("Unsupported trend: " + (Object)((Object)trend));
    }

    public static String glyphFor(Trend trend) {
        switch (trend) {
            case UP: {
                return "\u2191";
            }
            case DOWN: {
                return "\u2193";
            }
            case STEADY: {
                return "\u2192";
            }
        }
        throw new IllegalArgumentException("Unsupported trend: " + (Object)((Object)trend));
    }

    public static enum Trend {
        UP,
        DOWN,
        STEADY;

    }

    public static final class SnapshotPresentation {
        private final String bodyText;

        private SnapshotPresentation(String bodyText) {
            this.bodyText = bodyText;
        }

        public String getBodyText() {
            return this.bodyText;
        }
    }
}
