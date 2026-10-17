package dev.rlcraftceaddons.sdtemperaturehud.integration.xaero;

import dev.rlcraftceaddons.sdtemperaturehud.SDTemperatureHud;
import java.lang.reflect.Method;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.Loader;

/** Match the optional public translator used by the installed GuiMap name wrapper. */
public final class NativeBiomeHeadingName {
    private boolean resolved,present,unavailable;
    private Object option;
    private Method enabled,translate;
    public String name(Biome biome) {
        if(biome==null)return null;
        String original=biome.getBiomeName();
        if(!resolved)bind();
        if(!present)return original;
        if(unavailable)return null;
        return resolve(original,biome,option,enabled,translate);
    }
    private void bind() {
        resolved=true;present=Loader.isModLoaded("debrisclient");if(!present)return;
        try {
            Class<?> config=Class.forName("com.github.debris.debrisclient.config.DCConfig");
            option=config.getField("XaeroTranslateBiome").get(null);
            enabled=option.getClass().getMethod("getBooleanValue");
            translate=Class.forName("com.github.debris.debrisclient.util.StringUtil").getMethod("translateBiome",Biome.class);
        }catch(ReflectiveOperationException | RuntimeException | LinkageError problem) {
            unavailable=true;
            SDTemperatureHud.LOGGER.warn("Map heading temperature skipped: optional biome heading translator unavailable",problem);
        }
    }
    static String resolve(String original,Object biome,Object option,Method enabled,Method translate) {
        try {
            if(!Boolean.TRUE.equals(enabled.invoke(option)))return original;
            Object name=translate.invoke(null,biome);return name instanceof String?(String)name:null;
        }catch(ReflectiveOperationException | RuntimeException | LinkageError problem){return null;}
    }
}
