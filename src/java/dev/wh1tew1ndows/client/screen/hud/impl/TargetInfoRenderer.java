/*package dev.wh1tew1ndows.client.screen.hud.impl;

import dev.wh1tew1ndows.client.managers.events.player.AttackEvent;
import dev.wh1tew1ndows.client.managers.events.render.Render2DEvent;
import dev.wh1tew1ndows.client.managers.module.impl.combat.AttackAura;
import dev.wh1tew1ndows.client.managers.module.impl.misc.FixHP;
import dev.wh1tew1ndows.client.managers.module.impl.render.InterFace;
import dev.wh1tew1ndows.client.managers.module.settings.impl.DragSetting;
import dev.wh1tew1ndows.client.screen.hud.IAttack;
import dev.wh1tew1ndows.client.screen.hud.IRenderer;
import dev.wh1tew1ndows.client.utils.animation.Animation;
import dev.wh1tew1ndows.client.utils.animation.NumberTransition;
import dev.wh1tew1ndows.client.utils.animation.util.Easings;
import dev.wh1tew1ndows.client.utils.math.Mathf;
import dev.wh1tew1ndows.client.utils.player.PlayerUtil;
import dev.wh1tew1ndows.client.utils.render.color.ColorFormatting;
import dev.wh1tew1ndows.client.utils.render.color.ColorUtil;
import dev.wh1tew1ndows.client.utils.render.draw.RectUtil;
import dev.wh1tew1ndows.client.utils.render.draw.RenderUtil;
import dev.wh1tew1ndows.client.utils.render.draw.RenderUtil.Rounded;
import dev.wh1tew1ndows.client.utils.render.draw.Round;
import dev.wh1tew1ndows.client.utils.render.font.Fonts;
import dev.wh1tew1ndows.client.utils.render.font.StripFont;
import dev.wh1tew1ndows.client.utils.render.particle.Particle;
import dev.wh1tew1ndows.common.impl.fastrandom.FastRandom;
import dev.wh1tew1ndows.lib.util.time.StopWatch;
import lombok.AccessLevel;
import lombok.Generated;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.Score;
import net.minecraft.util.Namespaced;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.mojang.blaze3d.matrix.MatrixStack;
import net.mojang.blaze3d.platform.GlStateManager;
import net.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.StreamSupport;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TargetHudRenderer implements IRenderer, IAttack {
    private final DragSetting drag;
    private final Animation openAnimation = new Animation();
    private final Animation healthAnimation = new Animation();
    private final Animation absorptionAnimation = new Animation();
    private final StopWatch time = new StopWatch();
    public boolean inWorld;
    public LivingEntity target;
    private final List<Particle> particles = new ArrayList();
    private final ResourceLocation bloom = new Namespaced("particle/glow.png");
    private final Random random = new FastRandom();
    private final float radius = 3.0F;
    private final Round round = Round.of(3.0F);
    private final StripFont stripFont = new StripFont();
    private float barX;
    private float barY;
    private float barW;
    private float barH;
    private final long lastFrameNanos = 0L;
    private final boolean firstFrame = true;
    private float tempHp = 0.0F;
    private final float tempAbsorption = 0.0F;

    // Для независимого от FPS спавна партиклов
    private long lastHeadParticleSpawn = 0L;
    private static final long HEAD_PARTICLE_SPAWN_INTERVAL = 200L; // 200ms между спавнами

    public TargetHudRenderer(DragSetting drag) {
        this.drag = drag;
    }


    @Override
    public void render(Render2DEvent event) {
        AttackAura killAura = AttackAura.getInstance();
        if (mc.pointedEntity instanceof PlayerEntity player) {
            target = player;
            time.reset();
        }
        if (killAura.isEnabled() && killAura.target != null) {
            target = killAura.target;
            time.reset();
        }
        if (mc.currentScreen instanceof ChatScreen) {
            target = mc.player;
            time.reset();
        }
        if (target == null) {
            inWorld = false;
            return;
        }

        openAnimation.update();
        healthAnimation.update();

        inWorld = StreamSupport.stream(mc.world.getAllEntities().spliterator(), true).anyMatch(entity -> entity.equals(target));

        boolean out = (!inWorld || time.finished(1000));

        openAnimation.run(out ? 0.0 : 1.0, 0.3, out ? Easings.CUBIC_IN : Easings.CUBIC_OUT, true);
        if (openAnimation.getValue() <= 0.0) {
            return;
        }

        particles.removeIf(Particle::isFinished);

        particles.forEach(particle -> particle.update(Mathf.deltaTime()));

        // Автоматический спавн партиклов за головой


        drawDefault(event);
    }


    private void drawDefault(Render2DEvent renderEvent) {
        MatrixStack matrixStack = renderEvent.getMatrix();
        if (InterFace.getInstance().targetHUD.is("1")) {
            float x = this.drag.position.x;
            float y = this.drag.position.y + (-3F + 3F * this.openAnimation.get());
            float width = this.drag.size.x;
            float height = this.drag.size.y;
            float cornerRadius = 3.0F;
            float padding = 4.0F;
            float textOffset = 12.0F;
            float barWidth = width - 33;
            this.drag.size.set(90, 29.5F);
            float textX = x + padding + textOffset + padding;
            float textY = y + padding;
            matrixStack.push();
            RenderUtil.clientStyledRectDark(matrixStack, x, y, width, height, this.openAnimation.get(), 6);
            int backgroundColor = ColorUtil.replAlpha(ColorUtil.getColor(15), this.openAnimation.get() * 0.35F);
            LivingEntity targetEntity = this.target;
            float currentHealth;
            float maxHealth;
            if (targetEntity instanceof AbstractClientPlayerEntity clientPlayer) {
                currentHealth = !this.inWorld ? 0.0F : (float) Mathf.round(clientPlayer.getHealthFixed(), 1);
                maxHealth = clientPlayer.getMaxHealth();
            } else {
                currentHealth = !this.inWorld ? 0.0F : (float) Mathf.round(this.target.getHealth(), 1);
                maxHealth = this.target.getMaxHealth();
            }


            // Get absorption amount
            float absorptionAmount = this.target.getAbsorptionAmount();

            // Absorption bar animation
            float totalHealth = maxHealth + absorptionAmount;
            float absorptionRatio = totalHealth <= 0.0F ? 0.0F : absorptionAmount / totalHealth;
            this.absorptionAnimation.run(Mathf.clamp01(absorptionRatio), 0.3, Easings.SINE_OUT, true);
            int healthBarBackgroundColor = ColorUtil.replAlpha(ColorUtil.multDark(ColorUtil.fade(0), 0.5F), (int) ((double) 255.0F * this.openAnimation.getValue()));
            int healthBarColor = ColorUtil.replAlpha(ColorUtil.fade(90), (int) ((double) 255.0F * this.openAnimation.getValue()));
            float iconSize = 7.0F;
            this.barH = 6.0F;
            this.barW = barWidth;
            this.barX = x + 29;

            RenderUtil.bindTexture(new Namespaced("particle/starnew.png"));
            if (InterFace.getInstance().targetHudParticles.getValue()) {
                for (Particle particle : this.particles) {
                    // Позиция партиклов за головой (центр головы + смещение)
                    float headCenterX = x + 1.0F + 12.0F; // x + 1.0F (позиция головы) + 12.0F (половина ширины головы 24px)
                    float headCenterY = textY - 3.0F + 12.0F; // textY - 3.0F (позиция головы) + 12.0F (половина высоты головы 24px)

                    particle.setBaseX(headCenterX + particle.getOffsetX());
                    particle.setBaseY(headCenterY + particle.getOffsetY());
                    particle.getAnimation().run((double) particle.getTimePC(1500L) < (double) 0.5F ? (double) 1.0F : (double) 0.0F, 0.75F, Easings.SINE_OUT, true);
                    int particleColor = ColorUtil.multAlpha(this.theme().clientColor(), particle.getAnimation().get() * this.openAnimation.get());
                    RectUtil.drawRect(matrixStack, particle.getBaseX() - particle.getSize(), particle.getBaseY() - particle.getSize(), particle.getSize() * 2.0F, particle.getSize() * 2.0F, particleColor, true, true);
                }
            }

            float hurtAnimation = ((float) this.target.hurtTime - (this.target.hurtTime != 0 ? mc.timer.renderPartialTicks : 0.0F)) / 10.0F;

            String rs = EntityType.getKey(target.getType()).getPath();
            ResourceLocation skin = target instanceof AbstractClientPlayerEntity e ? (e).getLocationSkin() : new ResourceLocation("textures/entity/" + rs + ".png");
            float hurtPercent = (target.hurtTime - (target.hurtTime != 0 ? mc.timer.renderPartialTicks : 0.0f)) / 10.0f;
            // Only try to draw head for players
            if (this.target instanceof AbstractClientPlayerEntity) {
                RenderUtil.drawHead(skin, x + 1.0F, textY - 3.25F, 24, 24, 3, 1 * this.openAnimation.get(), hurtPercent);
            } else {
                // For non-players, draw a placeholder
                Rounded.smooth(matrixStack, x + 3.25F, textY - 1.0F, 24, 24, backgroundColor, Round.of(4));
                Fonts.MONTSERRAT_MEDIUM.draw(matrixStack, "?", x + 12.8F, textY + 5.5F, ColorUtil.replAlpha(-1, this.openAnimation.get() * 0.8F), 10);
            }


            FloatFormatter formatter = new FloatFormatter();
            float formattedHealth = formatter.format(currentHealth + (PlayerUtil.isFuntime() && PlayerUtil.isBedwars() ? 0 : absorptionAmount));
            float hp = target.getHealth();

            Score score = null;
            // Только для не-клиентских игроков получаем счет из scoreboard
            if (!(target instanceof ClientPlayerEntity) && target != mc.player && mc.world != null && mc.world.getScoreboard() != null) {
                try {
                    String scoreboardName = target.getScoreboardName();
                    if (scoreboardName != null && !scoreboardName.isEmpty()) {
                        score = mc.world.getScoreboard().getOrCreateScore(scoreboardName, mc.world.getScoreboard().getObjectiveInDisplaySlot(2));
                    }
                } catch (Exception e) {
                    // Игнорируем ошибки при получении счета
                    score = null;
                }
            }

            if (target instanceof PlayerEntity && FixHP.getInstance().isEnabled() && score != null) {
                if (score.getScorePoints() != 0) {
                    hp = score.getScorePoints();
                }
            }

            int finalHp = (int) (hp + target.getAbsorptionAmount());
            if (PlayerUtil.isFuntime()) {
                this.tempHp = NumberTransition.resultspeed(this.tempHp, target.getHealthFixed(), 0.15F);
            } else if (PlayerUtil.isBedwars()) {
                this.tempHp = NumberTransition.resultspeed(this.tempHp, target.getHealthFixed(), 0.15F);
            } else {
                this.tempHp = NumberTransition.resultspeed(this.tempHp, finalHp, 0.15F);
            }

            String healthText = String.format("%.1f", this.tempHp);
            if (finalHp > 100) {
                healthText = "null";
            }


            String hpDisplayText = "HP:" + ColorFormatting.reset() + healthText.replace(",", ".");
            float fontSize = 7.5F;

            String entityName;

            entityName = this.target.getName().getString();

            float healthRatio = maxHealth <= 0.0F ? 0.0F : finalHp / maxHealth;
            this.healthAnimation.run(Mathf.clamp01(healthRatio), 0.3, Easings.SINE_OUT, true);

            // Fonts.ICON_DESHUX.draw(matrixStack, "p", textX + 10 + iconSize, textY + textOffset - fontSize - padding + 11.0F, ColorUtil.multAlpha(InterFace.getInstance().themeColor(), openAnimation.get()), 7);

            this.stripFont.draw(Fonts.SFX_MEDIUM, matrixStack, entityName, textX + 3 + iconSize, textY, 55F, ColorUtil.multAlpha(ColorUtil.getColor(220), this.openAnimation.get()), 8, 2.0F);


            this.stripFont.draw(Fonts.SFX_SEMIBOLD, matrixStack, hpDisplayText, textX + 3 + iconSize, textY + textOffset - fontSize - padding + 8, 38, ColorUtil.multAlpha(ColorUtil.getColor(220), this.openAnimation.get()), 6.5F, 2.0F);

            this.barY = y + 22F;
            float round = 1.5F, heis = 4;
            Rounded.smooth(matrixStack, this.barX, this.barY, this.barW, heis, backgroundColor, Round.of(round));
            RenderUtil.Shadow.drawShadow(matrixStack, this.barX, this.barY, this.barW * this.healthAnimation.get(), heis, 6, openAnimation.get(), ColorUtil.replAlpha(ColorUtil.fade(0), 1 * openAnimation.get()), ColorUtil.replAlpha(ColorUtil.fade(90), 1 * openAnimation.get()), ColorUtil.replAlpha(ColorUtil.fade(180), 1 * openAnimation.get()), ColorUtil.replAlpha(ColorUtil.fade(270), 1 * openAnimation.get()), Round.of(round));
            Rounded.smooth(matrixStack, this.barX, this.barY, this.barW * this.healthAnimation.get(), heis, ColorUtil.replAlpha(ColorUtil.fade(0), 1 * openAnimation.get()), ColorUtil.replAlpha(ColorUtil.fade(90), 1 * openAnimation.get()), ColorUtil.replAlpha(ColorUtil.fade(180), 1 * openAnimation.get()), ColorUtil.replAlpha(ColorUtil.fade(270), 1 * openAnimation.get()), Round.of(round));


            // Draw absorption bar (golden) if absorption > 0
            if (absorptionAmount > 0.0F && !(PlayerUtil.isFuntime() || PlayerUtil.isBedwars())) {
                int absorptionColor = ColorUtil.replAlpha(ColorUtil.getColor(220, 200, 0), (int) (255.0F * this.openAnimation.getValue()));
                int absorptionColorDark = ColorUtil.replAlpha(ColorUtil.getColor(150, 100, 0), (int) (255.0F * this.openAnimation.getValue()));
                float absorptionBarY = this.barY; // Position below the main HP bar
                Rounded.smooth(matrixStack, this.barX, absorptionBarY, this.barW, heis, backgroundColor, Round.of(round));
                Rounded.smooth(matrixStack, this.barX, absorptionBarY, this.barW * this.absorptionAnimation.get(), heis, absorptionColorDark, absorptionColor, Round.of(round));
            }
            if (this.target instanceof PlayerEntity) {
                this.drawArmorRow(this.target, x + width - 2.0F - width / 2.0F - 10, y, width, matrixStack);
            }
            matrixStack.pop();
            // Only draw armor for players
        } else if (InterFace.getInstance().targetHUD.is("2")) {
            float x = drag.position.x;
            float y = drag.position.y + (-15 + (15 * openAnimation.get()));

            float width = drag.size.x;
            float height = drag.size.y;

            float hpHeight = 3.0F;
            float margin = 4.0F;
            float avatarSize = 12;
            float hpWidth = width - 37;

            // чтобы влезало
            drag.size.set(100, 35);

            float appendX = x + margin + (avatarSize + margin);
            float appendY = y + margin;

            matrixStack.push();

            RenderUtil.clientStyledRectDark(matrixStack, x, y, width, height, openAnimation.get(), 7);

            int color = ColorUtil.replAlpha(ColorUtil.getColor(15), openAnimation.get() * 0.35F);

            // ====== HEALTH раньше — для привязки партиклов ======
            float health;
            float maxHealth;
            if (target instanceof AbstractClientPlayerEntity player) {
                health = !inWorld ? 0 : (float) Mathf.round(player.getHealthFixed(), 1);
                maxHealth = player.getMaxHealth();
            } else {
                health = !inWorld ? 0 : (float) Mathf.round(target.getHealth(), 1);
                maxHealth = target.getMaxHealth();
            }
            float healthBar = (maxHealth <= 0 ? 0F : (health / maxHealth));
            this.healthAnimation.run(Mathf.clamp01(healthBar), 0.8, Easings.SINE_OUT, true);

            // ====== Параметры HP-бара ======
            int hpColor1 = ColorUtil.replAlpha(ColorUtil.multDark(InterFace.getInstance().themeColor(), 0.25F), (int) (255 * openAnimation.getValue()));
            int hpColor2 = ColorUtil.replAlpha(ColorUtil.multDark(InterFace.getInstance().themeColor(), 0.8F), (int) (255 * openAnimation.getValue()));

            barH = 6;
            barW = hpWidth;
            barX = x + margin + 30;
            barY = appendY + 29 - 3 - 6 + 0.15F; // верх бара (центр = + barH/2)

            float barCenterY = barY + barH / 2F;
            float tipX = barX + barW * healthAnimation.get(); // кончик заполнения

            // ====== РЕНДЕР ПАРТИКЛОВ ПО БАРУ HP ======
            RenderUtil.bindTexture(bloom);
            for (Particle particle : particles) {
                particle.setBaseX(drag.position.x + margin + 29 / 2);
                particle.setBaseY(drag.position.y + margin + 29 / 2);
                particle.getAnimation().run(particle.getTimePC(1500) < 0.5 ? 1 : 0, 1.500 / 2D, Easings.SINE_OUT, true);
                int white = ColorUtil.multAlpha(theme().clientColor(), particle.getAnimation().get() * openAnimation.get());

                RectUtil.drawRect(matrixStack,
                        particle.getBaseX() - (particle.getSize()),
                        particle.getBaseY() - (particle.getSize()),
                        particle.getSize() * 2.0F,
                        particle.getSize() * 2.0F,
                        white, true, true);
            }
            // ====== АВАТАР/ИМЯ ======
            String rs = EntityType.getKey(target.getType()).getPath();
            ResourceLocation skin = target instanceof AbstractClientPlayerEntity e ? (e).getLocationSkin() : new ResourceLocation("textures/entity/" + rs + ".png");
            float hurtPercent = (target.hurtTime - (target.hurtTime != 0 ? mc.timer.renderPartialTicks : 0.0f)) / 10.0f;
            if (target instanceof AbstractClientPlayerEntity) {
                RenderUtil.drawHead(skin, x + 1F, appendY - 3, 29, 29, 4, openAnimation.get(), hurtPercent);
            } else {
                RenderUtil.Rounded.smooth(matrixStack, x + 3.25F, appendY - 1, 29, 29, color, Round.of(6));
                Fonts.MONTSERRAT_MEDIUM.draw(matrixStack, "?", x + 14.5F, appendY + 7, ColorUtil.replAlpha(-1, openAnimation.get() * 1), 12);
            }

            String name = target.getName().getString();
            stripFont.draw(Fonts.MONTSERRAT_MEDIUM, matrixStack, name, appendX + 15, appendY + 0.5F, 60, ColorUtil.multAlpha(-1, openAnimation.get()), 8.7F, 2);

            // ====== HP текст ======
            float hp = target.getHealth();
            float maxHpObj = target.getMaxHealth();
            Score score = mc.world.getScoreboard().getOrCreateScore(target.getScoreboardName(), mc.world.getScoreboard().getObjectiveInDisplaySlot(2));
            if (target instanceof PlayerEntity && FixHP.getInstance().isEnabled()) {
                if (score.getScorePoints() != 0) {
                    hp = score.getScorePoints();
                }
            }
            FloatFormatter formatter = new FloatFormatter();
            float finalHp = PlayerUtil.isFuntime() ? formatter.format(hp) : formatter.format(hp + target.getAbsorptionAmount());

            tempHp = NumberTransition.resultspeed(tempHp, (int) finalHp);
            String hpText = "HP: " + (int) (tempHp);
            float fontSize = 7.5F;
            stripFont.draw(Fonts.MONTSERRAT_MEDIUM, matrixStack, hpText,
                    appendX + 15, appendY + avatarSize - fontSize - margin + 10,
                    50, ColorUtil.multAlpha(ColorUtil.getColor(225), openAnimation.get() * 0.9F), fontSize, 2);

            // ====== РИСУЕМ HP-БАР (перекроет частицы) ======
            RenderUtil.Rounded.smooth(matrixStack, barX, barY, barW, 7, color, Round.of(2.5F));
            RenderUtil.Rounded.smooth(matrixStack, barX, barY, barW * healthAnimation.get(), 7, hpColor1, hpColor2, Round.of(2.5F));

            matrixStack.pop();

            // ====== РЕНДЕР ОРУЖИЯ/БРОНИ ======
            if (target instanceof PlayerEntity) {
                drawArmorRow((PlayerEntity) target, x, y, width);
            }
        } else {
            float x = this.drag.position.x;
            float y = this.drag.position.y + (-3F + 3F * this.openAnimation.get());
            float width = this.drag.size.x;
            float height = this.drag.size.y;
            float cornerRadius = 3.0F;
            float padding = 4.0F;
            float textOffset = 12.0F;
            float barWidth = width - 33;
            this.drag.size.set(112.5F, 29.5F);
            float textX = x + padding + textOffset + padding;
            float textY = y + padding;
            matrixStack.push();
            RenderUtil.clientStyledRectDark(matrixStack, x, y, width, height, this.openAnimation.get(), 6);
            int backgroundColor = ColorUtil.replAlpha(ColorUtil.getColor(15), this.openAnimation.get() * 0.35F);
            LivingEntity targetEntity = this.target;
            float currentHealth;
            float maxHealth;
            if (targetEntity instanceof AbstractClientPlayerEntity clientPlayer) {
                currentHealth = !this.inWorld ? 0.0F : (float) Mathf.round(clientPlayer.getHealthFixed(), 1);
                maxHealth = clientPlayer.getMaxHealth();
            } else {
                currentHealth = !this.inWorld ? 0.0F : (float) Mathf.round(this.target.getHealth(), 1);
                maxHealth = this.target.getMaxHealth();
            }


            // Get absorption amount
            float absorptionAmount = this.target.getAbsorptionAmount();

            // Absorption bar animation
            float totalHealth = maxHealth + absorptionAmount;
            float absorptionRatio = totalHealth <= 0.0F ? 0.0F : absorptionAmount / totalHealth;
            this.absorptionAnimation.run(Mathf.clamp01(absorptionRatio), 0.3, Easings.SINE_OUT, true);
            int healthBarBackgroundColor = ColorUtil.replAlpha(ColorUtil.multDark(ColorUtil.fade(0), 0.5F), (int) ((double) 255.0F * this.openAnimation.getValue()));
            int healthBarColor = ColorUtil.replAlpha(ColorUtil.fade(90), (int) ((double) 255.0F * this.openAnimation.getValue()));
            float iconSize = 7.0F;
            this.barH = 6.0F;
            this.barW = barWidth;
            this.barX = x + 29;

            RenderUtil.bindTexture(new Namespaced("particle/glow.png"));
            if (InterFace.getInstance().targetHudParticles.getValue()) {
                for (Particle particle : this.particles) {
                    // Позиция партиклов за головой (центр головы + смещение)
                    float headCenterX = x + 1.0F + 12.0F; // x + 1.0F (позиция головы) + 12.0F (половина ширины головы 24px)
                    float headCenterY = textY - 3.0F + 12.0F; // textY - 3.0F (позиция головы) + 12.0F (половина высоты головы 24px)

                    particle.setBaseX(headCenterX + particle.getOffsetX());
                    particle.setBaseY(headCenterY + particle.getOffsetY());
                    particle.getAnimation().run((double) particle.getTimePC(1500L) < (double) 0.5F ? (double) 1.0F : (double) 0.0F, 0.75F, Easings.SINE_OUT, true);
                    int particleColor = ColorUtil.multAlpha(this.theme().clientColor(), particle.getAnimation().get() * this.openAnimation.get());
                    RectUtil.drawRect(matrixStack, particle.getBaseX() - particle.getSize(), particle.getBaseY() - particle.getSize(), particle.getSize() * 2.0F, particle.getSize() * 2.0F, particleColor, true, true);
                }
            }

            float hurtAnimation = ((float) this.target.hurtTime - (this.target.hurtTime != 0 ? mc.timer.renderPartialTicks : 0.0F)) / 10.0F;

            String rs = EntityType.getKey(target.getType()).getPath();
            ResourceLocation skin = target instanceof AbstractClientPlayerEntity e ? (e).getLocationSkin() : new ResourceLocation("textures/entity/" + rs + ".png");
            float hurtPercent = (target.hurtTime - (target.hurtTime != 0 ? mc.timer.renderPartialTicks : 0.0f)) / 10.0f;
            // Only try to draw head for players
            if (this.target instanceof AbstractClientPlayerEntity) {
                RenderUtil.drawHead(skin, x + 1.0F, textY - 3.25F, 24, 24, 3, 1 * this.openAnimation.get(), hurtPercent);
            } else {
                // For non-players, draw a placeholder
                Rounded.smooth(matrixStack, x + 3.25F, textY - 1.0F, 24, 24, backgroundColor, Round.of(4));
                Fonts.MONTSERRAT_MEDIUM.draw(matrixStack, "?", x + 12.8F, textY + 5.5F, ColorUtil.replAlpha(-1, this.openAnimation.get() * 0.8F), 10);
            }


            FloatFormatter formatter = new FloatFormatter();
            float formattedHealth = formatter.format(currentHealth + (PlayerUtil.isFuntime() && PlayerUtil.isBedwars() ? 0 : absorptionAmount));
            float hp = target.getHealth();

            Score score = null;
            // Только для не-клиентских игроков получаем счет из scoreboard
            if (!(target instanceof ClientPlayerEntity) && target != mc.player && mc.world != null && mc.world.getScoreboard() != null) {
                try {
                    String scoreboardName = target.getScoreboardName();
                    if (scoreboardName != null && !scoreboardName.isEmpty()) {
                        score = mc.world.getScoreboard().getOrCreateScore(scoreboardName, mc.world.getScoreboard().getObjectiveInDisplaySlot(2));
                    }
                } catch (Exception e) {
                    // Игнорируем ошибки при получении счета
                    score = null;
                }
            }

            if (target instanceof PlayerEntity && FixHP.getInstance().isEnabled() && score != null) {
                if (score.getScorePoints() != 0) {
                    hp = score.getScorePoints();
                }
            }

            int finalHp = (int) (hp + target.getAbsorptionAmount());
            if (PlayerUtil.isFuntime()) {
                this.tempHp = NumberTransition.resultspeed(this.tempHp, target.getHealthFixed(), 0.15F);
            } else if (PlayerUtil.isBedwars()) {
                this.tempHp = NumberTransition.resultspeed(this.tempHp, target.getHealthFixed(), 0.15F);
            } else {
                this.tempHp = NumberTransition.resultspeed(this.tempHp, finalHp, 0.15F);
            }

            String healthText = String.format("%.0f", this.tempHp);
            float addx = 0;
            float addxy = 0;
            if (finalHp > 100) {
                healthText = "?";
                addx = 2F;
                addxy = -0.3f;
            } else {
                addx = -0.14F;
            }


            String hpDisplayText = ColorFormatting.reset() + healthText.replace(",", ".");
            float fontSize = 7.5F;

            String entityName;

            entityName = this.target.getName().getString();

            float addx3 = 8;
            float healthRatio = maxHealth <= 0.0F ? 0.0F : finalHp / maxHealth;
            this.healthAnimation.run(Mathf.clamp01(healthRatio), 0.3, Easings.SINE_OUT, true);

            // Fonts.ICON_DESHUX.draw(matrixStack, "p", textX + 10 + iconSize, textY + textOffset - fontSize - padding + 11.0F, ColorUtil.multAlpha(InterFace.getInstance().themeColor(), openAnimation.get()), 7);

            this.stripFont.draw(Fonts.SFX_SEMIBOLD, matrixStack, entityName, textX + 3 + iconSize, textY + 1.5F, 55, ColorUtil.multAlpha(ColorUtil.getColor(220), this.openAnimation.get()), 8, 2.0F);


            float sizeF = 7.5F;
            this.stripFont.draw(Fonts.SFX_SEMIBOLD, matrixStack, hpDisplayText, textX + 63.3F + iconSize + addx3 - addx - Fonts.SFX_SEMIBOLD.getWidth(hpDisplayText, sizeF) / 2, textY + textOffset - sizeF + addxy - padding + 6F, 10, ColorUtil.multAlpha(ColorUtil.getColor(220), this.openAnimation.get()), sizeF, 2.0F);

            this.barY = y + 22F;
            float round = 1.5F, heis = 4;
            // Rounded.smooth(matrixStack, this.barX, this.barY, this.barW, heis, backgroundColor, Round.of(round));
            // RenderUtil.Shadow.drawShadow(matrixStack, this.barX, this.barY, this.barW * this.healthAnimation.get(), heis, 6, openAnimation.get(), ColorUtil.replAlpha(ColorUtil.fade(0), 1 * openAnimation.get()), ColorUtil.replAlpha(ColorUtil.fade(90), 1 * openAnimation.get()), ColorUtil.replAlpha(ColorUtil.fade(180), 1 * openAnimation.get()), ColorUtil.replAlpha(ColorUtil.fade(270), 1 * openAnimation.get()), Round.of(round));
            // Rounded.smooth(matrixStack, this.barX, this.barY, this.barW * this.healthAnimation.get(), heis, ColorUtil.replAlpha(ColorUtil.fade(0), 1 * openAnimation.get()), ColorUtil.replAlpha(ColorUtil.fade(90), 1 * openAnimation.get()), ColorUtil.replAlpha(ColorUtil.fade(180), 1 * openAnimation.get()), ColorUtil.replAlpha(ColorUtil.fade(270), 1 * openAnimation.get()), Round.of(round));


            drawCircle2(
                    x + 90 + addx3,
                    y + 14.5F,
                    0,
                    360,
                    10,
                    4,
                    false,
                    backgroundColor
            );

            drawCircle(
                    x + 90 + addx3,
                    y + 14.5F,
                    0,
                    this.healthAnimation.get() * 360 + 1,
                    10,
                    4,
                    false,
                    openAnimation.get()
            );
            // Draw absorption bar (golden) if absorption > 0
            // if (absorptionAmount > 0.0F && !(PlayerUtil.isFuntime() || PlayerUtil.isBedwars())) {
            //     int absorptionColor = ColorUtil.replAlpha(ColorUtil.getColor(220, 200, 0), (int) (255.0F * this.openAnimation.getValue()));
            //     int absorptionColorDark = ColorUtil.replAlpha(ColorUtil.getColor(150, 100, 0), (int) (255.0F * this.openAnimation.getValue()));
            //     float absorptionBarY = this.barY; // Position below the main HP bar
            //     Rounded.smooth(matrixStack, this.barX, absorptionBarY, this.barW, heis, backgroundColor, Round.of(round));
            //     Rounded.smooth(matrixStack, this.barX, absorptionBarY, this.barW * this.absorptionAnimation.get(), heis, absorptionColorDark, absorptionColor, Round.of(round));
            // }
            if (this.target instanceof PlayerEntity) {
                this.drawArmorRow(this.target, x + iconSize * 2 + 15, y + 25.5F, width, matrixStack);
            }
            matrixStack.pop();
            // Only draw armor for players
        }
    }

    private void drawArmorRow(LivingEntity entity, float x, float y, float width, MatrixStack matrixStack) {
        ArrayList<ItemStack> items = new ArrayList<>();
        items.add(entity.getHeldItemOffhand());
        items.add(entity.getHeldItemMainhand());

        for (ItemStack armorItem : entity.getArmorInventoryList()) {
            items.add(armorItem.isEmpty() ? ItemStack.EMPTY : armorItem.copy());
        }

        float itemSpacing = 9.5F;
        float totalWidth = (float) items.size() * itemSpacing;
        float startX = x;
        float startY = y - 10.0F;
        matrixStack.push();
        RenderSystem.pushMatrix();
        RenderSystem.scaled(0.5F, 0.5F, 0.5F);
        float currentOffset = 0.0F;
        float scale = (float) Math.max(0.001, this.openAnimation.get());

        for (ItemStack item : items) {
            float itemX = (startX + currentOffset) * 2.0F;
            float itemY = startY * 2.0F;
            RenderSystem.pushMatrix();
            if (!item.isEmpty()) {
                RenderSystem.pushMatrix();
                RenderSystem.translatef(itemX + 8.0F, itemY + 8.0F, 0.0F);
                RenderSystem.scalef(scale, scale, scale);
                RenderSystem.translatef(-(itemX + 8.0F), -(itemY + 8.0F), 0.0F);
                float animationValue = this.openAnimation.get();
                int itemColor = ColorUtil.replAlpha(ColorUtil.WHITE, (int) (255.0F * animationValue));
                RenderUtil.drawItemStack(matrixStack, item, (int) itemX, (int) itemY, itemColor);
                RenderSystem.popMatrix();
            }

            RenderSystem.popMatrix();
            currentOffset += itemSpacing;
        }

        RenderSystem.popMatrix();
        matrixStack.pop();

    }

    public static String trimToFit(String text, int maxLength, float maxWidth, float fontSize) {
        if (text == null) {
            return "";
        } else if (text.isEmpty()) {
            return "";
        } else if (text.length() <= maxLength && Fonts.MONTSERRAT_BOLD.getWidth(text, fontSize) <= maxWidth) {
            return text;
        } else {
            float ellipsisWidth = Fonts.MONTSERRAT_BOLD.getWidth("...", fontSize);
            int currentLength = Math.min(maxLength, text.length());
            if (ellipsisWidth > maxWidth) {
                return "";
            } else {
                while (currentLength > 0) {
                    boolean needsEllipsis = currentLength < text.length();
                    String substring = text.substring(0, currentLength);
                    String result = substring + (needsEllipsis ? "..." : "");
                    float resultWidth = Fonts.MONTSERRAT_BOLD.getWidth(result, fontSize);
                    if (result.length() <= maxLength && resultWidth <= maxWidth) {
                        return result;
                    }

                    --currentLength;
                }

                return "";
            }
        }
    }

    public class FloatFormatter {
        public float format(float value) {
            float precision = (float) Math.pow(6.0F, 1.0F);
            return (float) Math.round(value * precision) / precision;
        }
    }

    private void spawnParticles(float x, float y, float size, float spread, float velocityX, float velocityY, float gravity, float lifetime) {
        float randomX = (this.random.nextFloat() - 0.5F) * spread;
        float randomY = (this.random.nextFloat() - 0.5F) * spread;
        this.particles.add(new Particle(randomX + x, randomY + y, size, randomX, randomY, velocityX, velocityY, gravity, lifetime));
    }

    private void spawnHeadParticles() {
        long currentTime = System.currentTimeMillis();

        // Спавним партиклы за головой через определенные интервалы времени (независимо от FPS)
        if (currentTime - lastHeadParticleSpawn >= HEAD_PARTICLE_SPAWN_INTERVAL) {
            lastHeadParticleSpawn = currentTime;

            float headCenterX = this.drag.position.x + 1.0F + 12.0F; // Центр головы по X
            float headCenterY = this.drag.position.y + 4.0F + 12.0F; // Центр головы по Y

            // Спавним 1-2 партикла за головой
            int count = this.random.nextInt(2) + 1;
            for (int i = 0; i < count; i++) {
                this.spawnParticles(headCenterX, headCenterY, 3, 15, 0.0F, 0.0F, 0.0F, 2000.0F);
            }
        }
    }

    public void attack(AttackEvent attackEvent) {
        if (this.target != null && attackEvent.getTarget() == this.target && InterFace.getInstance().targetHudParticles.getValue()) {
            // Спавн партиклов за головой при атаке
            float headCenterX = this.drag.position.x + 1.0F + 12.0F; // Центр головы по X
            float headCenterY = this.drag.position.y + 4.0F + 12.0F; // Центр головы по Y (textY - 3.0F + 12.0F)

            for (int i = 0; i < 12; ++i) {
                this.spawnParticles(headCenterX, headCenterY, 5, 30, 0.0F, 0.0F, 0.0F, 1500.0F);
            }
        }

    }


    public static void drawCircle(float x, float y, float start, float end, float radius, float width, boolean filled, float alpha) {

        float i;
        float endOffset;
        if (start > end) {
            endOffset = end;
            end = start;
            start = endOffset;
        }
        GlStateManager.enableBlend();
        RenderSystem.disableAlphaTest();
        GL11.glDisable(GL_TEXTURE_2D);
        RenderSystem.blendFuncSeparate(770, 771, 1, 0);
        RenderSystem.shadeModel(7425);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glLineWidth(width);

        GL11.glBegin(GL11.GL_LINE_STRIP);
        for (i = end; i >= start; i--) {
            ColorUtil.setColor(ColorUtil.replAlpha(ColorUtil.fade((int) (i * 1)), alpha));
            float cos = MathHelper.cos((float) (i * Math.PI / 180)) * radius;
            float sin = MathHelper.sin((float) (i * Math.PI / 180)) * radius;
            GL11.glVertex2f(x + cos, y + sin);
        }
        GL11.glEnd();
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        if (filled) {
            GL11.glBegin(GL11.GL_TRIANGLE_FAN);
            for (i = end; i >= start; i--) {
                ColorUtil.setColor(ColorUtil.replAlpha(ColorUtil.fade((int) (i * 1)), alpha));
                float cos = MathHelper.cos((float) (i * Math.PI / 180)) * radius;
                float sin = MathHelper.sin((float) (i * Math.PI / 180)) * radius;
                GL11.glVertex2f(x + cos, y + sin);
            }
            GL11.glEnd();
        }

        RenderSystem.enableAlphaTest();
        RenderSystem.shadeModel(7424);
        GL11.glEnable(GL_TEXTURE_2D);
        GlStateManager.disableBlend();
    }


    public static void drawCircle2(float x, float y, float start, float end, float radius, float width, boolean filled, int alpha) {

        float i;
        float endOffset;
        if (start > end) {
            endOffset = end;
            end = start;
            start = endOffset;
        }
        GlStateManager.enableBlend();
        RenderSystem.disableAlphaTest();
        GL11.glDisable(GL_TEXTURE_2D);
        RenderSystem.blendFuncSeparate(770, 771, 1, 0);
        RenderSystem.shadeModel(7425);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glLineWidth(width);

        GL11.glBegin(GL11.GL_LINE_STRIP);
        for (i = end; i >= start; i--) {
            ColorUtil.setColor(alpha);
            float cos = MathHelper.cos((float) (i * Math.PI / 180)) * radius;
            float sin = MathHelper.sin((float) (i * Math.PI / 180)) * radius;
            GL11.glVertex2f(x + cos, y + sin);
        }
        GL11.glEnd();
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        if (filled) {
            GL11.glBegin(GL11.GL_TRIANGLE_FAN);
            for (i = end; i >= start; i--) {
                ColorUtil.setColor(alpha);
                float cos = MathHelper.cos((float) (i * Math.PI / 180)) * radius;
                float sin = MathHelper.sin((float) (i * Math.PI / 180)) * radius;
                GL11.glVertex2f(x + cos, y + sin);
            }
            GL11.glEnd();
        }

        RenderSystem.enableAlphaTest();
        RenderSystem.shadeModel(7424);
        GL11.glEnable(GL_TEXTURE_2D);
        GlStateManager.disableBlend();
    }

    private void drawArmorRow(PlayerEntity player, float x, float y, float width) {
        List<ItemStack> items = new ArrayList<>();
        if (!player.getHeldItemOffhand().isEmpty()) items.add(player.getHeldItemOffhand());
        if (!player.getHeldItemMainhand().isEmpty()) items.add(player.getHeldItemMainhand());
        for (ItemStack itemStack : player.getArmorInventoryList()) {
            if (!itemStack.isEmpty()) items.add(itemStack.copy());
        }

        float totalWidth = 0f;
        for (ItemStack st : items) {
            if (st.isEmpty()) continue;
            totalWidth += 9.5f;
        }

        float startX = x;
        float rowY = y - 10f;

        RenderSystem.pushMatrix();
        RenderSystem.scaled(0.5f, 0.5f, 0.5f);
        float offset = 0f;
        float scale = (float) Math.max(0.001, openAnimation.get()); // аним. масштаб
        for (ItemStack itemStack : items) {
            if (itemStack.isEmpty()) continue;

            float drawX = (startX + offset) * 2f;
            float drawY = rowY * 2f;

            RenderSystem.pushMatrix();
            RenderSystem.translatef(drawX + 8f, drawY + 8f, 0f);
            RenderSystem.scalef(scale, scale, scale);
            RenderSystem.translatef(-(drawX + 8f), -(drawY + 8f), 0f);

            mc.ingameGUI.renderHotbarItem((int) drawX, (int) drawY, mc.getRenderPartialTicks(), player, itemStack);
            RenderSystem.popMatrix();

            offset += 9f;
        }
        RenderSystem.popMatrix();
    }

    @Generated
    public DragSetting drag() {
        return this.drag;
    }

    @Generated
    public Animation openAnimation() {
        return this.openAnimation;
    }

    @Generated
    public Animation healthAnimation() {
        return this.healthAnimation;
    }

    @Generated
    public StopWatch time() {
        return this.time;
    }

    @Generated
    public boolean inWorld() {
        return this.inWorld;
    }

    @Generated
    public LivingEntity target() {
        return this.target;
    }

    @Generated
    public List<Particle> particles() {
        return this.particles;
    }

    @Generated
    public ResourceLocation bloom() {
        return this.bloom;
    }

    @Generated
    public Random random() {
        return this.random;
    }

    @Generated
    public float radius() {
        Objects.requireNonNull(this);
        return 3.0F;
    }

    @Generated
    public Round round() {
        return this.round;
    }

    @Generated
    public StripFont stripFont() {
        return this.stripFont;
    }

    @Generated
    public float barX() {
        return this.barX;
    }

    @Generated
    public float barY() {
        return this.barY;
    }

    @Generated
    public float barW() {
        return this.barW;
    }

    @Generated
    public float barH() {
        return this.barH;
    }

    @Generated
    public long lastFrameNanos() {
        return this.lastFrameNanos;
    }

    @Generated
    public boolean firstFrame() {
        return this.firstFrame;
    }

    @Generated
    public float tempHp() {
        return this.tempHp;
    }
} */
