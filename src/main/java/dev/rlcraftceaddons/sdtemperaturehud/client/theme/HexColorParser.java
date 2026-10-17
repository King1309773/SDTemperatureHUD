package dev.rlcraftceaddons.sdtemperaturehud.client.theme;

import java.util.Locale;

public final class HexColorParser {
    private HexColorParser() {
    }

    public static int parse(String value) {
        if (value == null) {
            throw new IllegalArgumentException("color must not be null");
        }
        String digits;
        if (value.length() == 7 && value.charAt(0) == '#') {
            digits = value.substring(1);
        } else if (value.length() == 6) {
            digits = value;
        } else {
            throw new IllegalArgumentException("color must use RRGGBB or #RRGGBB");
        }
        int color = 0;
        for (int index = 0; index < digits.length(); index++) {
            int digit = hexDigit(digits.charAt(index));
            if (digit < 0) {
                throw new IllegalArgumentException("color contains a non-hexadecimal digit");
            }
            color = color << 4 | digit;
        }
        return color;
    }

    public static String format(int color) {
        return String.format(Locale.ROOT, "#%06X", color & 0xFFFFFF);
    }

    private static int hexDigit(char character) {
        if (character >= '0' && character <= '9') {
            return character - '0';
        }
        if (character >= 'A' && character <= 'F') {
            return character - 'A' + 10;
        }
        if (character >= 'a' && character <= 'f') {
            return character - 'a' + 10;
        }
        return -1;
    }
}
