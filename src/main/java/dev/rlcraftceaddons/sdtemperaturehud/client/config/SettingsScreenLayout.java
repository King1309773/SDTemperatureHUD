package dev.rlcraftceaddons.sdtemperaturehud.client.config;

/** GUI-pixel layout shared by drawing, hit testing and pagination. */
final class SettingsScreenLayout {
    final int left, width, controlWidth, previewLeft, previewWidth, footerY, pagerY;
    private SettingsScreenLayout(int screenWidth, int screenHeight) {
        width = Math.min(620, Math.max(280, screenWidth - 16));
        left = (screenWidth - width) / 2;
        controlWidth = Math.min(250, (width - 16) / 2);
        previewLeft = left + controlWidth + 16;
        previewWidth = width - controlWidth - 16;
        footerY = screenHeight - 27;
        pagerY = footerY - 26;
    }
    static SettingsScreenLayout forScreen(int width, int height) {
        return new SettingsScreenLayout(width, height);
    }
    int rows() { return Math.max(1, Math.min(9, (pagerY - 96) / 22)); }
    int rowY(int index) { return 96 + index * 22; }
    int pageCount(int count) { return Math.max(1, (count + rows() - 1) / rows()); }
    int clampPage(int page, int count) { return Math.max(0, Math.min(pageCount(count) - 1, page)); }
}

