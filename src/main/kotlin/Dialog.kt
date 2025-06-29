package fi.sulku.mc.parrots

import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.protocol.dialog.CommonDialogData
import com.github.retrooper.packetevents.protocol.dialog.DialogAction
import com.github.retrooper.packetevents.protocol.dialog.NoticeDialog
import com.github.retrooper.packetevents.protocol.dialog.action.DynamicCustomAction
import com.github.retrooper.packetevents.protocol.dialog.button.ActionButton
import com.github.retrooper.packetevents.protocol.dialog.button.CommonButtonData
import com.github.retrooper.packetevents.protocol.dialog.input.Input
import com.github.retrooper.packetevents.protocol.dialog.input.SingleOptionInputControl
import com.github.retrooper.packetevents.resources.ResourceLocation
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerShowDialog
import net.kyori.adventure.text.Component
import net.md_5.bungee.api.ChatColor
import org.bukkit.entity.Player


fun showDialog(player: Player) {
    //todo config

    val options: List<Pair<String, ChatColor>> = listOf(
        "none" to ChatColor.WHITE,
        "red" to ChatColor.RED,
        "blue" to ChatColor.BLUE,
        "green" to ChatColor.GREEN,
        "cyan" to ChatColor.AQUA,
        "gray" to ChatColor.GRAY
    )

    val inputs = listOf(
        Input(
            "left_shoulder", SingleOptionInputControl(
                500, options.mapIndexed { index, pair ->
                    SingleOptionInputControl.Entry(
                        pair.first, Component.text(pair.first), index == 0
                    )
                }, Component.text("Left Shoulder"), true
            )
        ),
        Input(
            "right_shoulder", SingleOptionInputControl(
                500, options.mapIndexed { index, pair ->
                    SingleOptionInputControl.Entry(
                        pair.first, Component.text(pair.first), index == 0
                    )
                }, Component.text("Right Shoulder"), true
            )
        ),
    )

    val action = ActionButton(
        CommonButtonData(Component.text("submit_parrot"), null, 50),
        DynamicCustomAction(ResourceLocation("parrots:submit_parrot"), null)
    )

    val notice = NoticeDialog(
        CommonDialogData(
            Component.text("Parrot Selection"), null, true, false, DialogAction.CLOSE, emptyList(), inputs
        ), action
    )

    val packet = WrapperPlayServerShowDialog(notice)

    PacketEvents.getAPI().playerManager.sendPacket(player, packet)
}