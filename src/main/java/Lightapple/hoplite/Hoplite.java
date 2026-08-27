package Lightapple.hoplite;

import org.bukkit.plugin.java.JavaPlugin;

public final class Hoplite extends JavaPlugin {

    private LightApple lightApple;
    private Shortsword shortsword;
    private CustomTrident customTrident;
    private CustomTotem customTotem;
    private LightAnvil lightAnvil;
    private BundledArrows bundledArrows;
    private Shortbow shortbow;
    private LightNetheriteSword lightNetheriteSword;
    private CrystallizationShard crystallizationShard;
    private GoldenHead goldenHead;
    private SmeltersPickaxe smeltersPickaxe;
    private SuperSmeltersPickaxe superSmeltersPickaxe;
    private BlazingCrossbow blazingCrossbow;
    private CactusChestplate cactusChestplate;
    private BanditLeggings banditLeggings;
    private CowboyBoots cowboyBoots;
    private SkeletonLeggings skeletonLeggings;
    private AxolotlBoots axolotlBoots;
    private AgonyPotion agonyPotion;
    private CustomBundle customBundle;
    private VerySuspiciousStew verySuspiciousStew;
    private CustomPlayerHead customPlayerHead;
    private Panacea panacea;
    private CustomFishingRod customFishingRod;
    private PortableVillager portableVillager;
    private ExplosivePickaxe explosivePickaxe;
    private LumberjacksAxe lumberjacksAxe;
    private TrackerPack trackerPack;
    private NetherReactorCore netherReactorCore;
    private RecipeBook recipeBook;

    @Override
    public void onEnable() {
        this.lightApple = new LightApple(this);
        this.shortsword = new Shortsword(this);
        this.customTrident = new CustomTrident(this);
        this.customTotem = new CustomTotem(this);
        this.lightAnvil = new LightAnvil(this);
        this.bundledArrows = new BundledArrows(this);
        this.shortbow = new Shortbow(this);
        this.lightNetheriteSword = new LightNetheriteSword(this);
        this.crystallizationShard = new CrystallizationShard(this);
        this.goldenHead = new GoldenHead(this);
        this.smeltersPickaxe = new SmeltersPickaxe(this);
        this.superSmeltersPickaxe = new SuperSmeltersPickaxe(this);
        this.blazingCrossbow = new BlazingCrossbow(this);
        this.cactusChestplate = new CactusChestplate(this);
        this.banditLeggings = new BanditLeggings(this);
        this.cowboyBoots = new CowboyBoots(this);
        this.skeletonLeggings = new SkeletonLeggings(this);
        this.axolotlBoots = new AxolotlBoots(this);
        this.agonyPotion = new AgonyPotion(this);
        this.customBundle = new CustomBundle(this);
        this.verySuspiciousStew = new VerySuspiciousStew(this);
        this.customPlayerHead = new CustomPlayerHead(this);
        this.panacea = new Panacea(this);
        this.customFishingRod = new CustomFishingRod(this);
        this.portableVillager = new PortableVillager(this);
        this.explosivePickaxe = new ExplosivePickaxe(this);
        this.lumberjacksAxe = new LumberjacksAxe(this);
        this.trackerPack = new TrackerPack(this);
        this.netherReactorCore = new NetherReactorCore(this);

        this.recipeBook = new RecipeBook(
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
                goldenHead,
                smeltersPickaxe,
                superSmeltersPickaxe,
                blazingCrossbow,
                cactusChestplate,
                banditLeggings,
                cowboyBoots,
                skeletonLeggings,
                axolotlBoots,
                agonyPotion,
                customBundle,
                verySuspiciousStew,
                panacea,
                customFishingRod,
                portableVillager,
                explosivePickaxe,
                lumberjacksAxe,
                trackerPack,
                netherReactorCore
        );

        // Register listeners
        getServer().getPluginManager().registerEvents(recipeBook, this);
        getServer().getPluginManager().registerEvents(explosivePickaxe, this);
        getServer().getPluginManager().registerEvents(lumberjacksAxe, this);
        getServer().getPluginManager().registerEvents(trackerPack, this);
        getServer().getPluginManager().registerEvents(netherReactorCore, this);
        getServer().getPluginManager().registerEvents(customBundle, this);
        getServer().getPluginManager().registerEvents(verySuspiciousStew, this);
        getServer().getPluginManager().registerEvents(customPlayerHead, this);
        getServer().getPluginManager().registerEvents(panacea, this);
        getServer().getPluginManager().registerEvents(customFishingRod, this);
        getServer().getPluginManager().registerEvents(portableVillager, this);
        getServer().getPluginManager().registerEvents(new ProjectileHitListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(this), this);
        getServer().getPluginManager().registerEvents(new CowboyBootsListener(this), this);
        getServer().getPluginManager().registerEvents(new SkeletonLeggingsListener(this), this);

        // Register commands
        if (getCommand("giverecipebook") != null) {
            getCommand("giverecipebook").setExecutor(recipeBook);
        }
        if (getCommand("recipebook") != null) {
            getCommand("recipebook").setExecutor(recipeBook);
        }

        getLogger().info("Hoplite plugin enabled successfully!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Hoplite plugin disabled.");
    }

    public LightApple getLightApple() { return lightApple; }
    public Shortsword getShortsword() { return shortsword; }
    public CustomTrident getCustomTrident() { return customTrident; }
    public CustomTotem getCustomTotem() { return customTotem; }
    public LightAnvil getLightAnvil() { return lightAnvil; }
    public BundledArrows getBundledArrows() { return bundledArrows; }
    public Shortbow getShortbow() { return shortbow; }
    public LightNetheriteSword getLightNetheriteSword() { return lightNetheriteSword; }
    public CrystallizationShard getCrystallizationShard() { return crystallizationShard; }
    public GoldenHead getGoldenHead() { return goldenHead; }
    public SmeltersPickaxe getSmeltersPickaxe() { return smeltersPickaxe; }
    public SuperSmeltersPickaxe getSuperSmeltersPickaxe() { return superSmeltersPickaxe; }
    public BlazingCrossbow getBlazingCrossbow() { return blazingCrossbow; }
    public CactusChestplate getCactusChestplate() { return cactusChestplate; }
    public BanditLeggings getBanditLeggings() { return banditLeggings; }
    public CowboyBoots getCowboyBoots() { return cowboyBoots; }
    public SkeletonLeggings getSkeletonLeggings() { return skeletonLeggings; }
    public AxolotlBoots getAxolotlBoots() { return axolotlBoots; }
    public AgonyPotion getAgonyPotion() { return agonyPotion; }
    public CustomBundle getCustomBundle() { return customBundle; }
    public VerySuspiciousStew getVerySuspiciousStew() { return verySuspiciousStew; }
    public CustomPlayerHead getCustomPlayerHead() { return customPlayerHead; }
    public Panacea getPanacea() { return panacea; }
    public CustomFishingRod getCustomFishingRod() { return customFishingRod; }
    public PortableVillager getPortableVillager() { return portableVillager; }
    public ExplosivePickaxe getExplosivePickaxe() { return explosivePickaxe; }
    public LumberjacksAxe getLumberjacksAxe() { return lumberjacksAxe; }
    public TrackerPack getTrackerPack() { return trackerPack; }
    public NetherReactorCore getNetherReactorCore() { return netherReactorCore; }
}