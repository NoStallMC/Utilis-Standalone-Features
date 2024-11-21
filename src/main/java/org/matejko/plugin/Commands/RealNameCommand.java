package main.java.org.matejko.plugin.Commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import main.java.org.matejko.plugin.Managers.NickManager;

import java.util.Map;

public class RealNameCommand implements org.bukkit.command.CommandExecutor {

    private final NickManager nickManager;

    public RealNameCommand(NickManager nickManager) {
        this.nickManager = nickManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /realname <nickname>");
            return false;
        }

        // Prefix the nickname with "~" if the player is searching by nickname
        String nickname = "~" + args[0];

        // Get the player's real name by searching the player data for the nickname
        String realName = getRealNameFromNickname(nickname);

        if (realName != null) {
            // Get the color of the nickname (from player data) and apply it
            String nicknameColor = getNicknameColor(nickname);
            String coloredNickname = nicknameColor + nickname + ChatColor.WHITE;

            sender.sendMessage(ChatColor.GOLD + "Real name of " + coloredNickname + ChatColor.GOLD + " is " + realName + ".");
        } else {
            sender.sendMessage(ChatColor.RED + "No real name found for " + nickname + ".");
        }

        return true;
    }

    // Method to get the real player name from the nickname stored in playerData
    private String getRealNameFromNickname(String nickname) {
        // Iterate over the playerData map to find the original player name from the nickname
        for (Map.Entry<String, String[]> entry : nickManager.getPlayerData().entrySet()) {
            String[] data = entry.getValue();
            if (data[0].equals(nickname)) {
                return entry.getKey();  // Return the original player name
            }
        }
        return null;  // Return null if the nickname is not found
    }

    // Method to get the nickname color from player data
    private String getNicknameColor(String nickname) {
        // Iterate over the player data to find the color associated with the nickname
        for (Map.Entry<String, String[]> entry : nickManager.getPlayerData().entrySet()) {
            String[] data = entry.getValue();
            if (data[0].equals(nickname)) {
                String color = data[1];  // Color is stored as the second element in the array
                try {
                    // Try to convert the color to a ChatColor
                    return ChatColor.valueOf(color.toUpperCase()).toString();
                } catch (IllegalArgumentException e) {
                    // If the color is invalid, default to white
                    return ChatColor.WHITE.toString();
                }
            }
        }
        return ChatColor.WHITE.toString();  // Default to white if no color is found
    }
}
