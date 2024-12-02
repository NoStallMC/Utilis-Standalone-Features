package main.java.org.matejko.plugin.Managers;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.HashMap;
import java.util.Map;

public class CooldownManager {

    private final Map<Player, Long> nicknameCooldowns = new HashMap<>();
    private final Map<Player, Long> colorCooldowns = new HashMap<>();
    private final Map<Player, Long> resetCooldowns = new HashMap<>();
    private final long cooldownTime;

    public CooldownManager(JavaPlugin plugin, long cooldownTimeInSeconds) {
        this.cooldownTime = cooldownTimeInSeconds * 1000;
    }

    // Check if a player is on cooldown for a specific type
    public boolean isOnNicknameCooldown(Player player) {
        return isOnCooldown(nicknameCooldowns, player);
    }

    public boolean isOnColorCooldown(Player player) {
        return isOnCooldown(colorCooldowns, player);
    }

    public boolean isOnResetCooldown(Player player) {
        return isOnCooldown(resetCooldowns, player);
    }

    private boolean isOnCooldown(Map<Player, Long> cooldownMap, Player player) {
        return cooldownMap.containsKey(player) && System.currentTimeMillis() - cooldownMap.get(player) < cooldownTime;
    }

    // Get remaining cooldown time for a player
    public long getRemainingNicknameCooldown(Player player) {
        return getRemainingCooldown(nicknameCooldowns, player);
    }

    public long getRemainingColorCooldown(Player player) {
        return getRemainingCooldown(colorCooldowns, player);
    }

    public long getRemainingResetCooldown(Player player) {
        return getRemainingCooldown(resetCooldowns, player);
    }

    private long getRemainingCooldown(Map<Player, Long> cooldownMap, Player player) {
        if (isOnCooldown(cooldownMap, player)) {
            return (cooldownTime - (System.currentTimeMillis() - cooldownMap.get(player))) / 1000;
        }
        return 0;
    }

    // Set the cooldown for a player
    public void setNicknameCooldown(Player player) {
        setCooldown(nicknameCooldowns, player);
    }

    public void setColorCooldown(Player player) {
        setCooldown(colorCooldowns, player);
    }

    public void setResetCooldown(Player player) {
        setCooldown(resetCooldowns, player);
    }

    private void setCooldown(Map<Player, Long> cooldownMap, Player player) {
        cooldownMap.put(player, System.currentTimeMillis());
    }

    // Remove a player's cooldown
    public void removeNicknameCooldown(Player player) {
        nicknameCooldowns.remove(player);
    }

    public void removeColorCooldown(Player player) {
        colorCooldowns.remove(player);
    }

    public void removeResetCooldown(Player player) {
        resetCooldowns.remove(player);
    }
}
