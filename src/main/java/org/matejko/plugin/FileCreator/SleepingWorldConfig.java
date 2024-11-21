package main.java.org.matejko.plugin.FileCreator;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.util.config.Configuration;
import java.io.File;

public class SleepingWorldConfig {

    private final File file;
    private Configuration config;

    // Constructor: Creates the file path and attempts to load configuration
    public SleepingWorldConfig() {
        file = new File(Bukkit.getServer().getPluginManager().getPlugin("Utilis").getDataFolder(), "SleepingWorlds.yml");
        loadConfig();  // Load the configuration when the object is created
    }

    // Load the configuration from the file (if the file doesn't exist, it creates one)
    public void loadConfig() {
        if (!file.exists()) {
            createDefaultConfig();  // Create the file if it doesn't exist
        }

        config = new Configuration(file);
        config.load();  // Load the configuration file
    }

    // Create the default configuration (worlds enabled except for the Nether)
    private void createDefaultConfig() {
        config = new Configuration(file);

        // Loop through all the worlds and set the default configuration
        for (World world : Bukkit.getWorlds()) {
            if (world.getName().equalsIgnoreCase("world_nether")) {
                config.setProperty(world.getName(), "disabled");  // Set the Nether world to disabled
            } else {
                config.setProperty(world.getName(), "enabled");  // Enable sleeping for all other worlds
            }
        }

        saveConfig();  // Save the configuration after creation
    }

    // Save the configuration to the file
    public void saveConfig() {
        try {
            config.save();  // Save the configuration to the file
        } catch (Exception e) {
            e.printStackTrace();  // Handle potential exceptions when saving
        }
    }

    // Get the sleeping status for a world (default to "enabled" if not found)
    public boolean isSleepingEnabled(World world) {
        String status = config.getString(world.getName(), "enabled");  // Default to "enabled"
        return status.equalsIgnoreCase("enabled");
    }

    // Set the sleeping status for a world
    public void setSleepingStatus(World world, boolean enabled) {
        config.setProperty(world.getName(), enabled ? "enabled" : "disabled");
        saveConfig();  // Save the updated status
    }

}
