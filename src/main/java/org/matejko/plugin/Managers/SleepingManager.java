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
    private final SleepingWorldConfig worldConfig;
    private final ConcurrentHashMap<World, ArrayList<Player>> sleepingPlayers;
    private final Messages messages;

    public SleepingManager(Utilis plugin) {
        this.worldConfig = new SleepingWorldConfig();
        this.sleepingPlayers = new ConcurrentHashMap<>();
        this.messages = new Messages(plugin);
    }

    public boolean isSleepingEnabled(World world) {
        return worldConfig.isSleepingEnabled(world);
    }

    public void toggleSleeping(World world) {
        boolean currentStatus = isSleepingEnabled(world);
        worldConfig.setSleepingStatus(world, !currentStatus);
        Bukkit.getLogger().info("Sleeping in world '" + world.getName() + "' has been " + (currentStatus ? "disabled" : "enabled"));
    }

    @EventHandler
    public void onPlayerBedEnter(PlayerBedEnterEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();
        Bukkit.getLogger().info(player.getName() + " entered bed in world: " + world.getName());
        if (!isSleepingEnabled(world)) {
            return;
        }
        sleepingPlayers.computeIfAbsent(world, k -> new ArrayList<>()).add(player);
        Bukkit.getLogger().info("Sleeping players in " + world.getName() + ": " + sleepingPlayers.get(world).size());
        checkForAtLeastOnePlayerSleeping(world, player);
    }
    @EventHandler
    public void onPlayerBedLeave(PlayerBedLeaveEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();
        if (!isSleepingEnabled(world)) {
            return;
        }
        ArrayList<Player> worldSleepingPlayers = sleepingPlayers.get(world);
        if (worldSleepingPlayers != null) {
            worldSleepingPlayers.remove(player);
        }
    }

    // Check if at least one player in the world is sleeping
    private void checkForAtLeastOnePlayerSleeping(World world, Player player) {
        if (!isSleepingEnabled(world)) {
            return;
        }
        List<Player> sleepingPlayersInWorld = sleepingPlayers.getOrDefault(world, new ArrayList<>());
        if (!sleepingPlayersInWorld.isEmpty()) {
            BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
            scheduler.scheduleSyncDelayedTask(Bukkit.getPluginManager().getPlugin("Utilis"), new Runnable() {
                @Override
                public void run() {
                    world.setTime(23500);
                    String messageTemplate = messages.getMessage("sleeping.night-skip");
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
                        p.sendMessage(message);
                    }

                    Bukkit.getLogger().info("At least one player is asleep. Skipping the night in '" + world.getName() + "'.");
                }
            }, 50L);  // 50 ticks (2.5 seconds)
        }
    }

    public void cleanupPlayer(Player player) {
        for (World world : sleepingPlayers.keySet()) {
            ArrayList<Player> worldSleepingPlayers = sleepingPlayers.get(world);
            if (worldSleepingPlayers != null) {
                worldSleepingPlayers.remove(player);
            }
        }
    }

    public void loadConfiguration() {
        worldConfig.loadConfig();
    }
}
