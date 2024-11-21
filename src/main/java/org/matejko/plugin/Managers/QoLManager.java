package main.java.org.matejko.plugin.Managers;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class QoLManager implements Listener{
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        // Check if the block is a stair (ID: 53 or ID: 67 for cobblestone stairs)
        if (block.getTypeId() == 53 || block.getTypeId() == 67 || block.getTypeId() == 30) {
            block.getWorld().dropItem(block.getLocation().add(0.5, 0.5, 0.5), new ItemStack(block.getTypeId(), 1)); // Drop stair item at the center
            block.setTypeId(0); // Set the block to air to remove it
        }

        // Check if the block is glass (ID: 20), glowstone (ID: 89), ice (ID: 79), or stone (ID: 1)
        if (block.getTypeId() == 20 || block.getTypeId() == 89 || block.getTypeId() == 79 || block.getTypeId() == 1) {
            // Check if the player is using a gold pickaxe (ID: 285)
            if (player.getInventory().getItemInHand().getTypeId() == 285) {
                block.getWorld().dropItem(block.getLocation().add(0.5, 0.5, 0.5), new ItemStack(block.getTypeId(), 1)); // Drop block itself at the center
                block.setTypeId(0); // Set the block to air to remove it
            }
        }
            // Check if the block is grass (ID: 2)
            if (block.getTypeId() == 2) {
                // Check if the player is using a gold shovel (ID: 284)
                if (player.getInventory().getItemInHand().getTypeId() == 284) {
                    block.getWorld().dropItem(block.getLocation().add(0.5, 0.5, 0.5), new ItemStack(block.getTypeId(), 1)); // Drop block itself at the center
                    block.setTypeId(0); // Set the block to air to remove it
                }
            }
    }
}
