package dev.kscott.quantumspawn;

import com.google.inject.Guice;
import com.google.inject.Injector;
import dev.kscott.quantumspawn.config.Config;
import dev.kscott.quantumspawn.inject.ConfigModule;
import dev.kscott.quantumspawn.inject.PluginModule;
import dev.kscott.quantumspawn.inject.QuantumModule;
import dev.kscott.quantumspawn.listeners.PlayerDeathListener;
import dev.kscott.quantumspawn.listeners.PlayerJoinListener;
import dev.kscott.quantumspawn.utils.MySQLProcessor;
import dev.kscott.quantumspawn.utils.SqlProcessor;
import dev.kscott.quantumspawn.utils.SqliteProcessor;
import org.bstats.bukkit.Metrics;
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
                new ConfigModule()
        );

        config = Objects.requireNonNull(loadConfig(injector));

        if (config.isSpawnOnFirstJoinEnabled()) {
            this.getServer().getPluginManager().registerEvents(injector.getInstance(PlayerJoinListener.class), this);
            this.getLogger().info("Random spawn on first join is enabled!");
        }

        if (config.isSpawnOnDeathEnabled()) {
            this.getServer().getPluginManager().registerEvents(injector.getInstance(PlayerDeathListener.class), this);
            this.getLogger().info("Random spawn on death is enabled!");
        }

        if (config.getDBTYPE().equals("sqlite")) {
            SqlProcessor sqliteProcessor = new SqliteProcessor();
            sqliteProcessor.setSqlConnectionPoll();
            sqliteProcessor.checkDatabase();
        } else if (config.getDBTYPE().equals("mysql")) {
            SqlProcessor MySQLProcess = new MySQLProcessor();
            MySQLProcess.setSqlConnectionPoll();
            MySQLProcess.checkDatabase();
        }


        new Metrics(this, 9727);
    }

    private @NonNull Config loadConfig(final @NonNull Injector injector) {
        return injector.getInstance(Config.class);
    }
}
