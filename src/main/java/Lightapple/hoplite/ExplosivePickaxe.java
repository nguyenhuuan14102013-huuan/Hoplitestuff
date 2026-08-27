package Lightapple.hoplite;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class ExplosivePickaxe implements Listener {

    private final Hoplite plugin;
    private final NamespacedKey key;

    public ExplosivePickaxe(Hoplite plugin) {
        this.plugin = plugin;
        this.key = new NamespacedKey(plugin, "explosive_pickaxe");
        registerRecipe();
    }

    public ItemStack getExplosivePickaxe() {
        ItemStack item = new ItemStack(Material.DIAMOND_PICKAXE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("Explosive Pickaxe", NamedTextColor.GOLD)
                    .decoration(TextDecoration.ITALIC, false));
            meta.lore(List.of(
                    Component.text("A pickaxe that detonates an area around", NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false),
                    Component.text("the mined block then drops all", NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false),
                    Component.text("destroyed blocks.", NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false)
            ));
            meta.setCustomModelData(2);
            meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
            item.setItemMeta(meta);
        }
        return item;
    }

    private void registerRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(key, getExplosivePickaxe());
        recipe.shape("RTR", "RPR", "RAR");
        recipe.setIngredient('R', Material.REDSTONE_BLOCK);
        recipe.setIngredient('T', Material.TNT);
        recipe.setIngredient('P', Material.DIAMOND_PICKAXE);
        recipe.setIngredient('A', Material.AMETHYST_SHARD);
        Bukkit.addRecipe(recipe);
    }

    /**
     * Checks if a block is breakable by the pickaxe.
     * Limits breaking to blocks with hardness equal to or less than Deepslate ( hardness <= 3.0f ).
     */
    private boolean isBreakable(Block block) {
        Material type = block.getType();
        if (type.isAir()) {
            return false;
        }
        float hardness = type.getHardness();
        return hardness >= 0.0f && hardness <= 3.0f;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.LEFT_CLICK_BLOCK) {
            return;
        }

        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.DIAMOND_PICKAXE || !item.hasItemMeta()) {
            return;
        }

        ItemMeta meta = item.getItemMeta();
        if (!meta.getPersistentDataContainer().has(key, PersistentDataType.BYTE)) {
            return;
        }

        Block centerBlock = event.getClickedBlock();
        if (centerBlock == null || !isBreakable(centerBlock)) {
            return;
        }

        event.setCancelled(true);
        Player player = event.getPlayer();

        // Spawn explosion effects without dealing entity damage
        centerBlock.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, centerBlock.getLocation().add(0.5, 0.5, 0.5), 1);
        centerBlock.getWorld().playSound(centerBlock.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);

        // Break 3x3x3 area of blocks up to Deepslate hardness (<= 3.0 hardness)
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    Block target = centerBlock.getRelative(x, y, z);
                    if (isBreakable(target)) {
                        target.breakNaturally(item);
                    }
                }
            }
        }

        // Apply 10 durability damage
        if (meta instanceof Damageable damageable) {
            int newDamage = damageable.getDamage() + 10;
            if (newDamage >= item.getType().getMaxDurability()) {
                player.getInventory().setItemInMainHand(null);
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
            } else {
                damageable.setDamage(newDamage);
                item.setItemMeta(damageable);
            }
        }
    }
}