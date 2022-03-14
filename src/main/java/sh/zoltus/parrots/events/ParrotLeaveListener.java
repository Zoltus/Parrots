package sh.zoltus.parrots.events;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleFlightEvent;

public class ParrotLeaveListener implements Listener {

    @EventHandler
    public void move(PlayerToggleFlightEvent e) {
        Player p = e.getPlayer();
        if (!e.isFlying()) {
           Bukkit.getPluginManager().callEvent(new ParrotLeaveEvent(p));
        }
    }
}
