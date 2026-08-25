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
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class VerySuspiciousStew implements Listener {

    private final Hoplite plugin;
    private final NamespacedKey stewKey;
    private final Random random = new Random();

    private final List<PotionEffectType> positiveEffects = List.of(
            PotionEffectType.ABSORPTION,
            PotionEffectType.DOLPHINS_GRACE,
            PotionEffectType.FIRE_RESISTANCE,
            PotionEffectType.HASTE,
            PotionEffectType.HEALTH_BOOST,
            PotionEffectType.HERO_OF_THE_VILLAGE,
            PotionEffectType.INVISIBILITY,
            PotionEffectType.JUMP_BOOST,
            PotionEffectType.LUCK,
            PotionEffectType.NIGHT_VISION,
            PotionEffectType.RESISTANCE,
            PotionEffectType.SATURATION,
            PotionEffectType.SPEED,
            PotionEffectType.STRENGTH,
            PotionEffectType.WATER_BREATHING
    );

    private final List<PotionEffectType> negativeEffects = List.of(
            PotionEffectType.BLINDNESS,
            PotionEffectType.DARKNESS,
            PotionEffectType.HUNGER,
            PotionEffectType.MINING_FATIGUE,
            PotionEffectType.NAUSEA,
            PotionEffectType.POISON,
            PotionEffectType.SLOWNESS,
            PotionEffectType.WEAKNESS,
            PotionEffectType.WITHER
    );

    public VerySuspiciousStew(Hoplite plugin) {
        this.plugin = plugin;
        this.stewKey = new NamespacedKey(plugin, "very_suspicious_stew");
        registerRecipe();
    }

    public ItemStack getVerySuspiciousStew() {
        ItemStack stew = new ItemStack(Material.SUSPICIOUS_STEW);
        ItemMeta meta = stew.getItemMeta();

        if (meta != null) {
            meta.displayName(Component.text("Very Suspicious Stew", NamedTextColor.WHITE)
                    .decoration(TextDecoration.ITALIC, false));

            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("A bowl of stew made from a mishmash of", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("ingredients.", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.empty());
            lore.add(Component.text("CONSUME to regenerate 8 hearts over 10", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("seconds, and gain random effect.", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));

            meta.lore(lore);
            meta.getPersistentDataContainer().set(stewKey, PersistentDataType.BYTE, (byte) 1);
            stew.setItemMeta(meta);
        }

        return stew;
    }

    private void registerRecipe() {
        if (Bukkit.getRecipe(stewKey) != null) {
            Bukkit.removeRecipe(stewKey);
        }

        ShapedRecipe recipe = new ShapedRecipe(stewKey, getVerySuspiciousStew());
        recipe.shape(
                "RBH",
                " O ",
                "   "
        );

        recipe.setIngredient('R', Material.RED_MUSHROOM);
        recipe.setIngredient('B', Material.BROWN_MUSHROOM);
        recipe.setIngredient('H', Material.PLAYER_HEAD);
        recipe.setIngredient('O', Material.BOWL);

        Bukkit.addRecipe(recipe);
    }

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();
        if (item.getType() == Material.SUSPICIOUS_STEW && item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.getPersistentDataContainer().has(stewKey, PersistentDataType.BYTE)) {
                Player player = event.getPlayer();

                // Apply guaranteed Regen 3 (amplifier 2) for 10 seconds (200 ticks = 16 HP = 8 hearts)
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 200, 2));

                // Choose a random effect across positive (2 mins / 2400 ticks) and negative (15s / 300 ticks)
                boolean isPositive = random.nextBoolean();
                if (isPositive) {
                    PotionEffectType effect = positiveEffects.get(random.nextInt(positiveEffects.size()));
                    player.addPotionEffect(new PotionEffect(effect, 2400, 0));
                } else {
                    PotionEffectType effect = negativeEffects.get(random.nextInt(negativeEffects.size()));
                    player.addPotionEffect(new PotionEffect(effect, 300, 0));
                }
            }
        }
    }
}