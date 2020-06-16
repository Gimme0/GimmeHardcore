package me.gimme.gimmehardcore.listeners;

import me.gimme.gimmehardcore.GimmeHardcore;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.jetbrains.annotations.NotNull;

public class UnbreakableBlocks implements Listener {

    private FileConfiguration config;

    public UnbreakableBlocks(@NotNull FileConfiguration config) {
        this.config = config;
    }

    @EventHandler(priority = EventPriority.LOW)
    private void onBlockBreak(BlockBreakEvent event) {
        if (event.getPlayer().getGameMode().equals(GameMode.CREATIVE)) return;

        String blockNamespacedKey = event.getBlock().getType().getKey().getKey();
        ConfigurationSection unbreakableBlocks = config.getConfigurationSection(GimmeHardcore.CONFIG_UNBREAKABLE_BLOCKS);
        if (unbreakableBlocks == null) return;

        ConfigurationSection toolExceptions = unbreakableBlocks.getConfigurationSection(blockNamespacedKey);
        if (toolExceptions == null) return;

        String usedTool = event.getPlayer().getInventory().getItemInMainHand().getType().getKey().getKey();
        if (toolExceptions.contains(usedTool)) {
            int y = toolExceptions.getInt(usedTool);
            if (event.getBlock().getY() >= y) return;
        } else if (toolExceptions.contains("default")) {
            int y = toolExceptions.getInt("default");
            if (event.getBlock().getY() >= y) return;
        }

        event.setCancelled(true);
    }

}
