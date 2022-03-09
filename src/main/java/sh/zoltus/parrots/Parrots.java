package sh.zoltus.parrots;

import lombok.Getter;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;
import sh.zoltus.parrots.configuration.OneYml;

@Getter
public class Parrots extends JavaPlugin {

    @Getter
    private static Parrots plugin;
    private final OneYml yml = new OneYml("name", this.getDataFolder());

    @Override
    public void onEnable() {
        plugin = this;
        new Metrics(this, 13235);
        new UpdateChecker(this, 42035).getVersion(version -> {
            if (this.getDescription().getVersion().equals(version)) {
                getLogger().info("There is not a new update available.");
            } else {
                getLogger().info("There is a new update available.");
            }
        });
    }

    @Override
    public void onDisable() {
        //re
    }
}
