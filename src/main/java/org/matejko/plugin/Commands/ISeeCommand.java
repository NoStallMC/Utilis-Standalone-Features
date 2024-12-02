package main.java.org.matejko.plugin.Commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import main.java.org.matejko.plugin.Managers.ISeeManager;
import main.java.org.matejko.plugin.Utilis;
import main.java.org.matejko.plugin.Listeners.ISeeInventoryListener;
import main.java.org.matejko.plugin.Listeners.ISeeArmorListener;

public class ISeeCommand implements CommandExecutor {
    private final ISeeManager iSeeManager;
    private final ISeeInventoryListener iSeeInventoryListener;
    private final ISeeArmorListener iSeeArmorListener;
    
    public ISeeCommand(ISeeManager iSeeManager, ISeeInventoryListener iSeeInventoryListener, ISeeArmorListener iSeeArmorListener, Utilis plugin) {
        this.iSeeManager = iSeeManager;
        this.iSeeInventoryListener = iSeeInventoryListener;
        this.iSeeArmorListener = iSeeArmorListener;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check if the sender is a player
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }
        // Check if iSeeManager is null
        Player player = (Player) sender;
        if (iSeeManager == null) {
            player.sendMessage("§cInternal error: iSeeManager is not initialized.");
            System.err.println("Error: iSeeManager is null. Check plugin initialization.");
            return true;
        }
        // Check if the player is already in inventory viewing mode
        Player currentTarget = iSeeManager.getCurrentTarget(player);
        if (currentTarget != null) {
            // Restore the player's original inventory and exit viewing mode
            try {
                iSeeManager.restoreInventoryAndArmor(player);
                iSeeManager.clearTarget(player);
                player.sendMessage("§aYou have exited inventory viewing mode and your inventory and armor has been restored.");        
                // Stop inventory and armor syncing
                iSeeInventoryListener.stopInventorySync(player);
                iSeeArmorListener.stopArmorSync(player);
            } catch (Exception e) {
                player.sendMessage("§cError while restoring your inventory.");
                e.printStackTrace();
            }
            return true;
        }
        // Ensure a target player is specified
        if (args.length < 1) {
            player.sendMessage("§cUsage: /isee <player>");
            return true;
        }
        // Find the target player
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null || !target.isOnline()) {
            player.sendMessage("§cPlayer not found or offline.");
            return true;
        }
        // Save the viewer´s current inventory to restore later.
        try {
            iSeeManager.saveInventoryAndArmor(player);
        } catch (Exception e) {
            player.sendMessage("§cError saving your inventory. Please try again.");
            e.printStackTrace();
            return true;
        }
        // Clone the target player's inventory
        try {
            PlayerInventory targetInventory = target.getInventory();
            PlayerInventory viewerInventory = player.getInventory();
            viewerInventory.clear();
            ItemStack[] targetContents = targetInventory.getContents();
            if (targetContents != null) {
                viewerInventory.setContents(targetContents);
            }
            player.sendMessage("§aNow viewing and editing " + target.getName() + "'s inventory. Type /isee again to exit and restore your inventory.");
            iSeeManager.setCurrentTarget(player, target);
            iSeeInventoryListener.startInventorySync(player);
            iSeeArmorListener.startArmorSync(player);
        } catch (Exception e) {
            player.sendMessage("§cError accessing the target player's inventory.");
            e.printStackTrace();
            return true;
        }
        return true;
    }
}
