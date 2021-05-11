package dev.kscott.quantumspawn.utils;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.kscott.quantumspawn.QuantumSpawnPlugin;
import dev.kscott.quantumspawn.config.Config;
import dev.kscott.quantumspawn.data.RespawnLocation;
import dev.kscott.quantumspawn.inject.ConfigModule;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SqliteProcessor implements DataBaseProcessor {
    private static HikariDataSource sqlConnectionPool;
    public static void setSqlConnectionPoll() {
        final Injector injector = Guice.createInjector(new ConfigModule());
        final Config config = injector.getInstance(Config.class);

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setDriverClassName("org.sqlite.JDBC");

        hikariConfig.setConnectionTimeout(config.getConnectionTimeout());
        hikariConfig.setMinimumIdle(config.getMinimumIdle());
        hikariConfig.setMaximumPoolSize(config.getMaximumPoolSize());

        String URL = "jdbc:sqlite:" + QuantumSpawnPlugin.getPlugin().getDataFolder().getAbsolutePath() + "database.db";
        hikariConfig.setJdbcUrl(URL);

        hikariConfig.setAutoCommit(true);
        sqlConnectionPool= new HikariDataSource(hikariConfig);
    }

    public Connection getConnection() {
        try {
            return sqlConnectionPool.getConnection();
        } catch (SQLException e) {
            QuantumSpawnPlugin.getPlugin().getLogger().warning("Failed to connect to database");
            return null;
        }
    }

    public void checkDatabase() {
        try (Connection connection = getConnection()) {
            String sql = "CREATE TABLE location (player TEXT, x int, z int) IF NOT EXISTS";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            QuantumSpawnPlugin.getPlugin().getLogger().warning("Failed to check database");
        }

    }

    public boolean checkJoined(Player player) {
        ResultSet rs;
        String playerName = player.getName();
        try (Connection connection = getConnection()) {
            String sql = "SELECT * FROM location WHERE player = '?'";
            try (PreparedStatement psmt = connection.prepareStatement(sql)) {
                psmt.setString(1, playerName);
                rs = psmt.executeQuery();
                if (rs.next()) {
                    respawnLocationMap.put(playerName, new RespawnLocation(rs.getInt("x"), rs.getInt("z")));
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
            String sql = "INSERT INTO location (TEXT, INTEGER, INTEGER) VALUES (?,?,?)";
            try (PreparedStatement psmt = connection.prepareStatement(sql)) {
                psmt.setString(1, playerName);
                psmt.setInt(2, x);
                psmt.setInt(3, z);
                psmt.executeUpdate();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}
