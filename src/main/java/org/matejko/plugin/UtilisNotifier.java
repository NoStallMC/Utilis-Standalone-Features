package main.java.org.matejko.plugin;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import main.java.org.matejko.plugin.FileCreator.Messages;
import main.java.org.matejko.plugin.Managers.ColorUtil;
import main.java.org.matejko.plugin.Managers.VanishUserManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UtilisNotifier implements Listener {
    private final Utilis plugin;
    private final Map<String, String> messages;
    private final Messages messagesConfig; // Store the Messages instance

    // Constructor without VanishCommand
    public UtilisNotifier(Utilis plugin) {
        this.plugin = plugin;
        this.messages = new HashMap<>();
        this.messagesConfig = new Messages(plugin);  // Initialize the Messages class
        loadMessages();  // Load the messages from the config
    }

    // Load custom messages from the config
    private void loadMessages() {
        // Load the custom messages from the config
        messages.put("messages.vanished", messagesConfig.getMessage("messages.quit"));
        messages.put("messages.unvanished", messagesConfig.getMessage("messages.join"));
        messages.put("messages.join", messagesConfig.getMessage("messages.join"));
        messages.put("messages.quit", messagesConfig.getMessage("messages.quit"));
    }

    // Notify when a player vanishes
    public void notifyVanished(Player player) {
        String message = messages.get("messages.vanished");
        if (message != null) {
            Bukkit.broadcastMessage(formatMessage(message, player));
        } else {
            plugin.getLogger().warning("Vanished message is missing from the config.");
        }
    }

    // Notify when a player unvanishes
    public void notifyUnvanished(Player player) {
        String message = messages.get("messages.unvanished");
        if (message != null) {
            Bukkit.broadcastMessage(formatMessage(message, player));
        } else {
            plugin.getLogger().warning("Unvanished message is missing from the config.");
        }
    }

    // Send custom join message
    public void sendJoinMessage(Player player) {
        String message = messages.get("messages.join");
        if (message != null) {
            Bukkit.broadcastMessage(formatMessage(message, player));
        } else {
            plugin.getLogger().warning("Join message is missing from the config.");
        }
    }

    // Send custom quit message
    public void sendQuitMessage(Player player) {
        String message = messages.get("messages.quit");
        if (message != null) {
            Bukkit.broadcastMessage(formatMessage(message, player));
        } else {
            plugin.getLogger().warning("Quit message is missing from the config.");
        }
    }

    // Format the message to include the player's name and apply color codes
    private String formatMessage(String message, Player player) {
        if (message == null || player == null) {
            return "";
        }
        message = message.replace("%player%", player.getDisplayName());
        return ColorUtil.translateColorCodes(message);  // Translates color codes from the config
    }

    // Custom event handler for player join
    @EventHandler
    public void handlePlayerJoin(PlayerJoinEvent event) {
        Player newPlayer = event.getPlayer();
        event.setJoinMessage(null);  // Prevent the default join message

        // Ensure MOTD Manager is not null
        if (plugin.getUtilisGetters().getMotdManager() != null) {
            // Send MOTD to the joining player only
            if (!isPlayerVanished(newPlayer)) {  // Optional: Send MOTD only to non-vanished players
                List<String> motd = plugin.getUtilisGetters().getMotdManager().getMOTD(newPlayer);
                if (motd != null) {
                    for (String line : motd) {
                        newPlayer.sendMessage(ColorUtil.translateColorCodes(line));
                    }
                }
            }
        } else {
            plugin.getLogger().warning("MOTDManager is null.");
        }

        // Send the custom join message if the player is not vanished
        if (!isPlayerVanished(newPlayer)) {
            sendJoinMessage(newPlayer);
        }

        // Hide vanished players from the joining player
        for (VanishUserManager vanishUser : plugin.getUtilisGetters().getVanishedPlayers()) {
            if (vanishUser.getPlayer() != null) {
                newPlayer.hidePlayer(vanishUser.getPlayer());
            }
        }
    }

    // Custom event handler for player quit
    @EventHandler
    public void handlePlayerQuit(PlayerQuitEvent event) {
        Player quittingPlayer = event.getPlayer();
        event.setQuitMessage(null);  // Prevent the default quit message

        // If the player is not vanished, send the custom quit message
        if (!isPlayerVanished(quittingPlayer)) {
            sendQuitMessage(quittingPlayer);
        }
    }

    // Helper method to check if a player is vanished
    private boolean isPlayerVanished(Player player) {
        if (plugin.getUtilisGetters().getVanishedPlayers() == null) {
            plugin.getLogger().warning("Vanished players list is null.");
            return false;
        }
        return plugin.getUtilisGetters().getVanishedPlayers().stream()
                .anyMatch(vanishUser -> vanishUser.getPlayer().equals(player) && vanishUser.isVanished());
    }
}
