package fi.sulku.mc.parrots

import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.CommandAPIBukkitConfig
import fi.sulku.mc.parrots.command.ParrotCommand
import org.bukkit.plugin.java.JavaPlugin


class Parrots : JavaPlugin() {

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
        // this.metrics = Metrics(this, 12829)
        ParrotCommand.register()
        //Bukkit.getServer().pluginManager.registerEvents(this, this)
    }

    override fun onDisable() {}
}