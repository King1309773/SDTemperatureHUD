package dev.rlcraftceaddons.sdtemperaturehud.integration.xaero;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import net.minecraft.util.text.TextComponentString;
import org.junit.Test;
import static org.junit.Assert.*;
import xaero.hud.minimap.info.*;
import xaero.hud.minimap.common.config.info.config.*;
import xaero.hud.minimap.info.widget.InfoDisplayCommonWidgetFactories;
import xaero.lib.common.config.option.value.io.serialization.BuiltInConfigValueIOCodecs;

public class XaeroInfoRegistrationTest {
    private InfoDisplay<Boolean> display(String id) {
        return InfoDisplay.Builder.<Boolean>begin().setId(id).setName(new TextComponentString(id))
            .setDefaultState(true).setCodec(BuiltInConfigValueIOCodecs.BOOLEAN)
            .setWidgetFactory(InfoDisplayCommonWidgetFactories.OFF_ON).setCompiler((a,b,c,d,e)->{}).build();
    }
    private InfoDisplayManager manager(AtomicReference<InfoDisplayManagerConfigData> config) {
        List<String> defaults=new ArrayList<String>();
        return new InfoDisplayManager(new LinkedHashMap<String,InfoDisplay<?>>(),defaults,Collections.unmodifiableList(defaults),
            config::get,config::set,()->InfoDisplayManagerConfigData.EMPTY);
    }
    @Test public void realNativeRegistrationPreservesExistingOrderAndStyleData() {
        Map<String,InfoDisplayConfigData> values=new HashMap<String,InfoDisplayConfigData>();
        InfoDisplayConfigData preference=new InfoDisplayConfigData(5,11,"false");values.put("b",preference);
        InfoDisplayManagerConfigData original=new InfoDisplayManagerConfigData(Arrays.asList("b","a"),values);
        AtomicReference<InfoDisplayManagerConfigData> config=new AtomicReference<InfoDisplayManagerConfigData>(original);
        InfoDisplayManager manager=manager(config);manager.add(display("a"));manager.add(display("b"));
        InfoDisplay<Boolean> entry=display("sdtemperaturehud_biome_temperature");
        XaeroMinimapInfoBridge.ensureRegistered(manager,entry);
        XaeroMinimapInfoBridge.ensureRegistered(manager,display(entry.getId()));
        assertEquals(3,manager.getCount());assertSame(entry,manager.get(entry.getId()));
        assertEquals(1,manager.getDefaultOrder().stream().filter(entry.getId()::equals).count());
        assertEquals(Arrays.asList("b","a"),manager.getOrderedStream().map(InfoDisplay::getId)
            .filter(id->!id.equals(entry.getId())).collect(Collectors.toList()));
        assertSame(original,config.get());assertSame(preference,config.get().get("b"));
    }
    @Test public void realNativeProfileToggleControlsTheRegisteredEntry() {
        String id="sdtemperaturehud_biome_temperature";
        AtomicReference<InfoDisplayManagerConfigData> config=new AtomicReference<InfoDisplayManagerConfigData>(
            new InfoDisplayManagerConfigData(Collections.singletonList(id),Collections.singletonMap(id,new InfoDisplayConfigData(null,null,"false"))));
        InfoDisplayManager manager=manager(config);InfoDisplay<Boolean> entry=display(id);
        XaeroMinimapInfoBridge.ensureRegistered(manager,entry);assertFalse(entry.getEffectiveState());
        config.set(new InfoDisplayManagerConfigData(Collections.singletonList(id),Collections.singletonMap(id,new InfoDisplayConfigData(null,null,"true"))));
        manager.applyLocalConfig();manager.clearStateCache();assertTrue(entry.getEffectiveState());
        XaeroMinimapInfoBridge.ensureRegistered(manager,display(id));
        assertSame(entry,manager.get(id));assertTrue(entry.getEffectiveState());
    }
}
