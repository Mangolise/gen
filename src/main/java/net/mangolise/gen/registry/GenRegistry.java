package net.mangolise.gen.registry;

import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public interface GenRegistry {
    /**
     * Gets information about a block in the world
     * @param position the position of the block
     * @param block the block in question
     * @return A GenBlock if it can be broken, or null if it cannot be
     */
    @Nullable GenBlockDrop getBlockDrop(BlockVec position, Block block);

    /**
     * @return All item Ids
     */
    Set<Key> getItemIds();

    /**
     * @param key Key of the item to get
     * @return The item, null if there is no item with the given key
     */
    @Nullable ItemStack getItem(Key key);

    /**
     * @param player Who is moving
     * @param newPosition Where they moved to
     * @return The new spawn point of the player if they are in a spawn, null if they are not in a spawn
     */
    @Nullable Vec isInSpawn(Player player, Pos newPosition);

    void saveInventory(Player player);
    void loadPlayerSave(Player player);
}
