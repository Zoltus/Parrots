package fi.sulku.mc.parrots.data

import fi.sulku.mc.parrots.ParrotManager.userParrotData
import fi.sulku.mc.parrots.Parrots
import org.bukkit.Bukkit
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.upsert
import java.util.*

class DatabaseManager(private val plugin: Parrots) {
    private val saveInterval = 5 // ConfigYml.database.saveIntervalMinutes

    companion object {
        fun init(plugin: Parrots): DatabaseManager {
            plugin.logger.info("Loading database...")
            return DatabaseManager(plugin).also {
                it.connect()
                it.createTables()
                it.loadAll()
                it.initAutoSave()
            }
        }
    }

    /**
     * Initialize auto-save using Bukkit scheduler + coroutines
     */
    //todo if autosave is being done sametime as autosave
    fun initAutoSave() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(
            plugin,
            Runnable { saveAllAsync() },
            (saveInterval * 20L) * 60,
            (saveInterval * 20L) * 60
        )
        plugin.logger.info("Auto-save initialized (every $saveInterval minutes)")
    }

    private fun connect() {
        plugin.dataFolder.mkdirs() // Ensure data folder exists
        val config = plugin.config //todo configs for db
        val dbType = "sqlite" //ConfigYml.database.databaseType

        when (dbType) {
            "sqlite" -> {
                Database.connect(
                    url = "jdbc:sqlite:${plugin.dataFolder}/database.db?journal_mode=WAL&foreign_keys=ON",
                    driver = "org.sqlite.JDBC"
                )

                // Configure SQLite settings
                /*transaction {
                    exec("PRAGMA journal_mode=WAL")
                    exec("PRAGMA temp_store=MEMORY")
                    exec("PRAGMA synchronous=NORMAL")
                    exec("PRAGMA foreign_keys=ON")
                }*/
            }

            "mysql" -> {
                val host = config.getString("database.host") ?: "localhost"
                val port = config.getInt("database.port", 3306)
                val database = config.getString("database.name") ?: "parrots"
                val username = config.getString("database.username") ?: "root"
                val password = config.getString("database.password") ?: ""

                Database.connect(
                    url = "jdbc:mysql://$host:$port/$database?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
                    driver = "com.mysql.cj.jdbc.Driver",
                    user = username,
                    password = password
                )
            }

            "postgresql" -> {
                val host = config.getString("database.host") ?: "localhost"
                val port = config.getInt("database.port", 5432)
                val database = config.getString("database.name") ?: "parrots"
                val username = config.getString("database.username") ?: "postgres"
                val password = config.getString("database.password") ?: ""

                Database.connect(
                    url = "jdbc:postgresql://$host:$port/$database",
                    driver = "org.postgresql.Driver",
                    user = username,
                    password = password
                )
            }

            "h2" -> {
                Database.connect(
                    url = "jdbc:h2:${plugin.dataFolder}/database;MODE=MYSQL;DATABASE_TO_LOWER=TRUE",
                    driver = "org.h2.Driver"
                )
            }

            else -> throw IllegalArgumentException("Unsupported database type: $dbType")
        }

        plugin.logger.info("Connected to $dbType database")
    }

    private fun createTables() {
        transaction {
            SchemaUtils.create(ParrotTable)
            plugin.logger.info("Database tables created/verified")
        }
    }

    fun loadAll() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
            loadPlayers()
        })
    }

    private fun loadPlayers() {
        plugin.logger.info("Loading all player data into cache...")
        try {
            transaction {
                ParrotTable.selectAll().forEach { row ->
                    val uuid = UUID.fromString(row[ParrotTable.playerUuid])
                    val parrotData = ParrotData(
                        leftVariant = row[ParrotTable.leftVariant],
                        rightVariant = row[ParrotTable.rightVariant]
                    )
                    userParrotData[uuid] = parrotData
                }
            }
        } catch (e: Exception) {
            plugin.logger.severe("Failed to load player data: ${e.message}")
            e.printStackTrace()
        }
    }


    fun saveAllAsync() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable { saveAll() })
    }

    fun saveAll() {
        transaction {
            userParrotData.forEach { (uuid, parrotData) ->
                ParrotTable.upsert(ParrotTable.playerUuid) {
                    it[ParrotTable.playerUuid] = uuid.toString()
                    it[leftVariant] = parrotData.leftVariant
                    it[rightVariant] = parrotData.rightVariant
                }
            }
        }
        println("Upserted ${userParrotData.size} parrot data entries to database")
    }

    /**
     * Graceful shutdown - ensures all operations complete
     */
    fun shutdown() {
        plugin.logger.info("Shutting down database manager...")

        saveAll() // Save all players sync
        plugin.logger.info("Database manager shutdown complete")
    }
}