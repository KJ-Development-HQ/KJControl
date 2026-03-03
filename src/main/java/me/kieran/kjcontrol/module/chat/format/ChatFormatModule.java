package me.kieran.kjcontrol.module.chat.format;

import me.kieran.kjcontrol.core.KJControl;
import me.kieran.kjcontrol.module.AbstractModule;
import me.kieran.kjcontrol.module.chat.ChatPipeline;
import org.bukkit.configuration.file.FileConfiguration;

/// Module responsible for managing the chat formatting feature.
public class ChatFormatModule extends AbstractModule {

    private final ChatPipeline pipeline;
    private ChatFormat loadedFormat;

    public ChatFormatModule(KJControl plugin, ChatPipeline pipeline) {
        super(plugin, "Chat Format", "features.enable-chat-format", "modules/chat-format.yml", 2);
        this.pipeline = pipeline;
    }

    @Override
    protected void onEnable() {
        pipeline.setFormatter(this);
    }

    @Override
    protected void onDisable() {
        loadedFormat = null;
        pipeline.removeFormatter();
    }

    @Override
    protected boolean onConfigLoad(FileConfiguration config) {
        loadedFormat = new ChatFormat(
                config.getString("format.prefix"),
                config.getString("format.name"),
                config.getString("format.name_hover"),
                config.getString("format.name_click"),
                config.getString("format.suffix")
        );

        if (loadedFormat.isInvalid()) {
            plugin.getComponentLogger().error(
                    "chat-format.yml is invalid. Chat formatting has been disabled."
            );
            loadedFormat = null;
            return false;
        }

        if (loadedFormat.name().isEmpty()) {
            plugin.getComponentLogger().warn("chat-format.yml: format.name is empty - chat names will be blank");
        }

        return true;
    }

    /**
     * Retrieves the active chat format data container.
     * Replaces the old package-private getRawFormat() method.
     *
     * @return The active {@link ChatFormat}, or null if disabled/invalid.
     */
    public ChatFormat getFormat() {
        return loadedFormat;
    }

}
