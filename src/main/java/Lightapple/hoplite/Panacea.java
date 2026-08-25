package Lightapple.hoplite;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
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

public class Panacea implements Listener {

    private final Hoplite plugin;
    private final NamespacedKey panaceaKey;
    private final NamespacedKey usesKey;

    public Panacea(Hoplite plugin) {
        this.plugin = plugin;
        this.panaceaKey = new NamespacedKey(plugin, "panacea");
        this.usesKey = new NamespacedKey(plugin, "panacea_uses");
        registerRecipe();
    }

    public ItemStack getPanacea() {
        ItemStack potion = new ItemStack(Material.POTION);
        ItemMeta meta = potion.getItemMeta();

        if (meta != null) {
            meta.displayName(Component.text("Panacea", NamedTextColor.LIGHT_PURPLE)
                    .decoration(TextDecoration.ITALIC, false));

            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("A mythical regeneration potion that can", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("be consumed up to 5 times.", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.empty());
            lore.add(Component.text("CONSUME to regenerate 4 hearts.", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.empty());
            lore.add(Component.text("RIGHT CLICK on a bee nest with honey to", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("refill.", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));

            meta.lore(lore);

            // Start at threshold 1 (full state)[cite: 2, 7]
            meta.setCustomModelData(1);
            meta.getPersistentDataContainer().set(usesKey, PersistentDataType.INTEGER, 1);

            potion.setItemMeta(meta);
        }

        return potion;
    }

    public ItemStack getEmptyPanaceaBottle() {
        ItemStack bottle = new ItemStack(Material.POTION);
        ItemMeta meta = bottle.getItemMeta();

        if (meta != null) {
            meta.displayName(Component.text("Empty Panacea Bottle", NamedTextColor.LIGHT_PURPLE)
                    .decoration(TextDecoration.ITALIC, false));

            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("An empty bottle that can be refilled.", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            meta.lore(lore);

            // Set to a custom model data state or texture for fully empty if applicable,
            // or leave it default if empty texture is handled elsewhere
            meta.getPersistentDataContainer().set(usesKey, PersistentDataType.INTEGER, 6);

            bottle.setItemMeta(meta);
        }

        return bottle;
    }

    private void registerRecipe() {
        if (Bukkit.getRecipe(panaceaKey) != null) {
            Bukkit.removeRecipe(panaceaKey);
        }

        ShapedRecipe recipe = new ShapedRecipe(panaceaKey, getPanacea());
        recipe.shape(
                "WGW",
                "LHL",
                "WGW"
        );

        recipe.setIngredient('W', Material.WHITE_TULIP);
        recipe.setIngredient('G', Material.EXPERIENCE_BOTTLE);
        recipe.setIngredient('L', Material.OAK_LOG);
        recipe.setIngredient('H', Material.PLAYER_HEAD);

        Bukkit.addRecipe(recipe);
    }

    public boolean isPanacea(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        return meta != null && meta.hasDisplayName() && meta.displayName().equals(Component.text("Panacea", NamedTextColor.LIGHT_PURPLE).decoration(TextDecoration.ITALIC, false));
    }

    @EventHandler
    public void onPlayerConsume(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();
        if (isPanacea(item)) {
            Player player = event.getPlayer();

            // Apply regeneration and sound effect
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 60, 1));
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_BURP, 1.0f, 1.0f);

            // Cancel standard vanilla item cleanup so we can manage custom threshold states
            event.setCancelled(true);

            ItemMeta meta = item.getItemMeta();
            if (meta == null) return;

            int currentStep = meta.getPersistentDataContainer().getOrDefault(usesKey, PersistentDataType.INTEGER, 1);
            currentStep++; // Increment threshold (1 -> 2 -> 3 -> 4 -> 5)[cite: 2, 7]

            ItemStack resultItem;

            if (currentStep <= 5) {
                // Clone the item and shift the model threshold up
                resultItem = item.clone();
                ItemMeta resultMeta = resultItem.getItemMeta();

                resultMeta.setCustomModelData(currentStep);
                resultMeta.getPersistentDataContainer().set(usesKey, PersistentDataType.INTEGER, currentStep);
                resultItem.setItemMeta(resultMeta);
            } else {
                // Reached the 5th sip, turn into the empty bottle item
                resultItem = getEmptyPanaceaBottle();
            }

            // Handle stack deduction properly for main/off hand
            ItemStack mainHand = player.getInventory().getItemInMainHand();
            ItemStack offHand = player.getInventory().getItemInOffHand();

            if (isPanacea(mainHand)) {
                if (mainHand.getAmount() > 1) {
                    mainHand.setAmount(mainHand.getAmount() - 1);
                    player.getInventory().addItem(resultItem);
                } else {
                    player.getInventory().setItemInMainHand(resultItem);
                }
            } else if (isPanacea(offHand)) {
                if (offHand.getAmount() > 1) {
                    offHand.setAmount(offHand.getAmount() - 1);
                    player.getInventory().addItem(resultItem);
                } else {
                    player.getInventory().setItemInOffHand(resultItem);
                }
            }
        }
    }
}