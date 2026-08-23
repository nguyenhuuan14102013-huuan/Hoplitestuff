package Lightapple.hoplite;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class CrystallizationShard implements Listener {

    private final Hoplite plugin;
    private final NamespacedKey kbKey;

    public CrystallizationShard(Hoplite plugin) {
        this.plugin = plugin;
        this.kbKey = new NamespacedKey(plugin, "crystallization_anti_kb");
        registerRecipe();
    }

    public ItemStack getCrystallizationShard() {
        ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.displayName(Component.text("Crystallization Shard", NamedTextColor.WHITE)
                    .decoration(TextDecoration.ITALIC, false));

            meta.lore(List.of(
                    Component.text("CONSUME to crystallize as an immovable", NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false),
                    Component.text("object!", NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false),
                    Component.text("Gain anti-knockback and ", NamedTextColor.GRAY)
                            .append(Component.text("Resistance II", NamedTextColor.LIGHT_PURPLE))
                            .decoration(TextDecoration.ITALIC, false),
                    Component.text("for 15 seconds.", NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false)
            ));

            CustomModelDataComponent cmd = meta.getCustomModelDataComponent();
            cmd.setFloats(List.of(1.0f));
            meta.setCustomModelDataComponent(cmd);

            item.setItemMeta(meta);
        }
        return item;
    }

    private void registerRecipe() {
        NamespacedKey recipeKey = new NamespacedKey(plugin, "crystallization_shard");
        ShapedRecipe recipe = new ShapedRecipe(recipeKey, getCrystallizationShard());

        recipe.shape(
                " H ",
                "ABA",
                " A "
        );
        recipe.setIngredient('H', Material.PLAYER_HEAD);
        recipe.setIngredient('A', Material.AMETHYST_SHARD);
        recipe.setIngredient('B', Material.HONEY_BOTTLE); // Changed to Honey Bottle

        Bukkit.addRecipe(recipe);
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.AMETHYST_SHARD || !item.hasItemMeta()) {
            return;
        }

        CustomModelDataComponent cmd = item.getItemMeta().getCustomModelDataComponent();
        if (cmd.getFloats().isEmpty() || cmd.getFloats().get(0) != 1.0f) {
            return;
        }

        event.setCancelled(true);
        Player player = event.getPlayer();

        // Consume 1 shard
        item.setAmount(item.getAmount() - 1);

        // Play instant amethyst breaking sounds
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_BREAK, 1.0f, 0.8f);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_AMETHYST_CLUSTER_BREAK, 1.0f, 1.2f);

        // Apply Resistance II for 15 seconds (300 ticks)
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 300, 1, false, true, true));

        // Apply 80% Anti-Knockback attribute
        AttributeInstance kbAttr = player.getAttribute(Attribute.KNOCKBACK_RESISTANCE);
        if (kbAttr != null) {
            kbAttr.getModifiers().stream()
                    .filter(m -> m.getKey().equals(kbKey))
                    .forEach(kbAttr::removeModifier);

            AttributeModifier kbModifier = new AttributeModifier(
                    kbKey,
                    0.8, // 80% anti-knockback
                    AttributeModifier.Operation.ADD_NUMBER
            );
            kbAttr.addModifier(kbModifier);
        }

        // Repeating ambient sound task during active duration
        new BukkitRunnable() {
            int ticksPassed = 0;

            @Override
            public void run() {
                if (!player.isOnline() || player.isDead() || ticksPassed >= 300) {
                    AttributeInstance attr = player.getAttribute(Attribute.KNOCKBACK_RESISTANCE);
                    if (attr != null) {
                        attr.getModifiers().stream()
                                .filter(m -> m.getKey().equals(kbKey))
                                .forEach(attr::removeModifier);
                    }
                    this.cancel();
                    return;
                }

                if (ticksPassed % 15 == 0) {
                    player.getWorld().playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_RESONATE, 0.5f, 1.3f);
                    player.getWorld().playSound(player.getLocation(), Sound.ITEM_SHIELD_BLOCK, 0.2f, 0.5f);
                }

                ticksPassed += 5;
            }
        }.runTaskTimer(plugin, 0L, 5L);
    }
}