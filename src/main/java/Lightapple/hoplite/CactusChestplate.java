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

public class CactusChestplate {

    private final Hoplite plugin;
    private final NamespacedKey key;

    public CactusChestplate(Hoplite plugin) {
        this.plugin = plugin;
        this.key = new NamespacedKey(plugin, "cactus_chestplate");
        registerRecipe();
    }

    public ItemStack getCactusChestplate() {
        ItemStack item = new ItemStack(Material.IRON_CHESTPLATE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("Cactus Chestplate", NamedTextColor.GREEN)
                    .decoration(TextDecoration.ITALIC, false));

            meta.addEnchant(Enchantment.UNBREAKING, 10, true);
            meta.addEnchant(Enchantment.THORNS, 2, true);
            meta.addEnchant(Enchantment.PROTECTION, 1, true);

            // Sets CustomModelData to 4 from texture pack config
            meta.setCustomModelData(4);

            meta.lore(List.of(
                    Component.text("A prickly chestplate, made from the", NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false),
                    Component.text("finest cactus.", NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false),
                    Component.empty(),
                    Component.text("Equivalent to Iron Chestplate", NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false)
            ));

            meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
            item.setItemMeta(meta);
        }
        return item;
    }

    private void registerRecipe() {
        NamespacedKey recipeKey = new NamespacedKey(plugin, "cactus_chestplate_recipe");
        ShapedRecipe recipe = new ShapedRecipe(recipeKey, getCactusChestplate());
        recipe.shape("CCC", "CIC", "CCC");

        recipe.setIngredient('C', Material.CACTUS);
        recipe.setIngredient('I', Material.IRON_CHESTPLATE);

        Bukkit.addRecipe(recipe);
    }

    public NamespacedKey getKey() {
        return key;
    }
}