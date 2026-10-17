package dev.rlcraftceaddons.sdtemperaturehud.integration.xaero;

import java.lang.reflect.Method;
import org.junit.Test;
import static org.junit.Assert.*;

public class NativeBiomeHeadingNameTest {
    public static final class Option {
        boolean enabled;
        public boolean getBooleanValue(){return enabled;}
    }
    public static final class Translator {
        public static String translate(Object biome){return "\u51bb\u6d0b";}
        public static String fail(Object biome){throw new IllegalStateException("unsupported translation");}
    }
    @Test public void followsTheSameOptionalHeadingTranslatorAndLiveToggle() throws Exception {
        Option option=new Option();Method enabled=Option.class.getMethod("getBooleanValue");
        Method translate=Translator.class.getMethod("translate",Object.class);
        assertEquals("FrozenOcean",NativeBiomeHeadingName.resolve("FrozenOcean",new Object(),option,enabled,translate));
        option.enabled=true;
        assertEquals("\u51bb\u6d0b",NativeBiomeHeadingName.resolve("FrozenOcean",new Object(),option,enabled,translate));
        option.enabled=false;
        assertEquals("FrozenOcean",NativeBiomeHeadingName.resolve("FrozenOcean",new Object(),option,enabled,translate));
    }
    @Test public void anUnusableTranslationNeverClaimsTheWrongNameWidth() throws Exception {
        Option option=new Option();option.enabled=true;
        assertNull(NativeBiomeHeadingName.resolve("FrozenOcean",new Object(),option,Option.class.getMethod("getBooleanValue"),
            Translator.class.getMethod("fail",Object.class)));
    }
}
