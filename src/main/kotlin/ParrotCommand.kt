package fi.sulku.mc.parrots

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.PlayerArgument
import dev.jorel.commandapi.arguments.StringArgument
import dev.jorel.commandapi.executors.CommandExecutor
import dev.jorel.commandapi.executors.PlayerCommandExecutor
import fi.sulku.mc.parrots.data.Shoulder
import org.bukkit.entity.Parrot
import org.bukkit.entity.Player

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
                    ParrotManager.setFakeParrot(player, shoulder, null)
                } else {
                    val variant = Parrot.Variant.valueOf(color.uppercase())
                    ParrotManager.setFakeParrot(player, shoulder, variant)
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