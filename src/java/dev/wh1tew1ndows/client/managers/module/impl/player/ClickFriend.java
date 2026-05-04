package dev.wh1tew1ndows.client.managers.module.impl.player;

import dev.wh1tew1ndows.client.Zetrix;
import dev.wh1tew1ndows.client.api.events.orbit.EventHandler;
import dev.wh1tew1ndows.client.managers.events.input.EventKeyboardMouse;
import dev.wh1tew1ndows.client.managers.module.Category;
import dev.wh1tew1ndows.client.managers.module.Module;
import dev.wh1tew1ndows.client.managers.module.ModuleInfo;
import dev.wh1tew1ndows.client.managers.module.settings.impl.BindSetting;
import dev.wh1tew1ndows.client.managers.module.settings.impl.DelimiterSetting;
import dev.wh1tew1ndows.client.utils.chat.ChatUtil;
import dev.wh1tew1ndows.client.utils.other.Instance;
import dev.wh1tew1ndows.client.utils.player.PlayerUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.TextFormatting;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "ClickFriend", category = Category.PLAYER, desc = "Добавление/удаление друга по клику")
public class ClickFriend extends Module {
    public static ClickFriend getInstance() {
        return Instance.get(ClickFriend.class);
    }

    private final DelimiterSetting delimiter = new DelimiterSetting(this, "Добавить в друзья");
    final BindSetting throwKey = new BindSetting(this, "Кнопка добавления друга", -98);

    @EventHandler
    public void onKey(EventKeyboardMouse e) {
        if (e.getKey() == throwKey.getValue() && mc.pointedEntity instanceof PlayerEntity) {
            if (mc.player == null || mc.pointedEntity == null) {
                return;
            }

            String playerName = mc.pointedEntity.getName().getString();

            if (!PlayerUtil.isNameValid(playerName)) {
                ChatUtil.addText("Невозможно добавить бота в друзья, увы, как бы вам не хотелось это сделать");
                return;
            }

            if (Zetrix.inst().friendManager().isFriend(playerName)) {
                Zetrix.inst().friendManager().remove(playerName);
                printStatus(playerName, true);
            } else {
                Zetrix.inst().friendManager().add(playerName);
                printStatus(playerName, false);
            }
        }
    }

    void printStatus(String name, boolean remove) {
        if (remove) ChatUtil.addText(name + TextFormatting.RED + " удалён " + TextFormatting.GRAY + "из друзей");
        else ChatUtil.addText(name + TextFormatting.GREEN + " добавлен " + TextFormatting.GRAY + "в друзья");
    }
}
