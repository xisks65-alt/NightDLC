package dev.wh1tew1ndows.client.managers.module.impl.misc;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import dev.wh1tew1ndows.client.Zetrix;
import dev.wh1tew1ndows.client.api.events.orbit.EventHandler;
import dev.wh1tew1ndows.client.managers.events.player.UpdateEvent;
import dev.wh1tew1ndows.client.managers.module.Category;
import dev.wh1tew1ndows.client.managers.module.Module;
import dev.wh1tew1ndows.client.managers.module.ModuleInfo;
import dev.wh1tew1ndows.client.managers.module.settings.impl.StringSetting;
import dev.wh1tew1ndows.client.utils.other.Instance;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.Minecraft;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.text.ITextComponent;

import java.util.WeakHashMap;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "NameProtect", category = Category.MISC, desc = "Скрытие имени игрока в чате и интерфейсе")
public class NameProtect extends Module {
    public static NameProtect getInstance() {
        return Instance.get(NameProtect.class);
    }

    public static String fakeName = "";

    private final StringSetting customName = new StringSetting(this, "Кастомный ник", "zetrix67", false);

    private ITextComponent originalHeader = null;
    private static final WeakHashMap<String, String> replaceCache = new WeakHashMap<>();

    @Override
    public void toggle() {
        super.toggle();
        replaceCache.clear();
    }

    @EventHandler
    private void onUpdate(UpdateEvent e) {
        fakeName = isEnabled() ? customName.getValue() : mc.session.getProfile().getName();

        Minecraft mc = Minecraft.getInstance();
        if (mc.world == null || mc.player == null) return;

        Scoreboard scoreboard = mc.world.getScoreboard();
        if (scoreboard == null) return;

        ScoreObjective objective = scoreboard.getObjectiveInDisplaySlot(1);
        if (objective == null) return;

        ITextComponent header = objective.getDisplayName();
        if (header == null) return;

        if (isEnabled()) {
            String headerString = header.getString();

            // Проверяем наличие ключевых слов
            if (headerString.contains("⚡ Анархия-") || headerString.contains("⚡ Гриферский-")) {

                if (originalHeader == null) {
                    String json = ITextComponent.Serializer.toJson(header);
                    JsonElement jsonElement = new JsonParser().parse(json);
                    originalHeader = ITextComponent.Serializer.getComponentFromJson(jsonElement);
                }

                // Заменяем все возможные варианты
               
            }
        } else if (originalHeader != null && !header.getString().equals(originalHeader.getString())) {
            String json = ITextComponent.Serializer.toJson(originalHeader);
            JsonElement jsonElement = new JsonParser().parse(json);
            objective.setDisplayName(ITextComponent.Serializer.getComponentFromJson(jsonElement));
        }
    }


    @Override
    public void onDisable() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.world != null && mc.player != null) {
            Scoreboard scoreboard = mc.world.getScoreboard();
            if (scoreboard != null) {
                ScoreObjective objective = scoreboard.getObjectiveInDisplaySlot(1);
                if (objective != null && originalHeader != null) {
                    String json = ITextComponent.Serializer.toJson(originalHeader);
                    JsonElement jsonElement = new JsonParser().parse(json);
                    objective.setDisplayName(ITextComponent.Serializer.getComponentFromJson(jsonElement));
                }
            }
        }
        super.onDisable();
    }

    public static String getReplaced(String input) {
        if (!Zetrix.initialized()) {
            return input;
        }

        NameProtect nameProtect = NameProtect.getInstance();
        if (!nameProtect.isEnabled()) {
            return input;
        }

        if (replaceCache.containsKey(input)) {
            return replaceCache.get(input);
        }

        String sessionName = mc.session.getProfile().getName();
        String protectedName = nameProtect.customName.getValue();


        String result = input.replace(sessionName, protectedName);
        replaceCache.put(input, result);

        return result;
    }
}

