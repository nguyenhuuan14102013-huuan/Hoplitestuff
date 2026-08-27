package Lightapple.hoplite;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class LumberjacksAxe implements Listener {

    private final Hoplite plugin;
    private final NamespacedKey key;

    public LumberjacksAxe(Hoplite plugin) {
        this.plugin = plugin;
        this.key = new NamespacedKey(plugin, "lumberjacks_axe");
        registerRecipe();
    }

    public ItemStack getLumberjacksAxe() {
        ItemStack item = new ItemStack(Material.IRON_AXE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("Lumberjack's Axe", NamedTextColor.GOLD)
                    .decoration(TextDecoration.ITALIC, false));
            meta.lore(List.of(
                    Component.text("A sturdy axe capable of felling most", NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false),
                    Component.text("trees with one swing.", NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false)
            ));
            meta.addEnchant(Enchantment.EFFICIENCY, 1, true);
            meta.setCustomModelData(1);
            meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
            item.setItemMeta(meta);
        }
        return item;
    }

    private void registerRecipe() {
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

    private boolean isLog(Material material) {
        return Tag.LOGS.isTagged(material) ||
                material.name().endsWith("_LOG") ||
                material.name().endsWith("_WOOD") ||
                material.name().endsWith("_STEM") ||
                material.name().endsWith("_HYPHAE");
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item.getType() != Material.IRON_AXE || !item.hasItemMeta()) {
            return;
        }

        ItemMeta meta = item.getItemMeta();
        if (!meta.getPersistentDataContainer().has(key, PersistentDataType.BYTE)) {
            return;
        }

        Block startBlock = event.getBlock();
        Material targetType = startBlock.getType();

        if (!isLog(targetType)) {
            return;
        }

        Queue<Block> queue = new LinkedList<>();
        Set<Block> visited = new HashSet<>();
        List<Block> logsToBreak = new ArrayList<>();

        queue.add(startBlock);
        visited.add(startBlock);

        while (!queue.isEmpty() && logsToBreak.size() < 50) {
            Block current = queue.poll();
            logsToBreak.add(current);

            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        if (x == 0 && y == 0 && z == 0) continue;

                        Block neighbor = current.getRelative(x, y, z);
                        if (!visited.contains(neighbor) && neighbor.getType() == targetType) {
                            visited.add(neighbor);
                            queue.add(neighbor);
                        }
                    }
                }
            }
        }

        int additionalLogs = logsToBreak.size() - 1;
        if (additionalLogs <= 0) {
            return;
        }

        if (meta instanceof Damageable damageable) {
            int currentDamage = damageable.getDamage();
            int maxDurability = item.getType().getMaxDurability();

            for (int i = 1; i < logsToBreak.size(); i++) {
                Block log = logsToBreak.get(i);
                log.breakNaturally(item);
                currentDamage++;

                if (currentDamage >= maxDurability) {
                    player.getInventory().setItemInMainHand(null);
                    player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
                    return;
                }
            }

            damageable.setDamage(currentDamage);
            item.setItemMeta(damageable);
        }
    }
}