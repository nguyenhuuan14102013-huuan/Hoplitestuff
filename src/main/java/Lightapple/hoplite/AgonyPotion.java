package Lightapple.hoplite;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

public class AgonyPotion implements Listener {

    private final Hoplite plugin;

    public AgonyPotion(Hoplite plugin) {
        this.plugin = plugin;
        registerRecipe();
    }

    public ItemStack getAgonyPotion() {
        ItemStack potion = new ItemStack(Material.SPLASH_POTION);
        PotionMeta meta = (PotionMeta) potion.getItemMeta();

        if (meta != null) {
            meta.displayName(Component.text("Agony", NamedTextColor.RED)
                    .decoration(TextDecoration.ITALIC, false));

            meta.setCustomModelData(2);

            meta.setColor(Color.fromRGB(60, 0, 0));

            // Instant Damage II & Weakness I (01:00)
            meta.addCustomEffect(new PotionEffect(PotionEffectType.INSTANT_DAMAGE, 1, 1), true);
            meta.addCustomEffect(new PotionEffect(PotionEffectType.WEAKNESS, 1200, 0), true);

            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("A cursed potion that drains the life of", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("its victims.", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));

            meta.lore(lore);
            potion.setItemMeta(meta);
        }

        return potion;
    }

    private void registerRecipe() {
        NamespacedKey key = new NamespacedKey(plugin, "agony_potion");

        if (Bukkit.getRecipe(key) != null) {
            Bukkit.removeRecipe(key);
        }

        ShapedRecipe recipe = new ShapedRecipe(key, getAgonyPotion());
        recipe.shape(
                " H ",
                "FBF",
                " S "
        );

        recipe.setIngredient('H', Material.PLAYER_HEAD);
        recipe.setIngredient('F', Material.FIRE_CHARGE);
        recipe.setIngredient('B', Material.GLASS_BOTTLE);
        recipe.setIngredient('S', Material.FERMENTED_SPIDER_EYE);

        Bukkit.addRecipe(recipe);
    }
}