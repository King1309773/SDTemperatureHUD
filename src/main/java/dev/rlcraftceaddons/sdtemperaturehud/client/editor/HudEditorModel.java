package dev.rlcraftceaddons.sdtemperaturehud.client.editor;

import dev.rlcraftceaddons.sdtemperaturehud.client.layout.HudBounds;
import dev.rlcraftceaddons.sdtemperaturehud.client.layout.HudLayoutMath;
import dev.rlcraftceaddons.sdtemperaturehud.client.layout.HudLayoutState;
import dev.rlcraftceaddons.sdtemperaturehud.client.layout.HudPanel;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public final class HudEditorModel {
    public static final int MINIMUM_VISIBLE_PIXELS = 16;

    private final EnumMap<HudPanel, HudLayoutState> states =
        new EnumMap<HudPanel, HudLayoutState>(HudPanel.class);
    private final EnumMap<HudPanel, HudLayoutState> defaults =
        new EnumMap<HudPanel, HudLayoutState>(HudPanel.class);
    private HudPanel selected = HudPanel.COMPACT;
    private boolean dragging;
    private int dragOffsetX;
    private int dragOffsetY;

    public HudEditorModel(
        HudLayoutState compact,
        HudLayoutState analysis,
        HudLayoutState compactDefault,
        HudLayoutState analysisDefault
    ) {
        this.states.put(HudPanel.COMPACT, requireState(compact, "compact"));
        this.states.put(HudPanel.ANALYSIS, requireState(analysis, "analysis"));
        this.defaults.put(HudPanel.COMPACT, requireState(compactDefault, "compactDefault"));
        this.defaults.put(HudPanel.ANALYSIS, requireState(analysisDefault, "analysisDefault"));
    }

    public void select(HudPanel panel) {
        this.selected = requirePanel(panel);
        this.dragging = false;
    }

    public HudPanel selectAt(
        int mouseX,
        int mouseY,
        int screenWidth,
        int screenHeight,
        int compactWidth,
        int compactHeight,
        int analysisWidth,
        int analysisHeight
    ) {
        if (bounds(
            HudPanel.ANALYSIS,
            screenWidth,
            screenHeight,
            analysisWidth,
            analysisHeight
        ).contains(mouseX, mouseY)) {
            this.select(HudPanel.ANALYSIS);
            return HudPanel.ANALYSIS;
        }
        if (bounds(
            HudPanel.COMPACT,
            screenWidth,
            screenHeight,
            compactWidth,
            compactHeight
        ).contains(mouseX, mouseY)) {
            this.select(HudPanel.COMPACT);
            return HudPanel.COMPACT;
        }
        return null;
    }

    public void beginDrag(
        int mouseX,
        int mouseY,
        int screenWidth,
        int screenHeight,
        int panelWidth,
        int panelHeight
    ) {
        HudBounds current = bounds(
            this.selected,
            screenWidth,
            screenHeight,
            panelWidth,
            panelHeight
        );
        this.dragOffsetX = mouseX - current.getX();
        this.dragOffsetY = mouseY - current.getY();
        this.dragging = true;
    }

    public void dragTo(
        int mouseX,
        int mouseY,
        int screenWidth,
        int screenHeight,
        int panelWidth,
        int panelHeight
    ) {
        if (!this.dragging) {
            return;
        }
        HudLayoutState current = this.state(this.selected);
        HudLayoutState proposed = new HudLayoutState(
            (double)(mouseX - this.dragOffsetX) / screenWidth,
            (double)(mouseY - this.dragOffsetY) / screenHeight,
            current.getScale()
        );
        this.states.put(
            this.selected,
            validate(proposed, screenWidth, screenHeight, panelWidth, panelHeight)
        );
    }

    public void endDrag() {
        this.dragging = false;
        this.dragOffsetX = 0;
        this.dragOffsetY = 0;
    }

    void clearTransientState() {
        this.endDrag();
        this.selected = null;
    }

    public void wheel(
        int wheelDelta,
        int screenWidth,
        int screenHeight,
        int panelWidth,
        int panelHeight
    ) {
        HudLayoutState current = this.state(this.selected);
        HudLayoutState scaled = new HudLayoutState(
            current.getNormalizedX(),
            current.getNormalizedY(),
            HudLayoutMath.stepScale(current.getScale(), wheelDelta)
        );
        this.states.put(
            this.selected,
            validate(scaled, screenWidth, screenHeight, panelWidth, panelHeight)
        );
    }

    public void resetSelected(
        int screenWidth,
        int screenHeight,
        int compactWidth,
        int compactHeight,
        int analysisWidth,
        int analysisHeight
    ) {
        int panelWidth = this.selected == HudPanel.ANALYSIS ? analysisWidth : compactWidth;
        int panelHeight = this.selected == HudPanel.ANALYSIS ? analysisHeight : compactHeight;
        this.states.put(
            this.selected,
            validate(
                this.defaultState(this.selected),
                screenWidth,
                screenHeight,
                panelWidth,
                panelHeight
            )
        );
        this.endDrag();
    }

    public HudLayoutState state(HudPanel panel) {
        return this.states.get(requirePanel(panel));
    }

    public HudLayoutState defaultState(HudPanel panel) {
        return this.defaults.get(requirePanel(panel));
    }

    public HudPanel selected() {
        return this.selected;
    }

    public boolean isDragging() {
        return this.dragging;
    }

    public HudBounds bounds(
        HudPanel panel,
        int screenWidth,
        int screenHeight,
        int panelWidth,
        int panelHeight
    ) {
        return HudLayoutMath.toBounds(
            this.state(panel),
            screenWidth,
            screenHeight,
            panelWidth,
            panelHeight,
            MINIMUM_VISIBLE_PIXELS
        );
    }

    public Map<HudPanel, HudLayoutState> validatedStates(
        int screenWidth,
        int screenHeight,
        int compactWidth,
        int compactHeight,
        int analysisWidth,
        int analysisHeight
    ) {
        EnumMap<HudPanel, HudLayoutState> validated =
            new EnumMap<HudPanel, HudLayoutState>(HudPanel.class);
        validated.put(
            HudPanel.COMPACT,
            validate(
                this.state(HudPanel.COMPACT),
                screenWidth,
                screenHeight,
                compactWidth,
                compactHeight
            )
        );
        validated.put(
            HudPanel.ANALYSIS,
            validate(
                this.state(HudPanel.ANALYSIS),
                screenWidth,
                screenHeight,
                analysisWidth,
                analysisHeight
            )
        );
        return Collections.unmodifiableMap(validated);
    }

    private static HudLayoutState validate(
        HudLayoutState state,
        int screenWidth,
        int screenHeight,
        int panelWidth,
        int panelHeight
    ) {
        HudBounds constrained = HudLayoutMath.toBounds(
            state,
            screenWidth,
            screenHeight,
            panelWidth,
            panelHeight,
            MINIMUM_VISIBLE_PIXELS
        );
        return HudLayoutMath.normalized(constrained, screenWidth, screenHeight, state.getScale());
    }

    private static HudPanel requirePanel(HudPanel panel) {
        if (panel == null) {
            throw new IllegalArgumentException("panel must not be null");
        }
        return panel;
    }

    private static HudLayoutState requireState(HudLayoutState state, String name) {
        if (state == null) {
            throw new IllegalArgumentException(name + " must not be null");
        }
        return state;
    }
}
