package dev.wh1tew1ndows.client.managers.module;

import dev.wh1tew1ndows.client.Zetrix;
import dev.wh1tew1ndows.client.api.annotations.Beta;
import dev.wh1tew1ndows.client.api.interfaces.IMinecraft;
import dev.wh1tew1ndows.client.managers.module.impl.misc.Notifications;
import dev.wh1tew1ndows.client.managers.module.settings.Setting;
import dev.wh1tew1ndows.client.utils.animation.Animation;
import dev.wh1tew1ndows.client.utils.animation.util.Easings;
import dev.wh1tew1ndows.client.utils.other.SoundUtil;
import dev.wh1tew1ndows.client.utils.render.font.StripFont;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Data;
import org.apache.commons.lang3.NotImplementedException;

import java.util.List;

@Data
public abstract class Module implements IMinecraft {
    private final List<Setting<?>> settings = new ObjectArrayList<>();
    private ModuleInfo moduleInfo;
    private String name;
    private String desc;
    private Category category;
    private boolean enabled;
    private boolean autoEnabled;
    private boolean allowDisable;
    private boolean hidden;
    private int key;
    private final Animation animation = new Animation();
    private final StripFont stripFont = new StripFont();

    public Module() {
        Class<? extends Module> clazz = this.getClass();
        ModuleInfo moduleInfo = clazz.getAnnotation(ModuleInfo.class);

        if (moduleInfo == null) {
            throw new NotImplementedException("@ModuleInfo annotation not found on " + clazz.getSimpleName());
        }

        this.moduleInfo = moduleInfo;
        this.name = moduleInfo.name().trim().replaceAll(" ", "");
        this.desc = moduleInfo.desc();
        this.category = moduleInfo.category();
        this.autoEnabled = moduleInfo.autoEnabled();
        this.allowDisable = moduleInfo.allowDisable();
        this.hidden = moduleInfo.hidden();
        this.key = moduleInfo.key();
        setup();
    }

    public void toggle() {
        setEnabled(!enabled);
    }

    public void setEnabled(final boolean enabled) {
        setEnabled(enabled, true);
    }

    public void setEnabled(final boolean enabled, boolean notification) {
        if (this.enabled == enabled || (!this.allowDisable && !enabled)) {
            return;
        }
        this.enabled = enabled;
        final boolean beta = this.getClass().isAnnotationPresent(Beta.class);
        Notifications notifications = Notifications.getInstance();
        if (!this.isHidden() && notification && notifications.isEnabled() && notifications.getSound().getValue()) {
            //Zetrix.inst().notificationManager().register((
            //        beta ? ColorFormatting.getColor(ColorUtil.getColor(255, 45, 45)) + "(beta) " + ColorFormatting.reset() : "")
            //        + "Модуль" + (enabled ? ColorFormatting.getColor(ColorUtil.overCol(ColorUtil.getColor(45, 255, 45), ColorUtil.WHITE))
            //        : ColorFormatting.getColor(ColorUtil.overCol(ColorUtil.getColor(255, 45, 45), ColorUtil.WHITE)))
            //        + " " + this.name + ColorFormatting.reset() + " " + (enabled ? "включён" : "выключен"), NotificationType.INFO, 1500);

            String typeEneblae1 = "LOL";
            if (Notifications.getInstance().soundtype.is("Тип 1")) {
                typeEneblae1 = "Function_ON.wav";
            } else if (Notifications.getInstance().soundtype.is("Тип 2")) {
                typeEneblae1 = "enable.wav";
            }
            String typeEneblae2 = "LOL";
            if (Notifications.getInstance().soundtype.is("Тип 1")) {
                typeEneblae2 = "Function_OFF.wav";
            } else if (Notifications.getInstance().soundtype.is("Тип 2")) {
                typeEneblae2 = "disable.wav";
            }

            SoundUtil.playSound(enabled ? typeEneblae1 : typeEneblae2, notifications.getVolume().getValue() / 100);
        }
        if (!this.isHidden() && notification && notifications.isEnabled()) {
            notifications.push(this.name, enabled, beta, this.category);
        }

        if (enabled) {
            superEnable();
        } else {
            superDisable();
        }

        animation.run(enabled ? 1 : 0, 0.2F, Easings.SINE_OUT, false);

    }

    private void eventEnable() {
        Zetrix.eventHandler().subscribe(this);
    }

    private void eventDisable() {
        Zetrix.eventHandler().unsubscribe(this);
    }

    private void superEnable() {
        if (mc.player != null) onEnable();

        eventEnable();
    }

    private void superDisable() {
        if (mc.player != null) onDisable();

        eventDisable();
    }

    public void setup() {
    }

    protected void onEnable() {
    }

    protected void onDisable() {
    }
}