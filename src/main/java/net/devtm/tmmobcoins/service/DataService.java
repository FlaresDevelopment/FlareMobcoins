package net.devtm.tmmobcoins.service;

import net.devtm.tmmobcoins.TMMobCoins;
import net.flares.lib.Lib;
import net.flares.lib.database.MySQL.SQLHandler;
import net.flares.lib.database.json.JsonFile;
import net.devtm.tmmobcoins.API.MobcoinsPlayer;
import net.devtm.tmmobcoins.TMMobCoinsPlugin;
import net.devtm.tmmobcoins.files.FilesManager;
import net.devtm.tmmobcoins.util.FlarePlayer;
import net.devtm.tmmobcoins.util.Utils;
import org.bukkit.entity.Mob;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

public class DataService {

    int read = 0;
    final HashMap<UUID, MobcoinsPlayer> cache = new HashMap<>();
    final List<UUID> hasAccounts = new ArrayList<>();
    final List<UUID> cacheUpdates = new ArrayList<>();
    long lastUpdateTime = 0;
    String storageType = null;
    public String table = null;
    public int firstMobcoins = 0;

    public void updateTask () {
        new BukkitRunnable() {
            @Override
            public void run() {
                setMobcoins(null, 0);
            }
        }.runTaskTimerAsynchronously(TMMobCoins.PLUGIN.getPlugin(), 5, 1000);
    }

    public MobcoinsPlayer warpPlayer(UUID uuid) {
        // get player if the player don't exist (in cache)
        if(cache.containsKey(uuid)) {
            return cache.get(uuid);
        } else {
            if(hasAccount(uuid)) {
                return new MobcoinsPlayer(uuid, getMobcoins(uuid), getMultiplier(uuid));
            } else {
                return new MobcoinsPlayer(uuid, firstMobcoins, 1);
            }
        }
    }
    public MobcoinsPlayer warpPlayer(String name) {
        UUID uuid = Utils.UTILS.getPlayerUUID(name);
        // get player if the player don't exist (in cache)
        if(cache.containsKey(uuid)) {
            return cache.get(uuid);
        } else {
            if(hasAccount(uuid)) {
                return new MobcoinsPlayer(uuid, getMobcoins(uuid), getMultiplier(uuid));
            } else {
                return new MobcoinsPlayer(uuid, firstMobcoins, 1);
            }
        }
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
                try (Connection connection = TMMobCoins.PLUGIN.getDatabase().getConnection()) {
                    if(SQLHandler.exists(connection, table, "uuid", uuid.toString()))
                        hasAccounts.add(uuid);
                    return hasAccounts.contains(uuid);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            case "sqlite":
                try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + new File(TMMobCoins.PLUGIN.getPlugin().getDataFolder(), "data/data.db").getAbsolutePath())) {
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
                    try(Connection connection = TMMobCoins.PLUGIN.getDatabase().getConnection()) {
                        createPlayerProfile(connection, uuid);
                        mobcoins = Double.parseDouble(SQLHandler.get(connection, table, "mobcoins", new String[]{"uuid='" + uuid + "'"}).toString());
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                case "sqlite":
                    try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + new File(TMMobCoins.PLUGIN.getPlugin().getDataFolder(), "data/data.db").getAbsolutePath())) {
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
                try(Connection connection = TMMobCoins.PLUGIN.getDatabase().getConnection()) {
                    createPlayerProfile(connection, uuid);
                    multiplier = Double.parseDouble(SQLHandler.get(connection, table, "multiplier", new String[]{"uuid='" + uuid + "'"}).toString());
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                break;
            case "sqlite":
                try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + new File(TMMobCoins.PLUGIN.getPlugin().getDataFolder(), "data/data.db").getAbsolutePath())) {
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
                    try (Connection connection = TMMobCoins.PLUGIN.getDatabase().getConnection()) {
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
                    try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + new File(TMMobCoins.PLUGIN.getPlugin().getDataFolder(), "data/data.db").getAbsolutePath())) {
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
                try(Connection connection = TMMobCoins.PLUGIN.getDatabase().getConnection()) {
                    createPlayerProfile(connection, uuid);
                    SQLHandler.set(connection, table, "multiplier", multiplier, new String[]{"uuid = '" + uuid + "'"});
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                break;
            case "sqlite":
                try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + new File(TMMobCoins.PLUGIN.getPlugin().getDataFolder(), "data/data.db").getAbsolutePath())) {
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

