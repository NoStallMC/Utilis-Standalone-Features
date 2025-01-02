package main.java.org.matejko.plugin.Commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import main.java.org.matejko.plugin.Utilis;
import main.java.org.matejko.plugin.FileCreator.Config;
import main.java.org.matejko.plugin.Managers.VanishUserManager;
import org.bukkit.ChatColor;

public class VanishCommand implements CommandExecutor {
    private final Utilis plugin;
    private final Config config;

    public VanishCommand(Utilis plugin, Config config) {
        this.plugin = plugin;
        this.config = config;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be executed by a player.");
            return true;
        }
        Player player = (Player) sender;
        // If a player name is specified, toggle vanish for the target player
        if (args.length == 1) {
            String targetName = args[0];
            Player targetPlayer = getTargetPlayer(targetName);
            if (targetPlayer == null) {
                player.sendMessage(ChatColor.RED + "Player not found or offline.");
                return true;
            }
            toggleVanish(targetPlayer);
            return true;
        }
        // If no player name is provided, toggle vanish for the sender
        toggleVanish(player);
        return true;
    }
    // Toggle vanish for a player (whether the sender or a target player)
    private void toggleVanish(Player player) {
        VanishUserManager vanishUser = plugin.getUtilisGetters().getVanishedPlayers().stream()
                .filter(vu -> vu.getPlayer().equals(player))
                .findFirst()
                .orElse(null);
        if (vanishUser != null) {
            // Player is already vanished, so unvanish them
            plugin.getUtilisGetters().getVanishedPlayers().remove(vanishUser);
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.showPlayer(player);
            }
            plugin.getUtilisGetters().getUtilisNotifier().notifyUnvanished(player);
            if (config.isDynmapHideEnabled()) {
                plugin.getUtilisGetters().getDynmapManager().removeFromHiddenPlayersFile(player.getName()); // Show them on Dynmap.
            }
            player.sendMessage(ChatColor.GRAY + "You are now visible to other players.");
        } else {
            // Player isn't vanished, so vanish them
            VanishUserManager newVanishUser = new VanishUserManager(player, true);
            plugin.getUtilisGetters().getVanishedPlayers().add(newVanishUser);
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.hidePlayer(player);
                // Only show player to those with the permission
                if (config.isOpSeeVanishEnabled() && p.hasPermission("utilis.vanish")) {
                    p.showPlayer(player);
                }
            }
            plugin.getUtilisGetters().getUtilisNotifier().notifyVanished(player);
            if (config.isDynmapHideEnabled()) {
                plugin.getUtilisGetters().getDynmapManager().addToHiddenPlayersFile(player.getName()); // Hide them on Dynmap.
            }
            player.sendMessage(ChatColor.GRAY + "You are now hidden from other players.");
        }
        // Save the updated list of vanished players to the file
        plugin.getUtilisGetters().getVanishedPlayersManager().saveVanishedPlayers(plugin.getUtilisGetters().getVanishedPlayers());
    }
    // Get a target player using partial name match
    private Player getTargetPlayer(String targetName) {
        Player targetPlayer = null;
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.getName().toLowerCase().contains(targetName.toLowerCase())) {
                targetPlayer = onlinePlayer;
                break;
            }
        }
        if (targetPlayer == null) {
            targetPlayer = Bukkit.getPlayerExact(targetName);
        }
        return targetPlayer;
    }
}
