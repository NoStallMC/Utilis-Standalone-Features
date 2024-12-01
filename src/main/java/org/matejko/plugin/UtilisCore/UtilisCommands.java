package main.java.org.matejko.plugin.UtilisCore;

import main.java.org.matejko.plugin.Utilis;
import main.java.org.matejko.plugin.Commands.*;
import main.java.org.matejko.plugin.FileCreator.*;
import main.java.org.matejko.plugin.Managers.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class UtilisCommands {
    private final Utilis plugin;
    private final Config config;
    private final NickManager nickManager;
    private final CooldownManager cooldownManager;
    private final Messages messages;

    public UtilisCommands(Utilis plugin, Config config, NickManager nickManager, CooldownManager cooldownManager, Messages messages) {
        this.plugin = plugin;
        this.config = config;
        this.nickManager = nickManager;
        this.cooldownManager = cooldownManager;
        this.messages = messages;
    }

    public void registerCommands() {
        // Register commands with permission checks
        if (config.isNickEnabled()) {
            registerCommandWithPermission("nickname", "utilis.nickname", new NicknameCommand(nickManager, cooldownManager, messages));
        }
        if (config.isRenameEnabled()) {
            registerCommandWithPermission("rename", "utilis.rename", new RenameCommand(nickManager));
        }
        if (config.isColorEnabled()) {
            registerCommandWithPermission("color", "utilis.color", new ColorCommand(nickManager, cooldownManager, messages));
        }
        if (config.isNickResetEnabled()) {
            registerCommandWithPermission("nickreset", "utilis.nickreset", new NickResetCommand(nickManager, cooldownManager));
        }
        if (config.isRealNameEnabled()) {
            registerCommandWithPermission("realname", "utilis.realname", new RealNameCommand(nickManager));
        }
        if (config.isListEnabled()) {
            registerCommandWithPermission("list", "utilis.list", new ListCommand(plugin));
        }

        // Utilisdebug command with specific permission
        registerCommandWithPermission("utilisdebug", "utilis.debug", new UtilisDebugCommand(plugin));

        // Sudo command
        registerCommandWithPermission("sudo", "utilis.sudo", new SudoManager());

        // Suck command
        registerCommandWithPermission("suck", "utilis.suck", new SuckCommand());

        // Vanish command
        if (config.isVanishEnabled()) {
            VanishCommand vanishCommand = new VanishCommand(plugin);
            registerCommandWithPermission("vanish", "utilis.vanish", vanishCommand);
            registerCommandWithPermission("v", "utilis.vanish", vanishCommand); // Alias
        }
    }

    private void registerCommandWithPermission(String commandName, String permission, CommandExecutor executor) {
        plugin.getCommand(commandName).setExecutor(new CommandExecutor() {
            @Override
            public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    if (!player.hasPermission(permission)) {
                        player.sendMessage("§cYou do not have permission to use this command.");
                        return true;
                    }
                }
                return executor.onCommand(sender, command, label, args);
            }
        });
    }
}
