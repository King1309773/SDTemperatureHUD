package dev.rlcraftceaddons.sdtemperaturehud.client.config;

import dev.rlcraftceaddons.sdtemperaturehud.client.HudSettings;

public enum AppearanceItem {
    TREND("show_trend"), TARGET("show_target"), TITLE("show_analysis_title"),
    ENVIRONMENT("show_base_environment"), EQUIPMENT("show_equipment"), SEASON("show_season"),
    HEAT_SOURCE("show_nearby_heat_sources"), OTHER("show_other_modifiers"),
    ANALYSIS_TARGET("show_analysis_target");

    private final String key;
    AppearanceItem(String key) { this.key = "sdtemperaturehud.config." + key; }
    public String getLanguageKey() { return key; }

    public boolean read(HudSettings settings) {
        switch (this) {
            case TREND: return settings.isShowTrend();
            case TARGET: return settings.isShowTarget();
            case TITLE: return settings.isShowAnalysisTitle();
            case ENVIRONMENT: return settings.isShowBaseEnvironment();
            case EQUIPMENT: return settings.isShowEquipment();
            case SEASON: return settings.isShowSeason();
            case HEAT_SOURCE: return settings.isShowNearbyHeatSources();
            case OTHER: return settings.isShowOtherModifiers();
            default: return settings.isShowAnalysisTarget();
        }
    }
}

