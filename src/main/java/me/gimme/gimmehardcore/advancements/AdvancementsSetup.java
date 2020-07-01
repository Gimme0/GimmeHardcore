package me.gimme.gimmehardcore.advancements;

import me.gimme.gimmehardcore.advancements.completers.EnchantCompleter;
import me.gimme.gimmehardcore.advancements.completers.EnterEnvironmentCompleter;
import me.gimme.gimmehardcore.advancements.completers.ObtainCompleter;
import me.gimme.gimmehardcore.advancements.completers.SlayEntityCompleter;
import me.gimme.gimmehardcore.advancements.crazyadvancements.Advancement;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;
import java.util.List;

public class AdvancementsSetup {

    private List<Advancement> advancements = new ArrayList<>();

    public static List<Advancement> setupAdvancements() {
        return new AdvancementsSetup().getAdvancements();
    }

    private List<Advancement> getAdvancements() {

        Advancement root = add(Advancement.root(
                "Hardcore",
                "Don't die",
                Material.WITHER_SKELETON_SKULL,
                "textures/block/black_concrete.png"));

        add(new Advancement(
                root,
                "Eternal Slumber",
                "It's time to let go",
                Material.PLAYER_HEAD)
                .addCompleter(new Completer() {
                    @EventHandler(priority = EventPriority.MONITOR)
                    private void onDeath(PlayerDeathEvent event) {
                        grant(event.getEntity());
                    }
                })
                .setFrame(Advancement.Frame.GOAL)
                .setVisibility(Advancement.Visibility.HIDDEN)
                .setMirrored(true));

        setupHealingAdvancements(root);
        setupMainAdvancements(root);
        setupPickaxeAdvancements(root);
        setupProtectionAdvancements(root);
        setupWeaponAdvancements(root);

        return advancements;
    }

    private void setupHealingAdvancements(Advancement root) {
        Advancement oxeyeDaisy = add(Advancement.obtainItem(root, Material.OXEYE_DAISY, "A fine ingredient"));
        Advancement suspiciousStew = add(Advancement.obtainItem(oxeyeDaisy, Material.SUSPICIOUS_STEW, "The first healing")
                .setFrame(Advancement.Frame.GOAL));

        Advancement goldenApple = add(Advancement.obtainItem(suspiciousStew, Material.GOLDEN_APPLE, "Temptation")
                .setFrame(Advancement.Frame.GOAL));

        Advancement netherWart = add(Advancement.obtainItem(suspiciousStew, Material.NETHER_WART, "The base of all potions"));

        Advancement ghastTear = add(new Advancement(
                netherWart,
                "Ghast Tear",
                "Ghast hunter",
                Material.GHAST_TEAR)
                .addCompleter(new ObtainCompleter(Material.GHAST_TEAR)));
        ItemStack regenPotionIcon = new ItemStack(Material.POTION);
        PotionMeta regenPotionIconMeta = (PotionMeta) regenPotionIcon.getItemMeta();
        regenPotionIconMeta.setBasePotionData(new PotionData(PotionType.REGEN));
        regenPotionIcon.setItemMeta(regenPotionIconMeta);
        Advancement regenPotion = add(new Advancement(
                ghastTear,
                "Regeneration Potion",
                "",
                regenPotionIcon)
                .setFrame(Advancement.Frame.GOAL)
                .addCompleter(new ObtainCompleter(regenPotionIconMeta)));

        Advancement roseBush = add(Advancement.obtainItem(netherWart, Material.ROSE_BUSH, "They say it's revealed by the moonlight"));
        Advancement glisteringMelonSlice = add(Advancement.obtainItem(roseBush, Material.GLISTERING_MELON_SLICE, "A glistering ingredient"));
        ItemStack healingPotionIcon = new ItemStack(Material.POTION);
        PotionMeta healingPotionMeta = (PotionMeta) healingPotionIcon.getItemMeta();
        healingPotionMeta.setBasePotionData(new PotionData(PotionType.INSTANT_HEAL));
        healingPotionIcon.setItemMeta(healingPotionMeta);
        Advancement healingPotion = add(new Advancement(
                glisteringMelonSlice,
                "Healing Potion",
                "",
                healingPotionIcon)
                .setFrame(Advancement.Frame.GOAL)
                .addCompleter(new ObtainCompleter(healingPotionMeta)));
    }

    private void setupMainAdvancements(Advancement root) {
        Advancement log = add(new Advancement(
                root,
                "Log",
                "Timber!",
                Material.OAK_LOG)
                .addCompleter(new ObtainCompleter(Material.ACACIA_LOG, Material.BIRCH_LOG, Material.DARK_OAK_LOG,
                        Material.JUNGLE_LOG, Material.OAK_LOG, Material.SPRUCE_LOG)));
        Advancement cobblestone = add(Advancement.obtainItem(log, Material.COBBLESTONE, "Break ground"));
        Advancement ironOre = add(Advancement.obtainItem(cobblestone, Material.IRON_ORE, "Stronger than stone"));
        Advancement tnt = add(new Advancement(
                ironOre,
                "TNT",
                "This will break any rock",
                Material.TNT)
                .addCompleter(new ObtainCompleter(Material.TNT)));
        Advancement diamond = add(Advancement.obtainItem(tnt, Material.DIAMOND, "The ultimate gemstone")
                .setFrame(Advancement.Frame.CHALLENGE)
                .addCompleter(new Completer() {
                    private double maxDistanceSquared = Math.pow(30, 2);
                    @EventHandler(priority = EventPriority.MONITOR)
                    private void onExplodeDiamond(BlockExplodeEvent event) {
                        if (event.isCancelled()) return;
                        if (!event.getBlock().getType().equals(Material.DIAMOND)) return;

                        for (Player player : event.getBlock().getWorld().getPlayers()) {
                            if (player.getLocation().distanceSquared(event.getBlock().getLocation()) > maxDistanceSquared) continue;
                            grant(player);
                        }
                    }
                }));
        Advancement obsidian = add(Advancement.obtainItem(diamond, Material.OBSIDIAN, "Time to go to hell"));
        Advancement enterTheNether = add(new Advancement(
                obsidian,
                "Enter the Nether",
                "Welcome to hell",
                Material.NETHERRACK)
                .setFrame(Advancement.Frame.GOAL)
                .addCompleter(new EnterEnvironmentCompleter(World.Environment.NETHER)));
        Advancement blazeRod = add(Advancement.obtainItem(enterTheNether, Material.BLAZE_ROD, "You'll need a few...")
                .setFrame(Advancement.Frame.CHALLENGE)
                .addCompleter(new Completer() {
                    private double maxDistanceSquared = Math.pow(30, 2);
                    @EventHandler(priority = EventPriority.MONITOR)
                    private void onDropBlazeRod(EntityDeathEvent event) {
                        if (!event.getEntity().getType().equals(EntityType.BLAZE)) return;
                        if (event.getDrops().stream().noneMatch((item) -> item.getType().equals(Material.BLAZE_ROD))) return;

                        for (Player player : event.getEntity().getWorld().getPlayers()) {
                            if (player.getLocation().distanceSquared(event.getEntity().getLocation()) > maxDistanceSquared) continue;
                            grant(player);
                        }
                    }
                }));
        Advancement enderEye = add(Advancement.obtainItem(blazeRod, Material.ENDER_EYE, "This will lead the way"));
        Advancement enterTheEnd = add(new Advancement(
                enderEye,
                "Enter the End",
                "No looking back",
                Material.END_PORTAL_FRAME)
                .setFrame(Advancement.Frame.GOAL)
                .addCompleter(new EnterEnvironmentCompleter(World.Environment.THE_END)));
        Advancement slayTheDragon = add(new Advancement(
                enterTheEnd,
                "Slay the Dragon",
                "The final boss",
                Material.DRAGON_HEAD)
                .setFrame(Advancement.Frame.CHALLENGE)
                .addCompleter(new SlayEntityCompleter(EntityType.ENDER_DRAGON)));

        Advancement elytra = add(Advancement.obtainItem(slayTheDragon, Material.ELYTRA, "I believe I can fly")
                .setFrame(Advancement.Frame.GOAL));

        Advancement slayTheWither = add(new Advancement(
                slayTheDragon,
                "Slay the Wither",
                "The final final boss",
                Material.NETHER_STAR)
                .setFrame(Advancement.Frame.CHALLENGE)
                .addCompleter(new SlayEntityCompleter(EntityType.WITHER, 100)));
        Advancement beacon = add(Advancement.obtainItem(slayTheWither, Material.BEACON, "A beacon of hope")
                .setFrame(Advancement.Frame.GOAL)
                .setVisibility(Advancement.Visibility.PARENT_GRANTED));

        Advancement slayTheElderGuardian = add(new Advancement(
                slayTheDragon,
                "Slay an Elder Guardian",
                "The ocean boss",
                Material.SPONGE)
                .setFrame(Advancement.Frame.CHALLENGE)
                .addCompleter(new SlayEntityCompleter(EntityType.ELDER_GUARDIAN, 100)));
        Advancement conduit = add(Advancement.obtainItem(slayTheElderGuardian, Material.CONDUIT, "King of the ocean")
                .setFrame(Advancement.Frame.GOAL)
                .setVisibility(Advancement.Visibility.PARENT_GRANTED));
    }

    private void setupPickaxeAdvancements(Advancement root) {
        Advancement wooodenPick = add(Advancement.obtainItem(root, Material.WOODEN_PICKAXE, "Get some stone"));
        Advancement stonePick = add(Advancement.obtainItem(wooodenPick, Material.STONE_PICKAXE, "Get some iron"));
        Advancement ironPick = add(Advancement.obtainItem(stonePick, Material.IRON_PICKAXE, "Get some gold"));
        Advancement diamondPick = add(Advancement.obtainItem(ironPick, Material.DIAMOND_PICKAXE, "Get some obsidian")
                .setFrame(Advancement.Frame.GOAL));
    }

    private void setupProtectionAdvancements(Advancement root) {
        Advancement shield = add(Advancement.obtainItem(root, Material.SHIELD, "Your best bud"));
        Advancement ironArmor = add(new Advancement(
                shield,
                "Iron Armor",
                "Suit up",
                Material.IRON_CHESTPLATE)
                .addCompleter(new ObtainCompleter(Material.IRON_HELMET, Material.IRON_CHESTPLATE, Material.IRON_LEGGINGS, Material.IRON_BOOTS)));

        Advancement diamondArmor = add(new Advancement(
                ironArmor,
                "Diamond Armor",
                "An expensive upgrade",
                Material.DIAMOND_CHESTPLATE)
                .addCompleter(new ObtainCompleter(Material.DIAMOND_HELMET, Material.DIAMOND_CHESTPLATE, Material.DIAMOND_LEGGINGS, Material.DIAMOND_BOOTS)));

        Advancement netheriteArmor = add(new Advancement(
                diamondArmor,
                "Netherite Armor",
                "The Dark Knight",
                Material.NETHERITE_CHESTPLATE)
                .setFrame(Advancement.Frame.GOAL)
                .addCompleter(new ObtainCompleter(Material.NETHERITE_HELMET, Material.NETHERITE_CHESTPLATE, Material.NETHERITE_LEGGINGS, Material.NETHERITE_BOOTS)));

        ItemStack enchantedArmorIcon = new ItemStack(Material.IRON_CHESTPLATE);
        enchantedArmorIcon.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
        Advancement enchantArmor = add(new Advancement(
                ironArmor,
                "Enchanted Armor",
                "With the power of magic",
                enchantedArmorIcon)
                .setFrame(Advancement.Frame.GOAL)
                .addCompleter(new EnchantCompleter(
                        Material.IRON_HELMET, Material.IRON_CHESTPLATE, Material.IRON_LEGGINGS, Material.IRON_BOOTS,
                        Material.DIAMOND_HELMET, Material.DIAMOND_CHESTPLATE, Material.DIAMOND_LEGGINGS, Material.DIAMOND_BOOTS,
                        Material.CHAINMAIL_HELMET, Material.CHAINMAIL_CHESTPLATE, Material.CHAINMAIL_LEGGINGS, Material.CHAINMAIL_BOOTS,
                        Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS,
                        Material.GOLDEN_HELMET, Material.GOLDEN_CHESTPLATE, Material.GOLDEN_LEGGINGS, Material.GOLDEN_BOOTS)));
    }

    private void setupWeaponAdvancements(Advancement root) {
        Advancement sword = add(new Advancement(
                root,
                "Melee Weapon",
                "My first weapon",
                Material.STONE_SWORD)
                .addCompleter(new ObtainCompleter(
                        Material.WOODEN_SWORD, Material.STONE_SWORD, Material.IRON_SWORD, Material.DIAMOND_SWORD, Material.GOLDEN_SWORD,
                        Material.STONE_AXE, Material.IRON_AXE, Material.DIAMOND_AXE)));

        Advancement swordUpgrade = add(new Advancement(
                sword,
                "Melee Weapon Upgrade",
                "My first real weapon",
                Material.IRON_SWORD)
                .addCompleter(new ObtainCompleter(
                        Material.IRON_SWORD, Material.DIAMOND_SWORD,
                        Material.IRON_AXE, Material.DIAMOND_AXE)));
        ItemStack enchantedSwordIcon = new ItemStack(Material.IRON_SWORD);
        enchantedSwordIcon.addEnchantment(Enchantment.DAMAGE_ALL, 1);
        Advancement enchantedSword = add(new Advancement(
                swordUpgrade,
                "Melee Weapon Enchantment",
                "Excalibur",
                enchantedSwordIcon)
                .setFrame(Advancement.Frame.GOAL)
                .addCompleter(new EnchantCompleter(Material.WOODEN_SWORD, Material.STONE_SWORD, Material.IRON_SWORD, Material.DIAMOND_SWORD, Material.GOLDEN_SWORD)));

        Advancement ranged = add(new Advancement(
                sword,
                "Ranged Weapon",
                "It's safer from a distance",
                Material.BOW)
                .addCompleter(new ObtainCompleter(Material.BOW, Material.CROSSBOW)));
        ItemStack enchantedBowIcon = new ItemStack(Material.BOW);
        enchantedBowIcon.addEnchantment(Enchantment.ARROW_DAMAGE, 1);
        Advancement enchantedRanged = add(new Advancement(
                ranged,
                "Ranged Weapon Enchantment",
                "It's safer with magic",
                enchantedBowIcon)
                .setFrame(Advancement.Frame.GOAL)
                .addCompleter(new EnchantCompleter(Material.BOW, Material.CROSSBOW)));
    }

    private Advancement add(Advancement advancement) {
        advancements.add(advancement);
        return advancement;
    }
}
