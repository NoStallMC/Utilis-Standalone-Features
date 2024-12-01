package main.java.org.matejko.plugin.Listeners;

import java.util.Map;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import main.java.org.matejko.plugin.Utilis;
import main.java.org.matejko.plugin.Managers.ISeeManager;
import org.bukkit.util.config.Configuration;

public class ISeeInventoryListener implements Listener {
    private final ISeeManager iSeeManager;
    private final Utilis plugin;
    private boolean isSyncing = false;
    private boolean isSyncingInventory = false; // Prevent recursive syncing
    private boolean alive = true; // Controls whether the sync task continues running
    private ItemStack[] lastViewerInventory;
    private ItemStack[] lastTargetInventory;
    private long lastViewerSyncTime = 0L;
    private long lastTargetSyncTime = 0L;
    private int taskId = -1; // ID of the scheduled sync task
    private final Configuration config; // Direct reference to the plugin's configuration

    // Constructor to initialize plugin, iSeeManager, and config
    public ISeeInventoryListener(Utilis plugin, ISeeManager iSeeManager) {
        this.iSeeManager = iSeeManager;
        this.plugin = plugin;
        this.config = plugin.getConfiguration(); // Use the old config method
    }

    // Start inventory sync task
    public void startInventorySync(final Player viewer) {
        if (isSyncing) return; // Prevent starting multiple sync tasks
        alive = true; // Ensure the task is alive when starting

        taskId = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
        	@Override
        	public void run() {
        	    if (!alive) {
        	        if (isDebugEnabled()) { // Check if debug is enabled before printing
        	            System.out.println("[DEBUG] Stopping inventory sync for viewer " + viewer.getName());
        	        }
        	        plugin.getServer().getScheduler().cancelTask(taskId); // Cancel the task
        	        isSyncing = false;
        	        taskId = -1; // Reset the task ID
        	        return; // Exit the task if the flag is false
        	    }

        	    Player target = iSeeManager.getCurrentTarget(viewer);
        	    if (target != null && target.isOnline()) {
        	        syncInventory(viewer, target);
        	    } else {
        	        if (isDebugEnabled()) { // Check if debug is enabled before printing
        	            System.out.println("[DEBUG] Target is either null or offline. (inv)");
        	        }

        	        // If target is null or offline, recover the viewer's inventory and stop syncing
        	        iSeeManager.restoreInventoryAndArmor(viewer);
        	        iSeeManager.clearTarget(viewer);
        	        System.out.println("Viewer " + viewer.getName() + "'s inventory and armor restored due to target being null or offline.");

        	        // Cancel the sync task since the target is no longer available
        	        plugin.getServer().getScheduler().cancelTask(taskId); // Cancel the task
        	        isSyncing = false;
        	        taskId = -1; // Reset the task ID
        	    }
            }
        }, 0L, 1L); // Run every tick for fast response

        isSyncing = true;
    }

    // Restore the viewer's original inventory and armor
    public void onCancelSync(Player viewer) {
        alive = false; // Mark the task as stopped
        if (taskId != -1) {
            plugin.getServer().getScheduler().cancelTask(taskId); // Cancel the task
            taskId = -1; // Reset the task ID
        }
        iSeeManager.restoreInventoryAndArmor(viewer);
        iSeeManager.clearTarget(viewer);
        if (isDebugEnabled()) { // Check if debug is enabled before printing
            System.out.println("[DEBUG] Viewer " + viewer.getName() + "'s inventory and armor restored.");
        }
        isSyncing = false;
    }

    // Stop inventory sync task
    public void stopInventorySync(Player viewer) {
        alive = false; // Mark the task as stopped
        if (taskId != -1) {
            plugin.getServer().getScheduler().cancelTask(taskId); // Cancel the task
            taskId = -1; // Reset the task ID
        }
        isSyncing = false;
    }

    // Main sync method
    private void syncInventory(Player viewer, Player target) {
        if (isSyncingInventory) return; // Prevent recursive syncing
        isSyncingInventory = true;

        try {
            // Get the current inventories
            ItemStack[] viewerContents = viewer.getInventory().getContents();
            ItemStack[] targetContents = target.getInventory().getContents();
            long currentTime = System.currentTimeMillis();

            boolean viewerInventoryChanged = false;
            boolean targetInventoryChanged = false;

            // Detect if viewer's inventory has changed
            if (lastViewerInventory == null || !inventoryContentsMatch(viewerContents, lastViewerInventory)) {
                viewerInventoryChanged = true;
                lastViewerInventory = viewerContents.clone();
            }

            // Detect if target's inventory has changed
            if (lastTargetInventory == null || !inventoryContentsMatch(targetContents, lastTargetInventory)) {
                targetInventoryChanged = true;
                lastTargetInventory = targetContents.clone();
            }

            // Sync logic with suppression
            if (viewerInventoryChanged && currentTime - lastTargetSyncTime > 50) { // Prevent immediate loop
                suppressSyncCycle(target);
                target.getInventory().setContents(viewerContents.clone());
                lastViewerSyncTime = currentTime;
                if (isDebugEnabled()) { // Check if debug is enabled before printing
                    System.out.println("[DEBUG] Viewer edited inventory, syncing Target.");
                }
            }

            if (targetInventoryChanged && currentTime - lastViewerSyncTime > 50) { // Prevent immediate loop
                suppressSyncCycle(viewer);
                viewer.getInventory().setContents(targetContents.clone());
                lastTargetSyncTime = currentTime;
                if (isDebugEnabled()) { // Check if debug is enabled before printing
                    System.out.println("[DEBUG] Target edited inventory, syncing Viewer.");
                }
            }

        } finally {
            isSyncingInventory = false; // Reset flag after syncing
        }
    }

    // Utility method to compare inventories
    private boolean inventoryContentsMatch(ItemStack[] inventory1, ItemStack[] inventory2) {
        if (inventory1.length != inventory2.length) return false;

        for (int i = 0; i < inventory1.length; i++) {
            ItemStack item1 = inventory1[i];
            ItemStack item2 = inventory2[i];

            // Compare item type, amount, and durability
            if (item1 == null && item2 != null || item1 != null && !itemsAreEqual(item1, item2)) {
                return false;
            }
        }
        return true;
    }

    // Utility method to compare ItemStacks
    private boolean itemsAreEqual(ItemStack item1, ItemStack item2) {
        if (item1 == null || item2 == null) return item1 == item2;

        return item1.getType().equals(item2.getType()) &&
               item1.getAmount() == item2.getAmount() &&
               item1.getDurability() == item2.getDurability();
    }

    // Suppress sync for one cycle
    private void suppressSyncCycle(Player player) {
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            // No specific action; this just allows one tick delay to avoid immediate feedback loop
        }, 1L); // Slight delay to stabilize the sync
    }

    // Handle item drop
    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();

        // Sync inventories for both viewer and target after item drop
        syncAfterItemChange(player);
    }

    // Handle item pickup
    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();

        // Sync inventories for both viewer and target after item pickup
        syncAfterItemChange(player);
    }

    // Sync both inventories after item drop/pickup
    private void syncAfterItemChange(Player player) {
        // Sync inventories of the viewer and target
        for (Map.Entry<Player, Player> entry : iSeeManager.getCurrentTargets().entrySet()) {
            if (entry.getKey().equals(player) || entry.getValue().equals(player)) {
                final Player viewer = entry.getKey();
                final Player target = entry.getValue();
                if (viewer.isOnline() && target.isOnline()) {
                    // Delay sync to give time for item drop/pickup to register
                    plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {

                        // Force sync right after the first event to ensure proper inventory state
                        viewer.getInventory().setContents(viewer.getInventory().getContents()); // Force update for viewer
                        target.getInventory().setContents(target.getInventory().getContents()); // Force update for target
                        
                        syncInventory(viewer, target);
                        if (isDebugEnabled()) { // Check if debug is enabled before printing
                            System.out.println("[DEBUG] Item drop/pickup by " + player.getName() + ". Inventories synced.");
                        }
                    }, 10L); // 10 ticks delay to allow item drop/pickup processing
                    break;
                }
            }
        }
    }

    // Helper method to check if debug is enabled in the config
    private boolean isDebugEnabled() {
        return config.getBoolean("features.debug", false); // Default to false if the debug option is not set
    }
}
