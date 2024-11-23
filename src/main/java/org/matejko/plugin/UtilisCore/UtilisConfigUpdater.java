package main.java.org.matejko.plugin.UtilisCore;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@SuppressWarnings("unused")
public class UtilisConfigUpdater {

    private static final String CONFIG_FILE_PATH = "plugins/Utilis/config.yml"; // Config on disk
    private static final String DEFAULT_CONFIG_FILE_PATH = "/config.yml"; // Config inside the JAR
    private static final String OLD_CONFIG_FILE_PATH = "plugins/Utilis/oldconfig.yml"; // Backup location
    private static JavaPlugin plugin; // Plugin instance
   
    // Logger for logging events
    private static final Logger logger = Logger.getLogger(UtilisConfigUpdater.class.getName());

    // Constructor to accept the JavaPlugin instance
    public UtilisConfigUpdater(JavaPlugin plugin) {
        UtilisConfigUpdater.plugin = plugin; // Assign the plugin instance
    }

    public static void checkAndUpdateConfig() {
        boolean requiresRestart = false; // Track if restart is necessary

        try {
            // Step 1: Check if the config file exists in the plugin folder
            File configFile = new File(CONFIG_FILE_PATH);
            boolean configExists = configFile.exists();

            // If the config doesn't exist, copy the default config from JAR
            if (!configExists) {
                copyDefaultConfigToServer();
                return;
            }

            // Step 2: Load the current config from disk
            Map<String, Object> currentConfig = loadConfig(CONFIG_FILE_PATH);

            // Step 3: Get current version from config.yml on the server
            String currentVersion = getCurrentConfigVersion(currentConfig);

            // Step 4: Check if the current config version is outdated or missing
            if (currentVersion == null || isVersionOutdated(currentVersion)) {
                logger.info("Config is outdated or missing version. Merging will begin.");

                // Step 5: Backup old config (copy it, not rename it)
                backupOldConfig();

                // Step 6: Load default config from JAR
                Map<String, Object> defaultConfig = loadDefaultConfig();

                // Step 7: Merge old config values into the new config (excluding "Version")
                Map<String, Object> oldConfig = loadConfig(OLD_CONFIG_FILE_PATH);
                boolean mergeSuccess = mergeConfigs(defaultConfig, oldConfig);

                // Step 8: Save the updated config back to the server
                if (mergeSuccess) {
                    saveConfig(CONFIG_FILE_PATH, defaultConfig);
                    logger.info("Config merger is done!");

                    // Step 9: Reload the plugin
                    reloadPlugin();
                } else {
                    logger.severe("Config merger failed: Could not merge old config.");
                }
            } else {
                // Get the version number from the current config and append it to the message
                String configVersion = currentVersion != null ? currentVersion : "unknown";
                logger.info("Config is up to date! (v" + configVersion + ")");
            }

        } catch (Exception e) {
            //logger.severe("An error occurred while checking and updating the config: " + e.getMessage());
            //e.printStackTrace();
        }
    }

    // Load YAML file from disk into a Map
    @SuppressWarnings("unchecked")
    private static Map<String, Object> loadConfig(String filePath) throws FileNotFoundException {
        Yaml yaml = new Yaml();
        FileInputStream fileInputStream = new FileInputStream(filePath);
        return (Map<String, Object>) yaml.load(fileInputStream);
    }

    // Load default config from the JAR (config.yml)
    @SuppressWarnings("unchecked")
    private static Map<String, Object> loadDefaultConfig() throws IOException {
        Yaml yaml = new Yaml();
        InputStream inputStream = UtilisConfigUpdater.class.getResourceAsStream(DEFAULT_CONFIG_FILE_PATH);
        if (inputStream == null) {
            throw new FileNotFoundException("Default config (config.yml) not found in JAR.");
        }
        return (Map<String, Object>) yaml.load(inputStream);
    }

    // Get the current version from the config.yml
    private static String getCurrentConfigVersion(Map<String, Object> currentConfig) {
        Object versionObject = currentConfig.get("Version");
        if (versionObject != null) {
            return versionObject.toString();
        }
        return null;
    }

    // Check if the current version is outdated compared to the server version
    private static boolean isVersionOutdated(String currentVersion) throws IOException {
        String serverVersion = getServerVersionFromJar();
        if (serverVersion == null) {
            logger.warning("Server config version missing, cannot compare.");
            return false;
        }

        String[] currentParts = currentVersion.split("\\.");
        String[] serverParts = serverVersion.split("\\.");

        for (int i = 0; i < Math.min(currentParts.length, serverParts.length); i++) {
            int current = Integer.parseInt(currentParts[i]);
            int server = Integer.parseInt(serverParts[i]);
            if (current < server) return true;
            if (current > server) return false;
        }
        return false;
    }

    // Get the version from the plugin JAR's config.yml (inside the JAR file)
    private static String getServerVersionFromJar() throws FileNotFoundException {
        Yaml yaml = new Yaml();
        InputStream inputStream = UtilisConfigUpdater.class.getResourceAsStream(DEFAULT_CONFIG_FILE_PATH);
        if (inputStream == null) {
            throw new FileNotFoundException("Default config (config.yml) not found in JAR.");
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> defaultConfig = (Map<String, Object>) yaml.load(inputStream);
        Object versionObject = defaultConfig.get("Version");
        if (versionObject != null) {
            return versionObject.toString();
        }
        return null;
    }

    // Save a Map to YAML
    private static void saveConfig(String filePath, Map<String, Object> config) throws IOException {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK); // Nice block style for YAML
        Yaml yaml = new Yaml(options);
        FileWriter writer = new FileWriter(filePath);
        yaml.dump(config, writer);
    }

    // Backup old config by copying its content (no renaming)
    private static void backupOldConfig() throws IOException {
        File configFile = new File(CONFIG_FILE_PATH);
        File backupFile = new File(OLD_CONFIG_FILE_PATH);

        if (configFile.exists()) {
            try {
                // Only copy the file, do not rename it
                try (InputStream inputStream = new FileInputStream(configFile);
                     OutputStream outputStream = new FileOutputStream(backupFile)) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                }
                logger.info("Old config successfully copied to oldconfig.yml.");
            } catch (IOException e) {
                logger.severe("Failed to copy old config file: " + e.getMessage());
                throw e;
            }
        }
    }

    // Copy the default config from JAR to the server config path
    private static void copyDefaultConfigToServer() throws IOException {
        // Ensure the "plugins/Utilis" directory exists
        File configDirectory = new File("plugins/Utilis");
        if (!configDirectory.exists()) {
            if (configDirectory.mkdirs()) {
                logger.info("Created directory: " + configDirectory.getPath());
            } else {
                logger.severe("Failed to create directory: " + configDirectory.getPath());
                throw new IOException("Failed to create directory: " + configDirectory.getPath());
            }
        }

        // Now ensure that the config file exists and copy it from the JAR if needed
        InputStream inputStream = UtilisConfigUpdater.class.getResourceAsStream(DEFAULT_CONFIG_FILE_PATH);
        if (inputStream == null) {
            throw new FileNotFoundException("Default config (config.yml) not found in JAR.");
        }

        File outputFile = new File(CONFIG_FILE_PATH);
        try (OutputStream outputStream = new FileOutputStream(outputFile)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
        logger.info("Default config copied from JAR.");
    }
    
    // Merge old config values into the new config (excluding "Version")
    @SuppressWarnings("unchecked")
    private static boolean mergeConfigs(Map<String, Object> newConfig, Map<String, Object> oldConfig) {
        try {
            // Loop through all entries in the old config
            for (Map.Entry<String, Object> entry : oldConfig.entrySet()) {
                String key = entry.getKey();
                Object oldValue = entry.getValue();

                // Skip merging the "Version" key, as it's handled separately
                if ("Version".equalsIgnoreCase(key)) {
                    continue;
                }

                // Check if the key exists in the new config
                if (newConfig.containsKey(key)) {
                    // If the value in the new config is a Map, we recursively merge it
                    if (newConfig.get(key) instanceof Map && oldValue instanceof Map) {
                        Map<String, Object> newSubConfig = (Map<String, Object>) newConfig.get(key);
                        Map<String, Object> oldSubConfig = (Map<String, Object>) oldValue;
                        mergeConfigs(newSubConfig, oldSubConfig);  // Recursive merge
                    }
                    // Handle other types of merging (like Boolean, String, etc.)
                    else {
                        mergeValue(newConfig, key, oldValue);
                    }
                } else {
                    // If the key doesn't exist in the new config, add it directly
                    newConfig.put(key, oldValue);
                }
            }
            return true;
        } catch (Exception e) {
            logger.severe("Merge failed: " + e.getMessage());
            return false;
        }
    }

    // Helper method to merge the value into the new config with the correct type
    @SuppressWarnings("unchecked")
    private static void mergeValue(Map<String, Object> newConfig, String key, Object oldValue) {
        if (oldValue instanceof Boolean) {
            newConfig.put(key, ((Boolean) oldValue));
        } else if (oldValue instanceof String) {
            newConfig.put(key, ((String) oldValue));
        } else if (oldValue instanceof Integer) {
            newConfig.put(key, ((Integer) oldValue));
        } else if (oldValue instanceof Double) {
            newConfig.put(key, ((Double) oldValue));
        } else if (oldValue instanceof List) {
            newConfig.put(key, ((List<?>) oldValue));
        } else if (oldValue instanceof Map) {
            newConfig.put(key, ((Map<String, Object>) oldValue));
        } else {
            // Default behavior for unknown types (put the value as is)
            newConfig.put(key, oldValue);
        }
    }

    // Reload the plugin after updating the config
    private static void reloadPlugin() {
        logger.info("Restarting Utilis...");
        Bukkit.getPluginManager().disablePlugin(plugin);
        Bukkit.getPluginManager().enablePlugin(plugin);
    }
}
