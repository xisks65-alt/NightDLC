package dev.wh1tew1ndows.client.managers.module.impl.combat;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.TextFormatting;
import dev.wh1tew1ndows.client.Zetrix;
import dev.wh1tew1ndows.client.api.events.orbit.EventHandler;
import dev.wh1tew1ndows.client.managers.events.player.AttackEvent;
import dev.wh1tew1ndows.client.managers.module.Category;
import dev.wh1tew1ndows.client.managers.module.Module;
import dev.wh1tew1ndows.client.managers.module.ModuleInfo;
import dev.wh1tew1ndows.client.utils.other.Instance;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "NoFriendDamage", category = Category.COMBAT, desc = "Защита друзей от случайного урона")
public class NoFriendDamage extends Module {
    public static NoFriendDamage getInstance() {
        return Instance.get(NoFriendDamage.class);
    }

    @EventHandler
    public void onEvent(AttackEvent event) {
        if (event.getTarget() instanceof PlayerEntity player && Zetrix.inst().friendManager().isFriend(TextFormatting.removeFormatting(player.getGameProfile().getName()))) {
            event.cancel();
        }
    }
}
