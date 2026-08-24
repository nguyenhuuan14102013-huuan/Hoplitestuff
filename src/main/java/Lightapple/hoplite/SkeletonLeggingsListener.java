package Lightapple.hoplite;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.concurrent.ThreadLocalRandom;

public class SkeletonLeggingsListener implements Listener {

    private final Hoplite plugin;

    public SkeletonLeggingsListener(Hoplite plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityTarget(EntityTargetLivingEntityEvent event) {
        if (event.getTarget() instanceof Player player) {
            if (isWearingSkeletonLeggings(player)) {
                // Prevents all hostile mobs except the Warden from targeting the player
                if (event.getEntity() instanceof Monster && event.getEntityType() != EntityType.WARDEN) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onBowShoot(EntityShootBowEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (isWearingSkeletonLeggings(player)) {
                // 33% chance to not consume arrow
                if (ThreadLocalRandom.current().nextDouble() < 0.33) {
                    event.setConsumeItem(false);
                }
            }
        }
    }

    private boolean isWearingSkeletonLeggings(Player player) {
        if (player.getEquipment() == null) return false;
        ItemStack leggings = player.getEquipment().getLeggings();
        if (leggings == null || !leggings.hasItemMeta()) return false;

        NamespacedKey key = new NamespacedKey(plugin, "skeleton_leggings");
        return leggings.getItemMeta().getPersistentDataContainer().has(key, PersistentDataType.BYTE);
    }
}