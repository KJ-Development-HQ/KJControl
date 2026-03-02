package me.kieran.kjcontrol.menu;

import me.kieran.kjcontrol.core.KJControl;
import me.kieran.kjcontrol.core.ConfigManager;
import me.kieran.kjcontrol.module.KJModule;
import me.kieran.kjcontrol.util.MenuUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the configuration editor GUI for the KJControl plugin.
 * Dynamically builds itself based on the loaded modules in ConfigManager.
 */
public class ConfigMenu implements InventoryHolder {

    // The constructed Bukkit inventory instance backing this menu.
    private final Inventory menu;

    /**
     * Constructs the configuration menu and dynamically populates its layout
     * based on the current active state of the plugin's features.
     *
     * @param plugin The main plugin instance used for server context and inventory creation.
     */
    public ConfigMenu(KJControl plugin) {

        // Initialise a 3-row (27 slots) inventory with a formatted title
        Component title = MiniMessage.miniMessage().deserialize(
                "<dark_purple>Config Editor</dark_purple>"
        );
        menu = plugin.getServer().createInventory(this, 27, title);

        // Populate all empty slots with filler glass to prevent accidental item drops.
        for (int i = 0; i < menu.getSize(); i++) {
            menu.setItem(i, MenuUtil.getFillerGlass());
        }

        ConfigManager configManager = plugin.getConfigManager();

        // An array of visually pleasing slots in the middle row of a 27-slot inventory
        int[] displaySlots = {10, 11, 12, 13, 14, 15, 16};
        int currentSlotIndex = 0;

        // Loop through every single registered module and drop its icon into the menu
        for (KJModule module : configManager.getModules()) {
            // Safety check: Don't exceed our allowed display slots
            if (currentSlotIndex >= displaySlots.length) {
                plugin.getComponentLogger().warn("Too many modules to display in the current ConfigMenu layout!");
                break;
            }

            menu.setItem(displaySlots[currentSlotIndex], module.getDisplayItem());
            currentSlotIndex++;
        }
    }

    /**
     * Retrieves the underlying Bukkit inventory instance.
     * * Required by the {@link InventoryHolder} interface for event association.
     *
     * @return The custom {@link Inventory} mapped to this holder.
     */
    @Override
    public @NotNull Inventory getInventory() {
        return menu;
    }
}
