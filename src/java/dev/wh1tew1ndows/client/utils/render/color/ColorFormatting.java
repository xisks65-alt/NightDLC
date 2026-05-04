package dev.wh1tew1ndows.client.utils.render.color;

import lombok.experimental.UtilityClass;
import net.minecraft.util.text.Color;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class ColorFormatting {

    private static final HashMap<String, String> formats = new HashMap<String, String>() {{
        put("${black}", TextFormatting.BLACK + "");
        put("${dark_blue}", TextFormatting.DARK_BLUE + "");
        put("${dark_green}", TextFormatting.DARK_GREEN + "");
        put("${dark_aqua}", TextFormatting.DARK_AQUA + "");
        put("${dark_red}", TextFormatting.DARK_RED + "");
        put("${dark_purple}", TextFormatting.DARK_PURPLE + "");
        put("${orange}", TextFormatting.GOLD + "");
        put("${gray}", TextFormatting.GRAY + "");
        put("${dark_gray}", TextFormatting.DARK_GRAY + "");
        put("${blue}", TextFormatting.BLUE + "");
        put("${green}", TextFormatting.GREEN + "");
        put("${aqua}", TextFormatting.AQUA + "");
        put("${red}", TextFormatting.RED + "");
        put("${purple}", TextFormatting.LIGHT_PURPLE + "");
        put("${yellow}", TextFormatting.YELLOW + "");
        put("${white}", TextFormatting.WHITE + "");
        put("${bold}", TextFormatting.BOLD + "");
        put("${reset}", TextFormatting.RESET + "");
    }};

    public static String get(String input) {
        String finish = input;
        for (Map.Entry<String, String> format : formats.entrySet()) {
            finish = finish.replace(format.getKey(), format.getValue());
        }
        return finish;
    }

    public Pattern PATTERN = Pattern.compile("\\$\\{(rgba|rgb)\\((\\d{1,3}),(\\d{1,3}),(\\d{1,3})(?:,(\\d{1,3}))?\\)}|\\$\\{reset}", Pattern.CASE_INSENSITIVE);

    public String getColor(int red, int green, int blue) {
        return String.format("${rgb(%s,%s,%s)}", red, green, blue);
    }

    public String getColor(int red, int green, int blue, int alpha) {
        return String.format("${rgba(%s,%s,%s,%s)}", red, green, blue, alpha);
    }

    public static StringTextComponent gradient(String message) {

        StringTextComponent text = new StringTextComponent("");
        for (int i = 0; i < message.length(); i++) {
            text.append(new StringTextComponent(String.valueOf(message.charAt(i))).setStyle(Style.EMPTY.setColor(new Color(ColorUtil.fade()))));
        }

        return text;

    }

    public String getColor(int color) {
        return String.format("${rgba(%s,%s,%s,%s)}", ColorUtil.red(color), ColorUtil.green(color), ColorUtil.blue(color), ColorUtil.alpha(color));
    }

    public int overColor(int firstColor, int secondColor, float factorPercent) {
        return ColorUtil.overCol(firstColor, secondColor, factorPercent);
    }

    public String reset() {
        return "${reset}";
    }

    public String removeFormatting(String text) {
        return PATTERN.matcher(text).replaceAll("");
    }

    public String typeRGB() {
        return "rgb";
    }

    public String typeRGBA() {
        return "rgba";
    }

    public String replaceColor(String text, int red, int green, int blue) {
        return PATTERN.matcher(text).replaceAll(Matcher.quoteReplacement(getColor(red, green, blue)));
    }

    public String replaceColor(String text, int red, int green, int blue, int alpha) {
        return PATTERN.matcher(text).replaceAll(Matcher.quoteReplacement(getColor(red, green, blue, alpha)));
    }
}