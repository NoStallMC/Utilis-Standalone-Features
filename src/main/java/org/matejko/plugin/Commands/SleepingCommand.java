package main.java.org.matejko.plugin.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import main.java.org.matejko.plugin.Utilis;
import main.java.org.matejko.plugin.Managers.SleepingManager;

public class SleepingCommand implements CommandExecutor {
    private final SleepingManager sleepingManager;
    public SleepingCommand(Utilis plugin) {
        this.sleepingManager = new SleepingManager(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player p = (sender instanceof Player) ? (Player) sender : null;
        if (p == null || command == null) return true;

        String commandName = command.getName();
        if (commandName == null) return true;

        if (commandName.equalsIgnoreCase("as")) {
            if (args.length == 0) {
                // Show the sleeping status for the current world
                if (sleepingManager.isSleepingEnabled(p.getWorld())) {
                    p.sendMessage("Sleeping is currently enabled in this world!");
                } else {
                    p.sendMessage("Sleeping is currently disabled in this world!");
                }
            } else if (args.length == 1) {
                if (args[0].equalsIgnoreCase("toggle") || args[0].equalsIgnoreCase("t")) {
                    // Toggle sleeping in the current world
                    sleepingManager.toggleSleeping(p.getWorld());
                    if (sleepingManager.isSleepingEnabled(p.getWorld())) {
                        p.getServer().broadcastMessage("Sleeping is now enabled in this world!");
                    } else {
                        p.getServer().broadcastMessage("Sleeping is now disabled in this world!");
                    }
                }
            } else {
                p.sendMessage("Invalid command syntax!");
            }
        }
        return true;
    }
}
