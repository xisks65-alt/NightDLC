package dev.wh1tew1ndows.client.managers.module.impl.misc;

import dev.wh1tew1ndows.client.api.events.orbit.EventHandler;
import dev.wh1tew1ndows.client.managers.events.render.RenderBlockModelEvent;
import dev.wh1tew1ndows.client.managers.events.render.RenderNameEvent;
import dev.wh1tew1ndows.client.managers.module.Category;
import dev.wh1tew1ndows.client.managers.module.Module;
import dev.wh1tew1ndows.client.managers.module.ModuleInfo;
import dev.wh1tew1ndows.client.managers.module.settings.impl.BooleanSetting;
import dev.wh1tew1ndows.client.managers.module.settings.impl.MultiBooleanSetting;
import dev.wh1tew1ndows.client.utils.other.Instance;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.ArmorStandEntity;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "Optimizer", category = Category.MISC, desc = "Оптимизация производительности клиента")
public class Optimizer extends Module {
    public static Optimizer getInstance() {
        return Instance.get(Optimizer.class);
    }

    private final MultiBooleanSetting checks = new MultiBooleanSetting(this, "Убрать",
            BooleanSetting.of("Растительность", false),
            BooleanSetting.of("Стойки для брони", false),
            BooleanSetting.of("Ломание блоков", false),
            BooleanSetting.of("Глов текста", false));

    @EventHandler
    public void onEvent(RenderNameEvent event) {
        if (checks.getValue("Стойки для брони") && event.getEntity() instanceof ArmorStandEntity) {
            event.cancel();
        }
    }

    @EventHandler
    public void onEvent(RenderBlockModelEvent event) {
        BlockState blockState = event.getBlockState();
        if (checks.getValue("Растительность") && (blockState.getMaterial().equals(Material.PLANTS)
                || blockState.getMaterial().equals(Material.TALL_PLANTS)
                || blockState.getMaterial().equals(Material.OCEAN_PLANT)
                || blockState.getMaterial().equals(Material.NETHER_PLANTS)
                || blockState.getMaterial().equals(Material.SEA_GRASS))) {
            event.cancel();
        }
    }

}
