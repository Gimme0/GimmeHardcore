package me.gimme.gimmehardcore.brewing;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BrewingStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class CustomBrewing implements Listener {
    private Plugin plugin;

    private Map<Material, List<BrewingRecipe>> registeredBrewingRecipes = new HashMap<>();
    private Map<Block, BrewingClock> brewers = new HashMap<>();

    public CustomBrewing(@NotNull Plugin plugin) {
        this.plugin = plugin;

        registerCustomBrewingRecipe(new BrewingRecipe(
                Material.ROSE_BUSH,
                5,
                PotionType.AWKWARD,
                getResultingPotion(PotionType.INSTANT_HEAL)));
    }

    private void registerCustomBrewingRecipe(@NotNull BrewingRecipe recipe) {
        List<BrewingRecipe> recipes = registeredBrewingRecipes.computeIfAbsent(recipe.ingredientType, k -> new ArrayList<>());
        recipes.add(recipe);
    }

    private ItemStack getResultingPotion(@NotNull PotionType potionType) {
        ItemStack potion = new ItemStack(Material.POTION, 1);
        PotionMeta meta = (PotionMeta) potion.getItemMeta();
        assert meta != null;
        meta.setBasePotionData(new PotionData(potionType));
        potion.setItemMeta(meta);
        return potion;
    }

    private void startBrewing(@NotNull BrewingStand brewer, @NotNull BrewerInventory inventory, @NotNull BrewingRecipe recipe) {
        if (brewer.getFuelLevel() == 0) return;
        brewer.setFuelLevel(brewer.getFuelLevel() - 1);

        BrewingClock clock = new BrewingClock(plugin, recipe, inventory, () -> brewers.remove(brewer.getBlock()));
        brewers.put(brewer.getBlock(), clock);
        clock.start();
    }

    private void cancelBrewing(@NotNull Block brewer) {
        BrewingClock clock = brewers.get(brewer);
        if (clock == null) return;
        clock.cancel();
    }

    private boolean isBrewing(@NotNull Block brewer) {
        return brewers.containsKey(brewer);
    }

    private BrewingRecipe getRecipe(@NotNull BrewerInventory inventory) {
        ItemStack ingredient = inventory.getIngredient();
        if (ingredient == null) return null;

        List<BrewingRecipe> recipes = registeredBrewingRecipes.get(ingredient.getType());
        if (recipes == null) return null;

        for (BrewingRecipe recipe : recipes) {
            if (recipe.isSatisfied(inventory)) return recipe;
        }
        return null;
    }

    /**
     * Allows custom ingredients to be added in the ingredient slot of brewing stands.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    private void onPotionFuelInventoryClick(final InventoryClickEvent event) {
        if (event.isCancelled()) return;
        if (event.getClickedInventory() == null) return;
        if (event.getClickedInventory().getType() != InventoryType.BREWING) return;
        if (!event.getSlotType().equals(InventoryType.SlotType.FUEL)) return; // Is the ingredient slot (not the blaze powder slot)
        if (event.isShiftClick()) return;

        final ItemStack currentItem = event.getCurrentItem(); // Current item in the slot
        final ItemStack newItem = event.getCursor(); // Current item in the cursor

        if (newItem == null) return; // There is something in the cursor
        if (!registeredBrewingRecipes.containsKey(newItem.getType())) return; // Allow custom ingredients

        Player player = (Player)event.getWhoClicked();
        BrewerInventory inventory = (BrewerInventory) event.getClickedInventory();

        new BukkitRunnable() {
            @Override
            public void run() {
                player.setItemOnCursor(currentItem); // Make the switch
                event.setCurrentItem(newItem);
                onBrewingInventoryUpdate(inventory);
            }
        }.runTaskLater(plugin, 1L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private void onInventoryClick(final InventoryClickEvent event) {
        if (event.isCancelled()) return;
        if (event.getAction().equals(InventoryAction.NOTHING)) return;

        Inventory inventory = event.getClickedInventory();
        if (inventory == null) return;
        if (inventory.getType() != InventoryType.BREWING) return;

        onBrewingInventoryUpdate((BrewerInventory) inventory);
    }

    private void onBrewingInventoryUpdate(@NotNull BrewerInventory inventory) {
        BrewingStand brewingStand = inventory.getHolder();
        if (brewingStand == null) return;

        Block brewer = brewingStand.getBlock();
        BrewingRecipe recipe = getRecipe(inventory);

        if (isBrewing(brewer)) {
            if (recipe == null) cancelBrewing(brewer);
            return;
        }
        if (recipe == null) return;

        startBrewing(brewingStand, inventory, recipe);
    }
}
