package net.mangolise.gen.defaults;

import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.mangolise.gamesdk.features.EnderChestFeature;
import net.mangolise.gamesdk.log.Log;
import net.mangolise.gen.registry.GenBlock;
import net.mangolise.gen.registry.GenRegistry;
import net.mangolise.gen.registry.ItemDrop;
import net.mangolise.gen.registry.MaterialType;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.predicate.BlockPredicate;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.BlockPredicates;
import net.minestom.server.item.component.Tool;
import net.minestom.server.network.packet.server.common.TagsPacket;
import net.minestom.server.registry.RegistryTag;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class MangoGenRegistry implements GenRegistry {
    private static final Tag<Integer> ITEM_SAVE_ID = Tag.Integer("item_save_id").defaultValue(0);
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

    // Items
    private static ItemStack.Builder createItem(Material material, Component name) {
        return ItemStack.builder(material)
                .customName(name.decoration(TextDecoration.ITALIC, false))
                .hideExtraTooltip();
    }

    private static ItemStack createMatItem(Material material, String name, TextColor color, int saveId) {
        return createItem(material, Component.text(name).color(color).decorate(TextDecoration.BOLD))
                .build().withTag(ITEM_SAVE_ID, saveId);
    }

    public static final ItemStack LOAD_FAIL_ITEM = createItem(Material.BARRIER, Component.text("Failed to load item").color(NamedTextColor.RED)).build();

    // Materials
    public static final List<ItemStack> UNCOMPRESSED_MATS = List.of(
            createMatItem(Material.OAK_PLANKS, "Oak Planks", OAK_COLOR, 2),
            createMatItem(Material.BIRCH_PLANKS, "Birch Planks", BIRCH_COLOR, 3),
            createMatItem(Material.SPRUCE_PLANKS, "Spruce Planks", SPRUCE_COLOR, 4),
            createMatItem(Material.JUNGLE_PLANKS, "Jungle Planks", JUNGLE_COLOR, 5),
            createMatItem(Material.COBBLESTONE, "Cobblestone", STONE_COLOR, 6),
            createMatItem(Material.RAW_IRON, "Raw Iron", IRON_COLOR, 7),
            createMatItem(Material.LAPIS_LAZULI, "Raw Diamond", DIAMOND_COLOR, 8),
            createMatItem(Material.GOLD_NUGGET, "Gold Nugget", GOLD_COLOR, 9),
            createMatItem(Material.NETHERITE_SCRAP, "Netherite Scrap", NETHERITE_COLOR, 10),
            createMatItem(Material.WHEAT, "Wheat", WHEAT_COLOR, 11),
            createMatItem(Material.POPPY, "Rose", ROSE_COLOR, 12),
            createMatItem(Material.SWEET_BERRIES, "Sweet Berries", BERRY_COLOR, 13),
            createMatItem(Material.POTATO, "Potato", POTATO_COLOR, 14)
    );

    public static final List<ItemStack> COMPRESSED_MATS = List.of(
            createMatItem(Material.OAK_LOG, "Oak Log", OAK_COLOR, 32),
            createMatItem(Material.BIRCH_LOG, "Birch Log", BIRCH_COLOR, 33),
            createMatItem(Material.SPRUCE_LOG, "Spruce Log", SPRUCE_COLOR, 34),
            createMatItem(Material.JUNGLE_LOG, "Jungle Log", JUNGLE_COLOR, 35),
            createMatItem(Material.SMOOTH_STONE, "Smooth Stone", STONE_COLOR, 36),
            createMatItem(Material.IRON_INGOT, "Iron", IRON_COLOR, 37),
            createMatItem(Material.DIAMOND, "Diamond", DIAMOND_COLOR, 38),
            createMatItem(Material.NETHERITE_INGOT, "Netherite", NETHERITE_COLOR, 39),
            createMatItem(Material.HAY_BLOCK, "Hay Bale", WHEAT_COLOR, 40),
            createMatItem(Material.ROSE_BUSH, "Rose Bouquet", ROSE_COLOR, 41),
            createMatItem(Material.RED_GLAZED_TERRACOTTA, "Sweet Berry Jam", BERRY_COLOR, 42),
            createMatItem(Material.BAKED_POTATO, "Baked Potato", POTATO_COLOR, 43)
    );

    @Override
    public ItemStack getIngredient(MaterialType type, int tier, boolean compressed) {
        if (tier < 0 || tier >= getIngredientTierCount(type, compressed)) {
            throw new IndexOutOfBoundsException();
        }

        return switch (type) {
            case WOOD -> (compressed ? COMPRESSED_MATS : UNCOMPRESSED_MATS).get(tier);
            case STONE -> (compressed ? COMPRESSED_MATS : UNCOMPRESSED_MATS).get(tier + 4);
            case PLANTS -> (compressed ? COMPRESSED_MATS : UNCOMPRESSED_MATS).get(tier + (compressed ? 4 : 5) + 4);
        };
    }

    // Blocks
    private static final List<Block> WOOD_BLOCKS = List.of(Block.OAK_PLANKS, Block.BIRCH_PLANKS, Block.SPRUCE_PLANKS, Block.JUNGLE_PLANKS);
    private static final List<Component> WOOD_BLOCK_NAMES = List.of(Component.text("Oak").color(OAK_COLOR), Component.text("Birch").color(BIRCH_COLOR), Component.text("Spruce").color(SPRUCE_COLOR), Component.text("Jungle").color(JUNGLE_COLOR));
    private static final List<Block> STONE_BLOCKS = List.of(Block.STONE, Block.IRON_ORE, Block.DEEPSLATE_DIAMOND_ORE, Block.NETHER_GOLD_ORE, Block.ANCIENT_DEBRIS);
    private static final List<Component> STONE_BLOCK_NAMES = List.of(Component.text("Stone").color(STONE_COLOR), Component.text("Iron").color(IRON_COLOR), Component.text("Diamond").color(DIAMOND_COLOR), Component.text("Gold").color(GOLD_COLOR), Component.text("Ancient Debris", NETHERITE_COLOR));
    private static final List<Block> PLANT_BLOCKS = List.of(Block.WHEAT, Block.ROSE_BUSH, Block.SWEET_BERRY_BUSH, Block.POTATOES);
    private static final List<Component> PLANT_BLOCK_NAMES = List.of(Component.text("Wheat").color(WHEAT_COLOR), Component.text("Roses").color(ROSE_COLOR), Component.text("Sweet Berry").color(BERRY_COLOR), Component.text("Potatoes").color(POTATO_COLOR));

    private static final Map<Integer, GenBlock> breakableStateIds;

    private static void createGenBlock(Block block, ItemDrop itemDrop) {
        breakableStateIds.put(block.stateId(), new GenBlock(block, itemDrop, 10));
    }

    static {
        breakableStateIds = new HashMap<>(13);

        createGenBlock(Block.OAK_PLANKS, new ItemDrop(UNCOMPRESSED_MATS.get(0)));
        createGenBlock(Block.BIRCH_PLANKS, new ItemDrop(UNCOMPRESSED_MATS.get(1)));
        createGenBlock(Block.SPRUCE_PLANKS, new ItemDrop(UNCOMPRESSED_MATS.get(2)));
        createGenBlock(Block.JUNGLE_PLANKS, new ItemDrop(UNCOMPRESSED_MATS.get(3)));
        createGenBlock(Block.STONE, new ItemDrop(UNCOMPRESSED_MATS.get(4)));
        createGenBlock(Block.IRON_ORE, new ItemDrop(UNCOMPRESSED_MATS.get(5)));
        createGenBlock(Block.DEEPSLATE_DIAMOND_ORE, new ItemDrop(UNCOMPRESSED_MATS.get(6)));
        createGenBlock(Block.NETHER_GOLD_ORE, new ItemDrop(UNCOMPRESSED_MATS.get(7), 2, 6));
        createGenBlock(Block.ANCIENT_DEBRIS, new ItemDrop(UNCOMPRESSED_MATS.get(8)));
        createGenBlock(Block.WHEAT.withProperty("age", "7"), new ItemDrop(UNCOMPRESSED_MATS.get(9)));
        createGenBlock(Block.ROSE_BUSH, new ItemDrop(UNCOMPRESSED_MATS.get(10)));
        createGenBlock(Block.SWEET_BERRY_BUSH.withProperty("age", "3"), new ItemDrop(UNCOMPRESSED_MATS.get(11)));
        createGenBlock(Block.POTATOES.withProperty("age", "7"), new ItemDrop(UNCOMPRESSED_MATS.get(12)));
    }

    // Tools
    public static final ItemStack MULTITOOL = createItem(Material.TRIPWIRE_HOOK, Component.text("Multitool").color(NamedTextColor.WHITE))
            .set(DataComponents.CAN_BREAK, new BlockPredicates(new BlockPredicate(Block.OAK_PLANKS, Block.STONE, Block.WHEAT)))
            .set(DataComponents.TOOL, new Tool(List.of(
                    new Tool.Rule(RegistryTag.direct(Block.OAK_PLANKS), 0.5f, true),
                    new Tool.Rule(RegistryTag.direct(Block.STONE), 0.5f, true),
                    new Tool.Rule(RegistryTag.direct(Block.WHEAT), 0.5f, true)
            ), 1f, 1, true))
            .lore(Component.text("Can break ").color(NamedTextColor.GRAY)
                    .append(WOOD_BLOCK_NAMES.getFirst()).append(Component.text(", ").color(NamedTextColor.GRAY))
                    .append(STONE_BLOCK_NAMES.getFirst()).append(Component.text(" and ").color(NamedTextColor.GRAY))
                    .append(PLANT_BLOCK_NAMES.getFirst()))
            .build().withTag(ITEM_SAVE_ID, 1);

    @Override
    public ItemStack getMultitool() {
        return MULTITOOL;
    }

    private static ItemStack createToolItem(Material material, String name, TextColor color, float mineSpeed, int level, int tier, MaterialType type, List<Block> blocks, int limit) {
        Block[] blockArray = blocks.subList(0, limit).toArray(Block[]::new);
        mineSpeed *= level * 0.3f + 1f; // each level give 30% more mining speed

        if (level > 255) {
            throw new IndexOutOfBoundsException("Level must be 255 or less");
        }

        Component lore = Component.text("Can break ").color(NamedTextColor.GRAY);
        for (int i = 0;;) {
            lore = lore.append(switch (type) {
                case WOOD -> WOOD_BLOCK_NAMES.get(i);
                case STONE -> STONE_BLOCK_NAMES.get(i);
                case PLANTS -> PLANT_BLOCK_NAMES.get(i);
            });

            i++;
            if (i >= limit) {
                break;
            }

            if (i + 1 >= limit) {
                lore = lore.append(Component.text(" and ").color(NamedTextColor.GRAY));
            } else {
                lore = lore.append(Component.text(", ").color(NamedTextColor.GRAY));
            }
        }

        return createItem(material, Component.text(name + (level + 1)).color(color).decorate(TextDecoration.BOLD))
                .lore(lore)
                .set(DataComponents.CAN_BREAK, new BlockPredicates(new BlockPredicate(blockArray)))
                .set(DataComponents.TOOL, new Tool(List.of(new Tool.Rule(RegistryTag.direct(blockArray), mineSpeed, true)), mineSpeed, 1, true))
                .build().withTag(ITEM_SAVE_ID, ((level & 0xFF) << 24) | ((tier & 0xF) << 20) | ((type.id & 0x7) << 17) | 65536);
    }

    @Override
    public ItemStack getTool(MaterialType type, int tier, int level) {
        return switch (type) {
            case WOOD -> switch (tier) {
                case 0 -> createToolItem(Material.WOODEN_AXE, "Oak Axe ", OAK_COLOR, 1f, level, tier, MaterialType.WOOD, WOOD_BLOCKS, 1);
                case 1 -> createToolItem(Material.STONE_AXE, "Birch Axe ", BIRCH_COLOR, 2.2f, level, tier, MaterialType.WOOD, WOOD_BLOCKS, 2);
                case 2 -> createToolItem(Material.IRON_AXE, "Spruce Axe ", SPRUCE_COLOR, 4.84f, level, tier, MaterialType.WOOD, WOOD_BLOCKS, 3);
                case 3 -> createToolItem(Material.DIAMOND_AXE, "Jungle Axe ", JUNGLE_COLOR, 10.648f, level, tier, MaterialType.WOOD, WOOD_BLOCKS, 4);
                default -> throw new IndexOutOfBoundsException();
            };

            case STONE -> switch (tier) {
                case 0 -> createToolItem(Material.STONE_PICKAXE, "Stone Pickaxe ", STONE_COLOR, 1f, level, tier, MaterialType.STONE, STONE_BLOCKS, 1);
                case 1 -> createToolItem(Material.IRON_PICKAXE, "Iron Pickaxe ", IRON_COLOR, 1f, level, tier, MaterialType.STONE, STONE_BLOCKS, 2);
                case 2 -> createToolItem(Material.DIAMOND_PICKAXE, "Diamond Pickaxe ", DIAMOND_COLOR, 1f, level, tier, MaterialType.STONE, STONE_BLOCKS, 3);
                case 3 -> createToolItem(Material.NETHERITE_PICKAXE, "Netherite Pickaxe ", NETHERITE_COLOR, 1f, level, tier, MaterialType.STONE, STONE_BLOCKS, 5);
                default -> throw new IndexOutOfBoundsException();
            };

            case PLANTS -> switch (tier) {
                case 0 -> createToolItem(Material.WOODEN_HOE, "Wheat Hoe ", WHEAT_COLOR, 1f, level, tier, MaterialType.PLANTS, PLANT_BLOCKS, 1);
                case 1 -> createToolItem(Material.STONE_HOE, "Rose Hoe ", ROSE_COLOR, 1f, level, tier, MaterialType.PLANTS, PLANT_BLOCKS, 2);
                case 2 -> createToolItem(Material.IRON_HOE, "Berry Hoe ", BERRY_COLOR, 1f, level, tier, MaterialType.PLANTS, PLANT_BLOCKS, 3);
                case 3 -> createToolItem(Material.DIAMOND_HOE, "Potato Hoe ", POTATO_COLOR, 1f, level, tier, MaterialType.PLANTS, PLANT_BLOCKS, 4);
                default -> throw new IndexOutOfBoundsException();
            };
        };
    }

    @Override
    public boolean isBreakableBlock(Block block) {
        return breakableStateIds.containsKey(block.stateId());
    }

    @Override
    public @Nullable GenBlock getItemDrop(Block block) {
        return breakableStateIds.get(block.stateId());
    }

    @Override
    public int getCompressedIngredientCount() {
        return COMPRESSED_MATS.size();
    }

    @Override
    public int getUncompressedIngredientCount() {
        return UNCOMPRESSED_MATS.size();
    }

    @Override
    public int getToolTierCount(MaterialType type) {
        return 4;
    }

    @Override
    public int getIngredientTierCount(MaterialType type, boolean compressed) {
        return type != MaterialType.STONE || compressed ? 4 : 5;
    }

    // Spawns
    private static final DoubleList spawns = DoubleList.of(
            -10d, -10d, 9d, 9d, // Main Spawn
            -12d, -38d, -10d, -30d, // Oak Shop
            -21, -18, -12d, -16d,    // Wheat Shop
            21, 8, 29, 11           // Stone Shop
    );

    @Override
    public boolean isInSpawn(Player player, Pos newPosition) {
        BoundingBox box = player.getBoundingBox();

        for (int i = 0; i < spawns.size(); i += 4) {
            double x1 = spawns.getDouble(i);
            double z1 = spawns.getDouble(i + 1);
            double x2 = spawns.getDouble(i + 2) + 1;
            double z2 = spawns.getDouble(i + 3) + 1;

            if (newPosition.x() + box.maxX() > x1 && newPosition.z() + box.maxZ() > z1 &&
                    newPosition.x() + box.minX() < x2 && newPosition.z() + box.minZ() < z2) {
                return true;
            }
        }

        return false;
    }

    // Saving
    @Override
    public void saveInventory(Player player) {
        PlayerInventory inv = player.getInventory();
        Inventory enderInv = EnderChestFeature.getInventory(player);

        int capacity = inv.getSize() + enderInv.getSize();
        List<Integer> slots = new ArrayList<>(capacity);
        List<Integer> counts = new ArrayList<>(capacity);

        int i = 0;
        for (; i < inv.getSize(); i++) {
            ItemStack stack = inv.getItemStack(i);
            slots.add(stack.getTag(ITEM_SAVE_ID));
            counts.add(stack.amount());
        }

        for (int j = 0; i < capacity; j++, i++) {
            ItemStack stack = enderInv.getItemStack(j);
            slots.add(stack.getTag(ITEM_SAVE_ID));
            counts.add(stack.amount());
        }

        saveManager.save(player, new MangoGenRegistrySave.InventorySave(slots, counts));
    }

    @Override
    public void loadPlayerSave(Player player) {
        MangoGenRegistrySave.InventorySave save = saveManager.load(player);
        PlayerInventory inv = player.getInventory();

        if (save == null) {
            inv.addItemStack(MULTITOOL);
            return;
        }

        Inventory enderInv = EnderChestFeature.getInventory(player);
        boolean foundMultitool = false;

        int i = 0;
        for (; i < inv.getSize(); i++) {
            int saveId = save.slots().get(i);
            if (saveId == 1) {
                foundMultitool = true;
            }

            inv.setItemStack(i, getFromSave(saveId).withAmount(save.counts().get(i)));
        }

        for (int j = 0; j < enderInv.getSize(); i++, j++) {
            int saveId = save.slots().get(i);
            if (saveId == 1) {
                foundMultitool = true;
            }

            enderInv.setItemStack(j, getFromSave(saveId).withAmount(save.counts().get(i)));
        }

        if (!foundMultitool) {
            inv.addItemStack(MULTITOOL);
        }
    }

    private ItemStack getFromSave(int saveId) {
        // if the 16th bit is true, then it's a tool
        if ((saveId & 65536) > 0) {
            int level = (saveId & 0xFF000000) >> 24; // level is the highest byte
            int tier = (saveId & 0xf00000) >> 20; // tier is the next 4 bits
            int type = (saveId & 0x70000) >> 17; // type is next 3 bits

            if (type > MaterialType.values().length) {
                Log.logger().warn("Invalid MaterialType in tool {}", type);
                return LOAD_FAIL_ITEM.withLore(Component.text("tool material fail, Item id: " + saveId));
            }

            return getTool(MaterialType.fromId(type), tier, level);
        }

        if (saveId > 1 && saveId < 15) {
            return UNCOMPRESSED_MATS.get(saveId - 2);
        }

        if (saveId > 31 && saveId < 44) {
            return COMPRESSED_MATS.get(saveId - 32);
        }

        return switch (saveId) {
            case 0 -> ItemStack.AIR;
            case 1 -> MULTITOOL;
            default -> {
                Log.logger().warn("SaveId for item {} is out of bounds", saveId);
                yield LOAD_FAIL_ITEM.withLore(Component.text("normal item fail, Item id: " + saveId));
            }
        };
    }
}
