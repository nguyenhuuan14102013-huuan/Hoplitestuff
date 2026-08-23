package Lightapple.hoplite;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;

import java.util.List;

public class Shortbow {

    private final Hoplite plugin;

    public Shortbow(Hoplite plugin) {
        this.plugin = plugin;
        registerRecipe();
    }

    public ItemStack getShortbow() {
        ItemStack item = new ItemStack(Material.BOW);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.displayName(Component.text("Shortbow", NamedTextColor.WHITE)
                    .decoration(TextDecoration.ITALIC, false));

            meta.lore(List.of(
                    Component.text("A cheap bow that helps you into battle", NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false),
                    Component.text("quickly.", NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false)
            ));

            // Modern 1.21.5+ component method for threshold 4
            CustomModelDataComponent cmd = meta.getCustomModelDataComponent();
            cmd.setFloats(List.of(4.0f));
            meta.setCustomModelDataComponent(cmd);

            meta.addEnchant(Enchantment.POWER, 1, true);
            item.setItemMeta(meta);
        }
        return item;
    }

    private void registerRecipe() {
        NamespacedKey key = new NamespacedKey(plugin, "shortbow");
        ShapedRecipe recipe = new ShapedRecipe(key, getShortbow());

        recipe.shape(
                "CCC",
                "CBC",
                "CCC"
        );
        recipe.setIngredient('C', Material.COPPER_INGOT);
        recipe.setIngredient('B', Material.BOW);

        Bukkit.addRecipe(recipe);
    }
}