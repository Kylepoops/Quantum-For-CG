package dev.kscott.quantumwild;

import com.google.inject.Guice;
import com.google.inject.Injector;
import dev.kscott.quantumwild.command.WildCommand;
import dev.kscott.quantumwild.config.Config;
import dev.kscott.quantumwild.listeners.PlayerMovementListener;
import dev.kscott.quantumwild.module.*;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class QuantumWildPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        final @NonNull Injector injector = Guice.createInjector(
                new QuantumModule(this),
                new PluginModule(this),
                new CommandModule(this),
                new ConfigModule(),
                new IntegrationsModule(),
                new WildModule()
        );

        injector.getInstance(Config.class);

        injector.getInstance(WildCommand.class);

        this.getServer().getPluginManager().registerEvents(injector.getInstance(PlayerMovementListener.class), this);

        new Metrics(this, 9726);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
