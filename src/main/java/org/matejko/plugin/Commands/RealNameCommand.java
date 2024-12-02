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
        String nickname = "~" + args[0];
        String realName = getRealNameFromNickname(nickname);
        if (realName != null) {
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
        for (Map.Entry<String, String[]> entry : nickManager.getPlayerData().entrySet()) {
            String[] data = entry.getValue();
            if (data[0].equals(nickname)) {
                return entry.getKey();
            }
        }
        return null;
    }
    private String getNicknameColor(String nickname) {
        for (Map.Entry<String, String[]> entry : nickManager.getPlayerData().entrySet()) {
            String[] data = entry.getValue();
            if (data[0].equals(nickname)) {
                String color = data[1];
                try {
                    // Try to convert the color to a ChatColor
                    return ChatColor.valueOf(color.toUpperCase()).toString();
                  } catch (IllegalArgumentException e) {
                    return ChatColor.WHITE.toString();   // If the color is invalid, use white
                }
            }
        }
        return ChatColor.WHITE.toString();  // Use white if no color is found
    }
}
