package net.mangolise.gen;

import net.kyori.adventure.sound.Sound;
import net.mangolise.gamesdk.features.EnderChestFeature;
import net.mangolise.gamesdk.util.GameSdkUtils;
import net.mangolise.gen.registry.GenBlockDrop;
import net.mangolise.gen.registry.GenRegistry;
import net.minestom.server.MinecraftServer;
import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.event.inventory.InventoryItemChangeEvent;
import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.event.player.*;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.inventory.type.VillagerInventory;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.BlockPredicates;
import net.minestom.server.network.packet.server.play.TradeListPacket;
import net.minestom.server.network.player.GameProfile;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.NotNull;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class GenPlayer extends Player {
    private final GenRegistry registry;
    private boolean inventoryChanged = false;
    private boolean stop = false;
    private boolean inSpawn = true;

    public GenPlayer(@NotNull PlayerConnection playerConnection, @NotNull GameProfile gameProfile, GenRegistry registry) {
        super(playerConnection, gameProfile);
        this.registry = registry;
    }

    public void onFirstSpawn() {
        // Give items
        registry.loadPlayerSave(this);

        // Subscribe to events
        eventNode().addListener(PlayerMoveEvent.class, this::onMove);
        eventNode().addListener(ItemDropEvent.class, this::onDrop);
        eventNode().addListener(PlayerBlockBreakEvent.class, this::onBlockBreak);
        eventNode().addListener(PlayerDisconnectEvent.class, e -> {saveInventory(); stop = true;});
        eventNode().addListener(PlayerBlockInteractEvent.class, this::onBlockInteract);

        MinecraftServer.getSchedulerManager().scheduleTask(this::saveInventory, TaskSchedule.immediate());
    }

    public void onInventoryItemChange(InventoryItemChangeEvent e) {
        if (inventory == getInventory()) {
            inventoryChanged = true;
        }

        if (!((e.getInventory() instanceof VillagerInventory inv))) {
            return;
        }

        for (TradeListPacket.Trade trade : inv.getTrades()) {
            if (inv.getItemStack(0).material().equals(trade.inputItem1().material()) && inv.getItemStack(1).material().equals(trade.inputItem2().material())) {
                inv.setItemStack(2, trade.result());
                break;
            }
        }
    }

    private void onMove(PlayerMoveEvent e) {
        if (getGameMode().equals(GameMode.CREATIVE) ||
                getPosition().distanceSquared(e.getNewPosition()) < Vec.EPSILON) {
            return;
        }

        Pos newPos = e.getNewPosition();
        if (newPos.y() < 32) {
            damage(new Damage(DamageType.FALL, null, null, null, 100000));
        }

        inSpawn = registry.isInSpawn(this, newPos) != null;
    }

    private void onDrop(ItemDropEvent e) {
        if (getGameMode().equals(GameMode.CREATIVE)) {
            return;
        }

        if (e.getItemStack().material().equals(Material.TRIPWIRE_HOOK)) {
            e.setCancelled(true);
        }
    }

    private void onBlockBreak(PlayerBlockBreakEvent e) {
        boolean creative = getGameMode().equals(GameMode.CREATIVE);

        Block block = e.getBlock();

        // Test if the item is allowed to break this block in case they are in survival or are hacking
        BlockPredicates canBreak = getItemInMainHand().get(DataComponents.CAN_BREAK);
        if ((canBreak == null || !canBreak.test(block)) && !creative) {
            e.setCancelled(true);
            return;
        }

        GenBlockDrop drop = registry.getBlockDrop(e.getBlockPosition(), block);
        if (drop == null) {
            if (!creative) e.setCancelled(true);
            return;
        }

        Random random = ThreadLocalRandom.current();
        ItemStack item = drop.drop().generateDrop(random);
        if (getInventory().addItemStack(item)) {
            playSound(Sound.sound(SoundEvent.ENTITY_ITEM_PICKUP, Sound.Source.PLAYER, 0.2f, random.nextFloat(0.6f, 2.2f)));
        } else {
            GameSdkUtils.dropItemNaturally(getInstance(), e.getBlockPosition().add(0.5, 0.5, 0.5), item);
        }

        Instance instance = e.getInstance();
        MinecraftServer.getSchedulerManager().scheduleTask(() -> {
            instance.setBlock(e.getBlockPosition(), drop.respawnBlock());
        }, TaskSchedule.seconds(10), TaskSchedule.stop());
    }

    private void onBlockInteract(PlayerBlockInteractEvent e) {
        if (e.getBlock().compare(Block.ENDER_CHEST)) {
            EnderChestFeature.open(this, getInstance(), e.getBlockPosition(), false);
            e.setBlockingItemUse(true);
            return;
        }
    }

    private TaskSchedule saveInventory() {
        if (stop) {
            return TaskSchedule.stop();
        }

        if (inventoryChanged) {
            registry.saveInventory(this);
            inventoryChanged = false;
        }

        return TaskSchedule.minutes(5);
    }

    public boolean isInSpawn() {
        return inSpawn;
    }
}
