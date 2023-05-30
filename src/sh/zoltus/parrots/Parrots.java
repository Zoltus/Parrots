package sh.zoltus.parrots;

import lombok.Getter;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Parrot;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.annotation.plugin.*;
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
        Database.init(this);
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
        //todo to variable?
        //todo on disable remove all parrots
        Holder.getHolders().forEach((uuid, holder) -> holder.removeFakeParrot(Holder.Shoulder.BOTH));
       Database.database().saveUsers();
    }

    //Loads on login from db
    @EventHandler
    public void preLoginEvent(AsyncPlayerPreLoginEvent e) {
        //todo on onecore
        if (e.getLoginResult() == AsyncPlayerPreLoginEvent.Result.ALLOWED) {
            Database.database().loadPlayer(Bukkit.getOfflinePlayer(e.getUniqueId()));
        }
    }

    @EventHandler
    public void joinEvent(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        Holder holder = Holder.of(p);
        //Sets player
        Bukkit.getConsoleSender().sendMessage("§aleft" + holder.getColorLeft());

        Bukkit.getScheduler().runTaskLater(this, () -> {
            holder.setParrot(Holder.Shoulder.LEFT, holder.getColorLeft());
            holder.setParrot(Holder.Shoulder.RIGHT, holder.getColorRight());
            //todo wait tick for refresh?=
            holder.refreshShoulders();
        }, 20);
    }

    @EventHandler
    public void quitEvent(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        Holder holder = Holder.of(p);
        holder.removeFakeParrot(Holder.Shoulder.BOTH);
    }

    //event not called bug
    @EventHandler
    public void parrotShoulderEvent(CreatureSpawnEvent e) {
        if (e.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SHOULDER_ENTITY) {
            if (Holder.isFakeParrot(e.getEntity())) {
                Bukkit.broadcastMessage("fakespawnevent");
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void parrotInvisibleEvent(PlayerToggleFlightEvent e) {
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
                case "//rf":
                        p.setShoulderEntityLeft(null);
                        p.setShoulderEntityLeft(p.getShoulderEntityLeft());
                        p.sendMessage("refreshed1");

                        p.setShoulderEntityRight(null);
                        p.setShoulderEntityRight(p.getShoulderEntityRight());
                        p.sendMessage("refreshed2");

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
