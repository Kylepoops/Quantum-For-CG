package dev.kscott.quantumspawn;

import com.google.inject.Guice;
import com.google.inject.Injector;
import dev.kscott.quantumspawn.config.Config;
import dev.kscott.quantumspawn.inject.ConfigModule;
import dev.kscott.quantumspawn.inject.PluginModule;
import dev.kscott.quantumspawn.inject.QuantumModule;
import dev.kscott.quantumspawn.listeners.PlayerDeathListener;
import dev.kscott.quantumspawn.listeners.PlayerJoinListener;
import dev.kscott.quantumspawn.utils.LuckPermsProcessor;
import dev.kscott.quantumspawn.utils.SqliteProcessor;
import net.luckperms.api.LuckPerms;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * The QuantumSpawnPlugin.
 */
public final class QuantumSpawnPlugin extends JavaPlugin {

    static RegisteredServiceProvider<LuckPerms> lpProvider = null;

    static LuckPermsProcessor luckPermsProcessor = null;

    public static JavaPlugin getPlugin() {
        return JavaPlugin.getPlugin(QuantumSpawnPlugin.class);
    }

    public static RegisteredServiceProvider<LuckPerms> getLpProvider() {
        return lpProvider;
    }

    public static LuckPermsProcessor getLuckPermsProcessor() {
        return luckPermsProcessor;
    }

    @Override
    public void onEnable() {
        final @NonNull Injector injector = Guice.createInjector(
                new PluginModule(this),
                new QuantumModule(this),
                new ConfigModule()
        );

        lpProvider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);

        final @NonNull Config config = loadConfig(injector);

        if (config.isSpawnOnFirstJoinEnabled()) {
            this.getServer().getPluginManager().registerEvents(injector.getInstance(PlayerJoinListener.class), this);
            this.getLogger().info("Random spawn on first join is enabled!");
        }

        if (config.isSpawnOnDeathEnabled()) {
            this.getServer().getPluginManager().registerEvents(injector.getInstance(PlayerDeathListener.class), this);
            this.getLogger().info("Random spawn on death is enabled!");
        }

        SqliteProcessor.setSqlConnectionPoll();


        new Metrics(this, 9727);
    }

    private @NonNull Config loadConfig(final @NonNull Injector injector) {
        return injector.getInstance(Config.class);
    }
}
