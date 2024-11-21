package main.java.org.matejko.plugin.Managers;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class ChatFormattingManager implements Listener {

    private final JavaPlugin plugin;
    private boolean chatFormattingEnabled;

    public ChatFormattingManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.chatFormattingEnabled = plugin.getConfiguration().getBoolean("features.chat-formatting", true);  // Default is true
    }
    
    // Event handler to format chat messages
    @EventHandler
    public void onChat(PlayerChatEvent event) {
        if (chatFormattingEnabled) {
            String message = event.getMessage();
            // Use ColorUtil to translate & color codes to ChatColor
            message = ColorUtil.translateColorCodes(message);
            event.setMessage(message);
        }
    }

    // Method to load the configuration for chat formatting (can be called in onEnable)
    public void loadConfiguration() {
        this.chatFormattingEnabled = plugin.getConfiguration().getBoolean("features.chat-formatting", true);
    }
}
