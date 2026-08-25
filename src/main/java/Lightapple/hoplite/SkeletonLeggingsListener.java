package Lightapple.hoplite;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.concurrent.ThreadLocalRandom;

public class SkeletonLeggingsListener implements Listener {

    private final Hoplite plugin;
    private static final String AGGRO_KEY = "skeleton_leggings_aggro";

    public SkeletonLeggingsListener(Hoplite plugin) {
        this.plugin = plugin;
    }

    private boolean isWearingSkeletonLeggings(Player player) {
        ItemStack leggings = player.getInventory().getLeggings();
        return leggings != null && leggings.getType() == Material.CHAINMAIL_LEGGINGS && leggings.hasItemMeta();
    }

    @EventHandler
    public void onBowShoot(EntityShootBowEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (isWearingSkeletonLeggings(player)) {
                // 33% chance to preserve the arrow item
                if (ThreadLocalRandom.current().nextDouble() < 0.33) {
                    event.setConsumeItem(false);
                }
            }
        }
    }

    @EventHandler
    public void onEntityTarget(EntityTargetLivingEntityEvent event) {
        if (!(event.getTarget() instanceof Player player)) {
            return;
        }

        // Mobs ignore the player unless they've attacked the mob
        if (isWearingSkeletonLeggings(player)) {
            Entity mob = event.getEntity();
            if (!mob.hasMetadata(AGGRO_KEY)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player) {
            if (isWearingSkeletonLeggings(player)) {
                Entity victim = event.getEntity();
                if (victim instanceof LivingEntity) {
                    // Mark the mob so it can retaliate
                    victim.setMetadata(AGGRO_KEY, new FixedMetadataValue(plugin, true));
                }
            }
        }
    }
}