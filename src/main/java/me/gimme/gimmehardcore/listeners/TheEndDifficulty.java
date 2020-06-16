package me.gimme.gimmehardcore.listeners;

import me.gimme.gimmehardcore.GimmeHardcore;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.*;
import java.util.List;

public class TheEndDifficulty implements Listener {

    public static final String PILLAR_SPAWNED_TAG = "spawned_by_pillar";

    private Plugin plugin;
    private FileConfiguration config;

    private Random random = new Random();

    private final boolean pillarSpawningEnabled;
    private final int spawnInterval;
    private final int spawnPlayerRange;

    private Map<World, PlayerCountingTask> playerCountByWorld = new HashMap<>();
    private Map<World, PillarSpawningTask> pillarSpawnerByWorld = new HashMap<>();

    public TheEndDifficulty(@NotNull Plugin plugin, @NotNull FileConfiguration config) {
        this.plugin = plugin;
        this.config = config;

        pillarSpawningEnabled = config.getBoolean(GimmeHardcore.CONFIG_THE_END_PILLAR_ENABLED);
        spawnInterval = config.getInt(GimmeHardcore.CONFIG_THE_END_PILLAR_SPAWN_INTERVAL);
        spawnPlayerRange = config.getInt(GimmeHardcore.CONFIG_THE_END_PILLAR_ACTIVATION_RANGE);
    }

    @EventHandler(priority = EventPriority.LOW)
    private void onEnderCrystalExplosion(EntityExplodeEvent event) {
        if (!event.getEntityType().equals(EntityType.ENDER_CRYSTAL)) return;

        Location location = event.getLocation();
        World world = location.getWorld();
        if (world == null) return;


        ConfigurationSection entitiesToSpawn = config.getConfigurationSection(GimmeHardcore.CONFIG_THE_END_CRYSTAL_SPAWN_ON_DEATH);
        if (entitiesToSpawn == null) return;
        for (String entityToSpawn : entitiesToSpawn.getKeys(false)) {
            ConfigurationSection values = entitiesToSpawn.getConfigurationSection(entityToSpawn);
            if (values == null) continue;
            int base = values.getInt(GimmeHardcore.CONFIG_THE_END_SPAWN_BASE);
            int extra = values.getInt(GimmeHardcore.CONFIG_THE_END_SPAWN_EXTRA);
            double variance = values.getDouble(GimmeHardcore.CONFIG_THE_END_SPAWN_VARIANCE);

            int amount = base + extra * getCurrentPlayerCountInWorld(world);

            spawn(location, entityToSpawn, amount, variance);
        }
    }

    private void spawn(@NotNull Location location, @NotNull String entityTypeName, int baseAmount, double variance) {
        World world = location.getWorld();
        if (baseAmount <= 0) return;
        if (world == null) return;

        int min = (int) Math.round(baseAmount - baseAmount * variance);
        int max = (int) Math.round(baseAmount + baseAmount * variance);
        int amount = random.nextInt(max - min + 1) + min;

        EntityType entityType = EntityType.valueOf(entityTypeName);

        new BukkitRunnable() {
            @Override
            public void run() {
                for (int i = 0; i < amount; i++) {
                    Location loc = new Location(location.getWorld(),
                            location.getX() + (random.nextDouble() * 2 - 1),
                            location.getY(),
                            location.getZ() + (random.nextDouble() * 2 - 1));
                    world.spawnEntity(loc, entityType);
                }
            }
        }.runTaskLater(plugin, 2);
    }

    @EventHandler(priority = EventPriority.LOW)
    private void onEnterEnd(PlayerTeleportEvent event) {
        if (!event.getCause().equals(PlayerTeleportEvent.TeleportCause.END_PORTAL)) return;

        Location to = event.getTo();
        if (to == null) return;

        World world = to.getWorld();
        if (isNotTheEnd(world)) return;

        onAnyEnterTheEnd(world);
    }

    private boolean isNotTheEnd(World world) {
        return world == null || !world.getEnvironment().equals(World.Environment.THE_END);
    }

    @EventHandler(priority = EventPriority.LOW)
    private void onPlayerJoin(PlayerJoinEvent event) {
        World world = event.getPlayer().getWorld();
        if (isNotTheEnd(world)) return;
        onAnyEnterTheEnd(world);
    }

    @EventHandler(priority = EventPriority.LOW)
    private void onEntityDeath(EntityDeathEvent event) {
        World world = event.getEntity().getLocation().getWorld();

        PillarSpawningTask pillarSpawningTask = pillarSpawnerByWorld.get(world);
        if (pillarSpawningTask == null) return;
        if (!event.getEntity().hasMetadata(PILLAR_SPAWNED_TAG)) return;
        List<MetadataValue> values = event.getEntity().getMetadata(PILLAR_SPAWNED_TAG);

        pillarSpawningTask.onPillarSpawnedEntityDeath((PillarId) values.get(0).value());
    }

    private void onAnyEnterTheEnd(@NotNull World theEnd) {
        startMonitoringPlayerCount(theEnd);
        if (pillarSpawningEnabled) {
            startPillarSpawning(theEnd);
        }
    }

    private int getCurrentPlayerCountInWorld(@NotNull World world) {
        PlayerCountingTask playerCount = playerCountByWorld.get(world);
        if (playerCount == null) return 0;
        return playerCount.getCount();
    }

    private void startMonitoringPlayerCount(@NotNull World world) {
        PlayerCountingTask playerCount = new PlayerCountingTask(world);
        if (playerCountByWorld.putIfAbsent(world, playerCount) != null) return;

        playerCount.runTaskTimer(plugin, 0, 20);
    }

    private void startPillarSpawning(@NotNull World world) {
        PillarSpawningTask pillarSpawningTask = new PillarSpawningTask(world);
        if (pillarSpawnerByWorld.putIfAbsent(world, pillarSpawningTask) != null) return;

        pillarSpawningTask.runTaskTimer(plugin, 0, spawnInterval);
    }

    private Location getPositionNearPillar(@NotNull Location location) {
        World world = location.getWorld();
        assert world != null;

        Vector offset = new Vector(6, 0, 0);
        offset.rotateAroundY(random.nextDouble() * Math.PI * 2);

        Location newLoc = new Location(world, location.getX(), location.getY(), location.getZ());
        newLoc.add(offset);
        newLoc.setY(world.getHighestBlockYAt(newLoc));

        return newLoc;
    }

    private static boolean isWithinRange(Location loc1, Location loc2, double range) {
        return Math.pow((loc1.getBlockX() - loc2.getBlockX()), 2)
                + Math.pow((loc1.getBlockZ() - loc2.getBlockZ()), 2)
                < Math.pow(range, 2);
    }

    private class PlayerCountingTask extends BukkitRunnable {
        public int maxCount = 0;
        private Long timestampOfMax = 0L;

        private World world;
        private static final int TIMEOUT_SECONDS = 10;

        private PlayerCountingTask(@NotNull World world) {
            this.world = world;
        }

        @Override
        public void run() {
            int activeCount = world.getPlayers().size();

            if (activeCount >= maxCount) {
                maxCount = activeCount;
                timestampOfMax = System.currentTimeMillis();
            } else if (hasTimedOut()) {
                maxCount = Math.floorDiv(maxCount, 2);
                timestampOfMax = System.currentTimeMillis();
            }

            if (maxCount == 0 && hasTimedOut()) {
                cancel();
                playerCountByWorld.remove(world);
            }
        }

        private boolean hasTimedOut() {
            return System.currentTimeMillis() - timestampOfMax > TIMEOUT_SECONDS * 1000;
        }

        public int getCount() {
            if (this.isCancelled()) return 0;
            return maxCount;
        }
    }

    private class PillarSpawner {
        private EnderCrystal crystal;
        private PillarType type;

        private int amountSpawned = 0;

        private PillarSpawner(EnderCrystal crystal, PillarType type) {
            this.crystal = crystal;
            this.type = type;
        }

        private boolean isActive() {
            return crystal != null && !crystal.isDead();
        }

        private void onSpawnedEntityDeath() {
            amountSpawned--;
        }

        private void spawn() {
            Location location = crystal.getLocation();
            World world = location.getWorld();
            PillarId pillarId = new PillarId(location);

            if (world == null) return;

            int playersWithinRange = 0;
            for (Player player : world.getPlayers()) {
                if (isWithinRange(location, player.getLocation(), spawnPlayerRange)) {
                    playersWithinRange++;
                }
            }

            if (amountSpawned >= type.maxSpawnedBase + playersWithinRange * type.maxSpawnedExtra) return;

            for (EntitySpawnSettings entitySpawnSettings : type.spawnSettings) {
                double amount = entitySpawnSettings.base + playersWithinRange * entitySpawnSettings.extra;

                int intAmount = (int) Math.floor(amount);
                double residual = amount - intAmount;
                if (random.nextDouble() <= residual) intAmount++;

                for (int i = 0; i < intAmount; i++) {
                    Entity entity = world.spawnEntity(getPositionNearPillar(location), entitySpawnSettings.entityType);
                    entity.setMetadata(PILLAR_SPAWNED_TAG, new FixedMetadataValue(plugin, pillarId));
                    amountSpawned++;
                }
            }
        }
    }

    private class PillarSpawningTask extends BukkitRunnable {
        private World world;
        private List<PillarType> pillarTypes = new ArrayList<>();

        private Map<PillarId, PillarSpawner> pillarSpawners = new HashMap<>();

        private PillarSpawningTask(@NotNull World world) {
            this.world = world;

            setupPillarTypes();
        }

        @Override
        public void run() {
            if (world.getPlayers().size() == 0) return;

            spawn();
        }

        public void onPillarSpawnedEntityDeath(PillarId pillarId) {
            PillarSpawner pillarSpawner = pillarSpawners.get(pillarId);
            if (pillarSpawner == null) return;
            pillarSpawner.onSpawnedEntityDeath();
        }

        private void spawn() {
            for (EnderCrystal enderCrystal : world.getEntitiesByClass(EnderCrystal.class)) {
                PillarId pillarId = new PillarId(enderCrystal.getLocation());

                PillarSpawner spawner = pillarSpawners.computeIfAbsent(pillarId,
                        k -> new PillarSpawner(enderCrystal, getRandomPillarType()));

                if (spawner.isActive()) spawner.spawn();
                else pillarSpawners.remove(pillarId);
            }
        }

        private void setupPillarTypes() {
            ConfigurationSection pillarTypesSection = config.getConfigurationSection(GimmeHardcore.CONFIG_THE_END_PILLAR_TYPES);
            if (pillarTypesSection == null) return;

            double cumulativeWeight = 0;
            for (String pillarTypeKey : pillarTypesSection.getKeys(false)) {
                ConfigurationSection pillarTypeSection = pillarTypesSection.getConfigurationSection(pillarTypeKey);
                assert pillarTypeSection != null;

                int maxSpawnedBase = pillarTypeSection.getInt(GimmeHardcore.CONFIG_THE_END_PILLAR_TYPES_MAX_SPAWNED_BASE);
                int maxSpawnedExtra = pillarTypeSection.getInt(GimmeHardcore.CONFIG_THE_END_PILLAR_TYPES_MAX_SPAWNED_EXTRA);
                double weight = pillarTypeSection.getDouble(GimmeHardcore.CONFIG_THE_END_PILLAR_TYPES_WEIGHT);
                cumulativeWeight += weight;

                PillarType pillarType = new PillarType(cumulativeWeight, maxSpawnedBase, maxSpawnedExtra);
                pillarTypes.add(pillarType);

                ConfigurationSection entitiesSection = pillarTypeSection.getConfigurationSection(GimmeHardcore.CONFIG_THE_END_PILLAR_TYPES_ENTITIES);
                if (entitiesSection == null) continue;

                for (String entity : entitiesSection.getKeys(false)) {
                    ConfigurationSection entitySection = entitiesSection.getConfigurationSection(entity);
                    assert entitySection != null;

                    pillarType.addEntitySpawn(new EntitySpawnSettings(EntityType.valueOf(entity),
                            entitySection.getDouble(GimmeHardcore.CONFIG_THE_END_SPAWN_BASE),
                            entitySection.getDouble(GimmeHardcore.CONFIG_THE_END_SPAWN_EXTRA)));
                }
            }
        }

        @Nullable
        private PillarType getRandomPillarType() {
            int lastPillarTypeIndex = pillarTypes.size() - 1;
            if (lastPillarTypeIndex == -1) return null;

            double totalWeight = pillarTypes.get(lastPillarTypeIndex).weight;
            double weightRoll = random.nextDouble() * totalWeight;

            for (PillarType pillarType : pillarTypes) {
                if (weightRoll <= pillarType.weight) return pillarType;
            }

            assert false; // This point should never be reachable.
            return null;
        }
    }

    private static class PillarType {
        private double weight;
        private int maxSpawnedBase;
        private int maxSpawnedExtra;
        private List<EntitySpawnSettings> spawnSettings;

        private PillarType(double weight, int maxSpawnedBase, int maxSpawnedExtra, EntitySpawnSettings... entitySpawnSettings) {
            this.weight = weight;
            this.maxSpawnedBase = maxSpawnedBase;
            this.maxSpawnedExtra = maxSpawnedExtra;
            this.spawnSettings = new ArrayList<>(Arrays.asList(entitySpawnSettings));
        }

        private void addEntitySpawn(EntitySpawnSettings entitySpawnSettings) {
            spawnSettings.add(entitySpawnSettings);
        }
    }

    private static class EntitySpawnSettings {
        private EntityType entityType;
        private double base;
        private double extra;

        private EntitySpawnSettings(EntityType entityType, double base, double extra) {
            this.entityType = entityType;
            this.base = base;
            this.extra = extra;
        }
    }

    private static class PillarId {
        @NotNull
        private Point xz;

        private PillarId(int x, int z) {
            this.xz = new Point(x, z);
        }

        private PillarId(Location location) {
            this(location.getBlockX(), location.getBlockZ());
        }

        @Override
        public int hashCode() {
            return xz.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PillarId pillarId = (PillarId) o;
            return Objects.equals(xz, pillarId.xz);
        }
    }

}
