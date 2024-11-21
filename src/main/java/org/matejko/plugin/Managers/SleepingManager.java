package main.java.org.matejko.plugin.Managers;

import main.java.org.matejko.plugin.Utilis;
import main.java.org.matejko.plugin.FileCreator.Messages;
import main.java.org.matejko.plugin.FileCreator.SleepingWorldConfig;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.scheduler.BukkitScheduler;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class SleepingManager implements Listener {

    private final SleepingWorldConfig worldConfig;  // Config handler for world-specific sleeping settings
    private final ConcurrentHashMap<World, ArrayList<Player>> sleepingPlayers;  // Thread-safe HashMap for sleeping players
    private final Messages messages;  // Add Messages instance to fetch messages

    // Constructor to initialize SleepingWorldConfig, sleepingPlayers map, and Messages instance
    public SleepingManager(Utilis plugin) {
        this.worldConfig = new SleepingWorldConfig();  // Initialize the world config handler
        this.sleepingPlayers = new ConcurrentHashMap<>();  // Initialize the map for sleeping players
        this.messages = new Messages(plugin);  // Initialize the Messages instance
    }

    // Method to check if sleeping is enabled for a specific world
    public boolean isSleepingEnabled(World world) {
        return worldConfig.isSleepingEnabled(world);  // Get the sleeping status for the world
    }

    // Method to toggle sleeping for the current world
    public void toggleSleeping(World world) {
        boolean currentStatus = isSleepingEnabled(world);
        worldConfig.setSleepingStatus(world, !currentStatus);  // Toggle the sleeping status
        Bukkit.getLogger().info("Sleeping in world '" + world.getName() + "' has been " + (currentStatus ? "disabled" : "enabled"));
    }

    // Event handler for when a player enters a bed
    @EventHandler
    public void onPlayerBedEnter(PlayerBedEnterEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();

        // Log when a player enters a bed
        Bukkit.getLogger().info(player.getName() + " entered bed in world: " + world.getName());

        // Only add the player to sleepingPlayers if sleeping is enabled in this world
        if (!isSleepingEnabled(world)) {
            return;  // If sleeping is disabled in the world, ignore the event
        }

        // Log the current number of sleeping players in this world
        sleepingPlayers.computeIfAbsent(world, k -> new ArrayList<>()).add(player);
        Bukkit.getLogger().info("Sleeping players in " + world.getName() + ": " + sleepingPlayers.get(world).size());

        // Check if at least one player is asleep and perform action (skip night) with delay
        checkForAtLeastOnePlayerSleeping(world, player);
    }

    // Event handler for when a player leaves a bed
    @EventHandler
    public void onPlayerBedLeave(PlayerBedLeaveEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();

        // If sleeping is disabled in the world, ignore the event
        if (!isSleepingEnabled(world)) {
            return;
        }
        
        // Remove the player from the sleepingPlayers map when they leave the bed
        ArrayList<Player> worldSleepingPlayers = sleepingPlayers.get(world);
        if (worldSleepingPlayers != null) {
            worldSleepingPlayers.remove(player);
        }
    }

    // Check if at least one player in the world is asleep and perform some action (e.g., skip night)
    private void checkForAtLeastOnePlayerSleeping(World world, Player player) {
        // If sleeping is disabled in the world, don't skip the night
        if (!isSleepingEnabled(world)) {
            return;
        }

        // Get the list of all players currently asleep in this world
        List<Player> sleepingPlayersInWorld = sleepingPlayers.getOrDefault(world, new ArrayList<>());

        // If at least one player is asleep, perform an action (e.g., skip night)
        if (!sleepingPlayersInWorld.isEmpty()) {
            // Schedule a delayed task to skip the night after a delay of 50 ticks (2.5 seconds)
            BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
            scheduler.scheduleSyncDelayedTask(Bukkit.getPluginManager().getPlugin("Utilis"), new Runnable() {
                @Override
                public void run() {
                    world.setTime(23500);  // Skip the night by setting the time to 0

                    // Fetch the "sleeping.night-skip" message from the Messages API
                    String messageTemplate = messages.getMessage("sleeping.night-skip");

                    // If the message is not found or is empty, use a fallback message
                    if (messageTemplate == null || messageTemplate.isEmpty()) {
                        messageTemplate = "%player% took a little nap. It is now morning!";
                    }

                    // Replace the %player% placeholder with the actual player's name
                    String message = messageTemplate.replace("%player%", player.getDisplayName());

                    // Apply color code translation
                    message = ColorUtil.translateColorCodes(message);

                    // Log the message that will be sent to players
                    Bukkit.getLogger().info("Sending morning message to players in " + world.getName());

                    // Loop through all players in the same world and send the message to them
                    for (Player p : world.getPlayers()) {
                        p.sendMessage(message);  // Send the message only to players in the same world
                    }

                    Bukkit.getLogger().info("At least one player is asleep. Skipping the night in '" + world.getName() + "'.");
                }
            }, 50L);  // 50L = 50 ticks (2.5 seconds)
        }
    }

    // Ensure proper cleanup when players disconnect or change worlds
    public void cleanupPlayer(Player player) {
        for (World world : sleepingPlayers.keySet()) {
            ArrayList<Player> worldSleepingPlayers = sleepingPlayers.get(world);
            if (worldSleepingPlayers != null) {
                worldSleepingPlayers.remove(player);
            }
        }
    }

    // Load the world-specific configuration for sleeping
    public void loadConfiguration() {
        worldConfig.loadConfig();  // Load the world-specific sleep configurations
    }
}
