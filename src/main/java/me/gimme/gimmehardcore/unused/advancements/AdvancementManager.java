package me.gimme.gimmehardcore.unused.advancements;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.gimme.gimmehardcore.GimmeHardcore;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.UnsafeValues;
import org.bukkit.advancement.Advancement;
import org.bukkit.plugin.Plugin;

import java.io.*;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Handles loading in advancements from advancements.json (creates it with the default values if it doesn't exist).
 */
public class AdvancementManager {

    private final String ADVANCEMENTS_PATH = "advancements.json";
    @SuppressWarnings("FieldCanBeLocal")
    private final String DEFAULT_ADVANCEMENTS_RESOURCE_PATH = "/" + ADVANCEMENTS_PATH;

    private Plugin plugin;
    private Logger logger;
    private UnsafeValues unsafeValues;
    private File advancementsFile;

    public AdvancementManager() {
        plugin = GimmeHardcore.getPlugin(GimmeHardcore.class);
        logger = plugin.getLogger();
        unsafeValues = Bukkit.getUnsafe();
        File dataFolder = plugin.getDataFolder();
        advancementsFile = new File(dataFolder, ADVANCEMENTS_PATH);

        saveDefaultAdvancements();
    }

    /**
     * Reloads all the advancements from advancements.json
     *
     * Does not affect player progress as long as the path or criteria of the made advancement isn't changed.
     */
    public void setupAdvancements() {
        cleanHardcoreAdvancements();
        loadHardcoreAdvancements();
    }

    /**
     * Removes all hardcore advancements.
     */
    public void cleanHardcoreAdvancements() {
        cleanAdvancements(plugin.getName().toLowerCase(Locale.ROOT));
        Bukkit.reloadData(); // Need to reload or the cleaned advancements can't be replaced
    }

    /**
     * Creates the default advancements file if it doesn't exist already.
     */
    private void saveDefaultAdvancements() {
        if (!new File(plugin.getDataFolder(), ADVANCEMENTS_PATH).exists()) plugin.saveResource(ADVANCEMENTS_PATH, false);
        /*try {
            if (advancementsFile.createNewFile()) {
                InputStream in = getClass().getResourceAsStream(DEFAULT_ADVANCEMENTS_RESOURCE_PATH);
                byte[] buffer = new byte[in.available()];
                in.read(buffer);

                FileOutputStream out = new FileOutputStream(advancementsFile);
                out.write(buffer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }

    /**
     * Removes all advancements under this namespace.
     * @param namespace the namespace to remove from
     */
    private void cleanAdvancements(String namespace) {
        int achievementsCleaned = 0;
        Iterator<Advancement> advancements = Bukkit.advancementIterator();
        while (advancements.hasNext()) {
            NamespacedKey namespacedKey = advancements.next().getKey();
            if (namespacedKey.getNamespace().equals(namespace)) {
                unsafeValues.removeAdvancement(namespacedKey);
                achievementsCleaned++;
            }
        }
        logger.info("Cleaned " + achievementsCleaned + " advancements");
    }

    private void loadHardcoreAdvancements() {
        try {
            loadAdvancements(new FileReader(advancementsFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Bukkit.reloadData(); // Need to reload for the advancements to register
    }

    /**
     * Loads all the advancements from a json file into the game.
     * @param jsonReader the reader that reads from a json file
     */
    private void loadAdvancements(Reader jsonReader) {
        int achievementsLoaded = 0;
        JsonParser jsonParser = new JsonParser();
        JsonObject jsonObject = jsonParser.parse(jsonReader).getAsJsonObject();

        for (Map.Entry<String, JsonElement> tab : jsonObject.entrySet()) {
            Set<Map.Entry<String, JsonElement>> advancements = tab.getValue().getAsJsonObject().entrySet();

            for (Map.Entry<String, JsonElement> advancement : advancements) {
                if (unsafeValues.loadAdvancement(
                        new NamespacedKey(plugin, tab.getKey() + "/" + advancement.getKey()), advancement.getValue().toString()) != null)
                    achievementsLoaded++;
            }
        }
        logger.info("Loaded " + achievementsLoaded + " advancements");
    }
}
