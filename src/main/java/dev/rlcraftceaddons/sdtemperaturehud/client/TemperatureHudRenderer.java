/*
 * Decompiled with CFR 0.152.
 */
package dev.rlcraftceaddons.sdtemperaturehud.client;

import dev.rlcraftceaddons.sdtemperaturehud.client.layout.HudLayoutMath;
import dev.rlcraftceaddons.sdtemperaturehud.client.layout.HudLayoutState;
import dev.rlcraftceaddons.sdtemperaturehud.client.layout.HudBounds;
import dev.rlcraftceaddons.sdtemperaturehud.client.theme.HudColorPalette;
import dev.rlcraftceaddons.sdtemperaturehud.config.LegacyLayoutMigration;
import dev.rlcraftceaddons.sdtemperaturehud.temperature.TemperatureBreakdown;
import dev.rlcraftceaddons.sdtemperaturehud.temperature.TemperaturePresentation;
import dev.rlcraftceaddons.sdtemperaturehud.temperature.TemperatureSnapshot;
import dev.rlcraftceaddons.sdtemperaturehud.temperature.TemperatureWarning;
import dev.rlcraftceaddons.sdtemperaturehud.temperature.TemperatureWarningLevel;
import dev.rlcraftceaddons.sdtemperaturehud.temperature.TemperatureChangeTracker;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import org.lwjgl.opengl.GL11;

public final class TemperatureHudRenderer {
    private static final int PANEL_HEIGHT = 10;
    private final Minecraft minecraft;
    private final TextWidthProvider textWidthProvider;

    public TemperatureHudRenderer(Minecraft minecraft) {
        this(minecraft, new TextWidthProvider() {
            @Override
            public int width(String text) {
                return minecraft.fontRenderer.getStringWidth(text);
            }
        });
    }

    TemperatureHudRenderer(Minecraft minecraft, TextWidthProvider textWidthProvider) {
        this.minecraft = minecraft;
        this.textWidthProvider = textWidthProvider;
    }

    public void render(ScaledResolution resolution, TemperatureSnapshot snapshot, HudSettings settings) {
        this.render(resolution, snapshot, settings, false, System.currentTimeMillis());
    }

    public void render(ScaledResolution resolution, TemperatureSnapshot snapshot, HudSettings settings, long timeMs) {
        this.render(resolution, snapshot, settings, false, timeMs);
    }

    public void render(
        ScaledResolution resolution,
        TemperatureSnapshot snapshot,
        HudSettings settings,
        boolean analysisVisible,
        long timeMs
    ) {
        TemperatureWarningLevel warningLevel = warningLevelFor(snapshot);
        WarningPanelStyle style = WarningPanelStyle.forLevel(
            warningLevel,
            timeMs,
            settings.isWarningFlashEnabled()
        );
        CompactPanel compactPanel = this.measureCompact(snapshot, settings);
        AnalysisPanel analysisPanel = null;
        if (analysisVisible) {
            analysisPanel = this.measureAnalysis(snapshot, settings);
        }
        if (HudVisibility.shouldRenderAnalysis(
            analysisVisible,
            snapshot.hasBreakdown(),
            analysisPanel != null
        )) {
            this.renderAnalysis(resolution, analysisPanel, settings, style);
        }
        this.renderCompact(resolution, compactPanel, settings, style);
    }

    public CompactPanel measureCompact(TemperatureSnapshot snapshot, HudSettings settings) {
        HudColorPalette palette = settings.getColorPalette();
        String bodyText = TemperaturePresentation.from(snapshot).getBodyText();
        String trendText = "";
        String targetText = "";
        int trendColor = 0;
        int targetColor = 0;
        boolean showTrend = settings.isShowTrend() && snapshot.hasTargetTemperature();
        boolean showTarget = settings.isShowTarget() && snapshot.hasTargetTemperature();
        if (snapshot.isAvailable() && snapshot.hasTargetTemperature()) {
            int targetTemperature = snapshot.getTargetTemperature();
            TemperaturePresentation.Trend trend = TemperaturePresentation.trendFor(
                targetTemperature,
                snapshot.getBodyTemperature()
            );
            trendText = TemperaturePresentation.glyphFor(trend);
            targetText = Integer.toString(targetTemperature);
            trendColor = TemperaturePresentation.trendColor(trend, palette);
            targetColor = TemperaturePresentation.colorFor(targetTemperature, palette);
        }
        String compactText = HudVisibility.compactText(
            bodyText,
            trendText,
            targetText,
            showTrend,
            showTarget
        );
        int bodyWidth = this.textWidthProvider.width(bodyText);
        int trendWidth = showTrend ? this.textWidthProvider.width(trendText) : 0;
        return new CompactPanel(
            bodyText,
            showTrend ? trendText : null,
            showTarget ? targetText : null,
            bodyWidth,
            trendWidth,
            HudLayout.panelWidth(this.textWidthProvider.width(compactText)),
            snapshot.isAvailable()
                ? TemperaturePresentation.colorFor(snapshot.getBodyTemperature(), palette)
                : palette.getText(),
            trendColor,
            targetColor
        );
    }

    public AnalysisPanel measureAnalysis(TemperatureSnapshot snapshot, HudSettings settings) {
        if (snapshot == null) {
            throw new IllegalArgumentException("snapshot must not be null");
        }
        if (!snapshot.isAvailable() || !snapshot.hasBreakdown()) {
            return null;
        }
        return this.measureAnalysis(snapshot.getBreakdown(), settings);
    }

    public AnalysisPanel measureAnalysis(TemperatureBreakdown breakdown, HudSettings settings) {
        List<AnalysisRow> rows = this.createAnalysisRows(breakdown, settings);
        if (rows.isEmpty()) {
            return null;
        }
        String title = settings.isShowAnalysisTitle()
            ? I18n.format("sdtemperaturehud.analysis.title")
            : null;
        int lineCount = (title == null ? 0 : 1) + rows.size();
        int contentWidth = title == null ? 0 : this.textWidthProvider.width(title);
        boolean hasTarget = false;
        for (AnalysisRow row : rows) {
            contentWidth = Math.max(
                contentWidth,
                this.textWidthProvider.width(row.label)
                    + TemperatureAnalysisLayout.VALUE_GAP
                    + this.textWidthProvider.width(row.value)
            );
            hasTarget |= row.target;
        }
        int panelWidth = TemperatureAnalysisLayout.panelWidth(contentWidth);
        int panelHeight = TemperatureAnalysisLayout.panelHeight(lineCount, hasTarget);
        return new AnalysisPanel(title, rows, panelWidth, panelHeight);
    }

    public void renderCompact(
        ScaledResolution resolution,
        CompactPanel panel,
        HudSettings settings,
        WarningPanelStyle style
    ) {
        PanelTransform transform = compactTransform(
            resolution.getScaledWidth(),
            resolution.getScaledHeight(),
            panel.width,
            settings
        );

        FontRenderer font = this.minecraft.fontRenderer;
        boolean blendEnabled = GL11.glIsEnabled(GL11.GL_BLEND);
        GlStateManager.pushMatrix();
        try {
            GlStateManager.translate(transform.x, transform.y, 0.0);
            GlStateManager.scale(transform.scale, transform.scale, 1.0);
            Gui.drawRect(0, 0, panel.width, PANEL_HEIGHT, backgroundColor(settings, style));
            enablePanelBlend();
            font.drawStringWithShadow(
                panel.bodyText,
                HudLayout.bodyX(),
                1.0f,
                style.textColor(panel.bodyColor)
            );
            if (panel.trendText != null) {
                font.drawStringWithShadow(
                    panel.trendText,
                    HudLayout.arrowX(panel.bodyWidth),
                    1.0f,
                    style.textColor(panel.trendColor)
                );
            }
            if (panel.targetText != null) {
                font.drawStringWithShadow(
                    panel.targetText,
                    HudLayout.targetX(panel.bodyWidth, panel.trendWidth),
                    1.0f,
                    style.textColor(panel.targetColor)
                );
            }
        } finally {
            try {
                restoreGlState(blendEnabled);
            } finally {
                GlStateManager.popMatrix();
            }
        }
    }

    public void renderAnalysis(
        ScaledResolution resolution,
        AnalysisPanel panel,
        HudSettings settings,
        WarningPanelStyle style
    ) {
        if (panel == null) {
            return;
        }
        PanelTransform transform = analysisTransform(
            resolution.getScaledWidth(),
            resolution.getScaledHeight(),
            panel.width,
            panel.height,
            settings
        );
        FontRenderer font = this.minecraft.fontRenderer;
        boolean blendEnabled = GL11.glIsEnabled(GL11.GL_BLEND);
        GlStateManager.pushMatrix();
        try {
            GlStateManager.translate(transform.x, transform.y, 0.0);
            GlStateManager.scale(transform.scale, transform.scale, 1.0);
            Gui.drawRect(0, 0, panel.width, panel.height, backgroundColor(settings, style));
            enablePanelBlend();
            HudColorPalette palette = settings.getColorPalette();
            int lineIndex = 0;
            if (panel.title != null) {
                int titleX = (panel.width - font.getStringWidth(panel.title)) / 2;
                font.drawStringWithShadow(
                    panel.title,
                    titleX,
                    TemperatureAnalysisLayout.lineY(lineIndex),
                    style.textColor(TemperatureAnalysisPresentation.titleColor(palette))
                );
                ++lineIndex;
            }
            for (AnalysisRow row : panel.rows) {
                int y = TemperatureAnalysisLayout.lineY(lineIndex);
                if (row.target) {
                    y += TemperatureAnalysisLayout.TARGET_GAP;
                    Gui.drawRect(
                        TemperatureAnalysisLayout.labelX(),
                        y - TemperatureAnalysisLayout.TARGET_GAP,
                        panel.width - TemperatureAnalysisLayout.HORIZONTAL_PADDING,
                        y - 1,
                        style.textColor(palette.getBorder())
                    );
                }
                font.drawStringWithShadow(
                    row.label,
                    TemperatureAnalysisLayout.labelX(),
                    y,
                    style.textColor(palette.getText())
                );
                font.drawStringWithShadow(
                    row.value,
                    TemperatureAnalysisLayout.valueX(panel.width, font.getStringWidth(row.value)),
                    y,
                    style.textColor(row.color)
                );
                ++lineIndex;
            }
        } finally {
            try {
                restoreGlState(blendEnabled);
            } finally {
                GlStateManager.popMatrix();
            }
        }
    }

    public void renderChangeHints(ScaledResolution resolution, TemperatureSnapshot snapshot,
        HudSettings settings, boolean analysisVisible, List<TemperatureChangeTracker.Hint> hints) {
        if (hints.isEmpty()) return;
        FontRenderer font = this.minecraft.fontRenderer;
        List<String> lines = new ArrayList<String>();
        int panelWidth = 1;
        for (TemperatureChangeTracker.Hint hint : hints) {
            String line = I18n.format("sdtemperaturehud.hint.change",
                I18n.format(hint.getSource().getLanguageKey()),
                TemperatureAnalysisPresentation.signed(hint.getDelta()));
            lines.add(line);
            panelWidth = Math.max(panelWidth, font.getStringWidth(line) + 6);
        }
        CompactPanel compact = this.measureCompact(snapshot, settings);
        int screenWidth = resolution.getScaledWidth(), screenHeight = resolution.getScaledHeight();
        PanelTransform compactAt = compactTransform(screenWidth, screenHeight, compact.width, settings);
        double scale = compactAt.scale;
        // Keep every line inside small windows without changing the saved HUD scale.
        scale = Math.min(scale, Math.max(1, screenWidth - 8) / (double)panelWidth);
        int panelHeight = lines.size() * 10 + 4;
        HudBounds analysisBounds = null;
        AnalysisPanel analysis = analysisVisible ? this.measureAnalysis(snapshot, settings) : null;
        if (analysis != null) {
            PanelTransform at = analysisTransform(screenWidth, screenHeight,
                analysis.width, analysis.height, settings);
            analysisBounds = new HudBounds((int)Math.floor(at.x), (int)Math.floor(at.y),
                Math.max(1, (int)Math.ceil(analysis.width * at.scale)),
                Math.max(1, (int)Math.ceil(analysis.height * at.scale)));
        }
        HudBounds bounds = TemperatureHintLayout.place(screenWidth, screenHeight,
            new HudBounds((int)Math.floor(compactAt.x), (int)Math.floor(compactAt.y),
                Math.max(1, (int)Math.ceil(compact.width * compactAt.scale)),
                Math.max(1, (int)Math.ceil(PANEL_HEIGHT * compactAt.scale))),
            analysisBounds, Math.max(1, (int)Math.ceil(panelWidth * scale)),
            Math.max(1, (int)Math.ceil(panelHeight * scale)));
        boolean blendEnabled = GL11.glIsEnabled(GL11.GL_BLEND);
        GlStateManager.pushMatrix();
        try {
            GlStateManager.translate(bounds.getX(), bounds.getY(), 0);
            GlStateManager.scale(scale, scale, 1);
            Gui.drawRect(0, 0, panelWidth, panelHeight,
                (settings.getBackgroundAlpha() << 24) | settings.getColorPalette().getBackground());
            enablePanelBlend();
            for (int index = 0; index < lines.size(); index++) {
                font.drawStringWithShadow(lines.get(index), 3, 2 + index * 10,
                    TemperatureAnalysisPresentation.modifierColor(
                        hints.get(index).getDelta(), settings.getColorPalette()));
            }
        } finally {
            try { restoreGlState(blendEnabled); } finally { GlStateManager.popMatrix(); }
        }
    }

    private List<AnalysisRow> createAnalysisRows(TemperatureBreakdown breakdown, HudSettings settings) {
        ArrayList<AnalysisRow> rows = new ArrayList<AnalysisRow>();
        HudColorPalette palette = settings.getColorPalette();
        if (settings.isShowBaseEnvironment()) {
            rows.add(AnalysisRow.absolute(
                "sdtemperaturehud.analysis.environment",
                breakdown.getEnvironment(),
                palette
            ));
        }
        if (settings.isShowEquipment()) {
            rows.add(AnalysisRow.modifier(
                "sdtemperaturehud.analysis.equipment",
                breakdown.getEquipment(),
                palette
            ));
        }
        if (settings.isShowSeason()) {
            rows.add(AnalysisRow.modifier(
                "sdtemperaturehud.analysis.season",
                breakdown.getSeason(),
                palette
            ));
        }
        if (settings.isShowNearbyHeatSources()) {
            rows.add(AnalysisRow.modifier(
                "sdtemperaturehud.analysis.heat_source",
                breakdown.getHeatSource(),
                palette
            ));
        }
        if (settings.isShowOtherModifiers()
            && TemperatureAnalysisPresentation.shouldShowOther(breakdown.getOther())) {
            rows.add(AnalysisRow.modifier(
                "sdtemperaturehud.analysis.other",
                breakdown.getOther(),
                palette
            ));
        }
        if (settings.isShowAnalysisTarget()) {
            rows.add(AnalysisRow.target(
                "sdtemperaturehud.analysis.target",
                breakdown.getTarget(),
                palette
            ));
        }
        return rows;
    }

    static int backgroundColor(HudSettings settings, WarningPanelStyle style) {
        return style.backgroundColor(
            settings.getBackgroundAlpha(),
            settings.getColorPalette().getBackground()
        );
    }

    static TemperatureWarningLevel warningLevelFor(TemperatureSnapshot snapshot) {
        if (snapshot == null) {
            throw new IllegalArgumentException("snapshot must not be null");
        }
        return snapshot.isAvailable()
            ? TemperatureWarning.classifyBodyTemperature(snapshot.getBodyTemperature())
            : TemperatureWarningLevel.NORMAL;
    }

    private static void enablePanelBlend() {
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO
        );
    }

    private static void restoreGlState(boolean blendEnabled) {
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.enableTexture2D();
        GlStateManager.enableAlpha();
        GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO
        );
        if (blendEnabled) {
            GlStateManager.enableBlend();
        } else {
            GlStateManager.disableBlend();
        }
    }

    static PanelTransform compactTransform(
        int screenWidth,
        int screenHeight,
        int panelWidth,
        HudSettings settings
    ) {
        if (settings.isLegacyLayoutPending()) {
            double compactScale = LegacyLayoutMigration.pendingCompactScale(
                settings.getScale(),
                settings.getCompactLayout().getScale(),
                settings.getAnalysisLayout().getScale()
            );
            HudLayoutState legacy = LegacyLayoutMigration.compactFromLegacy(
                settings.getXOffset(),
                settings.getYOffset(),
                compactScale,
                screenWidth,
                screenHeight,
                panelWidth,
                PANEL_HEIGHT
            );
            return new PanelTransform(
                legacy.getNormalizedX() * screenWidth,
                legacy.getNormalizedY() * screenHeight,
                legacy.getScale()
            );
        }
        HudLayoutState layout = settings.getCompactLayout();
        HudLayoutMath.RenderPosition position = HudLayout.panelRenderPosition(
            layout,
            screenWidth,
            screenHeight,
            panelWidth,
            PANEL_HEIGHT
        );
        return new PanelTransform(
            position.getX(),
            position.getY(),
            position.getScale()
        );
    }

    static PanelTransform analysisTransform(
        int screenWidth,
        int screenHeight,
        int panelWidth,
        int panelHeight,
        HudSettings settings
    ) {
        if (settings.isLegacyLayoutPending()) {
            double compactScale = LegacyLayoutMigration.pendingCompactScale(
                settings.getScale(),
                settings.getCompactLayout().getScale(),
                settings.getAnalysisLayout().getScale()
            );
            double analysisScale = LegacyLayoutMigration.pendingAnalysisScale(
                settings.getScale(),
                settings.getCompactLayout().getScale(),
                settings.getAnalysisLayout().getScale()
            );
            HudLayoutState legacy = LegacyLayoutMigration.analysisFromLegacy(
                settings.getXOffset(),
                settings.getYOffset(),
                compactScale,
                analysisScale,
                screenWidth,
                screenHeight,
                PANEL_HEIGHT,
                panelWidth,
                panelHeight
            );
            return new PanelTransform(
                legacy.getNormalizedX() * screenWidth,
                legacy.getNormalizedY() * screenHeight,
                legacy.getScale()
            );
        }
        HudLayoutState layout = settings.getAnalysisLayout();
        HudLayoutMath.RenderPosition position =
            TemperatureAnalysisLayout.panelRenderPosition(
                layout,
                screenWidth,
                screenHeight,
                panelWidth,
                panelHeight
            );
        return new PanelTransform(
            position.getX(),
            position.getY(),
            position.getScale()
        );
    }

    interface TextWidthProvider {
        int width(String text);
    }

    static final class PanelTransform {
        private final double x;
        private final double y;
        private final double scale;

        private PanelTransform(double x, double y, double scale) {
            this.x = x;
            this.y = y;
            this.scale = scale;
        }

        double getX() {
            return this.x;
        }

        double getY() {
            return this.y;
        }

        double getScale() {
            return this.scale;
        }
    }

    public static final class CompactPanel {
        private final String bodyText;
        private final String trendText;
        private final String targetText;
        private final int bodyWidth;
        private final int trendWidth;
        private final int width;
        private final int bodyColor;
        private final int trendColor;
        private final int targetColor;

        private CompactPanel(
            String bodyText,
            String trendText,
            String targetText,
            int bodyWidth,
            int trendWidth,
            int width,
            int bodyColor,
            int trendColor,
            int targetColor
        ) {
            this.bodyText = bodyText;
            this.trendText = trendText;
            this.targetText = targetText;
            this.bodyWidth = bodyWidth;
            this.trendWidth = trendWidth;
            this.width = width;
            this.bodyColor = bodyColor;
            this.trendColor = trendColor;
            this.targetColor = targetColor;
        }

        public int getWidth() {
            return this.width;
        }

        public int getHeight() {
            return PANEL_HEIGHT;
        }

        public String getBodyText() {
            return this.bodyText;
        }

        public String getTrendText() {
            return this.trendText;
        }

        public String getTargetText() {
            return this.targetText;
        }

        int getBodyColor() {
            return this.bodyColor;
        }

        int getTrendColor() {
            return this.trendColor;
        }

        int getTargetColor() {
            return this.targetColor;
        }
    }

    public static final class AnalysisPanel {
        private final String title;
        private final List<AnalysisRow> rows;
        private final int width;
        private final int height;

        private AnalysisPanel(String title, List<AnalysisRow> rows, int width, int height) {
            this.title = title;
            this.rows = Collections.unmodifiableList(new ArrayList<AnalysisRow>(rows));
            this.width = width;
            this.height = height;
        }

        public int getWidth() {
            return this.width;
        }

        public int getHeight() {
            return this.height;
        }
    }

    private static final class AnalysisRow {
        private final String label;
        private final String value;
        private final int color;
        private final boolean target;

        private AnalysisRow(String labelKey, String value, int color, boolean target) {
            this.label = I18n.format(labelKey);
            this.value = value;
            this.color = color;
            this.target = target;
        }

        private static AnalysisRow absolute(String labelKey, int value, HudColorPalette palette) {
            return new AnalysisRow(
                labelKey,
                Integer.toString(value),
                TemperaturePresentation.colorFor(value, palette),
                false
            );
        }

        private static AnalysisRow modifier(String labelKey, int value, HudColorPalette palette) {
            return new AnalysisRow(
                labelKey,
                TemperatureAnalysisPresentation.signed(value),
                TemperatureAnalysisPresentation.modifierColor(value, palette),
                false
            );
        }

        private static AnalysisRow target(String labelKey, int value, HudColorPalette palette) {
            return new AnalysisRow(
                labelKey,
                Integer.toString(value),
                TemperaturePresentation.colorFor(value, palette),
                true
            );
        }
    }
}
