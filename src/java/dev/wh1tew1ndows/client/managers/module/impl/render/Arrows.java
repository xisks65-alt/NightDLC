package dev.wh1tew1ndows.client.managers.module.impl.render;

import dev.wh1tew1ndows.client.Zetrix;
import dev.wh1tew1ndows.client.api.events.orbit.EventHandler;
import dev.wh1tew1ndows.client.api.interfaces.IWindow;
import dev.wh1tew1ndows.client.managers.component.impl.rotation.Rotation;
import dev.wh1tew1ndows.client.managers.events.render.Render2DEvent;
import dev.wh1tew1ndows.client.managers.module.Category;
import dev.wh1tew1ndows.client.managers.module.Module;
import dev.wh1tew1ndows.client.managers.module.ModuleInfo;
import dev.wh1tew1ndows.client.managers.module.settings.impl.BooleanSetting;
import dev.wh1tew1ndows.client.managers.module.settings.impl.ColorSetting;
import dev.wh1tew1ndows.client.managers.module.settings.impl.ModeSetting;
import dev.wh1tew1ndows.client.managers.module.settings.impl.SliderSetting;
import dev.wh1tew1ndows.client.utils.animation.Animation;
import dev.wh1tew1ndows.client.utils.animation.util.Easings;
import dev.wh1tew1ndows.client.utils.math.Interpolator;
import dev.wh1tew1ndows.client.utils.other.Instance;
import dev.wh1tew1ndows.client.utils.player.MoveUtil;
import dev.wh1tew1ndows.client.utils.render.color.ColorUtil;
import dev.wh1tew1ndows.client.utils.render.draw.RectUtil;
import dev.wh1tew1ndows.client.utils.render.draw.RenderUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Namespaced;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.mojang.blaze3d.matrix.MatrixStack;
import org.lwjgl.opengl.GL11;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "Arrows", category = Category.RENDER, desc = "Стрелочка указывающая где находится игрок")
public class Arrows extends Module implements IWindow {
    public static Arrows getInstance() {
        return Instance.get(Arrows.class);
    }


    public ModeSetting mode = new ModeSetting(this,"Вид", "Type 1", "Type 2","Type 3");

    public SliderSetting size = new SliderSetting(this,"Размер",16,8,32,0.1F);
    public SliderSetting crosshairOffset = new SliderSetting(this,"Дистанция от прицела",0,0,50,1);
    public BooleanSetting walkAnimation = new BooleanSetting(this,"Анимация при ходьбе",true);

    public final ModeSetting color2 = new ModeSetting(this,"Цвет","Тема","Свой");

    private final ColorSetting colorC = new ColorSetting(this, "Свой цвет", ColorUtil.getColor(200, 200, 200)).setVisible(()-> color2.is("Свой"));

    private final ColorSetting color = new ColorSetting(this, "Цвет друзей", ColorUtil.getColor(0, 255, 0));

    private final Animation yawAnimation = new Animation();
    private final Animation moveAnimation = new Animation();
    private final Namespaced arrow = new Namespaced("texture/arrownew.png");

    @Override
    public void toggle() {
        super.toggle();

        moveAnimation.set(calculateMoveAnimation());
        yawAnimation.set(Rotation.cameraYaw());

    }

    @EventHandler
    public void onEvent(Render2DEvent event) {
        float cameraYaw = Rotation.cameraYaw();

        moveAnimation.update();
        moveAnimation.run(calculateMoveAnimation(), 0.5, Easings.EXPO_OUT);

        yawAnimation.update();
        yawAnimation.run(cameraYaw, 0.5, Easings.EXPO_OUT, true);


        final double cos = Math.cos(Math.toRadians(yawAnimation.getValue()));
        final double sin = Math.sin(Math.toRadians(yawAnimation.getValue()));

        final double radius = moveAnimation.getValue() + crosshairOffset.getValue();
        final double xOffset = (scaled().x / 2F) - radius;
        final double yOffset = (scaled().y / 2F) - radius;

        for (PlayerEntity player : mc.world.getPlayers()) {
            if (!isValidPlayer(player)) continue;

            Vector3d vector3d = mc.gameRenderer.getActiveRenderInfo().getProjectedView();
            final double xWay = ((Interpolator.lerp(player.lastTickPosX, player.getPosX(), mc.getRenderPartialTicks()) - vector3d.x));
            final double zWay = ((Interpolator.lerp(player.lastTickPosZ, player.getPosZ(), mc.getRenderPartialTicks()) - vector3d.z));
            final double rotationY = -(zWay * cos - xWay * sin);
            final double rotationX = -(xWay * cos + zWay * sin);
            final double angle = Math.toDegrees(Math.atan2(rotationY, rotationX));
            final double x = ((radius * Math.cos(Math.toRadians(angle))) + xOffset + radius);
            final double y = ((radius * Math.sin(Math.toRadians(angle))) + yOffset + radius);

            if (isValidRotation(rotationX, rotationY, radius)) {
                GL11.glPushMatrix();
                GL11.glTranslated(x, y, 0D);
                GL11.glRotated(angle, 0D, 0D, 1D);
                if(mode.is("Type 1")||mode.is("Type 2")) {
                    GL11.glRotatef(-90, 0F, 0F, 1F);
                } else {
                    GL11.glRotatef(90, 0F, 0F, 1F);
                }

                RenderUtil.start();
                drawTriangle(event.getMatrix(), Zetrix.inst().friendManager().isFriend(player.getGameProfile().getName()) ? color.getValue() : color2.is("Свой") ? colorC.getValue() : InterFace.getInstance().clientColor());
                RenderUtil.stop();
                GL11.glPopMatrix();
            }
        }
    }

    private float calculateMoveAnimation() {
        float set = 50;
        if (mc.currentScreen instanceof ContainerScreen<?> container) {
            set = Math.max(container.ySize, container.xSize) / 2F + 50 * (mc.gameSettings.guiScale - 1);
        }
        if (walkAnimation.getValue() && MoveUtil.isMoving()) {
            set += mc.player.isSneaking() ? 5 : 15;
        } else if (mc.player.isSneaking()) {
            set -= 10;
        }
        return set;
    }

    private boolean isValidPlayer(PlayerEntity player) {
        return player != mc.player && player.isAlive();
    }

    private boolean isValidRotation(double rotationX, double rotationY, double radius) {
        final double mrotY = -rotationY;
        final double mrotX = -rotationX;
        return MathHelper.sqrt(mrotX * mrotX + mrotY * mrotY) < radius;
    }

    private void drawTriangle(MatrixStack matrix, int color) {
        float size = this.size.getValue() / 2;
        if(mode.is("Type 1")) {
            size =  this.size.getValue();;
            RenderUtil.bindTexture(new Namespaced("texture/arrows/arrows1.png"));
        }
        if(mode.is("Type 2")) {
            size =  this.size.getValue();;
            RenderUtil.bindTexture(new Namespaced("texture/arrows/arrows.png"));
        }
        if(mode.is("Type 3"))
            RenderUtil.bindTexture(arrow);
        matrix.translate(size / 2F, size / 2F, 0);
        RectUtil.drawRect(matrix, -size, -size, size, size, color, color, ColorUtil.multDark(color, 0.5F), ColorUtil.multDark(color, 0.5F), true, true);
        matrix.translate(-(size / 2F), -(size / 2F), 0);
    }
}