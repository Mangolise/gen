package net.mangolise.gen.defaults;

import net.minestom.server.entity.Player;
import org.jspecify.annotations.Nullable;

import java.util.List;

public interface MangoGenRegistrySave {
    void save(Player player, InventorySave save);
    InventorySave load(Player player);

    record InventorySave(List<@Nullable String> slots, List<Integer> counts) { }
}
