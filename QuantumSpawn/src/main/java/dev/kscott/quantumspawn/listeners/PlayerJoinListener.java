package dev.kscott.quantumspawn.listeners;

import com.google.inject.Inject;
import dev.kscott.quantum.location.LocationProvider;
import dev.kscott.quantum.location.QuantumLocation;
import dev.kscott.quantum.rule.ruleset.QuantumRuleset;
import dev.kscott.quantumspawn.QuantumSpawnPlugin;
import dev.kscott.quantumspawn.config.Config;
import dev.kscott.quantumspawn.data.RespLoc;
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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class PlayerJoinListener implements Listener {

    private static final Map<String, RespLoc> respLoc = new HashMap<>();
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
    }

    public static Map<String, RespLoc> getRespawnMap() {
        return respLoc;
    }

    @EventHandler
    public void onPlayerJoin(final @NonNull PlayerJoinEvent event) {
        final @NonNull Player player = event.getPlayer();
        final @NonNull String playerName = player.getName();

        RegisteredServiceProvider<LuckPerms> provider = QuantumSpawnPlugin.getLpProvider();
        LuckPerms api = provider.getProvider();
        User user = api.getPlayerAdapter(Player.class).getUser(player);

        if (player.hasPermission("sp.hasLocation")) {
            try {
                int x = Integer.parseInt(Objects.requireNonNull(user.getCachedData().getMetaData().getMetaValue("x")));
                int z = Integer.parseInt(Objects.requireNonNull(user.getCachedData().getMetaData().getMetaValue("z")));
                respLoc.put(playerName, new RespLoc(x, z));
                return;
            } catch (NullPointerException ex) {
                Bukkit.getLogger().info("Player" + playerName + "has sp.hasLocation but don't have MetaData");
            }
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

                try {
                    int x = (int) location.getX();
                    int z = (int) location.getZ();
                    plugin.getLogger().info("Generating MetaDate for " + playerName + ": X=" + x + "; Z=" + z);
                    respLoc.put(player.getName(), new RespLoc(x, z));
                    user.data().add(Node.builder("meta.x." + x).build());
                    user.data().add(Node.builder("meta.z." + z).build());
                    user.data().add(Node.builder("sp.hasLocation").build());
                    api.getUserManager().saveUser(user);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }.runTask(plugin));
    }

}
