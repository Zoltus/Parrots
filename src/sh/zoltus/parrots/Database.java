package sh.zoltus.parrots;


import lombok.Getter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.scheduler.BukkitScheduler;
import org.sqlite.SQLiteConfig;
import sh.zoltus.parrots.player.Holder;

import java.sql.*;
import java.util.Map;
import java.util.UUID;

public class Database {

    @Getter
    @Accessors(fluent = true)
    private static Database database;

    private Connection connection;
    private final Parrots plugin;
    private final BukkitScheduler scheduler = Bukkit.getScheduler();
    private final SQLiteConfig config = new SQLiteConfig();

    private Database(Parrots plugin) {
        Bukkit.getConsoleSender().sendMessage("Loading database...");
        this.plugin = plugin;
        this.connection = connection(); //test
        this.config.setJournalMode(SQLiteConfig.JournalMode.WAL);
        this.config.setTempStore(SQLiteConfig.TempStore.MEMORY);
        this.config.setSynchronous(SQLiteConfig.SynchronousMode.OFF);
        createTables();
    }

    public static Database init(Parrots plugin) {
        return database = database == null ? new Database(plugin) : database;
    }

    @SneakyThrows
    private Connection connection() {
        try {
            String url = "jdbc:sqlite:" + plugin.getDataFolder() + "/" + "database.db";
            return connection = connection == null || connection.isClosed() ? DriverManager.getConnection(url, config.toProperties()) : connection;
        } catch (SQLException e) {
            throw new SQLException("§4Database connection failed!\n §c" + e.getMessage());
        }
    }

    //Creates tables if doesnt exist async?
    private void createTables() {
        //@Language("SQLite")
        String table = "CREATE TABLE IF NOT EXISTS Parrots(" +
                        "uuid TEXT NOT NULL UNIQUE, " +
                        "shoulderleft TEXT, " +
                        "shoulderright TEXT, " +
                        "PRIMARY KEY (uuid) " +
                        "); ";
        //Creates table
        try (Connection con = connection()
             ; Statement stmt = con.createStatement()) {
            stmt.execute(table);
        } catch (SQLException e) {
            Bukkit.getConsoleSender().sendMessage("§4Database table creation failed!\n §c" + e.getMessage());
        }
    }

    public void saveUsersAsync() {
        scheduler.runTaskAsynchronously(plugin, this::saveUsers);
    }

    /**
     * Saves users which has been edited to database there has been changes on their data
     */
    public void saveUsers() {
        final String sql = "INSERT OR REPLACE INTO Parrots(uuid, shoulderleft, shoulderright) VALUES(?,?,?)";
        try (Connection con = connection()
             ; PreparedStatement pStm = con.prepareStatement(sql)) {
            Map<UUID, Holder> holders = Holder.getHolders();
            for (Map.Entry<UUID, Holder> entry : holders.entrySet()) {
                UUID uuid = entry.getKey();
                Holder holder = entry.getValue();
                pStm.setString(1, uuid.toString());
                pStm.setString(2, holder.getParrotLeft().toString());
                pStm.setString(3, holder.getParrotRight().toString());
                pStm.addBatch();
                if (!holder.getPlayer().isOnline()) {
                    holders.remove(uuid);
                }
            }
            pStm.executeBatch();
        } catch (SQLException e) {
            Bukkit.getConsoleSender().sendMessage("§4Error saving players!\n §c" + e.getMessage());
        }
    }

    /**
     * Use only on async.
     * Should only be executed async
     *
     * @param offP offlinePlayer
     * @return OneUser
     */
    public boolean loadPlayer(OfflinePlayer offP) {
        String uuid = offP.getUniqueId().toString();
        try (Connection con = connection()
             ; PreparedStatement pStm = con.prepareStatement("SELECT * FROM Parrots WHERE uuid = ?")) {
            pStm.setString(1, uuid);
            try (ResultSet rs = pStm.executeQuery()) {
                if (!rs.next()) { //Goes here if new user onasyncprelogin
                    return false;
                } else {
                    Bukkit.getConsoleSender().sendMessage("§bFrom db");
                   // gson.fromJson(rs.getString("data"), Holder.class);
                    return true;
                }
            }
        } catch (SQLException e) {
            Bukkit.getConsoleSender().sendMessage("§4Error loading user: " + offP.getName() + "\n §c" + e.getMessage());
            return false;
        }
    }



}
