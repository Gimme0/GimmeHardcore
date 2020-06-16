package me.gimme.gimmehardcore.advancements.completers;

import me.gimme.gimmehardcore.advancements.Completer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;

public class AutoCompleter extends Completer {
    @EventHandler(priority = EventPriority.MONITOR)
    private void onPlayerJoin(PlayerJoinEvent event) {
        grant(event.getPlayer());
    }
}
