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
import fi.sulku.mc.parrots.ParrotManager.userParrotData
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import org.bukkit.entity.Parrot
import org.bukkit.entity.Player

fun showDialog(player: Player) {
    val currentData = userParrotData[player.uniqueId]

    val inputs = listOf(
        createShoulderInput("left_shoulder", "Left Shoulder", currentData?.leftVariant),
        createShoulderInput("right_shoulder", "Right Shoulder", currentData?.rightVariant)
    )

    val dialog = NoticeDialog(
        CommonDialogData(Component.text("Parrot Selection"), null, true, false, DialogAction.CLOSE, emptyList(), inputs),
        ActionButton(
            CommonButtonData(Component.text("Apply Changes"), null, 120),
            DynamicCustomAction(ResourceLocation("parrots:submit_parrot"), null)
        )
    )

    PacketEvents.getAPI().playerManager.sendPacket(player, WrapperPlayServerShowDialog(dialog))
}

private fun createShoulderInput(id: String, label: String, currentVariant: Parrot.Variant?): Input {
    val options = ParrotOption.entries.map { it.toInputEntry(currentVariant) }
    return Input(id, SingleOptionInputControl(200, options, Component.text(label), true))
}

private enum class ParrotOption(val displayName: String, val color: TextColor, val variant: Parrot.Variant?) {
    NONE("None", NamedTextColor.WHITE, null),
    RED("Red", NamedTextColor.RED, Parrot.Variant.RED),
    BLUE("Blue", NamedTextColor.BLUE, Parrot.Variant.BLUE),
    GREEN("Green", NamedTextColor.GREEN, Parrot.Variant.GREEN),
    CYAN("Cyan", NamedTextColor.AQUA, Parrot.Variant.CYAN),
    GRAY("Gray", NamedTextColor.GRAY, Parrot.Variant.GRAY);

    fun toInputEntry(currentVariant: Parrot.Variant?) =
        SingleOptionInputControl.Entry(
            name.lowercase(),
            Component.text(displayName).color(color),
            this.variant == currentVariant
        )
}