package me.gimme.gimmehardcore.advancements.completers;

import me.gimme.gimmehardcore.advancements.Completer;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.Nullable;

public class EnterEnvironmentCompleter extends Completer {

    private World.Environment environment;
    @Nullable
    private PlayerTeleportEvent.TeleportCause teleportCause;

    public EnterEnvironmentCompleter(World.Environment environment) {
        this.environment = environment;
        if (environment.equals(World.Environment.THE_END)) teleportCause = PlayerTeleportEvent.TeleportCause.END_PORTAL;
        if (environment.equals(World.Environment.NETHER)) teleportCause = PlayerTeleportEvent.TeleportCause.NETHER_PORTAL;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private void onEnterTheEnd(PlayerTeleportEvent event) {
        if (event.isCancelled()) return;
        if (teleportCause != null && !event.getCause().equals(teleportCause)) return;
        Location to = event.getTo();
        if (to == null) return;
        World toWorld = to.getWorld();
        if (toWorld == null) return;
        if (!toWorld.getEnvironment().equals(environment)) return;

        grant(event.getPlayer());
    }
}
