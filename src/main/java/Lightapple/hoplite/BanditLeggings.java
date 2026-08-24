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

public class BanditLeggings {

    private final Hoplite plugin;
    private final NamespacedKey key;

    public BanditLeggings(Hoplite plugin) {
        this.plugin = plugin;
        this.key = new NamespacedKey(plugin, "bandit_leggings");
        registerRecipe();
    }

    public ItemStack getBanditLeggings() {
        ItemStack item = new ItemStack(Material.IRON_LEGGINGS);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("Bandit Leggings", NamedTextColor.GREEN)
                    .decoration(TextDecoration.ITALIC, false));

            meta.addEnchant(Enchantment.UNBREAKING, 2, true);
            meta.addEnchant(Enchantment.PROTECTION, 2, true);

            // CustomModelData set to 2 according to model configuration
            meta.setCustomModelData(2);

            meta.lore(List.of(
                    Component.text("Crafted from the treasures of a bandit.", NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false),
                    Component.text("Gain 50% more golden nuggets on a kill.", NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false),
                    Component.empty(),
                    Component.text("Equivalent to Iron Leggings.", NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false)
            ));

            meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
            item.setItemMeta(meta);
        }
        return item;
    }

    private void registerRecipe() {
        NamespacedKey recipeKey = new NamespacedKey(plugin, "bandit_leggings_recipe");
        ShapedRecipe recipe = new ShapedRecipe(recipeKey, getBanditLeggings());
        recipe.shape(" M ", "GIG", " M ");

        recipe.setIngredient('M', Material.GLISTERING_MELON_SLICE);
        recipe.setIngredient('G', Material.GOLD_INGOT);
        recipe.setIngredient('I', Material.IRON_LEGGINGS);

        Bukkit.addRecipe(recipe);
    }

    public NamespacedKey getKey() {
        return key;
    }
}