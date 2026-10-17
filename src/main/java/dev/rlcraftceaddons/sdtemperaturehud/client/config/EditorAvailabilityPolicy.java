package dev.rlcraftceaddons.sdtemperaturehud.client.config;

public final class EditorAvailabilityPolicy {
    private EditorAvailabilityPolicy() {
    }

    public static boolean isAvailable(boolean hasWorld, boolean hasPlayer) {
        return hasWorld && hasPlayer;
    }
}
