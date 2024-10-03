package net.mangolise.gen.defaults;

import net.minestom.server.entity.Player;

import java.util.List;

public interface MangoGenRegistrySave {
    void save(Player player, InventorySave save);
    InventorySave load(Player player);

    record InventorySave(List<Integer> slots, List<Integer> counts) { }
}
