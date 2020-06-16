package me.gimme.gimmehardcore.listeners;

import me.gimme.gimmehardcore.GimmeHardcore;
import me.gimme.gimmehardcore.utils.DelayedTask;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class NetherBuildProtection implements Listener {
    private Plugin plugin;

    @Nullable
    private Material convertBlock;
    private double defaultDelay;
    private Map<String, Double> blockSpecificDelays = new HashMap<>();
    private boolean allowAllPassable;
    private Set<Material> blockPlaceWhitelist = new HashSet<>();
    private Set<Material> blockBreakWhitelist = new HashSet<>();
    private Set<Material> blockBlacklist = new HashSet<>();

    // Active block reset tasks
    private Map<Location, BlockResetTask> blockResetTasks = new HashMap<>();

    public NetherBuildProtection(@NotNull Plugin plugin, @NotNull FileConfiguration config) {
        this.plugin = plugin;

        String convertBlock = config.getString(GimmeHardcore.CONFIG_NETHER_BUILD_PROTECTION_BLOCK);
        if (convertBlock != null) this.convertBlock = Material.matchMaterial(convertBlock);

        this.defaultDelay = config.getDouble(GimmeHardcore.CONFIG_NETHER_BUILD_PROTECTION_DEFAULT_DELAY);
        ConfigurationSection blockDelaysSection = config.getConfigurationSection(GimmeHardcore.CONFIG_NETHER_BUILD_PROTECTION_BLOCK_SPECIFIC_DELAY);
        if (blockDelaysSection != null) {
            for (String key : blockDelaysSection.getKeys(false)) {
                blockSpecificDelays.put(key, blockDelaysSection.getDouble(key));
            }
        }
        this.allowAllPassable = config.getBoolean(GimmeHardcore.CONFIG_NETHER_BUILD_PROTECTION_ALLOW_ALL_PASSABLE_BLOCKS);

        for (String type : config.getStringList(GimmeHardcore.CONFIG_NETHER_BUILD_PROTECTION_BLOCK_PLACE_WHITELIST)) {
            blockPlaceWhitelist.add(Material.matchMaterial(type));
        }
        for (String type : config.getStringList(GimmeHardcore.CONFIG_NETHER_BUILD_PROTECTION_BLOCK_BREAK_WHITELIST)) {
            blockBreakWhitelist.add(Material.matchMaterial(type));
        }
        for (String type : config.getStringList(GimmeHardcore.CONFIG_NETHER_BUILD_PROTECTION_BLOCK_BLACKLIST)) {
            blockBlacklist.add(Material.matchMaterial(type));
        }
    }

    public void onDisable() {
        //Clean up
        new ArrayList<>(blockResetTasks.values()).forEach(BlockResetTask::cleanUp);
    }

    @EventHandler(priority = EventPriority.LOW)
    private void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) return;
        if (event.getPlayer().getGameMode().equals(GameMode.CREATIVE)) return;
        if (!event.getBlock().getWorld().getEnvironment().equals(World.Environment.NETHER)) return;
        if (!blockBlacklist.contains(event.getBlock().getType())) { // Don't allow blacklisted blocks
            if (allowAllPassable && event.getBlock().isPassable()) return; // Allow passable blocks
            if (blockBreakWhitelist.contains(event.getBlock().getType())) return; // Allow whitelisted blocks
            if (blockResetTasks.containsKey(event.getBlock().getLocation())) return; // Allow breaking player-placed blocks
        }

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW)
    private void onBlockPlace(BlockPlaceEvent event) {
        if (event.isCancelled()) return;
        if (event.getPlayer().getGameMode().equals(GameMode.CREATIVE)) return;
        if (!event.getBlock().getWorld().getEnvironment().equals(World.Environment.NETHER)) return;
        if (!blockBlacklist.contains(event.getBlock().getType())) { // Don't allow blacklisted blocks
            if (allowAllPassable && event.getBlock().isPassable()) return; // Allow passable blocks
            if (blockPlaceWhitelist.contains(event.getBlock().getType())) return; // Allow whitelisted blocks
        }

        if (!resetBlock(event.getBlockReplacedState(), event.getBlockPlaced().getType())) event.setCancelled(true);
    }

    /**
     * Handles falling blocks.
     */
    @EventHandler(priority = EventPriority.LOW)
    public void onEntityChangeBlockEvent(EntityChangeBlockEvent event) {
        if (event.isCancelled()) return;
        if (!event.getBlock().getWorld().getEnvironment().equals(World.Environment.NETHER)) return;
        if (!event.getEntityType().equals(EntityType.FALLING_BLOCK)) return;
        if (event.getTo().equals(Material.AIR)) return; // Not on start falling, only on land
        if (!blockBlacklist.contains(event.getBlock().getType())) { // Don't allow blacklisted blocks
            if (blockPlaceWhitelist.contains(event.getBlock().getType())) return; // Allow whitelisted blocks
        }

        if (!resetBlock(event.getBlock().getState(), event.getTo())) event.setCancelled(true);
    }

    private boolean resetBlock(@NotNull BlockState state, @NotNull Material newBlock) {
        double delay = blockSpecificDelays.getOrDefault(newBlock.getKey().getKey(), defaultDelay);
        return resetBlock(state, delay);
    }

    private boolean resetBlock(@NotNull BlockState state, double delay) {
        if (delay < 0) return false;

        Location location = state.getLocation();
        long ticksDelay = Math.round(20 * delay);

        BlockResetTask currentTask = blockResetTasks.get(location);
        if (currentTask != null) {
            currentTask.restart(ticksDelay);
            return true;
        }

        BlockResetTask task = new BlockResetTask(state);
        blockResetTasks.put(location, task);
        task.start(ticksDelay);
        return true;
    }

    private class BlockResetTask extends DelayedTask {
        private boolean cleanUp = false;

        public BlockResetTask(@NotNull BlockState state) {
            super(plugin);
            setOnFinish(() -> {
                Location location = state.getLocation();
                blockResetTasks.remove(location);

                Material currentType = location.getBlock().getType();
                if (cleanUp || convertBlock == null || currentType.equals(convertBlock) || location.getBlock().isPassable()) {
                    state.update(true, !cleanUp);
                } else {
                    location.getBlock().setType(convertBlock, true);
                    if (!resetBlock(state, convertBlock)) state.update(true, false);
                }
            });
        }

        private void cleanUp() {
            cleanUp = true;
            finish();
        }
    }
}
