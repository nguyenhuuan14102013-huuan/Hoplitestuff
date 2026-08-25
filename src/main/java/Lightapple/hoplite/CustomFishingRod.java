package Lightapple.hoplite;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.List;

public class CustomFishingRod implements Listener {

    private final Hoplite plugin;

    public CustomFishingRod(Hoplite plugin) {
        this.plugin = plugin;
    }

    public ItemStack getCustomFishingRod() {
        ItemStack rod = new ItemStack(Material.FISHING_ROD);
        ItemMeta meta = rod.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("Barbed Rod", NamedTextColor.WHITE)
                    .decoration(TextDecoration.ITALIC, false));
            meta.lore(List.of(
                    Component.text("Deals 0.75x 1.8 rod knockback to players", NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false)
            ));
            rod.setItemMeta(meta);
        }
        return rod;
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof FishHook hook)) return;
        if (!(hook.getShooter() instanceof Player shooter)) return;
        if (!(event.getHitEntity() instanceof Player target)) return;

        if (shooter.equals(target)) return;

        ItemStack mainHand = shooter.getInventory().getItemInMainHand();
        ItemStack offHand = shooter.getInventory().getItemInOffHand();

        boolean isMainHand = isCustomFishingRod(mainHand);
        boolean isOffHand = !isMainHand && isCustomFishingRod(offHand);

        if (!isMainHand && !isOffHand) return;

        ItemStack rod = isMainHand ? mainHand : offHand;

        // Apply 0.75x 1.8 Fishing Rod Knockback
        Vector kbDirection = target.getLocation().toVector().subtract(shooter.getLocation().toVector());
        if (kbDirection.lengthSquared() > 0) {
            kbDirection.normalize();
        } else {
            kbDirection = shooter.getLocation().getDirection();
        }

        double horizontalKb = 0.28; // ~0.75x 1.8 rod horizontal force
        double verticalKb = 0.25;   // ~0.75x 1.8 rod vertical lift

        Vector velocity = new Vector(kbDirection.getX() * horizontalKb, verticalKb, kbDirection.getZ() * horizontalKb);
        target.setVelocity(target.getVelocity().add(velocity));

        // Deplete 1 durability when knocking back a player
        if (shooter.getGameMode() != GameMode.CREATIVE) {
            ItemMeta meta = rod.getItemMeta();
            if (meta instanceof Damageable damageable) {
                int maxDamage = rod.getType().getMaxDurability();
                int newDamage = damageable.getDamage() + 1;

                if (newDamage >= maxDamage) {
                    if (isMainHand) shooter.getInventory().setItemInMainHand(null);
                    else shooter.getInventory().setItemInOffHand(null);
                    shooter.playSound(shooter.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
                } else {
                    damageable.setDamage(newDamage);
                    rod.setItemMeta(meta);
                }
            }
        }
    }

    public boolean isCustomFishingRod(ItemStack item) {
        if (item == null || item.getType() != Material.FISHING_ROD || !item.hasItemMeta()) return false;
        return item.isSimilar(getCustomFishingRod());
    }
}