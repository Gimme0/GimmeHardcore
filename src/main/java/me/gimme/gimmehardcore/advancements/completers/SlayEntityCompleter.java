package me.gimme.gimmehardcore.advancements.completers;

import me.gimme.gimmehardcore.advancements.Completer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDeathEvent;

public class SlayEntityCompleter extends Completer {

    private EntityType entityType;
    private double maxDistanceSquared = -1;

    public SlayEntityCompleter(EntityType entityType) {
        this.entityType = entityType;
    }

    public SlayEntityCompleter(EntityType entityType, double maxDistance) {
        this.entityType = entityType;
        this.maxDistanceSquared = maxDistance * maxDistance;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private void onSlayEntity(EntityDeathEvent event) {
        if (!event.getEntityType().equals(entityType)) return;

        for (Player player : event.getEntity().getWorld().getPlayers()) {
            if (maxDistanceSquared >= 0 && player.getLocation().distanceSquared(event.getEntity().getLocation()) > maxDistanceSquared) continue;

            grant(player);
        }
    }

}
