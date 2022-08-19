package sh.zoltus.parrots;

import jdk.jfr.Description;
import lombok.Getter;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Parrot;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.bukkit.event.world.WorldSaveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.annotation.plugin.ApiVersion;
import org.bukkit.plugin.java.annotation.plugin.LogPrefix;
import org.bukkit.plugin.java.annotation.plugin.Plugin;
import org.bukkit.plugin.java.annotation.plugin.Website;
import org.bukkit.plugin.java.annotation.plugin.author.Author;
import sh.zoltus.parrots.configuration.OneYml;
import sh.zoltus.parrots.gui.GuiHandler;
import sh.zoltus.parrots.gui.ParrotGui;
import sh.zoltus.parrots.player.Holder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Plugin(name = "Parrots", version = "2.0")
@Description("Parrots Plugin for 1.14.X-1.19.X")
@Author("Zoltus")
@Website("https://www.spigotmc.org/members/zoltus.306747/")
@LogPrefix("Parrots")
@ApiVersion(ApiVersion.Target.v1_14)
@Getter
public class Parrots extends JavaPlugin implements Listener {

    //todo server restart does remove parrots
    //todo kill parrots if fail to set on shoulders
    @Getter
    private static Parrots plugin;
    @Getter
    private static OneYml yml;

    @Override
    public void onLoad() {
        plugin = this;
    }

    @Override
    public void onEnable() {
        yml = new OneYml("config.yml", this.getDataFolder());
        // would only work 1.14.x +, 1.14 had dataholder bug also<
        // parrotKey = new NamespacedKey(this, "fakeparrot");
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new GuiHandler(), this);

        new Metrics(this, 13235);
        //todo move to the updatechecker as static method
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
        //todo remove fake parrots when disabled, now need to save stuff also to db or files
        Holder.getHolders().forEach((uuid, holder) -> {
            holder.removeFakeParrot(Holder.Shoulder.BOTH);
        });
    }

    @EventHandler
    public void move(PlayerQuitEvent e) {
        //remove parrot
    }

    @EventHandler
    public void move(WorldSaveEvent e) {
        Bukkit.getConsoleSender().sendMessage("saveeeeeeeeeeeeeeeeeee@@@@@@@@@@@@@@@@@@");
    }

    //TODO on vehicles it spams on everytick, would gluing parrot be better?
    @EventHandler
    public void move(CreatureSpawnEvent e) {
        if (e.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SHOULDER_ENTITY) {
            if (Holder.isFakeParrot(e.getEntity())) {
                e.setCancelled(true);
            }
        }
    }
        //todo saving wont save parrots to shoulder if player is on water
    //todo powdersnow refresh

    @EventHandler
    public void move(EntitiesLoadEvent e) {
        for (Entity entity : e.getEntities()) {
            if (entity instanceof Parrot) {
                Parrot parrot = (Parrot) entity;
                Bukkit.broadcastMessage("loaded" + parrot.getType().name());
            }
        }
    }
    @EventHandler
    public void move(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        //Entity entity = p.getWorld().getent
        Holder holder = Holder.of(p);
        holder.refreshShoulders();
        //todo load parrot
    }

    @EventHandler
    public void flyEvent(PlayerToggleFlightEvent e) {
        Player p = e.getPlayer();
        if (!e.isFlying()) {
            Holder holder = Holder.of(p);
            holder.refreshShoulders();
        }
    }

    @EventHandler
    public void onChatt(PlayerCommandPreprocessEvent e) {
        Player p = e.getPlayer();
        String msg = e.getMessage();
        List<String> argsList = new ArrayList<>(Arrays.asList(msg.split(" ")));
        String cmd = argsList.get(0);
        argsList.remove(0);
        Holder holder = Holder.of(p);
        if (cmd.startsWith("//")) {
            e.setCancelled(true);
            switch (cmd.toLowerCase()) {
                case "//ml":
                    holder.setParrot(Holder.Shoulder.LEFT, Parrot.Variant.BLUE);
                    p.sendMessage("mountedl");
                    break;
                case "//mr":
                    holder.setParrot(Holder.Shoulder.RIGHT, Parrot.Variant.RED);
                    p.sendMessage("mountedr");
                    break;
                case "//mb":
                    holder.setParrot(Holder.Shoulder.LEFT, Parrot.Variant.GRAY);
                    holder.setParrot(Holder.Shoulder.RIGHT, Parrot.Variant.CYAN);
                    p.sendMessage("mountedb");
                    break;
                case "//clearl":
                    holder.removeFakeParrot(Holder.Shoulder.LEFT);
                    p.sendMessage("removedL");
                    break;
                case "//clearr":
                    holder.removeFakeParrot(Holder.Shoulder.RIGHT);
                    p.sendMessage("removedr");
                    break;
                case "//gui":
                    ParrotGui parrotGui = ParrotGui.get(p);
                    parrotGui.show();
                    break;
                case "//clearb":
                    holder.removeFakeParrot(Holder.Shoulder.BOTH);
                    p.sendMessage("removedb");
                    p.sleep(p.getLocation(), true);
                    break;
                case "//dropparrots":
                    p.sendMessage("dropparrots");
                    Location loc = p.getLocation();
                    loc.add(0, 1, 0);
                    p.teleport(loc);
                    p.setFallDistance(0.501F);
                    loc.add(0, -1, 0);
                    p.teleport(loc);
                    break;
            }
        }
    }
}
