package fi.sulku.mc.parrots

import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.CommandAPIBukkitConfig
import org.bstats.bukkit.Metrics
import org.bukkit.plugin.java.JavaPlugin

class Parrots : JavaPlugin() {

    //todo mysql incase servers want to have on multiple servers
    companion object {
        lateinit var instance: Parrots
            private set
    }

    override fun onLoad() {
        CommandAPI.onLoad(CommandAPIBukkitConfig(this))
        instance = this
    }

    override fun onEnable() {
        CommandAPI.onEnable() // Loads CommandAPI
        Metrics(this, 13235) // bStats ID for Parrots plugin
        ParrotCommand.register() // Register the Parrot command
        server.pluginManager.registerEvents(ParrotManager, this) // Register ParrotManager as an event listener
    }

    override fun onDisable() {
        //todo save
    }
}