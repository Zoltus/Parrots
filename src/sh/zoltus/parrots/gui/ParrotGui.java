package sh.zoltus.parrots.gui;

import lombok.Data;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Parrot;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import sh.zoltus.parrots.player.Holder;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Data
public class ParrotGui {
    @Getter     //todo remove on quit
    private static final Map<UUID, ParrotGui> guis = new HashMap<>();

    private final Item[] items;
    private final Player player;
    private final Inventory inventory;

    public static ParrotGui get(Player p) {
        return Optional.ofNullable(guis.get(p.getUniqueId())).orElseGet(() -> new ParrotGui(p, "title", 3));
    }

    private ParrotGui(Player player, String title, int rows) {
        this.player = player;
        this.items = new Item[rows * 9];
        this.inventory = Bukkit.createInventory(player, rows * 9, title);
        guis.put(player.getUniqueId(), this);
        setupGui();
    }

    private void setupGui() {
        Item glass = new Item()
                .setStack(new ItemStack(Material.GRAY_STAINED_GLASS_PANE))
                .setClick(e -> e.getWhoClicked().closeInventory());

        Item close = new Item()
                .setStack(new ItemBuilder(Material.BARRIER)
                        .setName("close")
                        .setLore("lore")
                        .build())
                .setClick(e -> e.getWhoClicked().closeInventory());

        setItem(shoulderItem(), 8);
        setItem(parrotItem(Material.GRAY_CONCRETE, Parrot.Variant.GRAY), 11);
        setItem(parrotItem(Material.BLUE_CONCRETE, Parrot.Variant.BLUE), 12);
        setItem(parrotItem(Material.CYAN_CONCRETE, Parrot.Variant.CYAN), 13);
        setItem(parrotItem(Material.GREEN_CONCRETE, Parrot.Variant.GREEN), 14);
        setItem(parrotItem(Material.RED_CONCRETE, Parrot.Variant.RED), 15);
        setItem(close, 26);
        fill(glass);
    }


    private Item shoulderItem() {
        return new Item()
                .setStack(new ItemBuilder(Material.PLAYER_HEAD)
                        .setName("&a&lswitch shoulder")
                        .setLore("&7Side: <side")
                        .build());
    }
    //todo get selected shoulder from corner
    private Item parrotItem(Material material, Parrot.Variant color) {
        return new Item()
                .setStack(new ItemBuilder(material)
                        .setName("&a&lParrot 1")
                        .setLore("&7Click to set as parrot 1")
                        .build())
                .setClick(e -> {
                    Holder holder = Holder.of((Player) e.getWhoClicked());
                    holder.setParrot(Holder.Shoulder.RIGHT, color);
                    e.getWhoClicked().closeInventory();
                });
    }

    public void setItem(Item item, int... slots) {
        item.setGui(this);
        for (int slot : slots) {
            items[slot] = item;
            inventory.setItem(slot, item.getStack());
        }
    }

    public void fill(Item item) {
        for (int slot = 0; slot < inventory.getContents().length; slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack == null || stack.getType() == Material.AIR) {
                setItem(item, slot);
            }
        }
    }

    public Item getItem(int slot) {
        return items[slot];
    }

    public void show() {
        player.openInventory(inventory);
    }




















    /*

    public OneGui setContents(ItemStack[] stacks) {
        inv.setContents(stacks);
        return this;
    }


    public OneGui replaceItem(ItemStack replace, ItemStack replaceWith, boolean isExactlySame) {
        for (int slot = 0; slot < inv.getSize(); slot++) {
            ItemStack item = inv.getItem(slot);
            if (item == null) {
                continue;
            }
            if (isExactlySame && item == replace) {
                inv.setItem(slot, replaceWith);
            } else if (!isExactlySame && item.isSimilar(replace)) {
                inv.setItem(slot, replaceWith);
            }
        }
        return this;
    }


    public Integer getItemSlot(ItemStack stack, boolean isExactlySame) {
        for (int slot = 0; slot < inv.getSize(); slot++) {
            ItemStack item = inv.getItem(slot);
            if (item == null) {
                continue;
            }
            if (isExactlySame && item == stack) {
                return slot;
            } else if (!isExactlySame && item.isSimilar(stack)) {
                return slot;
            }
        }
        return null;
    }


    public OneGui fill(ItemStack item) {
        fill(item, null);
        return this;
    }

    public OneGui fill(ItemStack item, Consumer<InventoryClickEvent> run) {
        for (int slot = 0; slot < inv.getSize(); slot++) {
            if (inv.getItem(slot) == null) {
                inv.setItem(slot, item);
                if (run != null)
                    clicks.put(slot, run);
            }
        }
        return this;
    }

    public OneGui addItem(ItemStack item) {
        inv.addItem(item);
        return this;
    }

    public OneGui addItem(int amount, ItemStack item) {
        // inv.addItem(item.clone().setAmountZ(amount));
        return this;
    }

    public OneGui addItem(ItemStack item, Consumer<InventoryClickEvent> run) {
        addItem(1, item, run);
        return this;
    }


    public OneGui addItem(int amount, ItemStack item, Consumer<InventoryClickEvent> run) {
        for (int slot = 0; slot < inv.getSize(); slot++) {
            if (inv.getItem(slot) == null) {
                inv.setItem(slot, item);
                if (run != null)
                    clicks.put(slot, run);
                if (amount != 1) {
                    amount--;
                } else {
                    return this;
                }
            }
        }
        return this;
    }


    public OneGui setItem(int y, int x, ItemStack item) {
        setItem(item, y * 9 + x);
        return this;
    }

    public OneGui setItem(int slot, ItemStack item) {
        return setItem(item, slot);
    }


    public OneGui setItem(ItemStack item, int... slots) {
        //Todo
        // slots.forEach((index, o) -> inv.setItem(index, item));
        return this;
    }


    public OneGui setItem(int y, int x, ItemStack item, Consumer<InventoryClickEvent> run) {
        setItem(y * 9 + x, item, run);
        return this;
    }

    public OneGui setItem(ItemStack item, Consumer<InventoryClickEvent> run, int... slots) {
        //Todo
        // slots.forEach((slot, o) -> setItem(slot, item, run));
        return this;
    }


    public OneGui setItem(int slot, ItemStack item, Consumer<InventoryClickEvent> run) {
        inv.setItem(slot, item);
        clicks.put(slot, run);
        return this;
    }


    public InventoryView show(Player player) {
        return player.openInventory(inv);
    }

    */
}
