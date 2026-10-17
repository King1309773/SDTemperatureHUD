package dev.rlcraftceaddons.sdtemperaturehud.client.layout;

public final class HudLayoutState {
    private final double normalizedX;
    private final double normalizedY;
    private final double scale;

    public HudLayoutState(double normalizedX, double normalizedY, double scale) {
        this.normalizedX = normalizedX;
        this.normalizedY = normalizedY;
        this.scale = scale;
    }

    public double getNormalizedX() {
        return this.normalizedX;
    }

    public double getNormalizedY() {
        return this.normalizedY;
    }

    public double getScale() {
        return this.scale;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof HudLayoutState)) {
            return false;
        }
        HudLayoutState that = (HudLayoutState)other;
        return Double.compare(this.normalizedX, that.normalizedX) == 0
            && Double.compare(this.normalizedY, that.normalizedY) == 0
            && Double.compare(this.scale, that.scale) == 0;
    }

    @Override
    public int hashCode() {
        int result = hash(this.normalizedX);
        result = 31 * result + hash(this.normalizedY);
        result = 31 * result + hash(this.scale);
        return result;
    }

    @Override
    public String toString() {
        return "HudLayoutState{" +
            "normalizedX=" + this.normalizedX +
            ", normalizedY=" + this.normalizedY +
            ", scale=" + this.scale +
            '}';
    }

    private static int hash(double value) {
        long bits = Double.doubleToLongBits(value);
        return (int)(bits ^ bits >>> 32);
    }
}
