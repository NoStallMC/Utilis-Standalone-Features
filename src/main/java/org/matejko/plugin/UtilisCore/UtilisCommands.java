package main.java.org.matejko.plugin.UtilisCore;

import main.java.org.matejko.plugin.Utilis;
import main.java.org.matejko.plugin.Commands.*;
import main.java.org.matejko.plugin.FileCreator.*;
import main.java.org.matejko.plugin.Managers.*;

public class UtilisCommands {
    private final Utilis plugin;
    private final Config config;
    private final NickManager nickManager;
    private final CooldownManager cooldownManager;
    private final Messages messages;
    // Constructor now uses Utilis instead of JavaPlugin
    public UtilisCommands(Utilis plugin, Config config, NickManager nickManager, CooldownManager cooldownManager, Messages messages) {
        this.plugin = plugin;
        this.config = config;
        this.nickManager = nickManager;
        this.cooldownManager = cooldownManager;
        this.messages = messages;
    }
    public void registerCommands() {
        // Register Nickname-related commands based on config setting
        if (config.isNickEnabled()) {
            plugin.getCommand("nickname").setExecutor(new NicknameCommand(nickManager, cooldownManager, messages));
        }
        if (config.isRenameEnabled()) {
            plugin.getCommand("rename").setExecutor(new RenameCommand(nickManager));
        }
        if (config.isColorEnabled()) {
            plugin.getCommand("color").setExecutor(new ColorCommand(nickManager, cooldownManager, messages));
        }
        if (config.isNickResetEnabled()) {
            plugin.getCommand("nickreset").setExecutor(new NickResetCommand(nickManager, cooldownManager));
        }
        if (config.isRealNameEnabled()) {
            plugin.getCommand("realname").setExecutor(new RealNameCommand(nickManager));
        }
        // Register /list command
        if (config.isListEnabled()) {
            ListCommand listCommand = new ListCommand(plugin);
            plugin.getCommand("list").setExecutor(listCommand);
        }
        // Register /utilisdebug command
        plugin.getCommand("utilisdebug").setExecutor(new UtilisDebugCommand(plugin));
        
        // Register sudo command
        SudoManager sudoCommand = new SudoManager();
        plugin.getCommand("sudo").setExecutor(sudoCommand);

        // Register Suck command
        SuckCommand suckCommand = new SuckCommand();
        plugin.getCommand("suck").setExecutor(suckCommand);

        // Vanish-related command registration
        if (config.isVanishEnabled()) {
            VanishCommand vanishCommand = new VanishCommand(plugin);
            plugin.getCommand("vanish").setExecutor(vanishCommand);
            plugin.getCommand("v").setExecutor(vanishCommand);
        }
    }
}
