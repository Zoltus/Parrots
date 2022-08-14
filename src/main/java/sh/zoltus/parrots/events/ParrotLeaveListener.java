package sh.zoltus.parrots.events;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleFlightEvent;

public class ParrotLeaveListener implements Listener {

    //todo
    @EventHandler
    public void flyEvent(PlayerToggleFlightEvent e) {
        Player p = e.getPlayer();
        if (!e.isFlying() && (p.getShoulderEntityLeft() != null || p.getShoulderEntityRight() != null)) {
           Bukkit.getPluginManager().callEvent(new ParrotLeaveEvent(p));
        }
    }
}
