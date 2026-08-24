package Lightapple.hoplite;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class RecipeBook implements Listener, CommandExecutor {

    private final Hoplite plugin;
    private final LightApple lightApple;
    private final Shortsword shortsword;
    private final CustomTrident customTrident;
    private final CustomTotem customTotem;
    private final LightAnvil lightAnvil;
    private final BundledArrows bundledArrows;
    private final Shortbow shortbow;
    private final LightNetheriteSword lightNetheriteSword;
    private final CrystallizationShard crystallizationShard;
    private final GoldenHead goldenHead;
    private final SmeltersPickaxe smeltersPickaxe;
    private final SuperSmeltersPickaxe superSmeltersPickaxe;
    private final BlazingCrossbow blazingCrossbow;
    private final CactusChestplate cactusChestplate;
    private final BanditLeggings banditLeggings;
    private final CowboyBoots cowboyBoots;
    private final SkeletonLeggings skeletonLeggings;
    private final AxolotlBoots axolotlBoots;

    public static class MainRecipeHolder implements InventoryHolder {
        @Override public Inventory getInventory() { return null; }
    }

    public static class RecipeDisplayHolder implements InventoryHolder {
        @Override public Inventory getInventory() { return null; }
    }

    public RecipeBook(
            Hoplite plugin,
            LightApple lightApple,
            Shortsword shortsword,
            CustomTrident customTrident,
            CustomTotem customTotem,
            LightAnvil lightAnvil,
            BundledArrows bundledArrows,
            Shortbow shortbow,
            LightNetheriteSword lightNetheriteSword,
            CrystallizationShard crystallizationShard,
            GoldenHead goldenHead,
            SmeltersPickaxe smeltersPickaxe,
            SuperSmeltersPickaxe superSmeltersPickaxe,
            BlazingCrossbow blazingCrossbow,
            CactusChestplate cactusChestplate,
            BanditLeggings banditLeggings,
            CowboyBoots cowboyBoots,
            SkeletonLeggings skeletonLeggings,
            AxolotlBoots axolotlBoots
    ) {
        this.plugin = plugin;
        this.lightApple = lightApple;
        this.shortsword = shortsword;
        this.customTrident = customTrident;
        this.customTotem = customTotem;
        this.lightAnvil = lightAnvil;
        this.bundledArrows = bundledArrows;
        this.shortbow = shortbow;
        this.lightNetheriteSword = lightNetheriteSword;
        this.crystallizationShard = crystallizationShard;
        this.goldenHead = goldenHead;
        this.smeltersPickaxe = smeltersPickaxe;
        this.superSmeltersPickaxe = superSmeltersPickaxe;
        this.blazingCrossbow = blazingCrossbow;
        this.cactusChestplate = cactusChestplate;
        this.banditLeggings = banditLeggings;
        this.cowboyBoots = cowboyBoots;
        this.skeletonLeggings = skeletonLeggings;
        this.axolotlBoots = axolotlBoots;
    }

    public static ItemStack getRecipeBookItem() {
        ItemStack book = new ItemStack(Material.BOOK);
        ItemMeta meta = book.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("Recipe Book", NamedTextColor.WHITE)
                    .decoration(TextDecoration.ITALIC, false));
            meta.lore(List.of(
                    Component.text("RIGHT CLICK this book to see all the recipes in this plugin", NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false)
            ));
            book.setItemMeta(meta);
        }
        return book;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage(Component.text("You must be an operator to use this command!", NamedTextColor.RED));
            return true;
        }

        ItemStack book = getRecipeBookItem();
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.getInventory().addItem(book.clone());
            player.sendMessage(Component.text("You received a Recipe Book!", NamedTextColor.GREEN));
        }
        sender.sendMessage(Component.text("Given Recipe Book to all online players.", NamedTextColor.GREEN));
        return true;
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            ItemStack item = event.getItem();
            if (item != null && item.getType() == Material.BOOK && item.hasItemMeta()) {
                if (event.getItem().isSimilar(getRecipeBookItem())) {
                    event.setCancelled(true);
                    openMainRecipeGUI(event.getPlayer());
                }
            }
        }
    }

    public void openMainRecipeGUI(Player player) {
        Inventory gui = Bukkit.createInventory(new MainRecipeHolder(), 27, Component.text("Recipe Book", NamedTextColor.DARK_GRAY));

        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        if (fillerMeta != null) {
            fillerMeta.displayName(Component.text(" "));
            filler.setItemMeta(fillerMeta);
        }
        for (int i = 0; i < gui.getSize(); i++) {
            gui.setItem(i, filler);
        }

        gui.setItem(0, lightApple.getLightApple());
        gui.setItem(1, shortsword.getShortsword());
        gui.setItem(2, customTrident.getTrident());
        gui.setItem(3, customTotem.getTotem());
        gui.setItem(4, lightAnvil.getLightAnvil());
        gui.setItem(5, bundledArrows.getBundledArrows());
        gui.setItem(6, shortbow.getShortbow());
        gui.setItem(7, lightNetheriteSword.getLightNetheriteSword());
        gui.setItem(8, crystallizationShard.getCrystallizationShard());
        gui.setItem(9, goldenHead.getGoldenHead());
        gui.setItem(10, smeltersPickaxe.getSmeltersPickaxe());
        gui.setItem(11, superSmeltersPickaxe.getSuperSmeltersPickaxe());
        gui.setItem(12, blazingCrossbow.getBlazingCrossbow());
        gui.setItem(13, cactusChestplate.getCactusChestplate());
        gui.setItem(14, banditLeggings.getBanditLeggings());
        gui.setItem(15, cowboyBoots.getCowboyBoots());
        gui.setItem(16, skeletonLeggings.getSkeletonLeggings());
        gui.setItem(17, axolotlBoots.getAxolotlBoots());

        player.openInventory(gui);
    }

    public void openAxolotlBootsRecipeGUI(Player player) {
        Inventory gui = Bukkit.createInventory(new RecipeDisplayHolder(), 27, Component.text("Recipe: Axolotl Boots", NamedTextColor.DARK_GRAY));
        applyBaseRecipeGUI(gui);

        gui.setItem(3, new ItemStack(Material.AXOLOTL_BUCKET));
        gui.setItem(11, new ItemStack(Material.GLOW_BERRIES));
        gui.setItem(12, new ItemStack(Material.DIAMOND_BOOTS));
        gui.setItem(13, new ItemStack(Material.GLOW_BERRIES));
        gui.setItem(21, new ItemStack(Material.SPORE_BLOSSOM));

        gui.setItem(16, axolotlBoots.getAxolotlBoots());
        player.openInventory(gui);
    }

    public void openCowboyBootsRecipeGUI(Player player) {
        Inventory gui = Bukkit.createInventory(new RecipeDisplayHolder(), 27, Component.text("Recipe: Cowboy Boots", NamedTextColor.DARK_GRAY));
        applyBaseRecipeGUI(gui);

        gui.setItem(2, new ItemStack(Material.IRON_CHAIN));
        gui.setItem(3, new ItemStack(Material.HAY_BLOCK));
        gui.setItem(4, new ItemStack(Material.IRON_CHAIN));
        gui.setItem(11, new ItemStack(Material.HAY_BLOCK));
        gui.setItem(12, new ItemStack(Material.LEATHER_BOOTS));
        gui.setItem(13, new ItemStack(Material.HAY_BLOCK));
        gui.setItem(20, new ItemStack(Material.IRON_CHAIN));
        gui.setItem(21, new ItemStack(Material.HAY_BLOCK));
        gui.setItem(22, new ItemStack(Material.IRON_CHAIN));

        gui.setItem(16, cowboyBoots.getCowboyBoots());
        player.openInventory(gui);
    }

    public void openSkeletonLeggingsRecipeGUI(Player player) {
        Inventory gui = Bukkit.createInventory(new RecipeDisplayHolder(), 27, Component.text("Recipe: Skeleton Leggings", NamedTextColor.DARK_GRAY));
        applyBaseRecipeGUI(gui);

        gui.setItem(2, new ItemStack(Material.BONE));
        gui.setItem(3, new ItemStack(Material.BONE));
        gui.setItem(4, new ItemStack(Material.BONE));
        gui.setItem(11, new ItemStack(Material.BONE));
        gui.setItem(12, new ItemStack(Material.IRON_LEGGINGS));
        gui.setItem(13, new ItemStack(Material.BONE));
        gui.setItem(20, new ItemStack(Material.BONE));
        gui.setItem(22, new ItemStack(Material.BONE));

        gui.setItem(16, skeletonLeggings.getSkeletonLeggings());
        player.openInventory(gui);
    }

    public void openBanditLeggingsRecipeGUI(Player player) {
        Inventory gui = Bukkit.createInventory(new RecipeDisplayHolder(), 27, Component.text("Recipe: Bandit Leggings", NamedTextColor.DARK_GRAY));
        applyBaseRecipeGUI(gui);

        gui.setItem(3, new ItemStack(Material.GLISTERING_MELON_SLICE));
        gui.setItem(11, new ItemStack(Material.GOLD_INGOT));
        gui.setItem(12, new ItemStack(Material.IRON_LEGGINGS));
        gui.setItem(13, new ItemStack(Material.GOLD_INGOT));
        gui.setItem(21, new ItemStack(Material.GLISTERING_MELON_SLICE));

        gui.setItem(16, banditLeggings.getBanditLeggings());
        player.openInventory(gui);
    }

    public void openCactusChestplateRecipeGUI(Player player) {
        Inventory gui = Bukkit.createInventory(new RecipeDisplayHolder(), 27, Component.text("Recipe: Cactus Chestplate", NamedTextColor.DARK_GRAY));
        applyBaseRecipeGUI(gui);

        gui.setItem(2, new ItemStack(Material.CACTUS));
        gui.setItem(3, new ItemStack(Material.CACTUS));
        gui.setItem(4, new ItemStack(Material.CACTUS));
        gui.setItem(11, new ItemStack(Material.CACTUS));
        gui.setItem(12, new ItemStack(Material.IRON_CHESTPLATE));
        gui.setItem(13, new ItemStack(Material.CACTUS));
        gui.setItem(20, new ItemStack(Material.CACTUS));
        gui.setItem(21, new ItemStack(Material.CACTUS));
        gui.setItem(22, new ItemStack(Material.CACTUS));

        gui.setItem(16, cactusChestplate.getCactusChestplate());
        player.openInventory(gui);
    }

    public void openBlazingCrossbowRecipeGUI(Player player) {
        Inventory gui = Bukkit.createInventory(new RecipeDisplayHolder(), 27, Component.text("Recipe: Blazing Crossbow", NamedTextColor.DARK_GRAY));
        applyBaseRecipeGUI(gui);

        gui.setItem(2, new ItemStack(Material.FIRE_CHARGE));
        gui.setItem(3, new ItemStack(Material.MAGMA_CREAM));
        gui.setItem(4, new ItemStack(Material.FIRE_CHARGE));
        gui.setItem(11, new ItemStack(Material.GLOWSTONE_DUST));
        gui.setItem(12, new ItemStack(Material.CROSSBOW));
        gui.setItem(13, new ItemStack(Material.GLOWSTONE_DUST));
        gui.setItem(20, new ItemStack(Material.FIRE_CHARGE));
        gui.setItem(21, new ItemStack(Material.MAGMA_CREAM));
        gui.setItem(22, new ItemStack(Material.FIRE_CHARGE));

        gui.setItem(16, blazingCrossbow.getBlazingCrossbow());
        player.openInventory(gui);
    }

    public void openLightAppleRecipeGUI(Player player) {
        Inventory gui = Bukkit.createInventory(new RecipeDisplayHolder(), 27, Component.text("Recipe: Light Apple", NamedTextColor.DARK_GRAY));
        applyBaseRecipeGUI(gui);

        gui.setItem(3, new ItemStack(Material.GOLD_INGOT));
        gui.setItem(11, new ItemStack(Material.GOLD_INGOT));
        gui.setItem(12, new ItemStack(Material.APPLE));
        gui.setItem(13, new ItemStack(Material.GOLD_INGOT));
        gui.setItem(21, new ItemStack(Material.GOLD_INGOT));

        gui.setItem(16, lightApple.getLightApple());
        player.openInventory(gui);
    }

    public void openShortswordRecipeGUI(Player player) {
        Inventory gui = Bukkit.createInventory(new RecipeDisplayHolder(), 27, Component.text("Recipe: Shortsword", NamedTextColor.DARK_GRAY));
        applyBaseRecipeGUI(gui);

        gui.setItem(2, new ItemStack(Material.COPPER_INGOT));
        gui.setItem(3, new ItemStack(Material.COPPER_INGOT));
        gui.setItem(4, new ItemStack(Material.COPPER_INGOT));
        gui.setItem(11, new ItemStack(Material.COPPER_INGOT));
        gui.setItem(12, new ItemStack(Material.IRON_SWORD));
        gui.setItem(13, new ItemStack(Material.COPPER_INGOT));
        gui.setItem(20, new ItemStack(Material.COPPER_INGOT));
        gui.setItem(21, new ItemStack(Material.COPPER_INGOT));
        gui.setItem(22, new ItemStack(Material.COPPER_INGOT));

        gui.setItem(16, shortsword.getShortsword());
        player.openInventory(gui);
    }

    public void openTridentRecipeGUI(Player player) {
        Inventory gui = Bukkit.createInventory(new RecipeDisplayHolder(), 27, Component.text("Recipe: Custom Trident", NamedTextColor.DARK_GRAY));
        applyBaseRecipeGUI(gui);

        gui.setItem(3, new ItemStack(Material.QUARTZ));
        gui.setItem(4, new ItemStack(Material.QUARTZ));
        gui.setItem(11, new ItemStack(Material.DIAMOND));
        gui.setItem(13, new ItemStack(Material.QUARTZ));
        gui.setItem(20, new ItemStack(Material.DIAMOND));

        gui.setItem(16, customTrident.getTrident());
        player.openInventory(gui);
    }

    public void openTotemRecipeGUI(Player player) {
        Inventory gui = Bukkit.createInventory(new RecipeDisplayHolder(), 27, Component.text("Recipe: Custom Totem", NamedTextColor.DARK_GRAY));
        applyBaseRecipeGUI(gui);

        gui.setItem(3, new ItemStack(Material.GOLD_INGOT));
        gui.setItem(11, new ItemStack(Material.GOLD_INGOT));
        gui.setItem(12, new ItemStack(Material.GHAST_TEAR));
        gui.setItem(13, new ItemStack(Material.GOLD_INGOT));
        gui.setItem(21, new ItemStack(Material.GOLD_INGOT));

        gui.setItem(16, customTotem.getTotem());
        player.openInventory(gui);
    }

    public void openLightAnvilRecipeGUI(Player player) {
        Inventory gui = Bukkit.createInventory(new RecipeDisplayHolder(), 27, Component.text("Recipe: Light Anvil", NamedTextColor.DARK_GRAY));
        applyBaseRecipeGUI(gui);

        gui.setItem(2, new ItemStack(Material.IRON_INGOT));
        gui.setItem(3, new ItemStack(Material.IRON_INGOT));
        gui.setItem(4, new ItemStack(Material.IRON_INGOT));
        gui.setItem(12, new ItemStack(Material.IRON_BLOCK));
        gui.setItem(20, new ItemStack(Material.IRON_INGOT));
        gui.setItem(21, new ItemStack(Material.IRON_INGOT));
        gui.setItem(22, new ItemStack(Material.IRON_INGOT));

        gui.setItem(16, lightAnvil.getLightAnvil());
        player.openInventory(gui);
    }

    public void openBundledArrowsRecipeGUI(Player player) {
        Inventory gui = Bukkit.createInventory(new RecipeDisplayHolder(), 27, Component.text("Recipe: Bundled Arrows", NamedTextColor.DARK_GRAY));
        applyBaseRecipeGUI(gui);

        gui.setItem(2, new ItemStack(Material.FLINT));
        gui.setItem(3, new ItemStack(Material.FLINT));
        gui.setItem(4, new ItemStack(Material.FLINT));
        gui.setItem(11, new ItemStack(Material.STICK));
        gui.setItem(12, new ItemStack(Material.STICK));
        gui.setItem(13, new ItemStack(Material.STICK));
        gui.setItem(20, new ItemStack(Material.FEATHER));
        gui.setItem(21, new ItemStack(Material.FEATHER));
        gui.setItem(22, new ItemStack(Material.FEATHER));

        gui.setItem(16, bundledArrows.getBundledArrows());
        player.openInventory(gui);
    }

    public void openShortbowRecipeGUI(Player player) {
        Inventory gui = Bukkit.createInventory(new RecipeDisplayHolder(), 27, Component.text("Recipe: Shortbow", NamedTextColor.DARK_GRAY));
        applyBaseRecipeGUI(gui);

        gui.setItem(2, new ItemStack(Material.COPPER_INGOT));
        gui.setItem(3, new ItemStack(Material.COPPER_INGOT));
        gui.setItem(4, new ItemStack(Material.COPPER_INGOT));
        gui.setItem(11, new ItemStack(Material.COPPER_INGOT));
        gui.setItem(12, new ItemStack(Material.BOW));
        gui.setItem(13, new ItemStack(Material.COPPER_INGOT));
        gui.setItem(20, new ItemStack(Material.COPPER_INGOT));
        gui.setItem(21, new ItemStack(Material.COPPER_INGOT));
        gui.setItem(22, new ItemStack(Material.COPPER_INGOT));

        gui.setItem(16, shortbow.getShortbow());
        player.openInventory(gui);
    }

    public void openLightNetheriteSwordRecipeGUI(Player player) {
        Inventory gui = Bukkit.createInventory(new RecipeDisplayHolder(), 27, Component.text("Recipe: Light Netherite Sword", NamedTextColor.DARK_GRAY));
        applyBaseRecipeGUI(gui);

        gui.setItem(2, new ItemStack(Material.NETHERITE_SCRAP));
        gui.setItem(10, new ItemStack(Material.OBSIDIAN));
        gui.setItem(11, new ItemStack(Material.DIAMOND_SWORD));
        gui.setItem(12, new ItemStack(Material.OBSIDIAN));
        gui.setItem(20, new ItemStack(Material.BLAZE_ROD));

        gui.setItem(16, lightNetheriteSword.getLightNetheriteSword());
        player.openInventory(gui);
    }

    public void openCrystallizationShardRecipeGUI(Player player) {
        Inventory gui = Bukkit.createInventory(new RecipeDisplayHolder(), 27, Component.text("Recipe: Crystallization Shard", NamedTextColor.DARK_GRAY));
        applyBaseRecipeGUI(gui);

        gui.setItem(2, new ItemStack(Material.PLAYER_HEAD));
        gui.setItem(10, new ItemStack(Material.AMETHYST_SHARD));
        gui.setItem(11, new ItemStack(Material.HONEY_BOTTLE));
        gui.setItem(12, new ItemStack(Material.AMETHYST_SHARD));
        gui.setItem(20, new ItemStack(Material.AMETHYST_SHARD));

        gui.setItem(16, crystallizationShard.getCrystallizationShard());
        player.openInventory(gui);
    }

    public void openGoldenHeadRecipeGUI(Player player) {
        Inventory gui = Bukkit.createInventory(new RecipeDisplayHolder(), 27, Component.text("Recipe: Golden Head", NamedTextColor.DARK_GRAY));
        applyBaseRecipeGUI(gui);

        gui.setItem(2, new ItemStack(Material.GOLD_INGOT));
        gui.setItem(3, new ItemStack(Material.GOLD_INGOT));
        gui.setItem(4, new ItemStack(Material.GOLD_INGOT));
        gui.setItem(11, new ItemStack(Material.GOLD_INGOT));
        gui.setItem(12, new ItemStack(Material.PLAYER_HEAD));
        gui.setItem(13, new ItemStack(Material.GOLD_INGOT));
        gui.setItem(20, new ItemStack(Material.GOLD_INGOT));
        gui.setItem(21, new ItemStack(Material.GOLD_INGOT));
        gui.setItem(22, new ItemStack(Material.GOLD_INGOT));

        gui.setItem(16, goldenHead.getGoldenHead());
        player.openInventory(gui);
    }

    public void openSmeltersPickaxeRecipeGUI(Player player) {
        Inventory gui = Bukkit.createInventory(new RecipeDisplayHolder(), 27, Component.text("Recipe: Smelter's Pickaxe", NamedTextColor.DARK_GRAY));
        applyBaseRecipeGUI(gui);

        gui.setItem(2, new ItemStack(Material.RAW_IRON));
        gui.setItem(3, new ItemStack(Material.RAW_IRON));
        gui.setItem(4, new ItemStack(Material.RAW_IRON));
        gui.setItem(11, new ItemStack(Material.COAL));
        gui.setItem(12, new ItemStack(Material.STICK));
        gui.setItem(13, new ItemStack(Material.COAL));
        gui.setItem(21, new ItemStack(Material.STICK));

        gui.setItem(16, smeltersPickaxe.getSmeltersPickaxe());
        player.openInventory(gui);
    }

    public void openSuperSmeltersPickaxeRecipeGUI(Player player) {
        Inventory gui = Bukkit.createInventory(new RecipeDisplayHolder(), 27, Component.text("Recipe: Super Smelter's Pickaxe", NamedTextColor.DARK_GRAY));
        applyBaseRecipeGUI(gui);

        gui.setItem(2, new ItemStack(Material.DIAMOND));
        gui.setItem(3, new ItemStack(Material.IRON_INGOT));
        gui.setItem(4, new ItemStack(Material.DIAMOND));
        gui.setItem(11, new ItemStack(Material.COAL));
        gui.setItem(12, new ItemStack(Material.STICK));
        gui.setItem(13, new ItemStack(Material.COAL));
        gui.setItem(21, new ItemStack(Material.STICK));

        gui.setItem(16, superSmeltersPickaxe.getSuperSmeltersPickaxe());
        player.openInventory(gui);
    }

    private void applyBaseRecipeGUI(Inventory gui) {
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        if (fillerMeta != null) {
            fillerMeta.displayName(Component.text(" "));
            filler.setItemMeta(fillerMeta);
        }
        for (int i = 0; i < gui.getSize(); i++) {
            gui.setItem(i, filler);
        }

        ItemStack arrow = new ItemStack(Material.ARROW);
        ItemMeta arrowMeta = arrow.getItemMeta();
        if (arrowMeta != null) {
            arrowMeta.displayName(Component.text("Crafts into", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));
            arrow.setItemMeta(arrowMeta);
        }
        gui.setItem(14, arrow);

        ItemStack back = new ItemStack(Material.BARRIER);
        ItemMeta backMeta = back.getItemMeta();
        if (backMeta != null) {
            backMeta.displayName(Component.text("Back", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
            back.setItemMeta(backMeta);
        }
        gui.setItem(18, back);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();

        if (holder instanceof MainRecipeHolder || holder instanceof RecipeDisplayHolder) {
            event.setCancelled(true);

            if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
                return;
            }

            Player player = (Player) event.getWhoClicked();

            if (holder instanceof MainRecipeHolder) {
                switch (event.getSlot()) {
                    case 0 -> openLightAppleRecipeGUI(player);
                    case 1 -> openShortswordRecipeGUI(player);
                    case 2 -> openTridentRecipeGUI(player);
                    case 3 -> openTotemRecipeGUI(player);
                    case 4 -> openLightAnvilRecipeGUI(player);
                    case 5 -> openBundledArrowsRecipeGUI(player);
                    case 6 -> openShortbowRecipeGUI(player);
                    case 7 -> openLightNetheriteSwordRecipeGUI(player);
                    case 8 -> openCrystallizationShardRecipeGUI(player);
                    case 9 -> openGoldenHeadRecipeGUI(player);
                    case 10 -> openSmeltersPickaxeRecipeGUI(player);
                    case 11 -> openSuperSmeltersPickaxeRecipeGUI(player);
                    case 12 -> openBlazingCrossbowRecipeGUI(player);
                    case 13 -> openCactusChestplateRecipeGUI(player);
                    case 14 -> openBanditLeggingsRecipeGUI(player);
                    case 15 -> openCowboyBootsRecipeGUI(player);
                    case 16 -> openSkeletonLeggingsRecipeGUI(player);
                    case 17 -> openAxolotlBootsRecipeGUI(player);
                }
            } else if (holder instanceof RecipeDisplayHolder) {
                if (event.getSlot() == 18) {
                    openMainRecipeGUI(player);
                }
            }
        }
    }
}