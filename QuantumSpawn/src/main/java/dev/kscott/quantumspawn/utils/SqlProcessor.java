package dev.kscott.quantumspawn.utils;

import com.zaxxer.hikari.HikariDataSource;
import dev.kscott.quantumspawn.data.RespawnLocation;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class SqlProcessor implements DataProcessor {
    protected static HikariDataSource sqlConnectionPool;
    public abstract void setSqlConnectionPoll();

    public Connection getConnection() {
        try {
            return sqlConnectionPool.getConnection();
        } catch (SQLException e) {
            Bukkit.getLogger().warning("[QuantumSpawn] Failed to connect to database");
            return null;
        }
    }

    public void checkDatabase() {
        try (Connection connection = getConnection()) {
            String sql = "CREATE TABLE IF NOT EXISTS location (uuid TEXT, x int, z int)";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            Bukkit.getLogger().warning("[QuantumSpawn] Failed to check database");
            e.printStackTrace();
        }

    }

    public boolean checkJoined(Player player) {
        ResultSet rs;
        try (Connection connection = getConnection()) {
            String sql = "SELECT * FROM location WHERE uuid = ?";
            try (PreparedStatement psmt = connection.prepareStatement(sql)) {
                psmt.setString(1, player.getUniqueId().toString());
                rs = psmt.executeQuery();
                if (rs.next()) {
                    respawnLocationMap.put(player.getName(), new RespawnLocation(rs.getInt("x"), rs.getInt("z")));
                    return true;
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public void buildData(Player player, int x, int z) {
        String playerName = player.getName();
        try (Connection connection = getConnection()) {
            String sql = "INSERT INTO location (uuid, x, z) VALUES (?,?,?)";
            try (PreparedStatement psmt = connection.prepareStatement(sql)) {
                psmt.setString(1, player.getUniqueId().toString());
                psmt.setInt(2, x);
                psmt.setInt(3, z);
                psmt.executeUpdate();
                respawnLocationMap.put(playerName, new RespawnLocation(x, z));
                Bukkit.getLogger().info("[QuantumSpawn] Generated Database table for " + playerName + ": {X=" + x + ", Z=" + z + "}");
            }
        } catch (SQLException ex) {
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
        String playerName = player.getName();
        try (Connection connection = getConnection()) {
            String sql = "DELETE FROM location WHERE player = ?";
            try (PreparedStatement psmt = connection.prepareStatement(sql)) {
                psmt.executeUpdate();
                respawnLocationMap.remove(playerName);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}
