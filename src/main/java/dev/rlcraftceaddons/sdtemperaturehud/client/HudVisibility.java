/*
 * Decompiled with CFR 0.152.
 */
package dev.rlcraftceaddons.sdtemperaturehud.client;

final class HudVisibility {
    private HudVisibility() {
    }

    static boolean shouldRender(boolean hasPlayer, boolean hasWorld, boolean hasPlayerController, boolean hideGui, boolean survivalOrAdventure, boolean temperatureEnabled, boolean alternateTemperatureEnabled, boolean gameOver, boolean hudEditorOpen) {
        return hasPlayer && hasWorld && hasPlayerController && !hideGui && survivalOrAdventure && temperatureEnabled && alternateTemperatureEnabled && !gameOver && !hudEditorOpen;
    }

    static String compactText(String body, String trend, String target, boolean showTrend, boolean showTarget) {
        StringBuilder text = new StringBuilder(body);
        if (showTrend) {
            text.append(trend);
        }
        if (showTarget) {
            text.append(target);
        }
        return text.toString();
    }

    static boolean shouldRenderAnalysis(boolean requested, boolean hasBreakdown, boolean hasVisibleRows) {
        return requested && hasBreakdown && hasVisibleRows;
    }
}
