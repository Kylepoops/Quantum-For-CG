package dev.kscott.quantumspawn.utils;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.kscott.quantumspawn.QuantumSpawnPlugin;
import dev.kscott.quantumspawn.config.Config;

public class MySQLProcessor extends SqlProcessor {

    public void setSqlConnectionPoll() {
        final Config config = QuantumSpawnPlugin.getSpawnConfig();

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setDriverClassName("com.mysql.jdbc.Driver");

        hikariConfig.setConnectionTimeout(config.getConnectionTimeout());
        hikariConfig.setMinimumIdle(config.getMinimumIdle());
        hikariConfig.setMaximumPoolSize(config.getMaximumPoolSize());

        String URL = "jdbc:mysql://"
                + config.getSqlAddress() + ":"
                + config.getSqlPort() + "/"
                + config.getSqlDatabase()
                + "?useUnicode=true&characterEncoding=UTF-8&useSSL=false";
        hikariConfig.setUsername(config.getSqlUser());
        hikariConfig.setPassword(config.getSqlPassword());
        hikariConfig.setJdbcUrl(URL);

        hikariConfig.setAutoCommit(true);
        sqlConnectionPool= new HikariDataSource(hikariConfig);
    }
}
