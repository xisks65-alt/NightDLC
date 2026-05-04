package dev.wh1tew1ndows.client.managers.module.impl.player;

import com.mojang.authlib.GameProfile;
import dev.wh1tew1ndows.client.api.events.orbit.EventHandler;
import dev.wh1tew1ndows.client.api.interfaces.IWindow;
import dev.wh1tew1ndows.client.managers.events.other.CameraClipEvent;
import dev.wh1tew1ndows.client.managers.events.other.PacketEvent;
import dev.wh1tew1ndows.client.managers.events.player.UpdateEvent;
import dev.wh1tew1ndows.client.managers.events.render.Render2DEvent;
import dev.wh1tew1ndows.client.managers.events.world.WorldChangeEvent;
import dev.wh1tew1ndows.client.managers.events.world.WorldLoadEvent;
import dev.wh1tew1ndows.client.managers.module.Category;
import dev.wh1tew1ndows.client.managers.module.Module;
import dev.wh1tew1ndows.client.managers.module.ModuleInfo;
import dev.wh1tew1ndows.client.managers.module.settings.impl.SliderSetting;
import dev.wh1tew1ndows.client.utils.math.Mathf;
import dev.wh1tew1ndows.client.utils.other.Instance;
import dev.wh1tew1ndows.client.utils.player.MoveUtil;
import dev.wh1tew1ndows.client.utils.render.font.Font;
import dev.wh1tew1ndows.client.utils.render.font.Fonts;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.entity.player.RemoteClientPlayerEntity;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.client.CPlayerAbilitiesPacket;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import net.minecraft.world.GameType;

import java.awt.*;
import java.util.UUID;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "FreeCam", category = Category.PLAYER, desc = "Свободная камера для обзора")
public class FreeCam extends Module implements IWindow {
    public static FreeCam getInstance() {
        return Instance.get(FreeCam.class);
    }

    private final SliderSetting speed = new SliderSetting(this, "Скорость", 1.5F, 0.1F, 5.0F, 0.1F);
    private final int TEMP_ENTITY_ID = Integer.MAX_VALUE - 1337;
    private float x, y, z;
    private GameType prev;
    private final Font font = Fonts.SF_BOLD;

    @Override
    protected void onEnable() {
        if (mc.player.getRidingEntity() != null) toggle();

        super.onEnable();
        prev = mc.playerController.getCurrentGameType();

        mc.player.connection.getPlayerInfo(mc.player.getUniqueID()).setGameType(GameType.SPECTATOR);

        x = (float) mc.player.getPosX();
        y = (float) mc.player.getPosY();
        z = (float) mc.player.getPosZ();

        final RemoteClientPlayerEntity fakePlayer = new RemoteClientPlayerEntity(mc.world, new GameProfile(UUID.randomUUID(), mc.getSession().getUsername()));

        fakePlayer.inventory = mc.player.inventory;
        fakePlayer.setHealth(mc.player.getHealth());
        fakePlayer.setPositionAndRotation(x, mc.player.getBoundingBox().minY, z, mc.player.rotationYaw, mc.player.rotationPitch);
        fakePlayer.rotationYawHead = mc.player.rotationYawHead;
        mc.world.addEntity(TEMP_ENTITY_ID, fakePlayer);
        mc.player.setGameType(GameType.ADVENTURE);
        mc.player.setSneaking(true);
    }

    @Override
    protected void onDisable() {
        super.onDisable();
        mc.player.connection.getPlayerInfo(mc.player.getUniqueID()).setGameType(prev);

        mc.player.setMotion(0, 0, 0);
        mc.player.setVelocity(0, 0, 0);
        mc.player.setPosition(x, y, z);
        mc.player.setSneaking(false);
        mc.world.removeEntityFromWorld(TEMP_ENTITY_ID);
    }

    @EventHandler
    public void onEvent(CameraClipEvent event) {
        event.cancel();
    }

    @EventHandler
    public void onEvent(WorldChangeEvent event) {
        toggle();
    }

    @EventHandler
    public void onEvent(WorldLoadEvent event) {
        toggle();
    }

    @EventHandler
    public void onEvent(Render2DEvent event) {

        float xPosition = (float) (x - mc.player.getPosX());
        float yPosition = (float) (y - mc.player.getPosY());
        float zPosition = (float) (z - mc.player.getPosZ());

        String position = "x:" + Mathf.round(xPosition, 1) + " y:" + Mathf.round(yPosition, 1) + " z:" + Mathf.round(zPosition, 1);
        Fonts.SFP_SEMIBOLD.drawCenter(event.getMatrix(), position, scaled().x / 2F, scaled().y / 2F - 30, new Color(0xB1B1B1).getRGB(), 8);

    }

    @EventHandler
    public void onEvent(PacketEvent event) {
        if (mc.player == null) {
            toggle();
            return;
        }

        if (!mc.player.isAlive()) {
            toggle();
        }

        if (mc.world == null) return;
        final IPacket<?> packet = event.getPacket();

        if (event.isSend()) {
            if (packet instanceof CPlayerPacket || packet instanceof CPlayerAbilitiesPacket) {
                event.cancel();
            }
        }

        if (event.isReceive()) {
            if (packet instanceof SPlayerPositionLookPacket) {
                event.cancel();
            }
        }
    }

    @EventHandler
    public void onEvent(UpdateEvent event) {
        if (mc.gameSettings.keyBindSneak.isKeyDown()) {
            mc.player.motion.y = -speed.getValue() * 0.75F;
        } else if (mc.gameSettings.keyBindJump.isKeyDown()) {
            mc.player.motion.y = speed.getValue() * 0.75F;
        } else {
            mc.player.setMotion(0, 0, 0);
        }

        MoveUtil.setSpeed(speed.getValue());
    }
}