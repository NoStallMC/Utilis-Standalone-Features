package main.java.org.matejko.plugin;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.Plugin;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import main.java.org.matejko.plugin.Managers.*;
import main.java.org.matejko.plugin.Commands.*;
import main.java.org.matejko.plugin.FileCreator.*;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

public class Utilis extends JavaPlugin implements Listener {

    private Logger logger;
    private Set<VanishUserManager> vanishedPlayers;
    private VanishedPlayersManager vanishedPlayersManager;
    private UtilisNotifier utilisNotifier;
    private Plugin dynmapPlugin;
    private MOTDManager motdManager;
    private DynmapManager dynmapManager;
    private Config config;
    @SuppressWarnings("unused")
	private SleepingWorldConfig sleepingWorldConfig;
    public SleepingManager sleepingManager;
    public NickManager nickManager;
    private CooldownManager cooldownManager;
    private Essentials essentials;
    private UtilisPluginUpdater pluginupdater;

    // Add a field for UtilisGetters
    private UtilisGetters utilisGetters;

    @SuppressWarnings("static-access")
	@Override
    public void onEnable() {
        this.logger = Logger.getLogger("Utilis");
        getLogger().info("[Utilis] is now active!");

        // Initialize the config updater
        UtilisConfigUpdater configupdater = new UtilisConfigUpdater(this);
        configupdater.checkAndUpdateConfig();

        // Initialize config early to ensure it's available for use
        config = new Config(this);
        if (!config.isLoaded()) {
            getLogger().warning("Config was not loaded properly!");
            return;  // Stop execution if config is not loaded properly
        }

        // Essentials plugin
        this.essentials = (Essentials) Bukkit.getServer().getPluginManager().getPlugin("Essentials");
        if (essentials == null) {
            getLogger().warning("Essentials plugin not found!");
        } else {
            getLogger().info("Essentials found!");
        }

        // Initialize the plugin updater
        pluginupdater = new UtilisPluginUpdater(this);
        if (config.isUpdateEnabled()) {
            pluginupdater.checkForUpdates();  // Trigger the update check
        } else {
            getLogger().info("[Utilis] Update check is disabled in the config.");
        }

        // Initialize the ChatFormattingManager
        ChatFormattingManager chatFormattingManager = new ChatFormattingManager(this);
        chatFormattingManager.loadConfiguration();  // Load initial configuration for chat formatting
        Bukkit.getPluginManager().registerEvents(chatFormattingManager, this);  // Register as event listener

        // Initialize SleepingWorldConfig to manage the worlds' sleeping settings
        sleepingWorldConfig = new SleepingWorldConfig();

        // SleepingManager (if enabled)
        if (config.isSleepingEnabled()) {
            this.sleepingManager = new SleepingManager(this);
            sleepingManager.loadConfiguration();
            getServer().getPluginManager().registerEvents(sleepingManager, this);

            // Register the /as command for Sleeping
            SleepingCommand sleepingCommand = new SleepingCommand(this);
            getCommand("as").setExecutor(sleepingCommand);
        } else {
            getLogger().info("Sleeping is disabled in the config. Sleeping features will be inactive.");
        }

        // Initialize vanished players set and managers
        vanishedPlayers = new HashSet<>();
        vanishedPlayersManager = new VanishedPlayersManager(this);
        vanishedPlayersManager.loadVanishedPlayers(vanishedPlayers);

        // NickManager initialization
        Bukkit.getPluginManager().registerEvents(new NickManager(this), this);
        this.nickManager = new NickManager(this);
        Messages messages = new Messages(this);  // Load messages from messages.yml
        this.cooldownManager = new CooldownManager(this, 60);  // Set 60 seconds cooldown

        // Move command registration to the UtilisCommands class
        UtilisCommands utilisCommands = new UtilisCommands(this, config, nickManager, cooldownManager, messages);
        utilisCommands.registerCommands();

        // Enable QoL feature (if enabled)
        if (config.isQoLEnabled()) {
            Bukkit.getPluginManager().registerEvents(new QoLManager(), this);
        }

        // Enable MOTD feature (if enabled)
        if (config.isMOTDEnabled()) {
            motdManager = new MOTDManager(this);
        }

        // Initialize dynmapPlugin safely
        dynmapPlugin = Bukkit.getPluginManager().getPlugin("dynmap");
        if (dynmapPlugin == null) {
            getLogger().warning("Dynmap plugin not found! Some features may not work.");
        }
        dynmapManager = new DynmapManager(dynmapPlugin, logger);

        // Initialize and register UtilisNotifier as a listener
        utilisNotifier = new UtilisNotifier(this);
        Bukkit.getPluginManager().registerEvents(utilisNotifier, this);

        // Initialize UtilisGetters
        utilisGetters = new UtilisGetters(getLogger(), vanishedPlayers, vanishedPlayersManager, motdManager,
                dynmapManager, utilisNotifier, config, essentials, dynmapPlugin);

        // Register events (also includes current class for other event handling)
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        getLogger().info("[Utilis] plugin disabled.");
    }

    // Getter methods for accessing the plugin's components
    public Logger getLogger() {
        return logger;
    }

    // Provide access to UtilisGetters
    public UtilisGetters getUtilisGetters() {
        return utilisGetters;
    }

    // Check if a player is AFK using the Essentials API
    public boolean isAFK(Player player) {
        if (essentials == null) {
            return false;  // Essentials is not available, return false by default
        }

        User user = essentials.getUser(player);  // Get the Essentials user
        return user.isAfk();  // Check if the player is AFK using Essentials API
    }
}
