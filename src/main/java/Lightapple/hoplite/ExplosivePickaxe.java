package Lightapple.hoplite;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class ExplosivePickaxe implements Listener {

    private final Hoplite plugin;
    private final Random random = new Random();
    private final Set<Location> processingBlocks = new HashSet<>();

    public ExplosivePickaxe(Hoplite plugin) {
        this.plugin = plugin;
        registerRecipe();
    }

    public ItemStack getExplosivePickaxe() {
        ItemStack item = new ItemStack(Material.DIAMOND_PICKAXE);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.displayName(
                    Component.text("Explosive Pickaxe", NamedTextColor.GOLD)
                            .decoration(TextDecoration.ITALIC, false)
            );

            meta.lore(List.of(
                    Component.text("Mines blocks in a blast radius", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
                    Component.text("Consumes high durability per use", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)
            ));

            meta.setCustomModelData(2);
            item.setItemMeta(meta);
        }

        return item;
    }

    private void registerRecipe() {
        NamespacedKey key = new NamespacedKey(plugin, "explosive_pickaxe");
        Bukkit.removeRecipe(key);

        ShapedRecipe recipe = new ShapedRecipe(key, getExplosivePickaxe());
        recipe.shape(
                "RTR",
                "RPR",
                "RAR"
        );

        recipe.setIngredient('R', Material.REDSTONE_BLOCK);
        recipe.setIngredient('T', Material.TNT);
        recipe.setIngredient('P', Material.DIAMOND_PICKAXE);
        recipe.setIngredient('A', Material.AMETHYST_SHARD);

        Bukkit.addRecipe(recipe);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack tool = player.getInventory().getItemInMainHand();

        if (!isExplosivePickaxe(tool)) return;

        Block targetBlock = event.getBlock();
        Location targetLoc = targetBlock.getLocation();

        // Prevent recursive event loops
        if (processingBlocks.contains(targetLoc)) return;

        World world = targetBlock.getWorld();

        // 1. Play ONLY explosion sound (no explosion particles spawned)
        world.playSound(targetLoc, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);

        // 2. Break blocks in a dynamic TNT-like circular/spherical pattern
        int radius = 2;
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    if (x == 0 && y == 0 && z == 0) continue; // Main block handled by original break event

                    double distance = Math.sqrt(x * x + y * y + z * z);

                    // Add random noise per block to create organic TNT crater variations
                    double maxDistance = 1.7 + (random.nextDouble() * 0.6 - 0.3);

                    if (distance <= maxDistance) {
                        Block blockToBreak = targetBlock.getRelative(x, y, z);
                        if (isBreakable(blockToBreak.getType())) {
                            processingBlocks.add(blockToBreak.getLocation());
                            blockToBreak.breakNaturally(tool);
                            processingBlocks.remove(blockToBreak.getLocation());
                        }
                    }
                }
            }
        }

        // 3. Custom durability damage scaled by Unbreaking
        applyCustomDurabilityLoss(player, tool);
    }

    private void applyCustomDurabilityLoss(Player player, ItemStack tool) {
        if (tool == null || !(tool.getItemMeta() instanceof Damageable damageable)) return;

        // Unbreaking 0 = 25 damage, Unbreaking 1 = 20, Unbreaking 2 = 15, Unbreaking 3 = 10
        int unbreakingLevel = tool.getEnchantmentLevel(Enchantment.UNBREAKING);
        int damageToApply = Math.max(1, 25 - (unbreakingLevel * 5));

        int newDamage = damageable.getDamage() + damageToApply;
        int maxDurability = tool.getType().getMaxDurability();

        if (newDamage >= maxDurability) {
            player.getInventory().setItemInMainHand(null);
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
        } else {
            damageable.setDamage(newDamage);
            tool.setItemMeta(damageable);
        }
    }

    private boolean isExplosivePickaxe(ItemStack item) {
        if (item == null || item.getType() != Material.DIAMOND_PICKAXE || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        return meta.hasCustomModelData() && meta.getCustomModelData() == 1;
    }

    private boolean isBreakable(Material material) {
        return !material.isAir()
                && material != Material.BEDROCK
                && material != Material.BARRIER
                && material != Material.END_PORTAL_FRAME
                && material != Material.COMMAND_BLOCK
                && material != Material.STRUCTURE_BLOCK;
    }
}