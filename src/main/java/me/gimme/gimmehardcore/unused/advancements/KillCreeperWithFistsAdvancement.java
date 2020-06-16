package me.gimme.gimmehardcore.unused.advancements;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class KillCreeperWithFistsAdvancement extends AbstractAdvancement {

    private static final int CLEAN_UP_DAMAGE_MAP_INTERVAL = 5; // Interval in minutes
    private static final int TICKS_PER_MINUTE = 1200;

    private Map<UUID, Double> damageToCreeperIdMap = new HashMap<>();

    public KillCreeperWithFistsAdvancement() {
        super(Hardcore.KILL_CREEPER_WITH_FISTS);
        // Clean up map every 10 min in case it mounts up
        new CleanUpMapTask().runTaskTimer(plugin,
                CLEAN_UP_DAMAGE_MAP_INTERVAL * TICKS_PER_MINUTE, CLEAN_UP_DAMAGE_MAP_INTERVAL * TICKS_PER_MINUTE);
    }

    @EventHandler
    public void onEntityDamaged(EntityDamageByEntityEvent event) {
        Entity entity = event.getEntity();
        if (!event.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_ATTACK)) return;
        if (!entity.getType().equals(EntityType.CREEPER)) return;
        if (!event.getDamager().getType().equals(EntityType.PLAYER)) return;
        Player player = (Player) event.getDamager();

        onPlayerAttackedCreeper(player, entity.getUniqueId(), event.getFinalDamage());
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity mob = event.getEntity();
        Player player = mob.getKiller();
        if (!mob.getType().equals(EntityType.CREEPER)) return;
        if (player == null) return;

        onPlayerKilledCreeper(player, mob.getUniqueId());
    }

    private void onPlayerAttackedCreeper(Player player, UUID creeperId, double damage) {
        if (!player.getInventory().getItemInMainHand().getType().equals(Material.AIR)) return;
        // If fists were used
        updateDamageToCreeper(creeperId, damage);
    }

    private void onPlayerKilledCreeper(Player player, UUID creeperId) {
        Double damageTakenByFists = damageToCreeperIdMap.get(creeperId);
        if (damageTakenByFists != null && damageTakenByFists > 19.9d) {
            grantAdvancement(player);
        }
        damageToCreeperIdMap.remove(creeperId);
    }

    private void updateDamageToCreeper(UUID creeperId, double damage) {
        double totalDamage = damageToCreeperIdMap.getOrDefault(creeperId, 0d);
        damageToCreeperIdMap.put(creeperId, totalDamage + damage);
    }

    private class CleanUpMapTask extends BukkitRunnable {
        @Override
        public void run(){
            for (UUID creeperId : damageToCreeperIdMap.keySet()) {
                if (plugin.getServer().getEntity(creeperId) == null) {
                    damageToCreeperIdMap.remove(creeperId);
                }
            }
        }
    }

}
