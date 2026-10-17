package dev.rlcraftceaddons.sdtemperaturehud.integration.xaero;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

/** Contains no Xaero type references: safe to load without either map installed. */
public final class XaeroIntegration {
    private XaeroIntegration() {}
    /** Region overlays are on hold; the heading reads only Xaero's already-visible data. */
    public static boolean areaSamplingEnabled() {return false;}
    public static boolean supported(boolean minimap,String version) {
        return minimap ? "26.5.1".equals(version) || "26.6.0".equals(version)
            : "1.46.0".equals(version) || "1.47.0".equals(version);
    }
    public static String status(boolean minimap) {
        ModContainer mod=Loader.instance().getIndexedModList().get(minimap?"xaerominimap":"xaeroworldmap");
        ModContainer library=Loader.instance().getIndexedModList().get("xaerolib");
        return status(minimap,mod==null?null:mod.getVersion(),library==null?null:library.getVersion());
    }
    static String status(boolean minimap,String mapVersion,String libraryVersion) {
        if(mapVersion==null)return "missing";
        return supported(minimap,mapVersion) && "1.7.3".equals(libraryVersion)
            ? "ready" : "unsupported";
    }
}
