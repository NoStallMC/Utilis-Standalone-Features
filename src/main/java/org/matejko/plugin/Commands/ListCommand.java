package main.java.org.matejko.plugin.Commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import main.java.org.matejko.plugin.Utilis;
import main.java.org.matejko.plugin.FileCreator.Messages;
import main.java.org.matejko.plugin.Managers.ColorUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListCommand implements CommandExecutor {
    private final Utilis plugin;
    private final Map<String, String> messages;

    // Constructor that accepts the Utilis plugin instance
    public ListCommand(Utilis plugin) {
        this.plugin = plugin;
        this.messages = new HashMap<>();
        loadMessages();  // Load messages from config or hardcoded
    }

    private void loadMessages() {
        Messages textManager = new Messages(plugin);
        // Fetching messages from the configuration
        String playerCountMessage = textManager.getMessagesConfig().getString("list.playerCountMessage");
        String onlineMessage = textManager.getMessagesConfig().getString("list.onlineMessage");

        // Adding the fetched messages to the message map
        if (playerCountMessage != null) {
            messages.put("list.playerCountMessage", playerCountMessage);
        }
        if (onlineMessage != null) {
            messages.put("list.onlineMessage", onlineMessage);
        }
    }

    // Command execution logic
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be executed by a player.");
            return true;
        }

        execute(sender);
        return true;
    }

    // Method to execute the /list command
    public void execute(CommandSender sender) {
        int playerHidden = 0;  // Counter for vanished players
        List<String> onlinePlayerNames = new ArrayList<>();  // List to hold names of visible players

        // Loop through all online players
        for (Player player : Bukkit.getOnlinePlayers()) {
            // Check if the player is vanished. If vanished, skip adding to the list of visible players.
            if (plugin.getUtilisGetters().getVanishedPlayers().stream().anyMatch(vanishUser -> vanishUser.getPlayer().equals(player) && vanishUser.isVanished())) {
                playerHidden++;  // Increment the hidden player counter
                continue;  // Skip this player as they are vanished
            }

            // Check if the player is AFK and modify their display name accordingly
            String playerName = player.getDisplayName();
            if (plugin.getUtilisGetters().isAFK(player)) {  // Check if the player is AFK using the isAFK method from Utilis
                playerName = ChatColor.GRAY + "[AFK] " + playerName;  // Append [AFK] if the player is AFK
            }

            // If the player is not vanished, add their modified name to the online player names list
            onlinePlayerNames.add(playerName);
        }

        // Calculate the visible player count
        int visiblePlayerCount = Bukkit.getOnlinePlayers().length - playerHidden;

        // Format the player count message
        String playerCountMessage = messages.get("list.playerCountMessage")
                .replace("%playernumber%", String.valueOf(visiblePlayerCount))
                .replace("%max%", String.valueOf(Bukkit.getMaxPlayers()));

        // Translate any color codes in the player count message
        playerCountMessage = ColorUtil.translateColorCodes(playerCountMessage);
        sender.sendMessage(playerCountMessage);  // Send the player count message to the sender

        // Format the online players message with a list of names
        String onlineMessage = messages.get("list.onlineMessage")
                .replace("%players%", String.join(", ", onlinePlayerNames));

        // Translate any color codes in the online players message
        onlineMessage = ColorUtil.translateColorCodes(onlineMessage);
        sender.sendMessage(onlineMessage);  // Send the list of online players to the sender
    }
}
