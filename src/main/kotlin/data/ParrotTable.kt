package fi.sulku.mc.parrots.data

import org.bukkit.entity.Parrot
import org.jetbrains.exposed.v1.core.Table

object ParrotTable : Table("parrots") {
    val playerUuid = varchar("player_uuid", 36)
    val leftVariant = enumerationByName<Parrot.Variant>("left_variant", 20).nullable()
    val rightVariant = enumerationByName<Parrot.Variant>("right_variant", 20).nullable()

    override val primaryKey = PrimaryKey(playerUuid)
}