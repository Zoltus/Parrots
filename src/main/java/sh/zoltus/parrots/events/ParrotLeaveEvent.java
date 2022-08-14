package sh.zoltus.parrots.events;

import lombok.Getter;
import lombok.NonNull;
import org.bukkit.entity.Parrot;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
public class ParrotLeaveEvent extends Event{

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final Parrot left, right;

    public ParrotLeaveEvent(Player player) {
        this.player = player;
        //todo only parrots can be on shoulder sofar so casting shouldnt be problem
        this.left = (Parrot) player.getShoulderEntityLeft();
        this.right = (Parrot) player.getShoulderEntityRight();
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public @NonNull HandlerList getHandlers() {
        return HANDLERS;
    }
}
