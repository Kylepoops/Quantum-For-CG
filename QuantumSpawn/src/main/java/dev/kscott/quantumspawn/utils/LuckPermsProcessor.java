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

import java.util.Objects;

public class LuckPermsProcessor implements DataBaseProcessor {
    private final static @NonNull RegisteredServiceProvider<LuckPerms> provider = QuantumSpawnPlugin.getLpProvider();
    private final static LuckPerms api = provider.getProvider();
    private final static JavaPlugin plugin = QuantumSpawnPlugin.getPlugin();

    @Override
    public boolean checkJoined(Player player) {
        if (!player.hasPermission("sp.hasLocation")) {
            return false;
        }
        String playerName = player.getName();
        User user = api.getPlayerAdapter(Player.class).getUser(player);
        try {
            int x = Integer.parseInt(Objects.requireNonNull(user.getCachedData().getMetaData().getMetaValue("x")));
            int z = Integer.parseInt(Objects.requireNonNull(user.getCachedData().getMetaData().getMetaValue("z")));
            respawnLocationMap.put(playerName, new RespawnLocation(x, z));
            return true;
        } catch (NullPointerException ex) {
            Bukkit.getLogger().info("Player" + playerName + "has sp.hasLocation but don't have MetaData");
            return false;
        }

    }

    @Override
    public void buildData(Player player, int x, int z) {
        String playerName = player.getName();
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
