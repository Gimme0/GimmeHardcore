package me.gimme.gimmehardcore.advancements.rewards;

import me.gimme.gimmehardcore.advancements.Reward;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class ItemReward implements Reward {
    private List<ItemStack> items;

    public ItemReward(ItemStack... items) {
        this.items = Arrays.asList(items);
    }

    @Override
    public void onReward(@NotNull Player player) {
        for (ItemStack item : items) {
            player.getWorld().dropItemNaturally(player.getLocation(), item);
        }
    }
}
