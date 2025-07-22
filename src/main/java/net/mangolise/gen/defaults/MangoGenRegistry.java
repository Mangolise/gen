package net.mangolise.gen.defaults;

import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.KeyPattern;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.mangolise.gamesdk.features.EnderChestFeature;
import net.mangolise.gen.registry.GenBlockDrop;
import net.mangolise.gen.registry.GenRegistry;
import net.mangolise.gen.registry.ItemDrop;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.predicate.BlockPredicate;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.BlockPredicates;
import net.minestom.server.item.component.Tool;
import net.minestom.server.registry.RegistryTag;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class MangoGenRegistry implements GenRegistry {
    private static final Tag<Key> ITEM_ID = Tag.String("mangogen.item_id").map(Key::key, Key::asString);

    private final MangoGenRegistrySave saveManager;

    public MangoGenRegistry(MangoGenRegistrySave saveManager) {
        this.saveManager = saveManager;
    }

    // Colors
    private static final TextColor OAK_COLOR = TextColor.color(0xC3A57A);
    private static final TextColor BIRCH_COLOR = TextColor.color(0xd4c989);
    private static final TextColor SPRUCE_COLOR = TextColor.color(0x6c502d);
    private static final TextColor JUNGLE_COLOR = TextColor.color(0x977054);
    private static final TextColor STONE_COLOR = TextColor.color(0x979393);
    private static final TextColor IRON_COLOR = TextColor.color(0xE3EEE3);
    private static final TextColor DIAMOND_COLOR = TextColor.color(0x3BBCBF);
    private static final TextColor NETHERITE_COLOR = TextColor.color(0x64463f);
    private static final TextColor GOLD_COLOR = TextColor.color(0xfaf25e);
    private static final TextColor WHEAT_COLOR = TextColor.color(0xd9b964);
    private static final TextColor ROSE_COLOR = TextColor.color(0xFA7275);
    private static final TextColor BERRY_COLOR = TextColor.color(0xFA3037);
    private static final TextColor POTATO_COLOR = TextColor.color(0xFFC87A);

    // Wood block names
    private static final Component OAK_NAME = Component.text("Oak").color(OAK_COLOR);
    private static final Component BIRCH_NAME = Component.text("Birch").color(BIRCH_COLOR);
    private static final Component SPRUCE_NAME = Component.text("Spruce").color(SPRUCE_COLOR);
    private static final Component JUNGLE_NAME = Component.text("Jungle").color(JUNGLE_COLOR);
    private static final List<Component> WOOD_NAMES = List.of(OAK_NAME, BIRCH_NAME, SPRUCE_NAME, JUNGLE_NAME);

    // Stone block names
    private static final Component STONE_NAME = Component.text("Stone").color(STONE_COLOR);
    private static final Component IRON_NAME = Component.text("Iron").color(IRON_COLOR);
    private static final Component DIAMOND_NAME = Component.text("Diamond").color(DIAMOND_COLOR);
    private static final Component GOLD_NAME = Component.text("Gold").color(GOLD_COLOR);
    private static final Component NETHERITE_NAME = Component.text("Ancient Debris", NETHERITE_COLOR);
    private static final List<Component> STONE_NAMES = List.of(STONE_NAME, IRON_NAME, DIAMOND_NAME, GOLD_NAME, NETHERITE_NAME);

    // Plant block names
    private static final Component WHEAT_NAME = Component.text("Wheat").color(WHEAT_COLOR);
    private static final Component ROSE_NAME = Component.text("Roses").color(ROSE_COLOR);
    private static final Component SWEET_BERRY_NAME = Component.text("Sweet Berry").color(BERRY_COLOR);
    private static final Component POTATO_NAME = Component.text("Potatoes").color(POTATO_COLOR);
    private static final List<Component> PLANT_NAMES = List.of(WHEAT_NAME, ROSE_NAME, SWEET_BERRY_NAME, POTATO_NAME);

    // Blocks
    private static final List<Block> WOOD_BLOCKS = List.of(Block.OAK_PLANKS, Block.BIRCH_PLANKS, Block.SPRUCE_PLANKS, Block.JUNGLE_PLANKS);
    private static final List<Block> STONE_BLOCKS = List.of(Block.STONE, Block.IRON_ORE, Block.DEEPSLATE_DIAMOND_ORE, Block.NETHER_GOLD_ORE, Block.ANCIENT_DEBRIS);
    private static final List<Block> PLANT_BLOCKS = List.of(Block.WHEAT, Block.ROSE_BUSH, Block.SWEET_BERRY_BUSH, Block.POTATOES);

    // Items
    private static ItemStack.Builder createItem(Material material, Component name) {
        return ItemStack.builder(material)
                .customName(name.decoration(TextDecoration.ITALIC, false))
                .hideExtraTooltip();
    }

    private static final Map<Key, ItemStack> ITEMS = new HashMap<>() {
        private void createMatItem(@KeyPattern.Value final @NotNull String id, Material material, String name, TextColor color) {
            Key key = Key.key("mangogen", id);
            put(key, createItem(material, Component.text(name).color(color).decorate(TextDecoration.BOLD))
                    .build().withTag(ITEM_ID, key));
        }

        private void createToolItems(
                @KeyPattern.Value final @NotNull String id,
                Material material, String name, TextColor color,
                float mineSpeed, int breakLimit,
                List<Component> canBreakNames, List<Block> blocks
        ) {
            for (int level = 1; level <= 3; level++) {
                createToolItem(id + "_" + level, material, name, color, mineSpeed, level, breakLimit, canBreakNames, blocks);
            }
        }

        private void createToolItem(
                @KeyPattern.Value final @NotNull String id,
                Material material, String name, TextColor color,
                float mineSpeed, int level, int breakLimit,
                List<Component> canBreakNames, List<Block> blocks
        ) {
            Block[] blockArray = blocks.subList(0, breakLimit).toArray(Block[]::new);
            mineSpeed *= (level - 1) * 0.3f + 1f; // each level give 30% more mining speed

            Component lore = Component.text("Can break ").color(NamedTextColor.GRAY);
            for (int i = 0;;) {
                lore = lore.append(canBreakNames.get(i));

                i++;
                if (i >= breakLimit) {
                    break;
                }

                if (i + 1 >= breakLimit) {
                    lore = lore.append(Component.text(" and ").color(NamedTextColor.GRAY));
                } else {
                    lore = lore.append(Component.text(", ").color(NamedTextColor.GRAY));
                }
            }

            Key key = Key.key("mangogen", id);

            put(key, createItem(material, Component.text(name + level).color(color).decorate(TextDecoration.BOLD))
                    .lore(lore)
                    .set(DataComponents.CAN_BREAK, new BlockPredicates(new BlockPredicate(blockArray)))
                    .set(DataComponents.TOOL, new Tool(List.of(new Tool.Rule(RegistryTag.direct(blockArray), mineSpeed, true)), mineSpeed, 1, true))
                    .build().withTag(ITEM_ID, key));
        }
        
    {
        // Uncompressed Mats
        createMatItem("oak_planks", Material.OAK_PLANKS, "Oak Planks", OAK_COLOR);
        createMatItem("birch_planks", Material.BIRCH_PLANKS, "Birch Planks", BIRCH_COLOR);
        createMatItem("spruce_planks", Material.SPRUCE_PLANKS, "Spruce Planks", SPRUCE_COLOR);
        createMatItem("jungle_planks", Material.JUNGLE_PLANKS, "Jungle Planks", JUNGLE_COLOR);
        createMatItem("cobblestone", Material.COBBLESTONE, "Cobblestone", STONE_COLOR);
        createMatItem("raw_iron", Material.RAW_IRON, "Raw Iron", IRON_COLOR);
        createMatItem("raw_diamond", Material.LAPIS_LAZULI, "Raw Diamond", DIAMOND_COLOR);
        createMatItem("gold_nugget", Material.GOLD_NUGGET, "Gold Nugget", GOLD_COLOR);
        createMatItem("netherite_scrap", Material.NETHERITE_SCRAP, "Netherite Scrap", NETHERITE_COLOR);
        createMatItem("wheat", Material.WHEAT, "Wheat", WHEAT_COLOR);
        createMatItem("rose", Material.POPPY, "Rose", ROSE_COLOR);
        createMatItem("sweet_berries", Material.SWEET_BERRIES, "Sweet Berries", BERRY_COLOR);
        createMatItem("potato", Material.POTATO, "Potato", POTATO_COLOR);

        // Compressed Mats
        createMatItem("oak_log", Material.OAK_LOG, "Oak Log", OAK_COLOR);
        createMatItem("birch_log", Material.BIRCH_LOG, "Birch Log", BIRCH_COLOR);
        createMatItem("spruce_log", Material.SPRUCE_LOG, "Spruce Log", SPRUCE_COLOR);
        createMatItem("jungle_log", Material.JUNGLE_LOG, "Jungle Log", JUNGLE_COLOR);
        createMatItem("smooth_stone", Material.SMOOTH_STONE, "Smooth Stone", STONE_COLOR);
        createMatItem("iron", Material.IRON_INGOT, "Iron", IRON_COLOR);
        createMatItem("diamond", Material.DIAMOND, "Diamond", DIAMOND_COLOR);
        createMatItem("netherite", Material.NETHERITE_INGOT, "Netherite", NETHERITE_COLOR);
        createMatItem("hay_bale", Material.HAY_BLOCK, "Hay Bale", WHEAT_COLOR);
        createMatItem("rose_bouquet", Material.ROSE_BUSH, "Rose Bouquet", ROSE_COLOR);
        createMatItem("sweet_berry_jam", Material.RED_GLAZED_TERRACOTTA, "Sweet Berry Jam", BERRY_COLOR);
        createMatItem("baked_potato", Material.BAKED_POTATO, "Baked Potato", POTATO_COLOR);

        // Multitool
        put(Key.key("mangogen:multitool"), createItem(Material.TRIPWIRE_HOOK, Component.text("Multitool").color(NamedTextColor.WHITE))
                .set(DataComponents.CAN_BREAK, new BlockPredicates(new BlockPredicate(Block.OAK_PLANKS, Block.STONE, Block.WHEAT)))
                .set(DataComponents.TOOL, new Tool(List.of(
                        new Tool.Rule(RegistryTag.direct(Block.OAK_PLANKS), 0.5f, true),
                        new Tool.Rule(RegistryTag.direct(Block.STONE), 0.5f, true),
                        new Tool.Rule(RegistryTag.direct(Block.WHEAT), 0.5f, true)
                ), 1f, 1, true))
                .lore(Component.text("Can break ").color(NamedTextColor.GRAY)
                        .append(OAK_NAME).append(Component.text(", ").color(NamedTextColor.GRAY))
                        .append(STONE_NAME).append(Component.text(" and ").color(NamedTextColor.GRAY))
                        .append(WHEAT_NAME))
                .build().withTag(ITEM_ID, Key.key("mangogen:multitool")));

        // Axes
        createToolItems("oak_axe", Material.WOODEN_AXE, "Oak Axe ", OAK_COLOR, 1f, 1, WOOD_NAMES, WOOD_BLOCKS);
        createToolItems("birch_axe", Material.STONE_AXE, "Birch Axe ", STONE_COLOR, 2.2f, 2, WOOD_NAMES, WOOD_BLOCKS);
        createToolItems("spruce_axe", Material.IRON_AXE, "Spruce Axe ", IRON_COLOR, 4.84f, 3, WOOD_NAMES, WOOD_BLOCKS);
        createToolItems("jungle_axe", Material.DIAMOND_AXE, "Jungle Axe ", DIAMOND_COLOR, 10.648f, 4, WOOD_NAMES, WOOD_BLOCKS);

        // Pickaxes
        createToolItems("stone_pickaxe", Material.STONE_PICKAXE, "Stone Pickaxe ", STONE_COLOR, 1f, 1, STONE_NAMES, STONE_BLOCKS);
        createToolItems("iron_pickaxe", Material.IRON_PICKAXE, "Iron Pickaxe ", IRON_COLOR, 1f, 2, STONE_NAMES, STONE_BLOCKS);
        createToolItems("diamond_pickaxe", Material.DIAMOND_PICKAXE, "Diamond Pickaxe ", DIAMOND_COLOR, 1f, 3, STONE_NAMES, STONE_BLOCKS);
        createToolItems("netherite_pickaxe", Material.NETHERITE_PICKAXE, "Netherite Pickaxe ", NETHERITE_COLOR, 1f, 5, STONE_NAMES, STONE_BLOCKS); // break limit is higher bc of gold + ancient debris

        // Hoes
        createToolItems("wheat_hoe", Material.WOODEN_HOE, "Wheat Hoe ", WHEAT_COLOR, 1f, 1, PLANT_NAMES, PLANT_BLOCKS);
        createToolItems("rose_hoe", Material.STONE_HOE, "Rose Hoe ", ROSE_COLOR, 1f, 2, PLANT_NAMES, PLANT_BLOCKS);
        createToolItems("berry_hoe", Material.IRON_HOE, "Berry Hoe ", BERRY_COLOR, 1f, 3, PLANT_NAMES, PLANT_BLOCKS);
        createToolItems("potato_hoe", Material.DIAMOND_HOE, "Potato Hoe ", POTATO_COLOR, 1f, 4, PLANT_NAMES, PLANT_BLOCKS);
    }};

    private static final Map<Integer, GenBlockDrop> breakableStateIds = new HashMap<>(13) {
        private void createGenBlock(Block block, ItemDrop itemDrop) {
            put(block.stateId(), new GenBlockDrop(block, itemDrop, 10));
        }

        private void createGenBlock(Block block, String itemDrop) {
            createGenBlock(block, new ItemDrop(getItem(itemDrop)));
        }

    {
        createGenBlock(Block.OAK_PLANKS, "oak_planks");
        createGenBlock(Block.OAK_PLANKS, "oak_planks");
        createGenBlock(Block.BIRCH_PLANKS, "birch_planks");
        createGenBlock(Block.SPRUCE_PLANKS, "spruce_planks");
        createGenBlock(Block.JUNGLE_PLANKS, "jungle_planks");
        createGenBlock(Block.STONE, "cobblestone");
        createGenBlock(Block.IRON_ORE, "raw_iron");
        createGenBlock(Block.DEEPSLATE_DIAMOND_ORE, "raw_diamond");
        createGenBlock(Block.NETHER_GOLD_ORE, "gold_nugget");
        createGenBlock(Block.ANCIENT_DEBRIS, "netherite_scrap");
        createGenBlock(Block.WHEAT.withProperty("age", "7"), "wheat");
        createGenBlock(Block.ROSE_BUSH, "rose");
        createGenBlock(Block.SWEET_BERRY_BUSH.withProperty("age", "3"), "sweet_berries");
        createGenBlock(Block.POTATOES.withProperty("age", "7"), "potato");
    }};

    // Spawns
    private static final DoubleList spawns = DoubleList.of(
            -10d, -10d, 9d, 9d, // Main Spawn
            -12d, -38d, -10d, -30d, // Oak Shop
            -21, -18, -12d, -16d,    // Wheat Shop
            21, 8, 29, 11           // Stone Shop
    );

    @Override
    public @Nullable GenBlockDrop getBlockDrop(BlockVec position, Block block) {
        return breakableStateIds.get(block.stateId());
    }

    @Override
    public Set<Key> getItemIds() {
        return ITEMS.keySet();
    }

    private static @Nullable ItemStack getItem(@KeyPattern.Value final @NotNull String name) {
        return ITEMS.get(Key.key("mangogen", name));
    }

    @Override
    public @Nullable ItemStack getItem(Key key) {
        if (!key.namespace().equals("mangogen")) {
            return null;
        }

        return getItem(key.value());
    }

    @Override
    public @Nullable Vec isInSpawn(Player player, Pos newPosition) {
        BoundingBox box = player.getBoundingBox();

        for (int i = 0; i < spawns.size(); i += 4) {
            double x1 = spawns.getDouble(i);
            double z1 = spawns.getDouble(i + 1);
            double x2 = spawns.getDouble(i + 2) + 1;
            double z2 = spawns.getDouble(i + 3) + 1;

            if (newPosition.x() + box.maxX() > x1 && newPosition.z() + box.maxZ() > z1 &&
                    newPosition.x() + box.minX() < x2 && newPosition.z() + box.minZ() < z2) {
                return new Vec(0);
            }
        }

        return null;
    }

    @Override
    public void saveInventory(Player player) {
        PlayerInventory inv = player.getInventory();
        Inventory enderInv = EnderChestFeature.getInventory(player);

        int capacity = inv.getSize() + enderInv.getSize();
        List<@Nullable String> slots = new ArrayList<>(capacity);
        List<Integer> counts = new ArrayList<>(capacity);

        for (ItemStack stack : inv.getItemStacks()) {
            slots.add(getSlot(stack));
            counts.add(stack.amount());
        }

        for (ItemStack stack : enderInv.getItemStacks()) {
            slots.add(getSlot(stack));
            counts.add(stack.amount());
        }

        player.sendMessage(String.join(", ", slots));

        saveManager.save(player, new MangoGenRegistrySave.InventorySave(slots, counts));
    }

    private static String getSlot(ItemStack stack) {
        Key id = stack.getTag(ITEM_ID);
        if (id != null) {
            return id.asString();
        }

        Material mat = stack.material();
        if (mat != Material.AIR) {
            return mat.key().asString();
        }

        return null;
    }

    @Override
    public void loadPlayerSave(Player player) {
        MangoGenRegistrySave.InventorySave save = saveManager.load(player);
        PlayerInventory inv = player.getInventory();

        if (save == null) {
            inv.addItemStack(getItem("multitool"));
            return;
        }

        Inventory enderInv = EnderChestFeature.getInventory(player);
        AtomicBoolean foundMultitool = new AtomicBoolean(false);

        int i = 0;
        for (; i < inv.getSize(); i++) {
            ItemStack item = getItemFromSave(save.slots().get(i), save.counts().get(i), foundMultitool);
            if (item != null) {
                inv.setItemStack(i, item);
            }
        }

        for (int j = 0; j < enderInv.getSize(); i++, j++) {
            ItemStack item = getItemFromSave(save.slots().get(i), save.counts().get(i), foundMultitool);
            if (item != null) {
                enderInv.setItemStack(j, item);
            }
        }

        if (!foundMultitool.get()) {
            inv.addItemStack(getItem("multitool"));
        }
    }

    private static @Nullable ItemStack getItemFromSave(String saveId, int count, AtomicBoolean foundMultitool) {
        if (saveId == null) {
            return null;
        }

        if (saveId.equals("mangogen:multitool")) {
            foundMultitool.set(true);
        }

        Key key = Key.key(saveId);
        ItemStack item;
        if (key.namespace().equals("mangogen")) {
            item = getItem(key.value());
        } else {
            Material mat = Material.fromKey(key);
            item = mat == null ? null : ItemStack.of(mat);
        }

        if (item == null) {
            return null;
        }

        return item.withAmount(count);
    }
}
