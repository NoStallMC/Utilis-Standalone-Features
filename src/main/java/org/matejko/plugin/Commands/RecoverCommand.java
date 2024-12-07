package main.java.org.matejko.plugin.Commands;

import main.java.org.matejko.plugin.Managers.RecoverManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.UUID;

public class RecoverCommand implements CommandExecutor {
    private final RecoverManager recoverManager;
    public RecoverCommand(RecoverManager recoverManager) {
        this.recoverManager = recoverManager;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check if the command arguments are correct
        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /recover <playername>");
            return true;
        }
        String playerName = args[0].toLowerCase();
        Player matchedPlayer = null;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getName().toLowerCase().contains(playerName)) {
                matchedPlayer = player;
                break;
            }
        }
        if (matchedPlayer == null) {
            sender.sendMessage(ChatColor.RED + "Player not found!");
            return true;
        }
        UUID playerUUID = matchedPlayer.getUniqueId();
        if (!recoverManager.hasSavedInventory(playerUUID)) {
            sender.sendMessage(ChatColor.YELLOW + "No saved inventory found for " + matchedPlayer.getName());
            return true;
        }
        // Restore the player's inventory from the saved data
        matchedPlayer.getInventory().setContents(recoverManager.recoverPlayerInventory(playerUUID));
        sender.sendMessage(ChatColor.GREEN + "Recovered inventory for " + matchedPlayer.getName());
        matchedPlayer.sendMessage(ChatColor.GREEN + "Your inventory has been recovered by an admin.");
        return true;
    }
}
