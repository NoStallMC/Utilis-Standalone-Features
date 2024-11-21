package main.java.org.matejko.plugin.FileCreator;

import org.bukkit.util.config.Configuration;
import main.java.org.matejko.plugin.Utilis;

import java.io.*;
import java.util.logging.Logger;

@SuppressWarnings("unused")
public class Messages {
    private final Utilis plugin;
    private File messagesFile;
    private Configuration messagesConfig;

    public Messages(Utilis plugin) {
        this.plugin = plugin;
        setup();
    }

    private void setup() {
        // The messages.yml file will be in the root directory of the plugin (plugins/Utilis/messages.yml)
        messagesFile = new File(plugin.getDataFolder(), "messages.yml");

        // Check if the file already exists in the plugin's data folder
        if (!messagesFile.exists()) {
            try {
                // Try to copy the resource from the JAR
                copyFromJar("messages.yml", messagesFile);
                plugin.getLogger().info("messages.yml copied from JAR.");
            } catch (IOException e) {
                plugin.getLogger().warning("Error copying messages.yml from JAR: " + e.getMessage());
                plugin.getLogger().warning("Message matejkoo on discord!");
            }
        }

        // Load the configuration after the file is either copied or created
        messagesConfig = new Configuration(messagesFile);
        messagesConfig.load();
    }

    private void copyFromJar(String resourceName, File outputFile) throws IOException {
        // Open the resource from the JAR file
        InputStream inputStream = plugin.getClass().getClassLoader().getResourceAsStream(resourceName);
        if (inputStream == null) {
            throw new IOException("Resource " + resourceName + " not found in JAR.");
        }

        // Ensure the file's parent directory exists
        if (!outputFile.exists()) {
            outputFile.getParentFile().mkdirs(); // Create directories if they don't exist
            outputFile.createNewFile(); // Create the file itself
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
