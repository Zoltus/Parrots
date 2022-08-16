package sh.zoltus.parrots.player.commands;

import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import sh.zoltus.parrots.Parrots;

import java.lang.reflect.Field;
import java.util.logging.Level;

public class CommandHandler {

    private final Parrots plugin;

    public CommandHandler(Parrots plugin) {
        this.plugin = plugin;
    }

    public boolean registerCommand(JavaPlugin plugin, Command command) {
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
