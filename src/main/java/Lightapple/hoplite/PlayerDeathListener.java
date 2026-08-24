package Lightapple.hoplite;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class PlayerDeathListener implements Listener {

    private final Hoplite plugin;

    public PlayerDeathListener(Hoplite plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        int nuggetAmount = 36;

        if (killer != null && killer.getEquipment() != null) {
            ItemStack leggings = killer.getEquipment().getLeggings();
            if (leggings != null && leggings.hasItemMeta()) {
                NamespacedKey banditKey = new NamespacedKey(plugin, "bandit_leggings");
                if (leggings.getItemMeta().getPersistentDataContainer().has(banditKey, PersistentDataType.BYTE)) {
                    nuggetAmount = (int) (nuggetAmount * 1.5); // 54 nuggets (50% bonus)
                }
            }
        }

        event.getDrops().add(new ItemStack(Material.GOLD_NUGGET, nuggetAmount));
    }
}