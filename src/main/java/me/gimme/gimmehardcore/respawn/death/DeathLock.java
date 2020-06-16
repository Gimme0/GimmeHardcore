package me.gimme.gimmehardcore.respawn.death;

import me.gimme.gimmehardcore.GimmeHardcore;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class DeathLock {

    public class DeathData {
        public int countdown;
        public Location deathLocation;
        private Consumer<Player> onRespawn;

        private DeathData(int respawnDelay, Location deathLocation, Consumer<Player> onRespawn) {
            this.countdown = respawnDelay;
            this.deathLocation = deathLocation;
            this.onRespawn = onRespawn;
        }
    }

    private Plugin plugin;
    private Map<UUID, DeathData> deadPlayers = new HashMap<>();

    public DeathLock(Plugin plugin) {
        this.plugin = plugin;
    }

    public void startRespawnTimer(Player player, Consumer<Player> onRespawn) {
        DeathData deathData = new DeathData(plugin.getConfig().getInt(GimmeHardcore.CONFIG_RESPAWN_DELAY), player.getLocation(), onRespawn);
        deadPlayers.put(player.getUniqueId(), deathData);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (deathData.countdown <= 0) {
                    respawn(player);
                }

                if (!isDead(player)) {
                    this.cancel();
                    return;
                }

                deathData.countdown--;
                updatePlayerListRespawnProgress(player, deathData.countdown);
            }
        }.runTaskTimer(plugin, 0, 20);
    }

    public boolean respawn(Player player) {
        DeathData deathData = deadPlayers.remove(player.getUniqueId());
        if (deathData == null) return false;

        deathData.onRespawn.accept(player);
        return true;
    }

    public boolean isDead(Player player) {
        return deadPlayers.containsKey(player.getUniqueId());
    }

    public DeathData getDeathData(Player player) {
        return deadPlayers.get(player.getUniqueId());
    }

    private void updatePlayerListRespawnProgress(Player player, int counter) {
        if (!player.isOnline()) return;
        if (!plugin.getConfig().getBoolean(GimmeHardcore.CONFIG_RESPAWN_TIMER_IN_PLAYER_LIST)) return;

        Duration duration = Duration.ofSeconds(counter);
        int hours = (int) duration.toHours();
        int minutes = (int) duration.toMinutes() % 60;
        int seconds = (int) duration.getSeconds() % 60;
        String formattedTime =
                (hours > 0)   ? String.format("%d:%02d:%02d", hours, minutes, seconds) :
                        (minutes > 0) ? String.format("%d:%02d", minutes, seconds) :
                                String.format("%d", seconds);

        player.setPlayerListName(ChatColor.DARK_RED + player.getName() + ChatColor.GRAY + " | " + formattedTime);
    }

}
