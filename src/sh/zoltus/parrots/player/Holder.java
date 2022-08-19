package sh.zoltus.parrots.player;

import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Parrot;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import sh.zoltus.parrots.Parrots;

import java.lang.reflect.Method;
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
@Getter
public class Holder {

    @Getter
    private static final Map<UUID, Holder> holders = new HashMap<>();

    private final Player player;
    private Parrot.Variant parrotLeft, parrotRight;

    public Holder(Player player) {
        this.player = player;
        holders.put(player.getUniqueId(), this);
    }

    public static Holder of(Player p) {
        return holders.getOrDefault(p.getUniqueId(), new Holder(p));
    }

    //Prevents clientside bug where parrots become invisible if players toggles fly and back
    public void refreshShoulders() {
        Entity left = player.getShoulderEntityLeft();
        Entity right = player.getShoulderEntityRight();
        if (isFakeParrot(left)) {
            player.setShoulderEntityLeft(null);
            player.setShoulderEntityLeft(left);
        }
        if (isFakeParrot(right)) {
            player.setShoulderEntityRight(null);
            player.setShoulderEntityRight(right);
        }
        player.sendMessage("refreshed");
    }

    public boolean removeFakeParrot(Shoulder shoulder) {
        if (shoulder == Shoulder.BOTH) {//If either shoulder had fake parrot returns true
            boolean removeLeft = removeFakeParrot(Shoulder.LEFT);
            boolean removeRight = removeFakeParrot(Shoulder.RIGHT);
            return removeLeft || removeRight;
        } else if (shoulder == Shoulder.LEFT) {
            if (isFakeParrot(player.getShoulderEntityLeft())) {
                player.setShoulderEntityLeft(null);
                return true;
            }
        } else if (shoulder == Shoulder.RIGHT) {
            if (isFakeParrot(player.getShoulderEntityRight())) {
                player.setShoulderEntityRight(null);
                return true;
            }
        }
        return false;
    }

    @SneakyThrows
    public void setParrot(Shoulder shoulder, Parrot.Variant color) {
        if (!player.isOnGround()) {
            player.sendMessage("parrot can only be set when on ground");
        } else if (shoulder == Shoulder.BOTH) { //If both calls method for both shoulders
            setParrot(Shoulder.LEFT, color);
            setParrot(Shoulder.RIGHT, color);
        } else {
            //Detatches current parrots
            releaseRealParrot(shoulder);
            //Creates new custom parrot for shoulder
            Parrot parrot = (Parrot) player.getWorld().spawnEntity(player.getLocation(), EntityType.PARROT);
            parrot.setVariant(color);
            parrot.setAI(false);
            parrot.setTamed(true);
            parrot.setOwner(player);
            parrot.setSitting(true);
            //todo remove static access
            parrot.setSilent(Parrots.getYml().getBoolean("Config.isSilent"));
            parrot.setCustomName("§cThis is a bug please notify admins.");
            parrot.setCustomNameVisible(false);
            if (shoulder == Shoulder.LEFT) {
                player.setShoulderEntityLeft(parrot);
            } else if (shoulder == Shoulder.RIGHT) {
                player.setShoulderEntityRight(parrot);
            }
        }
    }

    // private static String version =
//
    //version 14
    private static final String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
    private static final int subversion = Integer.parseInt(version.split("_")[1]);

    @SneakyThrows //todo catch throwable
    private void releaseRealParrot(Shoulder shoulder) {
        Entity parrot = shoulder == Shoulder.LEFT ? player.getShoulderEntityLeft() : player.getShoulderEntityRight();
        if (parrot != null && !isFakeParrot(parrot)) {
            World world = player.getWorld();
            Method getNmsWorld;
            if (subversion > 16) {
                getNmsWorld = world.getClass().getMethod("getHandle");
            } else {//todo check if works with new
                Class<?> worldServer = getNMSBukkit("CraftWorld");
                getNmsWorld = worldServer.getMethod("getHandle");
            }

            Object nmsWorld = getNmsWorld.invoke(world);
            Method getNmsParrot = parrot.getClass().getMethod("getHandle");
            Object entityParrot = getNmsParrot.invoke(parrot);
            Location loc = player.getLocation();
            parrot.teleport(new Location(loc.getWorld(), loc.getX(), loc.getY() + 0.699999988079071D, loc.getZ()));

            String addEntityMethodName = subversion > 16 ? "addWithUUID" : "addEntity";
            Method addEntity;
            if (subversion > 16) { //todo check if i can just use Entity.class
                addEntity = nmsWorld.getClass().getMethod(addEntityMethodName, Class.forName("net.minecraft.world.entity.Entity"), CreatureSpawnEvent.SpawnReason.class);
            } else {
                addEntity = nmsWorld.getClass().getMethod(addEntityMethodName, getNMSClass("Entity"), CreatureSpawnEvent.SpawnReason.class);
            }
            addEntity.invoke(nmsWorld, entityParrot, CreatureSpawnEvent.SpawnReason.SHOULDER_ENTITY);

            // nmsWorld.addWithUUID((net.minecraft.world.entity.Entity) entityParrot, CreatureSpawnEvent.SpawnReason.SHOULDER_ENTITY);
            //1.16.5 and below =, with addEntity(ent, reason)
        }
    }

    private Class<?> getNMSClass(String name) throws ClassNotFoundException {
        return Class.forName("net.minecraft.server." + version + "." + name);
    }

    private Class<?> getNMSBukkit(String name) throws ClassNotFoundException {
        return Class.forName("org.bukkit.craftbukkit." + version + "." + name);
    }


    public static boolean isFakeParrot(Entity entity) {
        return entity != null && entity.getCustomName() != null && entity.getCustomName().equals("§cThis is a bug please notify admins.");
    }

    public enum Shoulder {
        LEFT, RIGHT, BOTH
    }
}

