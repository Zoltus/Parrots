package sh.zoltus.parrots.player;

import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
public class Holder {

    private static final Map<UUID, Holder> holders = new HashMap<>();

    private final Player p;
    private Pet leftShoulder, rightShoulder;

    private Holder(Player p) {
        this.p = p;
        holders.put(p.getUniqueId(), this);
    }

    public static Holder from(Player p) {
        return holders.getOrDefault(p.getUniqueId(), new Holder(p));
    }

    public void setLeftShoulder(Pet pet) {
        this.leftShoulder = pet;
        p.setShoulderEntityLeft(pet.getParrot());
    }

    public void setRightShoulder(Pet pet) {
        this.rightShoulder = pet;
        p.setShoulderEntityRight(pet.getParrot());
    }
}
