package dev.wh1tew1ndows.client.managers.module.impl.render;

import dev.wh1tew1ndows.client.Zetrix;
import dev.wh1tew1ndows.client.api.events.orbit.EventHandler;
import dev.wh1tew1ndows.client.managers.events.render.EventRender;
import dev.wh1tew1ndows.client.managers.module.Category;
import dev.wh1tew1ndows.client.managers.module.Module;
import dev.wh1tew1ndows.client.managers.module.ModuleInfo;
import dev.wh1tew1ndows.client.managers.module.settings.impl.BooleanSetting;
import dev.wh1tew1ndows.client.managers.module.settings.impl.ModeSetting;
import dev.wh1tew1ndows.client.managers.module.settings.impl.MultiBooleanSetting;
import dev.wh1tew1ndows.client.utils.other.Instance;
import dev.wh1tew1ndows.client.utils.other.Project;
import dev.wh1tew1ndows.client.utils.render.color.ColorUtil;
import dev.wh1tew1ndows.client.utils.render.draw.RectUtil;
import dev.wh1tew1ndows.client.utils.render.font.Fonts;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraft.util.text.TextFormatting;
import net.mojang.blaze3d.matrix.MatrixStack;
import net.mojang.blaze3d.systems.RenderSystem;
import org.joml.Vector4d;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "Nametags TEST", category = Category.RENDER, desc = "Отображение имени предметов и энтити через стены")
public class NameTag2 extends Module {
    public static NameTag2 getInstance() {
        return Instance.get(NameTag2.class);
    }

    private static final float TAG_PADDING = 5.5f;
    private static final int BACKGROUND_COLOR = ColorUtil.getColor(20, 20, 20, 125);
    private static final int FRIEND_COLOR = ColorUtil.getColor(20, 250, 20, 85);
    private static final int BOX_COLOR = ColorUtil.getColor(20, 20, 20, 150);
    private static final int MAX_NAME_PARTS = 2;

    private final Map<Class<? extends Entity>, Map<Vector4d, Entity>> entityMaps = new HashMap<>();
    private final Map<Entity, String> cachedNames = new HashMap<>();
    private final Map<ItemStack, String> cachedSphereInfo = new HashMap<>();

    public final MultiBooleanSetting shown = new MultiBooleanSetting(this, "Отображать",
            BooleanSetting.of("Игроков", true),
            BooleanSetting.of("Мобов", false),
            BooleanSetting.of("Животных", false),
            BooleanSetting.of("Предметы", true),
            BooleanSetting.of("Себя", true)
    );

    public final MultiBooleanSetting elements = new MultiBooleanSetting(this, "Отображать у энтити",
            BooleanSetting.of("Боксы", true),
            BooleanSetting.of("Тэги", true),
            BooleanSetting.of("Броня и предметы", true)
    ).setVisible(() -> shown.getValue("Игроков") || shown.getValue("Себя"));

    public final MultiBooleanSetting item_elements = new MultiBooleanSetting(this, "Отображать у предметов",
            BooleanSetting.of("Боксы", true),
            BooleanSetting.of("Тэги", true)
    ).setVisible(() -> shown.getValue("Предметы"));

    public final ModeSetting boxType = new ModeSetting(this, "Стиль боксов", "Обычный", "Корнер");
    public final BooleanSetting ignore = new BooleanSetting(this, "Игнорировать голых", false);

    public NameTag2() {
        initEntityMaps();
    }

    private void initEntityMaps() {
        entityMaps.put(PlayerEntity.class, new HashMap<>());
        entityMaps.put(MonsterEntity.class, new HashMap<>());
        entityMaps.put(AnimalEntity.class, new HashMap<>());
        entityMaps.put(ItemEntity.class, new HashMap<>());
    }

    public void updatePositions(float partialTicks, Matrix4f matrix) {
        entityMaps.values().forEach(Map::clear);
        cachedNames.clear();
        cachedSphereInfo.clear();

        if (mc.world == null || matrix == null) return;

        mc.world.getAllEntities().forEach(entity -> {
            Vector4f pos4f = Project.getEntity2DPosition(matrix, entity, partialTicks);
            if (pos4f == null || pos4f.getX() >= Float.MAX_VALUE) return;
            
            Vector4d pos = new Vector4d(pos4f.getX(), pos4f.getY(), pos4f.getZ(), pos4f.getW());

            if (entity instanceof PlayerEntity player) {
                if (!shouldRenderPlayer(player)) return;
                entityMaps.get(PlayerEntity.class).put(pos, player);
            } else if (entity instanceof MonsterEntity) {
                if (!shown.getValue("Мобов")) return;
                entityMaps.get(MonsterEntity.class).put(pos, entity);
            } else if (entity instanceof AnimalEntity) {
                if (!shown.getValue("Животных")) return;
                entityMaps.get(AnimalEntity.class).put(pos, entity);
            } else if (entity instanceof ItemEntity) {
                if (!shown.getValue("Предметы")) return;
                entityMaps.get(ItemEntity.class).put(pos, entity);
            }
        });
    }


    private boolean shouldRenderPlayer(PlayerEntity player) {
        if (ignore.getValue() && isNaked(player)) return false;

        if (player == mc.player) {
            return !(mc.gameSettings.getPointOfView() == PointOfView.FIRST_PERSON || !shown.getValue("Себя"));
        }
        return shown.getValue("Игроков");
    }

    private String getEntityName(Entity entity) {
        return cachedNames.computeIfAbsent(entity, e -> {
            if (e instanceof PlayerEntity) {
                return e.getDisplayName().getString();
            } else {
                String name = e.getScoreboardName().replace("⚡ ", "");
                String[] parts = name.split("[  ]");
                if (parts.length > MAX_NAME_PARTS) {
                    name = name.replace((" " + parts[MAX_NAME_PARTS]), "");
                }
                return name;
            }
        });
    }

    private String getDonatPrefix(PlayerEntity player) {
        String displayName = player.getDisplayName().getString();
        String plainName = player.getGameProfile().getName();

        if (displayName.contains("§") && !displayName.equals(plainName)) {
            int nameIndex = displayName.indexOf(plainName);
            if (nameIndex > 0) {
                return displayName.substring(0, nameIndex);
            }

        }


        return "";
    }

    private String getItemName(Entity entity) {
        return cachedNames.computeIfAbsent(entity, e -> e.getDisplayName().getString());
    }

    private String getSphereInfo(ItemStack sphere) {
        if (sphere == null || sphere.isEmpty()) return "";
        // Упрощенная версия - просто показываем название предмета в оффхенде если он особенный
        if (sphere.getItem() != Items.PLAYER_HEAD && sphere.getItem() != Items.AIR) {
            return TextFormatting.DARK_GRAY + " [" + TextFormatting.YELLOW +
                    sphere.getDisplayName().getString() + TextFormatting.DARK_GRAY + "]";
        }
        return "";
    }

    public boolean isNaked(LivingEntity entity) {
        return entity.getItemStackFromSlot(EquipmentSlotType.HEAD).isEmpty()
                && entity.getItemStackFromSlot(EquipmentSlotType.CHEST).isEmpty()
                && entity.getItemStackFromSlot(EquipmentSlotType.LEGS).isEmpty()
                && entity.getItemStackFromSlot(EquipmentSlotType.FEET).isEmpty();
    }

    private void renderEntities(MatrixStack matrixStack, Class<? extends Entity> entityClass) {
        Map<Vector4d, Entity> entities = entityMaps.get(entityClass);
        if (entities.isEmpty()) return;

        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);

        List<Map.Entry<Vector4d, Entity>> sortedEntities = new ArrayList<>(entities.entrySet());
        sortedEntities.sort((e1, e2) -> Double.compare(e2.getKey().y, e1.getKey().y));

        for (Map.Entry<Vector4d, Entity> entry : sortedEntities) {
            Entity entity = entry.getValue();
            String name = getEntityName(entity);
            String health = "";
            String sphereText = "";
            String donatPrefix = "";

            if (entity instanceof LivingEntity living) {
                health = String.valueOf(Math.round(living.getHealth()));
                sphereText = getSphereInfo(living.getHeldItemOffhand());
                if (entity instanceof PlayerEntity player) {
                    donatPrefix = getDonatPrefix(player);
                }
            }

            renderEntityInfo(matrixStack, entity, entry.getKey(), name, health, sphereText, donatPrefix,
                    entity instanceof PlayerEntity);
        }
    }

    private void renderItems(MatrixStack matrixStack, Class<? extends Entity> entityClass) {
        Map<Vector4d, Entity> entities = entityMaps.get(entityClass);
        if (entities.isEmpty()) return;

        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);

        List<Map.Entry<Vector4d, Entity>> sortedEntities = new ArrayList<>(entities.entrySet());
        sortedEntities.sort((e1, e2) -> Float.compare((float) e2.getKey().y, (float) e1.getKey().y));

        for (Map.Entry<Vector4d, Entity> entry : sortedEntities) {
            Entity entity = entry.getValue();
            String name = getItemName(entity);

            renderEntityInfo(matrixStack, entity, entry.getKey(), name, "", "", "",
                    false);
        }
    }

    private void renderEntityInfo(MatrixStack matrixStack, Entity entity, Vector4d pos,
                                  String name, String health, String sphereText, String donatPrefix, boolean isPlayer) {
        if (pos == null) return;

        float x = (float) pos.x;
        float y = (float) pos.y;
        float endX = (float) pos.z;
        float endY = (float) pos.w;

        boolean isFriend = false;
        if (isPlayer && entity instanceof PlayerEntity player) {
            isFriend = Zetrix.inst().friendManager().isFriend(player.getGameProfile().getName());
        }
        boolean showElements = isPlayer ? elements.getValue("Боксы") : item_elements.getValue("Боксы");

        if (showElements) {
            drawBox(boxType.getIndex(), x, y, endX, endY, isFriend);
        }

        String tagText = formatTagText(name, health, sphereText, donatPrefix, isPlayer);

        if ((isPlayer && elements.getValue("Тэги")) || (!isPlayer && item_elements.getValue("Тэги"))) {
            renderTag(matrixStack, tagText, x, y, endX, isFriend);
        }

        if (isPlayer && elements.getValue("Броня и предметы") && entity instanceof PlayerEntity) {
            renderPlayerItems(matrixStack, (PlayerEntity) entity, x, y, endX);
        }
    }

    private String formatTagText(String name, String health, String sphereText, String donatPrefix, boolean isPlayer) {
        if (!isPlayer) return name;

        String displayName = name.contains(mc.player.getScoreboardName()) ?
                TextFormatting.WHITE + mc.getSession().getUsername() : name;

        return displayName +
                TextFormatting.DARK_GRAY + " [" + TextFormatting.RED + health +
                TextFormatting.DARK_GRAY + "]" + sphereText;
    }

    public static void drawItemStack(ItemStack stack, float x, float y, boolean overlay, boolean scale, float scaleValue) {
        RenderSystem.enableDepthTest();
        RenderSystem.pushMatrix();
        RenderSystem.enableBlend();

        RenderSystem.translatef(x, y, 0);
        if (scale) GL11.glScaled(scaleValue, scaleValue, scaleValue);

        mc.getItemRenderer().renderItemAndEffectIntoGUI(stack, 0, 0);
        if (overlay) mc.getItemRenderer().renderItemOverlays(mc.fontRenderer, stack, 0, 0);

        RenderSystem.disableBlend();
        RenderSystem.popMatrix();
        RenderSystem.disableDepthTest();
    }

    private void renderPlayerItems(MatrixStack matrixStack, PlayerEntity player, float x, float y, float endX) {
        ItemStack mainHand = player.getHeldItemMainhand();
        ItemStack offHand = player.getHeldItemOffhand();
        float yPos = y - (elements.getValue("Тэги") ? 22 : 11);

        if (!mainHand.isEmpty()) {
            drawItemStack(mainHand, (x + (endX - x) / 2) - 25, yPos, true, true, 0.45f);
        }
        if (!offHand.isEmpty()) {
            drawItemStack(offHand, (x + (endX - x) / 2 - 15), yPos, true, true, 0.45f);
        }

        float itemX = (x + (endX - x) / 2 - 5);
        for (ItemStack armor : player.getArmorInventoryList()) {
            if (!armor.isEmpty()) {
                drawItemStack(armor, itemX, yPos, true, true, 0.45f);
                itemX += 10;
            }
        }
    }

    private void renderTag(MatrixStack matrixStack, String text, float x, float y, float endX, boolean isFriend) {
        float textWidth = Fonts.SFP_SEMIBOLD.getWidth(text, 7);
        float centerX = x + (endX - x) / 2.0f;
        float textX = centerX - (textWidth / 2.0f);
        float rectStartX = textX - TAG_PADDING;
        float rectEndX = textX + textWidth + TAG_PADDING;
        float finalY = y - 5;

        if (isFriend) {
            RectUtil.drawRect(matrixStack, rectStartX - 2, finalY - 8, rectEndX + 2 - (rectStartX - 2), 10, FRIEND_COLOR);
        } else {
            RectUtil.drawRect(matrixStack, rectStartX + 3.5f, finalY - 8, rectEndX - 1 - (rectStartX + 3.5f), 9, BACKGROUND_COLOR);
        }

        matrixStack.push();
        matrixStack.translate(0, 0, 1);
        Fonts.SFP_SEMIBOLD.draw(matrixStack, text, textX, finalY - 4.5f, -1, 7);
        matrixStack.pop();
    }

    private void drawBox(int type, float x, float y, float endX, float endY, boolean isFriend) {
        int boxColor = BOX_COLOR;
        int rectColor = InterFace.getInstance().themeColor();

        if (type == 0) {
            drawSimpleBox(x, y, endX, endY, boxColor, rectColor);
        } else if (type == 1) {
            drawCornerBox(x, y, endX, endY, boxColor, rectColor);
        }
    }

    private void drawSimpleBox(float x, float y, float endX, float endY, int boxColor, int rectColor) {
        MatrixStack ms = new MatrixStack();
        RectUtil.drawRect(ms, x + 0.5f, y + 0.5f, endX - x - 1, 1.5f, boxColor);
        RectUtil.drawRect(ms, x + 0.5f, endY - 2, endX - x - 1, 1.5f, boxColor);
        RectUtil.drawRect(ms, x + 0.5f, y + 2, 1.5f, endY - y - 4, boxColor);
        RectUtil.drawRect(ms, endX - 2, y + 2, 1.5f, endY - y - 4, boxColor);

        RectUtil.drawRect(ms, x + 1, y + 1, endX - x - 2, 0.5f, rectColor);
        RectUtil.drawRect(ms, x + 1, endY - 1.5f, endX - x - 2, 0.5f, rectColor);
        RectUtil.drawRect(ms, x + 1, y + 1, 0.5f, endY - y - 2, rectColor);
        RectUtil.drawRect(ms, endX - 1.5f, y + 1.5f, 0.5f, endY - y - 2.5f, rectColor);
    }

    private void drawCornerBox(float x, float y, float endX, float endY, int boxColor, int rectColor) {
        float cornerSize = Math.min(Math.abs(endX - x) * 0.25f, 15);
        float accentWidth = 1.5f;

        drawCorner(x, y, cornerSize, boxColor, rectColor, 0);
        drawCorner(endX - cornerSize, y, cornerSize, boxColor, rectColor, 1);
        drawCorner(x, endY - cornerSize, cornerSize, boxColor, rectColor, 2);
        drawCorner(endX - cornerSize, endY - cornerSize, cornerSize, boxColor, rectColor, 3);
    }

    private void drawCorner(float x, float y, float size, int boxColor, int rectColor, int corner) {
        MatrixStack ms = new MatrixStack();
        float accentWidth = 1.5f;
        boolean isRight = corner == 1 || corner == 3;
        boolean isBottom = corner == 2 || corner == 3;

        if (!isBottom) {
            RectUtil.drawRect(ms, x, y, size, 2, boxColor);
            RectUtil.drawRect(ms, x + 1, y + 1, size - 2, accentWidth - 1, rectColor);
        } else {
            RectUtil.drawRect(ms, x, y + size - 2, size, 2, boxColor);
            RectUtil.drawRect(ms, x + 1, y + size - accentWidth, size - 2, accentWidth - 1, rectColor);
        }

        if (!isRight) {
            RectUtil.drawRect(ms, x, y, 2, size, boxColor);
            RectUtil.drawRect(ms, x + 1, y + 1, accentWidth - 1, size - 2, rectColor);
        } else {
            RectUtil.drawRect(ms, x + size - 2, y, 2, size, boxColor);
            RectUtil.drawRect(ms, x + size - accentWidth, y + 1, accentWidth - 1, size - 2, rectColor);
        }
    }

    private Matrix4f projectionMatrix;


    @EventHandler
    public void onRender2D(EventRender render) {
        if (mc.world == null) return;

        if (render.isRender3D()) {
            projectionMatrix = render.matrix;
            updatePositions(render.partialTicks, projectionMatrix);
        }

        if (render.isRender2D()) {
            if (shown.get("Игроков").getValue() || shown.get("Себя").getValue()) {
                renderEntities(render.matrixStack, PlayerEntity.class);
            }
            if (shown.get("Предметы").getValue()) {
                renderItems(render.matrixStack, ItemEntity.class);
            }
            if (shown.get("Мобов").getValue()) {
                renderItems(render.matrixStack, MonsterEntity.class);
            }
            if (shown.get("Животных").getValue()) {
                renderItems(render.matrixStack, AnimalEntity.class);
            }
        }
    }
}



