package me.gimme.gimmehardcore.brewing;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BrewingStand;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BrewingRecipe {
    public Material ingredientType;
    public int ingredientAmount;
    public PotionType potionType;
    public ItemStack result;

    public BrewingRecipe(Material ingredientType, PotionType potionType, ItemStack result) {
        this(ingredientType, 1, potionType, result);
    }

    public BrewingRecipe(Material ingredientType, int ingredientAmount, PotionType potionType, ItemStack result) {
        this.ingredientType = ingredientType;
        this.ingredientAmount = ingredientAmount;
        this.potionType = potionType;
        this.result = result;
    }

    public boolean isSatisfied(@NotNull BrewerInventory inventory) {
        ItemStack ingredient = inventory.getIngredient();

        if (ingredient == null) return false;
        if (!ingredient.getType().equals(ingredientType)) return false;
        if (ingredient.getAmount() < ingredientAmount) return false;

        for (int i = 0; i <= 2; i++) {
            if (matchPotionType(inventory.getItem(i))) return true;
        }
        return false;
    }

    public void brew(@NotNull BrewerInventory inventory) {
        BrewingStand brewer = inventory.getHolder();
        if (brewer == null) return;

        BrewEvent event = new BrewEvent(brewer.getBlock(), inventory, brewer.getFuelLevel());
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;

        consumeIngredient(inventory);

        for (int i = 0; i <= 2; i++) {
            if (!matchPotionType(inventory.getItem(i))) continue;

            inventory.setItem(i, result.clone());
        }
    }

    private boolean consumeIngredient(@NotNull BrewerInventory inventory) {
        ItemStack ingredient = inventory.getIngredient();
        if (ingredient == null) return false;
        if (!ingredient.getType().equals(ingredientType)) return false;
        if (ingredient.getAmount() < ingredientAmount) return false;

        ingredient.setAmount(ingredient.getAmount() - ingredientAmount);
        return true;
    }

    private boolean matchPotionType(@Nullable ItemStack itemStack) {
        if (itemStack == null) return false;

        ItemMeta itemMeta = itemStack.getItemMeta();
        if (!(itemMeta instanceof PotionMeta)) return false;

        PotionMeta potionMeta = (PotionMeta) itemMeta;
        return potionMeta.getBasePotionData().getType().equals(potionType);
    }
}
