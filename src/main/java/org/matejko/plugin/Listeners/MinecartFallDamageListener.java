package main.java.org.matejko.plugin.Listeners;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import main.java.org.matejko.plugin.Utilis;

public class MinecartFallDamageListener {
    public static void handleFallDamage(Player player, Utilis plugin) {
        Entity vehicle = player.getVehicle();
        if (vehicle instanceof Minecart) {
            Minecart minecart = (Minecart) vehicle;
         //   plugin.getLogger().info("Player " + player.getName() + " is in a minecart. Minecart fall distance: " + minecart.getFallDistance());
            if (minecart.getFallDistance() > 0.0F) {
                resetFallDistance(minecart, plugin);
            }
        } else {
          //  plugin.getLogger().info("Player " + player.getName() + " is NOT in a minecart.");
        }
    }
    private static void resetFallDistance(Minecart minecart, Utilis plugin) {
        minecart.setFallDistance(0.0F);
        //plugin.getLogger().info("Fall damage cancelled for minecart with player.");
    }
}
