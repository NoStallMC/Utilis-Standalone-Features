package main.java.org.matejko.plugin;

import org.bukkit.plugin.Plugin;
import com.earth2me.essentials.Essentials;
import main.java.org.matejko.plugin.FileCreator.*;
import main.java.org.matejko.plugin.Managers.*;

import java.util.Set;
import java.util.logging.Logger;

public class UtilisGetters {
    private Logger logger;
    private Set<VanishUserManager> vanishedPlayers;
    private VanishedPlayersManager vanishedPlayersManager;
    private MOTDManager motdManager;
    private DynmapManager dynmapManager;
    private UtilisNotifier utilisNotifier;
    private Config config;
    private Essentials essentials;
    private Plugin dynmapPlugin;

    public UtilisGetters(Logger logger, Set<VanishUserManager> vanishedPlayers, VanishedPlayersManager vanishedPlayersManager,
                         MOTDManager motdManager, DynmapManager dynmapManager, UtilisNotifier utilisNotifier,
                         Config config, Essentials essentials, Plugin dynmapPlugin) {
        this.logger = logger;
        this.vanishedPlayers = vanishedPlayers;
        this.vanishedPlayersManager = vanishedPlayersManager;
        this.motdManager = motdManager;
        this.dynmapManager = dynmapManager;
        this.utilisNotifier = utilisNotifier;
        this.config = config;
        this.essentials = essentials;
        this.dynmapPlugin = dynmapPlugin;
    }

    // Getter methods for accessing the plugin's components
    public Logger getLogger() {
        return logger;
    }

    public Set<VanishUserManager> getVanishedPlayers() {
        return vanishedPlayers;
    }

    public VanishedPlayersManager getVanishedPlayersManager() {
        return vanishedPlayersManager;
    }

    public MOTDManager getMotdManager() {
        return motdManager;
    }

    public DynmapManager getDynmapManager() {
        return dynmapManager;
    }

    public UtilisNotifier getUtilisNotifier() {
        return utilisNotifier;
    }

    public Config getConfig() {
        return config;
    }

    public Essentials getEssentials() {
        return essentials;
    }

    public Plugin getDynmapPlugin() {
        return dynmapPlugin;
    }
}
