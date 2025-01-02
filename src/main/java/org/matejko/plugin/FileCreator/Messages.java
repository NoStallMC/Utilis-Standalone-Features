package main.java.org.matejko.plugin.FileCreator;

import org.bukkit.util.config.Configuration;
import main.java.org.matejko.plugin.Utilis;
import java.io.*;

public class Messages {
    private Utilis plugin;
    private File messagesFile;
    private Configuration messagesConfig;

    public Messages(Utilis plugin) {
        this.plugin = plugin;
        setup();
    }
    private void setup() {
        messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            // Try to copy the messages.yml from the JAR
            try {
                copyFromJar("messages.yml", messagesFile);
                plugin.getLogger().info("[Utilis] messages.yml copied from JAR.");
            } catch (IOException e) {
                plugin.getLogger().warning("[Utilis] Error copying messages.yml from JAR: " + e.getMessage());
                plugin.getLogger().severe("[Utilis] Message matejkoo on discord!");
            }
        }
        messagesConfig = new Configuration(messagesFile);
        messagesConfig.load();
    }
    private void copyFromJar(String resourceName, File outputFile) throws IOException {
        InputStream inputStream = plugin.getClass().getClassLoader().getResourceAsStream(resourceName);
        if (inputStream == null) {
            throw new IOException("Resource " + resourceName + " not found in JAR.");
        }
        // Ensure the file's parent directory exists
        if (!outputFile.exists()) {
            outputFile.getParentFile().mkdirs();
            outputFile.createNewFile();
        }
        // Copy the resource from the JAR to the plugin's data folder
        try (OutputStream outputStream = new FileOutputStream(outputFile)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
        } finally {
            inputStream.close();
        }
    }
    public String getMessage(String path) {
        return messagesConfig.getString(path);
    }

    public File getMessagesFile() {
        return messagesFile;
    }

    public Configuration getMessagesConfig() {
        return messagesConfig;
    }
}
