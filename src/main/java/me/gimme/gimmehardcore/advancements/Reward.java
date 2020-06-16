package me.gimme.gimmehardcore.advancements;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface Reward {
    void onReward(@NotNull Player player);
}
