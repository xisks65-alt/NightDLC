package dev.wh1tew1ndows.client;

import com.jagrosh.discordipc.IPCClient;
import com.jagrosh.discordipc.IPCListener;
import com.jagrosh.discordipc.entities.Packet;
import com.jagrosh.discordipc.entities.RichPresence;
import com.jagrosh.discordipc.exceptions.NoDiscordClientException;
import dev.wh1tew1ndows.client.api.client.Constants;
import dev.wh1tew1ndows.client.api.events.orbit.EventBus;
import dev.wh1tew1ndows.client.gui.ClickGui;
import dev.wh1tew1ndows.client.screen.clickgui.ClickGuiScreen;
import dev.wh1tew1ndows.client.managers.alt.AltConfig;
import dev.wh1tew1ndows.client.managers.alt.AltWidget;
import dev.wh1tew1ndows.client.managers.command.AdviceCommandFactoryImpl;
import dev.wh1tew1ndows.client.managers.command.ParametersFactoryImpl;
import dev.wh1tew1ndows.client.managers.command.PrefixImpl;
import dev.wh1tew1ndows.client.managers.command.StandaloneCommandDispatcher;
import dev.wh1tew1ndows.client.managers.command.api.*;
import dev.wh1tew1ndows.client.managers.command.api.logger.ConsoleLogger;
import dev.wh1tew1ndows.client.managers.command.api.logger.MinecraftLogger;
import dev.wh1tew1ndows.client.managers.command.api.logger.MultiLogger;
import dev.wh1tew1ndows.client.managers.command.impl.*;
import dev.wh1tew1ndows.client.managers.component.ComponentManager;
import dev.wh1tew1ndows.client.managers.events.input.EventKey;
import dev.wh1tew1ndows.client.managers.module.ModuleManager;

import dev.wh1tew1ndows.client.managers.other.config.ConfigFile;
import dev.wh1tew1ndows.client.managers.other.config.ConfigManager;
import dev.wh1tew1ndows.client.managers.other.config.StaffStorage;
import dev.wh1tew1ndows.client.managers.other.friend.FriendFile;
import dev.wh1tew1ndows.client.managers.other.friend.FriendManager;
import dev.wh1tew1ndows.client.managers.other.macros.MacrosFile;
import dev.wh1tew1ndows.client.managers.other.macros.MacrosManager;
import dev.wh1tew1ndows.client.utils.file.FileManager;
import dev.wh1tew1ndows.client.utils.other.Console;
import dev.wh1tew1ndows.client.utils.render.font.Fonts;
import dev.wh1tew1ndows.client.utils.render.shader.ShaderManager;
import dev.wh1tew1ndows.client.utils.server.ServerTPS;
import dev.wh1tew1ndows.client.utils.time.Profiler;
import dev.wh1tew1ndows.common.impl.proxy.ProxyConfig;
import dev.wh1tew1ndows.common.impl.viaversion.ViaMCP;
import dev.wh1tew1ndows.common.impl.waveycapes.WaveyCapesBase;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j2;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.StringTextComponent;
import ru.nocturneguard.J2C.J2t;
import ru.nocturneguard.J2C.Native;
import ru.nocturneguard.J2C.NotNative;
import ru.nocturneguard.NocturneGuard;
import ru.nocturneguard.UserProfile;

import java.lang.invoke.MethodHandles;
import java.time.OffsetDateTime;
import java.util.*;

@SuppressWarnings("IfStatementWithIdenticalBranches")

@Log4j2
@Getter
@Accessors(fluent = true)
@Native
@J2t
public class Zetrix {
    @Getter
    private static final Zetrix inst = new Zetrix();
    
    public static Zetrix inst() {
        return inst;
    }

    @Getter
    private static boolean initialized = false;

    @Getter
    private static final boolean premiun = true;

    @Getter
    private static final EventBus eventHandler = EventBus.threadSafe();

    @Getter
    private static final boolean devMode = Objects.equals("whitew1ndows", Optional.ofNullable(System.getenv("USERNAME")).orElseGet(() -> System.getProperty("user.name", "")).trim().toLowerCase(Locale.ROOT));

    public static long startTime = System.currentTimeMillis();
//    @Setter
//    private UserData userData;

    private final long loadTime = System.currentTimeMillis();
    private FileManager fileManager;
    private ModuleManager moduleManager;
    private CommandDispatcher commandDispatcher;
    private ComponentManager componentManager;
    private ConfigManager configManager;
    private FriendManager friendManager;
    //  private AccountManager accountManager;ok

    private MacrosManager macrosManager;

    private AltWidget altWidget;
    private WaveyCapesBase waveyCapes;
    private AltConfig altConfig;
    private ClickGuiScreen clickGui;
    private ClickGui gui;


    // private AccountGuiScreen accountGui;


    private final Profiler profiler = new Profiler();
    private ServerTPS serverTps;
    public static String name = "1.0";

    public final List<String> sponsors = List.of("spookytime.net", "mc.funtime.su", "mc.holyworld.ru");
    private static final long CLIENT_ID = premiun ? 1436808317732393140L : 1415072526224326767L;
    private static IPCClient client;

    public void start() {
        eventHandler.registerLambdaFactory("", (lookupInMethod, klass) -> (MethodHandles.Lookup) lookupInMethod.invoke(null, klass, MethodHandles.lookup()));
    }


    public void load() {
        NocturneGuard.start();
        Fonts.loadFonts();

        ShaderManager.loadShaders();
        ProxyConfig.loadConfig();
        this.initVia();//nahui ti udalil

        log("Initializing..");

        startRPC();
        initManagers();
        initCommands();
        StaffStorage.load();
        altWidget = new AltWidget();
        altConfig = new AltConfig();


        try {
            altConfig.init();
        } catch (Exception e) {
            e.printStackTrace();
        }
        initWaveyCapes();

        initScreens();

        log("Start " + "Zetrix.cc " + "ms.");

        loadAllConfigs();

        Minecraft.getInstance().getMainWindow().setWindowTitle(Constants.TITLE);

        initialized = true;

        Runtime.getRuntime().addShutdownHook(new Thread(this::unload));
    }


    public static void startRPC() {
        try {
            client = new IPCClient(CLIENT_ID);
            client.setListener(new IPCListener() {
                @Override
                public void onPacketReceived(IPCClient client, Packet packet) {
                    IPCListener.super.onPacketReceived(client, packet);
                }

                @Override
                public void onReady(IPCClient client) {
                    RichPresence.Builder builder = new RichPresence.Builder();
                    builder.setDetails("USER » " + (UserProfile.get().getUsername())).setState("UID » " + (UserProfile.get().getUid()))
                            .setLargeImage("https://i.ibb.co/cKWGd5KR/gif-10mb-12.gif")
                            .setStartTimestamp(OffsetDateTime.now());
                    client.sendRichPresence(builder.build());
                }
            });
            try {
                client.connect();
            } catch (NoDiscordClientException e) {
                // Discord client not available, silently ignore
            }
        } catch (Exception e) {
            // Handle any other exceptions (e.g., on Linux when Discord is not running)
            // Log the error but don't crash the application
            log.warn("Failed to initialize Discord RPC: " + e.getMessage());
        }
    }

    @NotNative
    public String randomNickname() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        int length = 7;
        StringBuilder sb = new StringBuilder(length);
        Random random = new Random();

        for (int i = 0; i < length; i++) {
            sb.append(characters.charAt(random.nextInt(characters.length())));
        }

        return sb.toString();
    }

    @NotNative
    private void initWaveyCapes() {
        this.waveyCapes = new WaveyCapesBase();
        this.waveyCapes.init();
    }

    @NotNative
    private void initScreens() {
        this.gui = new ClickGui();
        this.clickGui = new ClickGuiScreen();
        //  this.accountGui = new AccountGuiScreen();
    }


    private void initManagers() {
        this.fileManager = new FileManager();

        this.serverTps = new ServerTPS();
        this.moduleManager = new ModuleManager();
        this.moduleManager.init();

        this.componentManager = new ComponentManager();
        this.componentManager.init();

        this.configManager = new ConfigManager();
        this.friendManager = new FriendManager();
        // this.accountManager = new AccountManager();
        this.macrosManager = new MacrosManager();

    }

    @NotNative
    private void initVia() {
        ViaMCP.create();
    }


    private void loadAllConfigs() {
        loadConfigs();
        loadFriends();
        loadMacros();
        NocturneGuard.start();
    }

    @NotNative
    public void unload() {
        if (configManager != null) {
            configManager.set();
        }
        if (friendManager != null) {
            friendManager.set();
        }

        if (macrosManager != null) {
            macrosManager.set();
        }

    }

    @NotNative
    public static void log(String str, Object... args) {
        log.info("{}{}{} -> {}{}",
                Console.getConsoleBackground(),
                Console.getConsoleText(),
                Constants.NAME,
                str.formatted(args),
                Console.getConsoleReset());
    }


    @NotNative
    public void loadConfigs() {
        configManager.update();
        final ConfigFile config = configManager.get("default");
        boolean ok = config != null && config.read();
        try {
        } catch (Throwable ignored) {
        }
    }

    @NotNative
    public void loadFriends() {
        final FriendFile friend = friendManager.get();
        if (friend == null || !friend.read()) {
            friendManager.set();
        } else {
            friendManager.set();
        }
    }

    @NotNative
    public void loadMacros() {
        final MacrosFile macros = macrosManager.get();
        if (macros == null || !macros.read()) {
            macrosManager.set();
        } else {
            macrosManager.set();
        }
    }


    private final EventKey eventKey = new EventKey(-1);

    public void onKeyPressed(int key) {
        eventKey.setKey(key);
        eventHandler().post(eventKey);

    }

    private void initCommands() {
        Logger logger = new MultiLogger(List.of(new ConsoleLogger(), new MinecraftLogger()));
        List<Command> commands = new ArrayList<>();
        Prefix prefix = new PrefixImpl();
        commands.add(new ListCommand(commands, logger));
        commands.add(new FriendCommand(friendManager, prefix, logger));
        commands.add(new BindCommand(prefix, logger));
        commands.add(new LoginCommand(logger));
        commands.add(new NameCommand(logger));
        commands.add(new PanicCommand(logger));
        commands.add(new ServerInfoCommand(logger));
        commands.add(new GPSCommand(prefix));
        commands.add(new WayCommand(prefix, logger));
        commands.add(new ConfigCommand(configManager, prefix, logger));
        commands.add(new MacroCommand(macrosManager, prefix, logger));
        commands.add(new VClipCommand(prefix, logger));
        commands.add(new HClipCommand(prefix, logger));
        commands.add(new MemoryCommand(logger));
        commands.add(new StaffCommand(prefix, logger));
        commands.add(new RCTCommand(logger));
        commands.add(new PrefixCommand(prefix, logger));
        commands.add(new NeuroCommand(prefix, logger));
        AdviceCommandFactory adviceCommandFactory = new AdviceCommandFactoryImpl(logger);
        ParametersFactory parametersFactory = new ParametersFactoryImpl();

        commandDispatcher = new StandaloneCommandDispatcher(commands, adviceCommandFactory, prefix, parametersFactory, logger);
    }
}