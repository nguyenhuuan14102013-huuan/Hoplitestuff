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
import org.bukkit.inventory.meta.components.CustomModelDataComponent;

import java.util.List;

public class Shortsword {

    private final Hoplite plugin;

    public Shortsword(Hoplite plugin) {
        this.plugin = plugin;
        registerRecipe();
    }

    public ItemStack getShortsword() {
        ItemStack item = new ItemStack(Material.IRON_SWORD);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.displayName(Component.text("Shortsword", NamedTextColor.WHITE)
                    .decoration(TextDecoration.ITALIC, false));

            meta.lore(List.of(
                    Component.text("A fast, compact blade designed for", NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false),
                    Component.text("close-quarters combat.", NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false)
            ));

            // Modern 1.21.5+ component method for threshold 2
            CustomModelDataComponent cmd = meta.getCustomModelDataComponent();
            cmd.setFloats(List.of(2.0f));
            meta.setCustomModelDataComponent(cmd);

            item.setItemMeta(meta);
        }
        return item;
    }

    private void registerRecipe() {
        NamespacedKey key = new NamespacedKey(plugin, "shortsword");
        ShapedRecipe recipe = new ShapedRecipe(key, getShortsword());

        recipe.shape(
                "CCC",
                "CIC",
                "CCC"
        );
        recipe.setIngredient('C', Material.COPPER_INGOT);
        recipe.setIngredient('I', Material.IRON_SWORD);

        Bukkit.addRecipe(recipe);
    }
}