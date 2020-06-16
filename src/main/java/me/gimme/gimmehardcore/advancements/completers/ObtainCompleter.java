package me.gimme.gimmehardcore.advancements.completers;

import me.gimme.gimmehardcore.advancements.Completer;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class ObtainCompleter extends Completer {
    private enum CompareType {
        ITEM,
        MATERIAL,
        POTION
    }

    private Set<Material> types = new HashSet<>();
    private List<ItemStack> items = new ArrayList<>();
    private PotionMeta potionMeta;
    private CompareType compareType;

    public ObtainCompleter(@NotNull Material... types) {
        Collections.addAll(this.types, types);
        this.compareType = CompareType.MATERIAL;
    }

    public ObtainCompleter(@NotNull ItemStack... items) {
        Collections.addAll(this.items, items);
        this.types = Arrays.stream(items).map(ItemStack::getType).collect(Collectors.toSet());
        this.compareType = CompareType.ITEM;
    }

    public ObtainCompleter(@NotNull PotionMeta potionMeta) {
        this.types.add(Material.POTION);
        this.types.add(Material.SPLASH_POTION);
        this.types.add(Material.LINGERING_POTION);
        this.potionMeta = potionMeta;
        this.compareType = CompareType.POTION;
    }

    private boolean compareItem(@Nullable ItemStack otherItem) {
        if (otherItem == null) return false;

        switch (compareType) {
            case MATERIAL:
                return types.contains(otherItem.getType());
            case ITEM:
                if (!types.contains(otherItem.getType())) return false;
                for (ItemStack itemStack : items) {
                    if (itemStack.isSimilar(otherItem)) return true;
                }
                return false;
            case POTION:
                if (!types.contains(otherItem.getType())) return false;
                PotionMeta otherPotionMeta = (PotionMeta) otherItem.getItemMeta();
                assert otherPotionMeta != null;
                return otherPotionMeta.getBasePotionData().getType().equals(potionMeta.getBasePotionData().getType());
            default:
                return false;
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private void onPickupItem(EntityPickupItemEvent event) {
        if (event.isCancelled()) return;
        if (!event.getEntity().getType().equals(EntityType.PLAYER)) return;
        if (!compareItem(event.getItem().getItemStack())) return;

        grant((Player) event.getEntity());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private void onCraftItem(InventoryClickEvent event) {
        if (event.isCancelled()) return;
        if (event.getClickedInventory() == null) return;
        if (!event.getClickedInventory().getType().equals(InventoryType.WORKBENCH)) return;
        if (!event.getSlotType().equals(InventoryType.SlotType.RESULT)) return;
        if (!event.getWhoClicked().getType().equals(EntityType.PLAYER)) return;

        ItemStack currentItem = event.getCurrentItem();
        if (currentItem == null) return;
        if (!compareItem(currentItem)) return;

        grant((Player) event.getWhoClicked());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private void onInventoryMoveItem(InventoryClickEvent event) {
        if (event.isCancelled()) return;

        ItemStack item;

        InventoryAction action = event.getAction();
        if (action.equals(InventoryAction.PLACE_ALL) || action.equals(InventoryAction.PLACE_ONE)
                || action.equals(InventoryAction.PLACE_SOME)) {
            item = event.getCursor();
        } else if (action.equals(InventoryAction.MOVE_TO_OTHER_INVENTORY)) {
            if (event.getClickedInventory() != null
                    && event.getClickedInventory().getType().equals(InventoryType.PLAYER)) return;
            item = event.getCurrentItem();
        } else return;

        if (!compareItem(item)) return;

        grant((Player) event.getWhoClicked());
    }
}
