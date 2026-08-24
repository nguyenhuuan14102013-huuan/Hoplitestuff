package Lightapple.hoplite;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class ProjectileHitListener implements Listener {

    private final NamespacedKey shortbowKey;
    private final NamespacedKey blazingCrossbowKey;

    public ProjectileHitListener(Hoplite plugin) {
        this.shortbowKey = new NamespacedKey(plugin, "shortbow");
        this.blazingCrossbowKey = new NamespacedKey(plugin, "blazing_crossbow");
    }

    @EventHandler
    public void onBowShoot(EntityShootBowEvent event) {
        ItemStack bow = event.getBow();
        if (bow == null || !bow.hasItemMeta()) {
            return;
        }

        if (event.getProjectile() instanceof Projectile projectile) {
            // Shortbow check
            if (bow.getItemMeta().getPersistentDataContainer().has(shortbowKey, PersistentDataType.BYTE)) {
                projectile.getPersistentDataContainer().set(shortbowKey, PersistentDataType.BYTE, (byte) 1);
            }
            // Blazing Crossbow check
            else if (bow.getItemMeta().getPersistentDataContainer().has(blazingCrossbowKey, PersistentDataType.BYTE)) {
                projectile.getPersistentDataContainer().set(blazingCrossbowKey, PersistentDataType.BYTE, (byte) 1);
                // Lights released projectile on fire
                projectile.setFireTicks(1200);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Projectile projectile)) {
            return;
        }

        if (!(projectile.getShooter() instanceof Player attacker)) {
            return;
        }

        if (!(event.getEntity() instanceof LivingEntity victim)) {
            return;
        }

        // Checks if projectile originated from Shortbow OR Blazing Crossbow
        boolean isShortbow = projectile.getPersistentDataContainer().has(shortbowKey, PersistentDataType.BYTE);
        boolean isBlazingCrossbow = projectile.getPersistentDataContainer().has(blazingCrossbowKey, PersistentDataType.BYTE);

        if (!isShortbow && !isBlazingCrossbow) {
            return;
        }

        double damageDealt = event.getFinalDamage();
        double remainingHealth = Math.max(0.0, victim.getHealth() - damageDealt);

        String victimName = (victim instanceof Player playerVictim) ? playerVictim.getName() : victim.getName();

        // Sends message to shooter (including when shooting self)
        attacker.sendMessage(
                Component.text(victimName, NamedTextColor.RED)
                        .append(Component.text(" is on ", NamedTextColor.GRAY))
                        .append(Component.text(String.format("%.1f", remainingHealth) + " HP", NamedTextColor.GREEN))
                        .append(Component.text("\nDealt ", NamedTextColor.GRAY))
                        .append(Component.text(String.format("%.1f", damageDealt) + " HP", NamedTextColor.GOLD))
        );
    }
}