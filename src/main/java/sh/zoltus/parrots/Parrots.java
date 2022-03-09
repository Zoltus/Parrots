package sh.zoltus.parrots;

import jdk.jfr.Description;
import lombok.Getter;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Parrot;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.annotation.plugin.ApiVersion;
import org.bukkit.plugin.java.annotation.plugin.LogPrefix;
import org.bukkit.plugin.java.annotation.plugin.Plugin;
import org.bukkit.plugin.java.annotation.plugin.Website;
import org.bukkit.plugin.java.annotation.plugin.author.Author;
import sh.zoltus.parrots.configuration.OneYml;
import sh.zoltus.parrots.player.Holder;
import sh.zoltus.parrots.utils.NBTPlayer;

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
    private final OneYml yml = new OneYml("config.yml", this.getDataFolder());

    @Override
    public void onEnable() {
        plugin = this;
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
    public void move(PlayerMoveEvent e) {
        Player p = e.getPlayer();


        //Reflection isInPowderSnow
        //isClientSide tracks logics this.level.isClientSide

        if (((p.getFallDistance() > 0.5F || p.isInWater())) || p.isFlying() || p.isSleeping()) {
            Bukkit.broadcastMessage("§c-move");
            Holder holder = Holder.of(p);
            holder.refreshShoulders();
        }
    }

    @EventHandler
    public void parrotTest(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        Holder holder = Holder.of(p);
        Parrot parrot = parr(p, Parrot.Variant.BLUE, holder);
        holder.refreshShoulders();
        NBTPlayer nbtPlayer = new NBTPlayer(p);
        Bukkit.broadcastMessage("" + nbtPlayer.getShoulderLeft());

    }

    private Parrot parr(Player p, Parrot.Variant color, Holder holder) {
        Parrot parrot = (Parrot) p.getWorld().spawnEntity(p.getLocation(), EntityType.PARROT);
        holder.setLeftShoulder(parrot);
        parrot.remove();
        parrot.setSilent(plugin.getYml().getBoolean("Config.isSilent"));
        parrot.setCustomNameVisible(false);
        parrot.setVariant(color);
        parrot.setSitting(true);
        parrot.setOwner(p);
        parrot.setTamed(true);
        parrot.setAI(true);
        return parrot;
    }


    /**

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




     {AbsorptionAmount:0.0f,Age:0,AgeLocked:0b,Air:300s,ArmorDropChances:[0.085f,0.085f,0.085f,0.085f],ArmorItems:[{},{},{},{}],Attributes:[{Base:6.0d,Name:"minecraft:generic.max_health"},{Base:16.0d,Modifiers:[{Amount:0.022699729516014625d,Name:"Random spawn bonus",Operation:1,UUID:[I;134221125,251809616,-1115463660,1392643272]}],Name:"minecraft:generic.follow_range"}],Brain:{memories:{}},Bukkit.Aware:1b,Bukkit.updateLevel:2,CanPickUpLoot:0b,DeathTime:0s,FallDistance:0.0f,FallFlying:0b,Fire:-1s,ForcedAge:0,HandDropChances:[0.085f,0.085f],HandItems:[{},{}],Health:6.0f,HurtByTimestamp:0,HurtTime:0s,InLove:0,Invulnerable:0b,LeftHanded:0b,Motion:[0.0d,0.0d,0.0d],OnGround:0b,Owner:[I;529187973,1944471018,-1532636126,-2014973603],Paper.Origin:[-47.583991373246285d,63.0d,-9.214380896530914d],Paper.OriginWorld:[I;2142263796,-2086845947,-1705589142,-784772837],Paper.SpawnReason:"CUSTOM",PersistenceRequired:1b,PortalCooldown:0,Pos:[-47.583991373246285d,63.0d,-9.214380896530914d],Purpur.ShouldBurnInDay:0b,Purpur.ticksSinceLastInteraction:0,Rotation:[111.51586f,19.101288f],Silent:1b,Sitting:1b,Spigot.ticksLived:0,UUID:[I;-1483941076,-548387880,-1497422390,480439213],Variant:1,WorldUUIDLeast:-7325449581792505573L,WorldUUIDMost:9200952945432936965L,id:"minecraft:parrot"}
     */
}
