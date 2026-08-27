package Lightapple.hoplite;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantInventory;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class NetherReactorCore implements Listener {

    private final Hoplite plugin;
    private final Random random = new Random();
    private static boolean globalNetheriteTraded = false;

    public NetherReactorCore(Hoplite plugin) {
        this.plugin = plugin;
        registerRecipe();
    }

    public ItemStack getNetherReactorCore() {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.displayName(
                    Component.text("Nether Reactor Core", NamedTextColor.RED)
                            .decoration(TextDecoration.ITALIC, false)
            );

            meta.lore(List.of(
                    Component.text("An ancient artifact that corrupts", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
                    Component.text("nearby blocks and terraforms the area", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
                    Component.text("into a nether mini-biome.", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
                    Component.empty(),
                    Component.text("PLACE to convert nearby blocks into", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
                    Component.text("their nether variants and summon nether", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
                    Component.text("mobs.", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
                    Component.text("The Nether Merchant will also offer his", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
                    Component.text("trades to you.", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)
            ));

            meta.setCustomModelData(1);
            item.setItemMeta(meta);
        }

        return item;
    }

    private void registerRecipe() {
        NamespacedKey key = new NamespacedKey(plugin, "nether_reactor_core");

        // Remove existing recipe if reloaded
        Bukkit.removeRecipe(key);

        ShapedRecipe recipe = new ShapedRecipe(key, getNetherReactorCore());
        recipe.shape(
                "ICI",
                "IGI",
                "ICI"
        );

        recipe.setIngredient('I', Material.BLUE_STAINED_GLASS);
        recipe.setIngredient('C', Material.COAL_BLOCK);
        recipe.setIngredient('G', Material.GUNPOWDER);

        Bukkit.addRecipe(recipe);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();

        if (item.getType() == Material.BARRIER && item.hasItemMeta() && item.getItemMeta().hasCustomModelData()) {
            if (item.getItemMeta().getCustomModelData() == 1) {
                Block placedBlock = event.getBlockPlaced();
                Location center = placedBlock.getLocation();

                // Terraforms ground blocks into netherrack and ground ores into nether gold
                terraformArea(center);

                // Spawns Nether Merchant villager with custom trades
                spawnNetherMerchant(center.clone().add(0.5, 1.0, 0.5));

                // Spawns 5 to 7 random nether mobs
                spawnNetherMobs(center.clone().add(0, 1, 0));

                // Visual & Audio effects
                World world = center.getWorld();
                if (world != null) {
                    world.playSound(center, Sound.ENTITY_WITHER_SPAWN, 1.0f, 0.8f);
                    world.spawnParticle(Particle.FLAME, center.clone().add(0.5, 1.0, 0.5), 100, 2.0, 2.0, 2.0, 0.1);
                    world.spawnParticle(Particle.LARGE_SMOKE, center.clone().add(0.5, 1.0, 0.5), 50, 2.0, 2.0, 2.0, 0.05);
                }
            }
        }
    }

    private void terraformArea(Location center) {
        World world = center.getWorld();
        if (world == null) return;

        int radiusX = 5;
        int radiusZ = 5;

        for (int x = -radiusX; x <= radiusX; x++) {
            for (int z = -radiusZ; z <= radiusZ; z++) {

                // 2D elliptical distance calculation along ground level (Y = 0 offset)
                double normX = (double) x / radiusX;
                double normZ = (double) z / radiusZ;

                double distanceSquared = (normX * normX) + (normZ * normZ);

                // Add random noise per block to morph/deform the circular boundary every time
                double noiseOffset = (random.nextDouble() * 0.4) - 0.2; // -0.2 to +0.2 offset

                if (distanceSquared <= (1.0 + noiseOffset)) {
                    Block block = world.getBlockAt(center.getBlockX() + x, center.getBlockY(), center.getBlockZ() + z);
                    Material type = block.getType();

                    if (isOre(type)) {
                        block.setType(Material.NETHER_GOLD_ORE);
                    } else if (isTerraformable(type)) {
                        block.setType(Material.NETHERRACK);
                    }
                }
            }
        }
    }

    private boolean isOre(Material material) {
        return material.name().endsWith("_ORE");
    }

    private boolean isTerraformable(Material material) {
        return material == Material.DIRT
                || material == Material.GRASS_BLOCK
                || material == Material.STONE
                || material == Material.COBBLESTONE
                || material == Material.DEEPSLATE
                || material == Material.COBBLED_DEEPSLATE
                || material == Material.SAND
                || material == Material.GRAVEL
                || material == Material.ANDESITE
                || material == Material.DIORITE
                || material == Material.GRANITE;
    }

    private void spawnNetherMerchant(Location loc) {
        World world = loc.getWorld();
        if (world == null) return;

        Villager merchant = (Villager) world.spawnEntity(loc, EntityType.VILLAGER);
        merchant.customName(Component.text("Nether Merchant", NamedTextColor.RED).decoration(TextDecoration.BOLD, true));
        merchant.setCustomNameVisible(true);
        merchant.setProfession(Villager.Profession.NITWIT);
        merchant.setVillagerType(Villager.Type.TAIGA);

        List<MerchantRecipe> trades = new ArrayList<>();

        // 1: 8 Iron Ingot -> 8 Gold Ingot
        MerchantRecipe trade1 = new MerchantRecipe(new ItemStack(Material.GOLD_INGOT, 8), 999);
        trade1.addIngredient(new ItemStack(Material.IRON_INGOT, 8));
        trade1.setExperienceReward(false);
        trades.add(trade1);

        // 2: 16 Carrots -> 1 Nether Wart
        MerchantRecipe trade2 = new MerchantRecipe(new ItemStack(Material.NETHER_WART, 1), 999);
        trade2.addIngredient(new ItemStack(Material.CARROT, 16));
        trade2.setExperienceReward(false);
        trades.add(trade2);

        // 3: 1 Bone -> 1 Blaze Rod
        MerchantRecipe trade3 = new MerchantRecipe(new ItemStack(Material.BLAZE_ROD, 1), 999);
        trade3.addIngredient(new ItemStack(Material.BONE, 1));
        trade3.setExperienceReward(false);
        trades.add(trade3);

        // 4: 4 Gold Ingot -> 1 Netherite Scrap (Global 1-use limit across the entire world)
        MerchantRecipe trade4 = new MerchantRecipe(new ItemStack(Material.NETHERITE_SCRAP, 1), globalNetheriteTraded ? 0 : 1);
        trade4.addIngredient(new ItemStack(Material.GOLD_INGOT, 4));
        trade4.setExperienceReward(false);
        trades.add(trade4);

        merchant.setRecipes(trades);
    }

    @EventHandler
    public void onMerchantTrade(InventoryClickEvent event) {
        if (event.getInventory().getType() == InventoryType.MERCHANT) {
            MerchantInventory merchantInv = (MerchantInventory) event.getInventory();
            MerchantRecipe recipe = merchantInv.getSelectedRecipe();

            if (recipe != null && recipe.getResult().getType() == Material.NETHERITE_SCRAP) {
                if (event.getSlot() == 2 && event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.NETHERITE_SCRAP) {
                    globalNetheriteTraded = true;
                }
            }
        }
    }

    private void spawnNetherMobs(Location center) {
        World world = center.getWorld();
        if (world == null) return;

        int mobCount = random.nextInt(3) + 5; // Spawns 5, 6, or 7 mobs

        for (int i = 0; i < mobCount; i++) {
            Location spawnLoc = center.clone().add(
                    (random.nextDouble() * 6) - 3,
                    1,
                    (random.nextDouble() * 6) - 3
            );

            EntityType mobType = getRandomMobType();
            world.spawnEntity(spawnLoc, mobType);
        }
    }

    private EntityType getRandomMobType() {
        int roll = random.nextInt(85);

        if (roll < 40) {
            return EntityType.ZOMBIFIED_PIGLIN;
        } else if (roll < 70) {
            return EntityType.BLAZE;
        } else if (roll < 80) {
            return EntityType.WITHER_SKELETON;
        } else {
            return EntityType.GHAST;
        }
    }
}