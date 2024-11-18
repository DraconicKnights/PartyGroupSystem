package com.draconincdomain.partygroup.Utils;

import net.kyori.adventure.text.format.TextColor;

/**
 * Custom Colour utility object for dealing with colours in using Velocities API
 */
public class ColourUtil {
    public enum CustomColour {
        GREEN(0x00FF00),
        RED(0xFF0000),
        BLUE(0x0000FF),
        GOLD(0xFFD700),
        DARK_RED(0x8B0000),
        DARK_BLUE(0x00008B),
        REG_GREEN(0x00AA00),
        YELLOW(0xFFFF00),
        LIGHT_PURPLE(0xFFC0CB),
        DARK_PURPLE(0xAA00AA),
        AQUA(0x00FFFF),
        DARK_AQUA(0x008B8B),
        ORANGE(0xFFA500),
        WHITE(0xFFFFFF),
        BLACK(0x000000),
        LIGHT_BLUE(0xADD8E6),
        PINK(0xFFC0CB),
        GREY(0x808080);

        private final int rgbValue;

        CustomColour(int rgbValue) {
            this.rgbValue = rgbValue;
        }

        public TextColor getTextColour() {
            return TextColor.color(rgbValue);
        }

        public int getRgbValue() {
            return rgbValue;
        }
    }

    public static TextColor fromRGB(int red, int green, int blue) {
        return TextColor.color(red, green, blue);
    }

    public static TextColor fromEnum(CustomColour colour) {
        return colour.getTextColour();
    }
}
