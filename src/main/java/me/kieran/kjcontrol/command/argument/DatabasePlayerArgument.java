package me.kieran.kjcontrol.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import me.kieran.kjcontrol.core.KJControl;
import org.jspecify.annotations.NullMarked;

import java.util.concurrent.CompletableFuture;

/// Custom Brigadier argument that suggests player names dynamically from the SQLite database.
@NullMarked
public class DatabasePlayerArgument implements CustomArgumentType<String, String> {

    private final KJControl plugin;
    private final String targetTable;

    public DatabasePlayerArgument(KJControl plugin, String targetTable) {
        this.plugin = plugin;
        this.targetTable = targetTable;
    }

    @Override
    public String parse(StringReader reader) {
        // We accept whatever they type as a string.
        // We will validate if the player has logs inside the ActionUtil executor.
        return reader.readUnquotedString();
    }

    @Override
    public ArgumentType<String> getNativeType() {
        return StringArgumentType.word();
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        String input = builder.getRemainingLowerCase();

        // Ask the repository for the names, then push them into the Brigadier builder
        return plugin.getLogRepository().getLoggedPlayers(targetTable, input)
                .thenApply(names -> {
                    for (String name : names) {
                        builder.suggest(name);
                    }
                    return builder.build();
                });
    }

}
