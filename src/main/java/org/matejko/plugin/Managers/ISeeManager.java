package main.java.org.matejko.plugin.Managers;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import main.java.org.matejko.plugin.Utilis;
import org.bukkit.util.config.Configuration;

import java.util.HashMap;
import java.util.Map;

public class ISeeManager {
    private final Map<Player, ItemStack[]> savedInventories = new HashMap<>();
    private final Map<Player, ItemStack[]> savedArmor = new HashMap<>();
    private final Map<Player, Player> currentTargets = new HashMap<>();
    private final Configuration config;  // Reference to Configuration to check if debug is enabled
    @SuppressWarnings("unused")
	private final Utilis plugin;
    // Constructor to initialize ISeeManager with a Configuration object
    public ISeeManager(Utilis plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfiguration();  // Use the plugin's Configuration directly
    }

    // Save the inventory and armor of the viewer
    public void saveInventoryAndArmor(Player player) {
        savedInventories.put(player, player.getInventory().getContents());

        // Save armor contents with proper checks using IDs instead of Material
        ItemStack[] armorContents = new ItemStack[4]; // For helmet, chestplate, leggings, boots
        armorContents[0] = player.getInventory().getHelmet() != null ? player.getInventory().getHelmet() : new ItemStack(0);  // 0 is an invalid item ID (AIR)
        armorContents[1] = player.getInventory().getChestplate() != null ? player.getInventory().getChestplate() : new ItemStack(0);  // 0 is an invalid item ID (AIR)
        armorContents[2] = player.getInventory().getLeggings() != null ? player.getInventory().getLeggings() : new ItemStack(0);  // 0 is an invalid item ID (AIR)
        armorContents[3] = player.getInventory().getBoots() != null ? player.getInventory().getBoots() : new ItemStack(0);  // 0 is an invalid item ID (AIR)

        savedArmor.put(player, armorContents);
    }

    // Restore the inventory and armor of the viewer instantly (no delays or steps)
    public void restoreInventoryAndArmor(Player player) {
        // Restore inventory immediately
        restoreInventoryStep(player);
        
        // Restore armor immediately after inventory
        restoreArmorStep(player);
    }

    private void restoreInventoryStep(final Player player) {
        // Restore inventory
        ItemStack[] contents = savedInventories.remove(player);
        if (contents != null) {
            try {
                if (isDebugEnabled()) { // Check if debug is enabled before printing
                    System.out.println("[DEBUG] Restoring inventory for player: " + player.getName());
                }
                player.getInventory().setContents(contents);
            } catch (Exception e) {
                if (isDebugEnabled()) { // Check if debug is enabled before printing
                    System.out.println("[ERROR] Failed to restore inventory for player " + player.getName() + ": " + e.getMessage());
                }
                e.printStackTrace();
                resetInventory(player); // Fallback if inventory restoration fails
            }
        } else {
            if (isDebugEnabled()) { // Check if debug is enabled before printing
                System.out.println("[DEBUG] No saved inventory for player: " + player.getName());
            }
            resetInventory(player); // Reset inventory if no saved inventory is found
        }
    }

    public void restoreArmorStep(final Player player) {
        // Restore armor immediately
        ItemStack[] armorContents = savedArmor.remove(player);
        if (armorContents != null) {
            if (armorContents.length == 4) {
                try {
                    if (isDebugEnabled()) { // Check if debug is enabled before printing
                        System.out.println("[DEBUG] Restoring armor for player: " + player.getName());
                    }

                    PlayerInventory inv = player.getInventory();
                    // Ensure armor pieces are restored only if they are not AIR (0 ID)
                    inv.setHelmet(armorContents[0].getTypeId() != 0 ? armorContents[0] : null);
                    inv.setChestplate(armorContents[1].getTypeId() != 0 ? armorContents[1] : null);
                    inv.setLeggings(armorContents[2].getTypeId() != 0 ? armorContents[2] : null);
                    inv.setBoots(armorContents[3].getTypeId() != 0 ? armorContents[3] : null);

                    // Debug: Armor has been restored
                    if (isDebugEnabled()) { // Check if debug is enabled before printing
                        System.out.println("[DEBUG] Armor restored for player: " + player.getName());
                    }
                } catch (Exception e) {
                    if (isDebugEnabled()) { // Check if debug is enabled before printing
                        System.out.println("[ERROR] Failed to restore armor for player " + player.getName() + ": " + e.getMessage());
                    }
                    e.printStackTrace();
                    resetArmor(player); // Fallback if armor restoration fails
                }
            } else {
                if (isDebugEnabled()) { // Check if debug is enabled before printing
                    System.out.println("[ERROR] Saved armor data for player " + player.getName() + " is invalid (length mismatch). Resetting armor.");
                }
                resetArmor(player);
            }
        } else {
            if (isDebugEnabled()) { // Check if debug is enabled before printing
                System.out.println("[DEBUG] No saved armor for player: " + player.getName());
            }
            resetArmor(player); // Reset armor if no saved armor is found
        }
    }

    // Helper method to reset inventory slots to null
    private void resetInventory(Player player) {
        try {
            player.getInventory().clear();
            if (isDebugEnabled()) { // Check if debug is enabled before printing
                System.out.println("[DEBUG] Inventory reset for player: " + player.getName());
            }
        } catch (Exception e) {
            if (isDebugEnabled()) { // Check if debug is enabled before printing
                System.out.println("[ERROR] Failed to reset inventory for player " + player.getName() + ": " + e.getMessage());
            }
            e.printStackTrace();
        }
    }

    // Helper method to reset armor slots to null (using IDs)
    private void resetArmor(Player player) {
        try {
            player.getInventory().setHelmet(new ItemStack(0));  // AIR is ID 0
            player.getInventory().setChestplate(new ItemStack(0));  // AIR is ID 0
            player.getInventory().setLeggings(new ItemStack(0));  // AIR is ID 0
            player.getInventory().setBoots(new ItemStack(0));  // AIR is ID 0
            if (isDebugEnabled()) { // Check if debug is enabled before printing
                System.out.println("[DEBUG] Armor reset for player: " + player.getName());
            }
        } catch (Exception e) {
            if (isDebugEnabled()) { // Check if debug is enabled before printing
                System.out.println("[ERROR] Failed to reset armor for player " + player.getName() + ": " + e.getMessage());
            }
            e.printStackTrace();
        }
    }

    // Set the current target player
    public void setCurrentTarget(Player viewer, Player target) {
        currentTargets.put(viewer, target);
    }

    // Get the current target player
    public Player getCurrentTarget(Player viewer) {
        return currentTargets.get(viewer);
    }

    // Clear the target tracking for a viewer
    public void clearTarget(Player viewer) {
        currentTargets.remove(viewer);
    }

    // Get current targets map
    public Map<Player, Player> getCurrentTargets() {
        return currentTargets;
    }

    // Check if the debug flag is enabled in the config
    public boolean isDebugEnabled() {
        return config.getBoolean("features.debug", false); // Default to false if not found
    }
}
