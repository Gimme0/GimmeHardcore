package me.gimme.gimmehardcore.listeners;

import me.gimme.gimmehardcore.GimmeHardcore;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Iterator;

public class NightSkipEventListener extends AbstractEventListener {

    private static final int MAX_SLEEP_TICKS = 98;
    private static final String NIGHT_SKIP_PREVENTED_MESSAGE = "Skipping the night is not allowed";

    @EventHandler
    public void onPlayerBedEnter(PlayerBedEnterEvent event) {
        if (!event.getBedEnterResult().equals(PlayerBedEnterEvent.BedEnterResult.OK)) return;

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getUniqueId().equals(event.getPlayer().getUniqueId())) continue;
            if (!player.isSleeping()) return;
        }

        //event.setCancelled(true);
        new StopNightSkipTask(event.getPlayer()).runTaskTimer(plugin, 0, 1);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        wakeUpAPlayerIfEveryoneIsSleeping();
    }

    private void wakeUpAPlayerIfEveryoneIsSleeping() {
        Iterator<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers().iterator();

        while (onlinePlayers.hasNext()) {
            Player player = onlinePlayers.next();
            if (!player.isSleeping()) return;

            if (!onlinePlayers.hasNext()) {
                forceWakeup(player);
            }
        }
    }

    private void forceWakeup(Player player) {
        player.wakeup(true);

        if (plugin.getConfig().getBoolean(GimmeHardcore.CONFIG_BROADCAST_NIGHT_SKIP_PREVENTED)) {
            Bukkit.broadcastMessage(NIGHT_SKIP_PREVENTED_MESSAGE);
        }
    }

    private class StopNightSkipTask extends BukkitRunnable {

        private Player player;

        private StopNightSkipTask(Player player) {
            this.player = player;
        }

        @Override
        public void run(){
            if (player == null || !player.isSleeping() || !player.isOnline()) {
                cancel();
                return;
            }

            if (player.getSleepTicks() >= MAX_SLEEP_TICKS) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (!player.isSleeping()) return;
                }
                forceWakeup(player);
            }
        }

    }

}
