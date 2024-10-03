package net.mangolise.gen.defaults;

import net.minestom.server.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HashMapMangoGenRegistrySave implements MangoGenRegistrySave {
    private final Map<UUID, InventorySave> saves = new HashMap<>();

    @Override
    public void save(Player player, InventorySave save) {
        saves.put(player.getUuid(), save);
    }

    @Override
    public InventorySave load(Player player) {
        return saves.get(player.getUuid());
    }
}
