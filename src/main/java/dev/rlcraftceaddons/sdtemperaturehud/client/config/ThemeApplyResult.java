package dev.rlcraftceaddons.sdtemperaturehud.client.config;

public final class ThemeApplyResult {
    private final boolean success;
    private final ThemeColorField invalidField;
    private final boolean changed;

    private ThemeApplyResult(boolean success, ThemeColorField invalidField, boolean changed) {
        this.success = success;
        this.invalidField = invalidField;
        this.changed = changed;
    }

    public static ThemeApplyResult success(boolean changed) {
        return new ThemeApplyResult(true, null, changed);
    }

    public static ThemeApplyResult invalid(ThemeColorField field) {
        return new ThemeApplyResult(false, field, false);
    }

    public boolean isSuccess() {
        return this.success;
    }

    public ThemeColorField getInvalidField() {
        return this.invalidField;
    }

    public boolean isChanged() {
        return this.changed;
    }
}
