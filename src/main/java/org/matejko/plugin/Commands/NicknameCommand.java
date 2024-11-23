package main.java.org.matejko.plugin.Commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import main.java.org.matejko.plugin.Managers.NickManager;
import main.java.org.matejko.plugin.Managers.CooldownManager;
import main.java.org.matejko.plugin.FileCreator.Messages;
import main.java.org.matejko.plugin.Managers.ColorUtil;

import java.util.logging.Logger;

public class NicknameCommand implements org.bukkit.command.CommandExecutor {

    private final NickManager nickManager;
    private final CooldownManager cooldownManager;
    private final Messages messages;
    private final Logger logger;

    public NicknameCommand(NickManager nickManager, CooldownManager cooldownManager, Messages messages) {
        this.nickManager = nickManager;
        this.cooldownManager = cooldownManager;
        this.messages = messages;
        this.logger = Logger.getLogger("Utilis");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can execute this command.");
            return false;
        }

        Player player = (Player) sender;

        // Check if the player is on cooldown
        if (cooldownManager.isOnNicknameCooldown(player)) {
            long remaining = cooldownManager.getRemainingNicknameCooldown(player);
            String cooldownMessage = ChatColor.RED + "You must wait " + remaining + " seconds before using /nickname again.";
            player.sendMessage(cooldownMessage);
            return false;
        }

        // If no nickname is provided
        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "Usage: /nickname <nickname>");
            return false;
        }

        String nickname = args[0];

        // Check if the nickname is valid (not already in use)
        if (!nickManager.isValidNickname(nickname)) {
            String nicknameUsedMessage = messages.getMessage("nickname.used");
            if (nicknameUsedMessage == null) {
                logger.warning("[Utilis] Message 'nickname.used' is null. Please check your messages config.");
                nicknameUsedMessage = "That nickname is already in use.";  // Fallback message
            }
            player.sendMessage(formatMessage(nicknameUsedMessage, player, nickname));
            return false;
        }

        // Set the player's nickname
        try {
            nickManager.setNickname(player, nickname);
        } catch (Exception e) {
            logger.severe("[Utilis] Error while setting nickname for player " + player.getName());
            e.printStackTrace();
            player.sendMessage(ChatColor.RED + "An error occurred while setting your nickname.");
            return false;
        }

        // Set the cooldown
        cooldownManager.setNicknameCooldown(player);

        // Notify the player their nickname has been set
        String nicknameSetMessage = messages.getMessage("nickname.set");
        if (nicknameSetMessage == null) {
            logger.warning("[Utilis] Message 'nickname.set' is null. Please check your messages config.");
            nicknameSetMessage = "&7Your nickname has been set to: %nickname%";  // Fallback message
        }

        player.sendMessage(formatMessage(nicknameSetMessage, player, nickname));
        return true;
    }

    // Helper method to format the messages
    private String formatMessage(String message, Player player) {
        message = message.replace("%player%", player.getDisplayName());
        return ColorUtil.translateColorCodes(message);  // Translates color codes
    }

    // Helper method for nickname-related messages
    private String formatMessage(String message, Player player, String nickname) {
        // Get the player's color and apply it to the nickname
        String playerColor = nickManager.getPlayerColor(player);
        message = message.replace("%nickname%", ChatColor.valueOf(playerColor.toUpperCase()) + "~" + nickname + ChatColor.WHITE);  // Apply the player's color
        message = formatMessage(message, player);  // Apply player name formatting
        return message;
    }
}
