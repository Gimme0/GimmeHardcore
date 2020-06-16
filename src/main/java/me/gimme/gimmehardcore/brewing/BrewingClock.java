package me.gimme.gimmehardcore.brewing;

import org.bukkit.block.BrewingStand;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

public class BrewingClock extends BukkitRunnable {
    public interface OnStopListener {
        void onStop();
    }

    private Plugin plugin;
    private BrewerInventory inventory;
    private BrewingRecipe recipe;
    private BrewingStand stand;
    private OnStopListener onStopListener;

    private int time = 400;

    public BrewingClock(@NotNull Plugin plugin, @NotNull BrewingRecipe recipe , @NotNull BrewerInventory inventory,
                        @NotNull OnStopListener onStopListener) {
        this.plugin = plugin;
        this.recipe = recipe;
        this.inventory = inventory;
        this.stand = inventory.getHolder();
        this.onStopListener = onStopListener;
    }

    public void start() {
        runTaskTimer(plugin, 0L, 1L);
    }

    @Override
    public void run() {
        if (!stand.isPlaced()) {
            cancel();
            return;
        }

        if(time <= 0) {
            recipe.brew(inventory);
            cancel();
            return;
        }

        time--;
        stand.setBrewingTime(time);
    }

    @Override
    public synchronized void cancel() throws IllegalStateException {
        super.cancel();
        onStopListener.onStop();
    }
}
