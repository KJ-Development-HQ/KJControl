package me.kieran.kjcontrol.util;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import me.kieran.kjcontrol.core.KJControl;
import me.kieran.kjcontrol.core.ConfigManager;
import me.kieran.kjcontrol.database.model.ChatLog;
import me.kieran.kjcontrol.database.model.InfractionLog;
import me.kieran.kjcontrol.menu.ConfigMenu;
import me.kieran.kjcontrol.module.KJModule;
import me.kieran.kjcontrol.module.chat.ChatListener;
import me.kieran.kjcontrol.module.chat.format.ChatFormatModule;
import me.kieran.kjcontrol.module.chat.format.ResolvedChatFormat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
    Utility class for executing administrative and player-facing actions.
    Acts as the bridge between the command layer and the business logic/UI layers.
 */
@SuppressWarnings("SameReturnValue")
public final class ActionUtil {

    // Hold a static reference injected at startup
    private static KJControl plugin;

    // Private constructor to prevent instantiation
    private ActionUtil() {}

    /**
     * Initialises the ActionUtil with the main plugin instance.
     * Bypasses Paper's strict ClassLoader checks for testing compatibility.
     *
     * @param pluginInstance The active plugin instance.
     */
    public static void init(KJControl pluginInstance) {
        plugin = pluginInstance;
    }

    /**
     * Resolves the active ConfigManager instance at runtime.
     * Replaces the static Singleton pattern for command execution.
     *
     * @return the active ConfigManager instance.
     */
    private static ConfigManager getConfigManager() {
        if (plugin == null) {
            throw new IllegalStateException("ActionUtil was used accessed before initialisation!");
        }
        return plugin.getConfigManager();
    }

    /*
        ----------------------------------------------------------------------
        System Actions
        ----------------------------------------------------------------------
     */

    /**
        Triggers a reload of all configuration files and provides feedback to the sender.

        @param sender The source requesting the reload.
     */
    public static void reload(CommandSender sender) {
        getConfigManager().loadConfigs(sender);
    }

    // Brigadier wrapper for the reload action.
    public static int reload(CommandContext<CommandSourceStack> ctx) {
        reload(ctx.getSource().getSender());
        return Command.SINGLE_SUCCESS;
    }

    /**
     Opens the configuration GUI for the requesting player.

     @param sender The entity requesting the menu.
     */
    public static void editConfig(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(
                    MiniMessage.miniMessage().deserialize(
                            "<red>The configuration GUI is only accessible in-game.</red>"
                    ));
            return;
        }

        // Initialise and open the provider-based inventory.
        ConfigMenu menu = new ConfigMenu(plugin);
        player.openInventory(menu.getInventory());
    }

    // Brigadier wrapper for the GUI entry point.
    public static int editConfig(CommandContext<CommandSourceStack> ctx) {
        editConfig(ctx.getSource().getSender());
        return Command.SINGLE_SUCCESS;
    }

    /*
        ----------------------------------------------------------------------
        Module-specific Actions
        ----------------------------------------------------------------------
     */

    /**
        Displays a preview of the sender's current chat format.
        Restricts execution to players, as console lacks chat format context.
        Prevents execution if the chat format module is disabled or invalid.

        @param sender The entity requesting the preview.
     */
    public static void preview(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(
                    MiniMessage.miniMessage().deserialize(
                            "<red>Only players can preview chat format!</red>"
                    ));
            return;
        }

        ChatFormatModule module = (ChatFormatModule) getConfigManager().getModule("Chat Format");

        // Defensive check: Ensure the format is actually enabled and loaded before previewing
        if (module == null || !module.isEnabled() || module.getFormat() == null) {
            player.sendMessage(
                    PluginMessagesUtil.format(MiniMessage.miniMessage().deserialize(
                            "<red>Cannot preview: The chat format module is currently disabled or invalid.</red>"
                    ))
            );
            return;
        }

        // Render the format using the module's data container
        ResolvedChatFormat format = module.getFormat().resolve(player, Component.text("Only you can see this preview!"));
        player.sendMessage(format.prefix().append(format.name()).append(format.suffixMessage()));
    }

    // Brigadier wrapper for the preview action.
    public static int preview(CommandContext<CommandSourceStack> ctx) {
        preview(ctx.getSource().getSender());
        return Command.SINGLE_SUCCESS;
    }

    /*
        ----------------------------------------------------------------------
        Database Actions
        ----------------------------------------------------------------------
     */

    public static int lookupPlayer(CommandContext<CommandSourceStack> ctx) {
        String playerName = ctx.getArgument("player", String.class);
        CommandSender sender = ctx.getSource().getSender();

        sender.sendRichMessage("<gray>Fetching chat logs for <yellow>" + playerName + "</yellow>...</gray>");

        plugin.getLogRepository().getChatLogs(playerName, null).thenAccept(logs -> {
            if (logs.isEmpty()) {
                sender.sendRichMessage("<red>No chat logs found for that player.</red>");
                return;
            }
            sender.sendRichMessage("<gold>--- Chat Logs: " + playerName + " ---</gold>");
            for (ChatLog log : logs) {
                sender.sendRichMessage("<dark_gray>[" + log.timestamp() + "]</dark_gray> <white>" + log.message() + "</white>");
            }
        });

        return Command.SINGLE_SUCCESS;
    }

    public static int lookupPlayerTime(CommandContext<CommandSourceStack> ctx) {
        String playerName = ctx.getArgument("player", String.class);
        long timeInMs = ctx.getArgument("time", Long.class);
        CommandSender sender = ctx.getSource().getSender();

        sender.sendRichMessage("<gray>Fetching recent chat logs for <yellow>" + playerName + "</yellow>...</gray>");

        plugin.getLogRepository().getChatLogs(playerName, timeInMs).thenAccept(logs -> {
            if (logs.isEmpty()) {
                sender.sendRichMessage("<red>No recent chat logs found for that player.</red>");
                return;
            }
            sender.sendRichMessage("<gold>--- Recent Chat Logs: " + playerName + " ---</gold>");
            for (ChatLog log : logs) {
                sender.sendRichMessage("<dark_gray>[" + log.timestamp() + "]</dark_gray> <white>" + log.message() + "</white>");
            }
        });

        return Command.SINGLE_SUCCESS;
    }

    public static int violationsTime(CommandContext<CommandSourceStack> ctx) {
        long timeInMs = ctx.getArgument("time", Long.class);
        CommandSender sender = ctx.getSource().getSender();

        plugin.getLogRepository().getInfractions(null, timeInMs).thenAccept(logs -> {
            if (logs.isEmpty()) {
                sender.sendRichMessage("<green>No recent infractions found.</green>");
                return;
            }
            sender.sendRichMessage("<red>--- Recent Violations ---</red>");
            for (InfractionLog log : logs) {
                sender.sendRichMessage(
                        "<dark_gray>[" + log.timestamp() + "]</dark_gray> <yellow>" + log.playerName() +
                                "</yellow> <gray>(<red>" + log.moduleName() +
                                "</red>):</gray> <white>" + log.message() + "</white>"
                );
            }
        });

        return Command.SINGLE_SUCCESS;
    }

    public static int violationsPlayer(CommandContext<CommandSourceStack> ctx) {
        String playerName = ctx.getArgument("player", String.class);
        CommandSender sender = ctx.getSource().getSender();

        plugin.getLogRepository().getInfractions(playerName, null).thenAccept(logs -> {
           if (logs.isEmpty()) {
               sender.sendRichMessage("<green>No infractions found for that player.</green>");
               return;
           }
           sender.sendRichMessage("<red>--- Violations: " + playerName + " ---</red>");
           for (InfractionLog log : logs) {
               sender.sendRichMessage("<dark_gray>[" + log.timestamp() + "]</dark_gray> <gray>(<red>" +
                       log.moduleName() + "</red>):</gray> <white>" + log.message() + "</white>"
               );
           }
        });

        return Command.SINGLE_SUCCESS;
    }

}
