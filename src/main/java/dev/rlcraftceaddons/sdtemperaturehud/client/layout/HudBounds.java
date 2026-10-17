package dev.rlcraftceaddons.sdtemperaturehud.client.layout;

public final class HudBounds {
    private final int x;
    private final int y;
    private final int width;
    private final int height;

    public HudBounds(int x, int y, int width, int height) {
        if (width <= 0) {
            throw new IllegalArgumentException("width must be positive");
        }
        if (height <= 0) {
            throw new IllegalArgumentException("height must be positive");
        }
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public boolean contains(int pointX, int pointY) {
        return pointX >= this.x
            && (long)pointX < (long)this.x + this.width
            && pointY >= this.y
            && (long)pointY < (long)this.y + this.height;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof HudBounds)) {
            return false;
        }
        HudBounds that = (HudBounds)other;
        return this.x == that.x
            && this.y == that.y
            && this.width == that.width
            && this.height == that.height;
    }

    @Override
    public int hashCode() {
        int result = this.x;
        result = 31 * result + this.y;
        result = 31 * result + this.width;
        result = 31 * result + this.height;
        return result;
    }

    @Override
    public String toString() {
        return "HudBounds{" +
            "x=" + this.x +
            ", y=" + this.y +
            ", width=" + this.width +
            ", height=" + this.height +
            '}';
    }
}
