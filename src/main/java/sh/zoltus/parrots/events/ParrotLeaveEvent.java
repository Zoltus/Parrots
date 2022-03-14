package sh.zoltus.parrots.events;

import lombok.Getter;
import lombok.NonNull;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ParrotLeaveEvent extends Event{

    private static final HandlerList HANDLERS = new HandlerList();
    @Getter
    private final Player player;

    public ParrotLeaveEvent(Player player) {
        this.player = player;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public @NonNull HandlerList getHandlers() {
        return HANDLERS;
    }
}
