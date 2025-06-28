package fi.sulku.mc.parrots

import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.dialog.DialogBase
import net.md_5.bungee.api.dialog.NoticeDialog
import net.md_5.bungee.api.dialog.action.ActionButton
import net.md_5.bungee.api.dialog.action.CustomClickAction
import net.md_5.bungee.api.dialog.input.DialogInput
import net.md_5.bungee.api.dialog.input.InputOption
import net.md_5.bungee.api.dialog.input.SingleOptionInput
import org.bukkit.entity.Player

fun showDialog(player: Player) {
    //todo config
    val options: List<Pair<String, ChatColor>> = listOf(
        "red" to ChatColor.RED,
        "blue" to ChatColor.BLUE,
        "green" to ChatColor.GREEN,
        "cyan" to ChatColor.AQUA,
        "gray" to ChatColor.GRAY,
        "none" to ChatColor.WHITE
    )
    val parrColors = options.mapIndexed { index, pair ->
        val (name, color) = pair
        InputOption(name.lowercase(), ComponentBuilder(name.lowercase()).color(color).build(), index == 0)
    }.toTypedArray()

    val inputs = listOf<DialogInput>(
        SingleOptionInput(
            "left_shoulder", ComponentBuilder("Left Shoulder").build(),
            *parrColors
        ),
        SingleOptionInput(
            "right_shoulder", ComponentBuilder("Right Shoulder").build(),
            *parrColors
        )
    )

    val action = ActionButton(
        ComponentBuilder("Submit").build(),
        CustomClickAction("submit_parrot")
    )

    val dialogBase = DialogBase(ComponentBuilder("Parrot Selection").build())
        .inputs(inputs)
        .afterAction(DialogBase.AfterAction.CLOSE)
        .pause(false)

    val dialog = NoticeDialog(dialogBase, action)

    player.showDialog(dialog)

    /*    var notice: Dialog = NoticeDialog(DialogBase(ComponentBuilder("Hello").color(ChatColor.RED).build()))
        player.showDialog(notice)

        var base = DialogBase(ComponentBuilder("Hello").color(ChatColor.RED).build())
            .inputs(
                listOf<DialogInput>(
                    TextInput("first", ComponentBuilder("First").build()),
                    TextInput("second", ComponentBuilder("Second").build())
                )
            )
        val noticeDialog = NoticeDialog(base)
        notice = noticeDialog.action(ActionButton(ComponentBuilder("Submit Button").build(), CustomClickAction("customform")))

        val build: BaseComponent = ComponentBuilder("click me").event(ShowDialogClickEvent(notice)).build()

        player.spigot().sendMessage(build)*/
}