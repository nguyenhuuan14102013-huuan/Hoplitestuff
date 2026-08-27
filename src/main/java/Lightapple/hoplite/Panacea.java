package Lightapple.hoplite;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Beehive;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

public class Panacea implements Listener {

    private final Hoplite plugin;
    private final NamespacedKey panaceaIdKey;
    private final NamespacedKey sipsKey;
    private final NamespacedKey filledKey;

    public Panacea(Hoplite plugin) {
        this.plugin = plugin;
        this.panaceaIdKey = new NamespacedKey(plugin, "is_panacea");
        this.sipsKey = new NamespacedKey(plugin, "panacea_sips");
        this.filledKey = new NamespacedKey(plugin, "panacea_filled");

        registerRecipe();
    }

    public ItemStack getPanacea() {
        return createPanacea();
    }

    public ItemStack getPanaceaItem() {
        return createPanacea();
    }

    private void registerRecipe() {
        NamespacedKey recipeKey = new NamespacedKey(plugin, "panacea_recipe");

        if (Bukkit.getRecipe(recipeKey) != null) {
            Bukkit.removeRecipe(recipeKey);
        }

        ShapedRecipe recipe = new ShapedRecipe(recipeKey, createPanacea());
        recipe.shape(
                "FHF",
                "LCL",
                "FHF"
        );

        recipe.setIngredient('F', Material.WHITE_TULIP);
        recipe.setIngredient('H', Material.HONEY_BOTTLE);
        recipe.setIngredient('L', Material.OAK_LOG);
        recipe.setIngredient('C', Material.PLAYER_HEAD);

        Bukkit.addRecipe(recipe);
    }

    public ItemStack createPanacea() {
        ItemStack item = new ItemStack(Material.POTION);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.displayName(Component.text("Panacea", NamedTextColor.LIGHT_PURPLE)
                    .decoration(TextDecoration.ITALIC, false));

            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            pdc.set(panaceaIdKey, PersistentDataType.BOOLEAN, true);
            pdc.set(sipsKey, PersistentDataType.INTEGER, 5);
            pdc.set(filledKey, PersistentDataType.BOOLEAN, true);

            meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
            item.setItemMeta(meta);
        }

        updateItemState(item, 5, true);
        return item;
    }

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        if (!pdc.has(panaceaIdKey, PersistentDataType.BOOLEAN)) return;

        Player player = event.getPlayer();
        boolean isFilled = pdc.getOrDefault(filledKey, PersistentDataType.BOOLEAN, false);

        if (!isFilled) {
            event.setCancelled(true);
            player.sendMessage(Component.text("This Panacea bottle is empty! Refill it at a bee nest first.", NamedTextColor.RED));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        int currentSips = pdc.getOrDefault(sipsKey, PersistentDataType.INTEGER, 5);
        int newSips = currentSips - 1;

        // Apply Regeneration I for 20 seconds (400 ticks)
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 400, 0, false, true, true));

        if (newSips <= 0) {
            event.setReplacement(new ItemStack(Material.AIR));
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
            return;
        }

        ItemStack updatedItem = item.clone();
        ItemMeta updatedMeta = updatedItem.getItemMeta();
        if (updatedMeta != null) {
            PersistentDataContainer updatedPdc = updatedMeta.getPersistentDataContainer();
            updatedPdc.set(sipsKey, PersistentDataType.INTEGER, newSips);
            updatedPdc.set(filledKey, PersistentDataType.BOOLEAN, false);
            updatedItem.setItemMeta(updatedMeta);
        }

        updateItemState(updatedItem, newSips, false);
        event.setReplacement(updatedItem);
    }

    @EventHandler
    public void onRefill(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;

        Block block = event.getClickedBlock();
        if (block == null) return;
        if (block.getType() != Material.BEE_NEST && block.getType() != Material.BEEHIVE) return;

        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.POTION) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        if (!pdc.has(panaceaIdKey, PersistentDataType.BOOLEAN)) return;

        boolean isFilled = pdc.getOrDefault(filledKey, PersistentDataType.BOOLEAN, false);
        if (isFilled) return;

        if (block.getBlockData() instanceof Beehive beehive) {
            if (beehive.getHoneyLevel() >= beehive.getMaximumHoneyLevel()) {
                event.setCancelled(true);

                beehive.setHoneyLevel(beehive.getHoneyLevel() - 1);
                block.setBlockData(beehive);

                int currentSips = pdc.getOrDefault(sipsKey, PersistentDataType.INTEGER, 1);

                pdc.set(filledKey, PersistentDataType.BOOLEAN, true);
                item.setItemMeta(meta);

                updateItemState(item, currentSips, true);

                Player player = event.getPlayer();
                player.playSound(player.getLocation(), Sound.ITEM_BOTTLE_FILL, 1.0f, 1.0f);
            }
        }
    }

    private void updateItemState(ItemStack item, int sipsRemaining, boolean isFilled) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        if (isFilled) {
            meta.setCustomModelData(1);
        } else {
            meta.setCustomModelData(2);
        }

        if (meta instanceof Damageable damageable) {
            damageable.setMaxDamage(5);
            damageable.setDamage(5 - sipsRemaining);
        }

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("A mythical regeneration potion that can", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("be consumed up to 5 times.", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("CONSUME to regenerate 4 hearts.", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("RIGHT CLICK on a bee nest with honey to", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("refill.", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false));

        if (!isFilled) {
            lore.add(Component.empty());
            lore.add(Component.text("[NEEDS REFILL]", NamedTextColor.RED, TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
        }

        meta.lore(lore);
        item.setItemMeta(meta);
    }
}