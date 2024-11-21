package main.java.org.matejko.plugin.Commands;

import java.util.Collection;
import java.util.logging.Logger;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;

public class SuckCommand implements CommandExecutor {

    @SuppressWarnings("unused")
	private Logger logger;
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("suck")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                // Check if the player is an OP
                if (player.isOp()) {
                    teleportItemsToPlayer(player);
                    player.sendMessage(ChatColor.GRAY + "All dropped items have been teleported to you!");
                } else {
                    player.sendMessage(ChatColor.RED +"You do not have permission to use this command.");
                }
            } else {
                sender.sendMessage("Only players can use this command.");
            }
            return true;
        }
        return false;
    }

    private void teleportItemsToPlayer(Player player) {
        Location playerLocation = player.getLocation();
        Collection<org.bukkit.entity.Entity> entities = player.getWorld().getEntities();

        for (org.bukkit.entity.Entity entity : entities) {
            if (entity instanceof Item) {
                Item item = (Item) entity;
                if (item.getLocation().distance(playerLocation) < 100) { // Adjust distance as needed
                    item.teleport(playerLocation);
                }
            }
        }
    }
}
