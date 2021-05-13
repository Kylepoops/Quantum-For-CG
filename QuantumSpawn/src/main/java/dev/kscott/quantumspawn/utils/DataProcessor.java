package dev.kscott.quantumspawn.utils;

import dev.kscott.quantumspawn.QuantumSpawnPlugin;
import dev.kscott.quantumspawn.data.RespawnLocation;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public interface DataProcessor {

    Map<String, RespawnLocation> respawnLocationMap = new HashMap<>();

    static Map<String, RespawnLocation> getRespawnMap() {
        return respawnLocationMap;
    }

    boolean checkJoined(Player player);

    void buildData(Player player, int x, int z);

    void setData(Player player, int x, int z);

    void clearData(Player player);

    static DataProcessor getDataProcessor() {
        switch (QuantumSpawnPlugin.getSpawnConfig().getDBTYPE()) {
            case "mysql" :
                return new MySQLProcessor();
            case "luckperms" :
                return new LuckPermsProcessor();
            default :
            case "sqlite" :
                return new SqliteProcessor();
        }
    }

}
