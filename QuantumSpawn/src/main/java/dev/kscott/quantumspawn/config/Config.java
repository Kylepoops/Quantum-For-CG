package dev.kscott.quantumspawn.config;

import dev.kscott.quantum.rule.ruleset.QuantumRuleset;
import dev.kscott.quantum.rule.ruleset.RulesetRegistry;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;

import java.io.File;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Stores the Quantum configuration and handles the loading and registration of rulesets
 */
public class Config {

    /**
     * JavaPlugin reference
     */
    private final @NonNull JavaPlugin plugin;

    /**
     * A map that stores a world's UUID and their associated ruleset
     */
    private final @NonNull Map<UUID, QuantumRuleset> worldRulesetMap;

    /**
     * The root quantum.conf config node
     */
    private @MonotonicNonNull CommentedConfigurationNode root;

    /**
     * RulesetRegistry reference
     */
    private final @NonNull RulesetRegistry rulesetRegistry;

    /**
     * How much cooldown should be applied if LP isn't enabled
     */
    private boolean SPAWN_ON_FIRST_JOIN;

    /**
     * Is EssentialsX TP integration enabled?
     */
    private boolean SPAWN_ON_DEATH;

    /**
     * Should players be sent to their bed on death, if they have one?
     */
    private boolean SPAWN_ON_DEATH_GO_TO_BED;

    /**
     * Is the default world system enabled?
     */
    private boolean DEFAULT_WORLD_ENABLED;

    private int RESPAWN_RADIUS;

    private String DBTYPE;
    private long connectionTimeout;
    private int minimumIdle;
    private int maximumPoolSize;

    /**
     * The default world to put the player in.
     */
    private @Nullable World DEFAULT_WORLD;

    /**
     * Constructs the config, loads it, and loads rulesets.
     *
     * @param plugin          {@link this#plugin}
     * @param rulesetRegistry {@link this#rulesetRegistry}
     */
    public Config(final @NonNull RulesetRegistry rulesetRegistry, final @NonNull JavaPlugin plugin) {
        this.plugin = plugin;
        this.rulesetRegistry = rulesetRegistry;

        // Save config to file if it doesn't already exist
        if (!new File(this.plugin.getDataFolder(), "config.conf").exists()) {
            plugin.saveResource("config.conf", false);
        }

        this.worldRulesetMap = new HashMap<>();

        // Load the config
        this.loadConfig();
        this.loadConfigurationValues();
    }

    /**
     * Returns a registered QuantumRuleset that is associated to a spawn world
     *
     * @param world World
     * @return associated QuantumRuleset
     */
    public @Nullable QuantumRuleset getRuleset(final @NonNull World world) {
        return this.worldRulesetMap.get(world.getUID());
    }

    /**
     * Loads the config into the {@link this.root} node
     */
    private void loadConfig() {
        final HoconConfigurationLoader loader = HoconConfigurationLoader.builder()
                .path(Paths.get(plugin.getDataFolder().getAbsolutePath(), "config.conf"))
                .build();

        try {
            root = loader.load();
        } catch (ConfigurateException e) {
            throw new RuntimeException("Failed to load the configuration.", e);
        }
    }

    /**
     * Loads QuantumWild's configuration values
     */
    private void loadConfigurationValues() {
        this.SPAWN_ON_FIRST_JOIN = this.root.node("spawn").node("spawn-on-join").node("first-join").getBoolean(false);
        this.SPAWN_ON_DEATH = this.root.node("spawn").node("spawn-on-death").node("enabled").getBoolean(false);
        this.SPAWN_ON_DEATH_GO_TO_BED = this.root.node("spawn").node("spawn-on-death").node("go-to-bed").getBoolean(false);
        this.RESPAWN_RADIUS = this.root.node("spawn").node("spawn-on-death").node("respawn-radius").getInt();
        this.DEFAULT_WORLD_ENABLED = this.root.node("spawn").node("default-world").node("enabled").getBoolean(false);

        this.DBTYPE = this.root.node("database").node("dbtype").getString("luckperms").toLowerCase();
        this.connectionTimeout = this.root.node("database").node("hikariConfig").node("connectionTimeout").getLong(30000);
        this.minimumIdle = this.root.node("database").node("hikariConfig").node("minimunIdle").getInt(10);
        this.maximumPoolSize = this.root.node("database").node("hikariConfig").node("maximumPollSize").getInt(50);


        this.DEFAULT_WORLD = Bukkit.getWorld(this.root.node("spawn").node("default-world").node("world").getString(""));

        if (this.DEFAULT_WORLD_ENABLED && this.DEFAULT_WORLD == null) {
            this.plugin.getLogger().warning("The default-world configuration option is enabled, but the world name does not exist! Please review your QuantumWild configuration.");
            this.DEFAULT_WORLD_ENABLED = false;
        }

        this.worldRulesetMap.clear();

        for (final Map.Entry<Object, ? extends ConfigurationNode> entry : root.node("worlds").childrenMap().entrySet()) {
            final @NonNull Object key = entry.getKey();

            if (!(key instanceof String)) {
                this.plugin.getLogger().severe("Error loading world ruleset map.");
                continue;
            }

            final @NonNull String worldName = (String) key;
            final @Nullable World world = Bukkit.getWorld(worldName);

            if (world == null) {
                this.plugin.getLogger().severe("Error loading ruleset map: world was null. Are you sure you spelled '" + worldName + "' correctly?");
                continue;
            }

            final @NonNull ConfigurationNode value = entry.getValue();

            final @Nullable String rulesetId = value.node("ruleset").getString();

            if (rulesetId == null) {
                this.plugin.getLogger().severe("Error loading ruleset map: ruleset id was null.");
                continue;
            }

            final @Nullable QuantumRuleset ruleset = this.rulesetRegistry.getRuleset(rulesetId);

            if (ruleset == null) {
                this.plugin.getLogger().severe("Error loading ruleset map: RulesetRegistry returned null. Are you sure you spelled '" + rulesetId + "' correctly?");
                continue;
            }

            this.worldRulesetMap.put(world.getUID(), ruleset);
        }
    }

    public boolean isSpawnOnFirstJoinEnabled() {
        return SPAWN_ON_FIRST_JOIN;
    }

    public boolean isSpawnOnDeathEnabled() {
        return SPAWN_ON_DEATH;
    }

    public boolean isGoToBedEnabled() {
        return SPAWN_ON_DEATH_GO_TO_BED;
    }

    /**
     * @return {@link this#DEFAULT_WORLD_ENABLED}
     */
    public boolean isDefaultWorldEnabled() {
        return DEFAULT_WORLD_ENABLED;
    }

    public int getRESPAWN_RADIUS() {
        return RESPAWN_RADIUS;
    }

    public String getDBTYPE() {
        return DBTYPE;
    }

    public long getConnectionTimeout() {
        return connectionTimeout;
    }

    public int getMinimumIdle() {
        return minimumIdle;
    }

    public int getMaximumPoolSize() {
        return maximumPoolSize;
    }

    /**
     * May be null if {@link this#DEFAULT_WORLD_ENABLED} is false.
     *
     * @return {@link this#DEFAULT_WORLD}
     */
    public @Nullable World getDefaultWorld() {
        return DEFAULT_WORLD;
    }
}

