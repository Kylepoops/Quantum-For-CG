package dev.kscott.quantumspawn.listeners;

import com.google.inject.Inject;
import dev.kscott.quantum.location.LocationProvider;
import dev.kscott.quantum.location.LocationQueue;
import dev.kscott.quantum.location.QuantumLocation;
import dev.kscott.quantum.rule.ruleset.QuantumRuleset;
import dev.kscott.quantumspawn.config.Config;
import dev.kscott.quantumspawn.data.RespLoc;
import dev.kscott.quantumspawn.inject.QuantumModule;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.concurrent.CompletableFuture;

/**
 * Listens on player death-related events.
 */
public class PlayerDeathListener implements Listener {

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
    public PlayerDeathListener(
            final @NonNull JavaPlugin plugin,
            final @NonNull Config config,
            final @NonNull LocationProvider locationProvider
    ) {
        this.config = config;
        this.plugin = plugin;
        this.locationProvider = locationProvider;
    }

    /**
     * Handles the {@link PlayerRespawnEvent} and teleports them if enabled.
     *
     * @param event {@link PlayerRespawnEvent}.
     */
    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        final @NonNull Player player = event.getPlayer();
        final @NonNull String playerName = player.getName();

        plugin.getLogger().info("Respawn Event start for" + playerName);

        if (!player.hasPlayedBefore()) {
            return;
        }

        if (config.isGoToBedEnabled() && player.getBedSpawnLocation() != null) {
            return;
        }

        RespLoc resploc = PlayerJoinListener.getRespawnMap().get(playerName);

        final int x = resploc.getX();
        final int z = resploc.getZ();

        plugin.getLogger().info("Respawn Point from HashMap is {X:" + x + ", Z:" + z + "}");

        int Respawn_Radius = config.getRESPAWN_RADIUS();


        final @NonNull World world;
        if (this.config.isDefaultWorldEnabled()) {
            assert this.config.getDefaultWorld() != null;
            world = this.config.getDefaultWorld();
        } else {
            world = player.getWorld();
        }

        QuantumRuleset ruleset = config.getRuleset(world);

        if (ruleset == null) {
            return;
        }

        final int MaxX = x + Respawn_Radius;
        final int MinX = x - Respawn_Radius;
        final int MaxZ = z + Respawn_Radius;
        final int MinZ = z - Respawn_Radius;

        (new LocationQueue(this.locationProvider, (new QuantumModule(this.plugin)).provideRulesetRegistry())).clearLocations();

        final @NonNull CompletableFuture<QuantumLocation> locationCf = this.locationProvider.getLocation(ruleset, MaxX, MinX, MaxZ, MinZ);
        locationCf.thenAccept(quantumLocation -> new BukkitRunnable() {
            @Override
            public void run() {
                Location loc = null;
                //noinspection IdempotentLoopBody
                while (loc == null) {
                    loc = quantumLocation.getLocation();
                }
                player.teleportAsync(QuantumLocation.toCenterHorizontalLocation(loc));
                plugin.getLogger().info("Player " + playerName + " has been teleported to " + loc.toString());
            }
        }.runTask(plugin));
    }
}
