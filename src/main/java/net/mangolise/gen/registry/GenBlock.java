package net.mangolise.gen.registry;

import net.minestom.server.instance.block.Block;

public record GenBlock(Block block, ItemDrop drop, int respawnTime) { }
