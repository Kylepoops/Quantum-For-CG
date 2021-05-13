package dev.kscott.quantumspawn.utils;

import dev.kscott.quantumspawn.data.RespawnLocation;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.matcher.NodeMatcher;
import net.luckperms.api.node.types.MetaNode;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Objects;

public class LuckPermsProcessor implements DataProcessor {
    private final static @NonNull RegisteredServiceProvider<LuckPerms> provider = Objects.requireNonNull(Bukkit.getServicesManager().getRegistration(LuckPerms.class));
    private final static LuckPerms api = Objects.requireNonNull(provider).getProvider();

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
            Bukkit.getLogger().info("[QuantumSpawn] Player" + playerName + "has sp.hasLocation but don't have MetaData");
            return false;
        }

    }

    @Override
    public void buildData(Player player, int x, int z) {
        String playerName = player.getName();
        try {
            User user = api.getPlayerAdapter(Player.class).getUser(player);
            Bukkit.getLogger().info("[QuantumSpawn] Generating MetaDate for " + playerName + ": {X=" + x + ", Z=" + z + "}");
            respawnLocationMap.put(player.getName(), new RespawnLocation(x, z));
            user.data().add(MetaNode.builder("x", String.valueOf(x)).build());
            user.data().add(MetaNode.builder("z", String.valueOf(z)).build());
            user.data().add(Node.builder("sp.hasLocation").build());
            api.getUserManager().saveUser(user);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void setData(Player player, int x, int z) {
            clearData(player);
            buildData(player, x, z);
    }

    @Override
    public void clearData(Player player) {
        try {
            respawnLocationMap.remove(player.getName());
            User user = api.getPlayerAdapter(Player.class).getUser(player);
            user.data().clear(NodeMatcher.metaKey("x"));
            user.data().clear(NodeMatcher.metaKey("z"));
            user.data().remove(Node.builder("sp.hasLocation").build());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
