package sh.zoltus.parrots;

import jdk.jfr.Description;
import lombok.Getter;
import org.bstats.bukkit.Metrics;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Parrot;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.annotation.plugin.ApiVersion;
import org.bukkit.plugin.java.annotation.plugin.LogPrefix;
import org.bukkit.plugin.java.annotation.plugin.Plugin;
import org.bukkit.plugin.java.annotation.plugin.Website;
import org.bukkit.plugin.java.annotation.plugin.author.Author;
import sh.zoltus.parrots.configuration.OneYml;
import sh.zoltus.parrots.player.Holder;

@Plugin(name = "Parrots", version = "2.0")
@Description("Parrots Plugin for 1.18.X")
@Author("Zoltus")
@Website("https://www.spigotmc.org/members/zoltus.306747/")
@LogPrefix("Parrots")
@ApiVersion(ApiVersion.Target.v1_18)
@Getter
public class Parrots extends JavaPlugin implements Listener {

    @Getter
    private static Parrots plugin;
    private final OneYml yml = new OneYml("config.yml", this.getDataFolder());

    @Override
    public void onEnable() {
        plugin = this;
        getServer().getPluginManager().registerEvents(this, this);
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

    @EventHandler
    public void parrotTest(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        Holder holder = Holder.of(p);
        Parrot parrot = parr(p, Parrot.Variant.BLUE, holder);
        holder.refreshShoulders();
    }

    private Parrot parr(Player p, Parrot.Variant color, Holder holder) {
        Parrot parrot = (Parrot) p.getWorld().spawnEntity(p.getLocation(), EntityType.PARROT);
        holder.setLeftShoulder(parrot);
        parrot.remove();
        parrot.damage(parrot.getHealth());
        parrot.setHealth(0);
        parrot.setSilent(plugin.getYml().getBoolean("Config.isSilent"));
        parrot.setCustomNameVisible(false);
        parrot.setVariant(color);
        parrot.setSitting(true);
        parrot.setOwner(p);
        parrot.setTamed(true);
        parrot.setAI(true);
        return parrot;
    }
}
