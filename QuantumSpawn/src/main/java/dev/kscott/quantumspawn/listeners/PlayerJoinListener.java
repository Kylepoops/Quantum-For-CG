package dev.kscott.quantumspawn.listeners;

import com.google.inject.Inject;
import dev.kscott.quantum.location.LocationProvider;
import dev.kscott.quantum.location.QuantumLocation;
import dev.kscott.quantum.rule.ruleset.QuantumRuleset;
import dev.kscott.quantumspawn.config.Config;
import dev.kscott.quantumspawn.utils.DataBaseProcessor;
import dev.kscott.quantumspawn.utils.LuckPermsProcessor;
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

    DataBaseProcessor dataBaseProcessor;

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

        if (config.getDBTYPE().equals("sqlite")) {
            dataBaseProcessor = new SqliteProcessor();
        } else {
            dataBaseProcessor = new LuckPermsProcessor();
        }
    }

    @EventHandler
    public void onPlayerJoin(final @NonNull PlayerJoinEvent event) {
        final @NonNull Player player = event.getPlayer();

        if (dataBaseProcessor.checkJoined(player)) {
            return;
        }

        final @NonNull World world;
        if (config.isDefaultWorldEnabled()) {
            assert config.getDefaultWorld() != null;
            world = config.getDefaultWorld();
        } else {
            world = player.getWorld();
        }

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
                dataBaseProcessor.buildData(player, x, z);
            }
        }.runTask(plugin));
    }

}
