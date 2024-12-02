package main.java.org.matejko.plugin.UtilisCore;

import main.java.org.matejko.plugin.Managers.*;
import main.java.org.matejko.plugin.Commands.*;
import main.java.org.matejko.plugin.FileCreator.*;
import main.java.org.matejko.plugin.Utilis;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.earth2me.essentials.Essentials;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

public class UtilisInitializer {
    private final Utilis plugin;
    private final Logger logger;
    public UtilisInitializer(Utilis plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    @SuppressWarnings("static-access")
	public void initialize() {
        logger.info("[Utilis] Initializing...");
        UtilisConfigUpdater configUpdater = new UtilisConfigUpdater(plugin);
        configUpdater.checkAndUpdateConfig();
        Config config = new Config(plugin);
        if (!config.isLoaded()) {
            logger.warning("[Utilis] Config was not loaded properly!");
            return;
        }
        Essentials essentials = (Essentials) Bukkit.getServer().getPluginManager().getPlugin("Essentials");
        if (essentials == null) {
            logger.warning("[Utilis] Essentials plugin not found!");
        } else {
            logger.info("[Utilis] Essentials plugin found!");
        }

        // ChatFormattingManager setup
        ChatFormattingManager chatFormattingManager = new ChatFormattingManager(plugin);
        chatFormattingManager.loadConfiguration();
        Bukkit.getPluginManager().registerEvents(chatFormattingManager, plugin);

        // VanishedPlayersManager
        Set<VanishUserManager> vanishedPlayers = new HashSet<>();
        VanishedPlayersManager vanishedPlayersManager = new VanishedPlayersManager(plugin);
        vanishedPlayersManager.loadVanishedPlayers(vanishedPlayers);

        // NickManager and cooldown setup
        NickManager nickManager = new NickManager(plugin);
        Messages messages = new Messages(plugin);
        CooldownManager cooldownManager = new CooldownManager(plugin, 60);
        Bukkit.getPluginManager().registerEvents(nickManager, plugin);

        // UtilisNotifier setup
        UtilisNotifier utilisNotifier = new UtilisNotifier(plugin);
        Bukkit.getPluginManager().registerEvents(utilisNotifier, plugin);

        // Command registration
        UtilisCommands utilisCommands = new UtilisCommands(plugin, config, nickManager, cooldownManager, messages);
        utilisCommands.registerCommands();

        // MOTD Manager
        MOTDManager motdManager = null;
        if (config.isMOTDEnabled()) {
            motdManager = new MOTDManager(plugin);
        }

        // Plugin Updater
        UtilisPluginUpdater pluginUpdater = new UtilisPluginUpdater(plugin, config);
        pluginUpdater.registerListener();
        if (config.isUpdateEnabled()) {
            pluginUpdater.checkForUpdates();
        } else {
            logger.info("[Utilis] Update check is disabled in the config.");
        }

        // Sleeping Manager
        SleepingManager sleepingManager = null;
        if (config.isSleepingEnabled()) {
            sleepingManager = new SleepingManager(plugin);
            sleepingManager.loadConfiguration();
            Bukkit.getPluginManager().registerEvents(sleepingManager, plugin);
            SleepingCommand sleepingCommand = new SleepingCommand(plugin);
            plugin.getCommand("as").setExecutor((sender, command, label, args) -> {
                if (!(sender instanceof Player)) {
                    sender.sendMessage("Only players can use this command.");
                    return true;
                }
                Player player = (Player) sender;
                if (!player.hasPermission("utilis.as")) {
                    player.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                    return true;
                }
                return sleepingCommand.onCommand(sender, command, label, args);
            });
        } else {
            logger.info("[Utilis] Sleeping is disabled in the config.");
        }

        // QoL Manager
        if (config.isQoLEnabled()) {
            Bukkit.getPluginManager().registerEvents(new QoLManager(), plugin);
        }

        // Dynmap setup
        Plugin dynmapPlugin = Bukkit.getPluginManager().getPlugin("dynmap");
        if (dynmapPlugin == null) {
            logger.warning("[Utilis] Dynmap plugin not found!");
        }
        DynmapManager dynmapManager = new DynmapManager(dynmapPlugin, logger);

        // Create UtilisGetters instance
        UtilisGetters utilisGetters = new UtilisGetters(
                logger, vanishedPlayers, vanishedPlayersManager,
                motdManager, dynmapManager, utilisNotifier,
                config, essentials, dynmapPlugin, sleepingManager, nickManager
        );

        // Store the UtilisGetters in the plugin for later access
        plugin.setUtilisGetters(utilisGetters);
        
        logger.info("[Utilis] Initialization complete!");
    }
}
