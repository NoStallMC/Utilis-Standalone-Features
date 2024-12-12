package main.java.org.matejko.plugin.Managers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import main.java.org.matejko.plugin.Utilis;
import main.java.org.matejko.plugin.FileCreator.Messages;
import java.util.ArrayList;
import java.util.List;

public class MOTDManager {
    private final List<String> motdLines;
    private final Utilis plugin;

    public MOTDManager(Utilis plugin) {
        this.plugin = plugin;
        Messages textManager = new Messages(plugin);
        this.motdLines = new ArrayList<>();
        loadMOTD(textManager);
    }
    
    private void loadMOTD(Messages textManager) {
        String[] motdMessages = {
            textManager.getMessage("motd.1"),
            textManager.getMessage("motd.2"),
            textManager.getMessage("motd.3")
        };
        for (String message : motdMessages) {
            if (message != null) {
                motdLines.add(message);
            }
        }
    }

    public void sendMOTD(Player player) {
        List<String> formattedMOTD = getMOTD(player);
        for (String line : formattedMOTD) {
            player.sendMessage(line);
        }
    }

    public List<String> getMOTD(Player player) {
        List<String> formattedMOTD = new ArrayList<>();
        String playerDisplayName = player.getDisplayName();
        for (String line : motdLines) {
            String formattedLine = line.replace("%player%", playerDisplayName);
            formattedLine = formattedLine.replace("%players%", getOnlinePlayerList());
            formattedMOTD.add(ColorUtil.translateColorCodes(formattedLine));
        }
        return formattedMOTD;
    }

    private String getOnlinePlayerList() {
        StringBuilder playerList = new StringBuilder();
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (!isPlayerVanished(onlinePlayer)) {
                String playerName = onlinePlayer.getDisplayName();
                // Add [AFK] if the player is AFK
                if (plugin.getUtilisGetters().isAFK(onlinePlayer)) {
                    playerName = ChatColor.GRAY + "[AFK] " + playerName;
                }
                if (playerList.length() > 0) {
                    playerList.append(", ");
                }
                playerList.append(playerName);
            }
        }
        return playerList.toString();
    }

    private boolean isPlayerVanished(Player player) {
        return plugin.getUtilisGetters().getVanishedPlayers().stream().anyMatch(vanishUser -> vanishUser.getPlayer().equals(player));
    }
}
