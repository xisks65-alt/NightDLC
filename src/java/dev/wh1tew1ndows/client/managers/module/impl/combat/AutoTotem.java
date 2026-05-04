package dev.wh1tew1ndows.client.managers.module.impl.combat;

import dev.wh1tew1ndows.client.api.events.orbit.EventHandler;
import dev.wh1tew1ndows.client.managers.events.player.UpdateEvent;
import dev.wh1tew1ndows.client.managers.events.render.Render2DEvent;
import dev.wh1tew1ndows.client.managers.module.Category;
import dev.wh1tew1ndows.client.managers.module.Module;
import dev.wh1tew1ndows.client.managers.module.ModuleInfo;
import dev.wh1tew1ndows.client.managers.module.impl.misc.Notifications;
import dev.wh1tew1ndows.client.managers.module.settings.impl.BooleanSetting;
import dev.wh1tew1ndows.client.managers.module.settings.impl.ModeSetting;
import dev.wh1tew1ndows.client.managers.module.settings.impl.MultiBooleanSetting;
import dev.wh1tew1ndows.client.managers.module.settings.impl.SliderSetting;
import dev.wh1tew1ndows.client.utils.SwapHelpers;
import dev.wh1tew1ndows.client.utils.other.Instance;
import dev.wh1tew1ndows.client.utils.player.MoveUtil;
import dev.wh1tew1ndows.client.utils.render.color.ColorUtil;
import dev.wh1tew1ndows.client.utils.render.font.Fonts;
import dev.wh1tew1ndows.client.utils.time.StopWatch;
import dev.wh1tew1ndows.client.managers.module.impl.movement.InvMove;
import lombok.Generated;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EnderCrystalEntity;
import net.minecraft.entity.item.TNTEntity;
import net.minecraft.entity.item.minecart.TNTMinecartEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SkullItem;
import net.minecraft.network.play.client.CClickWindowPacket;
import net.minecraft.network.play.client.CCloseWindowPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.mojang.blaze3d.platform.GlStateManager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

@ModuleInfo(
        name = "AutoTotem",
        category = Category.COMBAT,
        desc = "Автоматическое использование тотемов бессмертия"
)
public class AutoTotem extends Module {
    private final ModeSetting mode = new ModeSetting(this, "Режим", "Обычный", "Spooky");
    private final MultiBooleanSetting options = new MultiBooleanSetting(this, "Настройки", new BooleanSetting("Здоровье с элитрами", true), new BooleanSetting("Динамит", true), new BooleanSetting("Падение", false), new BooleanSetting("Трезубец", true), new BooleanSetting("Якорь", false), new BooleanSetting("Эндер-кристалл", false));
    private final BooleanSetting drawCounter = new BooleanSetting(this, "Рендерить кол тотемов", true);
    private final SliderSetting health = new SliderSetting(this, "Здоровье", 4.0F, 1.0F, 20.0F, 0.5F);
    private final SliderSetting elytraHealth = (new SliderSetting(this, "Здоровье на элитре", 9.0F, 0.0F, 20.0F, 0.5F)).setVisible(() -> this.options.getValue("Здоровье с элитрами"));
    private final SliderSetting crystalDistance = (new SliderSetting(this, "Дистанция до кристалла", 4.0F, 1.0F, 10.0F, 1.0F)).setVisible(() -> this.options.getValue("Эндер-кристалл"));
    private final SliderSetting tntDistance = (new SliderSetting(this, "Дистанция до динамита", 30.0F, 3.0F, 50.0F, 1.0F)).setVisible(() -> this.options.getValue("Динамит"));
    private final SliderSetting tridentDistance = (new SliderSetting(this, "Дистанция трезубца", 10.0F, 5.0F, 50.0F, 1.0F)).setVisible(() -> this.options.getValue("Трезубец"));
    private final BooleanSetting noBall = new BooleanSetting("Не свапать если шар", false);
    private final BooleanSetting ignoreEnchanted = new BooleanSetting("Игнорировать зачарованные", false);
    private final SwapHelpers swap = new SwapHelpers();
    private int oldSlot = -1;
    private ItemStack oldOffhandItem;
    private int nonEnchantedTotems;
    private boolean lastTotemInHand;
    private boolean lastLowHealth;
    private boolean lastCrystalNearby;
    private boolean lastTntNearby;
    private boolean lastTridentNearby;
    private boolean autoTotemSwapped;
    private boolean autoTotemSwappedBack;
    private long lastSwapTime;

    public AutoTotem() {
        this.oldOffhandItem = ItemStack.EMPTY;
        this.lastTotemInHand = false;
        this.lastLowHealth = false;
        this.lastCrystalNearby = false;
        this.lastTntNearby = false;
        this.lastTridentNearby = false;
        this.autoTotemSwapped = false;
        this.autoTotemSwappedBack = false;
        this.lastSwapTime = 0L;
    }

    public static AutoTotem getInstance() {
        return Instance.get(AutoTotem.class);
    }

    @EventHandler
    public void update(UpdateEvent event) {
        if (mc.player == null || !mc.player.isAlive() || mc.world == null) {
            this.resetSwapBack();
            return;
        }

        this.nonEnchantedTotems = (int) IntStream.range(0, 36)
                .mapToObj(slot -> mc.player.inventory.getStackInSlot(slot))
                .filter(stack -> stack.getItem() == Items.TOTEM_OF_UNDYING && !stack.isEnchanted())
                .count();
        this.checkNotifications();
        if (this.mode.getValue().equals("Spooky")) {
            this.Spooky();
        } else {
            this.Обычный();
        }

    }

    private void Spooky() {
        // Spooky: встроенная синхронизация с InvMove
        InvMove invMove = InvMove.getInstance();
        if (invMove != null && invMove.isEnabled() && invMove.stoppedStatus()) {
            return;
        }

        int totemSlot = this.findNonEnchantedTotemSlot();
        boolean hasTotemInHands = this.isTotemInHands();

        if (this.canSwap()) {
            if (totemSlot >= 0 && !hasTotemInHands) {
                if (this.oldSlot == -1) {
                    this.oldOffhandItem = mc.player.getHeldItemOffhand().copy();
                    this.oldSlot = totemSlot;
                }
                doSpookySwap(totemSlot);
                this.autoTotemSwapped = true;
                this.lastSwapTime = System.currentTimeMillis();
            }
        } else if (this.oldSlot != -1) {
            if (mc.player.getHeldItemOffhand().getItem() == Items.TOTEM_OF_UNDYING) {
                doSpookySwap(this.oldSlot);
                this.autoTotemSwappedBack = true;
                this.lastSwapTime = System.currentTimeMillis();
                this.resetSwapBack();
            } else {
                this.resetSwapBack();
            }
        }
    }

    private void doSwap(int slot) {
        mc.playerController.windowClick(0, slot, 40, ClickType.SWAP, mc.player);
        mc.player.connection.sendPacket(new CCloseWindowPacket());
    }

    private void doSpookySwap(int slot) {
        InvMove invMove = InvMove.getInstance();
        if (invMove != null && invMove.isEnabled() && !invMove.type().is("Обычный")) {
            CClickWindowPacket pkt = new CClickWindowPacket(0, slot, 40, ClickType.SWAP,
                    mc.player.openContainer.getSlot(slot).getStack(),
                    mc.player.openContainer.getNextTransactionID(mc.player.inventory));
            invMove.queueSwapPacket(pkt);
        } else {
            mc.playerController.windowClick(0, slot, 40, ClickType.SWAP, mc.player);
            mc.player.connection.sendPacket(new CCloseWindowPacket());
        }
    }

    private void Обычный() {
        int totemSlot = this.findNonEnchantedTotemSlot();
        boolean hasTotemInHands = this.isTotemInHands();
        if (this.canSwap()) {
            if (totemSlot >= 0 && !hasTotemInHands) {
                if (this.oldSlot == -1) {
                    this.oldOffhandItem = mc.player.getHeldItemOffhand().copy();
                    this.oldSlot = totemSlot;
                }

                doSwap(totemSlot);
                this.autoTotemSwapped = true;
                this.lastSwapTime = System.currentTimeMillis();
            }
        } else if (this.oldSlot != -1) {
            if (mc.player.getHeldItemOffhand().getItem() == Items.TOTEM_OF_UNDYING) {
                doSwap(this.oldSlot);
                this.autoTotemSwappedBack = true;
                this.lastSwapTime = System.currentTimeMillis();
                this.resetSwapBack();
            } else {
                this.resetSwapBack();
            }
        }

    }

    private void resetSwapBack() {
        this.oldOffhandItem = ItemStack.EMPTY;
        this.oldSlot = -1;
    }

    private int findNonEnchantedTotemSlot() {
        for (int slot = 0; slot < 36; ++slot) {
            ItemStack stack = mc.player.inventory.getStackInSlot(slot);
            if (stack.getItem() == Items.TOTEM_OF_UNDYING && !stack.isEnchanted()) {
                return slot < 9 ? slot + 36 : slot;
            }
        }

        if (!this.ignoreEnchanted.getValue()) {
            for (int slot = 0; slot < 36; ++slot) {
                ItemStack stack = mc.player.inventory.getStackInSlot(slot);
                if (stack.getItem() == Items.TOTEM_OF_UNDYING) {
                    return slot < 9 ? slot + 36 : slot;
                }
            }
        }

        return -1;
    }

    private boolean isTotemInHands() {
        ItemStack mainHandStack = mc.player.getHeldItemMainhand();
        ItemStack offHandStack = mc.player.getHeldItemOffhand();
        if (mainHandStack.getItem() == Items.TOTEM_OF_UNDYING) {
            if (this.ignoreEnchanted.getValue() && mainHandStack.isEnchanted()) {
                return false;
            }
            return !mainHandStack.isEnchanted() || this.nonEnchantedTotems <= 0;
        } else if (offHandStack.getItem() != Items.TOTEM_OF_UNDYING) {
            return false;
        } else {
            if (this.ignoreEnchanted.getValue() && offHandStack.isEnchanted()) {
                return false;
            }
            return !offHandStack.isEnchanted() || this.nonEnchantedTotems <= 0;
        }
    }

    private boolean canSwap() {
        boolean elytraRisk = this.elytraCheck();
        boolean crystalNearby = this.checkCrystal();
        boolean tntNearby = this.checkTnt();
        boolean fallingRisk = this.checkFall();
        boolean anchorNearby = this.checkAnchor();
        boolean tridentNearby = this.checkTrident();
        boolean lowHealth = mc.player.getHealth() + this.getAbsorption() <= this.health.getValue();
        return elytraRisk || crystalNearby || tntNearby || tridentNearby || fallingRisk || lowHealth || anchorNearby;
    }

    private final ItemStack stack = new ItemStack(Items.TOTEM_OF_UNDYING);

    @EventHandler
    public void onRender(Render2DEvent e) {
        if (!drawCounter.getValue())
            return;

        if (getTotemCount() > 0) {
            Fonts.SF_BOLD.drawOutline(e.getMatrix(), getTotemCount() + "x", mc.getMainWindow().getScaledWidth() / 2f + 12F,
                    mc.getMainWindow().getScaledHeight() / 2f + 84, ColorUtil.getColor(200), 7);
            GlStateManager.pushMatrix();
            GlStateManager.disableBlend();
            mc.getItemRenderer().renderItemAndEffectIntoGUI(stack, (int) (mc.getMainWindow().getScaledWidth() / 2F - 8), (int) (mc.getMainWindow().getScaledHeight() / 2F + 80));
            GlStateManager.popMatrix();
        }
    }

    private int getTotemCount() {
        int count = 0;
        for (int i = 0; i < mc.player.inventory.getSizeInventory(); i++) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack.getItem().equals(Items.TOTEM_OF_UNDYING)) {
                count++;
            }
        }
        return count;
    }

    private boolean checkAnchor() {
        if (!this.options.getValue("Якорь")) {
            return false;
        } else {
            return this.getBlock(4.0F, Blocks.RESPAWN_ANCHOR) != null;
        }
    }

    private boolean elytraCheck() {
        boolean hasElytra = mc.player.inventory.armorInventory.get(2).getItem() instanceof ElytraItem && this.options.getValue("Здоровье с элитрами");
        return hasElytra && this.checkHealth();
    }

    private boolean checkFall() {
        if (!this.options.getValue("Падение")) {
            return false;
        } else {
            return mc.player.fallDistance > 10.0F;
        }
    }

    private boolean checkHealth() {
        return mc.player.getHealth() + this.getAbsorption() <= this.elytraHealth.getValue();
    }

    private boolean checkCrystal() {
        for (Entity entity : mc.world.getAllEntities()) {
            if (entity instanceof EnderCrystalEntity && mc.player.getDistance(entity) <= this.crystalDistance.getValue() && this.options.getValue("Эндер-кристалл")) {
                return !(mc.player.getHeldItemOffhand().getItem() instanceof SkullItem) || !this.noBall.getValue();
            }
        }

        return false;
    }

    private boolean checkTnt() {
        if (!this.options.getValue("Динамит")) {
            return false;
        } else {
            for (Entity entity : mc.world.getAllEntities()) {
                float distance = mc.player.getDistance(entity);
                if ((entity instanceof TNTEntity || entity instanceof TNTMinecartEntity) && distance <= this.tntDistance.getValue()) {
                    return true;
                }
            }

            return false;
        }
    }

    private boolean checkTrident() {
        if (!this.options.getValue("Трезубец")) {
            return false;
        } else {
            for (Entity entity : mc.world.getAllEntities()) {
                float distance = mc.player.getDistance(entity);
                if (entity instanceof TridentEntity && distance <= this.tridentDistance.getValue()) {
                    return true;
                }
            }

            return false;
        }
    }

    private float getAbsorption() {
        return mc.player.getAbsorptionAmount();
    }

    private BlockPos getBlock(float radius, Block block) {
        return this.getSphere(this.getPlayerPosLocal(), radius, 6, false, true, 0).stream()
                .filter(pos -> mc.world.getBlockState(pos).getBlock() == block)
                .min(Comparator.comparing(pos -> this.getDistanceOfEntityToBlock(mc.player, pos)))
                .orElse(null);
    }

    private BlockPos getBlock(float radius) {
        return this.getSphere(this.getPlayerPosLocal(), radius, 6, false, true, 0).stream()
                .filter(pos -> mc.world.getBlockState(pos).getBlock() != Blocks.AIR)
                .min(Comparator.comparing(pos -> this.getDistanceOfEntityToBlock(mc.player, pos)))
                .orElse(null);
    }

    private BlockPos getBlockFlat(int range) {
        BlockPos centerPos = this.getPlayerPosLocal().add(0, -1, 0);

        for (int x = centerPos.getX() - range; x <= centerPos.getX() + range; ++x) {
            for (int z = centerPos.getX() - range; z <= centerPos.getZ() + range; ++z) {
                if (mc.world.getBlockState(new BlockPos(x, centerPos.getY(), z)).getBlock() != Blocks.AIR) {
                    return new BlockPos(x, centerPos.getY(), z);
                }
            }
        }

        return centerPos;
    }

    private List<BlockPos> getSphere(BlockPos center, float radius, int height, boolean hollow, boolean sphere, int yOffset) {
        ArrayList<BlockPos> positions = new ArrayList<>();
        int centerX = center.getX();
        int centerY = center.getY();
        int centerZ = center.getZ();

        for (int x = centerX - (int) radius; (float) x <= (float) centerX + radius; ++x) {
            for (int z = centerZ - (int) radius; (float) z <= (float) centerZ + radius; ++z) {
                for (int y = sphere ? centerY - (int) radius : centerY; (float) y < (sphere ? (float) centerY + radius : (float) (centerY + height)); ++y) {
                    double distanceSquared = (centerX - x) * (centerX - x) + (centerZ - z) * (centerZ - z) + (sphere ? (centerY - y) * (centerY - y) : 0);
                    if (distanceSquared < (double) (radius * radius) && (!hollow || distanceSquared >= (double) ((radius - 1.0F) * (radius - 1.0F)))) {
                        positions.add(new BlockPos(x, y + yOffset, z));
                    }
                }
            }
        }

        return positions;
    }

    private BlockPos getPlayerPosLocal() {
        return mc.player == null ? BlockPos.ZERO : new BlockPos(Math.floor(mc.player.getPosX()), Math.floor(mc.player.getPosY()), Math.floor(mc.player.getPosZ()));
    }

    private double getDistanceOfEntityToBlock(Entity entity, BlockPos pos) {
        return this.getDistance(entity.getPosX(), entity.getPosY(), entity.getPosZ(), pos.getX(), pos.getY(), pos.getZ());
    }

    private double getDistance(double x1, double y1, double z1, double x2, double y2, double z2) {
        double deltaX = x1 - x2;
        double deltaY = y1 - y2;
        double deltaZ = z1 - z2;
        return MathHelper.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
    }

    private void checkNotifications() {
        boolean totemInHands = this.isTotemInHands();
        boolean lowHealth = mc.player.getHealth() + this.getAbsorption() <= this.health.getValue();
        boolean crystalNearby = this.checkCrystal();
        boolean tntNearby = this.checkTnt();
        boolean tridentNearby = this.checkTrident();
        long currentTime = System.currentTimeMillis();

        if (this.autoTotemSwapped && totemInHands && currentTime - this.lastSwapTime < 1000L && this.hasAnyTotemInInventory()) {
            this.sendToastNotification("AutoTotem", "Тотем автоматически свапнут", true, "j");
            this.autoTotemSwapped = false;
        }

        if (this.autoTotemSwappedBack && !totemInHands && currentTime - this.lastSwapTime < 1000L && this.hasAnyTotemInInventory()) {
            this.sendToastNotification("AutoTotem", "Тотем автоматически убран", false, "j");
            this.autoTotemSwappedBack = false;
        }

        if (lowHealth && !this.lastLowHealth) {
            this.sendToastNotification("AutoTotem", "Низкое здоровье! Тотем активирован", true, "j");
        }

        if (crystalNearby && !this.lastCrystalNearby) {
            this.sendToastNotification("AutoTotem", "Эндер-кристалл рядом!", true, "j");
        }

        if (tntNearby && !this.lastTntNearby) {
            this.sendToastNotification("AutoTotem", "TNT рядом!", true, "j");
        }

        if (tridentNearby && !this.lastTridentNearby) {
            this.sendToastNotification("AutoTotem", "Трезубец рядом!", true, "j");
        }

        this.lastTotemInHand = totemInHands;
        this.lastLowHealth = lowHealth;
        this.lastCrystalNearby = crystalNearby;
        this.lastTntNearby = tntNearby;
        this.lastTridentNearby = tridentNearby;
    }

    private boolean hasAnyTotemInInventory() {
        for (int i = 0; i < 36; ++i) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack.getItem() == Items.TOTEM_OF_UNDYING) {
                return true;
            }
        }
        return false;
    }

    private void sendToastNotification(String title, String message, boolean isSuccess, String icon) {
        Notifications.getInstance().pushCustomWithIcon(title, message, isSuccess, icon);
    }

    public void onDisable() {
        super.onDisable();
        this.resetSwapBack();
    }

    @Generated
    public ModeSetting mode() {
        return this.mode;
    }

    @Generated
    public MultiBooleanSetting options() {
        return this.options;
    }

    @Generated
    public SliderSetting health() {
        return this.health;
    }

    @Generated
    public SliderSetting elytraHealth() {
        return this.elytraHealth;
    }

    @Generated
    public SliderSetting crystalDistance() {
        return this.crystalDistance;
    }

    @Generated
    public SliderSetting tntDistance() {
        return this.tntDistance;
    }

    @Generated
    public SliderSetting tridentDistance() {
        return this.tridentDistance;
    }

    @Generated
    public BooleanSetting noBall() {
        return this.noBall;
    }

    @Generated
    public BooleanSetting ignoreEnchanted() {
        return this.ignoreEnchanted;
    }

    @Generated
    public SwapHelpers swap() {
        return this.swap;
    }

    @Generated
    public int oldSlot() {
        return this.oldSlot;
    }

    @Generated
    public ItemStack oldOffhandItem() {
        return this.oldOffhandItem;
    }

    @Generated
    public int nonEnchantedTotems() {
        return this.nonEnchantedTotems;
    }

    @Generated
    public boolean lastTotemInHand() {
        return this.lastTotemInHand;
    }

    @Generated
    public boolean lastLowHealth() {
        return this.lastLowHealth;
    }

    @Generated
    public boolean lastCrystalNearby() {
        return this.lastCrystalNearby;
    }

    @Generated
    public boolean lastTntNearby() {
        return this.lastTntNearby;
    }

    @Generated
    public boolean lastTridentNearby() {
        return this.lastTridentNearby;
    }

    @Generated
    public boolean autoTotemSwapped() {
        return this.autoTotemSwapped;
    }

    @Generated
    public boolean autoTotemSwappedBack() {
        return this.autoTotemSwappedBack;
    }

    @Generated
    public long lastSwapTime() {
        return this.lastSwapTime;
    }
}
