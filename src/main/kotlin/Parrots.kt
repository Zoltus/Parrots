package fi.sulku.mc.parrots

import com.github.retrooper.packetevents.PacketEvents
import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.CommandAPIBukkitConfig
import fi.sulku.mc.parrots.data.DatabaseManager
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder
import org.bstats.bukkit.Metrics
import org.bukkit.plugin.java.JavaPlugin

class Parrots : JavaPlugin() {

    //todo mysql incase servers want to have on multiple servers
    companion object {
        lateinit var instance: Parrots
            private set
    }

    lateinit var database: DatabaseManager

    override fun onLoad() {
        CommandAPI.onLoad(CommandAPIBukkitConfig(this))
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().load();
        instance = this
    }

    override fun onEnable() {
        PacketEvents.getAPI().init();
        CommandAPI.onEnable() // Loads CommandAPI
        Metrics(this, 13235) // bStats ID for Parrots plugin
        ParrotCommand.register() // Register the Parrot command
        server.pluginManager.registerEvents(ParrotManager, this) // Register ParrotManager as an event listener
        this.database = DatabaseManager.init(this) // Initialize the database manager
    }

    override fun onDisable() {
        this.database.saveAll()
        PacketEvents.getAPI().terminate();
    }
}