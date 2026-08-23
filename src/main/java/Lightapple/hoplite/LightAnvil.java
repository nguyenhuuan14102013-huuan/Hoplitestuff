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

public class LightAnvil {

    private final Hoplite plugin;

    public LightAnvil(Hoplite plugin) {
        this.plugin = plugin;
        registerRecipe();
    }

    public ItemStack getLightAnvil() {
        ItemStack item = new ItemStack(Material.ANVIL);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.displayName(Component.text("Light Anvil", NamedTextColor.WHITE)
                    .decoration(TextDecoration.ITALIC, false));

            meta.lore(List.of(
                    Component.text("A lightweight anvil that requires less", NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false),
                    Component.text("resources to craft.", NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false)
            ));

            item.setItemMeta(meta);
        }
        return item;
    }

    private void registerRecipe() {
        NamespacedKey key = new NamespacedKey(plugin, "light_anvil");
        ShapedRecipe recipe = new ShapedRecipe(key, getLightAnvil());

        // Recipe layout: 6 Iron Ingots + 1 Iron Block
        recipe.shape(
                "III",
                " B ",
                "III"
        );
        recipe.setIngredient('I', Material.IRON_INGOT);
        recipe.setIngredient('B', Material.IRON_BLOCK);

        Bukkit.addRecipe(recipe);
    }
}