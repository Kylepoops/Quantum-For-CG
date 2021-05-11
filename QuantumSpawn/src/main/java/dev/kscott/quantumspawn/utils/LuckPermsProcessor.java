package dev.kscott.quantumspawn.utils;

import dev.kscott.quantumspawn.QuantumSpawnPlugin;
import dev.kscott.quantumspawn.data.RespawnLocation;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class LuckPermsProcessor {
    private final @NonNull RegisteredServiceProvider<LuckPerms> provider;
    private final LuckPerms api;
    private final JavaPlugin plugin;
    Map<String,RespawnLocation> respawnLocationMap = new HashMap<>();
    LuckPermsProcessor() {
        provider = QuantumSpawnPlugin.getLpProvider();
        api = provider.getProvider();
        plugin =  QuantumSpawnPlugin.getPlugin();
    }

    public static Map<String, RespawnLocation> getRespawnMap() {
        return respawnLocation;
    }

    public boolean isFirstJoin(Player player) {
        User user = api.getPlayerAdapter(Player.class).getUser(player);
        return player.hasPermission("sp.hasLocation");
    }

    public void loadLocation(Player player) {
        String playerName = player.getName();
        User user = api.getPlayerAdapter(Player.class).getUser(player);
        RespawnLocation respawnLocation = null;
        try {
            int x = Integer.parseInt(Objects.requireNonNull(user.getCachedData().getMetaData().getMetaValue("x")));
            int z = Integer.parseInt(Objects.requireNonNull(user.getCachedData().getMetaData().getMetaValue("z")));
            respawnLocationMap.put(playerName, new RespawnLocation(x, z));
            return;
        } catch (NullPointerException ex) {
            Bukkit.getLogger().info("Player" + playerName + "has sp.hasLocation but don't have MetaData");
        }
    }

    public void buildMateData(Player player, int x, int z) {
        try {
            User user = api.getPlayerAdapter(Player.class).getUser(player);
            plugin.getLogger().info("Generating MetaDate for " + playerName + ": {X=" + x + ", Z=" + z + "}");
            respawnLocationMap.put(player.getName(), new RespawnLocation(x, z));
            user.data().add(Node.builder("meta.x." + x).build());
            user.data().add(Node.builder("meta.z." + z).build());
            user.data().add(Node.builder("sp.hasLocation").build());
            api.getUserManager().saveUser(user);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
