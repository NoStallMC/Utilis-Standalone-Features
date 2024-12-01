package main.java.org.matejko.plugin.Listeners;

import org.bukkit.entity.Player;
import main.java.org.matejko.plugin.Utilis;
import main.java.org.matejko.plugin.Managers.ISeeManager;
import org.bukkit.util.config.Configuration;

public class ISeeArmorListener {
    private final Utilis plugin;
    private final ISeeManager iSeeManager;
    private boolean isSyncing = false; // Prevent starting multiple sync tasks
    private boolean alive = true; // Controls whether the sync task continues running
    private int taskId = -1; // ID of the scheduled sync task
    private final Configuration config; // Direct reference to the plugin's old config

    // Constructor to initialize plugin, iSeeManager, and config
    public ISeeArmorListener(Utilis plugin, ISeeManager iSeeManager) {
        this.plugin = plugin;
        this.iSeeManager = iSeeManager;
        this.config = plugin.getConfiguration(); // Use the old config method
    }

    // Start one-way armor sync task
    public void startArmorSync(final Player viewer) {
        if (isSyncing) return; // Prevent starting multiple sync tasks
        alive = true; // Ensure the task is alive when starting

        taskId = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            @Override
            public void run() {
                // Check if viewer is valid before continuing
                if (viewer == null || !viewer.isOnline()) {
                    if (isDebugEnabled()) { // Only log if debug is enabled
                        System.out.println("[DEBUG] Viewer is invalid or offline. Stopping armor sync.");
                    }
                    stopArmorSync(viewer);
                    return;
                }

                if (!alive) {
                    if (isDebugEnabled()) { // Only log if debug is enabled
                        System.out.println("[DEBUG] Stopping armor sync for viewer " + viewer.getName());
                    }
                    plugin.getServer().getScheduler().cancelTask(taskId); // Cancel the task
                    isSyncing = false;
                    taskId = -1; // Reset the task ID
                    return; // Exit the task if the flag is false
                }

                Player target = iSeeManager.getCurrentTarget(viewer);
                if (target != null && target.isOnline()) {
                    syncArmorFromTargetToViewer(viewer, target);
                } else {
                    if (isDebugEnabled()) { // Only log if debug is enabled
                        System.out.println("[DEBUG] Target is either null or offline. Stopping sync.");
                    }
                    stopArmorSync(viewer); // Stop sync if the target is invalid
                }
            }
        }, 0L, 20L); // Sync armor every 20 ticks (1 second)

        isSyncing = true; // Mark syncing as in progress
    }

    // Stop armor sync task
    public void stopArmorSync(Player viewer) {
        alive = false; // Stop syncing if the task is manually interrupted
        if (taskId != -1) {
            plugin.getServer().getScheduler().cancelTask(taskId); // Cancel the task
            taskId = -1; // Reset the task ID
        }
        isSyncing = false;
    }

    // Sync armor from the target to the viewer
    private void syncArmorFromTargetToViewer(Player viewer, Player target) {
        if (viewer == null || target == null || !viewer.isOnline() || !target.isOnline()) {
            if (isDebugEnabled()) { // Only log if debug is enabled
                System.out.println("[DEBUG] Viewer or target is invalid or offline. Stopping sync.");
            }
            stopArmorSync(viewer); // Stop sync task if invalid
            return;
        }

        try {
            // Assign each piece of armor, with null and AIR checks
            if (target.getInventory().getHelmet() != null && target.getInventory().getHelmet().getType() != null && !target.getInventory().getHelmet().getType().equals(org.bukkit.Material.AIR)) {
                viewer.getInventory().setHelmet(target.getInventory().getHelmet());
            } else {
                viewer.getInventory().setHelmet(null);
            }

            if (target.getInventory().getChestplate() != null && target.getInventory().getChestplate().getType() != null && !target.getInventory().getChestplate().getType().equals(org.bukkit.Material.AIR)) {
                viewer.getInventory().setChestplate(target.getInventory().getChestplate());
            } else {
                viewer.getInventory().setChestplate(null);
            }

            if (target.getInventory().getLeggings() != null && target.getInventory().getLeggings().getType() != null && !target.getInventory().getLeggings().getType().equals(org.bukkit.Material.AIR)) {
                viewer.getInventory().setLeggings(target.getInventory().getLeggings());
            } else {
                viewer.getInventory().setLeggings(null);
            }

            if (target.getInventory().getBoots() != null && target.getInventory().getBoots().getType() != null && !target.getInventory().getBoots().getType().equals(org.bukkit.Material.AIR)) {
                viewer.getInventory().setBoots(target.getInventory().getBoots());
            } else {
                viewer.getInventory().setBoots(null);
            }

            if (isDebugEnabled()) { // Only log if debug is enabled
                System.out.println("[DEBUG] Armor synced from " + target.getName() + " to " + viewer.getName());
            }
        } catch (Exception e) {
            if (isDebugEnabled()) { // Only log if debug is enabled
                System.out.println("[ERROR] Exception occurred while syncing armor: " + e.getMessage());
                e.printStackTrace();
            }
            stopArmorSync(viewer); // Stop sync task on error
        }
    }

    // Helper method to check if debug is enabled in the config
    private boolean isDebugEnabled() {
        return config.getBoolean("features.debug", false); // Default to false if the debug option is not set
    }
}
