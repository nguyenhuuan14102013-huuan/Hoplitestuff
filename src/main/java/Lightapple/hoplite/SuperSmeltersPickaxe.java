package Lightapple.hoplite;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SuperSmeltersPickaxe implements Listener {

    private final Hoplite plugin;
    private final NamespacedKey smelterKey;

    public SuperSmeltersPickaxe(Hoplite plugin) {
        this.plugin = plugin;
        this.smelterKey = new NamespacedKey(plugin, "super_smelters_pickaxe");
        registerRecipe();
    }

    public ItemStack getSuperSmeltersPickaxe() {
        ItemStack item = new ItemStack(Material.DIAMOND_PICKAXE);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.displayName(Component.text("Super Smelter's Pickaxe", NamedTextColor.WHITE)
                    .decoration(TextDecoration.ITALIC, false));

            meta.lore(List.of(
                    Component.text("An upgraded pickaxe that smelts ores", NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false),
                    Component.text("automatically.", NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false)
            ));

            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addEnchant(Enchantment.EFFICIENCY, 2, true);

            // Custom Model Data set to 6 based on resource pack dispatch
            meta.setCustomModelData(6);

            meta.getPersistentDataContainer().set(smelterKey, PersistentDataType.BYTE, (byte) 1);

            item.setItemMeta(meta);
        }
        return item;
    }

    private void registerRecipe() {
        NamespacedKey recipeKey = new NamespacedKey(plugin, "super_smelters_pickaxe_recipe");
        if (Bukkit.getRecipe(recipeKey) != null) {
            Bukkit.removeRecipe(recipeKey);
        }

        ShapedRecipe recipe = new ShapedRecipe(recipeKey, getSuperSmeltersPickaxe());
        recipe.shape(
                "DID",
                "CSC",
                " S "
        );
        recipe.setIngredient('D', Material.DIAMOND);
        recipe.setIngredient('I', Material.IRON_INGOT);
        recipe.setIngredient('C', Material.COAL);
        recipe.setIngredient('S', Material.STICK);

        Bukkit.addRecipe(recipe);
    }

    private boolean isSuperSmeltersPickaxe(ItemStack item) {
        if (item == null || item.getType() != Material.DIAMOND_PICKAXE || !item.hasItemMeta()) {
            return false;
        }
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        return pdc.has(smelterKey, PersistentDataType.BYTE);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack tool = player.getInventory().getItemInMainHand();

        if (!isSuperSmeltersPickaxe(tool)) {
            return;
        }

        Block block = event.getBlock();

        if (block.getState() instanceof Container container) {
            if (block.getType() == Material.FURNACE || block.getType() == Material.BLAST_FURNACE || block.getType() == Material.SMOKER) {
                ItemStack[] contents = container.getInventory().getContents();
                boolean contentsSmelted = false;

                for (int i = 0; i < contents.length; i++) {
                    ItemStack original = contents[i];
                    if (original != null && original.getType() != Material.AIR) {
                        Material cooked = getSmeltedResult(original.getType());
                        if (cooked != null) {
                            ItemStack cookedStack = new ItemStack(cooked, original.getAmount());
                            block.getWorld().dropItemNaturally(block.getLocation(), cookedStack);
                            container.getInventory().setItem(i, null);
                            contentsSmelted = true;
                        }
                    }
                }

                if (contentsSmelted) {
                    spawnSmeltEffects(block);
                }
                return;
            }
        }

        Collection<ItemStack> drops = block.getDrops(tool, player);
        List<ItemStack> newDrops = new ArrayList<>();
        boolean tookSmeltEffect = false;

        for (ItemStack drop : drops) {
            Material cooked = getSmeltedResult(drop.getType());
            if (cooked != null) {
                newDrops.add(new ItemStack(cooked, drop.getAmount()));
                tookSmeltEffect = true;
            } else {
                newDrops.add(drop);
            }
        }

        if (tookSmeltEffect) {
            event.setDropItems(false);
            for (ItemStack dropItem : newDrops) {
                block.getWorld().dropItemNaturally(block.getLocation(), dropItem);
            }
            spawnSmeltEffects(block);
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null || !isSuperSmeltersPickaxe(killer.getInventory().getItemInMainHand())) {
            return;
        }

        boolean foodSmelted = false;
        List<ItemStack> drops = event.getDrops();

        for (int i = 0; i < drops.size(); i++) {
            ItemStack drop = drops.get(i);
            Material cooked = getSmeltedResult(drop.getType());
            if (cooked != null) {
                drops.set(i, new ItemStack(cooked, drop.getAmount()));
                foodSmelted = true;
            }
        }

        if (foodSmelted) {
            event.getEntity().getWorld().spawnParticle(
                    Particle.FLAME,
                    event.getEntity().getLocation().add(0, 0.5, 0),
                    12, 0.2, 0.2, 0.2, 0.05
            );
            event.getEntity().getWorld().playSound(event.getEntity().getLocation(), Sound.ITEM_FIRECHARGE_USE, 0.8f, 1.2f);
        }
    }

    private void spawnSmeltEffects(Block block) {
        block.getWorld().spawnParticle(
                Particle.FLAME,
                block.getLocation().add(0.5, 0.5, 0.5),
                15, 0.2, 0.2, 0.2, 0.05
        );
        block.getWorld().spawnParticle(
                Particle.LAVA,
                block.getLocation().add(0.5, 0.5, 0.5),
                3
        );
        block.getWorld().playSound(block.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 0.5f, 1.5f);
    }

    private Material getSmeltedResult(Material raw) {
        return switch (raw) {
            case RAW_IRON, IRON_ORE, DEEPSLATE_IRON_ORE -> Material.IRON_INGOT;
            case RAW_GOLD, GOLD_ORE, DEEPSLATE_GOLD_ORE, NETHER_GOLD_ORE -> Material.GOLD_INGOT;
            case RAW_COPPER, COPPER_ORE, DEEPSLATE_COPPER_ORE -> Material.COPPER_INGOT;
            case RAW_IRON_BLOCK -> Material.IRON_BLOCK;
            case RAW_GOLD_BLOCK -> Material.GOLD_BLOCK;
            case RAW_COPPER_BLOCK -> Material.COPPER_BLOCK;
            case ANCIENT_DEBRIS -> Material.NETHERITE_SCRAP;

            case SAND, RED_SAND -> Material.GLASS;
            case CLAY -> Material.TERRACOTTA;
            case COBBLESTONE -> Material.STONE;
            case WET_SPONGE -> Material.SPONGE;

            case BEEF -> Material.COOKED_BEEF;
            case PORKCHOP -> Material.COOKED_PORKCHOP;
            case CHICKEN -> Material.COOKED_CHICKEN;
            case MUTTON -> Material.COOKED_MUTTON;
            case RABBIT -> Material.COOKED_RABBIT;
            case SALMON -> Material.COOKED_SALMON;
            case COD -> Material.COOKED_COD;
            case POTATO -> Material.BAKED_POTATO;
            case KELP -> Material.DRIED_KELP;

            default -> null;
        };
    }
}