package dev.wh1tew1ndows.client.managers.module.impl.render;

import com.google.common.collect.Lists;
import dev.wh1tew1ndows.client.api.events.orbit.EventHandler;
import dev.wh1tew1ndows.client.api.events.orbit.EventPriority;
import dev.wh1tew1ndows.client.managers.events.render.Render3DPosedEvent;
import dev.wh1tew1ndows.client.managers.module.Category;
import dev.wh1tew1ndows.client.managers.module.Module;
import dev.wh1tew1ndows.client.managers.module.ModuleInfo;
import dev.wh1tew1ndows.client.managers.module.settings.impl.BooleanSetting;
import dev.wh1tew1ndows.client.managers.module.settings.impl.ModeSetting;
import dev.wh1tew1ndows.client.managers.module.settings.impl.MultiBooleanSetting;
import dev.wh1tew1ndows.client.managers.module.settings.impl.SliderSetting;
import dev.wh1tew1ndows.client.utils.other.Instance;
import dev.wh1tew1ndows.client.utils.render.color.ColorUtil;
import dev.wh1tew1ndows.client.utils.render.draw.RenderUtil3D;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.List;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "ContainerESP", category = Category.RENDER, desc = "Подсветка контейнеров через стены")
public class ContainerESP extends Module {
    public static ContainerESP getInstance() {
        return Instance.get(ContainerESP.class);
    }

    private final MultiBooleanSetting blocks = new MultiBooleanSetting(this, "Элементы",
            BooleanSetting.of("Сундук", true),
            BooleanSetting.of("Эндер-Сундук", false),
            BooleanSetting.of("Шалкер", false),
            BooleanSetting.of("Бочка", false),
            BooleanSetting.of("Воронка", false),
            BooleanSetting.of("Печка", false)
    );

    private final MultiBooleanSetting renderElements = new MultiBooleanSetting(this, "Рендеринг",
            BooleanSetting.of("Заливка", true),
            BooleanSetting.of("Обходка", true),
            BooleanSetting.of("Штрихи", false)
    ).setVisible(() -> blocks != null && blocks.isAnyTrue());

    private final ModeSetting renderType = new ModeSetting(this, "Тип выделения", "По боксу", "По границам").set("По боксу");
    private final SliderSetting expandWidth = new SliderSetting(this, "Масштаб границ", .05F, .01F, .1F, .005F);

    private final List<BlockPos> list = Lists.newArrayList();

    @EventHandler(priority = EventPriority.LOWEST)
    public void onRender3D(Render3DPosedEvent e) {
        if (mc.world == null) return;
        boolean out = renderElements.getValue("Обходка"), decu = renderElements.getValue("Штрихи"), fill = renderElements.getValue("Заливка");
        if (!out && !decu && !fill) return;
        boolean typeRenderShapes = renderType.is("По границам");
        float expandBox = expandWidth.getValue() / 2.F;
        for (TileEntity entity : mc.world.loadedTileEntityList) {
            if (entity == null) continue;
            BlockState state;
            if ((state = entity.getBlockState()) == null || entity.getPos() == null) continue;
            VoxelShape shape;
            if ((shape = state.getShape(mc.world, entity.getPos())) == null) continue;
            Color color = null;
            if (entity instanceof ChestTileEntity && blocks.get("Сундук").getValue()) {
                color = new Color(0xFFB327);
            } else if (entity instanceof EnderChestTileEntity && blocks.get("Эндер-Сундук").getValue()) {
                color = new Color(0x9456FF);
            } else if (entity instanceof BarrelTileEntity && blocks.get("Бочка").getValue()) {
                color = new Color(0xDC8024);
            } else if (entity instanceof HopperTileEntity && blocks.get("Воронка").getValue()) {
                color = new Color(0x47484D);
            } else if (entity instanceof FurnaceTileEntity && blocks.get("Печка").getValue()) {
                color = new Color(0x181818);
            } else if (entity instanceof ShulkerBoxTileEntity && blocks.get("Шалкер").getValue()) {
                color = new Color(0xF12289);
            }
            if (color == null) continue;
            final int toWhiteColor = ColorUtil.overCol(color.getRGB(), -1, .06F), colorFill = ColorUtil.multDark(toWhiteColor, .076F), colorLine = ColorUtil.multDark(toWhiteColor, .5F), colorDecu = ColorUtil.multDark(toWhiteColor, .35F);
            if (typeRenderShapes) {
                shape.forEachEdge((a1, b1, c1, d1, e1, f1) -> {
                    AxisAlignedBB aabb = new AxisAlignedBB(a1, b1, c1, d1, e1, f1).offset(entity.getPos()).grow(expandBox);
                    drawBox(e, aabb, out, decu, fill, colorLine, colorDecu, colorFill);
                });
            } else {
                shape.forEachBox((a1, b1, c1, d1, e1, f1) -> {
                    AxisAlignedBB aabb = new AxisAlignedBB(a1, b1, c1, d1, e1, f1).offset(entity.getPos());
                    drawBox(e, aabb, out, decu, fill, colorLine, colorDecu, colorFill);
                });
            }

        }
    }

    private void drawBox(Render3DPosedEvent event, AxisAlignedBB aabb, boolean out, boolean decu, boolean fill, int colorLine, int colorDecu, int colorFill) {
        if (aabb != null && mc.worldRenderer.getClippinghelper().isBoundingBoxInFrustum(aabb)) {
            GL11.glEnable(GL11.GL_LINE_SMOOTH);
            GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
            GL11.glLineWidth(.125F);
            RenderUtil3D.setup3dForBlockPos(event, () -> RenderUtil3D.drawCanisterBox(event.getMatrix(), RenderUtil3D.BUFFER, RenderUtil3D.TESSELLATOR, aabb, out, decu, fill, colorLine, colorDecu, colorFill), true, true);
            GL11.glLineWidth(1.F);
            GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_DONT_CARE);
            GL11.glDisable(GL11.GL_LINE_SMOOTH);
        }
    }
}
