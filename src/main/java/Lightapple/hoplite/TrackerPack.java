package Lightapple.hoplite;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;

import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TrackerPack implements Listener {

    private final Hoplite plugin;
    private final NamespacedKey trackerItemKey;
    private final NamespacedKey trackerEntityKey;

    // Maps Tracker Mob UUID -> Target Player UUID
    private final Map<UUID, UUID> activeTrackers = new HashMap<>();

    public TrackerPack(Hoplite plugin) {
        this.plugin = plugin;
        this.trackerItemKey = new NamespacedKey(plugin, "is_tracker_pack");
        this.trackerEntityKey = new NamespacedKey(plugin, "is_tracker_entity");
        registerRecipe();
    }

    public ItemStack getTrackerPack() {
        ItemStack item = new ItemStack(Material.BONE);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.displayName(
                    Component.text("Tracker Pack", NamedTextColor.RED)
                            .decoration(TextDecoration.ITALIC, false)
            );

            meta.lore(List.of(
                    Component.text("Deploys a tracker that homes in", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
                    Component.text("on the nearest player.", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)
            ));

            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            pdc.set(trackerItemKey, PersistentDataType.BOOLEAN, true);

            meta.setCustomModelData(1);
            item.setItemMeta(meta);
        }

        return item;
    }

    private void registerRecipe() {
        NamespacedKey recipeKey = new NamespacedKey(plugin, "tracker_pack");
        if (Bukkit.getRecipe(recipeKey) != null) {
            Bukkit.removeRecipe(recipeKey);
        }

        ShapedRecipe recipe = new ShapedRecipe(recipeKey, getTrackerPack());
        recipe.shape(
                " B ",
                "BCB",
                " B "
        );

        recipe.setIngredient('B', Material.BONE);
        recipe.setIngredient('C', Material.COMPASS);

        Bukkit.addRecipe(recipe);
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;

        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;

        ItemStack item = event.getItem();
        if (item == null || !item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        if (!pdc.has(trackerItemKey, PersistentDataType.BOOLEAN)) return;

        event.setCancelled(true);
        Player user = event.getPlayer();

        // 1. Find nearest player (without revealing name)
        Player targetPlayer = findNearestPlayer(user);

        // If no players found nearby
        if (targetPlayer == null) {
            user.sendMessage(Component.text("No players found nearby to track!", NamedTextColor.RED));
            user.playSound(user.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
            return;
        }

        // 2. Secret message (Does NOT reveal target's name)
        user.sendMessage(Component.text("Tracker deployed! Homing in on nearest player...", NamedTextColor.GREEN));
        user.playSound(user.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1.0f, 1.5f);

        // Consume item if not in creative
        if (user.getGameMode() != GameMode.CREATIVE) {
            item.setAmount(item.getAmount() - 1);
        }

        // 3. Spawn the Tracker entities
        spawnTrackerEntity(user.getLocation(), targetPlayer);
    }

    private Player findNearestPlayer(Player source) {
        Player nearest = null;
        double nearestDistSq = Double.MAX_VALUE;

        for (Player other : source.getWorld().getPlayers()) {
            if (other.equals(source)) continue;
            if (other.getGameMode() == GameMode.SPECTATOR || other.getGameMode() == GameMode.CREATIVE) continue;

            double distSq = other.getLocation().distanceSquared(source.getLocation());
            if (distSq < nearestDistSq) {
                nearestDistSq = distSq;
                nearest = other;
            }
        }

        return nearest;
    }

    private void spawnTrackerEntity(Location loc, Player targetPlayer) {
        // Spawn 3 wolves in a loop
        for (int i = 0; i < 3; i++) {
            Wolf tracker = (Wolf) loc.getWorld().spawnEntity(loc, EntityType.WOLF);
            tracker.customName(Component.text("Tracker Hound", NamedTextColor.RED));
            tracker.setCustomNameVisible(true);
            tracker.setAngry(true);
            tracker.setTarget(targetPlayer);

            // Mark entity with PDC
            PersistentDataContainer pdc = tracker.getPersistentDataContainer();
            pdc.set(trackerEntityKey, PersistentDataType.BOOLEAN, true);

            // Store active tracker mapping
            activeTrackers.put(tracker.getUniqueId(), targetPlayer.getUniqueId());

            // Repeatedly guide tracker toward the target player
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!tracker.isValid() || tracker.isDead() || !targetPlayer.isOnline()) {
                        activeTrackers.remove(tracker.getUniqueId());
                        tracker.remove();
                        cancel();
                        return;
                    }

                    // Force target and pathfinding directly to player location
                    tracker.setTarget(targetPlayer);
                    tracker.getPathfinder().moveTo(targetPlayer.getLocation(), 1.45); // Restored speed
                }
            }.runTaskTimer(plugin, 5L, 20L); // Recalculate path every second
        }
    }

    @EventHandler
    public void onEntityTarget(EntityTargetLivingEntityEvent event) {
        // Prevent Tracker entity from targeting/attacking any other mob
        if (!(event.getEntity() instanceof Wolf wolf)) return;

        PersistentDataContainer pdc = wolf.getPersistentDataContainer();
        if (!pdc.has(trackerEntityKey, PersistentDataType.BOOLEAN)) return;

        UUID targetPlayerUUID = activeTrackers.get(wolf.getUniqueId());
        if (targetPlayerUUID == null) return;

        LivingEntity newTarget = event.getTarget();

        // If trying to target non-players or players other than designated target, CANCEL
        if (newTarget == null || !newTarget.getUniqueId().equals(targetPlayerUUID)) {
            event.setCancelled(true);
        }
    }
}