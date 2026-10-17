package dev.rlcraftceaddons.sdtemperaturehud.client.theme;

public final class HudColorPalette {
    private static final int RGB_MASK = 0xFFFFFF;

    private final int severeCold;
    private final int cold;
    private final int comfortable;
    private final int warm;
    private final int severeHot;
    private final int rising;
    private final int falling;
    private final int stable;
    private final int background;
    private final int border;
    private final int text;

    public HudColorPalette(
        int severeCold,
        int cold,
        int comfortable,
        int warm,
        int severeHot,
        int rising,
        int falling,
        int stable,
        int background,
        int border,
        int text
    ) {
        this.severeCold = severeCold & RGB_MASK;
        this.cold = cold & RGB_MASK;
        this.comfortable = comfortable & RGB_MASK;
        this.warm = warm & RGB_MASK;
        this.severeHot = severeHot & RGB_MASK;
        this.rising = rising & RGB_MASK;
        this.falling = falling & RGB_MASK;
        this.stable = stable & RGB_MASK;
        this.background = background & RGB_MASK;
        this.border = border & RGB_MASK;
        this.text = text & RGB_MASK;
    }

    public int getSevereCold() {
        return this.severeCold;
    }

    public int getCold() {
        return this.cold;
    }

    public int getComfortable() {
        return this.comfortable;
    }

    public int getWarm() {
        return this.warm;
    }

    public int getSevereHot() {
        return this.severeHot;
    }

    public int getRising() {
        return this.rising;
    }

    public int getFalling() {
        return this.falling;
    }

    public int getStable() {
        return this.stable;
    }

    public int getBackground() {
        return this.background;
    }

    public int getBorder() {
        return this.border;
    }

    public int getText() {
        return this.text;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof HudColorPalette)) {
            return false;
        }
        HudColorPalette that = (HudColorPalette)other;
        return this.severeCold == that.severeCold
            && this.cold == that.cold
            && this.comfortable == that.comfortable
            && this.warm == that.warm
            && this.severeHot == that.severeHot
            && this.rising == that.rising
            && this.falling == that.falling
            && this.stable == that.stable
            && this.background == that.background
            && this.border == that.border
            && this.text == that.text;
    }

    @Override
    public int hashCode() {
        int result = this.severeCold;
        result = 31 * result + this.cold;
        result = 31 * result + this.comfortable;
        result = 31 * result + this.warm;
        result = 31 * result + this.severeHot;
        result = 31 * result + this.rising;
        result = 31 * result + this.falling;
        result = 31 * result + this.stable;
        result = 31 * result + this.background;
        result = 31 * result + this.border;
        result = 31 * result + this.text;
        return result;
    }

    @Override
    public String toString() {
        return "HudColorPalette{" +
            "severeCold=" + HexColorParser.format(this.severeCold) +
            ", cold=" + HexColorParser.format(this.cold) +
            ", comfortable=" + HexColorParser.format(this.comfortable) +
            ", warm=" + HexColorParser.format(this.warm) +
            ", severeHot=" + HexColorParser.format(this.severeHot) +
            ", rising=" + HexColorParser.format(this.rising) +
            ", falling=" + HexColorParser.format(this.falling) +
            ", stable=" + HexColorParser.format(this.stable) +
            ", background=" + HexColorParser.format(this.background) +
            ", border=" + HexColorParser.format(this.border) +
            ", text=" + HexColorParser.format(this.text) +
            '}';
    }
}
