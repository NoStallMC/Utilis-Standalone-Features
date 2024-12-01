package main.java.org.matejko.plugin;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.Listener;
import main.java.org.matejko.plugin.Managers.*;
import main.java.org.matejko.plugin.Commands.ISeeCommand;
import main.java.org.matejko.plugin.FileCreator.*;
import main.java.org.matejko.plugin.Listeners.*;
import main.java.org.matejko.plugin.UtilisCore.*;
import java.util.logging.Logger;

public class Utilis extends JavaPlugin implements Listener {
    private Logger logger;
    private Config config;
    public SleepingManager sleepingManager;
    public NickManager nickManager;
    private UtilisGetters utilisGetters;
	private ISeeManager iSeeManager;

	@Override
    public void onEnable() {
        this.logger = Logger.getLogger("Utilis");
        getLogger().info("[Utilis] is starting up!");
        
        // Initialize ISee
        iSeeManager = new ISeeManager(this);
        ISeeInventoryListener iSeeInventoryListener = new ISeeInventoryListener(this, iSeeManager);
        ISeeArmorListener iSeeArmorListener = new ISeeArmorListener(this, iSeeManager);
        getServer().getPluginManager().registerEvents(new ISeeArmorRemover(iSeeManager), this);
        getServer().getPluginManager().registerEvents(iSeeInventoryListener, this);
        getCommand("isee").setExecutor(new ISeeCommand(iSeeManager, iSeeInventoryListener, iSeeArmorListener, this));
        
        // Initialize the plugin using UtilisInitializer
        UtilisInitializer initializer = new UtilisInitializer(this);
        initializer.initialize();
        getLogger().info("[Utilis] has been enabled!");
    }
    
    @Override
    public void onDisable() {
        getLogger().info("[Utilis] is shutting down...");
    }

    // Getter methods for accessing the plugin's components
    public Logger getLogger() {
        return logger;
    }
    public Config getConfig() {
        return config;
    }

    public void setConfig(Config config) {
        this.config = config;
    }
    public UtilisGetters getUtilisGetters() {
        return utilisGetters;
    }
    public void setUtilisGetters(UtilisGetters utilisGetters) {
        this.utilisGetters = utilisGetters;
    }
}
