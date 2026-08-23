package Lightapple.hoplite;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class CustomTrident {

    private final Hoplite plugin;

    public CustomTrident(Hoplite plugin) {
        this.plugin = plugin;
        registerRecipe();
    }

    public ItemStack getTrident() {
        ItemStack item = new ItemStack(Material.TRIDENT);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.displayName(Component.text("Trident", NamedTextColor.WHITE)
                    .decoration(TextDecoration.ITALIC, false));

            meta.lore(List.of(
                    Component.text("A versatile weapon imbued with the", NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false),
                    Component.text("powers of the sea.", NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false)
            ));

            item.setItemMeta(meta);
        }
        return item;
    }

    private void registerRecipe() {
        NamespacedKey key = new NamespacedKey(plugin, "custom_trident");
        ShapedRecipe recipe = new ShapedRecipe(key, getTrident());

        // Crafting pattern: 3 Quartz (head) and 2 Diamonds (diagonal handle)
        recipe.shape(
                " QQ",
                " DQ",
                "D  "
        );
        recipe.setIngredient('Q', Material.QUARTZ);
        recipe.setIngredient('D', Material.DIAMOND);

        Bukkit.addRecipe(recipe);
    }
}