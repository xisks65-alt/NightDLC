package dev.wh1tew1ndows.client.managers.module.legacyModule;

import dev.wh1tew1ndows.client.Zetrix;
import dev.wh1tew1ndows.client.api.events.orbit.EventHandler;
import dev.wh1tew1ndows.client.managers.component.impl.rotation.Rotation;
import dev.wh1tew1ndows.client.managers.component.impl.rotation.RotationComponent;
import dev.wh1tew1ndows.client.managers.events.player.MotionEvent;
import dev.wh1tew1ndows.client.managers.events.player.UpdateEvent;
import dev.wh1tew1ndows.client.managers.module.Category;
import dev.wh1tew1ndows.client.managers.module.Module;
import dev.wh1tew1ndows.client.managers.module.ModuleInfo;
import dev.wh1tew1ndows.client.managers.module.settings.impl.BooleanSetting;
import dev.wh1tew1ndows.client.managers.module.settings.impl.ModeSetting;
import dev.wh1tew1ndows.client.managers.module.settings.impl.MultiBooleanSetting;
import dev.wh1tew1ndows.client.managers.module.settings.impl.SliderSetting;
import dev.wh1tew1ndows.client.utils.math.Mathf;
import dev.wh1tew1ndows.client.utils.player.InventoryUtil;
import dev.wh1tew1ndows.client.utils.time.TimerUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EnderCrystalEntity;
import net.minecraft.entity.item.FallingBlockEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.ProjectileItemEntity;
import net.minecraft.entity.projectile.ThrowableEntity;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CAnimateHandPacket;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "CrystalAura", category = Category.COMBAT, desc = "Автоматическое размещение и взрыв эндер-кристаллов")
public class CrystalAura extends Module {

    public final MultiBooleanSetting options = new MultiBooleanSetting(this, "Опции",
            BooleanSetting.of("Не взрывать себя", true),
            BooleanSetting.of("Ставить кристаллы", true),
            BooleanSetting.of("Ротация", true),
            BooleanSetting.of("Визуализация", true)
    );

    private final ModeSetting distanceMode = new ModeSetting(this, "Тип радиуса", "Vanilla", "Custom");
    private final SliderSetting customDistance = new SliderSetting(this, "Радиус", 5, 2.5f, 6, 0.05f).setVisible(() -> distanceMode.is("Custom"));
    private final SliderSetting customUp = new SliderSetting(this, "Вверх", 2, 1, 6, 0.05f);
    private final SliderSetting customDown = new SliderSetting(this, "Вниз", 2, 1, 6, 0.05f);
    private final SliderSetting breakDelay = new SliderSetting(this, "Задержка (мс)", 150, 0, 500, 1);


    private Entity crystalTarget = null;
    public Vector2f rotate = new Vector2f(0, 0);
    private Vector3d obsidianVec = new Vector3d(0, 0, 0);
    private BlockPos closestObsidian = null;
    private Entity closestCrystal;
    private final List<BlockPos> obsidianPositions = new ArrayList<>();
    private final TimerUtil stopWatch = new TimerUtil();
    private boolean crystalAttack = false;

    double distance() {
        return distanceMode.is("Vanilla") ? mc.playerController.getBlockReachDistance() : customDistance.getValue();
    }

    public boolean check() {
        return (crystalTarget != null || closestObsidian != null) && rotate != null && options.getValue("Коррекция движения") && (options.getValue("Ротация"));
    }

    @Override
    public void onDisable() {
        reset();

        super.onDisable();
    }

    /**
     * private final MultiBooleanSetting renderElements = new MultiBooleanSetting(this, "Рендеринг",
     * BooleanSetting.of("Заливка", true),
     * BooleanSetting.of("Обходка", true),
     * BooleanSetting.of("Штрихи", false)
     * );
     * <p>
     * private final ModeSetting renderType = new ModeSetting(this, "Тип выделения", "По боксу", "По границам").set("По боксу");
     * private final SliderSetting expandWidth = new SliderSetting(this, "Масштаб границ", .05F, .01F, .1F, .005F);
     * <p>
     * private final List<BlockPos> list = Lists.newArrayList();
     *
     * @EventHandler(priority = EventPriority.LOWEST)
     * public void onRender3D(Render3DLastEvent e) {
     * if (mc.world == null) return;
     * boolean out = renderElements.getValue("Обходка"), decu = renderElements.getValue("Штрихи"), fill = renderElements.getValue("Заливка");
     * if (!out && !decu && !fill) return;
     * boolean typeRenderShapes = renderType.is("По границам");
     * float expandBox = expandWidth.getValue() / 2.F;
     * for (TileEntity entity : mc.world.loadedTileEntityList) {
     * if (entity == null) continue;
     * BlockState state;
     * if ((state = entity.getBlockState()) == null || entity.getPos() == null) continue;
     * VoxelShape shape;
     * if ((shape = state.getShape(mc.world, entity.getPos())) == null) continue;
     * Color color = new Color(ColorUtil.skyRainbow(5, 5));
     * if (color == null) continue;
     * final int toWhiteColor = ColorUtil.overCol(color.getRGB(), -1, .06F), colorFill = ColorUtil.multDark(toWhiteColor, .076F), colorLine = ColorUtil.multDark(toWhiteColor, .5F), colorDecu = ColorUtil.multDark(toWhiteColor, .35F);
     * if (typeRenderShapes) {
     * shape.forEachEdge((a1, b1, c1, d1, e1, f1) -> {
     * AxisAlignedBB aabb = new AxisAlignedBB(a1, b1, c1, d1, e1, f1).offset(entity.getPos()).grow(expandBox);
     * drawBox(e, aabb, out, decu, fill, colorLine, colorDecu, colorFill);
     * });
     * } else {
     * shape.forEachBox((a1, b1, c1, d1, e1, f1) -> {
     * AxisAlignedBB aabb = new AxisAlignedBB(a1, b1, c1, d1, e1, f1).offset(entity.getPos());
     * drawBox(e, aabb, out, decu, fill, colorLine, colorDecu, colorFill);
     * });
     * }
     * <p>
     * }
     * }
     * <p>
     * private void drawBox(Render3DLastEvent event, AxisAlignedBB aabb, boolean out, boolean decu, boolean fill, int colorLine, int colorDecu, int colorFill) {
     * if (aabb != null && mc.worldRenderer.getClippinghelper().isBoundingBoxInFrustum(aabb)) {
     * GL11.glEnable(GL11.GL_LINE_SMOOTH);
     * GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
     * GL11.glLineWidth(.125F);
     * RenderUtil3D.setup3dForBlockPos(event, () -> RenderUtil3D.drawCanisterBox(event.getMatrix(), RenderUtil3D.BUFFER, RenderUtil3D.TESSELLATOR, aabb, out, decu, fill, colorLine, colorDecu, colorFill), true, true);
     * GL11.glLineWidth(1.F);
     * GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_DONT_CARE);
     * GL11.glDisable(GL11.GL_LINE_SMOOTH);
     * }
     * }
     */


    @EventHandler
    public void onUpdate(UpdateEvent e) {
        findAndAttackCrystal();
        findAndClickObsidian();
    }

    private void findAndAttackCrystal() {
        closestCrystal = null;
        double closestDistanceToTarget = Double.MAX_VALUE;
        double maxDistanceToCrystal = distance();

        if (!options.getValue("Ставить кристаллы")) {
            crystalAttack = true;
        }

        for (Entity entity : mc.world.getAllEntities()) {
            if (entity instanceof EnderCrystalEntity enderCrystal) {
                double distanceToCrystal = mc.player.getDistance(enderCrystal);
                if (distanceToCrystal > maxDistanceToCrystal) {
                    continue;
                }

                if (mc.player.getPosY() >= enderCrystal.getPosY() && options.getValue("Не взрывать себя")) {
                    continue;
                }

                if (distanceToCrystal < closestDistanceToTarget) {
                    closestDistanceToTarget = distanceToCrystal;
                    closestCrystal = enderCrystal;
                }
            }
        }

        if (closestCrystal != null && crystalAttack) {
            crystalTarget = closestCrystal;
            if (stopWatch.isReached(breakDelay.getValue().longValue())) {
                mc.playerController.attackEntity(mc.player, closestCrystal);
                mc.player.connection.sendPacket(new CAnimateHandPacket(Hand.MAIN_HAND));
                crystalTarget = null;
                stopWatch.reset();
            }
        } else {
            reset();
        }
    }


    private void findAndClickObsidian() {
        int crystal = InventoryUtil.getInstance().getSlotInInventoryOrHotbar(Items.END_CRYSTAL, true);
        if (crystal == -1 || !options.getValue("Ставить кристаллы")) return;

        double closestDistanceToTarget = Double.MAX_VALUE;
        double maxDistanceToObsidian = distance() * 2;
        closestObsidian = null;
        obsidianPositions.clear();
        crystalAttack = false;

        for (Entity entity : mc.world.getAllEntities()) {
            if (entity == mc.player || Zetrix.inst().friendManager().isFriend(entity.getName().getString()) || entity instanceof EnderCrystalEntity || entity instanceof ArrowEntity || entity instanceof ProjectileItemEntity || entity instanceof ItemEntity || entity instanceof ThrowableEntity || entity instanceof FallingBlockEntity) {
                continue;
            }

            for (int x = (int) -distance(); x <= distance(); x++) {
                for (int z = (int) -distance(); z <= distance(); z++) {
                    for (int y = -customDown.getValue().intValue(); y <= customUp.getValue().intValue(); y++) {
                        BlockPos pos = new BlockPos(entity.getPosX() + x, entity.getPosY() - 0.5f + y, entity.getPosZ() + z);
                        if (mc.world.getBlockState(pos).getBlock() == Blocks.OBSIDIAN) {
                            Block blockAbove = mc.world.getBlockState(pos.up()).getBlock();
                            if (!(blockAbove instanceof AirBlock)) {
                                continue;
                            }

                            if (pos.getY() < mc.player.getPosY() && options.getValue("Не взрывать себя") && !mc.player.isCreative() || !entity.isAlive()) {
                                continue;
                            }

                            if (entity.getPosition().equals(pos.up()) || mc.player.getPosition().equals(pos.up())) {
                                continue;
                            }

                            if (entity.getPosY() - 0.5f < pos.getY()) {
                                continue;
                            }

                            double distanceToPlayer = mc.player.getDistanceSq(Vector3d.copyCentered(pos));
                            if (distanceToPlayer > maxDistanceToObsidian) {
                                continue;
                            }

                            double distanceToTarget = entity.getDistanceSq(Vector3d.copyCentered(pos));
                            if (distanceToTarget < closestDistanceToTarget) {
                                closestDistanceToTarget = distanceToTarget;
                                closestObsidian = pos;
                                obsidianPositions.clear();
                                obsidianPositions.add(closestObsidian);

                            }

                        }
                    }
                }
            }
        }

        if (!obsidianPositions.isEmpty()) {
            int last = mc.player.inventory.currentItem;
            mc.player.inventory.currentItem = crystal;
            obsidianVec = new Vector3d(closestObsidian.getX() + 0.5, closestObsidian.getY() + 0.5, closestObsidian.getZ() + 0.5);
            BlockRayTraceResult rayTraceResult = new BlockRayTraceResult(obsidianVec, Direction.UP, closestObsidian, false);
            mc.playerController.rightClickBlock(mc.player, mc.world, Hand.MAIN_HAND, rayTraceResult);
            mc.player.connection.sendPacket(new CAnimateHandPacket(Hand.MAIN_HAND));
            crystalAttack = true;
            mc.player.inventory.currentItem = last;
        }
    }

    @EventHandler
    public void onMotion(MotionEvent e) {
        if (obsidianVec != null && crystalTarget == null && options.getValue("Ротация")) {
            rotate = Mathf.rotationToVec(obsidianVec);

            RotationComponent.update(new Rotation(rotate.x + ThreadLocalRandom.current().nextFloat(-1, 1), rotate.y + ThreadLocalRandom.current().nextFloat(-3, 3)), 360, 360, 0, 75);

        } else if (closestCrystal != null && options.getValue("Ротация")) {
            rotate = Mathf.rotationToEntity(closestCrystal);

            RotationComponent.update(new Rotation(rotate.x + ThreadLocalRandom.current().nextFloat(-1, 1), rotate.y + ThreadLocalRandom.current().nextFloat(-3, 3)), 360, 360, 0, 80);

        }

    }


    public void reset() {
        closestObsidian = null;
        closestCrystal = null;
        crystalTarget = null;
        obsidianVec = null;
        obsidianPositions.clear();
        crystalAttack = false;
    }
}
