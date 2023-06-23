package net.devtm.tmmobcoins;

import lombok.Getter;
import net.flares.lib.Lib;
import net.flares.lib.base.bStatsMetrics;
import net.flares.lib.base.color.ColorAPI;
import net.devtm.tmmobcoins.command.MobcoinsCommand;
import net.devtm.tmmobcoins.files.FilesManager;
import net.devtm.tmmobcoins.listener.BasicListener;
import net.devtm.tmmobcoins.listener.ShopCommand;
import net.devtm.tmmobcoins.service.ServiceHandler;
import net.devtm.tmmobcoins.util.PlaceholderAPI;
import net.devtm.tmmobcoins.util.PlaceholdersClass;
import net.devtm.tmmobcoins.util.Utils;
import net.devtm.tmmobcoins.util.components.BuyComponent;
import net.devtm.tmmobcoins.util.components.MobcoinsComponent;
import net.devtm.tmmobcoins.util.components.OpenMenuComponent;
import net.flares.lib.database.MySQL.SQLHandler;
import net.flares.lib.database.hikari.HikariDatabase;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.logging.Level;

@Getter
public enum TMMobCoins {
  PLUGIN;
  private TMMobCoinsPlugin plugin;
  private HikariDatabase database;

  private final boolean enabledMenu = true;

  public void start(final TMMobCoinsPlugin plugin) {
    FilesManager.ACCESS.initialization();
    Lib.LIB.libStart(plugin);
    Lib.LIB.setCustomPlaceholders(new PlaceholdersClass());
    Lib.LIB.setLocales(FilesManager.ACCESS.getLocale().getConfig());
    Lib.LIB.getTMLArray().addComponent(new MobcoinsComponent());
    Lib.LIB.getTMLArray().addComponent(new OpenMenuComponent());
    Lib.LIB.getTMLArray().addComponent(new BuyComponent());
    this.plugin = plugin;
    startStorage();
    assert plugin != null : "Something went wrong! Plugin was null.";
    this.init();
    startLog();
    usebStats();
    ServiceHandler.SERVICE.onEnable();
    commandsSetup();
    ServiceHandler.SERVICE.getLoggerService().fileSetup();
    ServiceHandler.SERVICE.getDataService().reloadDataService();
    Utils.UTILS.reloadUtils();
  }

  /**
   * Stop method for the plugin - {@link JavaPlugin}
   *
   * @param plugin the plugin instance
   */
  public void stop(final TMMobCoinsPlugin plugin) {
    this.plugin = plugin;
    ServiceHandler.SERVICE.getDataService().setMobcoins(null, 0);
    stopLog();
  }

  /**
   * Initialize everything
   */
  private void init() {
    this.registerListener();
    if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null)
      new PlaceholderAPI().register();
    if(this.enabledMenu) {
      Lib.LIB.enableGUI();
    }
  }

  public void commandsSetup() {
    plugin.getCommand("mobcoins").setExecutor(new MobcoinsCommand());
  }

  public void startStorage() {
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
        String url = "jdbc:sqlite:" + new File(plugin.getDataFolder(), "data/data.db").getAbsolutePath();
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

  private void startLog() {

    plugin.getLogger().log(Level.INFO, ColorAPI.process(" _____ _                _____     _           _         "));
    plugin.getLogger().log(Level.INFO, ColorAPI.process("|   __| |___ ___ ___   |     |___| |_ ___ ___|_|___ ___ "));
    plugin.getLogger().log(Level.INFO, ColorAPI.process("|   __| | .'|  _| -_|  | | | | . | . |  _| . | |   |_ -|"));
    plugin.getLogger().log(Level.INFO, ColorAPI.process("|__|  |_|__,|_| |___|  |_|_|_|___|___|___|___|_|_|_|___|"));
    plugin.getLogger().log(Level.INFO, ColorAPI.process("__________________________________________ By Flares.dev"));
    plugin.getLogger().log(Level.INFO, ColorAPI.process("⬝ Loading plugin"));
    plugin.getLogger().log(Level.INFO, ColorAPI.process("  ⬞ Plugin version: v" + plugin.getDescription().getVersion()));
    plugin.getLogger().log(Level.INFO, ColorAPI.process("  ⬞ Using " + FilesManager.ACCESS.getConfig().getConfig().getString("storage_type.type") + " for data saving"));
    plugin.getLogger().log(Level.INFO, ColorAPI.process(""));
    plugin.getLogger().log(Level.INFO, ColorAPI.process("⬝ Getting dependencies"));
    plugin.getLogger().log(Level.INFO, ColorAPI.process("  ⬞ PlaceholderAPI - " + (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null ? "not available" : "enabled")));
    plugin.getLogger().log(Level.INFO, ColorAPI.process("  ⬞ Vault - " + (Bukkit.getPluginManager().getPlugin("Vault") == null ? "not available" : "enabled")));
    plugin.getLogger().log(Level.INFO, ColorAPI.process(""));
    plugin.getLogger().log(Level.INFO, ColorAPI.process("⬝ Need support or want to report a bug join our discord (discord.flares.dev)"));
    /*new VersionCheckers(getPlugin(), 101123).getUpdate(version -> {
      if (getPlugin().getDescription().getVersion().equals(version)) {
        plugin.getLogger().log(Level.INFO, ColorAPI.process("Running latest build (" + version + ")"));
      } else {
        Bukkit.getLogger().log(Level.WARNING, ColorAPI.process("Running an old build (" + getPlugin().getDescription().getVersion()
                + ") Latest build is (" + version + "). Please try to update to the last version!"));
      }
      plugin.getLogger().log(Level.INFO, ColorAPI.process("Made with love in Romania"));
    });*/
  }

  private void stopLog() {
    plugin.getLogger().log(Level.INFO, ColorAPI.process(" _____ _                _____     _           _         "));
    plugin.getLogger().log(Level.INFO, ColorAPI.process("|   __| |___ ___ ___   |     |___| |_ ___ ___|_|___ ___ "));
    plugin.getLogger().log(Level.INFO, ColorAPI.process("|   __| | .'|  _| -_|  | | | | . | . |  _| . | |   |_ -|"));
    plugin.getLogger().log(Level.INFO, ColorAPI.process("|__|  |_|__,|_| |___|  |_|_|_|___|___|___|___|_|_|_|___|"));
    plugin.getLogger().log(Level.INFO, ColorAPI.process("__________________________________________ By Flares.dev"));
    plugin.getLogger().log(Level.INFO, ColorAPI.process("⬝ Disabling plugin"));
    plugin.getLogger().log(Level.INFO, ColorAPI.process("  ⬞ Plugin version: v" + plugin.getDescription().getVersion()));
    plugin.getLogger().log(Level.INFO, ColorAPI.process("  ⬞ Closing " + FilesManager.ACCESS.getConfig().getConfig().getString("storage_type.type") + " connections"));
    plugin.getLogger().log(Level.INFO, ColorAPI.process(""));
    plugin.getLogger().log(Level.INFO, ColorAPI.process("⬝ Thanks for using Flare Mobcoins."));

  }

  /**
   * Register all listener
   */
  private void registerListener() {
    final Listener[] listeners = new Listener[]{
        new BasicListener(), new ShopCommand()
    };

    Arrays.stream(listeners)
        .forEach(listener -> Bukkit.getPluginManager().registerEvents(listener, this.plugin));
  }

  private void usebStats() {
    if(FilesManager.ACCESS.getConfig().getConfig().getBoolean("allow_bstats")) {
      bStatsMetrics metrics = new bStatsMetrics(getPlugin(), 17684);
    }
  }

}
