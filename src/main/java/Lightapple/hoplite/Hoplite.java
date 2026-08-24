package Lightapple.hoplite;

import org.bukkit.plugin.java.JavaPlugin;

public final class Hoplite extends JavaPlugin {

    @Override
    public void onEnable() {
        // Instantiate custom items
        LightApple lightApple = new LightApple(this);
        Shortsword shortsword = new Shortsword(this);
        CustomTrident customTrident = new CustomTrident(this);
        CustomTotem customTotem = new CustomTotem(this);
        LightAnvil lightAnvil = new LightAnvil(this);
        BundledArrows bundledArrows = new BundledArrows(this);
        Shortbow shortbow = new Shortbow(this);
        LightNetheriteSword lightNetheriteSword = new LightNetheriteSword(this);
        CrystallizationShard crystallizationShard = new CrystallizationShard(this);
        GoldenHead goldenHead = new GoldenHead(this);
        CustomPlayerHead customPlayerHead = new CustomPlayerHead(this);

        // Register event listeners for items with right-click / interactive mechanics
        getServer().getPluginManager().registerEvents(crystallizationShard, this);
        getServer().getPluginManager().registerEvents(goldenHead, this);
        getServer().getPluginManager().registerEvents(customPlayerHead, this);

        // Register Recipe Book GUI listener and command (10 craftable items)
        RecipeBook recipeBook = new RecipeBook(
                this,
                lightApple,
                shortsword,
                customTrident,
                customTotem,
                lightAnvil,
                bundledArrows,
                shortbow,
                lightNetheriteSword,
                crystallizationShard,
                goldenHead
        );

        getServer().getPluginManager().registerEvents(recipeBook, this);

        if (getCommand("recipebook") != null) {
            getCommand("recipebook").setExecutor(recipeBook);
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}