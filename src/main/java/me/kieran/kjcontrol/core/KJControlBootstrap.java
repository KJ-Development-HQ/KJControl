package me.kieran.kjcontrol.core;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.bootstrap.PluginProviderContext;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import me.kieran.kjcontrol.command.KJControlCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NonNull;

import java.util.List;

/**
 * Handles the initial bootstrap phase for the KJControl plugin.
 * * This class utilises the Paper {@link PluginBootstrap} API to manage early-lifecycle
 * tasks, such as command registration and dependency configuration, before the
 * standard {@link JavaPlugin} instance is initialised.
 */
public class KJControlBootstrap implements PluginBootstrap {

    /**
     * Entry point for the bootstrap phase.
     * * Registers commands via the {@link LifecycleEventManager} to ensure they
     * correctly injected into the server's Brigadier command tree during the
     * appropriate lifecycle event.
     *
     * @param context The bootstrap context providing access to lifecycle managers.
     */
    @Override
    public void bootstrap(BootstrapContext context) {
        // Register the main command tree during the initial command registration event.
        context.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            commands.registrar().register(
                    KJControlCommand.buildKJControlCommand,
                    "Main plugin command",
                    List.of("kjc")
            );
        });
    }

    /**
     * Instantiates the primary plugin class.
     * * Called by the server after the bootstrap phase has concluded.
     *
     * @param context Contextual information for plugin instantiation.
     * @return A new instance of the {@link KJControl} main class.
     */
    @Override
    public @NonNull JavaPlugin createPlugin(@NonNull PluginProviderContext context) {
        return new KJControl();
    }
}
