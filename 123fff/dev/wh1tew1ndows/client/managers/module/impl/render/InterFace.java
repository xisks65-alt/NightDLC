package dev.wh1tew1ndows.client.managers.module.impl.render;

import dev.wh1tew1ndows.client.api.annotations.Client;
import dev.wh1tew1ndows.client.api.events.orbit.EventHandler;
import dev.wh1tew1ndows.client.api.events.orbit.EventPriority;
import dev.wh1tew1ndows.client.managers.events.player.AttackEvent;
import dev.wh1tew1ndows.client.managers.events.render.Render2DEvent;
import dev.wh1tew1ndows.client.managers.module.Category;
import dev.wh1tew1ndows.client.managers.module.Module;
import dev.wh1tew1ndows.client.managers.module.ModuleInfo;
import dev.wh1tew1ndows.client.managers.module.settings.impl.*;
import dev.wh1tew1ndows.client.screen.hud.impl.*;
import dev.wh1tew1ndows.client.utils.math.Interpolator;
import dev.wh1tew1ndows.client.utils.math.Mathf;
import dev.wh1tew1ndows.client.utils.other.Instance;
import dev.wh1tew1ndows.client.utils.render.color.ColorUtil;
import dev.wh1tew1ndows.client.utils.render.draw.RenderUtil;
import dev.wh1tew1ndows.client.utils.render.draw.Round;
import dev.wh1tew1ndows.client.utils.render.shader.impl.BlurShader;
import dev.wh1tew1ndows.client.utils.ПенисУтилита;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.util.math.MathHelper;
import net.mojang.blaze3d.matrix.MatrixStack;

import java.awt.*;

@Getter
@Accessors(fluent = true)
@Client
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "InterFace", category = Category.RENDER, desc = "Основной интерфейс клиента и HUD")
public class InterFace extends Module {
    public static InterFace getInstance() {
        return Instance.get(InterFace.class);
    }

    private final WatermarkRenderer watermarkRenderer;
    private final TargetHudRenderer targetHudRenderer;
    private final KeybindsRenderer keybindsRenderer;
    private final PotionsRenderer potionsRenderer;
    private final ArmorHudRenderer armorHudRenderer;
    private final CooldownRenderer cooldownRenderer;
    private final StaffListRenderer staffListRenderer;
    private final FriendsRenderer friendsRenderer;
    private final AutoSwapRenderer autoSwapRenderer;

    //private final ArrayListRenderer arrayListRenderer;
    //private final BindSettingsRenderer bindSettingsRenderer;
    public final MultiBooleanSetting checks = new MultiBooleanSetting(this, "Отображать",
            BooleanSetting.of("Ватер марк", true),
            BooleanSetting.of("Таргет худ", true),
            BooleanSetting.of("Активные бинды", true),
            BooleanSetting.of("Активные зелье", true),
            BooleanSetting.of("Броня", true),
            BooleanSetting.of("Задержка", true),
            BooleanSetting.of("Стафф лист", true),
            BooleanSetting.of("Друзья", true),
            BooleanSetting.of("AutoSwap", true)
    );

    public final BooleanSetting colorHP = new BooleanSetting(this, "Окрашивать полосу здоровья под ХП противника", false).setVisible(() -> checks.getValue("Таргет худ"));

    private final DelimiterSetting watermarkDelimiter3 = new DelimiterSetting(this, "Настройки ректа");

    public final BooleanSetting blur = new BooleanSetting(this, "Блюр", false);
    public final BooleanSetting shadow = new BooleanSetting(this, "Тень", false).setVisible(() -> blur.getValue());

    public final SliderSetting blurPC = new SliderSetting(this, "Сила блюра", 2, 1, 3, 1).setVisible(() -> blur.getValue());

    private final DelimiterSetting watermarkDelimiter4 = new DelimiterSetting(this, "Настройки цвета");



    private final ModeSetting theme = new ModeSetting(this, "Тема", "Ледяная сирень",
            "Лиловый сумрак", "Чистое небо",
            "Песчаное тепло", "Пудровая роза", "Закатный коралл", "JShine", "Своя");

    private final ColorSetting color1 = new ColorSetting(this, "1 цвет").setVisible(() -> theme.is("Своя"));

    private final ColorSetting color2 = new ColorSetting(this, "2 цвет").setVisible(() -> theme.is("Своя"));

    private final SliderSetting speed = new SliderSetting(this, "Скорость темы", 10, 5, 30, 1);


    private final DragSetting targetHudRendererDrag = new DragSetting(this, "TargetHud");
    private final DragSetting keybindsRendererDrag = new DragSetting(this, "Keybinds");
    private final DragSetting potionsRendererDrag = new DragSetting(this, "Potions");
    private final DragSetting staffDrag = new DragSetting(this, "staff33");
    private final DragSetting friendsDrag = new DragSetting(this, "friends33");

    private final DragSetting coldowbDrag = new DragSetting(this, "coldowns33");

    public InterFace() {
        targetHudRenderer = new TargetHudRenderer(targetHudRendererDrag);
        keybindsRenderer = new KeybindsRenderer(keybindsRendererDrag);
        potionsRenderer = new PotionsRenderer(potionsRendererDrag);
        cooldownRenderer = new CooldownRenderer(coldowbDrag);
        staffListRenderer = new StaffListRenderer(staffDrag);
        friendsRenderer = new FriendsRenderer(friendsDrag);
        watermarkRenderer = new WatermarkRenderer();
        armorHudRenderer = new ArmorHudRenderer();
        autoSwapRenderer = new AutoSwapRenderer();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEvent(Render2DEvent event) {
        if (checks.getValue("Ватер марк")) watermarkRenderer.render(event);
        if (checks.getValue("Броня")) armorHudRenderer.render(event);
        if (checks.getValue("Таргет худ")) targetHudRenderer.render(event);
        if (checks.getValue("Активные бинды")) keybindsRenderer.render(event);
        // if (checks.getValue("Кей бинд в модулех")) bindSettingsRenderer.render(event);
        if (checks.getValue("Активные зелье")) potionsRenderer.render(event);
        if (checks.getValue("Задержка")) cooldownRenderer.render(event);
        if (checks.getValue("Стафф лист")) staffListRenderer.render(event);
        if (checks.getValue("Друзья")) friendsRenderer.render(event);
        if (checks.getValue("AutoSwap")) autoSwapRenderer.render(event);
        // if (checks.getValue("Список модулей")) arrayListRenderer.render(event);
    }


    @EventHandler
    public void onEvent(AttackEvent event) {
        if (checks.getValue("Таргет худ")) targetHudRenderer.attack(event);
    }


    public int ThemeTextHud() {

         return ColorUtil.getColor(220);
    }

    public int themeAccent(int index) {
        switch (theme.getValue()) {
            case "Ледяная сирень":
                return new Color(160, 176, 239).getRGB();
            case "Лиловый сумрак":
                return new Color(156, 138, 223).getRGB();
            case "Чистое небо":
                return new Color(127, 186, 221).getRGB();
            case "Песчаное тепло":
                return new Color(214, 212, 168).getRGB();
            case "Пудровая роза":
                return new Color(221, 149, 177).getRGB();
            case "Закатный коралл":
                return new Color(221, 127, 129).getRGB();
            case "Виноград":
                return ColorUtil.fade(index, speed.getValue().intValue(), new Color(0x928DAB).getRGB(), new Color(0x1F1C2C).getRGB());
            case "JShine":
                return ColorUtil.skyRainbow(speed.getValue().intValue(), index);

        }

        return 0;
    }


    public int accent(int index, float alpha) {
        Integer solo = themeColor();

        int result;

        result = solo;

        return ColorUtil.replAlpha(result, alpha);
    }

    public int surface() {
        return ColorUtil.overCol(accent(0, 0), ColorUtil.BLACK, 0.93f);
    }

    public int surface(int index, float alpha) {
        return ColorUtil.overCol(accent(index, alpha), ColorUtil.BLACK, 0.93f);
    }

    public int accent(float alpha) {
        return accent(0, alpha);
    }

    public int surface(float alpha) {
        return ColorUtil.replAlpha(surface(), alpha);
    }

    public void drawClientRect(MatrixStack matrix, float x, float y, float width, float height, float alpha, float radius) {

        x = (float) Mathf.step(x, 0.5);
        y = (float) Mathf.step(y, 0.5);
        width = (float) Mathf.step(width, 0.5);
        height = (float) Mathf.step(height, 0.5);

        //if (sicretNastriokaEbana.getValue()) {
            ПенисУтилита.ЕбатьПенка(matrix, x, y, width, height, alpha, radius);


            ///} else {
            ///    if (InterFace.getInstance().blur.getValue()) {
                ///        if (shadow.getValue()) {
                    ///            RenderUtil.Shadow.drawShadow(matrix, x - radius / 2, y - radius / 2, width + radius, height + radius, 12, ColorUtil.replAlpha(ColorUtil.getColor(0), (float) Math.pow(alpha, 3)));
                    ///        }
                ///        RenderUtil.bindTexture(BlurShader.INSTANCE.getBuffer().framebufferTexture);
                ///        RenderUtil.Texture.customRound(matrix, RenderUtil.Texture.ShaderType.BLUR, x, y, width, height, alpha, 0, 0, 0, 0, Round.of(radius));
                ///    }

            ///    RenderUtil.Rounded.smooth(matrix, x, y, width, height, ColorUtil.replAlpha(rectColor.is("Свой") ? colorRect.getValue() : ColorUtil.multDark(themeColor(), 0.1F), alpha * alphaPC.getValue()), Round.of(radius));
            ///}
        //RenderUtil.Rounded.roundedOutline(matrix, x, y, width, height, 1, ColorUtil.replAlpha(-1, alpha * 0.05F), Round.of(radius).sub(0.5F));
    }

    public void drawClientRect(MatrixStack matrix, float x, float y, float width, float height, float alpha) {
        drawClientRect(matrix, x, y, width, height, alpha, 5);
    }

    public void drawClientRect(MatrixStack matrix, float x, float y, float width, float height) {
        drawClientRect(matrix, x, y, width, height, 1F, 4);
    }

    public int themeColor() {
        if (theme.is("Своя")) {
            return ColorUtil.fade(speed.getValue().intValue(), 5, color1.getValue(), color2.getValue());
        }

        return themeAccent(1);
    }

    public int themeColor(int intex) {
        if (theme.is("Своя")) {
            return ColorUtil.fade(speed.getValue().intValue(), intex, color1.getValue(), color2.getValue());
        }

        return ColorUtil.fade(speed.getValue().intValue(), intex, themeAccent(intex), themeAccent(intex));
    }

    public static int gradient(int color1, int color2, int speed, int index) {
        Color col1 = new Color(color1);
        Color col2 = new Color(color2);

        double angle = (System.currentTimeMillis() / speed + index) % 360;
        float ratio = (float) ((angle %= 360) / 360.0);

        int red = (int) (col1.getRed() * (1 - ratio) + col2.getRed() * ratio);
        int green = (int) (col1.getGreen() * (1 - ratio) + col2.getGreen() * ratio);
        int blue = (int) (col1.getBlue() * (1 - ratio) + col2.getBlue() * ratio);

        Color interpolatedColor = new Color(red, green, blue);

        return interpolatedColor.getRGB();
    }

    public static int interpolate(int color1, int color2, double amount) {
        amount = (float) MathHelper.clamp(amount, 0, 1);
        return ColorUtil.getColor(
                Interpolator.lerp(ColorUtil.red(color1), ColorUtil.red(color2), amount),
                Interpolator.lerp(ColorUtil.green(color1), ColorUtil.green(color2), amount),
                Interpolator.lerp(ColorUtil.blue(color1), ColorUtil.blue(color2), amount),
                Interpolator.lerp(ColorUtil.alpha(color1), ColorUtil.alpha(color2), amount)
        );
    }


    public int clientColor() {
        return themeColor();
    }


    public int backgroundColor() {

        return ColorUtil.multDark(accent(0, 1), 0.1F);
    }


    public int backgroundColorBg() {

        return ColorUtil.multDark(accent(0, 1), 0.15F);
    }


    public int textColor() {

        return ColorUtil.multDark(accent(0, 1), 0.9F);
    }

    public int themeColor2() {

        return ColorUtil.multDark(accent(0, 1), 0.5F);
    }

    public int textAccentColor() {

        return ColorUtil.multDark(accent(0, 1), 0.7F);
    }

    public int iconColor() {

        return ColorUtil.multDark(accent(0, 1), 0.9F);
    }

    public int darkColor() {

        return ColorUtil.multDark(themeColor(), 0.25F);
    }

    public int getSpeed() {
        return 10;
    }


}
