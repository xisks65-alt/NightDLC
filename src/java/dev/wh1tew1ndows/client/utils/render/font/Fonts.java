package dev.wh1tew1ndows.client.utils.render.font;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Field;

@Getter
@RequiredArgsConstructor
public class Fonts {
    public static Font
            SF_BOLD,
            SF_MEDIUM,
            SFP_BOLD,
            SFP_MEDIUM,
            SFP_SEMIBOLD,
            SFP_REGULAR,
            MONTSERRAT_BOLD,
            MONTSERRAT_MEDIUM,
            ICON_DIMAS,
            ICON_DESHUX,
            ICON_NURIK,
            ICON_ZENIT,
            ICON_V1,
            ICON_ESSENS;

    public static void loadFonts() {
        SF_BOLD           = new Font("main_bold");
        SF_MEDIUM         = new Font("main_normal");
        SFP_BOLD          = new Font("sf_bold");
        SFP_MEDIUM        = new Font("sf_medium");
        SFP_SEMIBOLD      = new Font("sf_semibold");
        SFP_REGULAR       = new Font("sf_regular");
        MONTSERRAT_BOLD   = new Font("montserrat_bold");
        MONTSERRAT_MEDIUM = new Font("montserrat_medium");
        ICON_V1           = new Font("icon");
        ICON_ZENIT        = new Font("iconZ");
        ICON_DIMAS        = new Font("icon_dimas");
        ICON_DESHUX       = new Font("icon_deshux");
        ICON_NURIK        = new Font("icons");
        ICON_ESSENS       = new Font("icoes");
    }

    public static Font valueOf(String name) {
        try {
            Field field = Fonts.class.getDeclaredField(name.toUpperCase());
            return (Font) field.get(null);
        } catch (NoSuchFieldException | IllegalAccessException exception) {
            throw new IllegalArgumentException("Font not found: " + name, exception);
        }
    }
}
