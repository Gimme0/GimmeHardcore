package me.gimme.gimmehardcore.respawn.death;

import me.gimme.gimmehardcore.GimmeHardcore;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AwaitAction {

    private Plugin plugin;
    private Map<UUID, GameMode> awaitingActionPlayers = new HashMap<>();

    public AwaitAction(Plugin plugin) {
        this.plugin = plugin;
    }

    public void awaitAction(Player player) {
        awaitingActionPlayers.put(player.getUniqueId(), player.getGameMode());

        player.setGameMode(GameMode.SPECTATOR);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!isAwaitingAction(player)) {
                    this.cancel();
                    return;
                }

                showRespawnedMessage(player);
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    public void stopAwaitAction(Player player) {
        GameMode gameMode = awaitingActionPlayers.remove(player.getUniqueId());

        player.setGameMode(gameMode);
        clearRespawnMessages(player);
    }

    public boolean isAwaitingAction(Player player) {
        return awaitingActionPlayers.containsKey(player.getUniqueId());
    }

    public void clearRespawnMessages(Player player) {
        if (plugin.getConfig().getBoolean(GimmeHardcore.CONFIG_RESPAWN_TIMER_IN_PLAYER_LIST)) {
            player.setPlayerListName(player.getName());
        }
        player.resetTitle();
    }

    private void showRespawnedMessage(Player player) {
        String title = ChatColor.GREEN + plugin.getConfig().getString(GimmeHardcore.CONFIG_RESPAWN_FINISHED_TITLE);
        String subtitle = plugin.getConfig().getString(GimmeHardcore.CONFIG_RESPAWN_FINISHED_SUBTITLE);
        player.sendTitle(title, subtitle, 0, 25, 10);
    }
}
