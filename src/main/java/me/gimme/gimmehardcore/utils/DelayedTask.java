package me.gimme.gimmehardcore.utils;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DelayedTask {
    private Plugin plugin;
    private Runnable onFinish;

    private BukkitRunnable runnable;

    public DelayedTask(@NotNull Plugin plugin) {
        this(plugin, null);
    }

    public DelayedTask(@NotNull Plugin plugin, @Nullable Runnable onFinish) {
        this.plugin = plugin;
        this.onFinish = onFinish;
    }

    public void start(long delay) {
        runnable = new BukkitRunnable() {
            @Override
            public void run() {
                if (onFinish != null) onFinish.run();
            }
        };
        runnable.runTaskLater(plugin, delay);
    }

    public void restart(long delay) {
        cancel();
        start(delay);
    }

    public void cancel() {
        if (runnable == null) return;
        runnable.cancel();
        runnable = null;
    }

    public void finish() {
        if (runnable == null) return;
        runnable.run();
        cancel();
    }

    public void setOnFinish(@Nullable Runnable onFinish) {
        this.onFinish = onFinish;
    }
}
