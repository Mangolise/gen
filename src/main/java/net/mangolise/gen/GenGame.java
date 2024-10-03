package net.mangolise.gen;

import net.hollowcube.polar.PolarLoader;
import net.mangolise.combat.CombatConfig;
import net.mangolise.combat.MangoCombat;
import net.mangolise.gamesdk.BaseGame;
import net.mangolise.gamesdk.features.AdminCommandsFeature;
import net.mangolise.gamesdk.features.EnderChestFeature;
import net.mangolise.gamesdk.features.ItemDropFeature;
import net.mangolise.gamesdk.features.ItemPickupFeature;
import net.mangolise.gamesdk.log.Log;
import net.mangolise.gamesdk.util.GameSdkUtils;
import net.mangolise.gen.command.GenItemsCommand;
import net.mangolise.gen.registry.GenRegistry;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.world.DimensionType;

import java.util.List;

public class GenGame extends BaseGame<GenGame.Config> {
    public static GenGame instance;

    public GenGame(GenGame.Config config) {
        super(config);
        instance = this;
    }

    @Override
    public void setup() {
        super.setup();

        DimensionType dimension = DimensionType.builder().build();
        DynamicRegistry.Key<DimensionType> dim = MinecraftServer.getDimensionTypeRegistry().register("gen", dimension);

        PolarLoader loader = GameSdkUtils.getPolarLoaderFromResource("worlds/gen.polar");
        Instance instance = MinecraftServer.getInstanceManager().createInstanceContainer(dim, loader);

        MinecraftServer.getConnectionManager().setPlayerProvider(GenPlayer::new);
        MangoCombat.enableGlobal(CombatConfig.create().withFakeDeath(false).withAutomaticRespawn(true).withVoidDeath(false));

        MinecraftServer.getCommandManager().register(new GenItemsCommand());

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

        Log.logger().info("Started Gen game");
    }

    @Override
    public List<Feature<?>> features() {
        return List.of(
                new AdminCommandsFeature(),
                new ItemPickupFeature(),
                new ItemDropFeature(),
                new EnderChestFeature()
        );
    }

    public record Config(GenRegistry registry) { }
}
