package me.gimme.gimmehardcore.crafting;

import me.gimme.gimmehardcore.GimmeHardcore;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.*;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class RecipeModification {
    private Plugin plugin;
    private Server server;
    private FileConfiguration config;

    public RecipeModification(@NotNull Plugin plugin, @NotNull Server server, @NotNull FileConfiguration config) {
        this.plugin = plugin;
        this.server = server;
        this.config = config;
    }

    public void modifyRecipes() {
        if (!config.getBoolean(GimmeHardcore.CONFIG_ROSE_BUSH_ENABLED)) return;
        if (!config.getBoolean(GimmeHardcore.CONFIG_ROSE_BUSH_GLISTERING_MELON_RECIPE)) return;

        disableGlisteringMelon();
        addCustomGlisteringMelon();
    }

    private void disableGlisteringMelon() {
        server.removeRecipe(NamespacedKey.minecraft("glistering_melon_slice"));
    }

    private void addCustomGlisteringMelon() {
        ShapedRecipe r = createShapedRecipe("glistering_melon_slice", Material.GLISTERING_MELON_SLICE, 1);
        r.shape("RRR", "RMR", "RRR")
                .setIngredient('M', Material.MELON_SLICE)
                .setIngredient('R', Material.ROSE_BUSH);
        addRecipe(r);
    }

    private ShapedRecipe createShapedRecipe(String namespacedKey, Material result, int amount) {
        return new ShapedRecipe(new NamespacedKey(plugin, namespacedKey), new ItemStack(result, amount));
    }

    private void addRecipe(Recipe recipe) {
        server.addRecipe(recipe);
    }
}
