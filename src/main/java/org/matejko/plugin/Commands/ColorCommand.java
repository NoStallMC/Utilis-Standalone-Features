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

public class ColorCommand implements org.bukkit.command.CommandExecutor {

    private final NickManager nickManager;
    private final CooldownManager cooldownManager;
    private final Messages messages;
    private final Logger logger;

    public ColorCommand(NickManager nickManager, CooldownManager cooldownManager, Messages messages) {
        this.nickManager = nickManager;
        this.cooldownManager = cooldownManager;
        this.messages = messages;
        this.logger = Logger.getLogger("Utilis");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can change their color.");
            return false;
        }

        Player player = (Player) sender;

        // Check if the player is on cooldown
        if (cooldownManager.isOnColorCooldown(player)) {
            long remaining = cooldownManager.getRemainingColorCooldown(player);
            String cooldownMessage = ChatColor.RED + "You must wait " + remaining + " seconds before using /color again.";
            player.sendMessage(cooldownMessage);
            return false;
        }

        // Handle command arguments
        if (args.length == 0) {
            String usageMessage = messages.getMessage("color.usage");
            if (usageMessage == null) {
                logger.warning("[Utilis] Message 'color.usage' is null. Please check your messages config.");
                usageMessage = ChatColor.RED + "Usage: /color <color> or /color help";  // Fallback message
            }
            player.sendMessage(usageMessage);
            return false;
        }

        if (args[0].equalsIgnoreCase("help")) {
            player.sendMessage(ChatColor.GOLD + "Available nickname colors: " +
                    ChatColor.BLACK + "black, " + ChatColor.DARK_BLUE + "dark_blue, " +
                    ChatColor.DARK_GREEN + "dark_green, " + ChatColor.DARK_AQUA + "dark_aqua, " +
                    ChatColor.DARK_RED + "dark_red, " + ChatColor.DARK_PURPLE + "dark_purple, " +
                    ChatColor.GOLD + "gold, " + ChatColor.GRAY + "gray, " +
                    ChatColor.DARK_GRAY + "dark_gray, " + ChatColor.BLUE + "blue, " +
                    ChatColor.GREEN + "green, " + ChatColor.AQUA + "aqua, " +
                    ChatColor.RED + "red, " + ChatColor.LIGHT_PURPLE + "light_purple, " +
                    ChatColor.YELLOW + "yellow, " + ChatColor.WHITE + "white.");
            return true;
        }

        // Validate the color
        String color = args[0].toUpperCase();
        if (!nickManager.isValidColor(color)) {
            String invalidMessage = messages.getMessage("color.invalid");
            if (invalidMessage == null) {
                logger.warning("[Utilis] Message 'color.invalid' is null. Please check your messages config.");
                invalidMessage = ChatColor.RED + "Invalid color.";  // Fallback message
            }
            player.sendMessage(invalidMessage);
            return false;
        }

        // Set the player's nickname color
        nickManager.setNicknameColor(player, color);

        // Set the cooldown
        cooldownManager.setColorCooldown(player);

        // Inform the player of the change
        String colorSetMessage = messages.getMessage("color.set");
        if (colorSetMessage == null) {
            logger.warning("[Utilis] Message 'color.set' is null. Please check your messages config.");
            colorSetMessage = "&7Your color has been changed to %color%";  // Fallback message
        }

        // Format the message to include the actual color (and apply the color formatting).
        ChatColor chatColor = ChatColor.valueOf(color);
        colorSetMessage = colorSetMessage.replace("%color%", chatColor + color);  // Insert the color and apply formatting
        player.sendMessage(ColorUtil.translateColorCodes(colorSetMessage));  // Translates color codes

        return true;
    }
}
