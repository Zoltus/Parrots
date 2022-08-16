package sh.zoltus.parrots.player;

import lombok.SneakyThrows;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Parrot;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import sh.zoltus.parrots.Parrots;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/*
1. open menu
2. click setparrot
3. check if shoulder has real or fake parrot
4. if has even 1 real parrot it drops both, because glue is for both shoulders
5. sets fake parrot on shoulder/shoulders.
6. locks empty shoulder if it still has parrot

removing parrot:
1. if shoulders are empty after removing it unlocks shoulders
2. if player has even 1 parrot it only sets the other shoulder null.
3. need to check if parrots are fakeparrots from datacontainer
 */
public record Holder(Player player) {

    private static final Map<UUID, Holder> holders = new HashMap<>();
    private static final NamespacedKey fakeParrotKey = Parrots.getParrotKey();

    public Holder(Player player) {
        this.player = player;
        holders.put(player.getUniqueId(), this);
    }

    public static Holder of(Player p) {
        return holders.getOrDefault(p.getUniqueId(), new Holder(p));
    }

    public void refreshShoulders() {
        Entity left = player.getShoulderEntityLeft();
        Entity right = player.getShoulderEntityRight();
        player.setShoulderEntityLeft(null);
        player.setShoulderEntityRight(null);
        player.setShoulderEntityLeft(left);
        player.setShoulderEntityRight(right);
        player.sendMessage("refreshed");
    }

    public void removeParrot(Shoulder shoulder) {
        //todo check if is fakeparrot
        if (hasFakeParrots()) {
            switch (shoulder) {
                case LEFT -> player.setShoulderEntityLeft(null);
                case RIGHT -> player.setShoulderEntityRight(null);
                case BOTH -> {
                    player.setShoulderEntityLeft(null);
                    player.setShoulderEntityRight(null);
                }
            }
        }
    }

    @SneakyThrows
    //This could be done with nms but ill save my brain
    //Basicly fakes falling so normal parrots get removed
    //todo improve, possibly nms removeparrot method.
    private void dropRealParrots() {
        Location loc = player.getLocation();
        loc.add(0, 1, 0);
        player.teleport(loc);
        player.setFallDistance(0.501F);
        loc.add(0, -1, 0);
        player.teleport(loc);
    }

    @SneakyThrows
    public void setParrot(Shoulder shoulder, Parrot.Variant color) {
        if (!player.isOnGround()) {
            player.sendMessage("parrot can only be set when on ground");
        } else {
            dropRealParrots();
            Parrot parrot = (Parrot) player.getWorld().spawnEntity(player.getLocation(), EntityType.PARROT);
            //todo remove static access
            parrot.setSilent(Parrots.getYml().getBoolean("Config.isSilent"));
            parrot.getPersistentDataContainer().set(fakeParrotKey, PersistentDataType.BYTE, (byte) 0);
            parrot.setVariant(color);
            parrot.setSitting(true);
            parrot.setOwner(player);
            parrot.setTamed(true);
            parrot.setAI(false);
            switch (shoulder) {
                case LEFT -> player.setShoulderEntityLeft(parrot);
                case RIGHT -> player.setShoulderEntityRight(parrot);
                case BOTH -> { //Has separate entitys so incase of a bug console wont spam duplicate uuid's
                    setParrot(Shoulder.LEFT, color);
                    setParrot(Shoulder.RIGHT, color);
                }
            }
        }
    }

    private boolean hasParrots() {
        return player.getShoulderEntityRight() != null || player.getShoulderEntityLeft() != null;
    }

    public boolean hasFakeParrots() {
        return hasParrots() && (!isRealParrot(player.getShoulderEntityLeft()) || !isRealParrot(player.getShoulderEntityRight()));
    }

    public void removeFakeParrots() {
        if (!isRealParrot(player.getShoulderEntityLeft())) {
            removeParrot(Shoulder.LEFT);
        }
        if (!isRealParrot(player.getShoulderEntityRight())) {
            removeParrot(Shoulder.RIGHT);
        }
    }

    public static boolean isRealParrot(Entity entity) {
        return entity instanceof Parrot parrot && !parrot.getPersistentDataContainer().has(fakeParrotKey, PersistentDataType.BYTE);
    }

    public enum Shoulder {
        LEFT, RIGHT, BOTH
    }
}

