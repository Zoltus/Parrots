package sh.zoltus.parrots.gui;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

@Data
public class Item {

    private ParrotGui gui;
    private ItemStack stack;
    private Consumer<InventoryClickEvent> click;

    public Item setStack(ItemStack stack) {
        this.stack = stack;
        return this;
    }

    public Item setClick(Consumer<InventoryClickEvent> click) {
        this.click = click;
        return this;
    }

    public Item click(InventoryClickEvent e) {
        if (click != null) {
            click.accept(e);
        }
        return this;
    }
}
