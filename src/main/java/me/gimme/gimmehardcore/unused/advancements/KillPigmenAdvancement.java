package me.gimme.gimmehardcore.unused.advancements;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

public class KillPigmenAdvancement extends AbstractAdvancement {

    // Settings
    private static final boolean ADVANCEMENT_MESSAGES = true;
    // --- Changing kills/time/range requires updating the description in advancements.json
    private static final int KILLS = 50; // Required amount of kills
    private static final int TIME = 5; // Time to complete after entering the nether in minutes
    private static final int RANGE = 50; // Shared kills range (in blocks around the death event)
    private static final boolean SEND_KILL_MESSAGES = true; // If this advancement should send messages
    // Message source will be displayed first in the messages sent from this advancement
    // References the advancement name
    private static final String MESSAGE_SOURCE = "[They're Coming] "; // Message prefix
    private static final int SEND_MESSAGE_EVERY_N_KILLS = 10; // How often players should get kill message updates

    private static final int TICKS_PER_MINUTE = 1200;

    private Map<Player, Integer> pigmenKilledMap = new HashMap<>();
    private Map<Player, Integer> playerTimerIdMap = new HashMap<>();

    public KillPigmenAdvancement() {
        super(Hardcore.KILL_PIGMEN);
    }

    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        World.Environment to = player.getWorld().getEnvironment();
        World.Environment from = event.getFrom().getEnvironment();
        if (hasAdvancement(player)) return;
        if (to.equals(World.Environment.NETHER)) {
            startEvent(player);
        } else if (from.equals(World.Environment.NETHER)) {
            endEvent(player);
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity mob = event.getEntity();
        Player player = mob.getKiller();
        if (!mob.getType().equals(EntityType.PIG_ZOMBIE)) return;
        if (player == null) return;

        onPigmanKillCredit(player);

        for (Entity entity : mob.getNearbyEntities(RANGE, RANGE, RANGE)) {
            if (entity instanceof Player && entity != player) {
                onPigmanKillCredit((Player) entity);
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (!player.getWorld().getEnvironment().equals(World.Environment.NETHER)) return;

        endEvent(player);
    }

    private void onPigmanKillCredit(Player player) {
        if (!playerTimerIdMap.containsKey(player)) return; // Event hasn't started for this player

        int pigmenKilled = 1 + pigmenKilledMap.getOrDefault(player, 0);
        pigmenKilledMap.put(player, pigmenKilled);
        if (pigmenKilled >= KILLS) {
            endEvent(player);
            return;
        }
        if (ADVANCEMENT_MESSAGES && SEND_KILL_MESSAGES && pigmenKilled % SEND_MESSAGE_EVERY_N_KILLS == 0) {
            player.sendMessage(MESSAGE_SOURCE + "Kills: " + pigmenKilled);
        }
    }

    private void startEvent(Player player) {
        playerTimerIdMap.put(player, new TimerTask(player).runTaskLater(plugin, TIME * TICKS_PER_MINUTE).getTaskId());
    }

    private void endEvent(Player player) {
        int pigmenKilled = pigmenKilledMap.getOrDefault(player, 0);
        if (pigmenKilled >= KILLS) {
            grantAdvancement(player);
        } else if (ADVANCEMENT_MESSAGES && SEND_KILL_MESSAGES && pigmenKilled >= SEND_MESSAGE_EVERY_N_KILLS) {
            player.sendMessage(MESSAGE_SOURCE + "Failed: " + pigmenKilled + "/" + KILLS + " kills");
        }
        pigmenKilledMap.remove(player);
        Integer timerId = playerTimerIdMap.remove(player);
        if (timerId == null) return;
        Bukkit.getScheduler().cancelTask(timerId);
    }

    private class TimerTask extends BukkitRunnable {

        Player player;

        TimerTask(Player player) {
            this.player = player;
        }

        @Override
        public void run(){
            endEvent(player);
        }

    }

}
