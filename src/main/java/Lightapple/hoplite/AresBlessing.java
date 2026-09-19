package Lightapple.hoplite;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Barrel;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.function.BiConsumer;

public class AresBlessing implements Listener {

    private final Hoplite plugin;
    private final NamespacedKey aresKey;
    private final Random random = new Random();
    private final List<BiConsumer<Location, Barrel>> lootTable = new ArrayList<>();
    private static final int CUSTOM_MODEL_DATA = 66;

    public AresBlessing(Hoplite plugin) {
        this.plugin = plugin;
        this.aresKey = new NamespacedKey(plugin, "ares_blessing");
        registerRecipe();
        buildLootTable();
    }

    public ItemStack getAresBlessing() {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.displayName(Component.text("Ares Blessing", NamedTextColor.RED)
                    .decoration(TextDecoration.ITALIC, false));

            meta.lore(List.of(
                    Component.text("A sacrifice made to the god of war,", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
                    Component.text("which grants an unknown treasure.", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
                    Component.empty(),
                    Component.text("CRAFT to summon a supply crate that", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false),
                    Component.text("contains a reward; its fate determined", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false),
                    Component.text("by the divine roll of the dice.", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false)
            ));

            meta.setCustomModelData(CUSTOM_MODEL_DATA);

            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            pdc.set(aresKey, PersistentDataType.BYTE, (byte) 1);

            item.setItemMeta(meta);
        }

        return item;
    }

    private void registerRecipe() {
        NamespacedKey recipeKey = new NamespacedKey(plugin, "ares_blessing_recipe");
        if (Bukkit.getRecipe(recipeKey) != null) {
            Bukkit.removeRecipe(recipeKey);
        }

        ShapedRecipe recipe = new ShapedRecipe(recipeKey, getAresBlessing());
        recipe.shape(
                "RRR",
                "RHR",
                "RRR"
        );
        recipe.setIngredient('R', Material.REDSTONE);
        recipe.setIngredient('H', new RecipeChoice.MaterialChoice(Material.PLAYER_HEAD));

        Bukkit.addRecipe(recipe);
    }

    @EventHandler
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        CraftingInventory inv = event.getInventory();
        ItemStack[] matrix = inv.getMatrix();
        if (matrix.length != 9) return;

        for (int i = 0; i < 9; i++) {
            ItemStack item = matrix[i];
            if (i == 4) {
                if (item == null || item.getType() != Material.PLAYER_HEAD) return;
            } else {
                if (item == null || item.getType() != Material.REDSTONE) return;
            }
        }

        inv.setResult(getAresBlessing());
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;

        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;

        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.PAPER || !item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();
        if (!meta.hasCustomModelData() || meta.getCustomModelData() != CUSTOM_MODEL_DATA) return;

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        if (!pdc.has(aresKey, PersistentDataType.BYTE)) return;

        event.setCancelled(true);
        Player player = event.getPlayer();

        if (player.hasCooldown(Material.PAPER)) return;
        player.setCooldown(Material.PAPER, 20);

        if (player.getGameMode() != org.bukkit.GameMode.CREATIVE) {
            item.setAmount(item.getAmount() - 1);
        }

        spawnFireworkEffect(player.getLocation());
        spawnAresSupplyDrop(player);
    }

    private void spawnFireworkEffect(Location loc) {
        World world = loc.getWorld();
        if (world == null) return;

        Firework firework = (Firework) world.spawnEntity(loc, EntityType.FIREWORK_ROCKET);
        FireworkMeta fwMeta = firework.getFireworkMeta();

        fwMeta.addEffect(FireworkEffect.builder()
                .withColor(Color.RED, Color.ORANGE, Color.YELLOW)
                .withFade(Color.WHITE)
                .with(FireworkEffect.Type.BURST)
                .trail(true)
                .flicker(true)
                .build());

        fwMeta.setPower(0);
        firework.setFireworkMeta(fwMeta);
        firework.detonate();
    }

    private void spawnAresSupplyDrop(Player player) {
        Location targetLoc = player.getLocation();
        World world = targetLoc.getWorld();
        if (world == null) return;

        Location spawnLoc = targetLoc.clone().add(0, 10, 0);
        FallingBlock fallingBarrel = world.spawnFallingBlock(spawnLoc, Material.BARREL.createBlockData());
        fallingBarrel.setDropItem(false);
        fallingBarrel.setHurtEntities(false);
        fallingBarrel.setGravity(false); // Disables standard acceleration for a smoother glide

        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                ticks++;

                // Set constant slow descent speed
                fallingBarrel.setVelocity(new Vector(0, -0.15, 0));

                // Increased timeout to 300 to account for slower falling speed
                if (!fallingBarrel.isValid() || fallingBarrel.isOnGround() || ticks > 300) {
                    Location landLoc = fallingBarrel.getLocation();
                    Location blockLoc = landLoc.getBlock().getLocation();

                    if (blockLoc.getBlock().getType().isAir() || blockLoc.getBlock().isReplaceable()) {
                        blockLoc.getBlock().setType(Material.BARREL);
                    } else {
                        blockLoc = landLoc;
                        blockLoc.getBlock().setType(Material.BARREL);
                    }

                    Barrel barrel = null;
                    if (blockLoc.getBlock().getState() instanceof Barrel b) {
                        barrel = b;
                    }

                    world.spawnParticle(Particle.EXPLOSION_EMITTER, blockLoc, 1);
                    world.playSound(blockLoc, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);

                    fallingBarrel.remove();

                    triggerLootRoll(blockLoc, barrel);
                    cancel();
                    return;
                }
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }

    private void triggerLootRoll(Location loc, Barrel barrel) {
        if (lootTable.isEmpty()) return;
        BiConsumer<Location, Barrel> lootAction = lootTable.get(random.nextInt(lootTable.size()));
        lootAction.accept(loc, barrel);
    }

    private void addLoot(BiConsumer<Location, Barrel> action, int weight) {
        for (int i = 0; i < weight; i++) {
            lootTable.add(action);
        }
    }

    private void buildLootTable() {
        // 4 gold x11
        addLoot((loc, barrel) -> drop(loc, barrel, new ItemStack(Material.GOLD_INGOT, 4)), 11);

        // Holy pickle x1
        addLoot((loc, barrel) -> {
            ItemStack pickle = new ItemStack(Material.SEA_PICKLE);
            ItemMeta m = pickle.getItemMeta();
            m.displayName(Component.text("Holy pickle", NamedTextColor.LIGHT_PURPLE).decoration(TextDecoration.ITALIC, false));
            m.lore(List.of(Component.text("it is one of many useless items", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)));
            pickle.setItemMeta(m);
            drop(loc, barrel, pickle);
        }, 1);

        // Totem x2
        addLoot((loc, barrel) -> drop(loc, barrel, new ItemStack(Material.TOTEM_OF_UNDYING)), 2);

        // Protection 2 book x1
        addLoot((loc, barrel) -> drop(loc, barrel, createEnchantedBook(Enchantment.PROTECTION, 2)), 1);

        // Instant health 2 potion x6
        addLoot((loc, barrel) -> drop(loc, barrel, createPotion(PotionEffectType.INSTANT_HEALTH, 1, 1, false)), 6);

        // Levitation 5 potion (0:04) x3
        addLoot((loc, barrel) -> drop(loc, barrel, createPotion(PotionEffectType.LEVITATION, 80, 4, false)), 3);

        // 8 gold x9
        addLoot((loc, barrel) -> drop(loc, barrel, new ItemStack(Material.GOLD_INGOT, 8)), 9);

        // Instant health 3 potion x3
        addLoot((loc, barrel) -> drop(loc, barrel, createPotion(PotionEffectType.INSTANT_HEALTH, 1, 2, false)), 3);

        // Raw fries x1
        addLoot((loc, barrel) -> {
            ItemStack fries = new ItemStack(Material.POTATO);
            ItemMeta m = fries.getItemMeta();
            m.displayName(Component.text("Raw fries", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));
            fries.setItemMeta(m);
            drop(loc, barrel, fries);
        }, 1);

        // "Turtle Helmet" x1
        addLoot((loc, barrel) -> {
            ItemStack helm = new ItemStack(Material.LEATHER_HELMET);
            LeatherArmorMeta m = (LeatherArmorMeta) helm.getItemMeta();
            m.setColor(Color.GREEN);
            m.displayName(Component.text("Turtle Helmet", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false));
            m.addEnchant(Enchantment.LUCK_OF_THE_SEA, 3, true);
            m.addEnchant(Enchantment.RESPIRATION, 3, true);
            m.addEnchant(Enchantment.AQUA_AFFINITY, 3, true);
            helm.setItemMeta(m);
            drop(loc, barrel, helm);
        }, 1);

        // 64 iron ingot x4
        addLoot((loc, barrel) -> drop(loc, barrel, new ItemStack(Material.IRON_INGOT, 64)), 4);

        // dragon head x4
        addLoot((loc, barrel) -> drop(loc, barrel, new ItemStack(Material.DRAGON_HEAD)), 4);

        // 3 golden apples x4
        addLoot((loc, barrel) -> drop(loc, barrel, new ItemStack(Material.GOLDEN_APPLE, 3)), 4);

        // Splash potion of speed 2 (1:30) x3
        addLoot((loc, barrel) -> drop(loc, barrel, createPotion(PotionEffectType.SPEED, 1800, 1, true)), 3);

        // Absorption 3 potion (1:00) x4
        addLoot((loc, barrel) -> drop(loc, barrel, createPotion(PotionEffectType.ABSORPTION, 1200, 2, false)), 4);

        // 5 diamonds x1
        addLoot((loc, barrel) -> drop(loc, barrel, new ItemStack(Material.DIAMOND, 5)), 1);

        // Golden head x3
        addLoot((loc, barrel) -> drop(loc, barrel, new GoldenHead(plugin).getGoldenHead()), 3);

        // Warden x2 (Spawns right away)
        addLoot((loc, barrel) -> loc.getWorld().spawnEntity(loc, EntityType.WARDEN), 2);

        // Sharpness 3 book x1
        addLoot((loc, barrel) -> drop(loc, barrel, createEnchantedBook(Enchantment.SHARPNESS, 3)), 1);

        // Regen (0:40) x2
        addLoot((loc, barrel) -> drop(loc, barrel, createPotion(PotionEffectType.REGENERATION, 800, 0, false)), 2);

        // 5 dead bushes x2
        addLoot((loc, barrel) -> drop(loc, barrel, new ItemStack(Material.DEAD_BUSH, 5)), 2);

        // Lingering potion of instant health (0:01) x4
        addLoot((loc, barrel) -> {
            ItemStack item = new ItemStack(Material.LINGERING_POTION);
            PotionMeta meta = (PotionMeta) item.getItemMeta();
            meta.addCustomEffect(new PotionEffect(PotionEffectType.INSTANT_HEALTH, 20, 0), true);
            item.setItemMeta(meta);
            drop(loc, barrel, item);
        }, 4);

        // 2 netherite scraps x1
        addLoot((loc, barrel) -> drop(loc, barrel, new ItemStack(Material.NETHERITE_SCRAP, 2)), 1);

        // 1 sniffer egg x1
        addLoot((loc, barrel) -> drop(loc, barrel, new ItemStack(Material.SNIFFER_EGG)), 1);

        // 16 gold x1
        addLoot((loc, barrel) -> drop(loc, barrel, new ItemStack(Material.GOLD_INGOT, 16)), 1);

        // power 3 book x2
        addLoot((loc, barrel) -> drop(loc, barrel, createEnchantedBook(Enchantment.POWER, 3)), 2);

        // sharpness 2 book x4
        addLoot((loc, barrel) -> drop(loc, barrel, createEnchantedBook(Enchantment.SHARPNESS, 2)), 4);

        // absorption 4 potion (1:00) x1
        addLoot((loc, barrel) -> drop(loc, barrel, createPotion(PotionEffectType.ABSORPTION, 1200, 3, false)), 1);

        // 5 diamonds x5
        addLoot((loc, barrel) -> drop(loc, barrel, new ItemStack(Material.DIAMOND, 5)), 5);

        // Fire aspect 1 book x2
        addLoot((loc, barrel) -> drop(loc, barrel, createEnchantedBook(Enchantment.FIRE_ASPECT, 1)), 2);

        // Instant health 4 x1
        addLoot((loc, barrel) -> drop(loc, barrel, createPotion(PotionEffectType.INSTANT_HEALTH, 1, 3, false)), 1);

        // 2 golden apples x2
        addLoot((loc, barrel) -> drop(loc, barrel, new ItemStack(Material.GOLDEN_APPLE, 2)), 2);

        // jump boost 4 potion (0:10) x2
        addLoot((loc, barrel) -> drop(loc, barrel, createPotion(PotionEffectType.JUMP_BOOST, 200, 3, false)), 2);

        // curse of binding carved pumpkin x2
        addLoot((loc, barrel) -> {
            ItemStack pumpkin = new ItemStack(Material.CARVED_PUMPKIN);
            ItemMeta m = pumpkin.getItemMeta();
            m.addEnchant(Enchantment.BINDING_CURSE, 1, true);
            pumpkin.setItemMeta(m);
            drop(loc, barrel, pumpkin);
        }, 2);

        // TNT x2 (Already ignited when crate comes down)
        addLoot((loc, barrel) -> {
            loc.getWorld().spawn(loc, TNTPrimed.class);
            loc.getWorld().spawn(loc, TNTPrimed.class);
        }, 2);

        // Lightning x2 (Strikes next to crate, causes fire)
        addLoot((loc, barrel) -> {
            loc.getWorld().strikeLightning(loc.clone().add(1, 0, 0));
            loc.getWorld().strikeLightning(loc.clone().add(-1, 0, 0));
        }, 2);

        // Enchanting golden apple x1
        addLoot((loc, barrel) -> drop(loc, barrel, new ItemStack(Material.ENCHANTED_GOLDEN_APPLE)), 1);

        // Flame book x1
        addLoot((loc, barrel) -> drop(loc, barrel, createEnchantedBook(Enchantment.FLAME, 1)), 1);

        // Leather pants x1
        addLoot((loc, barrel) -> {
            ItemStack pants = new ItemStack(Material.LEATHER_LEGGINGS);
            LeatherArmorMeta m = (LeatherArmorMeta) pants.getItemMeta();
            m.setColor(Color.GREEN);
            m.addEnchant(Enchantment.POWER, 5, true);
            m.addEnchant(Enchantment.FLAME, 1, true);
            m.addEnchant(Enchantment.INFINITY, 1, true);
            pants.setItemMeta(m);
            drop(loc, barrel, pants);
        }, 1);

        // Instant damage 2 potion x1
        addLoot((loc, barrel) -> drop(loc, barrel, createPotion(PotionEffectType.INSTANT_DAMAGE, 1, 1, false)), 1);

        // Leather Boots x1
        addLoot((loc, barrel) -> {
            ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);
            LeatherArmorMeta m = (LeatherArmorMeta) boots.getItemMeta();
            m.setColor(Color.RED);
            m.addEnchant(Enchantment.RIPTIDE, 3, true);
            m.addEnchant(Enchantment.UNBREAKING, 3, true);
            boots.setItemMeta(m);
            drop(loc, barrel, boots);
        }, 1);

        // Leather tunic x1
        addLoot((loc, barrel) -> {
            ItemStack tunic = new ItemStack(Material.LEATHER_CHESTPLATE);
            LeatherArmorMeta m = (LeatherArmorMeta) tunic.getItemMeta();
            m.setColor(Color.BLUE);
            m.addEnchant(Enchantment.POWER, 5, true);
            m.addEnchant(Enchantment.FLAME, 1, true);
            m.addEnchant(Enchantment.INFINITY, 1, true);
            tunic.setItemMeta(m);
            drop(loc, barrel, tunic);
        }, 1);

        // Netherite shovel x1
        addLoot((loc, barrel) -> drop(loc, barrel, new ItemStack(Material.NETHERITE_SHOVEL)), 1);

        // Elder guardian x1 (Spawns right away)
        addLoot((loc, barrel) -> loc.getWorld().spawnEntity(loc, EntityType.ELDER_GUARDIAN), 1);
    }

    private void drop(Location loc, Barrel barrel, ItemStack item) {
        if (barrel != null) {
            HashMap<Integer, ItemStack> leftover = barrel.getInventory().addItem(item);
            for (ItemStack extra : leftover.values()) {
                loc.getWorld().dropItemNaturally(loc, extra);
            }
            barrel.update();
        } else {
            loc.getWorld().dropItemNaturally(loc, item);
        }
    }

    private ItemStack createPotion(PotionEffectType type, int durationTicks, int amplifier, boolean splash) {
        ItemStack item = new ItemStack(splash ? Material.SPLASH_POTION : Material.POTION);
        PotionMeta meta = (PotionMeta) item.getItemMeta();
        meta.addCustomEffect(new PotionEffect(type, durationTicks, amplifier), true);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createEnchantedBook(Enchantment ench, int level) {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        EnchantmentStorageMeta meta = (EnchantmentStorageMeta) book.getItemMeta();
        meta.addStoredEnchant(ench, level, true);
        book.setItemMeta(meta);
        return book;
    }
}