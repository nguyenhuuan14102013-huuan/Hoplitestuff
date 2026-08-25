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
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public class Panacea implements Listener {

    private final Hoplite plugin;
    private final NamespacedKey refillKey;

    public Panacea(Hoplite plugin) {
        this.plugin = plugin;
        this.refillKey = new NamespacedKey(plugin, "panacea_refill_count");
        registerRecipe();
    }

    public ItemStack getPanacea() {
        return createPanaceaItem(0);
    }

    private ItemStack createPanaceaItem(int refillCount) {
        ItemStack potion = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) potion.getItemMeta();

        if (meta != null) {
            meta.displayName(Component.text("Panacea", NamedTextColor.LIGHT_PURPLE)
                    .decoration(TextDecoration.ITALIC, false));

            if (refillCount == 0) {
                meta.lore(List.of(
                        Component.text("A mythical regeneration potion that can", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
                        Component.text("be consumed up to 5 times.", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
                        Component.empty(),
                        Component.text("CONSUME ", NamedTextColor.WHITE, TextDecoration.BOLD)
                                .append(Component.text("to regenerate 4 hearts.", NamedTextColor.GRAY).decoration(TextDecoration.BOLD, false))
                                .decoration(TextDecoration.ITALIC, false),
                        Component.empty(),
                        Component.text("RIGHT CLICK ", NamedTextColor.WHITE, TextDecoration.BOLD)
                                .append(Component.text("on a bee nest with honey to", NamedTextColor.GRAY).decoration(TextDecoration.BOLD, false))
                                .decoration(TextDecoration.ITALIC, false),
                        Component.text("refill.", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)
                ));
            } else {
                meta.lore(List.of(
                        Component.text("Empty Panacea Bottle (" + refillCount + "/5 Refills Used)", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
                        Component.empty(),
                        Component.text("RIGHT CLICK ", NamedTextColor.WHITE, TextDecoration.BOLD)
                                .append(Component.text("on a bee nest with honey to", NamedTextColor.GRAY).decoration(TextDecoration.BOLD, false))
                                .decoration(TextDecoration.ITALIC, false),
                        Component.text("refill.", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)
                ));
                // Sets CustomModelData (1-5) for empty texture stages
                meta.setCustomModelData(refillCount);
            }

            meta.getPersistentDataContainer().set(refillKey, PersistentDataType.INTEGER, refillCount);
            potion.setItemMeta(meta);
        }
        return potion;
    }

    private void registerRecipe() {
        NamespacedKey key = new NamespacedKey(plugin, "panacea");
        ShapedRecipe recipe = new ShapedRecipe(key, getPanacea());

        recipe.shape(
                "ABA",
                "CDC",
                "ABA"
        );

        recipe.setIngredient('A', Material.LILY_OF_THE_VALLEY);
        recipe.setIngredient('B', Material.HONEY_BOTTLE);
        recipe.setIngredient('C', Material.OAK_LOG);
        recipe.setIngredient('D', Material.PLAYER_HEAD);

        Bukkit.addRecipe(recipe);
    }

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();
        if (item.getType() != Material.POTION || !item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();

        if (!pdc.has(refillKey, PersistentDataType.INTEGER)) return;

        int refillsUsed = pdc.getOrDefault(refillKey, PersistentDataType.INTEGER, 0);

        if (refillsUsed >= 5) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(Component.text("This Panacea bottle is completely depleted and needs to be refilled!", NamedTextColor.RED));
            return;
        }

        Player player = event.getPlayer();

        // 20 Seconds (400 ticks) of Regen I = 4 Hearts restored
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 400, 0));

        // Replace bottle in player hand with the next empty texture stage
        int nextRefillState = refillsUsed + 1;
        ItemStack emptyStageItem = createPanaceaItem(nextRefillState);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            player.getInventory().setItemInMainHand(emptyStageItem);
        }, 1L);
    }

    @EventHandler
    public void onRefill(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Block block = event.getClickedBlock();
        if (block == null) return;

        if (block.getType() != Material.BEE_NEST && block.getType() != Material.BEEHIVE) return;

        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.POTION || !item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();

        if (!pdc.has(refillKey, PersistentDataType.INTEGER)) return;

        int refillsUsed = pdc.getOrDefault(refillKey, PersistentDataType.INTEGER, 0);
        if (refillsUsed == 0) return; // Already full

        Beehive beehiveData = (Beehive) block.getBlockData();

        // Must be full of honey (Honey Level = 5)
        if (beehiveData.getHoneyLevel() < beehiveData.getMaximumHoneyLevel()) {
            event.getPlayer().sendMessage(Component.text("This bee nest is not full of honey!", NamedTextColor.RED));
            return;
        }

        event.setCancelled(true);

        // Empty the honey from the hive
        beehiveData.setHoneyLevel(0);
        block.setBlockData(beehiveData);

        // Refill Panacea back to full state
        event.getPlayer().getInventory().setItem(event.getHand(), getPanacea());
        event.getPlayer().playSound(block.getLocation(), Sound.ITEM_BOTTLE_FILL, 1.0f, 1.0f);
        event.getPlayer().sendMessage(Component.text("Successfully refilled your Panacea!", NamedTextColor.GREEN));
    }
}