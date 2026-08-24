package Lightapple.hoplite;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.UUID;

public class GoldenHead implements Listener {

    private final Hoplite plugin;
    private final NamespacedKey goldenHeadKey;

    private static final UUID GOLDEN_HEAD_PROFILE_UUID = UUID.fromString("bca6a8d0-9eea-4d97-a893-939f42364e98");
    private static final String GOLDEN_HEAD_TEXTURE =
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2JiNjEyZWI0OTVlZGUyYzVjYTUxNzhkMmQxZWNmMWNhNWEyNTVkMjVkZmMzYzI1NGJjNDdmNjg0ODc5MWQ4In19fQ==";

    public GoldenHead(Hoplite plugin) {
        this.plugin = plugin;
        this.goldenHeadKey = new NamespacedKey(plugin, "golden_head");
        registerRecipe();
    }

    public ItemStack getGoldenHead() {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD, 1);
        if (!(item.getItemMeta() instanceof SkullMeta meta)) {
            return item;
        }

        meta.displayName(Component.text("Golden Head", NamedTextColor.GOLD)
                .decoration(TextDecoration.ITALIC, false));

        meta.lore(List.of(
                Component.text("An enlightened form of healing rooted", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false),
                Component.text("from the slain heads of enemies.", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false),
                Component.empty(),
                Component.text("CONSUME to gain Regeneration", NamedTextColor.WHITE)
                        .decoration(TextDecoration.ITALIC, false),
                Component.text("III (0:05), Absorption II (2:00), and", NamedTextColor.WHITE)
                        .decoration(TextDecoration.ITALIC, false),
                Component.text("Speed II (0:14).", NamedTextColor.WHITE)
                        .decoration(TextDecoration.ITALIC, false)
        ));

        boolean textureApplied = applyTextureValue(meta, GOLDEN_HEAD_TEXTURE);
        if (!textureApplied) {
            OfflinePlayer fallbackOwner = Bukkit.getOfflinePlayer("Frankyx16");
            meta.setOwningPlayer(fallbackOwner);
        }

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(goldenHeadKey, PersistentDataType.BYTE, (byte) 1);

        item.setItemMeta(meta);
        return item;
    }

    private boolean applyTextureValue(SkullMeta meta, String textureValue) {
        if (textureValue != null && !textureValue.isEmpty()) {
            try {
                PlayerProfile profile = Bukkit.createProfile(GOLDEN_HEAD_PROFILE_UUID, "golden_head");
                profile.setProperty(new ProfileProperty("textures", textureValue));
                meta.setPlayerProfile(profile);
                return true;
            } catch (Throwable ignored) {
                return false;
            }
        } else {
            return false;
        }
    }

    private void registerRecipe() {
        NamespacedKey recipeKey = new NamespacedKey(plugin, "golden_head_recipe");

        // Remove existing recipe instance to ensure recipe book receives the updated output item
        if (Bukkit.getRecipe(recipeKey) != null) {
            Bukkit.removeRecipe(recipeKey);
        }

        ShapedRecipe recipe = new ShapedRecipe(recipeKey, getGoldenHead());
        recipe.shape(
                "GGG",
                "GHG",
                "GGG"
        );
        recipe.setIngredient('G', Material.GOLD_INGOT);
        recipe.setIngredient('H', new RecipeChoice.MaterialChoice(Material.PLAYER_HEAD));

        Bukkit.addRecipe(recipe);
    }

    @EventHandler
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        CraftingInventory inv = event.getInventory();
        ItemStack[] matrix = inv.getMatrix();

        if (matrix.length != 9) {
            return;
        }

        boolean validRecipe = true;

        for (int i = 0; i < 9; i++) {
            ItemStack item = matrix[i];

            if (i == 4) { // Center slot must be ANY PLAYER_HEAD
                if (item == null || item.getType() != Material.PLAYER_HEAD) {
                    validRecipe = false;
                    break;
                }
            } else { // Outer 8 slots must be Gold Ingots
                if (item == null || item.getType() != Material.GOLD_INGOT) {
                    validRecipe = false;
                    break;
                }
            }
        }

        if (validRecipe) {
            inv.setResult(getGoldenHead());
        }
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.PLAYER_HEAD || !item.hasItemMeta()) {
            return;
        }

        if (!item.getItemMeta().getPersistentDataContainer().has(goldenHeadKey, PersistentDataType.BYTE)) {
            return;
        }

        event.setCancelled(true);
        Player player = event.getPlayer();

        if (player.hasCooldown(Material.PLAYER_HEAD)) {
            return;
        }

        player.setCooldown(Material.PLAYER_HEAD, 300);

        item.setAmount(item.getAmount() - 1);

        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_BURP, 1.0f, 1.0f);
        player.getWorld().playSound(player.getLocation(), Sound.ITEM_HONEY_BOTTLE_DRINK, 0.8f, 1.2f);

        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 2, false, true, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 2400, 1, false, true, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 280, 1, false, true, true));
    }
}