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

public class CustomTotem {

    private final Hoplite plugin;

    public CustomTotem(Hoplite plugin) {
        this.plugin = plugin;
        registerRecipe();
    }

    public ItemStack getTotem() {
        ItemStack item = new ItemStack(Material.TOTEM_OF_UNDYING);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.displayName(Component.text("Totem of Undying", NamedTextColor.WHITE)
                    .decoration(TextDecoration.ITALIC, false));

            meta.lore(List.of(
                    Component.text("A spiritually bound artifact that when", NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false),
                    Component.text("held, can reverse the effects of a", NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false),
                    Component.text("fatal blow.", NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false)
            ));

            item.setItemMeta(meta);
        }
        return item;
    }

    private void registerRecipe() {
        NamespacedKey key = new NamespacedKey(plugin, "custom_totem");
        ShapedRecipe recipe = new ShapedRecipe(key, getTotem());

        // Cross pattern with Ghast Tear in the center
        recipe.shape(
                " G ",
                "GTG",
                " G "
        );
        recipe.setIngredient('G', Material.GOLD_INGOT);
        recipe.setIngredient('T', Material.GHAST_TEAR);

        Bukkit.addRecipe(recipe);
    }
}