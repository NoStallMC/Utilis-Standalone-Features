package main.java.org.matejko.plugin.Listeners;

import main.java.org.matejko.plugin.Managers.MinecartFallDamageManager;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;

public class MinecartEventListener implements Listener {
    private final MinecartFallDamageManager manager;

    public MinecartEventListener(MinecartFallDamageManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onPlayerEnterMinecart(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof Minecart) {
            Player player = event.getPlayer();
            manager.onPlayerEnterMinecart(player);
        }
    }

    @EventHandler
    public void onPlayerExitMinecart(VehicleExitEvent event) {
        if (event.getExited() instanceof Player && event.getVehicle() instanceof Minecart) {
            Player player = (Player) event.getExited();
            manager.onPlayerExitMinecart(player);
        }
    }
}
