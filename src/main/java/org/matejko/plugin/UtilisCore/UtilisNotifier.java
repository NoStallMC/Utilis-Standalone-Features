package main.java.org.matejko.plugin.UtilisCore;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import main.java.org.matejko.plugin.Utilis;
import main.java.org.matejko.plugin.FileCreator.Messages;
import main.java.org.matejko.plugin.Managers.ColorUtil;
import main.java.org.matejko.plugin.Managers.VanishUserManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UtilisNotifier implements Listener {
    private final Utilis plugin;
    private final Map<String, String> messages;
    private final Messages messagesConfig;
    public UtilisNotifier(Utilis plugin) {
        this.plugin = plugin;
        this.messages = new HashMap<>();
        this.messagesConfig = new Messages(plugin);
        loadMessages();
    }
    // Load custom messages from the messages.yml
    private void loadMessages() {
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
            plugin.getLogger().warning("[Utilis] Quit message is missing from the config.");
        }
    }
    // Notify when a player unvanishes
    public void notifyUnvanished(Player player) {
        String message = messages.get("messages.unvanished");
        if (message != null) {
            Bukkit.broadcastMessage(formatMessage(message, player));
        } else {
            plugin.getLogger().warning("[Utilis] Join message is missing from the config.");
        }
    }
    // Send custom join message
    public void sendJoinMessage(Player player) {
        String message = messages.get("messages.join");
        if (message != null) {
            Bukkit.broadcastMessage(formatMessage(message, player));
        } else {
            plugin.getLogger().warning("[Utilis] Join message is missing from the config.");
        }
    }
    // Send custom quit message
    public void sendQuitMessage(Player player) {
        String message = messages.get("messages.quit");
        if (message != null) {
            Bukkit.broadcastMessage(formatMessage(message, player));
        } else {
            plugin.getLogger().warning("[Utilis] Quit message is missing from the config.");
        }
    }
    // Format the message to include the player's name and apply color codes
    private String formatMessage(String message, Player player) {
        if (message == null || player == null) {
            return "";
        }
        message = message.replace("%player%", player.getDisplayName());
        return ColorUtil.translateColorCodes(message);
    }
    @EventHandler
    public void handlePlayerJoin(PlayerJoinEvent event) {
        Player newPlayer = event.getPlayer();
        event.setJoinMessage(null);
        if (plugin.getUtilisGetters().getMotdManager() != null) {
             List<String> motd = plugin.getUtilisGetters().getMotdManager().getMOTD(newPlayer);
                if (motd != null) {
                    for (String line : motd) {
                        newPlayer.sendMessage(ColorUtil.translateColorCodes(line));
                    }
                }
        } else {
            plugin.getLogger().warning("[Utilis] MOTDManager is null.");
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
    @EventHandler
    public void handlePlayerQuit(PlayerQuitEvent event) {
        Player quittingPlayer = event.getPlayer();
        event.setQuitMessage(null);
        if (!isPlayerVanished(quittingPlayer)) {
            sendQuitMessage(quittingPlayer);
        }
    }
    private boolean isPlayerVanished(Player player) {
        if (plugin.getUtilisGetters().getVanishedPlayers() == null) {
            plugin.getLogger().warning("[Utilis] Vanished players list is null.");
            return false;
        }
        return plugin.getUtilisGetters().getVanishedPlayers().stream()
                .anyMatch(vanishUser -> vanishUser.getPlayer().equals(player) && vanishUser.isVanished());
    }
}
