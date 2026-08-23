package Lightapple.hoplite;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;

import java.util.List;

public class LightNetheriteSword {

    private final Hoplite plugin;

    public LightNetheriteSword(Hoplite plugin) {
        this.plugin = plugin;
        registerRecipe();
    }

    public ItemStack getLightNetheriteSword() {
        ItemStack item = new ItemStack(Material.NETHERITE_SWORD);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.displayName(Component.text("Light Netherite Sword", NamedTextColor.WHITE)
                    .decoration(TextDecoration.ITALIC, false));

            // Custom lore matching the screenshot layout
            meta.lore(List.of(
                    Component.text("A slightly degraded netherite sword.", NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false),
                    Component.empty(),
                    Component.text("When in Main Hand:", NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false),
                    Component.text(" 7.5 Attack Damage", NamedTextColor.GREEN)
                            .decoration(TextDecoration.ITALIC, false),
                    Component.text(" 1.6 Attack Speed", NamedTextColor.GREEN)
                            .decoration(TextDecoration.ITALIC, false)
            ));

            // Hides default "+6.5 / -2.4" vanilla attribute text
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

            // Component method for custom model threshold 2
            CustomModelDataComponent cmd = meta.getCustomModelDataComponent();
            cmd.setFloats(List.of(2.0f));
            meta.setCustomModelDataComponent(cmd);

            // Mechanical attributes (6.5 damage + 1.0 base = 7.5 total damage)
            AttributeModifier damageModifier = new AttributeModifier(
                    new NamespacedKey(plugin, "light_netherite_sword_damage"),
                    6.5,
                    AttributeModifier.Operation.ADD_NUMBER,
                    EquipmentSlotGroup.MAINHAND
            );
            meta.addAttributeModifier(Attribute.ATTACK_DAMAGE, damageModifier);

            // Mechanical attributes (4.0 base - 2.4 = 1.6 total attack speed)
            AttributeModifier speedModifier = new AttributeModifier(
                    new NamespacedKey(plugin, "light_netherite_sword_speed"),
                    -2.4,
                    AttributeModifier.Operation.ADD_NUMBER,
                    EquipmentSlotGroup.MAINHAND
            );
            meta.addAttributeModifier(Attribute.ATTACK_SPEED, speedModifier);

            item.setItemMeta(meta);
        }
        return item;
    }

    private void registerRecipe() {
        NamespacedKey key = new NamespacedKey(plugin, "light_netherite_sword");
        ShapedRecipe recipe = new ShapedRecipe(key, getLightNetheriteSword());

        recipe.shape(
                " S ",
                "ODO",
                " B "
        );
        recipe.setIngredient('S', Material.NETHERITE_SCRAP);
        recipe.setIngredient('O', Material.OBSIDIAN);
        recipe.setIngredient('D', Material.DIAMOND_SWORD);
        recipe.setIngredient('B', Material.BLAZE_ROD);

        Bukkit.addRecipe(recipe);
    }
}