package me.gimme.gimmehardcore.advancements;

import me.gimme.gimmehardcore.GimmeHardcore;
import me.gimme.gimmehardcore.advancements.crazyadvancements.Advancement;
import me.gimme.gimmehardcore.hooks.CrazyAdvancementsHook;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class AdvancementManager {

    private Plugin plugin;
    private CrazyAdvancementsHook crazyAdvancementsHook;

    private boolean silent;

    public AdvancementManager(@NotNull Plugin plugin, @NotNull CrazyAdvancementsHook crazyAdvancementsHook) {
        this.plugin = plugin;
        this.crazyAdvancementsHook = crazyAdvancementsHook;
        plugin.getServer().getPluginManager().registerEvents(crazyAdvancementsHook, plugin);

        this.silent = plugin.getConfig().getBoolean(GimmeHardcore.CONFIG_HARDCORE_ADVANCEMENTS_SILENT);
    }

    public void setupAdvancements() {
        registerAdvancements(AdvancementsSetup.setupAdvancements());
    }

    public void grantAdvancement(@NotNull Player player, @NotNull Advancement advancement) {
        if (crazyAdvancementsHook.isGranted(advancement, player)) return;
        crazyAdvancementsHook.grantAdvancement(player, advancement);
    }

    private void registerAdvancements(List<Advancement> advancements) {
        autoAlign(advancements);

        for (Advancement advancement : advancements) {
            for (Completer completer : advancement.getCompleters()) {
                completer.setManager(this);
                completer.setAdvancement(advancement);
                plugin.getServer().getPluginManager().registerEvents(completer, plugin);
            }

            if (silent) advancement.setShowToast(false).setAnnounceChat(false);
        }

        crazyAdvancementsHook.registerAdvancements(advancements);
    }

    private void autoAlign(List<Advancement> advancements) {
        for (Advancement advancement : advancements) {
            if (!advancement.isRoot()) continue;
            autoAlignChildren(advancement, false);
        }
    }

    private void autoAlignChildren(Advancement advancement, boolean mirrored) {
        if (advancement.isMirrored()) mirrored = !mirrored;
        if (mirrored) advancement.setX(-advancement.getX());

        List<Advancement> children = advancement.getChildren();

        // Recursively do the same for each advancement in the tree
        for (Advancement child : children) {
            autoAlignChildren(child, mirrored);
        }

        List<Advancement> leftChildren = new ArrayList<>();
        List<Advancement> rightChildren = new ArrayList<>();
        for (Advancement child : children) {
            if (child.isMirrored()) leftChildren.add(child);
            else rightChildren.add(child);
        }
        alignChildren(leftChildren);
        alignChildren(rightChildren);
    }

    private void alignChildren(List<Advancement> children) {
        // Align direct children, by checking each depth level
        for (int i = 0; i < children.size() - 1; i++) {
            Advancement a = children.get(i);
            Advancement b = children.get(i+1);

            // Compare each depth level of a and b, and move b downward enough to not overlap anywhere
            int minDepth = Math.min(a.getDepth(), b.getDepth());
            for (int depth = 0; depth <= minDepth; depth++) {
                Float aMaxY = a.getMaxY(depth);
                Float bMinY = b.getMinY(depth);
                assert aMaxY != null;
                assert bMinY != null;

                float distance = bMinY - aMaxY;
                // The distance between them should be at least 1
                if (distance >= 1) continue;
                b.setY(b.getY() + (1 - distance));
            }
        }

        // Make root be in the middle of the children's y values
        if (children.size() > 0) {
            float min = Float.POSITIVE_INFINITY;
            float max = Float.NEGATIVE_INFINITY;
            for (Advancement child : children) {
                float y = child.getY();
                min = Math.min(min, y);
                max = Math.max(max, y);
            }

            // Move children to make the center be at 0
            float average = (min + max) / 2;
            for (Advancement child : children) {
                child.setY(child.getY() - average);
            }
        }
    }
}
