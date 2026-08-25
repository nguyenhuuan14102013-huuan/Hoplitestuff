package Lightapple.hoplite;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

public class CustomBundle implements Listener {

    private final Hoplite plugin;
    private final NamespacedKey bundleKey;
    private final NamespacedKey dataKey;

    public static class BundleHolder implements InventoryHolder {
        private final ItemStack bundleItem;

        public BundleHolder(ItemStack bundleItem) {
            this.bundleItem = bundleItem;
        }

        public ItemStack getBundleItem() {
            return bundleItem;
        }

        @Override
        public Inventory getInventory() {
            return null;
        }
    }

    public CustomBundle(Hoplite plugin) {
        this.plugin = plugin;
        this.bundleKey = new NamespacedKey(plugin, "custom_bundle_id");
        this.dataKey = new NamespacedKey(plugin, "custom_bundle_data");
        registerRecipe();
    }

    public ItemStack getCustomBundle() {
        ItemStack bundle = new ItemStack(Material.BUNDLE);
        ItemMeta meta = bundle.getItemMeta();

        if (meta != null) {
            meta.displayName(Component.text("Bundle", NamedTextColor.WHITE)
                    .decoration(TextDecoration.ITALIC, false));

            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("A sack for holding items.", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.empty());
            lore.add(Component.text("RIGHT CLICK to open and store items.", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));

            meta.lore(lore);

            PersistentDataContainer container = meta.getPersistentDataContainer();
            if (!container.has(bundleKey, PersistentDataType.STRING)) {
                container.set(bundleKey, PersistentDataType.STRING, UUID.randomUUID().toString());
            }

            bundle.setItemMeta(meta);
        }

        return bundle;
    }

    public boolean isCustomBundle(ItemStack item) {
        if (item == null || item.getType() != Material.BUNDLE || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        return meta != null && meta.getPersistentDataContainer().has(bundleKey, PersistentDataType.STRING);
    }

    private void registerRecipe() {
        NamespacedKey key = new NamespacedKey(plugin, "custom_bundle");

        if (Bukkit.getRecipe(key) != null) {
            Bukkit.removeRecipe(key);
        }

        ShapedRecipe recipe = new ShapedRecipe(key, getCustomBundle());
        recipe.shape(
                " L ",
                "LCL",
                " L "
        );

        recipe.setIngredient('L', Material.LEATHER);
        recipe.setIngredient('C', Material.CHEST);

        Bukkit.addRecipe(recipe);
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            ItemStack item = event.getItem();
            if (isCustomBundle(item)) {
                event.setCancelled(true);

                Player player = event.getPlayer();
                Inventory gui = Bukkit.createInventory(new BundleHolder(item), 27, Component.text("Bundle Storage", NamedTextColor.DARK_GRAY));

                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    String serializedData = meta.getPersistentDataContainer().get(dataKey, PersistentDataType.STRING);
                    if (serializedData != null && !serializedData.isEmpty()) {
                        ItemStack[] contents = deserializeItems(serializedData);
                        if (contents != null) {
                            gui.setContents(contents);
                        }
                    }
                }

                player.openInventory(gui);
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof BundleHolder bundleHolder) {
            ItemStack bundle = bundleHolder.getBundleItem();
            ItemMeta meta = bundle.getItemMeta();

            if (meta != null) {
                String serialized = serializeItems(event.getInventory().getContents());
                meta.getPersistentDataContainer().set(dataKey, PersistentDataType.STRING, serialized);
                bundle.setItemMeta(meta);
            }
        }
    }

    private String serializeItems(ItemStack[] items) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            dataOutput.writeInt(items.length);
            for (ItemStack item : items) {
                dataOutput.writeObject(item);
            }
            dataOutput.close();
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (Exception e) {
            return "";
        }
    }

    private ItemStack[] deserializeItems(String data) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            int length = dataInput.readInt();
            ItemStack[] items = new ItemStack[length];
            for (int i = 0; i < length; i++) {
                items[i] = (ItemStack) dataInput.readObject();
            }
            dataInput.close();
            return items;
        } catch (Exception e) {
            return new ItemStack[27];
        }
    }
}