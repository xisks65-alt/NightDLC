package net.minecraft.client.gui.screen.inventory;

import dev.wh1tew1ndows.client.Zetrix;
import dev.wh1tew1ndows.client.managers.module.impl.misc.BetterMinecraft;
import dev.wh1tew1ndows.client.utils.player.PlayerUtil;
import dev.wh1tew1ndows.client.utils.render.color.ColorUtil;
import dev.wh1tew1ndows.client.utils.render.draw.RenderUtil;
import dev.wh1tew1ndows.client.utils.render.draw.Round;
import dev.wh1tew1ndows.client.utils.render.draw.Scissor;
import dev.wh1tew1ndows.client.utils.render.shader.impl.GaussianBlur;
import net.minecraft.client.gui.IHasContainer;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ChestContainer;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.mojang.blaze3d.matrix.MatrixStack;
import net.mojang.blaze3d.systems.RenderSystem;

import java.util.ArrayList;
import java.util.List;

import static dev.wh1tew1ndows.client.api.interfaces.IMinecraft.mc;

public class ChestScreen extends ContainerScreen<ChestContainer> implements IHasContainer<ChestContainer> {
    /**
     * The ResourceLocation containing the chest GUI texture.
     */
    private static final ResourceLocation CHEST_GUI_TEXTURE = new ResourceLocation("textures/gui/container/generic_54.png");

    /**
     * Window height is calculated with these values; the more rows, the higher
     */
    private final int inventoryRows;

    @Override
    protected void init() {
        super.init();

        if ((title.getString().contains("Аукционы") || title.getString().contains("Поиск")) && PlayerUtil.isConnectedToServer("spookytime")) {
            // AutoBuy button removed
        }
        this.addButton(new Button(width / 2 + 95, height / 2 - 85, 100, 20,
                new StringTextComponent("Выбросить всё"), (button) -> {
            if (mc.player != null && mc.playerController != null) {
                dropItems();
            }
        }));
        this.addButton(new Button(width / 2 + 95, height / 2 - 60, 100, 20,
                new StringTextComponent("Забрать всё"), (button) -> {
            if (mc.player != null && mc.playerController != null) {
                PickItems();
            }
        }));
        this.addButton(new Button(width / 2 + 95, height / 2 - 35, 100, 20,
                new StringTextComponent("Сложить всё"), (button) -> {
            if (mc.player != null && mc.playerController != null) {
                BeckItems();
            }
        }));
    }


    public void dropItems() {
        int chestSlots = inventoryRows * 9;
        for (int i = 0; i < chestSlots; i++) {
            Slot slot = this.container.inventorySlots.get(i);
            ItemStack stack = slot.getStack();
            if (slot.getHasStack() && stack.getItem() != Items.AIR) {
                mc.playerController.windowClick(this.container.windowId, slot.slotNumber, 1, ClickType.THROW, mc.player);
            }
        }
    }

    public void PickItems() {
        int chestSlots = inventoryRows * 9;
        for (int i = 0; i < chestSlots; i++) {
            Slot slot = this.container.getSlot(i);
            ItemStack stack = slot.getStack();
            if (slot.getHasStack() && stack.getItem() != Items.AIR) {
                mc.playerController.windowClick(container.windowId, slot.slotNumber, 0, ClickType.QUICK_MOVE, mc.player);
            }
        }
    }

    public void BeckItems() {
        int chestSlots = inventoryRows * 9;
        int totalSlots = this.container.inventorySlots.size();
        for (int i = chestSlots; i < totalSlots; i++) {
            Slot slot = this.container.getSlot(i);
            ItemStack stack = slot.getStack();
            if (slot.getHasStack() && stack.getItem() != Items.AIR) {
                mc.playerController.windowClick(container.windowId, slot.slotNumber, 0, ClickType.QUICK_MOVE, mc.player);
            }
        }
    }


    private int msX = 0;
    private int msY = 0;

    public ChestScreen(ChestContainer container, PlayerInventory playerInventory, ITextComponent title) {
        super(container, playerInventory, title);
        this.passEvents = false;
        int i = 222;
        int j = 114;
        this.inventoryRows = container.getNumRows();
        this.ySize = 114 + this.inventoryRows * 18;
        this.playerInventoryTitleY = this.ySize - 94;
    }

    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        msX = mouseX;
        msY = mouseY;

        this.renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.renderHoveredTooltip(matrixStack, mouseX, mouseY);
    }


    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(mouseX, mouseY, button);
    }

    protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int x, int y) {
        if (BetterMinecraft.getInstance().blurInventory.getValue() && BetterMinecraft.getInstance().isEnabled()) {
            RenderUtil.Rounded.smooth(matrixStack, 0, 0, this.width, this.height,
                    ColorUtil.getColor(0, 0, 0, 80), Round.of(0));
            GaussianBlur.blur(12, 1);
        } else {
            RenderUtil.Rounded.drawRoundedRect(0, 0, minecraft.getMainWindow().getScaledWidth(), minecraft.getMainWindow().getScaledHeight(), 0,
                    ColorUtil.getColor(0, 0, 0, 60));
        }
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getTextureManager().bindTexture(CHEST_GUI_TEXTURE);
        int i = (this.width - this.xSize) / 2;
        int j = (this.height - this.ySize) / 2;
        this.blit(matrixStack, i, j, 0, 0, this.xSize, this.inventoryRows * 18 + 17);
        this.blit(matrixStack, i, j + this.inventoryRows * 18 + 17, 0, 126, this.xSize, 96);
    }
}

