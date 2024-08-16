package net.flares.flaremobcoins;

import lombok.Getter;
import net.flarepowered.FlarePowered;
import net.flarepowered.other.Logger;
import net.flares.flaremobcoins.command.MobcoinsCommand;
import net.flares.flaremobcoins.files.FilesManager;
import net.flares.flaremobcoins.listener.ShopCommand;
import net.flares.flaremobcoins.service.Service;
import net.flares.flaremobcoins.util.PlaceholderAPI;
import net.flares.flaremobcoins.util.PlaceholdersClass;
import net.flares.flaremobcoins.util.Utils;
import net.flares.flaremobcoins.util.bStats;
import net.flares.flaremobcoins.util.components.BuyComponent;
import net.flares.flaremobcoins.util.components.MobcoinsComponent;
import net.flares.flaremobcoins.util.components.OpenMenuComponent;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

@Getter
public enum FlareMobcoins {
  PLUGIN;
  private MobcoinsByFlares plugin;

  private final boolean enabledMenu = true;
  private PlaceholderAPI papi;

  public void start(final MobcoinsByFlares plugin) {
    this.plugin = plugin;
    assert plugin != null : "Something went wrong! Plugin was null.";
    FilesManager.ACCESS.initialization();
    FlarePowered.LIB.useLib(plugin);
    FilesManager.ACCESS.loadLocales();
    FlarePowered.LIB.addNewPlaceholder(new PlaceholdersClass());
    FlarePowered.LIB.getTMLObject().addComponent(new MobcoinsComponent(), new BuyComponent(), new OpenMenuComponent());
    FlarePowered.LIB.enableMenus();
    this.init();
    startLog();
    usebStats();
    commandsSetup();
    Service.SERVICE.onEnable();
    Service.SERVICE.getDataService().reloadDataService();
    Utils.UTILS.reloadUtils();
  }

  /**
   * Stop method for the plugin - {@link JavaPlugin}
   *
   * @param plugin the plugin instance
   */
  public void stop(final MobcoinsByFlares plugin) {
    this.plugin = plugin;
    Service.SERVICE.getDataService().setMobcoins(null, 0);
    papi.unregister();
    stopLog();
  }

  /**
   * Initialize everything
   */
  private void init() {
    this.registerListener();
    if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
      papi = new PlaceholderAPI();
      papi.register();
    }
  }

  public void commandsSetup() {
    plugin.getCommand("mobcoins").setExecutor(new MobcoinsCommand());
  }

  private void startLog() {
    Logger.info("         ∘₊✧────────────────────────✧₊∘       ");
    Logger.info("            ░█▀▀▀ █── █▀▀█ █▀▀█ █▀▀ ");
    Logger.info("            ░█▀▀▀ █── █▄▄█ █▄▄▀ █▀▀ ");
    Logger.info("            ░█─── ▀▀▀ ▀──▀ ▀─▀▀ ▀▀▀ ");
    Logger.info("─────────────────── Mobcoins ───────────────────");
    Logger.info(" › Loading FlareMobcoins v" + plugin.getDescription().getVersion());
    Logger.info(" › Saving players data into " + FilesManager.ACCESS.getConfig().getConfig().getString("storage.type"));
    Logger.info(" › Starting threads...");
    Logger.info(" › Sit back and relax while its loading..");
    Logger.info(" › Made with love in Romania by flares.dev");
    Logger.info("────────────────────────────────────────────────");
    Logger.info("For support please access our discord server! (discord.flares.dev)");
  }

  private void stopLog() {
    Logger.info("──────────────── FlareMobcoins ────────────────");
    Logger.info(" › Closing FlareMobcoins v" + plugin.getDescription().getVersion());
    Logger.info(" › Saving players data into " + FilesManager.ACCESS.getConfig().getConfig().getString("storage.type"));
    Logger.info(" › Sit back and relax while its closing..");
    Logger.info(" › Made with love in Romania by flares.dev");
    Logger.info("────────────────────────────────────────────────");

  }

  /**
   * Register all listener
   */
  private void registerListener() {
    final Listener[] listeners = new Listener[]{
        new ShopCommand()
    };

    Arrays.stream(listeners)
        .forEach(listener -> Bukkit.getPluginManager().registerEvents(listener, this.plugin));
  }

  private void usebStats() {
    if(FilesManager.ACCESS.getConfig().getConfig().getBoolean("allow_bstats")) {
      bStats metrics = new bStats(getPlugin(), 17684);
    }
  }

}
