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

public class SkeletonLeggings {

    private final Hoplite plugin;
    private final NamespacedKey key;

    public SkeletonLeggings(Hoplite plugin) {
        this.plugin = plugin;
        this.key = new NamespacedKey(plugin, "skeleton_leggings");
        registerRecipe();
    }

    public ItemStack getSkeletonLeggings() {
        ItemStack item = new ItemStack(Material.IRON_LEGGINGS);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("Skeleton Leggings", NamedTextColor.GREEN)
                    .decoration(TextDecoration.ITALIC, false));

            meta.addEnchant(Enchantment.PROJECTILE_PROTECTION, 2, true);

            // Set CustomModelData to 3 for Skeleton Leggings
            meta.setCustomModelData(3);

            meta.lore(List.of(
                    Component.text("Leggings that have been reinforced with", NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false),
                    Component.text("bone plates.", NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false),
                    Component.empty(),
                    Component.text("When these leggings are worn, hostile", NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false),
                    Component.text("mobs (except for the Warden) will not", NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false),
                    Component.text("attack you.", NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false),
                    Component.empty(),
                    Component.text("33% chance of not consuming an arrow", NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false),
                    Component.text("upon firing a bow.", NamedTextColor.GRAY)
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
        NamespacedKey recipeKey = new NamespacedKey(plugin, "skeleton_leggings_recipe");
        ShapedRecipe recipe = new ShapedRecipe(recipeKey, getSkeletonLeggings());
        recipe.shape("BBB", "BLB", "B B");

        recipe.setIngredient('B', Material.BONE);
        recipe.setIngredient('L', Material.IRON_LEGGINGS);

        Bukkit.addRecipe(recipe);
    }

    public NamespacedKey getKey() {
        return key;
    }
}