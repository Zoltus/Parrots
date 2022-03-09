package sh.zoltus.parrots.player;

import lombok.Getter;
import net.minecraft.world.entity.player.EntityHuman;
import org.bukkit.Bukkit;
import org.bukkit.entity.Parrot;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
public class Holder {

    private static final Map<UUID, Holder> holders = new HashMap<>();

    private final Player p;
    private Parrot leftShoulder, rightShoulder;

    protected Holder(Player p) {
        this.p = p;
        holders.put(p.getUniqueId(), this);
    }

    public static Holder of(Player p) {
        return holders.getOrDefault(p.getUniqueId(), new Holder(p));
    }

    public void refreshShoulders() {
        setLeftShoulder(leftShoulder);
        setRightShoulder(rightShoulder);
        Bukkit.broadcastMessage("aa: " + leftShoulder);
    }

    public void setLeftShoulder(Parrot pet) {
        this.leftShoulder = pet;
        p.setShoulderEntityLeft(pet);
    }

    public void setRightShoulder(Parrot pet) {
        this.rightShoulder = pet;
        p.setShoulderEntityRight(pet);
    }
}
