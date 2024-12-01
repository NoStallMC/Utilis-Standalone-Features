package main.java.org.matejko.plugin.Listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import main.java.org.matejko.plugin.Managers.ISeeManager;

public class ISeeArmorRemover implements Listener {
    private final ISeeManager manager;  // Reference to ISeeManager

    // Constructor to initialize ISeeArmorRemover with ISeeManager
    public ISeeArmorRemover(ISeeManager manager) {
        this.manager = manager;
    }

    // Handle player item drop event
    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        ItemStack droppedItem = event.getItemDrop().getItemStack();  // Get the dropped item

        // Ensure the dropped item is not null or air (using ID-based check)
        if (droppedItem == null || droppedItem.getTypeId() == 0) {
            return;
        }

        // Get the target player using ISeeManager
        Player target = manager.getCurrentTarget(player);  // Fetch the target player from ISeeManager

        // If a valid target is set and the dropped item matches target's armor
        if (target != null && isMatchingArmor(droppedItem, target)) {
            // Remove the matching armor piece from the target's inventory
            removeArmorPieceFromTarget(target, droppedItem);

            // Optionally, log the armor removal for debugging purposes
            if (isDebugEnabled()) {
                System.out.println("[DEBUG] Removed matching armor piece from target: " + target.getName());
            }
        }
    }

    // Compare the dropped item with an armor piece from the target based on material, durability, and meta
    private boolean isMatchingArmor(ItemStack droppedItem, Player target) {
        // Ensure the dropped item and target armor are not null
        if (droppedItem == null || droppedItem.getTypeId() == 0) {
            return false;
        }

        // Compare each piece of the target's armor (helmet, chestplate, leggings, boots)
        if (target.getInventory().getHelmet() != null && isSameItem(droppedItem, target.getInventory().getHelmet())) {
            return true;
        } else if (target.getInventory().getChestplate() != null && isSameItem(droppedItem, target.getInventory().getChestplate())) {
            return true;
        } else if (target.getInventory().getLeggings() != null && isSameItem(droppedItem, target.getInventory().getLeggings())) {
            return true;
        } else if (target.getInventory().getBoots() != null && isSameItem(droppedItem, target.getInventory().getBoots())) {
            return true;
        }

        return false;
    }

    // Compare the dropped item with an armor piece from the target based on material, durability, and meta
    private boolean isSameItem(ItemStack droppedItem, ItemStack targetItem) {
        // Compare material type by ID
        if (droppedItem.getTypeId() != targetItem.getTypeId()) {
            return false;
        }

        // Compare durability
        if (droppedItem.getDurability() != targetItem.getDurability()) {
            return false;
        }

        return true;
    }

 // Remove the matching armor piece from the target's inventory
    private void removeArmorPieceFromTarget(Player target, ItemStack droppedItem) {
        // Ensure armor removal is handled correctly, with proper null checks
        boolean armorChanged = false;

        if (target.getInventory().getHelmet() != null && isSameItem(droppedItem, target.getInventory().getHelmet())) {
            target.getInventory().setHelmet(null);  // Use `null` to clear the helmet slot
            armorChanged = true;
        } else if (target.getInventory().getChestplate() != null && isSameItem(droppedItem, target.getInventory().getChestplate())) {
            target.getInventory().setChestplate(null);  // Use `null` to clear the chestplate slot
            armorChanged = true;
        } else if (target.getInventory().getLeggings() != null && isSameItem(droppedItem, target.getInventory().getLeggings())) {
            target.getInventory().setLeggings(null);  // Use `null` to clear the leggings slot
            armorChanged = true;
        } else if (target.getInventory().getBoots() != null && isSameItem(droppedItem, target.getInventory().getBoots())) {
            target.getInventory().setBoots(null);  // Use `null` to clear the boots slot
            armorChanged = true;
        }

        // If any armor was changed, save the new state and defer restoration
        if (armorChanged) {
            manager.saveInventoryAndArmor(target); // Save updated state to avoid blank armor upon rejoin

            // Optionally, log the change for debugging purposes
            if (manager.isDebugEnabled()) {
                System.out.println("[DEBUG] Updated armor for player: " + target.getName());
            }
        }
    }

    // Check if debug flag is enabled in ISeeManager's configuration
    private boolean isDebugEnabled() {
        return manager.isDebugEnabled();  // Access debug setting from ISeeManager
    }
}
