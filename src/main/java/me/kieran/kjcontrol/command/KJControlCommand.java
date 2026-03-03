package me.kieran.kjcontrol.command;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import me.kieran.kjcontrol.util.ActionUtil;
import me.kieran.kjcontrol.util.CommandUtil;

/**
 * Defines the command hierarchy and permission nodes for the "/kjcontrol" root command.
 * * This class follows a declarative structure using the Brigadier API to map
 * command syntax to execution handlers defined in utility classes.
 */
@SuppressWarnings("unused")
public class KJControlCommand {

    /**
     * The immutable command tree for the primary administrative command.
     * Initialised as a static node for registration during the bootstrap phase.
     */
    public static final LiteralCommandNode<CommandSourceStack> KJC_COMMAND_NODE = Commands.literal("kjcontrol")

            // Restricts root command access to users with administrative privileges.
            .requires(sender -> sender.getSender().hasPermission("kjcontrol.admin"))

            // Default execution path for "/kjcontrol"
            .executes(CommandUtil::executeMain)

            /*
                Subcommand: /kjcontrol preview
                Triggers a visual representation of the active chat formatting.
             */
            .then(Commands.literal("preview")
                    .requires(sender -> sender.getSender().hasPermission("kjcontrol.preview"))
                    .executes(ActionUtil::preview)
            )

            /*
                Subcommand: /kjcontrol config
                Entry point for configuration management via GUI or targeted arguments.
             */
            .then(Commands.literal("config")
                    .requires(sender -> sender.getSender().hasPermission("kjcontrol.config"))
                    .executes(ActionUtil::editConfig)

                    // Logic branch for toggling the chat format module
                    .then(Commands.literal("chat-format-enabled")
                            .then(Commands.argument("state", BoolArgumentType.bool())
                                    .executes(ActionUtil::setChatFormatEnabled)
                            )
                    )

                    // Logic branch for toggling general message systems.
                    .then(Commands.literal("messages-enabled")
                            .then(Commands.argument("state", BoolArgumentType.bool())
                                    .executes(ActionUtil::setMessagesEnabled)
                            )
                    )
            )

            /*
                Subcommand: /kjcontrol reload
                Invalidates caches and reloads configuration files from disk.
             */
            .then(Commands.literal("reload")
                    .requires(sender -> sender.getSender().hasPermission("kjcontrol.reload"))
                    .executes(ActionUtil::reload)
            )

            /*
                Subcommand: /kjcontrol help
                Generates a permission-aware help menu.
             */
            .then(Commands.literal("help")
                    .executes(CommandUtil::executeHelp)
            )
            .build();
}
