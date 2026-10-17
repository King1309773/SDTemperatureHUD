package dev.rlcraftceaddons.sdtemperaturehud.client.config;

import dev.rlcraftceaddons.sdtemperaturehud.SDTemperatureHud;
import net.minecraftforge.fml.client.IModGuiFactory;
import net.minecraftforge.fml.common.Mod;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class GuiFactoryPolicyTest {
    @Test
    public void forgeRegistrationSelectsCustomConfigFactoryInsteadOfDefaultConfigScreen() throws Exception {
        Mod registration = SDTemperatureHud.class.getAnnotation(Mod.class);
        assertNotNull(registration);
        assertEquals("Forge reads guiFactory from @Mod, not mcmod.info",
            SDTemperatureHudGuiFactory.class.getName(), registration.guiFactory());
        Class<?> factoryType = Class.forName(registration.guiFactory());
        assertTrue(IModGuiFactory.class.isAssignableFrom(factoryType));
    }

    @Test
    public void factoryAdvertisesConfigGuiAndNoRuntimeCategories() {
        IModGuiFactory factory = new SDTemperatureHudGuiFactory();
        Set<IModGuiFactory.RuntimeOptionCategoryElement> categories =
            factory.runtimeGuiCategories();

        assertTrue(factory.hasConfigGui());
        assertTrue(categories.isEmpty());
    }

    @Test
    public void fixedFooterStaysBelowPageControls() {
        SettingsScreenLayout layout = SettingsScreenLayout.forScreen(320, 240);
        assertTrue(layout.rowY(layout.rows() - 1) + 20 < layout.pagerY);
        assertTrue(layout.pagerY + 20 < layout.footerY);
    }
}
