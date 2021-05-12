package dev.kscott.quantumspawn.utils;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.kscott.quantumspawn.QuantumSpawnPlugin;
import dev.kscott.quantumspawn.config.Config;

public class SqliteProcessor extends SqlProcessor {

    public void setSqlConnectionPoll() {
        final Config config = QuantumSpawnPlugin.getSpawnConfig();

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setDriverClassName("org.sqlite.JDBC");

        hikariConfig.setConnectionTimeout(config.getConnectionTimeout());
        hikariConfig.setMinimumIdle(config.getMinimumIdle());
        hikariConfig.setMaximumPoolSize(config.getMaximumPoolSize());

        String URL = "jdbc:sqlite:" + QuantumSpawnPlugin.getPlugin().getDataFolder().getAbsolutePath() + "/database.db";
        hikariConfig.setJdbcUrl(URL);

        hikariConfig.setAutoCommit(true);
        sqlConnectionPool= new HikariDataSource(hikariConfig);
    }
}