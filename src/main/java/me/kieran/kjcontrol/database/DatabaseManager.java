package me.kieran.kjcontrol.database;

import me.kieran.kjcontrol.core.KJControl;
import me.kieran.kjcontrol.util.PluginMessagesUtil;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/// Handles the lifecycle, configuration, and connection pooling for the plugin's database.
public class DatabaseManager {

    private final KJControl plugin;
    private Connection connection;

    public DatabaseManager(KJControl plugin) {
        this.plugin = plugin;
    }

    /// Initialises the database connection and creates the SQLite file if it doesn't exist.
    @SuppressWarnings({"SqlNoDataSourceInspection", "SqlResolve"})
    public void connect() {
        try {
            File dataFolder = plugin.getDataFolder();
            if (!dataFolder.exists() && !dataFolder.mkdirs()) {
                plugin.getComponentLogger().error("Failed to create plugin data folder for database!");
                return;
            }

            File dbFile = new File(dataFolder, "database.db");
            String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();

            connection = DriverManager.getConnection(url);

            // Enable Write-Ahead Logging (WAL) for significantly better concurrent performance
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("PRAGMA journal_mode=WAL;");
                stmt.execute("PRAGMA synchronous=NORMAL;");
            }

            plugin.getComponentLogger().info("Successfully connected to SQLite database.");
            createTables();

        } catch (SQLException e) {
            plugin.getComponentLogger().error("Failed to connect to the database!");
            plugin.getComponentLogger().error(PluginMessagesUtil.defaultErrorMessage(e));
        }
    }

    /// Generate the schema if it does not already exist.
    @SuppressWarnings({"SqlNoDataSourceInspection", "SqlResolve"})
    private void createTables() {
        String chatLogsTable = """
                CREATE TABLE IF NOT EXISTS chat_logs (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    uuid VARCHAR(36) NOT NULL,
                    player_name VARCHAR(16) NOT NULL,
                    message TEXT NOT NULL,
                    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP
                );
        """;

        String infractionsTable = """
                CREATE TABLE IF NOT EXISTS infractions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    uuid VARCHAR(36) NOT NULL,
                    player_name VARCHAR(16) NOT NULL,
                    module_name VARCHAR(50) NOT NULL,
                    message TEXT NOT NULL,
                    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP
                );
        """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(chatLogsTable);
            stmt.execute(infractionsTable);
        } catch (SQLException e) {
            plugin.getComponentLogger().error("Failed to load database tables!");
            plugin.getComponentLogger().error(PluginMessagesUtil.defaultErrorMessage(e));
        }
    }

    /// @return The active SQLite connection.
    public Connection getConnection() {
        return connection;
    }

    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                plugin.getComponentLogger().info("Disconnected from the SQLite database.");
            }
        } catch (SQLException e) {
            plugin.getComponentLogger().error("Failed to disconnect from the database!");
            plugin.getComponentLogger().error(PluginMessagesUtil.defaultErrorMessage(e));
        }
    }

}
