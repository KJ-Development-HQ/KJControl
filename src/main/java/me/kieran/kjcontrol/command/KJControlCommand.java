package me.kieran.kjcontrol.command;

import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import me.kieran.kjcontrol.command.argument.DatabasePlayerArgument;
import me.kieran.kjcontrol.command.argument.TimeArgument;
import me.kieran.kjcontrol.core.KJControl;
import me.kieran.kjcontrol.util.ActionUtil;
import me.kieran.kjcontrol.util.CommandUtil;

import javax.swing.*;

/**
 * Defines the command hierarchy and permission nodes for the "/kjcontrol" root command.
 * This class follows a declarative structure using the Brigadier API to map
 * command syntax to execution handlers defined in utility classes.
 */
public class KJControlCommand {

    /**
     * Builds the immutable command tree for the primary administrative command.
     *
     * @param plugin The main plugin instance, required for database-driven argument suggestions.
     * @return The fully constructed command node ready for registration.
     */
    public static LiteralCommandNode<CommandSourceStack> build(KJControl plugin) {
        return Commands.literal("kjcontrol")
                .requires(sender -> sender.getSender().hasPermission("kjcontrol.admin"))
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
                )

                /*
                    Subcommand: /kjcontrol lookup <player> [time]
                    Retrieves standard chat history from the database.
                 */
                .then(Commands.literal("lookup")
                        .requires(sender -> sender.getSender().hasPermission("kjcontrol.lookup"))
                        .then(Commands.argument("player", new DatabasePlayerArgument(plugin, "chat_logs"))
                                .executes(ActionUtil::lookupPlayer)
                                .then(Commands.argument("time", new TimeArgument())
                                        .executes(ActionUtil::lookupPlayerTime)
                                )
                        )
                )

                /*
                    Subcommand: /kjcontrol violations
                    Shows recent violations, either totally or just for a player
                 */
                .then(Commands.literal("violations")
                        .requires(sender -> sender.getSender().hasPermission("kjcontrol.violations"))
                        .then(Commands.literal("recent")
                                .then(Commands.argument("time", new TimeArgument())
                                    .executes(ActionUtil::violationsTime)
                            )
                        )
                        .then(Commands.literal("player")
                            .then(Commands.argument("player", new DatabasePlayerArgument(plugin, "infractions"))
                                    .executes(ActionUtil::violationsPlayer)
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
}
