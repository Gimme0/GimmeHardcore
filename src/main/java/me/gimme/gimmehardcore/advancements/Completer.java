package me.gimme.gimmehardcore.advancements;

import me.gimme.gimmehardcore.advancements.crazyadvancements.Advancement;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

public class Completer implements Listener {
    private AdvancementManager advancementManager;
    protected Advancement advancement;

    protected void grant(@NotNull Player player) {
        advancementManager.grantAdvancement(player, advancement);
    }

    public void setManager(@NotNull AdvancementManager advancementManager) {
        this.advancementManager = advancementManager;
    }

    public void setAdvancement(@NotNull Advancement advancement) {
        this.advancement = advancement;
    }
}
