package com.erwandano.fsdiff.diffwindow;

import javafx.scene.paint.Color;

/**
 * Colors used in the interface
 */

public enum CssColor {

    MATCHED("grey", "#808080", 0.5),
    MODIFIED("goldenrod", "#daa520", 0.5),
    CREATED("green", "#008000", 0.5),
    DELETED("red", "#ff0000", 0.5),
    ERROR("red", "#ff0000", 0.5),
    LIGHT_PILL_LABEL("ivory", "#fffff0", 0),
    DARK_PILL_LABEL("black", "#000000", 0);

    private String name;
    private String backgroundColor;
    private String textColor;
    private double opacity;

    CssColor(String name,
             String color,
             double opacity) {
        this.name = name;
        this.backgroundColor = color;
        this.opacity = opacity;
        this.textColor = "#050709";
    }

    CssColor(String name,
             String color,
             double opacity,
             String textColor) {
        this.name = name;
        this.backgroundColor = color;
        this.opacity = opacity;
        this.textColor = textColor;
    }

    public String getName() {
        return name;
    }

    public String getBackgroundHexColor() {
        return backgroundColor;
    }

    public double getOpacity() {
        return opacity;
    }

    public String getTextHexColor() {
        return textColor;
    }

    public Color getTextColor() {
        return Color.valueOf(textColor);
    }

    public String getRgba(String color, double opacity) {
        Color c = Color.valueOf(color);
        int red = (int) (c.getRed() * 255);
        int green = (int) (c.getGreen() * 255);
        int blue = (int) (c.getBlue() * 255);
        return "rgba(" + red + ", " + green + ", " + blue + ", " + opacity + ")";
    }

    public String getTextRgba() {
        return getRgba(textColor, 1);
    }

    public String getBackgroundRgba() {
        return getRgba(backgroundColor, opacity);
    }
}
