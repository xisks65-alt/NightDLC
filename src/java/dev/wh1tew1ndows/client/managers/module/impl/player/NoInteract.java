package dev.wh1tew1ndows.client.managers.module.impl.player;

import dev.wh1tew1ndows.client.Zetrix;
import dev.wh1tew1ndows.client.managers.module.Category;
import dev.wh1tew1ndows.client.managers.module.Module;
import dev.wh1tew1ndows.client.managers.module.ModuleInfo;
import dev.wh1tew1ndows.client.managers.module.settings.impl.BooleanSetting;
import dev.wh1tew1ndows.client.managers.module.settings.impl.MultiBooleanSetting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@ModuleInfo(name = "No Interact", category = Category.PLAYER, desc = "Отменяет взаимодействие с выбранными блоками")
public class NoInteract extends Module {

    public final MultiBooleanSetting ignore = new MultiBooleanSetting(this, "Блоки",
            new BooleanSetting("Стойки", true),
            new BooleanSetting("Сундуки", false),
            new BooleanSetting("Двери", false),
            new BooleanSetting("Кнопки", false),
            new BooleanSetting("Воронки", false),
            new BooleanSetting("Раздатчики", false),
            new BooleanSetting("Музыкальные блоки", false),
            new BooleanSetting("Верстаки", true),
            new BooleanSetting("Люки", false),
            new BooleanSetting("Печки", false),
            new BooleanSetting("Калитки", false),
            new BooleanSetting("Наковальни", false),
            new BooleanSetting("Рычаги", false)
            );

    public static Set<Integer> getBlocks() {
        HashSet<Integer> blocks = new HashSet<Integer>();
        addBlocksForInteractionType(blocks, 1, 147, 329, 270);
        addBlocksForInteractionType(blocks, 2, 173, 161, 485, 486, 487, 488, 489, 720, 721);
        addBlocksForInteractionType(blocks, 3, 183, 308, 309, 310, 311, 312, 313, 718, 719, 758);
        addBlocksForInteractionType(blocks, 4, 336);
        addBlocksForInteractionType(blocks, 5, 70, 342, 508);
        addBlocksForInteractionType(blocks, 6, 74);
        addBlocksForInteractionType(blocks, 7, 151);
        addBlocksForInteractionType(blocks, 8, 222, 223, 224, 225, 226, 227, 712, 713, 379);
        addBlocksForInteractionType(blocks, 9, 154, 670);
        addBlocksForInteractionType(blocks, 10, 250, 475, 476, 477, 478, 479, 714, 715);
        addBlocksForInteractionType(blocks, 11, 328, 327, 326);
        addBlocksForInteractionType(blocks, 12, 171);
        return blocks;
    }

    public static void addBlocksForInteractionType(Set<Integer> blocks, int interactionType, Integer... blockIds) {
        MultiBooleanSetting ignore = Zetrix.inst().moduleManager().get(NoInteract.class).ignore;
        List<BooleanSetting> settingsList = new ArrayList<>(ignore.getValues());
        if (interactionType > 0 && interactionType <= settingsList.size()) {
                BooleanSetting setting = settingsList.get(interactionType);
                if (setting != null && setting.getValue()) {
                blocks.addAll(Arrays.asList(blockIds));
            }
        }
    }
}
