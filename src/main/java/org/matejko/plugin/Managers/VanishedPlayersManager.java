package main.java.org.matejko.plugin.Managers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import main.java.org.matejko.plugin.Utilis;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public class VanishedPlayersManager {

    private final File filePath;
    private Logger logger;

    public VanishedPlayersManager(Utilis plugin) {
        this.logger = Logger.getLogger("Utilis");

        // Use File API for the file path
        this.filePath = new File(plugin.getDataFolder(), "VanishedPlayers.txt");
    }

    public void loadVanishedPlayers(Set<VanishUserManager> vanishedPlayers) {
        // Ensure the file exists; create it if necessary
        if (!filePath.exists()) {
            try {
                filePath.createNewFile(); // Create the file if it doesn't exist
                logger.info("VanishedPlayers.txt file created.");
            } catch (IOException e) {
                logger.warning("Failed to create VanishedPlayers.txt file: " + e.getMessage());
                return;
            }
        }

        try {
            List<String> lines = Files.readAllLines(filePath.toPath());
            for (String name : lines) {
                Player player = Bukkit.getPlayer(name);
                if (player != null && player.isOnline()) {
                    VanishUserManager vanishUser = new VanishUserManager(player, true);
                    vanishedPlayers.add(vanishUser);
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        if (p != player) {
                            p.hidePlayer(player);
                        }
                    }
                }
            }
            logger.info("Loaded vanished players: " + vanishedPlayers.size());
        } catch (IOException e) {
            logger.warning("Could not load vanished players: " + e.getMessage());
        }
    }

    public void saveVanishedPlayers(Set<VanishUserManager> vanishedPlayers) {
        try {
            List<String> playerNames = new ArrayList<>();
            for (VanishUserManager vanishUser : vanishedPlayers) {
                playerNames.add(vanishUser.getName());
            }
            Files.write(filePath.toPath(), playerNames);
            logger.info("Saved vanished players: " + vanishedPlayers.size());
        } catch (IOException e) {
            logger.warning("Could not save vanished players: " + e.getMessage());
        }
    }
}
