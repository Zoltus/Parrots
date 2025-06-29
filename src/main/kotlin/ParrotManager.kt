package fi.sulku.mc.parrots

import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.protocol.entity.data.EntityData
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes
import com.github.retrooper.packetevents.protocol.nbt.NBTByte
import com.github.retrooper.packetevents.protocol.nbt.NBTCompound
import com.github.retrooper.packetevents.protocol.nbt.NBTInt
import com.github.retrooper.packetevents.protocol.nbt.NBTString
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata
import fi.sulku.mc.parrots.data.ParrotData
import fi.sulku.mc.parrots.data.Shoulder
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.entity.Entity
import org.bukkit.entity.Parrot
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.*
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object ParrotManager : Listener {
    val userParrotData = ConcurrentHashMap<UUID, ParrotData>()

    init {
        //Repeating restore mostly for powdered snow
        Bukkit.getScheduler().runTaskTimer(Parrots.instance, Runnable {
            Bukkit.getOnlinePlayers().forEach(::restoreParrot)
        }, 0L, 15 * 20L) // Every 15 second todo config
    }

    //todo toggle can pickup parrots
    //todo switch world?
    //EntityPotionEffectEvent invisible?
    //SuperVanish/PremiumVanish support? EssentialsX?
    //todo packets, instant only self and for others rely on scheduler for less packets
    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        removeLegacyParrots(player)     //Todo config
        restoreWithDelay(event.player) // todo only if has parrots
    }

    private fun removeLegacyParrots(player: Player) {
        if (shouldRemoveParrot(player.shoulderEntityLeft)) {
            player.shoulderEntityLeft = null
        }
        if (shouldRemoveParrot(player.shoulderEntityRight)) {
            player.shoulderEntityRight = null
        }
    }

    private fun shouldRemoveParrot(entity: Entity?): Boolean {
        return entity is Parrot
                && entity.customName?.contains("Parrot") == true
                && entity.isSitting
    }

    //todo remove old versions real parrots
    /*
      public ParrotPet(Player p, Parrot.Variant color, Shoulder side) {
    this.parrot.setCustomName(p.getUniqueId() + "Parrot");
    this.parrot.setCustomNameVisible(false);
    this.parrot.setSitting(true);
  }
     */

    @EventHandler
    fun onRespawn(event: PlayerCustomClickEvent) {
        val id = event.id.key

        if (id == "submit_parrot") {
            val player = event.player
            val data = event.data!!
            val leftCol = data.asJsonObject.get("left_shoulder").asString.uppercase()
            val rightCol = data.asJsonObject.get("right_shoulder").asString.uppercase()

            //todo perform cmd? it has the perms
            player.performCommand("parrots set LEFT $leftCol")
            player.performCommand("parrots set RIGHT $rightCol") //todo dont use cmd
            player.sendMessage("Left shoulder parrot: $leftCol, Right shoulder parrot: $rightCol")
        }
    }

    @EventHandler
    fun onBedLeave(event: PlayerBedLeaveEvent) = restoreParrot(event.player)

    @EventHandler
    fun onRespawn(event: PlayerRespawnEvent) = restoreWithDelay(event.player)

    @EventHandler
    fun onGameModeChange(event: PlayerGameModeChangeEvent) =
        restoreWithDelay(event.player, event.newGameMode != GameMode.SPECTATOR)

    @EventHandler
    fun onToggleFly(event: PlayerToggleFlightEvent) = restoreWithDelay(event.player, !event.isFlying)

    fun restoreWithDelay(player: Player, shouldRestore: Boolean = true) {
        if (shouldRestore) {
            Bukkit.getScheduler().runTaskLater(Parrots.instance, Runnable {
                restoreParrot(player)
            }, 2L)
        }
    }

    private fun canHaveParrot(player: Player) =
        player.gameMode != GameMode.SPECTATOR && !player.isDead && !player.isSleeping && !player.isFlying


    //todo restore only if canHave parrot/ send packet only then
    fun restoreParrot(player: Player) {
        val data = userParrotData[player.uniqueId] ?: return
        // Restore left shoulder parrot
        setFakeParrot(player, Shoulder.LEFT, data.leftVariant)
        // Restore right shoulder parrot
        setFakeParrot(player, Shoulder.RIGHT, data.rightVariant)
    }

    fun setFakeParrot(player: Player, shoulder: Shoulder, variant: Parrot.Variant? = null) {
        //Set to userParrotData
        userParrotData.getOrPut(player.uniqueId) { ParrotData() }.apply {
            when (shoulder) {
                Shoulder.LEFT -> leftVariant = variant
                Shoulder.RIGHT -> rightVariant = variant
                Shoulder.BOTH -> {
                    leftVariant = variant
                    rightVariant = variant
                }
            }
        }

        if (!canHaveParrot(player)) return // Dont send packets if player can't have parrot

        // Create the NBT tag for the parrot empty to remove
        val parrotNbt = if (variant == null) {
            NBTCompound()
        } else {
            NBTCompound().apply {
                // Entity ID
                setTag("id", NBTString("minecraft:parrot"))
                // Parrot variant (0=red, 1=blue, 2=green, 3=cyan, 4=gray)
                setTag("Variant", NBTInt(variant.ordinal))
                // Parrot is sitting
                setTag("Sitting", NBTByte(1))
                // Age (0 = adult)
                setTag("Age", NBTInt(0))
                // Age locked (prevents growing up/down)
                setTag("AgeLocked", NBTByte(0))
                // Silent (no sounds)
                setTag("Silent", NBTByte(1))
                // Optional: Custom name
                // setTag("CustomName", NBTString("\"My Parrot\""))
                // setTag("CustomNameVisible", NBTByte(1))
            }
        }

        // Get player's real shoulder data
        val hasLeftRealParrot = player.shoulderEntityLeft != null
        val hasRightRealParrot = player.shoulderEntityRight != null
        // Add send fake parrot packet only to shoulders which don't contain real parrots
        // This mostly only affects PaperMc "parrots-are-unaffected-by-player-movement: true" setting
        // todo fix paper bug where parrot stays invisible if the parrot config is enable and player flies ect.

        val dataValues = buildList {
            if (shoulder == Shoulder.LEFT || shoulder == Shoulder.BOTH) {
                if (!hasLeftRealParrot) {
                    add(EntityData(19, EntityDataTypes.NBT, parrotNbt))
                }
            }

            if (shoulder == Shoulder.RIGHT || shoulder == Shoulder.BOTH) {
                if (!hasRightRealParrot) {
                    add(EntityData(20, EntityDataTypes.NBT, parrotNbt))
                }
            }
        }

        val packet2 = WrapperPlayServerEntityMetadata(player.entityId, dataValues)

        PacketEvents.getAPI().playerManager.sendPacket(player, packet2)
    }
}