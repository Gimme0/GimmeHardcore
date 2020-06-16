package me.gimme.gimmehardcore.respawn.death;

import me.gimme.gimmehardcore.GimmeHardcore;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DeathSpectating {

    public class RespawningData {
        private Location respawnLocation;
        private GameMode gameMode;
        private Entity vehicle;

        private RespawningData(Location respawnLocation, GameMode gameMode, Entity vehicle) {
            this.respawnLocation = respawnLocation;
            this.gameMode = gameMode;
            this.vehicle = vehicle;
        }
    }

    private Plugin plugin;
    private Map<UUID, RespawningData> deathSpectatingPlayers = new HashMap<>();

    public DeathSpectating(Plugin plugin) {
        this.plugin = plugin;
    }

    public void deathSpectate(Player player, Location spectateLocation, Location respawnLocation, DeathLock.DeathData deathData) {
        AreaEffectCloud cloud = (AreaEffectCloud) spectateLocation.getWorld()
                .spawnEntity(spectateLocation, EntityType.AREA_EFFECT_CLOUD);
        cloud.setRadius(0.1f);
        cloud.setDuration(deathData.countdown * 20);

        RespawningData respawningData = new RespawningData(respawnLocation, player.getGameMode(), cloud);
        deathSpectatingPlayers.put(player.getUniqueId(), respawningData);

        player.setGameMode(GameMode.SPECTATOR);

        new BukkitRunnable() {
            @Override
            public void run() {
                cloud.addPassenger(player);
            }
        }.runTaskLater(plugin, 2);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!isDeathSpectating(player)) {
                    this.cancel();
                    return;
                }

                updateRespawnProgressMessage(player, deathData.countdown);
            }
        }.runTaskTimer(plugin, 0, 20);
    }

    public void unDeathSpectate(Player player) {
        RespawningData respawningData = deathSpectatingPlayers.remove(player.getUniqueId());

        respawningData.vehicle.remove();
        player.setGameMode(respawningData.gameMode);
        player.teleport(respawningData.respawnLocation);
    }

    public boolean isDeathSpectating(Player player) {
        return deathSpectatingPlayers.containsKey(player.getUniqueId());
    }

    public Location getSpectateLocation(Player player) {
        return deathSpectatingPlayers.get(player.getUniqueId()).vehicle.getLocation();
    }

    private void updateRespawnProgressMessage(Player player, int counter) {
        Duration duration = Duration.ofSeconds(counter);
        int hours = (int) duration.toHours();
        int minutes = (int) duration.toMinutes() % 60;
        int seconds = (int) duration.getSeconds() % 60;
        String formattedTime =
                (hours > 0)   ? String.format("%dh %02dm %02ds", hours, minutes, seconds) :
                        (minutes > 0) ? String.format("%dm %02ds", minutes, seconds) :
                                String.format("%ds", seconds);

        String title = ChatColor.DARK_RED + plugin.getConfig().getString(GimmeHardcore.CONFIG_RESPAWN_SCREEN_TITLE).replaceAll(
                "%time%", formattedTime);
        String subtitle = plugin.getConfig().getString(GimmeHardcore.CONFIG_RESPAWN_SCREEN_SUBTITLE).replaceAll(
                "%time%", formattedTime);

        int fadeIn = 0;
        int stay =      (counter == 0) ? 0 : 25;
        int fadeOut =   (counter == 0) ? 20 : 10;

        player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
    }
}
