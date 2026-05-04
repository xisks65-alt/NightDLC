package dev.wh1tew1ndows.client.managers.module.impl.render;

import dev.wh1tew1ndows.client.api.events.orbit.EventHandler;
import dev.wh1tew1ndows.client.managers.events.render.ItemAnimationEvent;
import dev.wh1tew1ndows.client.managers.events.render.RenderItemEvent;
import dev.wh1tew1ndows.client.managers.module.Category;
import dev.wh1tew1ndows.client.managers.module.Module;
import dev.wh1tew1ndows.client.managers.module.ModuleInfo;
import dev.wh1tew1ndows.client.managers.module.impl.combat.AttackAura;
import dev.wh1tew1ndows.client.managers.module.settings.impl.BooleanSetting;
import dev.wh1tew1ndows.client.managers.module.settings.impl.DelimiterSetting;
import dev.wh1tew1ndows.client.managers.module.settings.impl.ModeSetting;
import dev.wh1tew1ndows.client.managers.module.settings.impl.SliderSetting;
import dev.wh1tew1ndows.client.utils.other.Instance;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;
import net.mojang.blaze3d.matrix.MatrixStack;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "CustomHand", category = Category.RENDER, desc = "Кастомизация отображения рук")
public class CustomHand extends Module {
    public static CustomHand getInstance() {
        return Instance.get(CustomHand.class);
    }

    public ModeSetting swingMode = new ModeSetting(this, "Анимация",
            "Дефолтная", "Взмах", "Взмах вниз", "Вниз Боком", "Вращение", "Необычный", "Пульс", "Выключено")
            .set("Выключено");

    public SliderSetting animspeed = new SliderSetting(this, "Скорость Анимации", 8, 3.0f, 10.0f, 1)
            .setVisible(() -> !swingMode.is("Выключено"));

    public SliderSetting animgsize = new SliderSetting(this, "Размер Анимации", 3.7F, 1, 10, 0.1F)
            .setVisible(() -> !swingMode.is("Выключено"));

    public BooleanSetting auraOnly = new BooleanSetting(this, "Только Аура", true);
    public BooleanSetting customhands = new BooleanSetting(this, "Модель Руки", false);
    public BooleanSetting zemetria = new BooleanSetting(this, "Менять обе руки", false).setVisible(() -> customhands.getValue());

    public SliderSetting x = new SliderSetting(this, "X", 0.0F, -2.0f, 2.0f, 0.01F).setVisible(() -> customhands.getValue() && zemetria.getValue());
    public SliderSetting y = new SliderSetting(this, "Y", 0.0F, -2.0f, 2.0f, 0.01F).setVisible(() -> customhands.getValue() && zemetria.getValue());
    public SliderSetting z = new SliderSetting(this, "Z", 0.0F, -2.0f, 2.0f, 0.01F).setVisible(() -> customhands.getValue() && zemetria.getValue());

    private final DelimiterSetting delimiter = new DelimiterSetting(this, "Правая рука").setVisible(() -> customhands.getValue() && !zemetria.getValue());

    public SliderSetting right_x = new SliderSetting(this, "X правая", 0.0F, -2.0f, 2.0f, 0.01F).setVisible(() -> customhands.getValue() && !zemetria.getValue());
    public SliderSetting right_y = new SliderSetting(this, "Y правая", 0.0F, -2.0f, 2.0f, 0.01F).setVisible(() -> customhands.getValue() && !zemetria.getValue());
    public SliderSetting right_z = new SliderSetting(this, "Z правая", 0.0F, -2.0f, 2.0f, 0.01F).setVisible(() -> customhands.getValue() && !zemetria.getValue());


    private final DelimiterSetting delimite2r = new DelimiterSetting(this, "Левая рука").setVisible(() -> customhands.getValue() && !zemetria.getValue());

    public SliderSetting lefvt_x = new SliderSetting(this, "X левая", 0.0F, -2.0f, 2.0f, 0.01F).setVisible(() -> customhands.getValue() && !zemetria.getValue());
    public SliderSetting lefvt_y = new SliderSetting(this, "Y левая", 0.0F, -2.0f, 2.0f, 0.01F).setVisible(() -> customhands.getValue() && !zemetria.getValue());
    public SliderSetting lefvt_z = new SliderSetting(this, "Z левая", 0.0F, -2.0f, 2.0f, 0.01F).setVisible(() -> customhands.getValue() && !zemetria.getValue());


    @EventHandler
    public void onEvent(ItemAnimationEvent event) {
        if (auraCheck() && event.getHand().equals(Hand.MAIN_HAND)) {

            final String swingMode = this.swingMode.getValue();
            float swingProgress = event.getSwingProgress();

            int i = event.getHandSide() == HandSide.RIGHT ? 1 : -1;

            MatrixStack matrix = event.getMatrix();


            float anim = (float) Math.sin(swingProgress * (Math.PI / 2) * 2);

            switch (swingMode) {
                case "Дефолтная" -> {

                }
                case "Взмах" -> {

                    matrix.translate((float) i * 0.67F, -0.32F, -1F);
                    matrix.rotate(Vector3f.YP.rotationDegrees(90));
                    matrix.rotate(Vector3f.ZP.rotationDegrees(-60));
                    matrix.rotate(Vector3f.XP.rotationDegrees(-90 - ((animgsize.getValue() * 10)) * anim));

                }
                case "Взмах вниз" -> {

                    matrix.translate((float) i * 0.67F, -0.32F, -1F);
                    matrix.rotate(Vector3f.YP.rotationDegrees(80));
                    matrix.rotate(Vector3f.ZP.rotationDegrees(-30));
                    matrix.rotate(Vector3f.XP.rotationDegrees(-100 - (animgsize.getValue() * 10) * anim));

                }
                case "Вниз Боком" -> {

                    matrix.translate((float) i * 0.67F, -0.32F, -1F);

                    matrix.translate(anim * -animgsize.getValue() / 35, 0, anim * -animgsize.getValue() / 35);

                    matrix.rotate(Vector3f.YP.rotationDegrees(25.0F));
                    //  matrix.rotate(Vector3f.ZP.rotationDegrees(anim * -animgsize.getValue() * 1));

                    matrix.rotate(Vector3f.XN.rotationDegrees(anim * animgsize.getValue() * 5));

                    matrix.rotate(Vector3f.YP.rotationDegrees(30.0F));
                    matrix.rotate(Vector3f.XP.rotationDegrees(-90.0F));
                    matrix.rotate(Vector3f.YP.rotationDegrees(50.0F));

                }


                case "Пульс" -> {
                    matrix.translate((float) i * 0.56F, -0.52F, -0.72F);
                    matrix.rotate(Vector3f.XP.rotationDegrees(anim * -animgsize.getValue() * 10));
                    matrix.rotate(Vector3f.YP.rotationDegrees(anim * animgsize.getValue() * i));
                    matrix.rotate(Vector3f.ZP.rotationDegrees(anim * animgsize.getValue()));
                }

                case "Вращение" -> {

                    matrix.translate((float) i * 0.56F, -0.52F, -0.72F);
                    matrix.translate(0, 0.1, 0);
                    matrix.rotate(Vector3f.XP.rotationDegrees(-0 + swingProgress * 360));
                    matrix.translate(0, -0.1, 0);
                }

                case "Необычный" -> {
                    matrix.translate((float) i * 0.56F, -0.32F, -0.72F);
                    matrix.translate(0, 0, -1.5f * anim / 5.0f);
                    matrix.rotate(Vector3f.YP.rotationDegrees(80));
                    matrix.rotate(Vector3f.ZN.rotationDegrees(45));
                    matrix.rotate(Vector3f.YP.rotationDegrees(-10));
                    matrix.translate(0, 0, -0.4f * anim);
                    matrix.rotate(Vector3f.XN.rotationDegrees(anim * -100));
                    matrix.rotate(Vector3f.XP.rotationDegrees(anim * -180));
                    matrix.rotate(Vector3f.XP.rotationDegrees(-70));
                }
                default -> {
                    float f6 = -0.4F * MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) Math.PI);
                    float f7 = 0.2F * MathHelper.sin(MathHelper.sqrt(swingProgress) * ((float) Math.PI * 2F));
                    float f10 = -0.2F * MathHelper.sin(swingProgress * (float) Math.PI);

                    matrix.translate((float) i * f6, f7, f10);
                    transformSideFirstPerson(matrix, event.getEquipProgress(), (float) i);
                    transformFirstPerson(matrix, swingProgress, (float) i);
                }
            }

            event.cancel();
        }
    }


    private static void transformFirstPerson(MatrixStack matrix, float swingProgress, float i) {
        float f = MathHelper.sin(swingProgress * swingProgress * (float) Math.PI);
        matrix.rotate(Vector3f.YP.rotationDegrees(i * (45.0F + f * -20.0F)));
        float f1 = MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) Math.PI);
        matrix.rotate(Vector3f.ZP.rotationDegrees(i * f1 * -20.0F));
        matrix.rotate(Vector3f.XP.rotationDegrees(f1 * -80.0F));
        matrix.rotate(Vector3f.YP.rotationDegrees(i * -45.0F));
    }

    private static void transformSideFirstPerson(MatrixStack matrix, float equipProgress, float i) {
        matrix.translate(i * 0.56F, -0.52F + equipProgress * -0.6F, -0.72F);
    }

    public boolean auraCheck() {
        return !auraOnly.getValue() || AttackAura.getInstance().isEnabled() && AttackAura.getInstance().target != null;
    }

    @EventHandler
    public void onEvent(RenderItemEvent event) {
        boolean rightHand = event.getHandSide() == HandSide.RIGHT;
        MatrixStack matrix = event.getMatrix();
        if (customhands.getValue() && zemetria.getValue()) {
            if (rightHand) {
                matrix.translate(
                        x.getValue(),
                        y.getValue(),
                        z.getValue()
                );

            } else {
                matrix.translate(
                        -x.getValue(),
                        y.getValue(),
                        z.getValue()
                );
            }
        }
        if (customhands.getValue() && !zemetria.getValue()) {
            if (rightHand) {
                matrix.translate(
                        right_x.getValue(),
                        right_y.getValue(),
                        right_z.getValue()
                );

            } else {
                matrix.translate(
                        lefvt_x.getValue(),
                        lefvt_y.getValue(),
                        lefvt_z.getValue()
                );
            }
        }
    }
}
