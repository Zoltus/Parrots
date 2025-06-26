package fi.sulku.mc.parrots.command

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.wrappers.WrappedDataValue
import com.comphenix.protocol.wrappers.WrappedDataWatcher
import com.comphenix.protocol.wrappers.nbt.NbtFactory
import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.PlayerArgument
import dev.jorel.commandapi.arguments.StringArgument
import dev.jorel.commandapi.executors.CommandExecutor
import dev.jorel.commandapi.executors.PlayerCommandExecutor
import fi.sulku.mc.parrots.Shoulder
import org.bukkit.entity.Parrot
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.ConcurrentHashMap

private val userParrotData = ConcurrentHashMap<UUID, ParrotData>()

//Todo custom arg?
data class ParrotData(
    val leftShoulder: Parrot.Variant? = null,
    val rightShoulder: Parrot.Variant? = null
)

fun setFakeParrot(player: Player, shoulder: Shoulder, variant: Parrot.Variant? = null) {
    val shoulders = if (shoulder == Shoulder.BOTH) listOf(Shoulder.LEFT, Shoulder.RIGHT) else listOf(shoulder)

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

    // Send packets for each shoulder
    ProtocolLibrary.getProtocolManager().sendServerPacket(
        player,
        PacketContainer(PacketType.Play.Server.ENTITY_METADATA).apply {
            integers.write(0, player.entityId)
            dataValueCollectionModifier.write(0, shoulders.map {
                WrappedDataValue(it.metaIndex, WrappedDataWatcher.Registry.getNBTCompoundSerializer(), parrotTag)
            })
        }
    )
}

object ParrotCommand {

    fun register() {
        val reload = CommandAPICommand("reload")
            .withShortDescription("Reloads the plugin configuration")
            .withUsage("/parrots reload")
            .withPermission("parrots.reload")
            .executesPlayer(PlayerCommandExecutor { player, args ->

            })

        val version = CommandAPICommand("version")
            .withShortDescription("Displays the plugin version")
            .withUsage("/parrots version")
            .executesPlayer(PlayerCommandExecutor { player, args ->
                //setShoulder(player)
            })

        //todo /parrot <shoulder> <variant/none>? for player cmd
        val set = CommandAPICommand("set")
            .withShortDescription("Sets a parrot for a player")
            .withUsage("/parrots set <player> <shoulder> <variant/none>")
            .withPermission("parrots.set")
            .withArguments(
                PlayerArgument("player"),
                StringArgument("shoulder").replaceSuggestions(ArgumentSuggestions.strings("LEFT", "RIGHT", "BOTH")),
                StringArgument("variant").replaceSuggestions(ArgumentSuggestions.strings(Parrot.Variant.entries.map { it.name } + "NONE"))
            ).executes(CommandExecutor { sender, args ->
                val player = args.get("player") as Player
                val side = args.get("shoulder") as String
                val color = args.get("variant") as String
                val shoulder = Shoulder.valueOf(side.uppercase())

                if (color == "NONE") {
                    setFakeParrot(player, shoulder, null)
                } else {
                    val variant = Parrot.Variant.valueOf(color.uppercase())
                    setFakeParrot(player, shoulder, variant)
                }
            })

        CommandAPICommand("parrots")
            .withShortDescription("Parrots related commands")
            .withUsage("/parrots <subcommand>")
            .withAliases("parrot", "parrots")
            .withPermission("parrots.menu")
            .withSubcommands(reload, version, set)
            .override()
        //todo menu
    }
}