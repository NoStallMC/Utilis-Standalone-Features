package main.java.org.matejko.plugin.Managers;

import main.java.org.matejko.plugin.Utilis;
import main.java.org.matejko.plugin.Listeners.MinecartFallDamageListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import java.util.HashSet;
import java.util.Set;
import main.java.org.matejko.plugin.FileCreator.Config;

public class MinecartFallDamageManager {
    private final Utilis plugin;
    private final Set<Player> playersInMinecarts = new HashSet<>();
    private int taskId = -1;
    private final Config config;

    public MinecartFallDamageManager(Utilis plugin, Config config) {
        this.plugin = plugin;
		this.config = config;
    }
    public void onPlayerEnterMinecart(Player player) {
        if (playersInMinecarts.add(player)) {
            if (isDebugEnabled()) {
            plugin.getLogger().info("[DEBUG] Player " + player.getName() + " entered a minecart.");}
            startTaskIfNeeded();
        }
    }
	public void onPlayerExitMinecart(Player player) {
        if (playersInMinecarts.remove(player)) {
            if (isDebugEnabled()) {
            plugin.getLogger().info("[DEBUG] Player " + player.getName() + " exited a minecart.");}
            stopTaskIfNeeded();
        }
    }
    private void startTaskIfNeeded() {
        if (taskId == -1) {
            if (isDebugEnabled()) {
            plugin.getLogger().info("[DEBUG] Starting MinecartFallDamageManager task...");}
            taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this::checkPlayers, 0L, 1L);
        }
    }
    private void stopTaskIfNeeded() {
        if (playersInMinecarts.isEmpty() && taskId != -1) {
            if (isDebugEnabled()) {
            plugin.getLogger().info("[DEBUG] Stopping MinecartFallDamageManager task...");}
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
        }
    }
    private void checkPlayers() {
        for (Player player : playersInMinecarts) {
            MinecartFallDamageListener.handleFallDamage(player, plugin);
        }
    }
    private boolean isDebugEnabled() {
        return config.isDebugEnabled();
    }
}
