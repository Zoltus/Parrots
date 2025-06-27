package fi.sulku.mc.parrots.data

import org.bukkit.entity.Parrot

//Todo custom arg?
data class ParrotData(
    var leftVariant: Parrot.Variant? = null,
    var rightVariant: Parrot.Variant? = null
)