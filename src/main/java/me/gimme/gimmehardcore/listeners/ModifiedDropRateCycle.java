package me.gimme.gimmehardcore.listeners;

import me.gimme.gimmehardcore.GimmeHardcore;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class ModifiedDropRateCycle implements Listener {

    private FileConfiguration config;
    private Random random = new Random();

    public ModifiedDropRateCycle(@NotNull FileConfiguration config) {
        this.config = config;
    }

    @EventHandler(priority = EventPriority.LOW)
    private void onBlockBreak(BlockBreakEvent event) {
        if (!isRightTime(event.getPlayer().getWorld())) return;

        String blockNamespacedKey = event.getBlock().getType().getKey().getKey();

        ConfigurationSection cs = config.getConfigurationSection(GimmeHardcore.CONFIG_MODIFIED_DROP_RATE_CYCLE_BLOCKS);
        if (cs == null) return;
        if (!cs.contains(blockNamespacedKey)) return;

        double dropRate = cs.getDouble(blockNamespacedKey);

        double roll = random.nextDouble();
        if (roll <= dropRate) return;

        event.setDropItems(false);
    }

    private boolean isRightTime(@NotNull World world) {
        long time = world.getTime();
        int startTime = config.getInt(GimmeHardcore.CONFIG_MODIFIED_DROP_RATE_CYCLE_START);
        int endTime = config.getInt(GimmeHardcore.CONFIG_MODIFIED_DROP_RATE_CYCLE_END);

        if (startTime <= endTime) return startTime <= time && time < endTime;
        else return startTime <= time || time < endTime;
    }

}
