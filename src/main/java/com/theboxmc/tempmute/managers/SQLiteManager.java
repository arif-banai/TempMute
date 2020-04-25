package com.theboxmc.tempmute.managers;

import me.arifbanai.bukkitSQL.Database;
import me.arifbanai.bukkitSQL.sqlite.SQLite;
import com.theboxmc.tempmute.exceptions.PlayerNotFoundException;
import com.theboxmc.tempmute.interfaces.TempMuteCallback;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

public class SQLiteManager {

    protected Database db;
    protected JavaPlugin plugin;

    PreparedStatement insertPlayerStatement;
    PreparedStatement deletePlayerStatement;
    PreparedStatement selectPlayerStatement;

    public SQLiteManager(final JavaPlugin plugin) throws SQLException {
        this.plugin = plugin;
    }

    public void setupDb() throws SQLException, ClassNotFoundException {
        db = new SQLite(plugin, "TempMute.db");
        db.openConnection();

        Statement statement = db.getConnection().createStatement();
        statement.executeUpdate(
                "CREATE TABLE IF NOT EXISTS muted ("
                        + "playerUUID TEXT PRIMARY KEY,"
                        + "mutedUntilMilli BIGINT NOT NULL"
                        + ");");

        insertPlayerStatement = db.getConnection().prepareStatement("INSERT INTO " + "muted(playerUUID,mutedUntilMilli)" + "VALUES(?,?)");
        deletePlayerStatement = db.getConnection().prepareStatement("DELETE FROM muted WHERE " + "playerUUID = ?");
        selectPlayerStatement = db.getConnection().prepareStatement("SELECT mutedUntilMilli FROM muted WHERE " + "playerUUID = ?");
    }

    public long getMuteTime(UUID playerUUID) throws SQLException, PlayerNotFoundException {
        selectPlayerStatement.setString(1, playerUUID.toString());

        ResultSet rs = selectPlayerStatement.executeQuery();
        if(rs.next()) {
            long timeMutedUntilMilliseconds =  rs.getLong("mutedUntilMilli");
            if(timeMutedUntilMilliseconds == 0) {
                throw new PlayerNotFoundException();
            }

            return timeMutedUntilMilliseconds;
        }

        throw new PlayerNotFoundException();
    }

    public void addPlayer(UUID playerUUID, long timeInMilliseconds) throws SQLException {
        insertPlayerStatement.setString(1, playerUUID.toString());
        insertPlayerStatement.setLong(2, timeInMilliseconds);
        insertPlayerStatement.executeUpdate();
    }

    public void deletePlayer(UUID playerUUID) throws SQLException {
        deletePlayerStatement.setString(1, playerUUID.toString());
        deletePlayerStatement.executeUpdate();
    }

    public void doAsyncGetMuteTime(final UUID playerUUID, final TempMuteCallback<Long> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                try {
                    final long mutedUntilMilli = getMuteTime(playerUUID);
                    Bukkit.getScheduler().runTask(plugin, new Runnable() {
                        @Override
                        public void run() {
                            callback.onSuccess(mutedUntilMilli);
                        }
                    });
                } catch (SQLException | PlayerNotFoundException e) {
                    callback.onFailure(e);
                }
            }
        });
    }

    public void doAsyncAddPlayer(final UUID playerUUID, final long timeInMilliseconds, final TempMuteCallback<Void> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                try {
                    addPlayer(playerUUID, timeInMilliseconds);
                    Bukkit.getScheduler().runTask(plugin, new Runnable() {
                        @Override
                        public void run() {
                            callback.onSuccess(null);
                        }
                    });
                } catch (SQLException e) {
                    callback.onFailure(e);
                }
            }
        });
    }

    public void doAsyncDeletePlayer(final UUID playerUUID, final TempMuteCallback<Void> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                try {
                    deletePlayer(playerUUID);
                    Bukkit.getScheduler().runTask(plugin, new Runnable() {
                        @Override
                        public void run() {
                            callback.onSuccess(null);
                        }
                    });
                } catch (SQLException e) {
                    callback.onFailure(e);
                }
            }
        });
    }
}
