package main.java.org.matejko.plugin.FileCreator;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;
import java.io.*;
import java.util.logging.Logger;

public class Config {

    @SuppressWarnings("unused")
	private final JavaPlugin plugin;
    private final File configFile;
    private Configuration config;
    private Logger logger;  // Logger for logging
    private boolean loaded = false;  // Track if the config is loaded

    public Config(JavaPlugin plugin) {
        this.plugin = plugin;
        configFile = new File(plugin.getDataFolder(), "config.yml");
        this.logger = Logger.getLogger("Utilis");  // Manually set logger

        // Load the configuration from the file
        loadConfig();
    }

    // Load the configuration using the Configuration class
    private void loadConfig() {
        try {
            // Initialize the configuration object
            config = new Configuration(configFile);
            config.load();  // Load the config from the file
            loaded = true;  // Mark the config as loaded successfully
        } catch (Exception e) {
            logger.severe("Error loading config file: " + e.getMessage());
            e.printStackTrace();
            loaded = false;  // Mark the config as failed to load
        }
    }

    // Check if the config was loaded successfully
    public boolean isLoaded() {
        return loaded;
    }

    // Save the current configuration back to the file (simplified)
    public void saveConfig() {
        if (config != null) {
            config.save();  // Save the current configuration
        }
    }

    public File getConfigFile() {
        return configFile;
    }

    // Getter methods to access config values
    public boolean isListEnabled() {
        return config.getBoolean("commands.list", true);  // Default to true if not set
    }

    public boolean isVanishEnabled() {
        return config.getBoolean("commands.vanish", true);  // Default to true if not set
    }

    public boolean isMOTDEnabled() {
        return config.getBoolean("features.motd", true);  // Default to true if not set
    }

    public boolean isQoLEnabled() {
        return config.getBoolean("features.qol", true);  // Default to true if not set
    }

    public boolean isNickEnabled() {
        return config.getBoolean("commands.nickname", true);  // Default to true if not set
    }

    public boolean isRenameEnabled() {
        return config.getBoolean("commands.rename", true);  // Default to true if not set
    }

    public boolean isColorEnabled() {
        return config.getBoolean("commands.color", true);  // Default to true if not set
    }

    public boolean isNickResetEnabled() {
        return config.getBoolean("commands.nickreset", true);  // Default to true if not set
    }

    public boolean isRealNameEnabled() {
        return config.getBoolean("commands.realname", true);  // Default to true if not set
    }

    public boolean isSleepingEnabled() {
        return config.getBoolean("features.sleeping", true);  // Default to true if not set
    }
    
    public boolean isChatFormattingEnabled() {
        return config.getBoolean("features.chat-formatting", true);  // Default to true if not set
    }

	public boolean isUpdateEnabled() {
        return config.getBoolean("features.update-check", true);  // Default to true if not set
	}
}
