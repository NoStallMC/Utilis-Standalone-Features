package main.java.org.matejko.plugin;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;

import main.java.org.matejko.plugin.Commands.VanishCommand;
import main.java.org.matejko.plugin.FileCreator.Config;
import main.java.org.matejko.plugin.FileCreator.Messages;
import main.java.org.matejko.plugin.FileCreator.SleepingWorldConfig;
import main.java.org.matejko.plugin.Commands.ListCommand;
import main.java.org.matejko.plugin.Commands.NicknameCommand;
import main.java.org.matejko.plugin.Commands.RenameCommand;
import main.java.org.matejko.plugin.Commands.SleepingCommand;
import main.java.org.matejko.plugin.Commands.SuckCommand;
import main.java.org.matejko.plugin.Commands.UtilisDebugCommand;
import main.java.org.matejko.plugin.Commands.ColorCommand;
import main.java.org.matejko.plugin.Commands.NickResetCommand;
import main.java.org.matejko.plugin.Commands.RealNameCommand;
import main.java.org.matejko.plugin.Managers.NickManager;
import main.java.org.matejko.plugin.Managers.ChatFormattingManager;
import main.java.org.matejko.plugin.Managers.CooldownManager;
import main.java.org.matejko.plugin.Managers.DynmapManager;
import main.java.org.matejko.plugin.Managers.MOTDManager;
import main.java.org.matejko.plugin.Managers.QoLManager;
import main.java.org.matejko.plugin.Managers.SleepingManager;
import main.java.org.matejko.plugin.Managers.VanishUserManager;
import main.java.org.matejko.plugin.Managers.VanishedPlayersManager;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
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
    public HashMap<World, ArrayList<Player>> sleepingPlayers = new HashMap<>();  // Sleeping players map
    public NickManager nickManager;
    private CooldownManager cooldownManager;
	private Essentials essentials;
    private UtilisPluginUpdater pluginupdater;
    @SuppressWarnings({ "static-access" })
	@Override
    public void onEnable() {
        this.logger = Logger.getLogger("Utilis");
        getLogger().info("[Utilis] is now active!");

        // Initialize config early to ensure it's available for use
        config = new Config(this);
        if (!config.isLoaded()) {
            getLogger().warning("Config was not loaded properly!");
            return;  // Stop execution if config is not loaded properly
        }
        this.essentials = (Essentials) Bukkit.getServer().getPluginManager().getPlugin("Essentials"); // Get Essentials plugin instance

        if (essentials == null) {
            getLogger().warning("Essentials plugin not found!");
        } else {
            getLogger().info("Essentials found!");
        }
        // Initialize the plugin updater
        pluginupdater = new UtilisPluginUpdater(this);
        
        // Check if update checking is enabled in the config and start the update process
        if (config.isUpdateEnabled()) {
            pluginupdater.checkForUpdates();  // Trigger the update check
        } else {
            getLogger().info("[Utilis] Update check is disabled in the config.");
        }
        
        // Initialize the config updater on startup
        UtilisConfigUpdater configupdater = new UtilisConfigUpdater(this);
        configupdater.checkAndUpdateConfig();
        
        // Initialize the ChatFormattingManager
        ChatFormattingManager chatFormattingManager = new ChatFormattingManager(this);
        chatFormattingManager.loadConfiguration();  // Load initial configuration for chat formatting
        Bukkit.getPluginManager().registerEvents(chatFormattingManager, this);  // Register as event listener
        
        // Initialize SleepingWorldConfig to manage the worlds' sleeping settings
        sleepingWorldConfig = new SleepingWorldConfig();  // Initialize SleepingWorldConfig

        // Initialize SleepingManager only if sleeping is enabled
        if (config.isSleepingEnabled()) {
            this.sleepingManager = new SleepingManager(this);  // Initialize SleepingManager with plugin instance
            sleepingManager.loadConfiguration();

            // Register the SleepingManager as an event listener
            getServer().getPluginManager().registerEvents(sleepingManager, this);

            // Conditionally register the /as command for Sleeping
            SleepingCommand sleepingCommand = new SleepingCommand(this);  // Pass the plugin instance to SleepingCommand constructor
            getCommand("as").setExecutor(sleepingCommand);  // Register the /as command executor
        } else {
            getLogger().info("Sleeping is disabled in the config. Sleeping features will be inactive.");
        }

        // Initialize vanished players set and managers
        vanishedPlayers = new HashSet<>();

        // Check and copy the default messages.yml from JAR if it doesn't exist
        File messagesFile = new File(getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            copyResourceToFile("messages.yml", messagesFile);
        }
        Bukkit.getPluginManager().registerEvents(new NickManager(this), this);   // Initialize NickManager here
        this.nickManager = new NickManager(this);  // Initialize NickManager here
        Messages messages = new Messages(this);  // Initialize the Messages object (to load messages from messages.yml)
        this.cooldownManager = new CooldownManager(this, 60);  // Set 60 seconds cooldown

        // Conditionally register the Nickname-related commands based on config setting
        if (config.isNickEnabled()) {
            getCommand("nickname").setExecutor(new NicknameCommand(nickManager, cooldownManager, messages));  // Pass Messages object
        }
        if (config.isRenameEnabled()) {
            getCommand("rename").setExecutor(new RenameCommand(nickManager));
        }
        if (config.isColorEnabled()) {
            getCommand("color").setExecutor(new ColorCommand(nickManager, cooldownManager, messages));
        }
        if (config.isNickResetEnabled()) {
            getCommand("nickreset").setExecutor(new NickResetCommand(nickManager, cooldownManager));
        }
        if (config.isRealNameEnabled()) {
            getCommand("realname").setExecutor(new RealNameCommand(nickManager));
        }

        // Conditionally register the /list command based on config setting
        if (config.isListEnabled()) {
            ListCommand listCommand = new ListCommand(this);  // Pass 'this' to the ListCommand constructor
            this.getCommand("list").setExecutor(listCommand);  // Register the /list command
        }

        // Conditionally enable the QoL feature
        if (config.isQoLEnabled()) {
            // Register the QoL event listener
            Bukkit.getPluginManager().registerEvents(new QoLManager(), this);  // Register QoL events for block breaks
        }
        // Register the /utilisdebug command
        getCommand("utilisdebug").setExecutor(new UtilisDebugCommand(this));
        // Initialize other managers and systems
        vanishedPlayersManager = new VanishedPlayersManager(this);  // Pass the plugin instance to VanishedPlayersManager
        vanishedPlayersManager.loadVanishedPlayers(vanishedPlayers);

        SuckCommand suckCommand = new SuckCommand();  // Pass 'this' to the VanishCommand constructor
        this.getCommand("suck").setExecutor(suckCommand);

        // Conditionally register the /vanish command based on config setting
        if (config.isVanishEnabled()) {
            VanishCommand vanishCommand = new VanishCommand(this);  // Pass 'this' to the VanishCommand constructor
            this.getCommand("vanish").setExecutor(vanishCommand);
            this.getCommand("v").setExecutor(vanishCommand);
        }

        // Conditionally enable the MOTD feature based on the config setting
        if (config.isMOTDEnabled()) {
            motdManager = new MOTDManager(this);  // Initialize MOTDManager with the plugin instance
        }

        // Initialize dynmapPlugin safely
        dynmapPlugin = Bukkit.getPluginManager().getPlugin("dynmap");
        if (dynmapPlugin == null) {
            getLogger().warning("Dynmap plugin not found! Some features may not work.");
        }
        dynmapManager = new DynmapManager(dynmapPlugin, logger);

        // Initialize and register the UtilisNotifier as a listener
        utilisNotifier = new UtilisNotifier(this);  // Pass only the plugin instance now
        Bukkit.getPluginManager().registerEvents(utilisNotifier, this);  // Register the event listener

        // Register events (also includes the current class for other event handling)
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

    public Set<VanishUserManager> getVanishedPlayers() {
        return vanishedPlayers;
    }

    public VanishedPlayersManager getVanishedPlayersManager() {
        return vanishedPlayersManager;
    }

    public MOTDManager getMotdManager() {
        return motdManager;
    }

    public DynmapManager getDynmapManager() {
        return dynmapManager;
    }

    public UtilisNotifier getUtilisNotifier() {
        return utilisNotifier;
    }

    public Config getConfig() {
        return config;
    }

    public String getName() {
        return "Utilis";
    }
    public Essentials getEssentials() {
        return essentials;
    }

    public Plugin getDynmapPlugin() {
        return dynmapPlugin;
    }
    private void copyResourceToFile(String resourceName, File outputFile) {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourceName);
        
        if (inputStream == null) {
            getLogger().warning("Resource not found: " + resourceName);
            return;
        }

        // Ensure the output file and directories exist
        try {
            if (!outputFile.exists()) {
                outputFile.getParentFile().mkdirs(); // Ensure parent directories are created
                outputFile.createNewFile();           // Create the new file
            }

            // Use a BufferedOutputStream to handle writing
            try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFile))) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                getLogger().info("Resource " + resourceName + " copied successfully.");
            }
        } catch (IOException e) {
            getLogger().warning("Error copying resource " + resourceName + ": " + e.getMessage());
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
