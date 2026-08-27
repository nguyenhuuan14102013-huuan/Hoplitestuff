package Lightapple.hoplite;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Comparator;
import java.util.List;

public class TrackerPack implements Listener {

    private final Hoplite plugin;
    private final NamespacedKey key;

    public TrackerPack(Hoplite plugin) {
        this.plugin = plugin;
        this.key = new NamespacedKey(plugin, "tracker_pack");
        registerRecipe();
    }

    public ItemStack getTrackerPack() {
        ItemStack item = new ItemStack(Material.BONE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("Tracker Pack", NamedTextColor.GOLD)
                    .decoration(TextDecoration.ITALIC, false));
            meta.lore(List.of(
                    Component.text("A pack of hungry wolves with a good", NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false),
                    Component.text("sense of smell.", NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false),
                    Component.empty(),
                    Component.text("RIGHT CLICK to summon a pack of wolves", NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false),
                    Component.text("that track down the last known location", NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false),
                    Component.text("of a nearby enemy player on the surface.", NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false),
                    Component.text("After reaching their target, the pack", NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false),
                    Component.text("will fight alongside you.", NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false)
            ));
            meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
            item.setItemMeta(meta);
        }
        return item;
    }

    private void registerRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(key, getTrackerPack());
        recipe.shape(
                " B ",
                "BCB",
                " B "
        );
        recipe.setIngredient('B', Material.BONE);
        recipe.setIngredient('C', Material.COMPASS);
        Bukkit.addRecipe(recipe);
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            ItemStack item = event.getItem();
            if (item != null && item.getType() == Material.BONE && item.hasItemMeta()) {
                ItemMeta meta = item.getItemMeta();
                if (meta.getPersistentDataContainer().has(key, PersistentDataType.BYTE)) {
                    event.setCancelled(true);
                    Player player = event.getPlayer();

                    item.setAmount(item.getAmount() - 1);

                    // Find nearest enemy player in the same world
                    Player target = player.getWorld().getPlayers().stream()
                            .filter(p -> !p.equals(player) && p.getGameMode() == GameMode.SURVIVAL)
                            .min(Comparator.comparingDouble(p -> p.getLocation().distanceSquared(player.getLocation())))
                            .orElse(null);

                    // Spawn 3 wolves
                    for (int i = 0; i < 3; i++) {
                        Wolf wolf = (Wolf) player.getWorld().spawnEntity(player.getLocation(), EntityType.WOLF);
                        wolf.setOwner(player);
                        if (target != null) {
                            wolf.setTarget(target);
                        }
                    }

                    if (target != null) {
                        player.sendMessage(Component.text("Your wolf pack is tracking " + target.getName() + "!", NamedTextColor.GREEN));
                    } else {
                        player.sendMessage(Component.text("Summoned a wolf pack, but no enemy players were nearby.", NamedTextColor.YELLOW));
                    }
                }
            }
        }
    }
}