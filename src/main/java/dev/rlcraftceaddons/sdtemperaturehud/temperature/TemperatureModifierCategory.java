/*
 * Decompiled with CFR 0.152.
 */
package dev.rlcraftceaddons.sdtemperaturehud.temperature;

import java.util.Locale;

public enum TemperatureModifierCategory {
    ENVIRONMENT,
    EQUIPMENT,
    SEASON,
    HEAT_SOURCE,
    OTHER;


    public static TemperatureModifierCategory forName(String modifierName) {
        String normalized;
        if (modifierName == null) {
            return OTHER;
        }
        switch (normalized = modifierName.toLowerCase(Locale.ROOT)) {
            case "default": 
            case "altitude": 
            case "biome": 
            case "dimension": 
            case "snow": 
            case "time": {
                return ENVIRONMENT;
            }
            case "armor": 
            case "helditems": 
            case "baubles": 
            case "auw": 
            case "auwdynamic": {
                return EQUIPMENT;
            }
            case "sereneseasons": 
            case "harvestfestival": {
                return SEASON;
            }
            case "blockstiles": {
                return HEAT_SOURCE;
            }
        }
        return OTHER;
    }
}

