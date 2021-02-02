package dev.kscott.quantumspawn.listeners;

import com.google.inject.Inject;
import dev.kscott.quantum.location.LocationProvider;
import dev.kscott.quantum.location.QuantumLocation;
import dev.kscott.quantum.rule.ruleset.QuantumRuleset;
import dev.kscott.quantumspawn.config.Config;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        final @NonNull Player player = event.getPlayer();
        if (!player.hasPlayedBefore()) {
            return;
        }

        if (config.isGoToBedEnabled() && player.getBedSpawnLocation() != null) {
            return;
        }

        final int x;
        final int z;

        int Respawn_Radius = config.getRESPAWN_RADIUS();

        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            LuckPerms api = provider.getProvider();
            User user = api.getPlayerAdapter(Player.class).getUser(player);
            String nodes = user.getNodes().toString();
            String pattern = "sp\\.location\\.(-?[0-9]+)\\.(-?[0-9]+)";
            Matcher m = Pattern.compile(pattern).matcher(nodes);
            x = Integer.getInteger(m.group(1));
            z = Integer.getInteger(m.group(2));

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

            int MaxX = x + Respawn_Radius;
            int MinX = x - Respawn_Radius;
            int MaxZ = z + Respawn_Radius;
            int MinZ = z - Respawn_Radius;

            final @NonNull CompletableFuture<QuantumLocation> locationCf = this.locationProvider.getLocation(ruleset, MaxX, MinX, MaxZ, MinZ);

            locationCf.thenAccept(quantumLocation -> new BukkitRunnable() {
                @Override
                public void run() {
                    player.teleportAsync(QuantumLocation.toCenterHorizontalLocation(quantumLocation.getLocation()));
                }
            }.runTask(plugin));
        }
    }

}
