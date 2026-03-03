package me.kieran.kjcontrol.util;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import me.kieran.kjcontrol.core.KJControl;
import me.kieran.kjcontrol.menu.KJControlMenu;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

/**
 * Utility class for handling Brigadier command executions.
 * Contains the core logic for the root command and the dynamic help menu.
 */
@SuppressWarnings("SameReturnValue")
public class CommandUtil {

    // Private constructor to prevent instantiation of this utility class
    private CommandUtil() {}

    /**
     * Executes the root "/kjcontrol" command.
     * Opens the primary administrative GUI for players.
     * Restricts execution to in-game players, as the console lacks an inventory view.
     *
     * @param ctx The Brigadier command context.
     * @return Command.SINGLE_SUCCESS to indicate successful execution.
     */
    public static int executeMain(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();

        /*
            Pattern matching for instanceof (Java 16+)
            Automatically casts 'sender' to 'player' if the condition is true,
            eliminating the need for boilerplate casting.
         */
        if (sender instanceof Player player) {

            // Only instantiate the menu IF the sender is a player.
            // This prevents wasting memory on GUI creation when console runs the command.
            KJControl plugin = JavaPlugin.getPlugin(KJControl.class);
            KJControlMenu menu = new KJControlMenu(plugin);
            player.openInventory(menu.getInventory());

        } else {
            // Send formatted error message if executed from the server console
            Component errorMsg = MiniMessage.miniMessage().deserialize(
                    "<red>Only players can open the KJControl menu!</red>"
            );
            sender.sendMessage(errorMsg);
        }

        return Command.SINGLE_SUCCESS;
    }

    /**
     * Executes the "/kjcontrol help" subcommand.
     * Generates and displays a dynamic, permission-aware list of available commands.
     *
     * @param ctx The Brigadier command context.
     * @return Command.SINGLE_SUCCESS to indicate successful execution.
     */
    public static int executeHelp(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();
        MiniMessage mm = MiniMessage.miniMessage();

        // 1. Get the original list of all possible help entries
        List<HelpEntry> entries = HelpUtil.getHelpEntries();

        // 2. Filter into a NEW list of entries the user actually has permission for
        List<HelpEntry> allowedEntries = entries.stream()
                .filter(entry -> entry.permission() == null || sender.hasPermission(entry.permission()))
                .toList();

        // 3. Start building the message
        Component message = Component.empty()
                .append(mm.deserialize("<gray>-------- <green>KJControl Help</green> --------</gray>\n"));

        // 4. Check if the user is missing some commands and add the warning if they are
        if (allowedEntries.size() < entries.size()) {
            message = message.append(mm.deserialize(
                    "<yellow><i>This is only showing you the commands you have permission to use!</i></yellow>\n"
            ));
        }

        // 5. Loop through the NEW allowed list to build the lines
        for (HelpEntry entry : allowedEntries) {
            Component line = mm.deserialize(
                    "<green>" + entry.command() + "</green> <gray>- " + entry.description() + "</gray>"
            ).clickEvent(ClickEvent.runCommand(entry.command()))
                    .hoverEvent(HoverEvent.showText(
                            mm.deserialize("<yellow>Click to run!</yellow>"
                                    + (entry.permission() != null
                                    ? "\n<gray>Permission: " + entry.permission() + "</gray>"
                                    : "")
                            )
                    ));
            message = message.append(line).append(Component.newline());
        }

        // 6. Close the menu
        message = message.append(mm.deserialize("<gray>-----------------------------------</gray>"));

        sender.sendMessage(message);
        return Command.SINGLE_SUCCESS;
    }

}
