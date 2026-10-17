package dev.rlcraftceaddons.sdtemperaturehud.client.theme;

import java.util.Locale;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HexColorParserTest {
    @Test
    public void parsesHashPrefixedAndBareRgbInEitherCase() {
        assertEquals(0x12ABEF, HexColorParser.parse("#12abEF"));
        assertEquals(0xA0B1C2, HexColorParser.parse("a0B1c2"));
    }

    @Test
    public void formatsUppercaseRgbAndMasksAlpha() {
        assertEquals("#12ABEF", HexColorParser.format(0x7F12ABEF));
        assertEquals("#00000A", HexColorParser.format(0x00000A));
    }

    @Test
    public void formatsConsistentlyUnderANonEnglishDefaultLocale() {
        Locale originalLocale = Locale.getDefault();
        try {
            Locale.setDefault(new Locale("ar", "EG"));
            assertEquals("#ABCDEF", HexColorParser.format(0xABCDEF));
        } finally {
            Locale.setDefault(originalLocale);
        }
    }

    @Test
    public void rejectsInvalidValues() {
        assertInvalid(null);
        assertInvalid("");
        assertInvalid("12345");
        assertInvalid("1234567");
        assertInvalid("#12345");
        assertInvalid("#1234567");
        assertInvalid(" 123456");
        assertInvalid("123456 ");
        assertInvalid("#80123456");
        assertInvalid("80123456");
        assertInvalid("#12G45Z");
        assertInvalid("\uFF11\uFF12\uFF13\uFF14\uFF15\uFF16");
    }

    private static void assertInvalid(String value) {
        try {
            HexColorParser.parse(value);
        } catch (IllegalArgumentException expected) {
            return;
        }
        throw new AssertionError("Expected IllegalArgumentException for: " + value);
    }
}
