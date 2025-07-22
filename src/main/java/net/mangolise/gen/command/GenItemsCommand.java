package net.mangolise.gen.command;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.mangolise.gamesdk.features.commands.MangoliseCommand;
import net.mangolise.gen.registry.GenRegistry;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class GenItemsCommand extends MangoliseCommand {
    private final GenRegistry registry;
    private final @Nullable String namespace;

    @Override
    protected String getPermission() {
        return "mangolise.command.genitems";
    }

    public GenItemsCommand(GenRegistry registry) {
        super("genitems");
        this.registry = registry;

        Set<Key> itemIds = registry.getItemIds();
        final String[] namespace = {null};

        boolean hasNamespace = itemIds.stream().anyMatch(k -> {
            if (namespace[0] == null) {
                namespace[0] = k.namespace();
                return false;
            }

            return !namespace[0].equals(k.namespace());
        });

        this.namespace = namespace[0];

        if (hasNamespace) {
            addPlayerSyntax(this::execute, ArgumentType.Word("item")
                    .from(registry.getItemIds().stream().map(Key::asString).toArray(String[]::new)));
        } else {
            addPlayerSyntax(this::execute, ArgumentType.Word("item")
                    .from(registry.getItemIds().stream().map(Key::value).toArray(String[]::new)));
        }
    }

    private void execute(Player player, CommandContext context) {
        Key key = Key.key(context.get("item"));
        if (namespace != null && key.namespace().equals(Key.MINECRAFT_NAMESPACE)) {
            key = Key.key(namespace, key.value());
        }

        ItemStack item = registry.getItem(key);

        if (item == null) {
            player.sendMessage("Item " + key.asString() + " not found!");
            return;
        }

        player.getInventory().addItemStack(item);
        player.sendMessage(Component.text("Gave item: ").append(item.get(DataComponents.CUSTOM_NAME, Component.text(key.asString()))));
    }
}
