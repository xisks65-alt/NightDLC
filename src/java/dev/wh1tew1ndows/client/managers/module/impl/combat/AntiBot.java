package dev.wh1tew1ndows.client.managers.module.impl.combat;

import com.mojang.authlib.GameProfile;
import dev.wh1tew1ndows.client.api.events.orbit.EventHandler;
import dev.wh1tew1ndows.client.managers.events.other.GameUpdateEvent;
import dev.wh1tew1ndows.client.managers.events.other.PacketEvent;
import dev.wh1tew1ndows.client.managers.events.world.WorldChangeEvent;
import dev.wh1tew1ndows.client.managers.events.world.WorldLoadEvent;
import dev.wh1tew1ndows.client.managers.module.Category;
import dev.wh1tew1ndows.client.managers.module.Module;
import dev.wh1tew1ndows.client.managers.module.ModuleInfo;
import dev.wh1tew1ndows.client.utils.other.Instance;
import dev.wh1tew1ndows.client.utils.player.PlayerUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.server.SPlayerListItemPacket;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "AntiBot", category = Category.COMBAT, desc = "Фильтрация ботов и фейковых игроков")
public class AntiBot extends Module {
    public static AntiBot getInstance() {
        return Instance.get(AntiBot.class);
    }

    private final Set<UUID> suspectSet = new HashSet<>();
    private final Set<UUID> botSet = new HashSet<>();

    @EventHandler
    public void onEvent(PacketEvent event) {
        IPacket<?> packet = event.getPacket();

        if (packet instanceof SPlayerListItemPacket wrapper) {
            if (wrapper.getAction().equals(SPlayerListItemPacket.Action.ADD_PLAYER)) {
                checkPlayerAfterSpawn(wrapper);
            }
            if (wrapper.getAction().equals(SPlayerListItemPacket.Action.REMOVE_PLAYER)) {
                removePlayerBecauseLeftServer(wrapper);
            }
        }
    }

    @EventHandler
    public void onTick(GameUpdateEvent gameUpdateEvent) {
        if (!suspectSet.isEmpty()) {
            mc.world.getPlayers().stream()
                    .filter(player -> suspectSet.contains(player.getUniqueID()))
                    .forEach(this::evaluateSuspectPlayer);
        }
    }

    @EventHandler
    public void onEvent(WorldChangeEvent event) {
        reset();
    }

    @EventHandler
    public void onEvent(WorldLoadEvent event) {
        reset();
    }

    private void checkPlayerAfterSpawn(SPlayerListItemPacket packet) {
        if (mc.getConnection() == null) return;
        packet.getPlayerAdditionEntries().forEach(data -> {
            GameProfile profile = data.getProfile();
            if (isRealPlayer(data, profile)) {
                return;
            }
            if (isDuplicateProfile(profile)) {
                botSet.add(profile.getId());
            } else {
                suspectSet.add(profile.getId());
            }
        });
    }

    private void removePlayerBecauseLeftServer(SPlayerListItemPacket packet) {
        packet.getPlayers().forEach(data -> {
            suspectSet.remove(data.getProfile().getId());
            botSet.remove(data.getProfile().getId());
        });
    }

    private boolean isRealPlayer(SPlayerListItemPacket.AddPlayerData data, GameProfile profile) {
        return data.getPing() < 5 || (profile.getProperties() != null && !profile.getProperties().isEmpty());
    }

    public boolean isDuplicateProfile(GameProfile profile) {
        return mc.getConnection().getPlayerInfoMap().stream()
                .filter(player -> player.getGameProfile().getName().equals(profile.getName()) && !player.getGameProfile().getId().equals(profile.getId()))
                .count() == 1;
    }

    private void evaluateSuspectPlayer(PlayerEntity player) {
        Iterable<ItemStack> armor = null;

        if (!isFullyEquipped(player)) {
            armor = player.getArmorInventoryList();
        }
        if ((isFullyEquipped(player) || hasArmorChanged(player, armor))) {
            botSet.add(player.getUniqueID());
        }
        suspectSet.remove(player.getUniqueID());
    }

    public boolean isFullyEquipped(PlayerEntity entity) {
        return IntStream.rangeClosed(0, 3)
                .mapToObj(slot -> entity.inventory.armorItemInSlot(slot))
                .allMatch(stack -> stack.getItem() instanceof ArmorItem && !stack.isEnchanted());
    }

    public boolean hasArmorChanged(PlayerEntity entity, Iterable<ItemStack> prevArmor) {
        if (prevArmor == null) {
            return true;
        }

        List<ItemStack> currentArmorList = StreamSupport.stream(entity.getArmorInventoryList().spliterator(), false).toList();
        List<ItemStack> prevArmorList = StreamSupport.stream(prevArmor.spliterator(), false).toList();

        return !IntStream.range(0, Math.min(currentArmorList.size(), prevArmorList.size()))
                .allMatch(i -> currentArmorList.get(i).equals(prevArmorList.get(i))) || currentArmorList.size() != prevArmorList.size();
    }

    public boolean isBot(PlayerEntity entity) {
        return PlayerUtil.isInvalidName(entity.getGameProfile().getName()) || botSet.contains(entity.getUniqueID());
    }

    public void reset() {
        suspectSet.clear();
        botSet.clear();
    }

    @Override
    public void toggle() {
        super.toggle();
        reset();
    }
}