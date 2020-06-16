package me.gimme.gimmehardcore.advancements.completers;

import me.gimme.gimmehardcore.advancements.Completer;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.jetbrains.annotations.Nullable;

public class BlockBreakCompleter extends Completer {
    private Material blockType;
    private Material toolType;

    public BlockBreakCompleter(@Nullable Material blockType, @Nullable Material toolType) {
        this.blockType = blockType;
        this.toolType = toolType;
    }

    @EventHandler
    private void onBlockBreak(BlockBreakEvent event) {
        if (blockType != null && !event.getBlock().getType().equals(blockType)) return;
        if (toolType != null && !event.getPlayer().getInventory().getItemInMainHand().getType().equals(toolType)) return;

        grant(event.getPlayer());
    }
}
