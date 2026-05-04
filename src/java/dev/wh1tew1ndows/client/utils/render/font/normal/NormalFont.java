package dev.wh1tew1ndows.client.utils.render.font.normal;

import it.unimi.dsi.fastutil.floats.Float2ObjectArrayMap;
import it.unimi.dsi.fastutil.floats.Float2ObjectMap;


public enum NormalFont {

    MEDIUM("Roboto-Medium.ttf"),
    SEMIBOLD("Roboto-SemiBold.ttf");

    private final String file;
    private final Float2ObjectMap<Font> fontMap = new Float2ObjectArrayMap<>();

    public String getFile() {
        return file;
    }

    public Float2ObjectMap<Font> getFontMap() {
        return fontMap;
    }

    NormalFont(String file) {
        this.file = file;
    }

    public Font get(float size) {
        return fontMap.computeIfAbsent(size, font -> {
            try {
                return Font.create(getFile(), size);
            } catch (Exception e) {
                throw new RuntimeException("Unable to load font: " + this, e);
            }
        });
    }

}
