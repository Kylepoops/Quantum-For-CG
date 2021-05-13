package dev.kscott.quantumspawn.listeners;

import com.google.inject.Inject;
import dev.kscott.quantum.location.LocationProvider;
import dev.kscott.quantum.location.QuantumLocation;
import dev.kscott.quantum.rule.ruleset.QuantumRuleset;
import dev.kscott.quantumspawn.config.Config;
import dev.kscott.quantumspawn.utils.DataProcessor;
import dev.kscott.quantumspawn.utils.LuckPermsProcessor;
import dev.kscott.quantumspawn.utils.MySQLProcessor;
import dev.kscott.quantumspawn.utils.SqliteProcessor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.concurrent.CompletableFuture;

public class PlayerJoinListener implements Listener {

    DataProcessor dataProcessor;

    /**
     * Config reference.
     */
    private final @NonNull Config config;

    /**
     * JavaPlugin reference.
     */
    private final @NonNull JavaPlugin plugin;

    /**
     * LocationProvider reference.
     */
    private final @NonNull LocationProvider locationProvider;

    /**
     * Constructs PlayerJoinListener.
     *
     * @param locationProvider LocationProvider reference.
     */
    @Inject
    public PlayerJoinListener(
            final @NonNull JavaPlugin plugin,
            final @NonNull Config config,
            final @NonNull LocationProvider locationProvider
    ) {
        this.config = config;
        this.plugin = plugin;
        this.locationProvider = locationProvider;

        switch (config.getDBTYPE()) {
            case "sqlite" :
                dataProcessor = new SqliteProcessor();
                break;
            case "mysql" :
                dataProcessor = new MySQLProcessor();
                break;
            case "luckperms" :
            default :
                dataProcessor = new LuckPermsProcessor();
                break;
        }
    }

    @EventHandler
    public void onPlayerJoin(final @NonNull PlayerJoinEvent event) {
        final @NonNull Player player = event.getPlayer();

        if (dataProcessor.checkJoined(player)) {
            return;
        }

        @SuppressWarnings("ConstantConditions")
        final @NonNull World world = this.config.isDefaultWorldEnabled() ? this.config.getDefaultWorld() : player.getWorld();

        @SuppressWarnings("ConstantConditions")
        final @Nullable QuantumRuleset ruleset = config.getRuleset(world);

        if (ruleset == null) {
            return;
        }

        final @NonNull CompletableFuture<QuantumLocation> locationCf = locationProvider.getLocation(ruleset, ruleset.getSearchArea().getMaxX(), ruleset.getSearchArea().getMinX(), ruleset.getSearchArea().getMaxZ(), ruleset.getSearchArea().getMinZ());

        locationCf.thenAccept(quantumLocation -> new BukkitRunnable() {
            @Override
            public void run() {
                Location location = quantumLocation.getLocation();
                player.teleportAsync(QuantumLocation.toCenterHorizontalLocation(location));
                int x = (int) location.getX();
                int z = (int) location.getZ();
                dataProcessor.buildData(player, x, z);
            }
        }.runTask(plugin));
    }

}
