package net.mangolise.gen.registry;

import net.minestom.server.instance.block.Block;

public record GenBlockDrop(Block respawnBlock, ItemDrop drop, int respawnTime) { }
