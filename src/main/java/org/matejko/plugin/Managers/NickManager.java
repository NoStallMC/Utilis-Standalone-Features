package main.java.org.matejko.plugin.Managers;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.util.config.Configuration;
import org.bukkit.event.EventHandler;
import main.java.org.matejko.plugin.Utilis;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class NickManager implements Listener {
    private static final Logger logger = Logger.getLogger("NickManager");
    private final Utilis plugin;  // Plugin instance to access the server
    private final File playerDataFile;
    private final Configuration config;
    private final Map<String, String[]> playerData;

    public NickManager(Utilis plugin) {
        this.plugin = plugin;  // Initialize plugin
        this.playerData = new HashMap<>();

        // Ensure the data folder exists
        File nicksFolder = plugin.getDataFolder();
        if (!nicksFolder.exists()) {
            nicksFolder.mkdirs();
        }

        // Initialize player data file
        this.playerDataFile = new File(nicksFolder, "playerData.yml");

        // Check if the file exists, if not create it
        if (!playerDataFile.exists()) {
            try {
                playerDataFile.createNewFile();
                logger.warning("[Utilis] playerData.yml not found! Creating a new one.");
            } catch (Exception e) {
                logger.severe("[Utilis] Could not create playerData.yml file.");
                e.printStackTrace();
            }
        }

        this.config = new Configuration(playerDataFile);
        loadPlayerData();  // Load player data when the plugin starts
    }

    // Load player data (nickname and color) from the YAML file
    private void loadPlayerData() {
        if (playerDataFile.exists()) {
            config.load();
            playerData.clear(); // Clear current data before reloading

            for (String playerName : config.getKeys()) {
                String nickname = config.getString(playerName + ".nickname");
                String color = config.getString(playerName + ".color");
                playerData.put(playerName, new String[]{nickname, color});
            }
        } else {
            logger.warning("[Utilis] Player data file does not exist.");
        }
    }

    // Save player data to the YAML file
    private void savePlayerData() {
        config.load();
        for (Map.Entry<String, String[]> entry : playerData.entrySet()) {
            String playerName = entry.getKey();
            String[] data = entry.getValue();
            config.setProperty(playerName + ".nickname", data[0]);
            config.setProperty(playerName + ".color", data[1]);
            logger.info("[Utilis] Saving data for " + playerName + ": " + data[0] + " " + data[1]);  // Log what is being saved
        }
        config.save();
    }

    // Validate if the color is a valid ChatColor
    public boolean isValidColor(String color) {
        try {
            ChatColor.valueOf(color.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    // Check if a nickname is already in use
    public boolean isNicknameUsed(String nickname) {
        for (String[] data : playerData.values()) {
            if (data[0].equals(nickname)) {
                return true;
            }
        }
        return false;
    }

    // Method to check if the nickname is valid (not already in use)
    public boolean isValidNickname(String nickname) {
        return !isNicknameUsed("~" + nickname); // Check if the nickname is not already in use
    }

    // Set a player's nickname (separate from color)
    public void setNickname(Player player, String nickname) {
        String playerName = player.getName();

        // Check if the nickname is already in use
        if (!isValidNickname(nickname)) {
            player.sendMessage(ChatColor.RED + "The nickname " + "~" + nickname + " is already in use.");
            return;
        }

        // Get the current color, if exists
        String currentColor = getPlayerColor(player);
        String nickWithTilde = "~" + nickname;

        // Apply the translated color codes to the nickname
        String translatedNickname = ColorUtil.translateColorCodes(nickWithTilde);

        // Set the nickname and color in player data
        playerData.put(playerName, new String[]{translatedNickname, currentColor});  // Keep current color
        player.setDisplayName(ChatColor.valueOf(currentColor.toUpperCase()) + translatedNickname + ChatColor.WHITE);  // Apply current color to display name

        savePlayerData();  // Save changes to file immediately
    }

    // Set a player's color while keeping the current nickname or player name
    public void setNicknameColor(Player player, String color) {
        String playerName = player.getName();
        String[] data = playerData.get(playerName);
        String currentNickname = (data != null) ? data[0] : playerName;  // Fallback to player name if no nickname

        // Update the display name with the new color
        try {
            player.setDisplayName(ChatColor.valueOf(color.toUpperCase()) + currentNickname + ChatColor.WHITE);
        } catch (IllegalArgumentException e) {
            logger.warning("Invalid color: " + color);
            player.sendMessage(ChatColor.RED + "Invalid color specified.");
            return;
        }

        // Update the stored data with the new color (and the existing nickname or player name)
        playerData.put(playerName, new String[]{currentNickname, color});
        savePlayerData(); // Save changes to file immediately
    }

    // Reset the player's nickname to their original name and reset color
    public void resetNickname(Player player) {
        String playerName = player.getName();
        player.setDisplayName(playerName); // Reset display name to original
        playerData.put(playerName, new String[]{playerName, "WHITE"}); // Reset to original name and default color
        savePlayerData(); // Save changes to file immediately
    }

    // Get the nickname for a player
    public String getPlayerNickname(Player player) {
        String[] data = playerData.get(player.getName());
        return (data != null && data[0] != null) ? data[0] : player.getName();
    }

    // Get the color of the player's nickname
    public String getPlayerColor(Player player) {
        String[] data = playerData.get(player.getName());
        return (data != null && data[1] != null) ? data[1] : "WHITE";  // Default to WHITE if not set
    }

    // Event handler when a player joins the server
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getName();

        // Reload player data when the player joins
        loadPlayerData();  // Reload the player data to ensure the newest changes are applied

        // Load the player's nickname and color data
        String[] data = playerData.get(playerName);

        // Clear the default join message to avoid conflict
        event.setJoinMessage(null);

        if (data != null && data[0] != null && data[1] != null) {
            String nickname = data[0];  // Get the nickname
            String color = data[1];     // Get the color

            try {
                // Apply the nickname with the stored color immediately when the player joins
                ChatColor chatColor = ChatColor.valueOf(color.toUpperCase());
                player.setDisplayName(chatColor + nickname + ChatColor.WHITE);  // Apply color and default white after
            } catch (IllegalArgumentException e) {
                // Fallback to default color (WHITE) in case of an invalid color
                player.setDisplayName(ChatColor.WHITE + nickname);
                logger.warning("Invalid color for player " + playerName + ". Defaulting to white.");
            }
        } else {
            player.setDisplayName(playerName);  // Default to player name if no nickname or color data exists
        }
    }

    // Getter for playerData map (to be used externally)
    public Map<String, String[]> getPlayerData() {
        return playerData;
    }

    // Accessor method for plugin's server (added for command handling)
    public org.bukkit.Server getServer() {
        return plugin.getServer();
    }
}
