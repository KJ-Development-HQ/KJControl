package me.kieran.kjcontrol.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

/**
 * Utility class for constructing and formatting GUI items.
 * Centralises ItemStack configuration to ensure consistent visual
 * styling across all plugin menus.
 */
public final class MenuUtil {

    // Private constructor to prevent instantiation of utility class
    private MenuUtil() {}

    /**
     * Generic factory method for creating standardised menu icons.
     *
     * @param material     The base item material.
     * @param displayName  The formatted title shown in the tooltip.
     * @param lore         A list of formatted description lines.
     * @param isGlowing    Whether to apply a visual enchantment glint.
     * @return A fully configured ItemStack ready for GUI placement.
     */
    public static ItemStack createBlock(Material material, Component displayName,
                                        List<Component> lore, boolean isGlowing) {
        ItemStack block = ItemStack.of(material);
        ItemMeta meta = block.getItemMeta();

        if (meta != null) {
            meta.displayName(displayName);
            meta.lore(lore);

            if (isGlowing) meta.setEnchantmentGlintOverride(true);

            block.setItemMeta(meta);
        }

        return block;
    }

    /**
     * Creates a blank, unnamed glass pane used to fill empty GUI slots.
     * Prevents players from placing their own items into the menu.
     *
     * @return A black stained-glass pane with no name.
     */
    public static ItemStack getFillerGlass() {
        return createBlock(
                Material.LIGHT_GRAY_STAINED_GLASS_PANE,
                Component.empty(),
                List.of(),
                false
        );
    }

    /**
     * ----------------------------------------------------------------------
     * Predefined Menu Icons
     * ----------------------------------------------------------------------
     */

    public static ItemStack getReloadBlock() {
        return createBlock(
                Material.EMERALD_BLOCK,
                Component.text("Reload KJControl", NamedTextColor.GREEN),
                List.of(
                        Component.text("Reloads the plugin!", NamedTextColor.GRAY)
                ),
                true
        );
    }

    public static ItemStack getPreviewFormatBlock() {
        return createBlock(
                Material.DIAMOND_BLOCK,
                Component.text("Preview Chat Format", NamedTextColor.AQUA),
                List.of(
                        Component.text(
                                "Shows you what your chat will look like!",
                                NamedTextColor.GRAY
                        )
                ),
                true
        );
    }

    public static ItemStack getConfigBlock() {
        return createBlock(
                Material.IRON_BLOCK,
                Component.text("Edit config settings", NamedTextColor.LIGHT_PURPLE),
                List.of(
                        Component.text(
                                "Opens a config GUI with options to",
                                NamedTextColor.GRAY
                        ),
                        Component.text(
                                "change different fields.",
                                NamedTextColor.GRAY
                        )
                ),
                true
        );
    }

}
