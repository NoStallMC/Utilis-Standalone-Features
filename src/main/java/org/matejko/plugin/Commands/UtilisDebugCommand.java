package main.java.org.matejko.plugin.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import main.java.org.matejko.plugin.Utilis;

public class UtilisDebugCommand implements CommandExecutor {

    private final Utilis plugin;

    public UtilisDebugCommand(Utilis plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check if the sender has the required permission
        if (!sender.hasPermission("utilis.debug")) {
            sender.sendMessage("§cYou do not have permission to run this command.");
            return false;  // Permission denied
        }

        String[] messages = new String[]{
            "[Utilis Debug Report]",
            plugin.getUtilisGetters().getConfig() == null || !plugin.getUtilisGetters().getConfig().isLoaded() ? "§cConfig is not loaded properly." : "§aConfig is loaded correctly.",
            plugin.getUtilisGetters().getEssentials() == null ? "§cEssentials plugin is not found!" : "§aEssentials plugin is loaded correctly.",
            plugin.sleepingManager == null ? "§cSleepingManager is not initialized." : "§aSleepingManager is initialized correctly.",
            plugin.nickManager == null ? "§cNickManager is not initialized." : "§aNickManager is initialized correctly.",
            plugin.getUtilisGetters().getMotdManager() == null ? "§cMOTDManager is not initialized." : "§aMOTDManager is initialized correctly.",
            plugin.getUtilisGetters().getDynmapPlugin() == null ? "§cDynmap plugin is not found." : "§aDynmap plugin is loaded correctly.",
            plugin.getUtilisGetters().getVanishedPlayersManager() == null ? "§cVanish system is not initialized." : "§aVanish system is working correctly."
        };

        // Send each line of the message separately
        for (String line : messages) {
            sender.sendMessage(line);
        }

        return true;
    }
}
