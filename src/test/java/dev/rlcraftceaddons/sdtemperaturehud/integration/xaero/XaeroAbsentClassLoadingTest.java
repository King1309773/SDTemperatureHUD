package dev.rlcraftceaddons.sdtemperaturehud.integration.xaero;

import java.net.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class XaeroAbsentClassLoadingTest {
    @Test public void entryAndControllerCanLinkWithEveryXaeroClassHidden() throws Exception {
        URL classes= XaeroIntegration.class.getProtectionDomain().getCodeSource().getLocation();
        try(URLClassLoader loader=new URLClassLoader(new URL[]{classes},getClass().getClassLoader()) {
            @Override protected synchronized Class<?> loadClass(String name,boolean resolve) throws ClassNotFoundException {
                if(name.startsWith("xaero."))throw new ClassNotFoundException("hidden optional dependency");
                if(name.startsWith("dev.rlcraftceaddons.sdtemperaturehud.")) {
                    Class<?> type=findLoadedClass(name);if(type==null)type=findClass(name);
                    if(resolve)resolveClass(type);return type;
                }
                return super.loadClass(name,resolve);
            }
        }) {
            for(String name:new String[]{"integration.xaero.XaeroIntegration","client.map.MapIntegrationController","SDTemperatureHud"}) {
                Class<?> type=Class.forName("dev.rlcraftceaddons.sdtemperaturehud."+name,false,loader);
                assertNotNull(type.getDeclaredMethods());assertNotNull(type.getDeclaredFields());
            }
        }
    }
}
