package sh.zoltus.parrots.player.commands;

import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.logging.Level;

public class CommandHandler {

    private final JavaPlugin plugin;

    public CommandHandler(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    APICommand[] cmds = {

    };


    public boolean registerCommand(JavaPlugin plugin, APICommand command) {
        try {
            SimplePluginManager spm = (SimplePluginManager) plugin.getServer().getPluginManager();
            Field field = SimplePluginManager.class.getDeclaredField("commandMap");
            field.setAccessible(true);
            SimpleCommandMap commandMap = (SimpleCommandMap) field.get(spm);
            field.setAccessible(false);
            return commandMap.register(plugin.getName(), command);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "There went something wrong with getting the CommandMap.");
            plugin.getLogger().log(Level.WARNING, "Message: " + e.getMessage());
            return false;
        }
    }
}
