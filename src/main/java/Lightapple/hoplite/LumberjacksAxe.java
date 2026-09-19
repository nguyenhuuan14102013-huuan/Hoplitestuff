package Lightapple.hoplite;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public class LumberjacksAxe implements Listener {

    private final Hoplite plugin;
    private static final int MAX_TREE_SIZE = 128; // Limit to prevent server lag on giant trees

    public LumberjacksAxe(Hoplite plugin) {
        this.plugin = plugin;
        registerRecipe();
    }

    public ItemStack getLumberjacksAxe() {
        ItemStack item = new ItemStack(Material.IRON_AXE);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.displayName(
                    Component.text("Lumberjack's Axe", NamedTextColor.GOLD)
                            .decoration(TextDecoration.ITALIC, false)
            );

            meta.lore(List.of(
                    Component.text("Chops down whole trees sequentially", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)
            ));

            meta.setCustomModelData(1);
            item.setItemMeta(meta);
        }

        return item;
    }

    private void registerRecipe() {
        NamespacedKey key = new NamespacedKey(plugin, "lumberjacks_axe");
        Bukkit.removeRecipe(key);

        ShapedRecipe recipe = new ShapedRecipe(key, getLumberjacksAxe());
        recipe.shape(
                "IIF",
                "IS ",
                " S "
        );

        recipe.setIngredient('I', Material.IRON_INGOT);
        recipe.setIngredient('F', Material.FLINT);
        recipe.setIngredient('S', Material.STICK);

        Bukkit.addRecipe(recipe);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack tool = player.getInventory().getItemInMainHand();

        if (!isLumberjacksAxe(tool)) return;

        Block startBlock = event.getBlock();
        if (!isLog(startBlock.getType())) return;

        // Collect all connected log blocks in the tree
        List<Block> logsToBreak = collectConnectedLogs(startBlock);

        // Remove the initial broken block (handled naturally by BlockBreakEvent)
        logsToBreak.remove(startBlock);

        if (logsToBreak.isEmpty()) return;

        // Sort blocks from bottom to top (Y-level ascending) for realistic chopping animation
        logsToBreak.sort(Comparator.comparingInt(Block::getY));

        // Sequential breaking task (animates breaking 1 block every 2 ticks / 0.1s)
        new BukkitRunnable() {
            private final Queue<Block> queue = new ArrayDeque<>(logsToBreak);

            @Override
            public void run() {
                ItemStack currentTool = player.getInventory().getItemInMainHand();

                // Stop animation if player switched tools or tool broke
                if (!isLumberjacksAxe(currentTool) || queue.isEmpty()) {
                    cancel();
                    return;
                }

                Block target = queue.poll();

                if (target != null && isLog(target.getType())) {
                    // Break block with drops and effects
                    target.breakNaturally(currentTool);

                    // Apply standard durability damage per log broken
                    damageTool(player, currentTool);
                }
            }
        }.runTaskTimer(plugin, 2L, 2L); // 2 ticks delay between each block
    }

    private List<Block> collectConnectedLogs(Block startBlock) {
        List<Block> logs = new ArrayList<>();
        Set<Block> visited = new HashSet<>();
        Queue<Block> queue = new ArrayDeque<>();

        queue.add(startBlock);
        visited.add(startBlock);

        while (!queue.isEmpty() && logs.size() < MAX_TREE_SIZE) {
            Block current = queue.poll();
            logs.add(current);

            // Search 3x3x3 neighbor area
            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        if (x == 0 && y == 0 && z == 0) continue;

                        Block neighbor = current.getRelative(x, y, z);
                        if (!visited.contains(neighbor) && isLog(neighbor.getType())) {
                            visited.add(neighbor);
                            queue.add(neighbor);
                        }
                    }
                }
            }
        }

        return logs;
    }

    private void damageTool(Player player, ItemStack tool) {
        if (tool == null || !(tool.getItemMeta() instanceof Damageable damageable)) return;

        int newDamage = damageable.getDamage() + 1;
        if (newDamage >= tool.getType().getMaxDurability()) {
            player.getInventory().setItemInMainHand(null);
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
        } else {
            damageable.setDamage(newDamage);
            tool.setItemMeta(damageable);
        }
    }

    private boolean isLumberjacksAxe(ItemStack item) {
        if (item == null || item.getType() != Material.IRON_AXE || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        return meta.hasCustomModelData() && meta.getCustomModelData() == 1;
    }

    private boolean isLog(Material material) {
        return Tag.LOGS.isTagged(material);
    }
}