package dev.rlcraftceaddons.sdtemperaturehud.client;

import dev.rlcraftceaddons.sdtemperaturehud.client.layout.HudBounds;

final class TemperatureHintLayout {
    private TemperatureHintLayout() {}

    static HudBounds place(int screenWidth, int screenHeight, HudBounds compact,
        HudBounds analysis, int width, int height) {
        int center = compact.getX() + compact.getWidth() / 2 - width / 2;
        int above = compact.getY() - height - 5;
        int top = analysis == null ? compact.getY() : Math.min(compact.getY(), analysis.getY());
        int right = analysis == null ? compact.getX() + compact.getWidth() :
            Math.max(compact.getX() + compact.getWidth(), analysis.getX() + analysis.getWidth());
        int left = analysis == null ? compact.getX() : Math.min(compact.getX(), analysis.getX());
        int bottom = compact.getY() + compact.getHeight();
        if (analysis != null) bottom = Math.max(bottom, analysis.getY() + analysis.getHeight());
        int[][] candidates = {{center, above}, {center, top - height - 5},
            {right + 5, compact.getY()}, {left - width - 5, compact.getY()}, {center, bottom + 5}};
        HudBounds best = null;
        long bestOverlap = Long.MAX_VALUE;
        for (int[] candidate : candidates) {
            HudBounds bounds = new HudBounds(
                Math.max(0, Math.min(Math.max(0, screenWidth - width), candidate[0])),
                Math.max(0, Math.min(Math.max(0, screenHeight - height), candidate[1])),
                width, height);
            long overlap = overlapArea(bounds, compact) + overlapArea(bounds, analysis);
            if (overlap == 0) return bounds;
            if (overlap < bestOverlap) { best = bounds; bestOverlap = overlap; }
        }
        return best;
    }

    static boolean overlaps(HudBounds a, HudBounds b) { return overlapArea(a, b) > 0; }
    private static long overlapArea(HudBounds a, HudBounds b) {
        if (b == null) return 0;
        long width = Math.max(0, Math.min((long)a.getX() + a.getWidth(),
            (long)b.getX() + b.getWidth()) - Math.max(a.getX(), b.getX()));
        long height = Math.max(0, Math.min((long)a.getY() + a.getHeight(),
            (long)b.getY() + b.getHeight()) - Math.max(a.getY(), b.getY()));
        return width * height;
    }
}

