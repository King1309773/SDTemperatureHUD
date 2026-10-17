package dev.rlcraftceaddons.sdtemperaturehud.client.layout;

public final class HudLayoutMath {
    public static final double MIN_SCALE = 0.50D;
    public static final double MAX_SCALE = 2.00D;
    public static final double SCALE_STEP = 0.05D;

    private static final double DEFAULT_SCALE = 1.0;
    private HudLayoutMath() {
    }

    public static double clampScale(double scale) {
        if (!Double.isFinite(scale)) {
            return DEFAULT_SCALE;
        }
        return Math.max(MIN_SCALE, Math.min(MAX_SCALE, scale));
    }

    public static double stepScale(double scale, int wheelDelta) {
        double clampedScale = clampScale(scale);
        if (wheelDelta == 0) {
            return clampedScale;
        }
        double direction = wheelDelta > 0 ? SCALE_STEP : -SCALE_STEP;
        return clampScale(clampedScale + direction);
    }

    public static HudBounds toBounds(
        HudLayoutState state,
        int screenWidth,
        int screenHeight,
        int baseWidth,
        int baseHeight,
        int visiblePixels
    ) {
        Projection projection = project(
            state,
            screenWidth,
            screenHeight,
            baseWidth,
            baseHeight,
            visiblePixels
        );
        return new HudBounds(
            projection.boundsX,
            projection.boundsY,
            projection.width,
            projection.height
        );
    }

    public static RenderPosition toRenderPosition(
        HudLayoutState state,
        int screenWidth,
        int screenHeight,
        int baseWidth,
        int baseHeight,
        int visiblePixels
    ) {
        Projection projection = project(
            state,
            screenWidth,
            screenHeight,
            baseWidth,
            baseHeight,
            visiblePixels
        );
        return new RenderPosition(
            projection.renderX,
            projection.renderY,
            projection.scale
        );
    }

    public static HudLayoutState normalized(HudBounds bounds, int screenWidth, int screenHeight, double scale) {
        double safeScale = clampScale(scale);
        if (screenWidth <= 0 || screenHeight <= 0) {
            return new HudLayoutState(0.0, 0.0, safeScale);
        }
        if (bounds == null) {
            throw new IllegalArgumentException("bounds must not be null");
        }
        return new HudLayoutState(
            (double)bounds.getX() / screenWidth,
            (double)bounds.getY() / screenHeight,
            safeScale
        );
    }

    private static int integerDimension(double exactDimension) {
        double scaled = Math.ceil(exactDimension);
        return scaled >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int)scaled;
    }

    private static Projection project(
        HudLayoutState state,
        int screenWidth,
        int screenHeight,
        int baseWidth,
        int baseHeight,
        int visiblePixels
    ) {
        if (state == null) {
            throw new IllegalArgumentException("state must not be null");
        }
        requirePositive("screenWidth", screenWidth);
        requirePositive("screenHeight", screenHeight);
        requirePositive("baseWidth", baseWidth);
        requirePositive("baseHeight", baseHeight);

        double scale = clampScale(state.getScale());
        double exactWidth = baseWidth * scale;
        double exactHeight = baseHeight * scale;
        int width = integerDimension(exactWidth);
        int height = integerDimension(exactHeight);
        double rawX = projectedPosition(state.getNormalizedX(), screenWidth);
        double rawY = projectedPosition(state.getNormalizedY(), screenHeight);
        double renderX = clampForVisibility(
            rawX,
            exactWidth,
            screenWidth,
            visiblePixels
        );
        double renderY = clampForVisibility(
            rawY,
            exactHeight,
            screenHeight,
            visiblePixels
        );
        int boundsX = (int)Math.round(clampForVisibility(
            rawX,
            width,
            screenWidth,
            visiblePixels
        ));
        int boundsY = (int)Math.round(clampForVisibility(
            rawY,
            height,
            screenHeight,
            visiblePixels
        ));
        return new Projection(
            renderX,
            renderY,
            boundsX,
            boundsY,
            scale,
            width,
            height
        );
    }

    private static double projectedPosition(double normalizedPosition, int screenDimension) {
        double projected = normalizedPosition * screenDimension;
        if (Double.isNaN(projected)) {
            return 0.0;
        }
        return projected;
    }

    private static double clampForVisibility(
        double position,
        double panelDimension,
        int screenDimension,
        int visiblePixels
    ) {
        double maximumVisible = Math.min(screenDimension, panelDimension);
        double safeVisible = Math.max(
            0.0,
            Math.min((double)visiblePixels, maximumVisible)
        );
        return Math.max(
            safeVisible - panelDimension,
            Math.min(screenDimension - safeVisible, position)
        );
    }

    private static void requirePositive(String name, int value) {
        if (value <= 0) {
            throw new IllegalArgumentException(name + " must be positive");
        }
    }

    public static final class RenderPosition {
        private final double x;
        private final double y;
        private final double scale;

        private RenderPosition(double x, double y, double scale) {
            this.x = x;
            this.y = y;
            this.scale = scale;
        }

        public double getX() {
            return this.x;
        }

        public double getY() {
            return this.y;
        }

        public double getScale() {
            return this.scale;
        }
    }

    private static final class Projection {
        private final double renderX;
        private final double renderY;
        private final int boundsX;
        private final int boundsY;
        private final double scale;
        private final int width;
        private final int height;

        private Projection(
            double renderX,
            double renderY,
            int boundsX,
            int boundsY,
            double scale,
            int width,
            int height
        ) {
            this.renderX = renderX;
            this.renderY = renderY;
            this.boundsX = boundsX;
            this.boundsY = boundsY;
            this.scale = scale;
            this.width = width;
            this.height = height;
        }
    }
}
