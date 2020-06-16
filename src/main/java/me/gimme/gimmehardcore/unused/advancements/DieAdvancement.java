package me.gimme.gimmehardcore.unused.advancements;

import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;

public class DieAdvancement extends AbstractAdvancement {

    public DieAdvancement() {
        super(Hardcore.DIE);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        grantAdvancement(event.getEntity());
    }
}
