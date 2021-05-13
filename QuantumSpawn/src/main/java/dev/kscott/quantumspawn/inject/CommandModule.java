package dev.kscott.quantumspawn.inject;

import cloud.commandframework.CommandManager;
import cloud.commandframework.bukkit.CloudBukkitCapabilities;
import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator;
import cloud.commandframework.paper.PaperCommandManager;
import cloud.commandframework.tasks.TaskRecipe;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import dev.kscott.quantumspawn.QuantumSpawnPlugin;
import dev.kscott.quantumspawn.config.Config;
import dev.kscott.quantumspawn.utils.DataBaseProcessor;
import dev.kscott.quantumspawn.utils.LuckPermsProcessor;
import dev.kscott.quantumspawn.utils.MySQLProcessor;
import dev.kscott.quantumspawn.utils.SqliteProcessor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.function.Function;

/**
 * Constructs & provides the cloud CommandManager
 */
public class CommandModule extends AbstractModule {

    private final@MonotonicNonNull PaperCommandManager<CommandSender> commandManager;

    private final @MonotonicNonNull DataBaseProcessor dataBaseProcessor;

    /**
     * Constructs the CommandModule
     *
     * @param plugin Plugin reference
     */
    public CommandModule(final @NonNull Plugin plugin) {
        try {
            final @NonNull Function<CommandSender, CommandSender> mapper = Function.identity();

            commandManager = new PaperCommandManager<>(
                    plugin,
                    AsynchronousCommandExecutionCoordinator.simpleCoordinator(),
                    mapper,
                    mapper
            );

            if (commandManager.queryCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
                commandManager.registerAsynchronousCompletions();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize the CommandManager");
        }

        @MonotonicNonNull Config config = QuantumSpawnPlugin.getSpawnConfig();

        switch (config.getDBTYPE()) {
            case "sqlite" :
                dataBaseProcessor = new SqliteProcessor();
                break;
            case "mysql" :
                dataBaseProcessor = new MySQLProcessor();
                break;
            case "luckperms" :
            default :
                dataBaseProcessor = new LuckPermsProcessor();
                break;
        }
    }

    @Provides
    @Singleton
    public CommandManager<CommandSender> provideCommandManager() {
        return this.commandManager;
    }

    @Provides
    @Singleton
    public PaperCommandManager<CommandSender> providePaperCommandManager() {
        return this.commandManager;
    }

    @Provides
    public TaskRecipe provideTaskRecipe() {
        return this.commandManager.taskRecipe();
    }

    @Provides
    @Singleton
    public DataBaseProcessor provideDataBaseProcessor() {
        return this.dataBaseProcessor;
    }

}
