package fi.sulku.mc.parrots

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.wrappers.WrappedDataValue
import com.comphenix.protocol.wrappers.WrappedDataWatcher
import com.comphenix.protocol.wrappers.nbt.NbtFactory
import fi.sulku.mc.parrots.data.ParrotData
import fi.sulku.mc.parrots.data.Shoulder
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.entity.Parrot
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerBedLeaveEvent
import org.bukkit.event.player.PlayerGameModeChangeEvent
import org.bukkit.event.player.PlayerRespawnEvent
import org.bukkit.event.player.PlayerToggleFlightEvent
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

    @EventHandler
    fun onBedLeave(event: PlayerBedLeaveEvent) = restoreParrot(event.player)

    @EventHandler
    fun onRespawn(event: PlayerRespawnEvent) = restoreWithDelay(event.player)

    @EventHandler
    fun onGameModeChange(event: PlayerGameModeChangeEvent) =
        restoreWithDelay(event.player, event.newGameMode != GameMode.SPECTATOR)

    @EventHandler
    fun onToggleFly(event: PlayerToggleFlightEvent) = restoreWithDelay(event.player, !event.isFlying)

    fun restoreWithDelay(player: Player, condition: Boolean = true) {
        if (condition) {
            Bukkit.getScheduler().runTaskLater(Parrots.instance, Runnable {
                restoreParrot(player)
            }, 2L)
        }
    }

    fun restoreParrot(player: Player) {
        val data = userParrotData[player.uniqueId] ?: return
        // Restore left shoulder parrot
        setFakeParrot(player, Shoulder.LEFT, data.leftVariant)
        // Restore right shoulder parrot
        setFakeParrot(player, Shoulder.RIGHT, data.rightVariant)
    }

    fun setFakeParrot(player: Player, shoulder: Shoulder, variant: Parrot.Variant? = null) {
        //Set to userParrotData
        val parrotData = userParrotData.getOrPut(player.uniqueId) { ParrotData() }.apply {
            when (shoulder) {
                Shoulder.LEFT -> leftVariant = variant
                Shoulder.RIGHT -> rightVariant = variant
                Shoulder.BOTH -> {
                    leftVariant = variant
                    rightVariant = variant
                }
            }
        }

        // Create the NBT tag for the parrot empty to remove
        val parrotTag = if (variant == null) {
            NbtFactory.ofCompound("").handle
        } else {
            NbtFactory.ofCompound("").apply {
                put("id", "minecraft:parrot")
                put("Variant", variant.ordinal)
                put("Sitting", 1.toByte())
                put("Age", 0)
                put("AgeLocked", 0.toByte())
                put("Silent", 1.toByte())
            }.handle
        }

        val registry = WrappedDataWatcher.Registry.getNBTCompoundSerializer()
        val dataValues = listOfNotNull(
            parrotData.leftVariant?.let { WrappedDataValue(19, registry, parrotTag) },
            parrotData.rightVariant?.let { WrappedDataValue(20, registry, parrotTag) }
        )
        // Send packets for each shoulder
        val packet = PacketContainer(PacketType.Play.Server.ENTITY_METADATA).apply {
            integers.write(0, player.entityId)
            dataValueCollectionModifier.write(0, dataValues)
        }
        ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet)
    }
}