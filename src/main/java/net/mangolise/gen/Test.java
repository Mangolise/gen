package net.mangolise.gen;

import net.mangolise.gamesdk.util.GameSdkUtils;
import net.mangolise.gen.defaults.HashMapMangoGenRegistrySave;
import net.mangolise.gen.defaults.MangoGenRegistry;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.extras.bungee.BungeeCordProxy;

import java.util.UUID;

// This is a dev server, not used in production
public class Test {
    public static void main(String[] args) {
        MinecraftServer server = MinecraftServer.init();

        if (GameSdkUtils.useBungeeCord()) {
            BungeeCordProxy.enable();
        }

        // give every permission to every player0
        MinecraftServer.getGlobalEventHandler().addListener(AsyncPlayerConfigurationEvent.class, e -> {
            //e.getPlayer().addPermission(new Permission("*"));
        });

        GenGame.Config config = new GenGame.Config(new MangoGenRegistry(new HashMapMangoGenRegistrySave()));
        GenGame game = new GenGame(config, GameSdkUtils::createFakeUUID);
        game.setup();

        server.start("0.0.0.0", GameSdkUtils.getConfiguredPort());
    }
}
