package Lightapple.hoplite;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public class LightApple implements Listener {

    private final Hoplite plugin;

    public LightApple(Hoplite plugin) {
        this.plugin = plugin;
        registerRecipe();
    }

    public ItemStack getLightApple() {
        ItemStack item = new ItemStack(Material.GOLDEN_APPLE);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.displayName(Component.text("Light Apple", NamedTextColor.WHITE)
                    .decoration(TextDecoration.ITALIC, false));

            meta.lore(List.of(
                    Component.text("A Golden Apple crafted using an", NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false),
                    Component.text("efficient process.", NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false)
            ));

            item.setItemMeta(meta);
        }
        return item;
    }

    private void registerRecipe() {
        NamespacedKey key = new NamespacedKey(plugin, "light_apple");
        ShapedRecipe recipe = new ShapedRecipe(key, getLightApple());

        recipe.shape(
                " G ",
                "GAG",
                " G "
        );
        recipe.setIngredient('G', Material.GOLD_INGOT);
        recipe.setIngredient('A', Material.APPLE);

        Bukkit.addRecipe(recipe);
    }

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();

        if (item.getType() == Material.GOLDEN_APPLE) {
            Player player = event.getPlayer();

            Bukkit.getScheduler().runTask(plugin, () -> {
                player.removePotionEffect(PotionEffectType.REGENERATION);
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 200, 1));
            });
        }
    }
}