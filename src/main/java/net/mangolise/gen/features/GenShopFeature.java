package net.mangolise.gen.features;

import net.mangolise.gamesdk.Game;
import net.mangolise.gen.GenGame;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.event.inventory.InventoryItemChangeEvent;
import net.minestom.server.event.player.PlayerEntityInteractEvent;
import net.minestom.server.inventory.type.VillagerInventory;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.client.play.ClientSelectTradePacket;
import net.minestom.server.network.packet.server.play.TradeListPacket;

public class GenShopFeature implements Game.Feature<Game> {
    public void createShopMenu(Player player) {
        VillagerInventory inv = new VillagerInventory("Tools");
        inv.addTrade(new TradeListPacket.Trade(ItemStack.of(Material.WHEAT, 1), ItemStack.of(Material.DIAMOND, 32),
                ItemStack.AIR, false, 0, 1, 0, 0, 1, 0));
        player.openInventory(inv);
    }

    @Override
    public void setup(Context<Game> context) {
        Entity entityShop = new Entity(EntityType.VILLAGER);
        entityShop.setInstance(GenGame.instance.instanceWorld, new Pos(0, 64, 0));

        MinecraftServer.getGlobalEventHandler().addListener(PlayerEntityInteractEvent.class, e -> {
            if (e.getTarget() != entityShop) {
                return;
            }
            createShopMenu(e.getPlayer());
        });
    }
}
