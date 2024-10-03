package net.mangolise.gen.registry;

import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import java.util.Random;

public record ItemDrop(ItemStack item, int minimum, int maximum) {
    public ItemDrop(ItemStack item, int minimum, int maximum) {
        this.item = item;
        this.minimum = minimum;
        this.maximum = maximum + 1;
    }

    public ItemDrop(Material item, int minimum, int maximum) {
        this(ItemStack.of(item), minimum, maximum);
    }

    public ItemDrop(ItemStack item, int amount) {
        this(item, amount, amount);
    }

    public ItemDrop(Material item, int amount) {
        this(ItemStack.of(item), amount, amount);
    }

    public ItemDrop(ItemStack item) {
        this(item, 1, 1);
    }

    public ItemDrop(Material item) {
        this(ItemStack.of(item), 1, 1);
    }

    public int maximum() {
        return maximum - 1;
    }

    public ItemStack generateDrop(Random random) {
        return item.withAmount(random.nextInt(minimum, maximum));
    }
}
