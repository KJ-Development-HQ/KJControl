package me.kieran.kjcontrol.database;

import me.kieran.kjcontrol.core.KJControl;
import me.kieran.kjcontrol.database.model.ChatLog;
import me.kieran.kjcontrol.database.model.InfractionLog;
import me.kieran.kjcontrol.util.PluginMessagesUtil;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/// Handles all asynchronous CRUD operations for the plugin's logging tables.
@SuppressWarnings({"SqlNoDataSourceInspection", "SqlResolve"})
public class LogRepository {

    private final KJControl plugin;
    private final DatabaseManager dbManager;

    public LogRepository(KJControl plugin, DatabaseManager dbManager) {
        this.plugin = plugin;
        this.dbManager = dbManager;
    }

    /**
     * Asynchronously logs a raw chat message to the database.
     *
     * @param uuid       The UUID of the player.
     * @param playerName The name of the player.
     * @param message    The unfiltered message sent.
     * @return A {@link CompletableFuture} representing the async task.
     */
    public CompletableFuture<Void> logChat(UUID uuid, String playerName, String message) {
        return CompletableFuture.runAsync(() -> {
            String sql = "INSERT INTO chat_logs (uuid, player_name, message) VALUES (?, ?, ?)";

            // We try-with-resources the PreparedStatement to prevent memory leaks,
            // but we DO NOT close the main dbManager connection!
            try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
                pstmt.setString(1, uuid.toString());
                pstmt.setString(2, playerName);
                pstmt.setString(3, message);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getComponentLogger().error("Failed to log chat to database!");
                plugin.getComponentLogger().error(PluginMessagesUtil.defaultErrorMessage(e));
            }
        });
    }

    /**
     * Asynchronously logs a triggered filter infraction to the database.
     *
     * @param uuid       The UUID of the player.
     * @param playerName The name of the player.
     * @param moduleName The name of the module that caught the message (e.g., "Blacklist").
     * @param message    The raw, unfiltered message that triggered the violation.
     * @return A {@link CompletableFuture} representing the async task.
     */
    public CompletableFuture<Void> logInfraction(UUID uuid, String playerName, String moduleName, String message) {
        return CompletableFuture.runAsync(() -> {
           String sql = "INSERT INTO infractions (uuid, player_name, module_name, message) VALUES (?, ?, ?, ?)";

           try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
               pstmt.setString(1, uuid.toString());
               pstmt.setString(2, playerName);
               pstmt.setString(3, moduleName);
               pstmt.setString(4, message);
               pstmt.executeUpdate();
           } catch (SQLException e) {
               plugin.getComponentLogger().error("Failed to log infraction to database!");
               plugin.getComponentLogger().error(PluginMessagesUtil.defaultErrorMessage(e));
           }
        });
    }

    /**
     * Asynchronously deletes records older than the specified number of days.
     *
     * @param daysOld The age threshold for deletion.
     * @return A {@link CompletableFuture} containing the total number of deleted rows.
     */
    public CompletableFuture<Integer> purgeOldLogs(int daysOld) {
        return CompletableFuture.supplyAsync(() -> {
           String chatSql = "DELETE FROM chat_logs WHERE timestamp <= datetime('now', ?)";
           String infractionSql = "DELETE FROM infractions WHERE timestamp <= datetime('now', ?)";
           String modifier = "-" + daysOld + "days";
           int deletedRows = 0;

           try {
               try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(chatSql)) {
                   pstmt.setString(1, modifier);
                   deletedRows += pstmt.executeUpdate();
               }
               try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(infractionSql)) {
                   pstmt.setString(1, modifier);
                   deletedRows += pstmt.executeUpdate();
               }
           } catch (SQLException e) {
               plugin.getComponentLogger().error("Failed to purge old database logs!");
               plugin.getComponentLogger().error(PluginMessagesUtil.defaultErrorMessage(e));
           }

           return deletedRows;
        });
    }

    /**
     * Asynchronously retrieves a list of distinct player names matching a prefix.
     * Used for Brigadier command auto-completions.
     *
     * @param targetTable The table to search (e.g., "chat_logs" or "infractions").
     * @param prefix      The partial name the player has typed so far.
     * @return A {@link CompletableFuture} containing a list of up to 10 matching names.
     */
    @SuppressWarnings("SqlSourceToSinkFlow")
    public CompletableFuture<List<String>> getLoggedPlayers(String targetTable, String prefix) {
        return CompletableFuture.supplyAsync(() -> {
           List<String> names = new ArrayList<>();
           String sql = "SELECT DISTINCT player_name FROM " + targetTable + " WHERE LOWER(player_name) LIKE ? LIMIT 10";

           try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
               pstmt.setString(1, prefix.toLowerCase() + "%");
               try (ResultSet rs = pstmt.executeQuery()) {
                   while (rs.next()) {
                       names.add(rs.getString("player_name"));
                   }
               }
           } catch (SQLException e) {
               plugin.getComponentLogger().error("Failed to fetch player suggestions from {}!", targetTable);
               plugin.getComponentLogger().error(PluginMessagesUtil.defaultErrorMessage(e));
           }
           return names;
        });
    }

    /**
     * Asynchronously retrieves chat logs.
     *
     * @param playerName The player to filter by (null for all players)
     * @param timeMs     The timeframe in milliseconds to look back (null for all time).
     * @return A {@link CompletableFuture} containing up to 100 recent chat logs.
     */
    public CompletableFuture<List<ChatLog>> getChatLogs(String playerName, Long timeMs) {
        return CompletableFuture.supplyAsync(() -> {
           List<ChatLog> results = new ArrayList<>();
           StringBuilder sql = new StringBuilder("SELECT player_name, message, timestamp FROM chat_logs WHERE 1=1");

           if (playerName != null) sql.append(" AND LOWER(player_name) = LOWER(?)");
           if (timeMs != null) sql.append(" AND timestamp >= datetime('now', ?)");
           sql.append(" ORDER BY id DESC LIMIT 100");

           try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql.toString())) {
               int paramIndex = 1;

               if (playerName != null) {
                   pstmt.setString(paramIndex++, playerName);
               }
               if (timeMs != null) {
                   pstmt.setString(paramIndex, "-" + (timeMs / 1000) + " seconds");
               }

               try (ResultSet rs = pstmt.executeQuery()) {
                   while (rs.next()) {
                       results.add(new ChatLog(
                               rs.getString("player_name"),
                               rs.getString("message"),
                               rs.getString("timestamp")
                       ));
                   }
               }
           } catch (SQLException e) {
               plugin.getComponentLogger().error("Failed to fetch chat logs!");
               plugin.getComponentLogger().error(PluginMessagesUtil.defaultErrorMessage(e));
           }
           return results;
        });
    }

    /**
     * Asynchronously retrieves filter infractions.
     *
     * @param playerName The player to filter by (null for all players).
     * @param timeMs     The timeframe in milliseconds to look back (null for all time).
     * @return A {@link CompletableFuture} containing up to 100 recent infractions.
     */
    public CompletableFuture<List<InfractionLog>> getInfractions(String playerName, Long timeMs) {
        return CompletableFuture.supplyAsync(() -> {
           List<InfractionLog> results = new ArrayList<>();
           StringBuilder sql = new StringBuilder("SELECT player_name, module_name, message, timestamp FROM infractions WHERE 1=1");

           if (playerName != null) sql.append(" AND LOWER(player_name) = LOWER(?)");
           if (timeMs != null) sql.append(" AND timestamp >= datetime('now', ?)");
           sql.append(" ORDER BY id DESC LIMIT 100");

           try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql.toString())) {
               int paramIndex = 1;

               if (playerName != null) {
                   pstmt.setString(paramIndex++, playerName);
               }
               if (timeMs != null) {
                   pstmt.setString(paramIndex, "-" + (timeMs / 1000) + " seconds");
               }

               try (ResultSet rs = pstmt.executeQuery()) {
                   while (rs.next()) {
                       results.add(new InfractionLog(
                               rs.getString("player_name"),
                               rs.getString("module_name"),
                               rs.getString("message"),
                               rs.getString("timestamp")
                       ));
                   }
               }
           } catch (SQLException e) {
               plugin.getComponentLogger().error("Failed to fetch infraction logs!");
               plugin.getComponentLogger().error(PluginMessagesUtil.defaultErrorMessage(e));
           }
           return results;
        });
    }

}
