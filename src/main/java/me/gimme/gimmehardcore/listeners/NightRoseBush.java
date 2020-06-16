package me.gimme.gimmehardcore.listeners;

import me.gimme.gimmehardcore.GimmeHardcore;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class NightRoseBush implements Listener {
    private static final Material HIDDEN_MATERIAL = Material.AIR;

    private Plugin plugin;
    private FileConfiguration config;

    private Set<Chunk> checkedChunks = new HashSet<>();

    private Map<World, Map<Chunk, Set<BlockState>>> worlds = new HashMap<>();
    private Set<Chunk> activeHidingChunks = new HashSet<>();

    private long startTime;
    private long endTime;

    private int y_min;
    private int y_max;

    public NightRoseBush(@NotNull Plugin plugin, @NotNull FileConfiguration config) {
        this.plugin = plugin;
        this.config = config;

        startTime = config.getInt(GimmeHardcore.CONFIG_ROSE_BUSH_HIDDEN_CYCLE_START);
        endTime = config.getInt(GimmeHardcore.CONFIG_ROSE_BUSH_HIDDEN_CYCLE_END);

        y_min = config.getInt(GimmeHardcore.CONFIG_ROSE_BUSH_HIDDEN_CYCLE_Y_MIN);
        y_max = config.getInt(GimmeHardcore.CONFIG_ROSE_BUSH_HIDDEN_CYCLE_Y_MAX);
    }

    /**
     * Hide/unhide all loaded blocks depending on time. Also cleans up invalid blocks.
     */
    public void start() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (World world : worlds.keySet()) {
                    boolean isRightTime = isRightTime(world);

                    Map<Chunk, Set<BlockState>> chunks = worlds.get(world);
                    for (Chunk chunk : chunks.keySet()) {
                        if (!chunk.isLoaded()) continue;
                        hideChunk(chunks, chunk, isRightTime);
                    }
                }

                // Clean up
                for (Iterator<World> iter = worlds.keySet().iterator(); iter.hasNext(); ) {
                    World world = iter.next();
                    boolean isRightTime = isRightTime(world);

                    Map<Chunk, Set<BlockState>> chunks = worlds.get(world);
                    for (Iterator<Set<BlockState>> iterator = chunks.values().iterator(); iterator.hasNext(); ) {
                        Set<BlockState> blocks = iterator.next();
                        blocks.removeIf(block -> !validateBlock(block.getBlock(), isRightTime));
                    }
                }

                for (Iterator<Map<Chunk, Set<BlockState>>> iterator = worlds.values().iterator(); iterator.hasNext(); ) {
                    Map<Chunk, Set<BlockState>> chunks = iterator.next();
                    chunks.values().removeIf(blocks -> blocks.size() == 0);
                }

                worlds.values().removeIf(chunks -> chunks.size() == 0);
            }
        }.runTaskTimer(plugin, 1L, config.getLong(GimmeHardcore.CONFIG_ROSE_BUSH_HIDDEN_CYCLE_UPDATE_FREQUENCY));
    }

    /**
     * Re-place all hidden blocks on server shutdown.
     */
    public void onDisable() {
        for (Map<Chunk, Set<BlockState>> chunks : worlds.values()) {
            for (Chunk chunk : chunks.keySet()) {
                hideChunk(chunks, chunk, false);
            }
        }
    }

    /**
     * Add unadded blocks. Update old chunks when re-loaded.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    private void onChunkLoad(ChunkLoadEvent event) {
        Chunk chunk = event.getChunk();
        if (!chunk.getWorld().getEnvironment().equals(World.Environment.NORMAL)) return;

        boolean isRightTime = isRightTime(chunk.getWorld());

        if (isNewChunk(chunk)) {
            for (int y = y_min; y < y_max; y++) {
                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        Block block = chunk.getBlock(x, y, z);
                        if (!block.getType().equals(Material.ROSE_BUSH)
                                || block.getRelative(BlockFace.DOWN).getType().equals(Material.ROSE_BUSH)) continue;

                        addBlock(block, isRightTime);
                    }
                }
            }
            checkedChunks.add(chunk);
        } else {
            World world = chunk.getWorld();
            Map<Chunk, Set<BlockState>> chunks = worlds.get(world);
            if (chunks == null) return;
            hideChunk(chunks, chunk, isRightTime(world));
        }
    }

    /**
     * Is the first time this chunk is loaded after server start.
     */
    private boolean isNewChunk(@NotNull Chunk chunk) {
        return !checkedChunks.contains(chunk);
    }

    /**
     * Check if the surroundings support the stored block state.
     */
    private boolean validateBlock(@NotNull Block block, boolean isRightTime) {
        if (isRightTime) {
            if (!block.getType().equals(HIDDEN_MATERIAL)) return false;
            if (block.getRelative(BlockFace.DOWN).getType().equals(Material.AIR)) return false;
            if (!block.getRelative(BlockFace.UP).getType().equals(Material.AIR)) return false;
        } else {
            if (!block.getType().equals(Material.ROSE_BUSH)) return false;
        }

        return true;
    }

    /**
     * Hide blocks in chunk if the right time.
     */
    private void hideChunk(@NotNull Map<Chunk, Set<BlockState>> chunks, @NotNull Chunk chunk, boolean isRightTime) {
        boolean hidden = activeHidingChunks.contains(chunk);
        if (isRightTime == hidden) return;

        Set<BlockState> blocks = chunks.get(chunk);
        if (blocks == null) return;

        if (isRightTime) activeHidingChunks.add(chunk);
        else activeHidingChunks.remove(chunk);

        for (BlockState block : blocks) {
            if (!block.getType().equals(Material.ROSE_BUSH) && !block.getType().equals(HIDDEN_MATERIAL)) continue;
            hide(block, isRightTime);
        }
    }

    /**
     * Add block state to be stored.
     */
    private void addBlock(@NotNull Block block, boolean isRightTime) {
        Map<Chunk, Set<BlockState>> chunks = worlds.computeIfAbsent(block.getWorld(), k -> new HashMap<>());
        chunks.computeIfAbsent(block.getChunk(), k -> new HashSet<>()).add(block.getState());
        if (isRightTime) hide(block.getState(), true);
    }

    /**
     * Is the right time to hide the blocks.
     */
    private boolean isRightTime(@NotNull World world) {
        long time = world.getTime();

        if (startTime <= endTime) return startTime <= time && time < endTime;
        else return startTime <= time || time < endTime;
    }

    /**
     * Hide/unhide block.
     */
    private void hide(BlockState block, boolean hidden) {
        if (hidden) {
            if (!block.getBlock().getType().equals(Material.ROSE_BUSH)) return;
            if (block.getBlock().getRelative(BlockFace.DOWN).getType().equals(Material.ROSE_BUSH)) return;

            Block up = block.getBlock().getRelative(BlockFace.UP);
            if (up.getType().equals(Material.ROSE_BUSH)) {
                BlockState upState = up.getState();
                upState.setType(Material.AIR);
                upState.update(true, false);
            }
            block.getBlock().setType(HIDDEN_MATERIAL);
        }
        else {
            block.update(true, false);

            Block up = block.getBlock().getRelative(BlockFace.UP);
            if (up.getType().equals(Material.AIR)) {
                BlockState upState = up.getState();
                upState.setType(Material.ROSE_BUSH);
                upState.update(true, false);
            }
        }
    }
}
