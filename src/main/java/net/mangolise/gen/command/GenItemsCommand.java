package net.mangolise.gen.command;

import net.mangolise.gamesdk.features.commands.MangoliseCommand;
import net.mangolise.gen.GenGame;
import net.mangolise.gen.registry.GenRegistry;
import net.mangolise.gen.registry.MaterialType;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentEnum;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import net.minestom.server.event.inventory.InventoryClickEvent;
import net.minestom.server.inventory.AbstractInventory;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.tag.Tag;

public class GenItemsCommand extends MangoliseCommand {
    private static GenRegistry registry;
    private static final Tag<Integer> INV_TYPE = Tag.Integer("gen_command_inv_type").defaultValue(-1);

    @Override
    protected String getPermission() {
        return "mangolise.command.genitems";
    }

    public GenItemsCommand() {
        super("genitems");
        registry = GenGame.instance.config().registry();

        MinecraftServer.getGlobalEventHandler().addListener(InventoryClickEvent.class, this::onClick);

        addPlayerSyntax(this::execute, ArgumentType.Enum("type", Type.class).setFormat(ArgumentEnum.Format.LOWER_CASED));
    }

    private void execute(Player player, CommandContext context) {
        Inventory inv = new Inventory(InventoryType.CHEST_6_ROW, "Gen Items");
        Type type = context.get("type");
        inv.setTag(INV_TYPE, type.ordinal());

        generateItems(inv, type);

        player.openInventory(inv);
    }

    private static void generateItems(AbstractInventory inv, Type type) {
        inv.clear();

        switch (type) {
            case TOOLS -> {
                for (MaterialType matType : MaterialType.values()) {
                    for (int level = 0; level < 4; level++) {
                        for (int tier = 0; tier < registry.getToolTierCount(matType); tier++) {
                            int slot = level + tier * 4 + (tier / 2) + matType.id * 18;
                            inv.setItemStack(slot, registry.getTool(matType, tier, level));
                        }
                    }
                }
            }

            case UNCOMPRESSED, COMPRESSED -> {
                boolean compressed = type == Type.COMPRESSED;

                for (int i = 0; i < registry.getIngredientTierCount(MaterialType.WOOD, compressed); i++) {
                    inv.addItemStack(registry.getIngredient(MaterialType.WOOD, i, compressed).withAmount(64));
                }

                for (int i = 0; i < registry.getIngredientTierCount(MaterialType.STONE, compressed); i++) {
                    inv.addItemStack(registry.getIngredient(MaterialType.STONE, i, compressed).withAmount(64));
                }

                for (int i = 0; i < registry.getIngredientTierCount(MaterialType.PLANTS, compressed); i++) {
                    inv.addItemStack(registry.getIngredient(MaterialType.PLANTS, i, compressed).withAmount(64));
                }
            }
        }
    }

    private void onClick(InventoryClickEvent e) {
        AbstractInventory inv = e.getPlayer().getOpenInventory();
        if (inv == null) {
            return;
        }

        int typeId = inv.getTag(INV_TYPE);
        if (typeId == -1) {
            return;
        }

        Type type = Type.values()[typeId];

        MinecraftServer.getSchedulerManager().scheduleNextTick(() -> generateItems(inv, type));
    }

    private enum Type {
        TOOLS,
        UNCOMPRESSED,
        COMPRESSED
    }
}
