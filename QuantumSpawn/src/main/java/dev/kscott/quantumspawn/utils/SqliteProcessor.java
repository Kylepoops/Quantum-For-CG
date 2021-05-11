package dev.kscott.quantumspawn.utils;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.kscott.quantumspawn.QuantumSpawnPlugin;
import dev.kscott.quantumspawn.config.Config;
import dev.kscott.quantumspawn.inject.ConfigModule;

import java.sql.Connection;
import java.sql.SQLException;

public class SqliteProcessor {
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
}
