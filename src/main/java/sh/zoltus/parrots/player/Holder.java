package sh.zoltus.parrots.player;

import lombok.SneakyThrows;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.level.EntityPlayer;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Parrot;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import sh.zoltus.parrots.Parrots;

import java.lang.reflect.Field;
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
public record Holder(Player p) {

    private static final Map<UUID, Holder> holders = new HashMap<>();
    private static final NamespacedKey fakeParrotKey = Parrots.getFakeParrotKey();

    public Holder(Player p) {
        this.p = p;
        holders.put(p.getUniqueId(), this);
    }

    public static Holder of(Player p) {
        return holders.getOrDefault(p.getUniqueId(), new Holder(p));
    }

    public void refreshShoulders() {
        Entity left = p.getShoulderEntityLeft();
        Entity right = p.getShoulderEntityRight();
        p.setShoulderEntityLeft(null);
        p.setShoulderEntityRight(null);
        p.setShoulderEntityLeft(left);
        p.setShoulderEntityRight(right);
        lockEmptyShoulders(true); //Needs relock when  fly toggle
        p.sendMessage("refreshed");
    }

    public void removeParrot(Shoulder shoulder) {
        if (hasFakeParrots()) {
            switch (shoulder) {
                case LEFT -> p.setShoulderEntityLeft(null);
                case RIGHT -> p.setShoulderEntityRight(null);
                case BOTH -> {
                    p.setShoulderEntityLeft(null);
                    p.setShoulderEntityRight(null);
                }
            }
        }
        if (hasFakeParrots()) {
            lockEmptyShoulders(true);
        } else if (!hasParrots()) {
            lockEmptyShoulders(false);
            setGlue(false);
        }
    }

    @SneakyThrows
    private void dropRealParrots() {
        //todo remove reflection
        CraftPlayer cp = (CraftPlayer) p;
        EntityPlayer ep = cp.getHandle();
        if (hasRealParrots()) {
            //todo make better
            Method method = ep.getClass().getSuperclass().getDeclaredMethod("fE"); //Drops both parrots
            //fe == 1.18.2, fD == 1.18, 1.19 = fP
            method.setAccessible(true);
            method.invoke(ep);
            lockEmptyShoulders(false); //this resets shoulders, useless here probably
        }
    }

    @SneakyThrows
    public void setParrot(Shoulder shoulder, Parrot.Variant color) {
        dropRealParrots();
        Parrot parrot = (Parrot) p.getWorld().spawnEntity(p.getLocation(), EntityType.PARROT);
        parrot.setSilent(Parrots.getPlugin().getYml().getBoolean("Config.isSilent"));
        parrot.getPersistentDataContainer().set(fakeParrotKey, PersistentDataType.BYTE, (byte) 0);
        parrot.setVariant(color);
        parrot.setSitting(true);
        parrot.setOwner(p);
        parrot.setTamed(true);
        parrot.setAI(false);
        //todo? wasnt orginally here
        parrot.remove();

        switch (shoulder) {
            case LEFT -> p.setShoulderEntityLeft(parrot);
            case RIGHT -> p.setShoulderEntityRight(parrot);
            case BOTH -> {
                //todo test if same parrot can be on both sametime
                p.setShoulderEntityRight(parrot);
                p.setShoulderEntityLeft(parrot);
            }
        }
        setGlue(true);
        lockEmptyShoulders(true);
    }

    //todo remove parrots, and if empty unlockshoulders and remove glue

    /**
     * @param glue for parrots. if true it sets gluetimer to 999, else false and parrots will drop on jump ect
     */
    @SneakyThrows
    public void setGlue(boolean glue) {
        CraftPlayer cp = (CraftPlayer) p;
        EntityPlayer ep = cp.getHandle();
        long time = glue ? 9999999 : 0;
        //timeEntitySatOnShoulder
        Field f = ep.getClass().getSuperclass().getDeclaredField("co"); //f == 1.18, co == 1.19
        f.setAccessible(true);
        f.set(ep, time);
        p.sendMessage("glued");
    }

    //i left, j right, h autodecide? shoulderentity //with craftEntity.save
    public void lockEmptyShoulders(boolean lock) {
        CraftPlayer cp = (CraftPlayer) p;
        EntityPlayer ep = cp.getHandle();
        NBTTagCompound tag = new NBTTagCompound();
        //todo check reflections old ones are 1.18.2
        if (lock) {
            tag.a("lock", "lock");
        }
        if (p.getShoulderEntityLeft() == null) {
            ep.i(tag);
            if (lock)
                p.sendMessage("lockedLEft");
        }
        if (p.getShoulderEntityRight() == null) {
            ep.j(tag);
            if (lock)
                p.sendMessage("lockedright");
        }
    }

    private boolean hasParrots() {
        return p.getShoulderEntityRight() != null || p.getShoulderEntityLeft() != null;
    }

    private boolean hasRealParrots() {
        return hasParrots() && isRealParrot(p.getShoulderEntityLeft()) || isRealParrot(p.getShoulderEntityRight());
    }

    public boolean hasFakeParrots() {
        return hasParrots() && (!isRealParrot(p.getShoulderEntityLeft()) || !isRealParrot(p.getShoulderEntityRight()));
    }

    private boolean isRealParrot(Entity entity) {
        return entity instanceof Parrot parrot && !parrot.getPersistentDataContainer().has(fakeParrotKey, PersistentDataType.BYTE);
    }

    public enum Shoulder {
        LEFT, RIGHT, BOTH
    }
}
