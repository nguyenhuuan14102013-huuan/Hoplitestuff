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

public class BundledArrows {

    private final Hoplite plugin;

    public BundledArrows(Hoplite plugin) {
        this.plugin = plugin;
        registerRecipe();
    }

    public ItemStack getBundledArrows() {
        ItemStack item = new ItemStack(Material.ARROW, 20);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.displayName(Component.text("Bundled Arrows", NamedTextColor.WHITE)
                    .decoration(TextDecoration.ITALIC, false));
            item.setItemMeta(meta);
        }
        return item;
    }

    private void registerRecipe() {
        NamespacedKey key = new NamespacedKey(plugin, "bundled_arrows");
        ShapedRecipe recipe = new ShapedRecipe(key, getBundledArrows());

        // Recipe layout: 3 Flint, 3 Sticks, 3 Feathers
        recipe.shape(
                "FFF",
                "SSS",
                "EEE"
        );
        recipe.setIngredient('F', Material.FLINT);
        recipe.setIngredient('S', Material.STICK);
        recipe.setIngredient('E', Material.FEATHER);

        Bukkit.addRecipe(recipe);
    }
}