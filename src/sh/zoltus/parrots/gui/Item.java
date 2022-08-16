package sh.zoltus.parrots.gui;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

public class Item {

    @Getter @Setter
    private ParrotGui gui;
    @Getter private ItemStack stack;
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
