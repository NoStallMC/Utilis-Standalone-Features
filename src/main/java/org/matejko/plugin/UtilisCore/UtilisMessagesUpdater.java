package main.java.org.matejko.plugin.UtilisCore;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import main.java.org.matejko.plugin.Utilis;
import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@SuppressWarnings("unused")
public class UtilisMessagesUpdater {
    private static final String MESSAGES_FILE_PATH = "plugins/Utilis/messages.yml";
    private static final String DEFAULT_MESSAGES_FILE_PATH = "/messages.yml";
    private static final String OLD_MESSAGES_FILE_PATH = "plugins/Utilis/oldmessages.yml";
    private final Logger logger;
    private static Utilis plugin;

    public UtilisMessagesUpdater(Utilis plugin) {
        UtilisMessagesUpdater.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    public static void checkAndUpdateConfig() {
        boolean requiresRestart = false;
        try {
            File configFile = new File(MESSAGES_FILE_PATH);
            boolean configExists = configFile.exists();
            if (!configExists) {
                copyDefaultConfigToServer();
                return;
            }
            Map<String, Object> currentConfig = loadConfig(MESSAGES_FILE_PATH);
            String currentVersion = getCurrentConfigVersion(currentConfig);
            if (currentVersion == null || isVersionOutdated(currentVersion)) {
                plugin.getLogger().info("[Utilis] messages.yml is outdated or missing version. Merging will begin.");

                backupOldConfig();
                Map<String, Object> defaultConfig = loadDefaultConfig();
                Map<String, Object> oldConfig = loadConfig(OLD_MESSAGES_FILE_PATH);
                boolean mergeSuccess = mergeConfigs(defaultConfig, oldConfig);
                if (mergeSuccess) {
                    saveConfigWithComments(MESSAGES_FILE_PATH, defaultConfig);
                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                        plugin.getLogger().info("[Utilis] Messages merger is done!");
                        plugin.getLogger().severe("[Utilis] Server reload needed!");
                    }, 40L);
                } else {
                    plugin.getLogger().severe("[Utilis] messages.yml merger failed: Could not merge old messages.yml.");
                }
            } else {
                String configVersion = currentVersion != null ? currentVersion : "unknown";
                plugin.getLogger().info("[Utilis] messages.yml is up to date! (v" + configVersion + ")");
            }
        } catch (Exception e) {
            plugin.getLogger().severe("An error occurred while checking and updating the messages.yml: " + e.getMessage());
            e.printStackTrace();
        }
    }
    @SuppressWarnings("unchecked")
    private static Map<String, Object> loadConfig(String filePath) throws FileNotFoundException {
        Yaml yaml = new Yaml();
        FileInputStream fileInputStream = new FileInputStream(filePath);
        return (Map<String, Object>) yaml.load(fileInputStream);
    }
    @SuppressWarnings("unchecked")
    private static Map<String, Object> loadDefaultConfig() throws IOException {
        Yaml yaml = new Yaml();
        InputStream inputStream = UtilisMessagesUpdater.class.getResourceAsStream(DEFAULT_MESSAGES_FILE_PATH);
        if (inputStream == null) {
            throw new FileNotFoundException("Default messages.yml not found in JAR.");
        }
        return (Map<String, Object>) yaml.load(inputStream);
    }
    private static String getCurrentConfigVersion(Map<String, Object> currentConfig) {
        Object versionObject = currentConfig.get("Version");
        return versionObject != null ? versionObject.toString() : null;
    }
    private static boolean isVersionOutdated(String currentVersion) throws IOException {
        String serverVersion = getServerVersionFromJar();
        if (serverVersion == null) {
            plugin.getLogger().warning("[Utilis] messages.yml version missing, cannot compare.");
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
    private static String getServerVersionFromJar() throws FileNotFoundException {
        Yaml yaml = new Yaml();
        InputStream inputStream = UtilisMessagesUpdater.class.getResourceAsStream(DEFAULT_MESSAGES_FILE_PATH);
        if (inputStream == null) {
            throw new FileNotFoundException("Default messages.yml not found in JAR.");
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> defaultConfig = (Map<String, Object>) yaml.load(inputStream);
        Object versionObject = defaultConfig.get("Version");
        return versionObject != null ? versionObject.toString() : null;
    }
    private static void saveConfigWithComments(String filePath, Map<String, Object> config) throws IOException {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml yaml = new Yaml(options);
        // Convert YAML data to string format
        String yamlContent = yaml.dump(config);
        // Load comments from the JAR resource
        InputStream inputStream = UtilisMessagesUpdater.class.getResourceAsStream(DEFAULT_MESSAGES_FILE_PATH);
        if (inputStream == null) {
            throw new FileNotFoundException("Default messages.yml not found in JAR.");
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder commentsBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("#")) {
                commentsBuilder.append(line).append("\n");
            }
        }
        // Add comments to the bottom
        String finalContent = yamlContent + "\n" + commentsBuilder.toString();
        // Save file
        FileWriter writer = new FileWriter(filePath);
        writer.write(finalContent);
        writer.close();
    }
    private static void backupOldConfig() throws IOException {
        File configFile = new File(MESSAGES_FILE_PATH);
        File backupFile = new File(OLD_MESSAGES_FILE_PATH);
        if (configFile.exists()) {
            try (InputStream inputStream = new FileInputStream(configFile);
                 OutputStream outputStream = new FileOutputStream(backupFile)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                plugin.getLogger().info("[Utilis] Old messages.yml successfully backed up.");
            } catch (IOException e) {
                plugin.getLogger().severe("[Utilis] Failed to backup old messages.yml: " + e.getMessage());
                throw e;
            }
        }
    }
    private static void copyDefaultConfigToServer() throws IOException {
        File configDirectory = new File("plugins/Utilis");
        if (!configDirectory.exists()) {
            if (configDirectory.mkdirs()) {
                plugin.getLogger().info("[Utilis] Created directory: " + configDirectory.getPath());
            } else {
                plugin.getLogger().severe("[Utilis] Failed to create directory: " + configDirectory.getPath());
                throw new IOException("[Utilis] Failed to create directory: " + configDirectory.getPath());
            }
        }
        InputStream inputStream = UtilisMessagesUpdater.class.getResourceAsStream(DEFAULT_MESSAGES_FILE_PATH);
        if (inputStream == null) {
            throw new FileNotFoundException("Default messages.yml not found in JAR.");
        }
        File outputFile = new File(MESSAGES_FILE_PATH);
        try (OutputStream outputStream = new FileOutputStream(outputFile)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
        plugin.getLogger().info("[Utilis] Default messages.yml copied from JAR.");
    }
    @SuppressWarnings("unchecked")
    private static boolean mergeConfigs(Map<String, Object> newConfig, Map<String, Object> oldConfig) {
        try {
            for (Map.Entry<String, Object> entry : oldConfig.entrySet()) {
                String key = entry.getKey();
                Object oldValue = entry.getValue();
                if ("Version".equalsIgnoreCase(key)) continue;

                if (newConfig.containsKey(key)) {
                    if (newConfig.get(key) instanceof Map && oldValue instanceof Map) {
                        Map<String, Object> newSubConfig = (Map<String, Object>) newConfig.get(key);
                        Map<String, Object> oldSubConfig = (Map<String, Object>) oldValue;
                        mergeConfigs(newSubConfig, oldSubConfig);
                    } else {
                        newConfig.put(key, oldValue);
                    }
                } else {
                    newConfig.put(key, oldValue);
                }
            }
            return true;
        } catch (Exception e) {
            plugin.getLogger().severe("[Utilis] Messages Merger failed: " + e.getMessage());
            return false;
        }
    }
}
