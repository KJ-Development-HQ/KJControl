package me.kieran.kjcontrol.util;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import me.kieran.kjcontrol.core.KJControl;
import me.kieran.kjcontrol.core.ConfigManager;
import me.kieran.kjcontrol.menu.ConfigMenu;
import me.kieran.kjcontrol.module.KJModule;
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
        Dynamic Module Toggling
        ----------------------------------------------------------------------
     */

    /**
        A unified handler for toggling boolean configuration settings.
        Dynamically resolves the target module and handles redundant state checks.

        @param sender        The command executor.
        @param moduleName    The registered name of the module.
        @param isPlural      Determines if the linking verb should be "are" instead of "is".
        @param targetState   The desired boolean state.
     */
    private static void handleToggle(CommandSender sender, String moduleName, boolean isPlural, boolean targetState) {
        KJModule module = getConfigManager().getModule(moduleName);

        if (module == null) {
            sender.sendMessage(PluginMessagesUtil.format(MiniMessage.miniMessage().deserialize(
                    "<red>Error: Module %s is not registered.</red>".formatted(moduleName)
            )));
            return;
        }

        if (targetState == module.isEnabled()) {
            String status = targetState ? "enabled" : "disabled";
            String verb = isPlural ? "are" : "is";

            String errorMessage = "<red>%s %s already %s!</red>".formatted(moduleName, verb, status);
            sender.sendMessage(PluginMessagesUtil.format(MiniMessage.miniMessage().deserialize(errorMessage)));
            return;
        }

        getConfigManager().setModuleState(moduleName, targetState, sender);
    }

    // Handles the logic for the "chat-format-enabled" command branch
    public static int setChatFormatEnabled(CommandContext<CommandSourceStack> ctx) {
        boolean state = BoolArgumentType.getBool(ctx, "state");
        handleToggle(ctx.getSource().getSender(), "Chat Format", false, state);
        return Command.SINGLE_SUCCESS;
    }

    // Handles the logic for the "messages-enabled" command branch
    public static int setMessagesEnabled(CommandContext<CommandSourceStack> ctx) {
        boolean state = BoolArgumentType.getBool(ctx, "state");
        handleToggle(ctx.getSource().getSender(), "Messages", true, state);
        return Command.SINGLE_SUCCESS;
    }

}
