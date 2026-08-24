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

public class CowboyBoots {

    private final Hoplite plugin;
    private final NamespacedKey key;

    public CowboyBoots(Hoplite plugin) {
        this.plugin = plugin;
        this.key = new NamespacedKey(plugin, "cowboy_boots");
        registerRecipe();
    }

    public ItemStack getCowboyBoots() {
        ItemStack item = new ItemStack(Material.LEATHER_BOOTS);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("Cowboy Boots", NamedTextColor.GREEN)
                    .decoration(TextDecoration.ITALIC, false));

            meta.addEnchant(Enchantment.UNBREAKING, 3, true);
            meta.addEnchant(Enchantment.PROTECTION, 2, true);

            // Set CustomModelData to 2 for Cowboy Boots
            meta.setCustomModelData(2);

            meta.lore(List.of(
                    Component.text("A pair of boots that allows your horse", NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false),
                    Component.text("to move 3 blocks per second faster.", NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false),
                    Component.empty(),
                    Component.text("Equivalent to Leather Boots.", NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false)
            ));

            meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
            item.setItemMeta(meta);
        }
        return item;
    }

    private void registerRecipe() {
        NamespacedKey recipeKey = new NamespacedKey(plugin, "cowboy_boots_recipe");
        ShapedRecipe recipe = new ShapedRecipe(recipeKey, getCowboyBoots());
        recipe.shape("CHC", "HBH", "CHC");

        recipe.setIngredient('C', Material.IRON_CHAIN);
        recipe.setIngredient('H', Material.HAY_BLOCK);
        recipe.setIngredient('B', Material.LEATHER_BOOTS);

        Bukkit.addRecipe(recipe);
    }

    public NamespacedKey getKey() {
        return key;
    }
}