package net.mangolise.gen;

import net.mangolise.combat.CombatConfig;
import net.mangolise.combat.MangoCombat;
import net.mangolise.combat.events.PlayerAttackEvent;
import net.mangolise.gamesdk.BaseGame;
import net.mangolise.gamesdk.features.*;
import net.mangolise.gamesdk.log.Log;
import net.mangolise.gen.command.GenItemsCommand;
import net.mangolise.gen.features.GenShopFeature;
import net.mangolise.gen.registry.GenRegistry;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.inventory.InventoryItemChangeEvent;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.instance.IChunkLoader;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.player.GameProfile;
import net.minestom.server.registry.RegistryKey;
import net.minestom.server.world.DimensionType;

import java.util.List;
import java.util.UUID;

public class GenGame extends BaseGame<GenGame.Config> {
    public Instance instance;

    private final UuidProvider uuidProvider;

    public GenGame(GenGame.Config config, UuidProvider uuidProvider) {
        super(config);
        this.uuidProvider = uuidProvider;
    }

    @Override
    public void setup() {
        DimensionType dimension = DimensionType.builder().build();
        RegistryKey<DimensionType> dim = MinecraftServer.getDimensionTypeRegistry().register("gen", dimension);

        instance = MinecraftServer.getInstanceManager().createInstanceContainer(dim, config().loader());

        MinecraftServer.getConnectionManager().setPlayerProvider((playerConnection, gameProfile) ->
                new GenPlayer(playerConnection, new GameProfile(uuidProvider.supply(gameProfile.name()), gameProfile.name()), config().registry));

        MangoCombat.enableGlobal(CombatConfig.create().withFakeDeath(false).withAutomaticRespawn(true).withVoidDeath(false));

        MinecraftServer.getCommandManager().register(new GenItemsCommand(config().registry));

        // Player spawning
        GlobalEventHandler events = MinecraftServer.getGlobalEventHandler();
        events.addListener(AsyncPlayerConfigurationEvent.class, e -> {
            e.setSpawningInstance(instance);

            e.getPlayer().setGameMode(GameMode.ADVENTURE);
            e.getPlayer().setRespawnPoint(new Pos(0, 68, 0, 180, 0));
        });

        events.addListener(PlayerSpawnEvent.class, e -> {
            if (e.isFirstSpawn()) {
                ((GenPlayer) e.getPlayer()).onFirstSpawn();
            }
        });

        events.addListener(PlayerAttackEvent.class, e -> {
            GenPlayer victim = (GenPlayer) e.victim();
            GenPlayer attacker = (GenPlayer) e.attacker();

            if (victim.isInSpawn() || attacker.isInSpawn()) {
                e.setCancelled(true);
            }
        });

        events.addListener(InventoryItemChangeEvent.class, e -> {
            for (Player player : e.getInventory().getViewers()) {
                ((GenPlayer) player).onInventoryItemChange(e);
            }
        });

        super.setup();
        Log.logger().info("Started Gen game");
    }

    @Override
    public List<Feature<?>> features() {
        return List.of(
                new AdminCommandsFeature(),
                new ItemPickupFeature(),
                new ItemDropFeature(),
                new EnderChestFeature(),
                new VillagerTradeFeature(),
                new GenShopFeature()
        );
    }

    @FunctionalInterface
    public interface UuidProvider {
        UUID supply(String username);
    }

    public record Config(GenRegistry registry, IChunkLoader loader) { }
}
