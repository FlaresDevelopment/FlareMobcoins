package net.flares.flaremobcoins.service;

import net.flarepowered.core.data.MySQL.SQLHandler;
import net.flarepowered.core.data.hikari.HikariDatabase;
import net.flares.flaremobcoins.FlareMobcoins;
import net.flares.flaremobcoins.API.MobcoinsPlayer;
import net.flares.flaremobcoins.files.FilesManager;
import net.flares.flaremobcoins.util.Utils;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

public class DataService {

    private HikariDatabase database;

    int read = 0;
    public final HashMap<UUID, MobcoinsPlayer> cache = new HashMap<>();
    public final List<UUID> hasAccounts = new ArrayList<>();
    public final List<UUID> cacheUpdates = new ArrayList<>();
    long lastUpdateTime = 0;
    String storageType = null;
    public String table = null;
    public int firstMobcoins = 0;

    public void onEnable() {
        switch (Objects.requireNonNull(FilesManager.ACCESS.getConfig().getConfig().getString("storage.type")).toLowerCase(Locale.ROOT)) {
            case "mysql":
                this.database = new HikariDatabase("mysql",
                        FilesManager.ACCESS.getConfig().getConfig().getString("storage.mysql.host"),
                        FilesManager.ACCESS.getConfig().getConfig().getInt("storage.mysql.port"),
                        FilesManager.ACCESS.getConfig().getConfig().getString("storage.mysql.database"),
                        FilesManager.ACCESS.getConfig().getConfig().getString("storage.mysql.password"),
                        FilesManager.ACCESS.getConfig().getConfig().getString("storage.mysql.username"), ""
                );
                try {
                    SQLHandler.createTableIfNotExists(database.getConnection(), FilesManager.ACCESS.getConfig().getConfig().getString("storage.mysql.table"),
                            "player VARCHAR(100), uuid VARCHAR(100), mobcoins DOUBLE(10,2), multiplier DOUBLE(10,2)");
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                break;
            case "sqlite":
                String url = "jdbc:sqlite:" + new File(FlareMobcoins.PLUGIN.getPlugin().getDataFolder(), "data/data.db").getAbsolutePath();
                try (Connection conn = DriverManager.getConnection(url)) {
                    if (conn != null) {
                        DatabaseMetaData meta = conn.getMetaData();
                    }
                    SQLHandler.createTableIfNotExists(conn, FilesManager.ACCESS.getConfig().getConfig().getString("storage.mysql.table"),
                            "player VARCHAR(100), uuid VARCHAR(100), mobcoins DOUBLE(10,2), multiplier DOUBLE(10,2)");
                } catch (SQLException e) {
                    System.out.println(e.getMessage());
                }
                break;
        }
    }

    public void updateTask () {
//        new BukkitRunnable() {
//            @Override
//            public void run() {
//                setMobcoins(null, 0);
//            }
//        }.runTaskTimerAsynchronously(FlareMobcoins.PLUGIN.getPlugin(), 5, 1000);
    }

    public MobcoinsPlayer warpPlayerNoCache(UUID uuid) {
        return new MobcoinsPlayer(uuid, getMobcoins(uuid), getMultiplier(uuid));
    }

    public void reloadDataService() {
        storageType = FilesManager.ACCESS.getConfig().getConfig().getString("storage.type").toLowerCase(Locale.ROOT);
        table = FilesManager.ACCESS.getConfig().getConfig().getString("storage.mysql.table");
        firstMobcoins = FilesManager.ACCESS.getConfig().getConfig().getInt("first_join_give_mobcoins");
    }

    public boolean hasAccount(UUID uuid) {
        if(hasAccounts.contains(uuid))
            return true;
        switch (storageType) {
            case "file":
                if(FilesManager.ACCESS.getData().getConfig().contains("account." + uuid))
                    hasAccounts.add(uuid);
                return hasAccounts.contains(uuid);
            case "mysql":
                try (Connection connection = database.getConnection()) {
                    if(SQLHandler.exists(connection, table, "uuid", uuid.toString()))
                        hasAccounts.add(uuid);
                    return hasAccounts.contains(uuid);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            case "sqlite":
                try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + new File(FlareMobcoins.PLUGIN.getPlugin().getDataFolder(), "data/data.db").getAbsolutePath())) {
                    if(SQLHandler.exists(conn, table, "uuid", uuid.toString()))
                        hasAccounts.add(uuid);
                    return hasAccounts.contains(uuid);
                } catch (SQLException e) {
                    System.out.println(e.getMessage());
                }
        }
        return false;
    }

    public double getMobcoins(UUID uuid) {
        double mobcoins = 0;
        if(!cacheUpdates.contains(uuid))
            switch (storageType) {
                case "file":
                    mobcoins = FilesManager.ACCESS.getData().getConfig().getDouble("account." + uuid + ".mobcoins");
                    break;
                case "mysql":
                    try(Connection connection = database.getConnection()) {
                        createPlayerProfile(connection, uuid);
                        mobcoins = Double.parseDouble(SQLHandler.get(connection, table, "mobcoins", new String[]{"uuid='" + uuid + "'"}).toString());
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                case "sqlite":
                    try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + new File(FlareMobcoins.PLUGIN.getPlugin().getDataFolder(), "data/data.db").getAbsolutePath())) {
                        mobcoins = Double.parseDouble(SQLHandler.get(conn, table, "mobcoins", new String[]{"uuid='" + uuid + "'"}).toString());
                    } catch (SQLException e) {
                        System.out.println(e.getMessage());
                    }
                    break;
            }
        if(!cache.containsKey(uuid)) {
            cache.put(uuid, new MobcoinsPlayer(uuid, mobcoins, -1));
            double multiplier = getMultiplier(uuid);
            cache.get(uuid).objectSetMultiplier(multiplier);
        } else
            cache.get(uuid).objectSetMobcoins(mobcoins);
        return mobcoins;
    }

    public double getMultiplier(UUID uuid) {
        double multiplier = 0;
        switch (storageType) {
            case "file":
                multiplier = FilesManager.ACCESS.getData().getConfig().getDouble("account." + uuid + ".multiplier");
                break;
            case "mysql":
                try(Connection connection = database.getConnection()) {
                    createPlayerProfile(connection, uuid);
                    multiplier = Double.parseDouble(SQLHandler.get(connection, table, "multiplier", new String[]{"uuid='" + uuid + "'"}).toString());
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                break;
            case "sqlite":
                try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + new File(FlareMobcoins.PLUGIN.getPlugin().getDataFolder(), "data/data.db").getAbsolutePath())) {
                    multiplier = Double.parseDouble(SQLHandler.get(conn, table, "multiplier", new String[]{"uuid='" + uuid + "'"}).toString());
                } catch (SQLException e) {
                    System.out.println(e.getMessage());
                }
                break;
        }

        if(!cache.containsKey(uuid)) {
            cache.put(uuid, new MobcoinsPlayer(uuid, -1, multiplier));
            double mobcoins = getMobcoins(uuid);
            cache.get(uuid).objectSetMobcoins(mobcoins);
        } else
            cache.get(uuid).objectSetMultiplier(multiplier);
        return multiplier;
    }

    public void setMobcoins(UUID uuid, double amount) {
        if(uuid != null)
            if(!cache.containsKey(uuid)) {
                cache.put(uuid, new MobcoinsPlayer(uuid, amount, -1));
                double multiplier = getMultiplier(uuid);
                cache.get(uuid).objectSetMultiplier(multiplier);
                cacheUpdates.add(uuid);
            } else {
                cache.get(uuid).objectSetMobcoins(amount);
                cacheUpdates.add(uuid);
            }
        if(lastUpdateTime <= System.currentTimeMillis() - 10000) {
            switch (storageType) {
                case "file":
                    for(UUID uuids : cacheUpdates) {
                        FilesManager.ACCESS.getData().getConfig().set("account." + uuids + ".mobcoins", cache.get(uuids).getMobcoins());
                    }
                    FilesManager.ACCESS.getData().saveConfig();
                    cacheUpdates.clear();
                    lastUpdateTime = System.currentTimeMillis();
                    break;
                case "mysql":
                    try (Connection connection = database.getConnection()) {
                        for(UUID uuids : cacheUpdates) {
                            createPlayerProfile(connection, uuids);
                            SQLHandler.set(connection, table, "mobcoins", cache.get(uuids).getMobcoins(), new String[]{"uuid = '" + uuids + "'"});
                        }
                        cacheUpdates.clear();
                        lastUpdateTime = System.currentTimeMillis();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                case "sqlite":
                    try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + new File(FlareMobcoins.PLUGIN.getPlugin().getDataFolder(), "data/data.db").getAbsolutePath())) {
                        createPlayerProfile(conn, uuid);
                        SQLHandler.set(conn, table, "mobcoins", cache.get(uuid).getMobcoins(), new String[]{"uuid = '" + uuid + "'"});
                    } catch (SQLException e) {
                        System.out.println(e.getMessage());
                    }
                    break;
            }
        }
    }

    public void setMultiplier(UUID uuid, double multiplier) {
        if(!cache.containsKey(uuid)) {
            cache.put(uuid, new MobcoinsPlayer(uuid, -1, multiplier));
            double mobcoins = getMobcoins(uuid);
            cache.get(uuid).objectSetMobcoins(mobcoins);
        } else
            cache.get(uuid).objectSetMultiplier(multiplier);
        switch (storageType) {
            case "file":
                FilesManager.ACCESS.getData().getConfig().set("account." + uuid + ".multiplier", multiplier);
                FilesManager.ACCESS.getData().saveConfig();
                break;
            case "mysql":
                try(Connection connection = database.getConnection()) {
                    createPlayerProfile(connection, uuid);
                    SQLHandler.set(connection, table, "multiplier", multiplier, new String[]{"uuid = '" + uuid + "'"});
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                break;
            case "sqlite":
                try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + new File(FlareMobcoins.PLUGIN.getPlugin().getDataFolder(), "data/data.db").getAbsolutePath())) {
                    createPlayerProfile(conn, uuid);
                    SQLHandler.set(conn, table, "multiplier", multiplier, new String[]{"uuid = '" + uuid + "'"});
                } catch (SQLException e) {
                    System.out.println(e.getMessage());
                }
                break;
        }
    }

    /**
     * This will create a player account ONLY if the player dose not have an account!
     * @param uuid Player
     */
    public void createPlayerProfile(Connection connection, UUID uuid) {
        switch (storageType) {
            case "file":
                if (!FilesManager.ACCESS.getData().getConfig().contains("account." + uuid)) {
                    FilesManager.ACCESS.getData().getConfig().set("account." + uuid + ".mobcoins", firstMobcoins);
                    FilesManager.ACCESS.getData().getConfig().set("account." + uuid + ".multiplier", 1);
                    FilesManager.ACCESS.getData().saveConfig();
                }
                break;
            case "mysql":
            case "sqlite":
                if(!SQLHandler.exists(connection, table, "uuid", uuid.toString()))
                    SQLHandler.insertData(connection, table,
                            "player, uuid, mobcoins, multiplier", "'" + Utils.UTILS.getPlayerName(uuid) + "', '" + uuid + "', '" + firstMobcoins + "', '" + 1 + "'");
                break;
        }
    }
}

