package dev.wh1tew1ndows.client.utils.other;

import dev.wh1tew1ndows.client.api.interfaces.IMinecraft;
import dev.wh1tew1ndows.client.utils.math.Mathf;
import net.minecraft.entity.Entity;

public class EntityUtil implements IMinecraft {

    public static double getRenderInterpX(Entity entity, float partialTicks) {
        return Mathf.interpolate(entity.getPosX(), entity.lastTickPosX, partialTicks) - mc.getRenderManager().renderPosX();
    }

    public static double getRenderInterpY(Entity entity, float partialTicks) {
        return Mathf.interpolate(entity.getPosY(), entity.lastTickPosY, partialTicks) - mc.getRenderManager().renderPosY();
    }

    public static double getRenderInterpZ(Entity entity, float partialTicks) {
        return Mathf.interpolate(entity.getPosZ(), entity.lastTickPosZ, partialTicks) - mc.getRenderManager().renderPosZ();
    }

    public static double getInterpX(Entity entity, float partialTicks) {
        return Mathf.interpolate(entity.getPosX(), entity.lastTickPosX, partialTicks);
    }

    public static double getInterpY(Entity entity, float partialTicks) {
        return Mathf.interpolate(entity.getPosY(), entity.lastTickPosY, partialTicks);
    }

    public static double getInterpZ(Entity entity, float partialTicks) {
        return Mathf.interpolate(entity.getPosZ(), entity.lastTickPosZ, partialTicks);
    }
}
