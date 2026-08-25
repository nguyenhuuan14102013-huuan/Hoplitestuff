package Lightapple.hoplite;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class CustomFishingRod implements Listener {

    private final Hoplite plugin;
    private final NamespacedKey rodKey;
    private final NamespacedKey durabilityKey;
    private final NamespacedKey maxDurabilityKey;

    private static final int MAX_DURABILITY = 125;

    public CustomFishingRod(Hoplite plugin) {
        this.plugin = plugin;
        this.rodKey = new NamespacedKey(plugin, "custom_fishing_rod");
        this.durabilityKey = new NamespacedKey(plugin, "custom_durability");
        this.maxDurabilityKey = new NamespacedKey(plugin, "max_durability");
        registerRecipe();
    }

    public ItemStack getCustomFishingRod() {
        ItemStack rod = new ItemStack(Material.FISHING_ROD);
        ItemMeta meta = rod.getItemMeta();

        if (meta != null) {
            meta.displayName(Component.text("Barbed Rod", NamedTextColor.LIGHT_PURPLE)
                    .decoration(TextDecoration.ITALIC, false));

            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("A special fishing rod that can knock", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("back enemy players during combat.", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.empty());
            lore.add(Component.text("This fishing rod has 125 durability", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("which is only consumed when the rod", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("connects with an enemy.", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));

            meta.lore(lore);

            meta.getPersistentDataContainer().set(rodKey, PersistentDataType.BYTE, (byte) 1);
            meta.getPersistentDataContainer().set(durabilityKey, PersistentDataType.INTEGER, MAX_DURABILITY);
            meta.getPersistentDataContainer().set(maxDurabilityKey, PersistentDataType.INTEGER, MAX_DURABILITY);

            rod.setItemMeta(meta);
        }

        return rod;
    }

    public boolean isCustomFishingRod(ItemStack item) {
        if (item == null || item.getType() != Material.FISHING_ROD || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        return meta != null && meta.getPersistentDataContainer().has(rodKey, PersistentDataType.BYTE);
    }

    private void registerRecipe() {
        if (Bukkit.getRecipe(rodKey) != null) {
            Bukkit.removeRecipe(rodKey);
        }

        ShapedRecipe recipe = new ShapedRecipe(rodKey, getCustomFishingRod());

        // Normal fishing rod with Flint (F) at Row 1, Column 2
        recipe.shape(
                "  S",
                " ST",
                "SFT"
        );

        recipe.setIngredient('S', Material.STICK);
        recipe.setIngredient('T', Material.STRING); // Added the string back in!
        recipe.setIngredient('F', Material.FLINT);

        Bukkit.addRecipe(recipe);
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (event.getEntity() instanceof FishHook hook && event.getHitEntity() instanceof Player target) {
            if (hook.getShooter() instanceof Player shooter) {
                ItemStack rod = shooter.getInventory().getItemInMainHand();
                if (!isCustomFishingRod(rod)) {
                    rod = shooter.getInventory().getItemInOffHand();
                }

                if (isCustomFishingRod(rod)) {
                    Vector velocity = shooter.getLocation().getDirection().normalize().multiply(0.75).setY(0.3);
                    target.setVelocity(velocity);
                    damageRod(shooter, rod);
                }
            }
        }
    }

    private void damageRod(Player player, ItemStack rod) {
        ItemMeta meta = rod.getItemMeta();
        if (meta == null) return;

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        if (!pdc.has(durabilityKey, PersistentDataType.INTEGER)) return;

        int currentDurability = pdc.get(durabilityKey, PersistentDataType.INTEGER);
        currentDurability--;

        if (currentDurability <= 0) {
            player.getInventory().setItemInMainHand(null);
            player.getInventory().setItemInOffHand(null);
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
            return;
        }

        pdc.set(durabilityKey, PersistentDataType.INTEGER, currentDurability);

        if (meta instanceof Damageable damageable) {
            int vanillaMax = Material.FISHING_ROD.getMaxDurability();
            int visualDamage = (vanillaMax * (MAX_DURABILITY - currentDurability)) / MAX_DURABILITY;
            damageable.setDamage(visualDamage);
        }

        rod.setItemMeta(meta);
    }
}