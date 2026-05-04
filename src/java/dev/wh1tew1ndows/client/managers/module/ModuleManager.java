package dev.wh1tew1ndows.client.managers.module;

import dev.wh1tew1ndows.client.Zetrix;
import dev.wh1tew1ndows.client.api.events.orbit.EventHandler;
import dev.wh1tew1ndows.client.managers.events.input.KeyboardPressEvent;
import dev.wh1tew1ndows.client.managers.events.input.MousePressEvent;
import dev.wh1tew1ndows.client.managers.module.impl.combat.*;
import dev.wh1tew1ndows.client.managers.module.impl.misc.*;
import dev.wh1tew1ndows.client.managers.module.impl.movement.*;
import dev.wh1tew1ndows.client.managers.module.impl.player.*;
import dev.wh1tew1ndows.client.managers.module.impl.render.*;

import java.util.*;
import java.util.stream.Collectors;

public final class ModuleManager extends LinkedHashMap<Class<? extends Module>, Module> {


    public void init() {
        addSorted(
                new AirStuck(),
                new AntiBot(),
                //new AntiHunger(),
                new Arrows(),
                new AspectRatio(),
                new AttackAura(),
                new AucHelper(),
                new AutoAuth(),
               // new AutoDuel(),
                new AutoEat(),
                new AutoFish(),
              //  new AutoHeal(),
                new AutoRespawn(),
                new AutoSwap(),
                new AutoTool(),
                new AutoTotem(),
                new AutoTpAccept(),
                new BetterMinecraft(),
                new ChinaHat(),
              //  new ChunkAnimator(),
                new ClickPearl(),
                new ClickFriend(),
                new ClickGuiModule(),
                new ContainerESP(),
                new ContainerStealer(),
               // new CreeperFarm(),
                new Crosshair(),
                //new CrystalAura(),
                new Criticals(),
                new CustomCamera(),
                new CustomHand(),
                new CustomModel(),
                new CustomWorld(),
               // new Eagle(),
               // new ElytraBooster(),
                new ElytraHelper(),
               // new ElytraRecast(),
                //new ElytraTarget(),
               // new EnderChestPlus(),
                new Esp(),
                new FixHP(),

               // new Flight(),
                new FreeCam(),
                new FreeLook(),
                new FullBright(),
                new FunTimeHelper(),
                new GlassHands(),
                new InvMove(),
               // new HighJump(),
                new HitBox(),
                new NoInteract(),
                new InterFace(),
                new ItemPhysics(),
                new ItemScroller(),
                new ItemSwapFix(),
               // new Jesus(),
                new JumpCircle(),
                //new KTLeave(),
               // new LeaveTracker(),
                new NameProtect(),
                new Nametags(),
                new NoDelay(),
                new NoEntityTrace(),
                new NoFriendDamage(),
                new NoPush(),
                new NoRender(),
                new NoServerRotation(),
                new NoSlow(),
                new Notifications(),
                ///new AutoLoot(), ФУ ЮГЕЙМ ХУЙНЯ КОД!
                new Optimizer(),
              //  new Parkour(),
                new Particles(),
                new PotionCombiner(),
                new Trajectories(),
                new SeeInvisibles(),
                new ShulkerViewer(),
                new Sneak(),
                // new SanderModule(),
                new SPJoiner(),
                new Speed(),
                new Spider(),
                new Sprint(),
                //new SRPSpoof(),
               // new Strafe(),
                new TapeMouse(),
                new TargetStrafe(),
                new Trails(),
              //  new XCarry(),
                new InterFace()

        );

        this.values().stream()
                .filter(Module::isAutoEnabled)
                .forEach(module -> module.setEnabled(true, false));


        Zetrix.eventHandler().subscribe(this);
    }

    public void addSorted(Module... modules) {
        Arrays.stream(modules)
                .forEach(module -> this.put(module.getClass(), module));
    }

    public void unregister(Module... modules) {
        Arrays.stream(modules).forEach(module -> this.remove(module.getClass()));
    }

    @EventHandler
    public void onKeyboardPress(KeyboardPressEvent event) {
        if (event.getScreen() == null) {
            this.values().stream()
                    .filter(module -> module.getKey() == event.getKey())
                    .forEach(Module::toggle);
        }
    }

    @EventHandler
    public void onMousePress(MousePressEvent event) {
        if (event.getScreen() == null) {
            this.values().stream()
                    .filter(module -> module.getKey() == event.getKey())
                    .forEach(Module::toggle);
        }
    }

    public <T extends Module> T get(final String name) {
        return this.values().stream()
                .filter(module -> module.getName().equalsIgnoreCase(name))
                .map(module -> (T) module)
                .findFirst()
                .orElse(null);
    }

    public <T extends Module> T get(final Class<T> clazz) {
        return this.values().stream()
                .filter(module -> clazz.isAssignableFrom(module.getClass()))
                .map(clazz::cast)
                .findFirst()
                .orElse(null);
    }

    public List<Module> get(final Category category) {
        return this.values().stream()
                .filter(module -> module.getCategory() == category)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<Module> values() {
        return super.values().stream()
                .sorted(Comparator.comparing(Module::getName, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
    }
}
