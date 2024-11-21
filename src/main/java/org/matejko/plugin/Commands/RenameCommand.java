package main.java.org.matejko.plugin.Commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import main.java.org.matejko.plugin.Managers.NickManager;

public class RenameCommand implements org.bukkit.command.CommandExecutor {

    private final NickManager nickManager;

    public RenameCommand(NickManager nickManager) {
        this.nickManager = nickManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can execute this command.");
            return false;
        }

        Player player = (Player) sender;

        // Check if the player is OP (this will bypass the permission check for OPs)
        if (!player.isOp()) {
            // Check permission for renaming other players (non-OP players)
            if (!player.hasPermission("nicks.rename.others")) {
                player.sendMessage(ChatColor.RED + "You do not have permission to rename other players.");
                return false;
            }
        }

        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /rename <player> <nickname>");
            return false;
        }

        String targetName = args[0];  // The first argument is either a nickname or a player name
        String newNickname = args[1];  // The second argument is the new nickname

        // Try to get the player by nickname or player name
        Player targetPlayer = getTargetPlayer(targetName);

        if (targetPlayer == null) {
            player.sendMessage(ChatColor.RED + "Player with the name or nickname '" + targetName + "' not found.");
            return false;
        }

        // Check if the new nickname is already taken
        if (!nickManager.isValidNickname(newNickname)) {
            player.sendMessage(ChatColor.RED + "The nickname '" + "~" + newNickname + "' is already in use.");
            return false;
        }

        // Set the new nickname for the target player
        nickManager.setNickname(targetPlayer, newNickname);

        player.sendMessage(ChatColor.GRAY + "You have renamed " + targetPlayer.getName() + " to " + "~" + newNickname + ".");
        targetPlayer.sendMessage(ChatColor.GRAY + "You have been renamed to " + "~" + newNickname + ".");
        return true;
    }

    // Method to get the target player by either their real name or nickname
    private Player getTargetPlayer(String targetName) {
        // First, check if the name is a nickname by looking it up in the NickManager
        Player targetPlayer = null;

        // Search by nickname (add a "~" prefix to the target name)
        String nickname = "~" + targetName;
        for (String playerName : nickManager.getPlayerData().keySet()) {
            String[] data = nickManager.getPlayerData().get(playerName);
            if (data != null && data[0].equals(nickname)) {
                // If the nickname is found, get the real player name and fetch the player object
                targetPlayer = nickManager.getServer().getPlayer(playerName);
                break;
            }
        }

        // If targetPlayer is still null, try to find the player by their real name
        if (targetPlayer == null) {
            targetPlayer = nickManager.getServer().getPlayerExact(targetName);
        }

        return targetPlayer;
    }
}
