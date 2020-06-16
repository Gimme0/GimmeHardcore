package me.gimme.gimmehardcore.advancements.rewards;

import me.gimme.gimmehardcore.advancements.Reward;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class XPReward implements Reward {
    private int amount;

    public XPReward(int amount) {
        this.amount = amount;
    }

    @Override
    public void onReward(@NotNull Player player) {
        player.giveExp(amount);
    }
}
