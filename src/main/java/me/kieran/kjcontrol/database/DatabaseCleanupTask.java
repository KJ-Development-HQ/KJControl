package me.kieran.kjcontrol.database;

import me.kieran.kjcontrol.core.KJControl;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * A scheduled asynchronous task that periodically purges old records
 * from the database to prevent file bloat.
 */
public class DatabaseCleanupTask implements Runnable {

    private final KJControl plugin;

    public DatabaseCleanupTask(KJControl plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        FileConfiguration config = plugin.getConfig();

        boolean enabled = config.getBoolean("database.auto-cleanup.enabled", true);
        int maxAgeDays = config.getInt("database.auto-cleanup.max-age-days", 30);

        if (!enabled) {
            return;
        }

        plugin.getComponentLogger().info("Initiating automated database cleanup (Threshold: {} days)...", maxAgeDays);

        plugin.getLogRepository().purgeOldLogs(maxAgeDays)
                .thenAccept(deletedRows -> {
                    if (deletedRows > 0) {
                        plugin.getComponentLogger().info("Database cleanup complete. Purged {} old records.", deletedRows);
                    } else {
                        plugin.getComponentLogger().info("Database cleanup complete. No records met the deletion threshold.");
                    }
                })
                .exceptionally(throwable -> {
                    plugin.getComponentLogger().error("An error occurred during the automated database cleanup!", throwable);
                    return null;
                });
    }

}
