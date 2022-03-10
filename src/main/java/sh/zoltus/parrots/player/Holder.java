package sh.zoltus.parrots.player;

import lombok.SneakyThrows;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.level.EntityPlayer;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer;
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

public record Holder(Player p) {
    private static NamespacedKey fakeParrotKey = Parrots.getFakeParrotKey();
    private static final Map<UUID, Holder> holders = new HashMap<>();
    //keep on login?, need to save so indentifies as custom parrot. onjoin can check if parrot has name

    public Holder(Player p) {
        this.p = p;
        holders.put(p.getUniqueId(), this);
    }


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

    public static Holder of(Player p) {
        return holders.getOrDefault(p.getUniqueId(), new Holder(p));
    }
    //removeEntitiesOnShoulder

    public void refreshShoulders() {
        Entity left = p.getShoulderEntityLeft();
        Entity right = p.getShoulderEntityRight();
        p.setShoulderEntityLeft(null);
        p.setShoulderEntityRight(null);
        p.setShoulderEntityLeft(left);
        p.setShoulderEntityRight(right);
        p.sendMessage("refreshed");
    }

    //
    @SneakyThrows
    private void dropRealParrots() {
        CraftPlayer cp = (CraftPlayer) p;
        EntityPlayer ep = cp.getHandle();
        Entity eParrot1 = p.getShoulderEntityLeft();
        Entity eParrot2 = p.getShoulderEntityRight();
        if (isRealParrot(eParrot1) || isRealParrot(eParrot2)) {
            setGlue(false); // this would drop fakes aswell, need check for this?
            Method method = ep.getClass().getSuperclass().getDeclaredMethod("fE"); //Drops both parrots
            method.setAccessible(true);
            method.invoke(ep);
            emptyShoulderLock(false); //this resets shoulders, useless here probably
        }
    }

    private boolean isRealParrot(Entity entity) {
        return entity instanceof Parrot parrot && !parrot.getPersistentDataContainer().has(fakeParrotKey, PersistentDataType.BYTE);
    }

    public enum Shoulder {
        LEFT, RIGHT, BOTH
    }

    @SneakyThrows
    public void setParrot(Shoulder shoulder, Parrot.Variant color) {
        dropRealParrots();
        Parrot parrot = (Parrot) p.getWorld().spawnEntity(p.getLocation(), EntityType.PARROT);
        //parrot.setSilent(plugin.getYml().getBoolean("Config.isSilent"));
        parrot.getPersistentDataContainer().set(fakeParrotKey, PersistentDataType.BYTE, (byte) 0);
        parrot.setVariant(color);
        parrot.setSitting(true);
        parrot.setOwner(p);
        parrot.setTamed(true);
        parrot.setAI(false);

        switch (shoulder) {
            case LEFT -> {
                p.setShoulderEntityLeft(parrot);
            }
            case RIGHT -> {
                p.setShoulderEntityRight(parrot);
            }
        }
        setGlue(true);
        emptyShoulderLock(true);
    }

    //todo remove parrots, and if empty unlockshoulders and remove glue
    /**
     * @param glue for parrots. if true it sets gluetimer to 999, else false and parrots will drop on jump ect
     */
    @SneakyThrows
    private void setGlue(boolean glue) {
        CraftPlayer cp = (CraftPlayer) p;
        EntityPlayer ep = cp.getHandle();
        long time = glue ? 9999999 : 0;
        //timeEntitySatOnShoulder
        Field f = ep.getClass().getSuperclass().getDeclaredField("f");
        f.setAccessible(true);
        f.set(ep, time);
        p.sendMessage("glued");
    }

    //i left, j right, h autdecide shoulderentity //with craftEntity.save
    private void emptyShoulderLock(boolean lock) {
        CraftPlayer cp = (CraftPlayer) p;
        EntityPlayer ep = cp.getHandle();
        NBTTagCompound tag = new NBTTagCompound();
        p.sendMessage("lock left " + lock);
        if (lock) {
            tag.a("lock", "lock");
        }
        if (p.getShoulderEntityLeft() == null) {
            ep.i(tag);
        }
        if (p.getShoulderEntityRight() == null) {
            ep.j(tag);
        }
    }
}
