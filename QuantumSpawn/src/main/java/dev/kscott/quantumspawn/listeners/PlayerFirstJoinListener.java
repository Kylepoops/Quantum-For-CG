package dev.kscott.quantumspawn.listeners;

import com.google.inject.Inject;
import dev.kscott.quantum.location.LocationProvider;
import dev.kscott.quantum.location.QuantumLocation;
import dev.kscott.quantum.rule.ruleset.QuantumRuleset;
import dev.kscott.quantumspawn.config.Config;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.concurrent.CompletableFuture;

public class PlayerFirstJoinListener implements Listener {

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
    public PlayerFirstJoinListener(
            final @NonNull JavaPlugin plugin,
            final @NonNull Config config,
            final @NonNull LocationProvider locationProvider
    ) {
        this.config = config;
        this.plugin = plugin;
        this.locationProvider = locationProvider;
    }

    @EventHandler
    public void onPlayerFirstJoin(final @NonNull PlayerJoinEvent event) {
        final @NonNull Player player = event.getPlayer();

        if (player.hasPermission("sp.hasLocation")) {
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
                RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
                if (provider != null) {
                    LuckPerms api = provider.getProvider();
                    User user = api.getPlayerAdapter(Player.class).getUser(player);
                    int x = (int) location.getX();
                    int z = (int) location.getZ();
                    user.data().add(Node.builder("sp.location." + x + "." + z).build());
                    user.data().add(Node.builder("sp.hasLocation").build());
                }
            }
        }.runTask(plugin));
    }

}
