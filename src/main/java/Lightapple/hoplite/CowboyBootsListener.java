package Lightapple.hoplite;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDismountEvent;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class CowboyBootsListener implements Listener {

    private final Hoplite plugin;
    private final NamespacedKey modifierKey;
    private final double SPEED_BOOST = 0.07116;

    public CowboyBootsListener(Hoplite plugin) {
        this.plugin = plugin;
        this.modifierKey = new NamespacedKey(plugin, "cowboy_boots_horse_speed");
        startHorseSpeedTask();
    }

    private void startHorseSpeedTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getVehicle() instanceof AbstractHorse horse) {
                    boolean wearingBoots = isWearingCowboyBoots(player);
                    var speedAttribute = horse.getAttribute(Attribute.MOVEMENT_SPEED);

                    if (speedAttribute != null) {
                        boolean hasModifier = speedAttribute.getModifier(modifierKey) != null;

                        if (wearingBoots && !hasModifier) {
                            AttributeModifier modifier = new AttributeModifier(
                                    modifierKey,
                                    SPEED_BOOST,
                                    AttributeModifier.Operation.ADD_NUMBER,
                                    EquipmentSlotGroup.ANY
                            );
                            speedAttribute.addModifier(modifier);
                        } else if (!wearingBoots && hasModifier) {
                            speedAttribute.removeModifier(modifierKey);
                        }
                    }
                }
            }
        }, 0L, 10L);
    }

    @EventHandler
    public void onDismount(EntityDismountEvent event) {
        if (event.getEntity() instanceof Player && event.getDismounted() instanceof AbstractHorse horse) {
            var speedAttribute = horse.getAttribute(Attribute.MOVEMENT_SPEED);
            if (speedAttribute != null) {
                speedAttribute.removeModifier(modifierKey);
            }
        }
    }

    private boolean isWearingCowboyBoots(Player player) {
        if (player.getEquipment() == null) return false;
        ItemStack boots = player.getEquipment().getBoots();
        if (boots == null || !boots.hasItemMeta()) return false;

        NamespacedKey cowboyKey = new NamespacedKey(plugin, "cowboy_boots");
        return boots.getItemMeta().getPersistentDataContainer().has(cowboyKey, PersistentDataType.BYTE);
    }
}