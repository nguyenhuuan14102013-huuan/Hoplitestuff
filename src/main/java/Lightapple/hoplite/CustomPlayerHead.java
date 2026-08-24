package Lightapple.hoplite;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public class CustomPlayerHead implements Listener {

    private final Hoplite plugin;

    public CustomPlayerHead(Hoplite plugin) {
        this.plugin = plugin;
    }

    /**
     * Creates an unstackable head with custom lore and name on death.
     */
    public ItemStack createPlayerHead(Player victim) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();

        if (meta != null) {
            meta.setOwningPlayer(victim);
            meta.displayName(Component.text(victim.getName() + "'s Head", NamedTextColor.RED)
                    .decoration(TextDecoration.ITALIC, false));
            applyLoreAndStackLimit(meta);
            item.setItemMeta(meta);
        }
        return item;
    }

    private void applyLoreAndStackLimit(SkullMeta meta) {
        meta.lore(List.of(
                Component.text("CONSUME to gain:", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false),
                Component.text("• Regeneration II (0:10)", NamedTextColor.LIGHT_PURPLE)
                        .decoration(TextDecoration.ITALIC, false),
                Component.text("• Speed II (0:15)", NamedTextColor.AQUA)
                        .decoration(TextDecoration.ITALIC, false)
        ));
        meta.setMaxStackSize(1);
    }

    /**
     * Dynamically attaches lore and max stack limit to any unformatted/vanilla head.
     */
    private void formatIfVanillaHead(ItemStack item) {
        if (item == null || item.getType() != Material.PLAYER_HEAD) {
            return;
        }

        SkullMeta meta = (SkullMeta) item.getItemMeta();
        if (meta != null && !meta.hasLore()) {
            if (!meta.hasDisplayName()) {
                meta.displayName(Component.text("Player Head", NamedTextColor.RED)
                        .decoration(TextDecoration.ITALIC, false));
            }
            applyLoreAndStackLimit(meta);
            item.setItemMeta(meta);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        // Drops custom head on any death type (including /kill)
        Player victim = event.getEntity();
        event.getDrops().add(createPlayerHead(victim));
    }

    @EventHandler
    public void onCreativeInventory(InventoryCreativeEvent event) {
        // Catches items grabbed directly from the Creative menu tab
        ItemStack cursor = event.getCursor();
        if (cursor.getType() == Material.PLAYER_HEAD) {
            formatIfVanillaHead(cursor);
            event.setCursor(cursor);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // Formats player heads whenever clicked inside any inventory GUI
        formatIfVanillaHead(event.getCurrentItem());
        formatIfVanillaHead(event.getCursor());
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.PLAYER_HEAD) {
            return;
        }

        // Cancel block placement
        event.setCancelled(true);

        // Format dynamically if pulled via /give or creative hotbar clone
        formatIfVanillaHead(item);

        Player player = event.getPlayer();

        if (player.hasCooldown(Material.PLAYER_HEAD)) {
            return;
        }

        // Instant consume
        item.setAmount(item.getAmount() - 1);

        // Play sound effects
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_BURP, 1.0f, 1.0f);
        player.getWorld().playSound(player.getLocation(), Sound.ITEM_HONEY_BOTTLE_DRINK, 0.8f, 1.2f);

        // Apply Regeneration II (10s) and Speed II (15s)
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 200, 1, false, true, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 300, 1, false, true, true));

        // 5-second cooldown
        player.setCooldown(Material.PLAYER_HEAD, 100);

        // Send chat notification when cooldown expires
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                player.sendMessage(Component.text("You can now eat Player Heads again", NamedTextColor.GREEN));
            }
        }, 100L);
    }
}