package dev.kscott.quantumspawn.command;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.Command;
import cloud.commandframework.CommandManager;
import cloud.commandframework.bukkit.parsers.PlayerArgument;
import cloud.commandframework.context.CommandContext;
import com.google.inject.Inject;
import dev.kscott.quantumspawn.utils.DataProcessor;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

public class DataCommand {

    private final JavaPlugin plugin;
    private final DataProcessor dataProcessor;
    private final CommandManager<CommandSender> commandManager;
    private final BukkitAudiences bukkitAudiences;
    private final Component PREFIX = MiniMessage.get().parse("<gray>[<color:#5bde9f>QuantumSpawn X</color:#5bde9f>]<gray>");

    @Inject
    DataCommand(
            @NotNull JavaPlugin plugin,
            @NotNull DataProcessor dataProcessor,
            @NotNull CommandManager<CommandSender> commandManager,
            @NonNull BukkitAudiences bukkitAudiences) {

        this.plugin = plugin;
        this.dataProcessor = dataProcessor;
        this.commandManager = commandManager;
        this.bukkitAudiences = bukkitAudiences;

        setupCommands();
    }

    public void setupCommands() {
        final Command.Builder<CommandSender> builder = this.commandManager.commandBuilder("quantumspawn", "qsp");

        this.commandManager.command(
                builder.handler(this::handleMain)
        );

        this.commandManager.command(
                builder.literal(
                        "setlocation",
                        ArgumentDescription.of("set respawn location for player")
                )
                        .permission("quantumspawn.location.set")
                        .argument(PlayerArgument.of("target"))
                        .handler(this::handleSet)
        );

        this.commandManager.command(
                builder.literal(
                        "clearlocation",
                        ArgumentDescription.of("clear respawn location for player")
                )
                        .permission("quantumspawn.location.clear")
                        .argument(PlayerArgument.of("target"))
                        .handler(this::handleClear)
        );
    }

    private void handleMain(final @NonNull CommandContext<CommandSender> context) {
        final @NonNull CommandSender sender = context.getSender();

        final @NonNull String version = this.plugin.getDescription().getVersion();

        final TextComponent.Builder component = Component.text()
                .append(this.PREFIX)
                .append(MiniMessage.get().parse(" <gray>QuantumSpawn X v<aqua>" + version + "</aqua></gray>"))
                .append(MiniMessage.get().parse(" <gray>Subcommand: <aqua>setloc clearloc</aqua>!</gray>"));

        bukkitAudiences.sender(sender).sendMessage(component);
    }

    private void handleSet(final @NonNull CommandContext<CommandSender> context) {
        if (!(checkSender(context))) {
            return;
        }

        Player sender = (Player) context.getSender();
        Player target = context.get("target");
        Location senderloc = sender.getLocation();
        int x = Integer.parseInt(String.valueOf(Math.round(senderloc.getX())));
        int z = Integer.parseInt(String.valueOf(Math.round(senderloc.getZ())));
        dataProcessor.setData(target, x, z);
        final TextComponent.Builder component = Component.text()
                .append(this.PREFIX)
                .append(MiniMessage.get().parse(
                        " <gray>Set Respawn Location for player</gray> "
                                + target.getName()
                                + ": {X:" + x
                                + ", Z:" + z
                                + "}"
                ));
        this.bukkitAudiences.sender(sender).sendMessage(component);

    }

    private void handleClear(final @NonNull CommandContext<CommandSender> context) {
        if (!(checkSender(context))) {
            return;
        }

        Player target = context.get("target");
        dataProcessor.clearData(target);

        final TextComponent.Builder component = Component.text()
                .append(this.PREFIX)
                .append(MiniMessage.get().parse(
                        " <gray>Cleared Respawn Location for player</gray> "
                                + target.getName()
                ));
        this.bukkitAudiences.sender(context.getSender()).sendMessage(component);
    }

    private boolean checkSender(final @NonNull CommandContext<CommandSender> context) {
        CommandSender commandsender = context.getSender();
        if (commandsender instanceof Player) {
            return true;
        }
        final TextComponent.Builder component = Component.text()
                .append(this.PREFIX)
                .append(MiniMessage.get().parse(" <red>Only players can execute this command.</red>"));
        this.bukkitAudiences.sender(commandsender).sendMessage(component);
        return false;
    }
}