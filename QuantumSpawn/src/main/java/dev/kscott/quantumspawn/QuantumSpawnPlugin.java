package dev.kscott.quantumspawn;

import com.google.inject.Guice;
import com.google.inject.Injector;
import dev.kscott.quantumspawn.command.DataCommand;
import dev.kscott.quantumspawn.config.Config;
import dev.kscott.quantumspawn.inject.CommandModule;
import dev.kscott.quantumspawn.inject.ConfigModule;
import dev.kscott.quantumspawn.inject.PluginModule;
import dev.kscott.quantumspawn.inject.QuantumModule;
import dev.kscott.quantumspawn.listeners.PlayerDeathListener;
import dev.kscott.quantumspawn.listeners.PlayerJoinListener;
import dev.kscott.quantumspawn.utils.MySQLProcessor;
import dev.kscott.quantumspawn.utils.SqlProcessor;
import dev.kscott.quantumspawn.utils.SqliteProcessor;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Objects;

/**
 * The QuantumSpawnPlugin.
 */
public final class QuantumSpawnPlugin extends JavaPlugin {

    static Config config;

    public static JavaPlugin getPlugin() {
        return JavaPlugin.getPlugin(QuantumSpawnPlugin.class);
    }

    public static Config getSpawnConfig() { return config; }

    @Override
    public void onEnable() {
        final @NonNull Injector injector = Guice.createInjector(
                new PluginModule(this),
                new QuantumModule(this),
                new ConfigModule(),
                new CommandModule(this)
        );

        config = Objects.requireNonNull(loadConfig(injector));

        injector.getInstance(DataCommand.class);

        if (config.isSpawnOnFirstJoinEnabled()) {
            this.getServer().getPluginManager().registerEvents(injector.getInstance(PlayerJoinListener.class), this);
            Bukkit.getLogger().info("[QuantumSpawn X] Random spawn on first join is enabled!");
        }

        if (config.isSpawnOnDeathEnabled()) {
            this.getServer().getPluginManager().registerEvents(injector.getInstance(PlayerDeathListener.class), this);
            Bukkit.getLogger().info("[QuantumSpawn X] Random spawn on death is enabled!");
        }

        switch (config.getDBTYPE()) {
            case "sqlite" :
                SqlProcessor sqliteProcessor = new SqliteProcessor();
                sqliteProcessor.setSqlConnectionPoll();
                sqliteProcessor.checkDatabase();
                Bukkit.getLogger().info("[QuantumSpawn X] Setup Sqlite data processor");
                break;
            case "mysql" :
                SqlProcessor MySQLProcess = new MySQLProcessor();
                MySQLProcess.setSqlConnectionPoll();
                MySQLProcess.checkDatabase();
                Bukkit.getLogger().info("[QuantumSpawn X] Setup MySQL data processor");
                break;
            case "luckperms" :
            default :
                Bukkit.getLogger().info("[QuantumSpawn X] Setup LuckPerms data processor");
                break;
        }


        new Metrics(this, 9727);
    }

    private @NonNull Config loadConfig(final @NonNull Injector injector) {
        return injector.getInstance(Config.class);
    }
}
