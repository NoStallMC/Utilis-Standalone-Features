package main.java.org.matejko.plugin;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

public class UtilisPluginUpdater {

    private static final String UTILIS_PLUGIN_NAME = "Utilis";
    private static final String GITHUB_RELEASE_URL = "https://api.github.com/repos/NoStallMC/Utilis/releases/latest"; 
    private static final String DOWNLOAD_URL = "https://github.com/NoStallMC/Utilis/releases/download/%s/Utilis.jar";
    private static final Logger logger = Bukkit.getLogger();
    private Plugin plugin;

    // Constructor that accepts the main plugin
    public UtilisPluginUpdater(Plugin plugin) {
        this.plugin = plugin;
    }

    // Method to be called from Utilis.java to check for updates
    public void checkForUpdates() {
        logger.info("[Utilis] Checking for updates...");

        // Adding a 5-second delay before performing the update check
        Bukkit.getScheduler().scheduleAsyncDelayedTask(plugin, this::checkAndUpdatePlugin, 100L); // 100 ticks = 5 seconds
    }

    private void checkAndUpdatePlugin() {
        Bukkit.getScheduler().scheduleAsyncDelayedTask(plugin, () -> {
            try {
                String latestVersion = getLatestVersionFromGitHub();
                String currentVersion = getCurrentPluginVersion();

                if (latestVersion == null) {
                    logger.severe("Failed to fetch the latest version from GitHub.");
                    return;
                }

                logger.info("Current Utilis Version: " + currentVersion);
                logger.info("Latest Version on GitHub: " + latestVersion);

                if (isUpdateAvailable(currentVersion, latestVersion)) {
                    logger.info("[Utilis] is outdated! New update downloaded to Utilis folder.");
                    downloadAndNotifyUpdate(latestVersion);
                } else {
                    logger.info("[Utilis] is up to date!");
                }
            } catch (Exception e) {
                logger.severe("Error during update check: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private String getLatestVersionFromGitHub() throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(GITHUB_RELEASE_URL).openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");

        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String jsonResponse = reader.lines().reduce("", (acc, line) -> acc + line);
                return jsonResponse.split("\"tag_name\":\"")[1].split("\"")[0];  // Parse the tag name (version)
            }
        }
        throw new IOException("Failed to fetch version. Response code: " + connection.getResponseCode());
    }

    private String getCurrentPluginVersion() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin(UTILIS_PLUGIN_NAME);
        return plugin != null ? plugin.getDescription().getVersion() : null;
    }

    private boolean isUpdateAvailable(String currentVersion, String latestVersion) {
        return currentVersion != null && !currentVersion.equals(latestVersion);
    }

    private void downloadAndNotifyUpdate(String latestVersion) throws IOException {
        String downloadUrl = String.format(DOWNLOAD_URL, latestVersion);
        HttpURLConnection connection = (HttpURLConnection) new URL(downloadUrl).openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");

        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            // Create a temporary file to store the new plugin
            File pluginFolder = new File("plugins/Utilis");
            if (!pluginFolder.exists()) {
                pluginFolder.mkdirs();
            }
            Path tempPluginFile = new File(pluginFolder, "Utilis.jar").toPath();

            // Download the new plugin
            try (InputStream inputStream = connection.getInputStream();
                 OutputStream outputStream = Files.newOutputStream(tempPluginFile)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
            logger.info("[Utilis] New update downloaded to Utilis folder.");

            // Notify OPs about the update
            warnOPs("[Utilis] is outdated! New update downloaded to Utilis folder.");
        } else {
            throw new IOException("Failed to download plugin. Response code: " + connection.getResponseCode());
        }
    }

    private void warnOPs(String message) {
        // Send a message to all players with OP permissions
        for (org.bukkit.entity.Player player : Bukkit.getOnlinePlayers()) {
            if (player.isOp()) {
                player.sendMessage(message);
            }
        }
    }
}
