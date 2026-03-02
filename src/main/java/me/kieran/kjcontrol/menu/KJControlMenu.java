package me.kieran.kjcontrol.menu;

import me.kieran.kjcontrol.core.KJControl;
import me.kieran.kjcontrol.util.MenuUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import org.jetbrains.annotations.NotNull;

/**
 * Represents the primary graphical user interface (GUI) for the KJControl plugin.
 * * Implements {@link InventoryHolder} to serve as a unique identifier during
 * inventory interaction events, ensuring click logic is only applied to this specific menu.
 */
public class KJControlMenu implements InventoryHolder {

    // The constructed Bukkit inventory instance backing this menu.
    private final Inventory menu;

    // Initialise a single-row (9 slots) inventory with a formatted title
    public KJControlMenu(KJControl plugin) {

        Component title = MiniMessage.miniMessage().deserialize(
                "<dark_gray>KJControl Menu</dark_gray>"
        );
        menu = plugin.getServer().createInventory(this, 9, title);

        // Fill all slots first
        for (int i = 0; i < menu.getSize(); i++) {
            menu.setItem(i, MenuUtil.getFillerGlass());
        }
        // Then overwrite specific slots with your actual buttons
        menu.setItem(2, MenuUtil.getReloadBlock());
        menu.setItem(4, MenuUtil.getPreviewFormatBlock());
        menu.setItem(6, MenuUtil.getConfigBlock());
    }

    /**
     * Retrieves the underlying Bukkit inventory instance.
     * * Required by the {@link InventoryHolder} interface for event association and UI rendering.
     *
     * @return The custom {@link Inventory} mapped to this holder.
     */
    @Override
    public @NotNull Inventory getInventory() {
        return menu;
    }
}
