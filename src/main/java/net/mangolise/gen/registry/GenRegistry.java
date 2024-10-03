package net.mangolise.gen.registry;

import net.minestom.server.entity.Player;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface GenRegistry {
    boolean isBreakableBlock(Block block);
    @Nullable GenBlock getItemDrop(Block block);

    int getCompressedIngredientCount();
    int getUncompressedIngredientCount();
    int getToolTierCount(MaterialType type);
    int getIngredientTierCount(MaterialType type, boolean compressed);

    ItemStack getIngredient(MaterialType type, int tier, boolean compressed);
    ItemStack getMultitool();
    ItemStack getTool(MaterialType type, int tier, int level);

    void saveInventory(Player player);
    void loadPlayerSave(Player player);
}
