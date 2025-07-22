package net.mangolise.gen.features;

import net.mangolise.gamesdk.Game;
import net.mangolise.gen.GenGame;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.*;
import net.minestom.server.event.player.PlayerEntityInteractEvent;
import net.minestom.server.inventory.type.VillagerInventory;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.server.play.*;

public class GenShopFeature implements Game.Feature<GenGame> {
    public void createShopMenu(Player player) {
        VillagerInventory inv = new VillagerInventory("Tools");
        inv.addTrade(new TradeListPacket.Trade(ItemStack.of(Material.WHEAT, 1), ItemStack.of(Material.DIAMOND, 32),
                ItemStack.AIR, false, 0, 1, 0, 0, 1, 0));
        player.openInventory(inv);
    }

    @Override
    public void setup(Context<GenGame> context) {
        Entity entityShop = new Entity(EntityType.VILLAGER);
        entityShop.setInstance(context.game().instance, new Pos(0, 64, 0));

        context.eventNode().addListener(PlayerEntityInteractEvent.class, e -> {
            if (e.getTarget() != entityShop) {
                return;
            }

            createShopMenu(e.getPlayer());
        });
    }
}
