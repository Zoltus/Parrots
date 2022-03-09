package sh.zoltus.parrots.utils;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

public class OneGui implements Listener {
	
	private static final int defaultRows = 6;
	private static final List<OneGui> guis = new ArrayList<>();

	@EventHandler
	private static void onClick(InventoryClickEvent e) {
		OneGui gui = OneGui.getGui(e.getClickedInventory());
		if (gui != null)  {
			e.setCancelled(true);
			if (gui.clicks.get(e.getSlot()) != null)
				gui.clicks.get(e.getSlot()).accept(e);
		}
	}
	
	@EventHandler
	private static void onOpen(InventoryOpenEvent e) {
		OneGui gui = OneGui.getGui(e.getInventory());
		if (gui != null && gui.openEvent != null)
			gui.openEvent.accept(e);
	}
	
	@EventHandler
	private static void onClose(InventoryCloseEvent e) {
		OneGui gui = OneGui.getGui(e.getInventory());
		if (gui != null && gui.closeEvent != null)
			gui.closeEvent.accept(e);
	}

	public static OneGui getGui(Inventory inv) {
		for (OneGui gui : guis) {
			if (gui == inv)
				return gui;
		}
		return null;
	}
	
	
	/*
	 * Gui Stuff
	 */
	//todo pages
	
	private final HashMap<Integer, Consumer<InventoryClickEvent>> clicks = new HashMap<>();
	private Consumer<InventoryOpenEvent> openEvent;
	private Consumer<InventoryCloseEvent> closeEvent;
	
	public OneGui setOpenEvent(Consumer<InventoryOpenEvent> openEvent) {
		this.openEvent = openEvent;
		return this;
	}
	
	public OneGui setCloseEvent(Consumer<InventoryCloseEvent> closeEvent) {
		this.closeEvent = closeEvent;
		return this;
	}
	
	
	//On register creates 1 inventory just to keep code in same class
	public OneGui() {
		super(null, 9);
	}

	public OneGui(String title) {
		this(null, defaultRows*9, title);
		guis.add(this);
	}

	public OneGui(String title, int rows) {
		this(null, rows*9, title);
		guis.add(this);
	}

	public OneGui(String title, InventoryType type) {
		this(null, type, title);
	}

	public OneGui(InventoryHolder owner, int size, String title) {
		super(owner, getClosesInvSize(size), title);
		guis.add(this);
	}

	public OneGui(InventoryHolder owner, int size) {
		super(owner, getClosesInvSize(size));
		guis.add(this);
	}

	public OneGui(InventoryHolder owner, InventoryType type, String title) {
		super(owner, type, title);
		guis.add(this);
	}

	public OneGui(InventoryHolder owner, InventoryType type) {
		super(owner, type);
		guis.add(this);
	}
	
	//Gets closes inv size so u cant get error from wrong inv size
	private static int getClosesInvSize(int size) {
		if (size % 9 != 0)
			size = (size / 9) * 9;
		if (size > 54)
			size = 54;
		if (size < 9)
			size = 9;
		return size;
	}
	
	public OneGui replaceItem(ItemStack replace, ItemStack replaceWith) {
		for (int slot = 0 ; slot < getSize() ; slot++) {
			if (getItem(slot) == replace) {
				setItem(slot, replaceWith);
				break;
			}
		}
		return this;
	}
	
	public OneGui clearSlots(int... slots) {
		for (int slot : slots)
			clearSlots(slot);
		return this;
	}
	
	//public OneGui fillBorders(ItemStack item, boolean overrideSlots) {
	//	return this;
	//}

	public OneGui fillRow(int row, ItemStack stack) {
		return fillRow(row, stack, null);
	}
	
	public OneGui fillRow(int row, ItemStack stack, Consumer<InventoryClickEvent> run) {
        if((row+1)*9 > getContents().length)
            return this;
          
        int slotStart = 9*row;
        int slotEnd = slotStart + 9;
        for (int slot = slotStart ; slot < slotEnd ; slot++) {
      	  setItem(slot, stack, run);
        }
        return this;
      }

	public OneGui fill(Material m) {
		return fill(new ItemStack(m), null);
	}
	
	public OneGui fill(ItemStack item) {
		return fill(item, null);
	}
	
	public OneGui fill(ItemStack item, Consumer<InventoryClickEvent> run) {
		for (int slot = 0 ; slot < getSize() ; slot++) {
			if (getItem(slot) == null) {
				setItem(slot, item);
				if (run != null)
					clicks.put(slot, run);
			}
		}
		return this;
	}

	public OneGui addItem(int amount, ItemStack item) {
		return addItem(amount, item, null);
	}
	
	public OneGui addItem(ItemStack item) {
		return addItem(1, item, null);
	}
	
	public OneGui addItem(ItemStack item, Consumer<InventoryClickEvent> run) {
		return addItem(1, item, run);
	}
	
	public OneGui addItem(int amount, ItemStack item, Consumer<InventoryClickEvent> run) {
		for (int slot = 0 ; slot < getSize() ; slot++) {
			if (getItem(slot) == null) {
				setItem(slot, item);
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
		return setItem(item, y*9 + x);
	}

	public OneGui setItem(ItemStack item, int... slots) {
		for (int slot : slots) {
			setItem(slot, item);
		}
		return this;
	}

	public OneGui setItem(int y, int x, ItemStack item,  Consumer<InventoryClickEvent> run) {
		return setItem(y*9 + x, item, run);
	}
	
	public OneGui setItem(ItemStack item, Consumer<InventoryClickEvent> run, int... slots) {
		for (int slot : slots) {
			setItem(slot, item, run);
		}
		return this;
	}
	
	public OneGui setItem(int slot, ItemStack item,  Consumer<InventoryClickEvent> run) {
		setItem(slot, item);
		clicks.put(slot, run);
		return this;
	}
}
