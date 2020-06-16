package me.gimme.gimmehardcore.advancements.crazyadvancements;

import eu.endercentral.crazy_advancements.AdvancementDisplay;
import eu.endercentral.crazy_advancements.AdvancementReward;
import eu.endercentral.crazy_advancements.AdvancementVisibility;
import eu.endercentral.crazy_advancements.NameKey;
import me.gimme.gimmehardcore.advancements.Completer;
import me.gimme.gimmehardcore.advancements.Reward;
import me.gimme.gimmehardcore.advancements.completers.AutoCompleter;
import me.gimme.gimmehardcore.advancements.completers.ObtainCompleter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class Advancement {

    public enum Frame {
        TASK,
        GOAL,
        CHALLENGE
    }

    public enum Visibility {
        ALWAYS,
        PARENT_GRANTED,
        VANILLA,
        HIDDEN
    }

    private String namespace;
    private eu.endercentral.crazy_advancements.Advancement crazyAdvancement;
    @Nullable
    private Advancement parent;
    private List<Advancement> children = new ArrayList<>();
    private String title;
    private String description;
    private ItemStack icon;

    private float x = 1;
    private float y = 0;
    private Frame frame = Frame.TASK;
    private Visibility visibility = Visibility.VANILLA;
    @Nullable
    private Reward reward;
    private boolean showToast = true;
    private boolean announceChat = true;
    private boolean mirrored = false;

    private List<Completer> completers = new ArrayList<>();

    @Nullable
    private String backgroundTexture;

    public Advancement(@Nullable Advancement parent, @NotNull String title, @NotNull String description,
                       @NotNull Material icon) {
        this(parent, title, description, new ItemStack(icon));
    }

    public Advancement(@Nullable Advancement parent, @NotNull String title, @NotNull String description,
                       @NotNull ItemStack icon) {
        this.parent = parent;
        this.title = title;
        this.description = description;
        this.icon = icon;
        this.namespace = parent == null ? namespacicfy(title) : parent.namespace;

        if (parent != null) {
            parent.addChild(this);
        }
    }

    public static Advancement obtainItem(@NotNull Advancement parent, @NotNull Material material, @NotNull String description) {
        return new Advancement(parent, capitalizeTitle(material.name()), description, material)
                .addCompleter(new ObtainCompleter(material));
    }

    public static Advancement root(@NotNull String title, @NotNull String description, @NotNull Material icon,
                                   @NotNull String backgroundTexture) {

        return new Advancement(null, title, description, icon)
                .addCompleter(new AutoCompleter())
                .setX(0)
                .setY(0)
                .setAnnounceChat(false)
                .setShowToast(false)
                .setBackgroundTexture(backgroundTexture);
    }

    public Advancement setX(float x) {
        this.x = x;
        return this;
    }

    public Advancement setY(float y) {
        this.y = y;
        return this;
    }

    public Advancement setFrame(Frame frame) {
        this.frame = frame;
        return this;
    }

    public Advancement setVisibility(Visibility visibility) {
        this.visibility = visibility;
        return this;
    }

    public Advancement setReward(@Nullable Reward reward) {
        this.reward = reward;
        return this;
    }

    public Advancement setShowToast(boolean showToast) {
        this.showToast = showToast;
        return this;
    }

    public Advancement setAnnounceChat(boolean announceChat) {
        this.announceChat = announceChat;
        return this;
    }

    public Advancement addCompleter(Completer completer) {
        this.completers.add(completer);
        return this;
    }

    public Advancement setMirrored(boolean mirrored) {
        this.mirrored = mirrored;
        return this;
    }

    private Advancement setBackgroundTexture(String backgroundTexture) {
        this.backgroundTexture = backgroundTexture;
        return this;
    }

    public String getNamespace() {
        return namespace;
    }

    @NotNull
    public eu.endercentral.crazy_advancements.Advancement getCrazyAdvancement() {
        return crazyAdvancement;
    }

    public boolean isMirrored() {
        return mirrored;
    }

    @NotNull
    public List<Completer> getCompleters() {
        return completers;
    }

    @NotNull
    public List<Advancement> getChildren() {
        return children;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public eu.endercentral.crazy_advancements.Advancement build() {
        AdvancementVisibility advancementVisibility;
        if (visibility.equals(Visibility.ALWAYS)) advancementVisibility = AdvancementVisibility.ALWAYS;
        else if (visibility.equals(Visibility.HIDDEN)) advancementVisibility = AdvancementVisibility.HIDDEN;
        else if (visibility.equals(Visibility.PARENT_GRANTED)) advancementVisibility = AdvancementVisibility.PARENT_GRANTED;
        else advancementVisibility = AdvancementVisibility.VANILLA;

        AdvancementDisplay rootDisplay = new AdvancementDisplay(
                icon,
                title,
                description,
                AdvancementDisplay.AdvancementFrame.valueOf(frame.name()),
                showToast,
                announceChat,
                advancementVisibility);

        rootDisplay.setX(getAbsoluteX());
        rootDisplay.setY(getAbsoluteY());
        rootDisplay.setBackgroundTexture(backgroundTexture);

        String key = parent == null ? "root" : namespacicfy(title);

        this.crazyAdvancement = new eu.endercentral.crazy_advancements.Advancement(
                parent == null ? null : parent.crazyAdvancement, new NameKey(namespace, key), rootDisplay);

        if (reward != null) this.crazyAdvancement.setReward(new AdvancementReward() {
            @Override
            public void onGrant(Player player) {
                reward.onReward(player);
            }
        });

        return this.crazyAdvancement;
    }

    public int getDepth() {
        int depth = 0;
        for (Advancement child : children) {
            int childDepth = child.getDepth();
            if (childDepth >= depth) depth = childDepth + 1;
        }
        return depth;
    }

    @Nullable
    public Float getMinY(int depth) {
        if (depth == 0) return getAbsoluteY();

        Float min = null;
        for (Advancement child : children) {
            Float childMin = child.getMinY(depth - 1);
            if (min == null) min = childMin;
            else if (childMin != null) min = Math.min(min, childMin);
        }
        return min;
    }

    @Nullable
    public Float getMaxY(int depth) {
        if (depth == 0) return getAbsoluteY();

        Float min = null;
        for (Advancement child : children) {
            Float childMax = child.getMaxY(depth - 1);
            if (min == null) min = childMax;
            else if (childMax != null) min = Math.max(min, childMax);
        }
        return min;
    }

    public boolean isRoot() {
        return parent == null;
    }

    private void addChild(Advancement advancement) {
        children.add(advancement);
    }

    private float getAbsoluteX() {
        if (parent == null) return x;
        return parent.getAbsoluteX() + x;
    }

    private float getAbsoluteY() {
        if (parent == null) return y;
        return parent.getAbsoluteY() + y;
    }

    private static Set<String> capitalizeExemptWords = new HashSet<>(Arrays.asList("of", "the", "a", "an", "and", "or", "in", "for"));
    private static String capitalizeTitle(String title) {
        StringBuilder capitalized = new StringBuilder();
        Scanner lineScan = new Scanner(title.toLowerCase().replace("_", " "));
        boolean isFirstWord = true;
        while(lineScan.hasNext()) {
            String word = lineScan.next();
            boolean capitalizeWord = isFirstWord || !capitalizeExemptWords.contains(word);

            if (!isFirstWord) capitalized.append(" ");

            if (capitalizeWord) capitalized.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
            else capitalized.append(word);

            isFirstWord = false;
        }
        return capitalized.toString();
    }

    private static String namespacicfy(String input) {
        return input.toLowerCase().replaceAll(" ", "_");
    }
}
