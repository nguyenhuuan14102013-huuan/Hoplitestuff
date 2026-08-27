package Lightapple.hoplite;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class PortableVillager implements Listener {

    private final Hoplite plugin;
    private final NamespacedKey idKey;
    private final NamespacedKey professionKey;

    private static final Villager.Profession[] PROFESSIONS = {
            Villager.Profession.NONE,
            Villager.Profession.FARMER,
            Villager.Profession.LIBRARIAN,
            Villager.Profession.ARMORER,
            Villager.Profession.BUTCHER,
            Villager.Profession.CARTOGRAPHER,
            Villager.Profession.CLERIC,
            Villager.Profession.FISHERMAN,
            Villager.Profession.FLETCHER,
            Villager.Profession.LEATHERWORKER,
            Villager.Profession.MASON,
            Villager.Profession.SHEPHERD,
            Villager.Profession.TOOLSMITH,
            Villager.Profession.WEAPONSMITH
    };

    public PortableVillager(Hoplite plugin) {
        this.plugin = plugin;
        this.idKey = new NamespacedKey(plugin, "is_portable_villager");
        this.professionKey = new NamespacedKey(plugin, "villager_profession");

        registerRecipe();
    }

    private void registerRecipe() {
        NamespacedKey recipeKey = new NamespacedKey(plugin, "portable_villager_recipe");

        if (Bukkit.getRecipe(recipeKey) != null) {
            Bukkit.removeRecipe(recipeKey);
        }

        ShapedRecipe recipe = new ShapedRecipe(recipeKey, createPortableVillager(Villager.Profession.NONE));
        recipe.shape(
                "WEW",
                "WLW",
                "WCW"
        );

        recipe.setIngredient('W', Material.WHEAT);
        recipe.setIngredient('E', Material.EMERALD);
        recipe.setIngredient('L', Material.LECTERN);
        recipe.setIngredient('C', Material.COMPOSTER);

        Bukkit.addRecipe(recipe);
    }

    public ItemStack createPortableVillager(Villager.Profession profession) {
        ItemStack item = new ItemStack(Material.VILLAGER_SPAWN_EGG);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.displayName(Component.text("Portable Villager", NamedTextColor.LIGHT_PURPLE)
                    .decoration(TextDecoration.ITALIC, false));

            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            pdc.set(idKey, PersistentDataType.BOOLEAN, true);
            pdc.set(professionKey, PersistentDataType.STRING, profession.name());

            updateItemLore(meta, profession);
            item.setItemMeta(meta);
        }

        return item;
    }

    private void updateItemLore(ItemMeta meta, Villager.Profession profession) {
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("A special item that allows you", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("to choose the villager's profession.", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("Selected Profession: ", NamedTextColor.WHITE)
                .append(Component.text(formatProfessionName(profession), NamedTextColor.GOLD))
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("Left-Click to change profession", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("Right-Click block to spawn", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));

        meta.lore(lore);
    }

    private String formatProfessionName(Villager.Profession profession) {
        String name = profession.name();
        if (name.equals("NONE")) return "Unemployed";
        return name.charAt(0) + name.substring(1).toLowerCase();
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;

        ItemStack item = event.getItem();
        if (item == null || !item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        if (!pdc.has(idKey, PersistentDataType.BOOLEAN)) return;

        Player player = event.getPlayer();
        Action action = event.getAction();

        // Left-Click: Cycle through professions
        if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
            event.setCancelled(true);

            String currentProfName = pdc.getOrDefault(professionKey, PersistentDataType.STRING, "NONE");
            Villager.Profession currentProf = Villager.Profession.valueOf(currentProfName);

            int nextIndex = (getProfessionIndex(currentProf) + 1) % PROFESSIONS.length;
            Villager.Profession nextProf = PROFESSIONS[nextIndex];

            pdc.set(professionKey, PersistentDataType.STRING, nextProf.name());
            updateItemLore(meta, nextProf);
            item.setItemMeta(meta);

            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.2f);
            player.sendActionBar(Component.text("Profession: " + formatProfessionName(nextProf), NamedTextColor.GOLD));
            return;
        }

        // Right-Click Block: Spawn the villager
        if (action == Action.RIGHT_CLICK_BLOCK) {
            event.setCancelled(true);
            Block clickedBlock = event.getClickedBlock();
            if (clickedBlock == null) return;

            String profName = pdc.getOrDefault(professionKey, PersistentDataType.STRING, "NONE");
            Villager.Profession profession = Villager.Profession.valueOf(profName);

            var spawnLocation = clickedBlock.getRelative(event.getBlockFace()).getLocation().add(0.5, 0, 0.5);

            Villager villager = (Villager) player.getWorld().spawnEntity(spawnLocation, EntityType.VILLAGER);
            villager.setProfession(profession);
            villager.setAdult();

            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_YES, 1.0f, 1.0f);

            // Consume the item
            item.setAmount(item.getAmount() - 1);
        }
    }

    private int getProfessionIndex(Villager.Profession profession) {
        for (int i = 0; i < PROFESSIONS.length; i++) {
            if (PROFESSIONS[i] == profession) return i;
        }
        return 0;
    }
}