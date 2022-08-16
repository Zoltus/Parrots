package sh.zoltus.parrots.player;

import lombok.SneakyThrows;
import net.minecraft.server.level.EntityPlayer;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Parrot;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
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
public record Holder(Player player) {

    private static final Map<UUID, Holder> holders = new HashMap<>();
    private static final NamespacedKey fakeParrotKey = Parrots.getFakeParrotKey();

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
       // lockEmptyShoulders(true); //Needs relock when fly toggle
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
        /*
        if (hasFakeParrots()) {
            //lockEmptyShoulders(true);
        } else if (!hasParrots()) {
            Bukkit.broadcastMessage("FALSE");
            //lockEmptyShoulders(false);
           // setGlue(false);
        }*/
    }

    @SneakyThrows //todo remove method , can be done other way, without reflection
    private void dropRealParrots() {
        //todo remove reflection
        CraftPlayer cp = (CraftPlayer) player;
        EntityPlayer ep = cp.getHandle();
        //todo check if shoulder side check is needed
        if (hasParrots() && !hasFakeParrots()) {
            //todo shouldnt work if lock is on
            Method method = ep.getClass().getSuperclass().getDeclaredMethod("fP"); //Drops both parrots
            //fe == 1.18.2, fD == 1.18, 1.19 = fP & 1.19.2
            method.setAccessible(true);
            method.invoke(ep);
            //lockEmptyShoulders(false); //this resets shoulders, useless here probably
        }
    }

    @SneakyThrows
    public void setParrot(Shoulder shoulder, Parrot.Variant color) {
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
        //todo? wasnt orginally here, does this affect relog?
        //parrot.remove();
        switch (shoulder) {
            case LEFT -> player.setShoulderEntityLeft(parrot);
            case RIGHT -> player.setShoulderEntityRight(parrot);
            case BOTH -> {
                player.setShoulderEntityRight(parrot);
                player.setShoulderEntityLeft(parrot);
            }
        }
      //  setGlue(true);
      //  lockEmptyShoulders(true);
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



        /* // remove parrots, and if empty unlockshoulders and remove glue
    //i left, j right, h autodecide? shoulderentity //with craftEntity.save
    //if i remember right adding tag to player shoulders fakes that it has parrot
    //so real parrots cant fly to it
    // Not needed
    public void lockEmptyShoulders(boolean lock) {


        CraftPlayer cp = (CraftPlayer) player;
        EntityPlayer ep = cp.getHandle();
        NBTTagCompound tag = new NBTTagCompound();

        //br right, bq left
        //EntityDataAccessor == datawatcher
        //todo check reflections old ones are 1.18.2// protected final SynchedEntityData entityData;
        if (lock) { // setShoulderEntityLeft(new CompoundTag());  = empty tag remove parrot
            //Creates tag?
            //puts tag to combound map
            tag.a("lock", "lock");
        }
        if (player.getShoulderEntityLeft() == null) {
            ep.i(tag); //Se setShoulderEntityLeft(CompoundTag nbttagcompound) {
            //  this.entityData.set(DATA_SHOULDER_LEFT, nbttagcompound);
            if (lock)
                player.sendMessage("lockedLEft");
        }
        if (player.getShoulderEntityRight() == null) {
            ep.j(tag);
            if (lock)
                player.sendMessage("lockedright");
        }

    }*/

/**
 * glue for parrots. if true it sets gluetimer to max longvalue, else false and parrots will drop on jump ect
 */
    /*@SneakyThrows
    public void setGlue(boolean glue) {
        CraftPlayer cp = (CraftPlayer) player;
        EntityPlayer ep = cp.getHandle();
        long time = glue ? 9999 : 0;
        //timeEntitySatOnShoulder

        Field f = ep.getClass().getSuperclass().getDeclaredField("co"); //f == 1.18, co == 1.19 && 1.19.2,
        f.setAccessible(true);
        Bukkit.broadcastMessage("time" + f.get(ep));
        f.set(ep, time);
        Bukkit.broadcastMessage("time" + f.get(ep));
        player.sendMessage("glued");
    }*/

