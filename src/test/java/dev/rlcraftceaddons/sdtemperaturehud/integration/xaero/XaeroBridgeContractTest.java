package dev.rlcraftceaddons.sdtemperaturehud.integration.xaero;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import org.junit.Test;
import static org.junit.Assert.*;

/** Source contracts supplement pure geometry tests; they do not claim graphical validation. */
public class XaeroBridgeContractTest {
    private String source(String relative) throws Exception {
        return new String(Files.readAllBytes(Paths.get("src/main/java/dev/rlcraftceaddons/sdtemperaturehud/"+relative)),StandardCharsets.UTF_8);
    }
    @Test public void entryPointRegistersTheControllerAndConfigEntry() throws Exception {
        assertTrue(source("SDTemperatureHud.java").contains("MapIntegrationController"));
        assertTrue(source("client/config/GuiHudConfig.java").contains("GuiMapIntegration"));
        assertTrue(source("client/ClientEventHandler.java").contains("instanceof GuiMapIntegration"));
    }
    @Test public void bridgesUseActualMapCallbacksAndDoNotInterceptClicks() throws Exception {
        String mini=source("integration/xaero/XaeroMinimapInfoBridge.java");
        String world=source("integration/xaero/XaeroWorldMapBridge.java");
        assertTrue(mini.contains("InfoDisplay.Builder"));assertTrue(mini.contains("getInfoDisplays()"));
        assertTrue(mini.contains("getEffectiveState()"));assertFalse(mini.contains("OVER_MINIMAP"));
        assertTrue(world.contains("extends ElementRenderer"));assertTrue(world.contains("isInteractable"));
        assertFalse(world.contains("setCanceled("));assertFalse(world.contains("MouseInputEvent"));
        assertTrue(world.contains("DrawScreenEvent.Post"));
        assertTrue(world.contains("isAutoSelected()"));
        assertTrue(world.contains("processor.getWorld()!=mc.world"));
    }
    @Test public void worldMapOnlyAppendsToTheNativeHeadingAndNeverOffersAreaElements() throws Exception {
        String world=source("integration/xaero/XaeroWorldMapBridge.java");
        assertTrue(world.contains("MapHeadingLayout.at("));
        assertTrue(world.contains("mainTextureLevel"));assertTrue(world.contains("getLeveledRegion("));
        assertTrue(world.contains("paintHeading("));
        assertTrue(world.contains("headingNames.name(biome)"));
        assertTrue(world.contains("hasNext(ElementRenderLocation location,Frame frame){return false;}"));
        assertFalse(world.contains("drawHoveringText("));assertFalse(world.contains(".regions("));
        assertFalse(world.contains(".layout("));
        assertTrue(source("client/map/MapIntegrationController.java").contains("XaeroIntegration.areaSamplingEnabled()"));
    }
    @Test public void bothLanguagesDescribeTheHeadingAndNativeInformationItem() throws Exception {
        for(String language:new String[]{"zh_cn","en_us"}) {
            String text=new String(Files.readAllBytes(Paths.get("src/main/resources/assets/sdtemperaturehud/lang/"+language+".lang")),StandardCharsets.UTF_8);
            assertTrue(text.contains("sdtemperaturehud.map.info.name="));
            assertTrue(text.contains("sdtemperaturehud.map.info.value="));
            assertTrue(text.contains("sdtemperaturehud.map.managed="));
            assertTrue(text.contains(language.equals("zh_cn")?"\u5927\u5730\u56fe\u9876\u90e8\u6e29\u5ea6":"World map heading temperature"));
            assertFalse(text.contains(language.equals("zh_cn")?"\u65e7\u5730\u56fe\u9700\u91cd\u65b0\u7ecf\u8fc7":"Revisit old areas to collect data"));
        }
    }
    @Test public void nativeInfoUsesSafeColumnsAndDoesNotDrawAnotherOverlay() throws Exception {
        String reader=source("integration/LoadedBiomeTemperature.java");
        assertTrue(reader.contains("getLoadedChunk("));assertTrue(reader.contains("BiomeColumnAccess"));
        assertFalse(reader.contains("getBiomeProvider("));assertFalse(reader.contains("getChunkFromBlockCoords("));
        String mini=source("integration/xaero/XaeroMinimapInfoBridge.java");
        assertTrue(mini.contains("InfoDisplayCommonWidgetFactories.OFF_ON"));assertTrue(mini.contains("BuiltInConfigValueIOCodecs.BOOLEAN"));
        assertTrue(mini.contains("applyLocalConfig()"));assertTrue(mini.contains(".addLine("));
        int callback=mini.indexOf("private void compile(");
        assertTrue(mini.indexOf("try {",callback)<mini.indexOf("getEffectiveState()",callback));
        assertFalse(mini.contains("getOverMapRendererHandler("));assertFalse(mini.contains(".regions("));
        assertTrue(source("client/map/MapIntegrationController.java").contains("XaeroMinimapInfoBridge"));
    }
    @Test public void coreCollectorNeverForcesChunksOrUsesBiomeProviderFallback() throws Exception {
        String controller=source("client/map/MapIntegrationController.java");
        assertTrue(controller.contains("getLoadedChunk("));
        assertFalse(controller.contains(".loadChunk("));assertFalse(controller.contains("getBiomeProvider("));
        assertFalse(controller.contains("provideChunk("));
        assertTrue(controller.contains("RegionWindow.contains("));
        assertTrue(controller.contains("bridges[index]!=null"));
        assertTrue(controller.contains("BiomeRegionCandidates.nearest("));
    }
    @Test public void painterRestoresItsOwnMatrixAndDoesNotUseWarningEffects() throws Exception {
        String painter=source("client/map/MapLabelPainter.java");
        assertTrue(painter.contains("finally"));assertTrue(painter.contains("popMatrix"));
        assertFalse(painter.contains("WarningPulse"));assertFalse(painter.contains("playSound"));
    }
}
