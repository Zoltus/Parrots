package sh.zoltus.parrots.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

public class GuiHandler implements Listener {
    /**
     * Handles Item click event
     *
     * @param e event
     */
    @EventHandler
    public void click(InventoryClickEvent e) {
        Inventory clickedInv = e.getClickedInventory();
        Bukkit.broadcastMessage("111");
        if (e.getWhoClicked() instanceof Player p) {
            Bukkit.broadcastMessage("asd22");
            ParrotGui inv = ParrotGui.get(p);
            if (inv != null && inv.getInventory().equals(clickedInv)) {
                Bukkit.broadcastMessage("asd333");
                e.setCancelled(true);
                int slot = e.getSlot();
                Item item = inv.getItem(slot);
                item.click(e);
            }
        }
    }
}
