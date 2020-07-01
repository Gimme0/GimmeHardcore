package me.gimme.gimmehardcore;

import me.gimme.gimmehardcore.advancements.AdvancementManager;
import me.gimme.gimmehardcore.crafting.RecipeModification;
import me.gimme.gimmehardcore.hooks.CrazyAdvancementsHook;
import me.gimme.gimmehardcore.listeners.*;
import me.gimme.gimmehardcore.respawn.RespawnDelay;
import me.gimme.gimmehardcore.listeners.DeathDropDisabler;
import org.bukkit.*;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class GimmeHardcore extends JavaPlugin {

    // Config paths
    public static final String CONFIG_HARDCORE_ADVANCEMENTS = "hardcore_advancements";
    public static final String CONFIG_HARDCORE_ADVANCEMENTS_ENABLED = CONFIG_HARDCORE_ADVANCEMENTS + ".enabled";
    public static final String CONFIG_HARDCORE_ADVANCEMENTS_SILENT = CONFIG_HARDCORE_ADVANCEMENTS + ".silent";

    public static final String CONFIG_PREVENT_NIGHT_SKIP = "night_skip_prevented";
    public static final String CONFIG_BROADCAST_NIGHT_SKIP_PREVENTED = "night_skip_prevented_broadcast";

    public static final String CONFIG_RESPAWN_DELAY = "respawn_delay";
    public static final String CONFIG_RESPAWN_AUTO = "respawn_auto";
    public static final String CONFIG_RESPAWN_AUTO_DELAY = "respawn_auto_delay";
    public static final String CONFIG_RESPAWN_SCREEN_TITLE = "respawn_screen_title";
    public static final String CONFIG_RESPAWN_SCREEN_SUBTITLE = "respawn_screen_subtitle";
    public static final String CONFIG_RESPAWN_FINISHED_TITLE = "respawn_finished_title";
    public static final String CONFIG_RESPAWN_FINISHED_SUBTITLE = "respawn_finished_subtitle";
    public static final String CONFIG_RESPAWN_SPECTATOR_MAX_DISTANCE = "respawn_spectator_max_distance";
    public static final String CONFIG_RESPAWN_TIMER_IN_PLAYER_LIST = "respawn_timer_in_player_list";
    public static final String CONFIG_ITEM_DROP_CHANCE_ON_DEATH = "item_drop_chance_on_death";
    public static final String CONFIG_ULTRA_HARDCORE = "ultra_hardcore";
    public static final String CONFIG_UNBREAKABLE_BLOCKS = "unbreakable_blocks";
    public static final String CONFIG_MODIFIED_DROP_RATE_CYCLE = "modified_drop_rate_cycle";
    public static final String CONFIG_MODIFIED_DROP_RATE_CYCLE_ENABLED = CONFIG_MODIFIED_DROP_RATE_CYCLE + ".enabled";
    public static final String CONFIG_MODIFIED_DROP_RATE_CYCLE_START = CONFIG_MODIFIED_DROP_RATE_CYCLE + ".start_time";
    public static final String CONFIG_MODIFIED_DROP_RATE_CYCLE_END = CONFIG_MODIFIED_DROP_RATE_CYCLE + ".end_time";
    public static final String CONFIG_MODIFIED_DROP_RATE_CYCLE_BLOCKS = CONFIG_MODIFIED_DROP_RATE_CYCLE + ".blocks";

    public static final String CONFIG_ROSE_BUSH = "rose_bush";
    public static final String CONFIG_ROSE_BUSH_ENABLED = CONFIG_ROSE_BUSH + ".enabled";
    public static final String CONFIG_ROSE_BUSH_GLISTERING_MELON_RECIPE = CONFIG_ROSE_BUSH + ".glistering_melon_recipe";
    public static final String CONFIG_ROSE_BUSH_HIDDEN_CYCLE = CONFIG_ROSE_BUSH + ".hidden_cycle";
    public static final String CONFIG_ROSE_BUSH_HIDDEN_CYCLE_ENABLED = CONFIG_ROSE_BUSH_HIDDEN_CYCLE + ".enabled";
    public static final String CONFIG_ROSE_BUSH_HIDDEN_CYCLE_START = CONFIG_ROSE_BUSH_HIDDEN_CYCLE + ".start_time";
    public static final String CONFIG_ROSE_BUSH_HIDDEN_CYCLE_END = CONFIG_ROSE_BUSH_HIDDEN_CYCLE + ".end_time";
    public static final String CONFIG_ROSE_BUSH_HIDDEN_CYCLE_Y_MIN = CONFIG_ROSE_BUSH_HIDDEN_CYCLE + ".y_min";
    public static final String CONFIG_ROSE_BUSH_HIDDEN_CYCLE_Y_MAX = CONFIG_ROSE_BUSH_HIDDEN_CYCLE + ".y_max";
    public static final String CONFIG_ROSE_BUSH_HIDDEN_CYCLE_UPDATE_FREQUENCY = CONFIG_ROSE_BUSH_HIDDEN_CYCLE + ".update_frequency";

    public static final String CONFIG_FOOD_MODIFIERS = "food_modifiers";
    public static final String CONFIG_FOOD_DEFAULT_MODIFIER = CONFIG_FOOD_MODIFIERS + ".default";
    public static final String CONFIG_FOOD_SPECIFIC_MODIFIERS = CONFIG_FOOD_MODIFIERS + ".food";
    public static final String CONFIG_UNSTACKABLE_ITEMS = "unstackable_items";
    public static final String CONFIG_WRATH = "wrath_of_the_nether";
    public static final String CONFIG_WRATH_ENABLED = CONFIG_WRATH + ".enabled";
    public static final String CONFIG_WRATH_MESSAGE = CONFIG_WRATH + ".on_wrath_message";
    public static final String CONFIG_WRATH_MESSAGE_TITLE = CONFIG_WRATH_MESSAGE + ".title";
    public static final String CONFIG_WRATH_MESSAGE_SUBTITLE = CONFIG_WRATH_MESSAGE + ".subtitle";
    public static final String CONFIG_WRATH_ENTER_NETHER_MESSAGE = CONFIG_WRATH + ".on_enter_nether_message";
    public static final String CONFIG_WRATH_ENTER_NETHER_MESSAGE_TITLE = CONFIG_WRATH_ENTER_NETHER_MESSAGE + ".title";
    public static final String CONFIG_WRATH_ENTER_NETHER_MESSAGE_SUBTITLE = CONFIG_WRATH_ENTER_NETHER_MESSAGE + ".subtitle";
    public static final String CONFIG_NETHER_BUILD_PROTECTION = "nether_build_protection";
    public static final String CONFIG_NETHER_BUILD_PROTECTION_ENABLED = CONFIG_NETHER_BUILD_PROTECTION + ".enabled";
    public static final String CONFIG_NETHER_BUILD_PROTECTION_BLOCK = CONFIG_NETHER_BUILD_PROTECTION + ".convert_block";
    public static final String CONFIG_NETHER_BUILD_PROTECTION_DEFAULT_DELAY = CONFIG_NETHER_BUILD_PROTECTION + ".default_delay";
    public static final String CONFIG_NETHER_BUILD_PROTECTION_BLOCK_SPECIFIC_DELAY = CONFIG_NETHER_BUILD_PROTECTION + ".block_specific_delay";
    public static final String CONFIG_NETHER_BUILD_PROTECTION_ALLOW_ALL_PASSABLE_BLOCKS = CONFIG_NETHER_BUILD_PROTECTION + ".allow_all_passable_blocks";
    public static final String CONFIG_NETHER_BUILD_PROTECTION_BLOCK_PLACE_WHITELIST = CONFIG_NETHER_BUILD_PROTECTION + ".block_place_whitelist";
    public static final String CONFIG_NETHER_BUILD_PROTECTION_BLOCK_BREAK_WHITELIST = CONFIG_NETHER_BUILD_PROTECTION + ".block_break_whitelist";
    public static final String CONFIG_NETHER_BUILD_PROTECTION_BLOCK_BLACKLIST = CONFIG_NETHER_BUILD_PROTECTION + ".block_blacklist";
    public static final String CONFIG_THE_END = "the_end";
    public static final String CONFIG_THE_END_CRYSTAL_SPAWN_ON_DEATH = CONFIG_THE_END + ".crystal_spawn_on_death";
    public static final String CONFIG_THE_END_SPAWN_BASE = "base";
    public static final String CONFIG_THE_END_SPAWN_EXTRA = "extra";
    public static final String CONFIG_THE_END_SPAWN_VARIANCE = "variance";
    public static final String CONFIG_THE_END_PILLAR = CONFIG_THE_END + ".spawn_around_active_pillar";
    public static final String CONFIG_THE_END_PILLAR_ENABLED = CONFIG_THE_END_PILLAR + ".enabled";
    public static final String CONFIG_THE_END_PILLAR_ACTIVATION_RANGE = CONFIG_THE_END_PILLAR + ".activation_range";
    public static final String CONFIG_THE_END_PILLAR_SPAWN_INTERVAL = CONFIG_THE_END_PILLAR + ".spawn_interval";
    public static final String CONFIG_THE_END_PILLAR_TYPES = CONFIG_THE_END_PILLAR + ".pillar_types";
    public static final String CONFIG_THE_END_PILLAR_TYPES_WEIGHT = "weight";
    public static final String CONFIG_THE_END_PILLAR_TYPES_MAX_SPAWNED_BASE = "max_spawned_base";
    public static final String CONFIG_THE_END_PILLAR_TYPES_MAX_SPAWNED_EXTRA = "max_spawned_extra";
    public static final String CONFIG_THE_END_PILLAR_TYPES_ENTITIES = "entities";
    public static final String CONFIG_DAMAGE_TAKEN_MODIFIER = "damage_taken_modifier";

    private RespawnDelay respawnDelay;
    private NetherBuildProtection netherBuildProtection;
    private NightRoseBush nightRoseBush;
    private RecipeModification recipeModification;
    private CrazyAdvancementsHook crazyAdvancementsHook;

    @Override
    public void onEnable() {
        respawnDelay = new RespawnDelay();
        netherBuildProtection = new NetherBuildProtection(this, getConfig());
        nightRoseBush = new NightRoseBush(this, getConfig());
        recipeModification = new RecipeModification(this, getServer(), getConfig());
        crazyAdvancementsHook = new CrazyAdvancementsHook(this);

        initConfig();
        initCommands();
        initListeners();
        setupAdvancements();

        recipeModification.modifyRecipes();

        boolean naturalRegeneration = !getConfig().getBoolean(CONFIG_ULTRA_HARDCORE);
        for (World world : getServer().getWorlds()) {
            world.setGameRule(GameRule.NATURAL_REGENERATION, naturalRegeneration);
        }
        getLogger().info("Game rule - Natural regeneration: " + naturalRegeneration);

        getLogger().info("Done");
    }

    @Override
    public void onDisable() {
        respawnDelay.onDisable();
        netherBuildProtection.onDisable();
        nightRoseBush.onDisable();
        crazyAdvancementsHook.onDisable();
    }

    private void initConfig() {
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();

        final String RECOMMENDED_CONFIG_PATH = "config(recommended).yml";
        if (!new File(getDataFolder(), RECOMMENDED_CONFIG_PATH).exists()) saveResource(RECOMMENDED_CONFIG_PATH, false);
    }

    private void initCommands() {
        final String HC_COMMAND = "hc";
        final String RESPAWN_COMMAND = "hc respawn";

        getCommand(HC_COMMAND).setExecutor(respawnDelay);
        getCommand(RESPAWN_COMMAND).setExecutor(respawnDelay);
    }

    private void initListeners() {
        if (getConfig().getBoolean(CONFIG_PREVENT_NIGHT_SKIP)) {
            registerEvents(new NightSkipEventListener());
        }

        if (getConfig().getInt(CONFIG_RESPAWN_DELAY) > 0) {
            registerEvents(respawnDelay);
        }

        if (getConfig().getBoolean(GimmeHardcore.CONFIG_MODIFIED_DROP_RATE_CYCLE_ENABLED)) {
            registerEvents(new ModifiedDropRateCycle(getConfig()));
        }

        if (getConfig().getBoolean(GimmeHardcore.CONFIG_WRATH_ENABLED)) {
            registerEvents(new WrathOfTheNether(getConfig()));
        }

        if (getConfig().getBoolean(GimmeHardcore.CONFIG_NETHER_BUILD_PROTECTION_ENABLED)) {
            registerEvents(netherBuildProtection);
        }

        if (getConfig().getBoolean(CONFIG_ROSE_BUSH_ENABLED)
                && getConfig().getBoolean(CONFIG_ROSE_BUSH_HIDDEN_CYCLE_ENABLED)) {
            registerEvents(nightRoseBush);
            nightRoseBush.start();
        }


        registerEvents(new DeathDropDisabler(getConfig()));
        registerEvents(new TheEndDifficulty(this, getConfig()));
        registerEvents(new UnbreakableBlocks(getConfig()));
        registerEvents(new FoodRestriction(getConfig()));
        registerEvents(new DamageTakenModifier(getConfig()));
        registerEvents(new UnstackableItems(getConfig()));
    }

    private void setupAdvancements() {
        if (getConfig().getBoolean(CONFIG_HARDCORE_ADVANCEMENTS_ENABLED)) {
            new AdvancementManager(this, crazyAdvancementsHook).setupAdvancements();
        }
    }

    private void registerEvents(Listener listener) {
        getServer().getPluginManager().registerEvents(listener, this);
    }

}
