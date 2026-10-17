package dev.rlcraftceaddons.sdtemperaturehud.client.config;

public enum ThemeColorField {
    SEVERE_COLD("sdtemperaturehud.theme.severe_cold"),
    COLD("sdtemperaturehud.theme.cold"),
    COMFORTABLE("sdtemperaturehud.theme.comfortable"),
    WARM("sdtemperaturehud.theme.warm"),
    SEVERE_HOT("sdtemperaturehud.theme.severe_hot"),
    RISING("sdtemperaturehud.theme.rising"),
    FALLING("sdtemperaturehud.theme.falling"),
    STABLE("sdtemperaturehud.theme.stable"),
    BACKGROUND("sdtemperaturehud.theme.background"),
    BORDER("sdtemperaturehud.theme.border"),
    TEXT("sdtemperaturehud.theme.text");

    private final String languageKey;

    ThemeColorField(String languageKey) {
        this.languageKey = languageKey;
    }

    public String getLanguageKey() {
        return this.languageKey;
    }
}
