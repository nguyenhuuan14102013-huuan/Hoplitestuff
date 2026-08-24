package Lightapple.hoplite;

import com.destroystokyo.paper.profile.PlayerProfile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
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
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.profile.PlayerTextures;

import java.net.URI;
import java.util.List;
import java.util.UUID;

public class GoldenHead implements Listener {

    private final Hoplite plugin;
    private final NamespacedKey goldenHeadKey;

    // Fixed UUID so heads stack cleanly in player inventories
    private static final UUID GOLDEN_HEAD_UUID = UUID.fromString("606f2fa0-ec77-4717-91a5-977327f2c908");

    // Direct skin URL for Golden Orb (#67206)
    private static final String TEXTURE_URL = "http://textures.minecraft.net/texture/6b4e5daea66cf2bfc986acf6d13e8507c4897e064a0820a9dc7a5a27c048bc";

    public GoldenHead(Hoplite plugin) {
        this.plugin = plugin;
        this.goldenHeadKey = new NamespacedKey(plugin, "golden_head");
        registerRecipe();
    }

    public ItemStack getGoldenHead() {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();

        if (meta != null) {
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

            // Paper Native Texture API
            PlayerProfile profile = Bukkit.createProfile(GOLDEN_HEAD_UUID, "GoldenHead");
            PlayerTextures textures = profile.getTextures();
            try {
                textures.setSkin(URI.create(TEXTURE_URL).toURL());
            } catch (Exception ignored) {}

            profile.setTextures(textures);
            meta.setPlayerProfile(profile);

            // Tag as Golden Head
            meta.getPersistentDataContainer().set(goldenHeadKey, PersistentDataType.BYTE, (byte) 1);

            item.setItemMeta(meta);
        }
        return item;
    }

    private void registerRecipe() {
        NamespacedKey recipeKey = new NamespacedKey(plugin, "golden_head_recipe");
        if (Bukkit.getRecipe(recipeKey) != null) {
            return;
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

            if (i == 4) { // Center must be ANY PLAYER_HEAD
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

        // Apply 15s cooldown
        player.setCooldown(Material.PLAYER_HEAD, 300);

        item.setAmount(item.getAmount() - 1);

        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_BURP, 1.0f, 1.0f);
        player.getWorld().playSound(player.getLocation(), Sound.ITEM_HONEY_BOTTLE_DRINK, 0.8f, 1.2f);

        // Apply effects
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 2, false, true, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 2400, 1, false, true, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 280, 1, false, true, true));
    }
}