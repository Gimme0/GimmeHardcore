package me.gimme.gimmehardcore.listeners;

import me.gimme.gimmehardcore.GimmeHardcore;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Random;

public class DeathDropDisabler extends AbstractEventListener {

    private FileConfiguration config;

    private Random random = new Random();

    public DeathDropDisabler(@NotNull FileConfiguration config) {
        this.config = config;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerDeath(PlayerDeathEvent event) {
        double dropChance = config.getDouble(GimmeHardcore.CONFIG_ITEM_DROP_CHANCE_ON_DEATH);
        if (dropChance > 0.9999) return;
        if (dropChance == 0) {
            event.getDrops().clear();
            return;
        }

        List<ItemStack> drops = event.getDrops();

        drops.removeIf((drop) -> random.nextDouble() < dropChance);
    }

}
