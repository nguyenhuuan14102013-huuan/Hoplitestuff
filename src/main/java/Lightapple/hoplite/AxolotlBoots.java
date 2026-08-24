package Lightapple.hoplite;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class AxolotlBoots implements Listener {

    private final Hoplite plugin;

    public AxolotlBoots(Hoplite plugin) {
        this.plugin = plugin;
        registerRecipe();
    }

    @SuppressWarnings("deprecation")
    public ItemStack getAxolotlBoots() {
        ItemStack boots = new ItemStack(Material.DIAMOND_BOOTS);
        ItemMeta meta = boots.getItemMeta();

        if (meta != null) {
            meta.displayName(Component.text("Axolotl Boots", NamedTextColor.LIGHT_PURPLE)
                    .decoration(TextDecoration.ITALIC, false));

            // CustomModelData 2 maps to civilization:item/armor/axolotl/boots
            meta.setCustomModelData(2);

            meta.addEnchant(Enchantment.DEPTH_STRIDER, 2, true);
            meta.addEnchant(Enchantment.PROTECTION, 2, true);

            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("Boots crafted with an Axolotl.", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.empty());
            lore.add(Component.text("Equivalent to ", NamedTextColor.GRAY)
                    .append(Component.text("Diamond Boots", NamedTextColor.DARK_AQUA))
                    .append(Component.text(".", NamedTextColor.GRAY))
                    .decoration(TextDecoration.ITALIC, false));

            meta.lore(lore);
            boots.setItemMeta(meta);
        }

        return boots;
    }

    public void registerRecipe() {
        NamespacedKey key = new NamespacedKey(plugin, "axolotl_boots");

        if (Bukkit.getRecipe(key) != null) {
            Bukkit.removeRecipe(key);
        }

        ShapedRecipe recipe = new ShapedRecipe(key, getAxolotlBoots());
        recipe.shape(
                " B ",
                "GDG",
                " S "
        );

        recipe.setIngredient('B', Material.AXOLOTL_BUCKET);
        recipe.setIngredient('G', Material.GLOW_BERRIES);
        recipe.setIngredient('D', Material.DIAMOND_BOOTS);
        recipe.setIngredient('S', Material.SPORE_BLOSSOM);

        Bukkit.addRecipe(recipe);
    }
}