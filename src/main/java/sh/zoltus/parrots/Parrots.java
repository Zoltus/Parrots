package sh.zoltus.parrots;

import jdk.jfr.Description;
import lombok.Getter;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.level.EntityPlayer;
import org.bstats.bukkit.Metrics;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Parrot;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.annotation.plugin.ApiVersion;
import org.bukkit.plugin.java.annotation.plugin.LogPrefix;
import org.bukkit.plugin.java.annotation.plugin.Plugin;
import org.bukkit.plugin.java.annotation.plugin.Website;
import org.bukkit.plugin.java.annotation.plugin.author.Author;
import sh.zoltus.parrots.configuration.OneYml;
import sh.zoltus.parrots.player.Holder;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Plugin(name = "Parrots", version = "2.0")
@Description("Parrots Plugin for 1.18.X")
@Author("Zoltus")
@Website("https://www.spigotmc.org/members/zoltus.306747/")
@LogPrefix("Parrots")
@ApiVersion(ApiVersion.Target.v1_18)
@Getter
public class Parrots extends JavaPlugin implements Listener {

    @Getter
    private static Parrots plugin;
    @Getter
    private static NamespacedKey fakeParrotKey;
    private final OneYml yml = new OneYml("config.yml", this.getDataFolder());

    @Override
    public void onEnable() {
        plugin = this;
        fakeParrotKey = new NamespacedKey(this, "fake");
        getServer().getPluginManager().registerEvents(this, this);
        new Metrics(this, 13235);
        new UpdateChecker(this, 42035).getVersion(version -> {
            if (this.getDescription().getVersion().equals(version)) {
                getLogger().info("There is not a new update available.");
            } else {
                getLogger().info("There is a new update available.");
            }
        });
    }

    @Override
    public void onDisable() {
        //re
    }

    @EventHandler
    public void move(PlayerToggleFlightEvent e) {
        Player p = e.getPlayer();
        if (!e.isFlying()) {
            Holder holder = Holder.of(p);
            holder.refreshShoulders();
        }
    }

    @EventHandler
    public void parrotTest(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        Holder holder = Holder.of(p);
        if (holder.hasFakeParrots()) {
            holder.setGlue(true);
        }
    }


    @EventHandler
    public void onChatt(PlayerCommandPreprocessEvent e) {
        Player p = e.getPlayer();
        String msg = e.getMessage();
        List<String> argsList = new ArrayList<>(Arrays.asList(msg.split(" ")));
        String cmd = argsList.get(0);
        argsList.remove(0);
        String[] args = argsList.toArray(new String[0]);

        Holder holder = Holder.of(p);

        if (cmd.startsWith("//")) {
            e.setCancelled(true);
            CraftPlayer cp = (CraftPlayer) p;
            EntityPlayer ep = cp.getHandle();

            switch (cmd.toLowerCase()) {
                case "//mount" -> {
                    holder.setParrot(Holder.Shoulder.LEFT, Parrot.Variant.GREEN);
                    holder.setParrot(Holder.Shoulder.RIGHT, Parrot.Variant.RED);
                    p.sendMessage("mounted");
                }

                case "//mount2" -> {
                    holder.setParrot(Holder.Shoulder.LEFT, Parrot.Variant.GRAY);
                    holder.setParrot(Holder.Shoulder.RIGHT, Parrot.Variant.CYAN);
                    p.sendMessage("mounted2");
                }

                case "//settime" -> {
                    try {
                        long time = Long.parseLong(args[0]);
                        //timeEntitySatOnShoulder
                        Field f = ep.getClass().getSuperclass().getDeclaredField("f");
                        f.setAccessible(true);
                        f.set(ep, time);
                        p.sendMessage("changed time");
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                }
                case "//resetmounttime" -> {
                    try {
                        long time = p.getWorld().getGameTime() + 20;
                        //timeEntitySatOnShoulder
                        Field f = ep.getClass().getSuperclass().getDeclaredField("f");
                        f.setAccessible(true);
                        f.set(ep, time);
                        p.sendMessage("changed time");
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                }
                case "//gametime" -> p.sendMessage("time: " + p.getWorld().getGameTime());
                case "//shoulders" -> {
                    p.sendMessage("left: " + p.getShoulderEntityLeft());
                    p.sendMessage("right: " + p.getShoulderEntityRight());
                }
                case "//mounttime" -> {//
                    try {
                        Field f = ep.getClass().getSuperclass().getDeclaredField("f");
                        f.setAccessible(true);
                        long time = f.getLong(ep);
                        p.sendMessage("munttime: " + time);
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                }
                case "//parrotlock" -> {
                    p.sendMessage("lock");
                    NBTTagCompound tag = new NBTTagCompound();
                    tag.a("a", "a");
                    //i left, j right, h autdecide shoulderentity //with craftEntity.save
                    ep.j(tag);
                    ep.i(tag);
                }
                case "//parrotunlock" -> {
                    p.sendMessage("unlock");
                    NBTTagCompound tag = new NBTTagCompound();
                    //i left, j right, h autdecide shoulderentity //with craftEntity.save
                    ep.j(tag);
                    ep.i(tag);
                }
                /*
                 1. aiTick checks if (!isPassenger() && this.onGround && !isInWater() && !this.isInPowderSnow)
                 2. if player. this.entityData.get(DATA_SHOULDER_LEFT) is empty it can mount
                 3. parrot mounts if it doesnt have cooldown and 1 of shoulders is empty


                 */
                // todo cant mount on water ect, reflection mount
                // public boolean setEntityOnShoulder(CompoundTag nbttagcompound) {
                // if (!isPassenger() && this.onGround && !isInWater() && !this.isInPowderSnow) {
                //if this.entityData.get(DATA_SHOULDER_LEFT) is empty it can mount

                //   public <T> T get(EntityDataAccessor<T> datawatcherobject) {
                //    return getItem(datawatcherobject).getValue();
                //  }
                //private <T> DataItem<T> getItem(EntityDataAccessor<T> datawatcherobject) {
                //     return (DataItem<T>)this.itemsById.get(datawatcherobject.getId());
                // }
                //nbttagcoumpound isEmpty


                //fake SynchedEntityData to isempty false, so other parrots cant mount
                // this.entityData.define(DATA_SHOULDER_LEFT, new CompoundTag());
                //    this.entityData.define(DATA_SHOULDER_RIGHT, new CompoundTag())
                //
                // public CompoundTag getShoulderEntityLeft() {
                //   return (CompoundTag)this.entityData.get(DATA_SHOULDER_LEFT);
                // }
                //
                // public void setShoulderEntityLeft(CompoundTag nbttagcompound) {
                //   this.entityData.set(DATA_SHOULDER_LEFT, nbttagcompound);
                // }
                //
                case "//refresh2" -> {
                    Entity left = p.getShoulderEntityLeft();
                    Entity right = p.getShoulderEntityRight();

                    p.setShoulderEntityLeft(null);
                    p.setShoulderEntityRight(null);
                    p.setShoulderEntityLeft(left);
                    p.setShoulderEntityRight(right);
                    p.sendMessage("refreshed");
                }


            }
        }
    }
    /*
    @EventHandler
    public void parrotTest(CreatureSpawnEvent e) {
        CreatureSpawnEvent.SpawnReason spawnReason = e.getSpawnReason();
        if (spawnReason == CreatureSpawnEvent.SpawnReason.SHOULDER_ENTITY) {
            if (e.getEntity() instanceof Parrot parrot) {
                String customName = parrot.getCustomName();
                if (customName != null && customName.equals("%PARROTS%")) {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void parrotTest(EntityDamageEvent e) {;
        if (e.getEntity() instanceof Player p) {
            Holder holder = Holder.of(p);
            if (holder.hasParrots()) {
                holder.refreshShoulders();
            }
        }
    }*/

    /**
     * Creates parrot with custom datacontainer byte to mark that parrot is custom.
     * Removes ai and makes parrot sit incase bugs happen so parrots wont escape.
     *
     * @param p     layer
     * @param color of the parrot
     * @return Parrot
     */
    private Parrot createParrot(Player p, Parrot.Variant color) {
        Parrot parrot = (Parrot) p.getWorld().spawnEntity(p.getLocation(), EntityType.PARROT);
        parrot.setSilent(plugin.getYml().getBoolean("Config.isSilent"));
        parrot.getPersistentDataContainer().set(fakeParrotKey, PersistentDataType.BYTE, (byte) 0);
        parrot.setVariant(color);
        parrot.setSitting(true);
        parrot.setOwner(p);
        parrot.setTamed(true);
        parrot.setAI(false);
        return parrot;
    }

    /*
     parrots are atleast 20ticks on shoulder when they entered
     100 tick sit cooldown on shoulder

     public Entity getShoulderEntityLeft() {
     if (!getHandle().fG().f()) {
     Optional<Entity> shoulder = EntityTypes.a(getHandle().fG(), (getHandle()).t);
     return !shoulder.isPresent() ? null : ((Entity)shoulder.get()).getBukkitEntity();
     }
     return null;
     }

     public void setShoulderEntityLeft(Entity entity) {
     getHandle().i((entity == null) ? new NBTTagCompound() : ((CraftEntity)entity).save());
     if (entity != null)
     entity.remove();
     }

     public Entity getShoulderEntityRight() {
     if (!getHandle().fH().f()) {
     Optional<Entity> shoulder = EntityTypes.a(getHandle().fH(), (getHandle()).t);
     return !shoulder.isPresent() ? null : ((Entity)shoulder.get()).getBukkitEntity();
     }
     return null;
     }

     public void setShoulderEntityRight(Entity entity) {
     getHandle().j((entity == null) ? new NBTTagCompound() : ((CraftEntity)entity).save());
     if (entity != null)
     entity.remove();
     }




     public <T> void b(DataWatcherObject<T> datawatcherobject, T t0) {
     DataWatcher.Item<T> datawatcher_item = this.b(datawatcherobject);
     if (ObjectUtils.notEqual(t0, datawatcher_item.b())) {
     datawatcher_item.a(t0);
     this.e.a(datawatcherobject);
     datawatcher_item.a(true);
     this.i = true;
     }

     }

     playerjava
     if (nbttagcompound.contains("ShoulderEntityLeft", 10)) {
     setShoulderEntityLeft(nbttagcompound.getCompound("ShoulderEntityLeft"));
     }

     if (nbttagcompound.contains("ShoulderEntityRight", 10)) {
     setShoulderEntityRight(nbttagcompound.getCompound("ShoulderEntityRight"));
     }


     public void setShoulderEntityLeft(CompoundTag nbttagcompound) {
     this.entityData.set(DATA_SHOULDER_LEFT, nbttagcompound);
     }


     public boolean setEntityOnShoulder(CompoundTag nbttagcompound) {
     if (!isPassenger() && this.onGround && !isInWater() && !this.isInPowderSnow) {
     if (getShoulderEntityLeft().isEmpty()) {
     setShoulderEntityLeft(nbttagcompound);
     this.timeEntitySatOnShoulder = this.level.getGameTime();
     return true;
     }  if (getShoulderEntityRight().isEmpty()) {
     setShoulderEntityRight(nbttagcompound);
     this.timeEntitySatOnShoulder = this.level.getGameTime();
     return true;
     }
     return false;
     }

     return false;
     }
     protected void removeEntitiesOnShoulder() {
     if (this.timeEntitySatOnShoulder + 20L < this.level.getGameTime()) {

     if (spawnEntityFromShoulder(getShoulderEntityLeft())) {
     setShoulderEntityLeft(new CompoundTag());
     }
     if (spawnEntityFromShoulder(getShoulderEntityRight())) {
     setShoulderEntityRight(new CompoundTag());
     }
     }
     }



     private boolean spawnEntityFromShoulder(CompoundTag nbttagcompound) {
     if (!this.level.isClientSide && !nbttagcompound.isEmpty()) {
     return ((Boolean)EntityType.create(nbttagcompound, this.level).map(entity -> {
     if (entity instanceof TamableAnimal) {
     ((TamableAnimal)entity).setOwnerUUID(this.uuid);
     }

     entity.setPos(getX(), getY() + 0.699999988079071D, getZ());
     return Boolean.valueOf(((ServerLevel)this.level).addWithUUID(entity, CreatureSpawnEvent.SpawnReason.SHOULDER_ENTITY));
     }).orElse(Boolean.valueOf(true))).booleanValue();
     }

     return true;
     }

     protected static final EntityDataAccessor<CompoundTag> DATA_SHOULDER_LEFT = SynchedEntityData.defineId(Player.class, EntityDataSerializers.COMPOUND_TAG);
     protected static final EntityDataAccessor<CompoundTag> DATA_SHOULDER_RIGHT = SynchedEntityData.defineId(Player.class, EntityDataSerializers.COMPOUND_TAG);
     private long timeEntitySatOnShoulder;

     protected void defineSynchedData() {
     super.defineSynchedData();
     this.entityData.define(DATA_PLAYER_ABSORPTION_ID, Float.valueOf(0.0F));
     this.entityData.define(DATA_SCORE_ID, Integer.valueOf(0));
     this.entityData.define(DATA_PLAYER_MODE_CUSTOMISATION, Byte.valueOf((byte)0));
     this.entityData.define(DATA_PLAYER_MAIN_HAND, Byte.valueOf((byte)1));
     this.entityData.define(DATA_SHOULDER_LEFT, new CompoundTag());
     this.entityData.define(DATA_SHOULDER_RIGHT, new CompoundTag());
     }

     step
     playShoulderEntityAmbientSound(getShoulderEntityLeft());
     playShoulderEntityAmbientSound(getShoulderEntityRight());
     //boom
     if ((!this.level.isClientSide && (this.fallDistance > 0.5F || isInWater())) || this.abilities.flying || isSleeping() || this.isInPowderSnow) {
     removeEntitiesOnShoulder();
     }


     private void playShoulderEntityAmbientSound(@Nullable CompoundTag nbttagcompound) {
     if (nbttagcompound != null && (!nbttagcompound.contains("Silent") || !nbttagcompound.getBoolean("Silent")) && this.level.random.nextInt(200) == 0) {
     String s = nbttagcompound.getString("id");

     EntityType.byString(s).filter(entitytypes -> (entitytypes == EntityType.PARROT))

     .ifPresent(entitytypes -> {
     if (!Parrot.imitateNearbyMobs(this.level, (Entity)this)) {
     this.level.playSound(null, getX(), getY(), getZ(), Parrot.getAmbient(this.level, this.level.random), getSoundSource(), 1.0F, Parrot.getPitch(this.level.random));
     }
     });
     }
     }

     public void startAutoSpinAttack(int i) {
     this.autoSpinAttackTicks = i;
     if (!this.level.isClientSide) {
     removeEntitiesOnShoulder();
     setLivingEntityFlag(4, true);
     }
     }

     on damage removeEntitiesOnShoulder


     private final ShoulderRidingEntity entity;
     this.entity.setEntityOnShoulder(this.owner);
     private ServerPlayer owner;


     {AbsorptionAmount:0.0f,Age:0,AgeLocked:0b,Air:300s,ArmorDropChances:[0.085f,0.085f,0.085f,0.085f],ArmorItems:[{},{},{},{}],Attributes:[{Base:6.0d,Name:"minecraft:generic.max_health"},{Base:16.0d,Modifiers:[{Amount:0.022699729516014625d,Name:"Random spawn bonus",Operation:1,UUID:[I;134221125,251809616,-1115463660,1392643272]}],Name:"minecraft:generic.follow_range"}],Brain:{memories:{}},Bukkit.Aware:1b,Bukkit.updateLevel:2,CanPickUpLoot:0b,DeathTime:0s,FallDistance:0.0f,FallFlying:0b,Fire:-1s,ForcedAge:0,HandDropChances:[0.085f,0.085f],HandItems:[{},{}],Health:6.0f,HurtByTimestamp:0,HurtTime:0s,InLove:0,Invulnerable:0b,LeftHanded:0b,Motion:[0.0d,0.0d,0.0d],OnGround:0b,Owner:[I;529187973,1944471018,-1532636126,-2014973603],Paper.Origin:[-47.583991373246285d,63.0d,-9.214380896530914d],Paper.OriginWorld:[I;2142263796,-2086845947,-1705589142,-784772837],Paper.SpawnReason:"CUSTOM",PersistenceRequired:1b,PortalCooldown:0,Pos:[-47.583991373246285d,63.0d,-9.214380896530914d],Purpur.ShouldBurnInDay:0b,Purpur.ticksSinceLastInteraction:0,Rotation:[111.51586f,19.101288f],Silent:1b,Sitting:1b,Spigot.ticksLived:0,UUID:[I;-1483941076,-548387880,-1497422390,480439213],Variant:1,WorldUUIDLeast:-7325449581792505573L,WorldUUIDMost:9200952945432936965L,id:"minecraft:parrot"}
     */
}
