package main.java.org.matejko.plugin.Commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import main.java.org.matejko.plugin.Managers.NickManager;
import main.java.org.matejko.plugin.Managers.CooldownManager;

public class NickResetCommand implements org.bukkit.command.CommandExecutor {
    private final NickManager nickManager;
    private final CooldownManager cooldownManager;

    public NickResetCommand(NickManager nickManager, CooldownManager cooldownManager) {
        this.nickManager = nickManager;
        this.cooldownManager = cooldownManager;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can execute this command.");
            return false;
        }
        Player player = (Player) sender;
        if (cooldownManager.isOnResetCooldown(player)) {
            long remaining = cooldownManager.getRemainingResetCooldown(player);
            player.sendMessage(ChatColor.RED + "You must wait " + remaining + " seconds before using /nickreset again.");
            return false;
        }
        // Reset player's nickname
        nickManager.resetNickname(player);
        player.sendMessage(ChatColor.GREEN + "Your nickname has been reset.");
        cooldownManager.setResetCooldown(player);
        return true;
    }
}
