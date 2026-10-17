package dev.rlcraftceaddons.sdtemperaturehud.client;

public final class RendererTestFactory {
    private RendererTestFactory() {
    }

    public static TemperatureHudRenderer fixedWidth(final int widthPerCharacter) {
        return new TemperatureHudRenderer(
            null,
            new TemperatureHudRenderer.TextWidthProvider() {
                @Override
                public int width(String text) {
                    return text.length() * widthPerCharacter;
                }
            }
        );
    }
}
