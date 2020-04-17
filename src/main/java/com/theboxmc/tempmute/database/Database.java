package com.theboxmc.tempmute.database;

import org.bukkit.plugin.Plugin;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Represents a generic SQL database
 */
public abstract class Database {

    // Plugin instance
    protected Plugin plugin;
    protected Connection connection;

    protected Database(Plugin plugin) {
        this.plugin = plugin;
    }

    // Start the connection with the DB
    public abstract Connection openConnection() throws SQLException, ClassNotFoundException;

    // Close the connection
    public boolean closeConnection() throws SQLException {
        if (connection == null)
            return false;

        connection.close();
        return true;
    }

    // Check if the connection is open
    public boolean checkConnection() throws SQLException {
        return connection != null && !connection.isClosed();
    }

    // Return the connection
    public Connection getConnection() {
        return connection;
    }
}
