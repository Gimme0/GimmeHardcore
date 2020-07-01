package me.gimme.gimmehardcore.hooks;

import com.google.common.reflect.TypeToken;
import com.google.gson.*;
import eu.endercentral.crazy_advancements.manager.AdvancementManager;
import me.gimme.gimmehardcore.advancements.crazyadvancements.Advancement;
import me.gimme.gimmehardcore.utils.JsonUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class CrazyAdvancementsHook implements Listener {
    private static final String ADVANCEMENTS_DIRECTORY = "advancements";

    private Plugin plugin;
    private AdvancementManager manager;

    private Set<String> namespaces = new HashSet<>();

    public CrazyAdvancementsHook(@NotNull Plugin plugin) {
        this.plugin = plugin;
        this.manager = AdvancementManager.getNewAdvancementManager();
    }

    public void registerAdvancements(@NotNull List<Advancement> advancements) {
        for (Advancement advancement : advancements) {
            namespaces.add(advancement.getNamespace());
        }

        manager.addAdvancement(advancements.stream().map(Advancement::build).toArray(eu.endercentral.crazy_advancements.Advancement[]::new));
    }

    public void grantAdvancement(@NotNull Player player, @NotNull Advancement advancement) {
        manager.grantAdvancement(player, advancement.getCrazyAdvancement());

        if (advancement.getCrazyAdvancement().getDisplay().isAnnouncedToChat())
            manager.displayMessage(player, advancement.getCrazyAdvancement());
    }

    public boolean isGranted(@NotNull Advancement advancement, @NotNull Player player) {
        return advancement.getCrazyAdvancement().isGranted(player);
    }

    public void onDisable() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            saveProgress(player);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private void onPlayerJoin(PlayerJoinEvent event) {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (String namespace : namespaces) {
                    loadProgress(event.getPlayer(), namespace);
                }
                manager.addPlayer(event.getPlayer());
            }
        }.runTaskLater(plugin, 20L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private void onPlayerQuit(PlayerQuitEvent event) {
        saveProgress(event.getPlayer());
        manager.removePlayer(event.getPlayer());
    }

    private void loadProgress(@NotNull Player player, @NotNull String namespace) {
        if (!existsSaveFile(player, namespace)) return;

        Map<String, List<String>> progress = getProgress(player, namespace);

        for (eu.endercentral.crazy_advancements.Advancement advancement : manager.getAdvancements()) {
            if (advancement.getName().getNamespace().equalsIgnoreCase(namespace)) {
                manager.getCriteriaProgress(player, advancement); //checkAwarded(player, advancement);
                String nameKey = advancement.getName().toString();
                if (progress.containsKey(nameKey)) {
                    List<String> loaded = progress.get(nameKey);
                    manager.grantCriteria(player, advancement, loaded.toArray(new String[0]));
                }
            }
        }
    }

    private Map<String, List<String>> getProgress(@NotNull Player player, @NotNull String namespace) {
        try {
            FileReader reader = new FileReader(getSavePath(player, namespace).toString());
            JsonElement element = new JsonParser().parse(reader);
            reader.close();

            Type type = new TypeToken<Map<String, List<String>>>(){}.getType();
            return new Gson().fromJson(element, type);
        } catch (Exception var8) {
            var8.printStackTrace();
            return new HashMap<>();
        }
    }

    private void saveProgress(@NotNull Player player) {
        for (String namespace : namespaces) {
            saveProgress(player, namespace);
        }
    }

    private void saveProgress(@NotNull Player player, @NotNull String namespace) {
        String json = JsonUtils.toPrettyFormat(manager.getProgressJSON(player, namespace));

        try {
            Path path = getSavePath(player, namespace);
            Files.createDirectories(path.getParent());
            Files.write(path, json.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean existsSaveFile(@NotNull Player player, @NotNull String namespace) {
        File saveFile = getSavePath(player, namespace).toFile();
        return saveFile.exists() && saveFile.isFile();
    }

    private Path getSavePath(@NotNull Player player, @NotNull String namespace) {
        return Paths.get(plugin.getDataFolder().getAbsolutePath()
                + File.separator + ADVANCEMENTS_DIRECTORY
                + File.separator + namespace
                + File.separator + player.getUniqueId() + ".json");
    }

}
