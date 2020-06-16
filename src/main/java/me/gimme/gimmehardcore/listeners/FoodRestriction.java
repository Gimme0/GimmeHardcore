package me.gimme.gimmehardcore.listeners;

import me.gimme.gimmehardcore.GimmeHardcore;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;

import java.util.HashMap;
import java.util.Map;

public class FoodRestriction implements Listener {

    private double defaultModifier;
    private Map<String, Double> foodModifiers = new HashMap<>();

    public FoodRestriction(FileConfiguration config) {
        defaultModifier = config.getDouble(GimmeHardcore.CONFIG_FOOD_DEFAULT_MODIFIER);

        ConfigurationSection foodListSection = config.getConfigurationSection(GimmeHardcore.CONFIG_FOOD_SPECIFIC_MODIFIERS);
        if (foodListSection != null) {
            for (String key : foodListSection.getKeys(false)) {
                foodModifiers.put(key, foodListSection.getDouble(key));
            }
        }
    }

    /**
     * Prevents certain food from being consumed.
     */
    @EventHandler(priority = EventPriority.LOW)
    private void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        if (event.isCancelled()) return;

        Material type = event.getItem().getType();
        if (!type.isEdible()) return;

        Double modifier = foodModifiers.getOrDefault(type.getKey().getKey(), defaultModifier);
        if (modifier >= 0) return;

        event.setCancelled(true);
    }

    /**
     * Modifies the restoration amount of certain food.
     */
    @EventHandler(priority = EventPriority.LOW)
    private void onRestoreHunger(FoodLevelChangeEvent event) {
        if (event.isCancelled()) return;
        if (event.getItem() == null) return;

        Double modifier = foodModifiers.getOrDefault(event.getItem().getType().getKey().getKey(), defaultModifier);
        if (modifier < 0) return;

        int oldFoodLevel = ((Player) event.getEntity()).getFoodLevel();
        int newFoodLevel = event.getFoodLevel();
        int diff = newFoodLevel - oldFoodLevel;
        int modifiedAmount = (int) Math.round(diff * modifier);

        event.setFoodLevel(oldFoodLevel + modifiedAmount);
    }

}
