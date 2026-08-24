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
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class BlazingCrossbow {

    private final Hoplite plugin;
    private final NamespacedKey key;

    public BlazingCrossbow(Hoplite plugin) {
        this.plugin = plugin;
        this.key = new NamespacedKey(plugin, "blazing_crossbow");
        registerRecipe();
    }

    public ItemStack getBlazingCrossbow() {
        ItemStack item = new ItemStack(Material.CROSSBOW);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("Blazing Crossbow", NamedTextColor.GOLD)
                    .decoration(TextDecoration.ITALIC, false));

            meta.addEnchant(Enchantment.QUICK_CHARGE, 1, true);
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);

            // Sets CustomModelData to 3 for the flaming_crossbow texture
            meta.setCustomModelData(3);

            meta.lore(List.of(
                    Component.text("An advanced crossbow that lights", NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false),
                    Component.text("released arrows on fire.", NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false)
            ));

            meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
            item.setItemMeta(meta);
        }
        return item;
    }

    private void registerRecipe() {
        NamespacedKey recipeKey = new NamespacedKey(plugin, "blazing_crossbow_recipe");
        ShapedRecipe recipe = new ShapedRecipe(recipeKey, getBlazingCrossbow());
        recipe.shape("FMF", "GCS", "FMF");

        recipe.setIngredient('F', Material.FIRE_CHARGE);
        recipe.setIngredient('M', Material.MAGMA_CREAM);
        recipe.setIngredient('G', Material.GLOWSTONE_DUST);
        recipe.setIngredient('S', Material.GLOWSTONE_DUST);
        recipe.setIngredient('C', Material.CROSSBOW);

        Bukkit.addRecipe(recipe);
    }

    public NamespacedKey getKey() {
        return key;
    }
}