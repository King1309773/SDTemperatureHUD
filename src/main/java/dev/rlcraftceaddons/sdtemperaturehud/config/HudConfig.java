/*
 * Decompiled with CFR 0.152.
 */
package dev.rlcraftceaddons.sdtemperaturehud.config;

import dev.rlcraftceaddons.sdtemperaturehud.client.AnalysisDisplayMode;
import dev.rlcraftceaddons.sdtemperaturehud.client.theme.HudThemePreset;
import net.minecraftforge.common.config.Config;

@Config(modid="sdtemperaturehud", name="sdtemperaturehud")
public final class HudConfig {
    @Config.LangKey(value="sdtemperaturehud.config.x_offset")
    @Config.Name(value="XOffset")
    @Config.Comment(value={"Horizontal offset from the default position, in scaled GUI pixels."})
    @Config.RangeInt(min=-500, max=500)
    public static int xOffset = 0;
    @Config.LangKey(value="sdtemperaturehud.config.y_offset")
    @Config.Name(value="YOffset")
    @Config.Comment(value={"Vertical offset from the default position, in scaled GUI pixels."})
    @Config.RangeInt(min=-500, max=500)
    public static int yOffset = 0;
    @Config.LangKey(value="sdtemperaturehud.config.scale")
    @Config.Name(value="Scale")
    @Config.Comment(value={"Scale multiplier applied to the temperature HUD."})
    @Config.RangeDouble(min=0.5, max=2.0)
    public static double scale = 1.0;
    @Config.LangKey(value="sdtemperaturehud.config.background_alpha")
    @Config.Name(value="BackgroundAlpha")
    @Config.Comment(value={"Background opacity from 0 (transparent) to 255 (opaque)."})
    @Config.RangeInt(min=0, max=255)
    public static int backgroundAlpha = 214;
    @Config.LangKey(value="sdtemperaturehud.config.warning_flash")
    @Config.Name(value="EnableWarningFlash")
    @Config.Comment(value={"Pulse the entire panel when approaching or entering dangerous temperatures."})
    public static boolean warningFlashEnabled = true;
    @Config.LangKey(value="sdtemperaturehud.config.warning_sound")
    @Config.Name(value="EnableWarningSound")
    @Config.Comment(value={"Play one short sound when first entering a critical danger zone."})
    public static boolean warningSoundEnabled = false;
    @Config.LangKey(value="sdtemperaturehud.config.temperature_analysis")
    @Config.Name(value="EnableTemperatureAnalysis")
    @Config.Comment(value={"Show the temperature influence breakdown while the analysis key is held."})
    public static boolean analysisEnabled = true;

    @Config.LangKey("sdtemperaturehud.config.change_hints")
    @Config.Name("EnableChangeHints")
    @Config.Comment({"Briefly show changes in temperature modifier contributions."})
    public static boolean changeHintsEnabled = true;

    @Config.LangKey("sdtemperaturehud.config.change_hint_threshold")
    @Config.Name("ChangeHintThreshold")
    @Config.Comment({"Minimum change in a modifier contribution before showing a hint."})
    @Config.RangeInt(min=1, max=25)
    public static int changeHintThreshold = 2;

    @Config.LangKey("sdtemperaturehud.config.change_hint_duration")
    @Config.Name("ChangeHintDurationSeconds")
    @Config.Comment({"How many seconds a temperature contribution change hint remains visible."})
    @Config.RangeInt(min=1, max=10)
    public static int changeHintDurationSeconds = 4;

    @Config.LangKey(value="sdtemperaturehud.config.layout")
    @Config.Name(value="Layout")
    public static final Layout layout = new Layout();
    @Config.LangKey(value="sdtemperaturehud.config.analysis")
    @Config.Name(value="Analysis")
    public static final Analysis analysis = new Analysis();
    @Config.LangKey(value="sdtemperaturehud.config.visibility")
    @Config.Name(value="Visibility")
    public static final Visibility visibility = new Visibility();
    @Config.LangKey(value="sdtemperaturehud.config.theme")
    @Config.Name(value="Theme")
    public static final Theme theme = new Theme();

    @Config.LangKey("sdtemperaturehud.map.title")
    public static final MapIntegration mapIntegration = new MapIntegration();

    public static final class MapIntegration {
        @Config.LangKey("sdtemperaturehud.map.minimap")
        public boolean minimapEnabled = true;
        @Config.LangKey("sdtemperaturehud.map.worldmap")
        public boolean worldMapEnabled = true;
    }

    private HudConfig() {
    }

    public static final class Layout {
        @Config.LangKey(value="sdtemperaturehud.config.layout_version")
        @Config.Name(value="Version")
        @Config.Comment(value={"Internal layout schema version. Version zero keeps the legacy offset layout pending migration."})
        public int version = 0;

        @Config.LangKey(value="sdtemperaturehud.config.compact_x")
        @Config.Name(value="CompactX")
        @Config.Comment(value={"Normalized horizontal position of the compact temperature panel."})
        @Config.RangeDouble(min=-2.0, max=3.0)
        public double compactX = 0.5;

        @Config.LangKey(value="sdtemperaturehud.config.compact_y")
        @Config.Name(value="CompactY")
        @Config.Comment(value={"Normalized vertical position of the compact temperature panel."})
        @Config.RangeDouble(min=-2.0, max=3.0)
        public double compactY = 0.5;

        @Config.LangKey(value="sdtemperaturehud.config.compact_scale")
        @Config.Name(value="CompactScale")
        @Config.Comment(value={"Scale multiplier applied to the compact temperature panel."})
        @Config.RangeDouble(min=0.5, max=2.0)
        @Config.SlidingOption
        public double compactScale = 1.0;

        @Config.LangKey(value="sdtemperaturehud.config.analysis_x")
        @Config.Name(value="AnalysisX")
        @Config.Comment(value={"Normalized horizontal position of the temperature analysis panel."})
        @Config.RangeDouble(min=-2.0, max=3.0)
        public double analysisX = 0.5;

        @Config.LangKey(value="sdtemperaturehud.config.analysis_y")
        @Config.Name(value="AnalysisY")
        @Config.Comment(value={"Normalized vertical position of the temperature analysis panel."})
        @Config.RangeDouble(min=-2.0, max=3.0)
        public double analysisY = 0.5;

        @Config.LangKey(value="sdtemperaturehud.config.analysis_scale")
        @Config.Name(value="AnalysisScale")
        @Config.Comment(value={"Scale multiplier applied to the temperature analysis panel."})
        @Config.RangeDouble(min=0.5, max=2.0)
        @Config.SlidingOption
        public double analysisScale = 1.0;
    }

    public static final class Analysis {
        @Config.LangKey(value="sdtemperaturehud.config.analysis_display_mode")
        @Config.Name(value="DisplayMode")
        @Config.Comment(value={"Controls when the temperature analysis panel is displayed."})
        public AnalysisDisplayMode displayMode = AnalysisDisplayMode.HOLD;
    }

    public static final class Visibility {
        @Config.LangKey(value="sdtemperaturehud.config.show_trend")
        @Config.Name(value="ShowTrend")
        public boolean showTrend = true;
        @Config.LangKey(value="sdtemperaturehud.config.show_target")
        @Config.Name(value="ShowTarget")
        public boolean showTarget = true;
        @Config.LangKey(value="sdtemperaturehud.config.show_analysis_title")
        @Config.Name(value="ShowAnalysisTitle")
        public boolean showAnalysisTitle = true;
        @Config.LangKey(value="sdtemperaturehud.config.show_base_environment")
        @Config.Name(value="ShowBaseEnvironment")
        public boolean showBaseEnvironment = true;
        @Config.LangKey(value="sdtemperaturehud.config.show_equipment")
        @Config.Name(value="ShowEquipment")
        public boolean showEquipment = true;
        @Config.LangKey(value="sdtemperaturehud.config.show_season")
        @Config.Name(value="ShowSeason")
        public boolean showSeason = true;
        @Config.LangKey(value="sdtemperaturehud.config.show_nearby_heat_sources")
        @Config.Name(value="ShowNearbyHeatSources")
        public boolean showNearbyHeatSources = true;
        @Config.LangKey(value="sdtemperaturehud.config.show_other_modifiers")
        @Config.Name(value="ShowOtherModifiers")
        public boolean showOtherModifiers = true;
        @Config.LangKey(value="sdtemperaturehud.config.show_analysis_target")
        @Config.Name(value="ShowAnalysisTarget")
        public boolean showAnalysisTarget = true;
    }

    public static final class Theme {
        @Config.LangKey(value="sdtemperaturehud.config.theme_preset")
        @Config.Name(value="Preset")
        public HudThemePreset preset = HudThemePreset.CLASSIC;
        @Config.LangKey(value="sdtemperaturehud.config.use_custom_colors")
        @Config.Name(value="UseCustomColors")
        public boolean useCustomColors = false;
        @Config.LangKey(value="sdtemperaturehud.config.severe_cold_color")
        @Config.Name(value="SevereCold")
        public String severeCold = "#48C8FF";
        @Config.LangKey(value="sdtemperaturehud.config.cold_color")
        @Config.Name(value="Cold")
        public String cold = "#82DDFF";
        @Config.LangKey(value="sdtemperaturehud.config.comfortable_color")
        @Config.Name(value="Comfortable")
        public String comfortable = "#8FF0AD";
        @Config.LangKey(value="sdtemperaturehud.config.warm_color")
        @Config.Name(value="Warm")
        public String warm = "#FFB34D";
        @Config.LangKey(value="sdtemperaturehud.config.severe_hot_color")
        @Config.Name(value="SevereHot")
        public String severeHot = "#FF5945";
        @Config.LangKey(value="sdtemperaturehud.config.rising_color")
        @Config.Name(value="Rising")
        public String rising = "#FFD064";
        @Config.LangKey(value="sdtemperaturehud.config.falling_color")
        @Config.Name(value="Falling")
        public String falling = "#7FDCFF";
        @Config.LangKey(value="sdtemperaturehud.config.stable_color")
        @Config.Name(value="Stable")
        public String stable = "#D5D9DC";
        @Config.LangKey(value="sdtemperaturehud.config.background_color")
        @Config.Name(value="Background")
        public String background = "#0A0D12";
        @Config.LangKey(value="sdtemperaturehud.config.border_color")
        @Config.Name(value="Border")
        public String border = "#4D5662";
        @Config.LangKey(value="sdtemperaturehud.config.text_color")
        @Config.Name(value="Text")
        public String text = "#C3C7CC";
    }
}
