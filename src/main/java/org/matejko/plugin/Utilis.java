package main.java.org.matejko.plugin;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.Listener;
import main.java.org.matejko.plugin.Managers.*;
import main.java.org.matejko.plugin.FileCreator.*;
import main.java.org.matejko.plugin.UtilisCore.*;
import java.util.logging.Logger;

public class Utilis extends JavaPlugin implements Listener {
    private Logger logger;
    private Config config;
    public SleepingManager sleepingManager;
    public NickManager nickManager;
    private UtilisGetters utilisGetters;
	@SuppressWarnings("unused")
	@Override
    public void onEnable() {
        this.logger = Logger.getLogger("Utilis");
        getLogger().info("[Utilis] is starting up!");
        RecoverManager recoverManager = new RecoverManager();
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
