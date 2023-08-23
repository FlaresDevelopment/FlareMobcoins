package net.flares.flaremobcoins;

import lombok.Getter;
import net.flarepowered.FlarePowered;
import net.flarepowered.core.text.ColorUtils;
import net.flarepowered.core.text.StringUtils;
import net.flares.flaremobcoins.command.MobcoinsCommand;
import net.flares.flaremobcoins.files.FilesManager;
import net.flares.flaremobcoins.drops.DropsListener;
import net.flares.flaremobcoins.listener.ShopCommand;
import net.flares.flaremobcoins.service.ServiceHandler;
import net.flares.flaremobcoins.util.PlaceholderAPI;
import net.flares.flaremobcoins.util.PlaceholdersClass;
import net.flares.flaremobcoins.util.Utils;
import net.flares.flaremobcoins.util.bStats;
import net.flares.flaremobcoins.util.components.BuyFromStockComponent;
import net.flares.flaremobcoins.util.components.MobcoinsComponent;
import net.flares.flaremobcoins.util.components.OpenMenuComponent;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Arrays;
import java.util.logging.Level;

@Getter
public enum FlareMobcoins {
  PLUGIN;
  private MobcoinsByFlares plugin;

  private final boolean enabledMenu = true;

  public void start(final MobcoinsByFlares plugin) {
    FilesManager.ACCESS.initialization();
    FlarePowered.LIB.useLib(plugin);
    FlarePowered.LIB.addNewPlaceholder(new PlaceholdersClass());
    StringUtils.loadLang(new File(plugin.getDataFolder(), "locale").toPath());
    FlarePowered.LIB.getTMLObject().addComponent(new MobcoinsComponent(), new BuyFromStockComponent(), new OpenMenuComponent());
    FlarePowered.LIB.enableMenus();
    this.plugin = plugin;
    assert plugin != null : "Something went wrong! Plugin was null.";
    this.init();
    startLog();
    usebStats();
    commandsSetup();
    ServiceHandler.SERVICE.getDataService().reloadDataService();
    Utils.UTILS.reloadUtils();
  }

  /**
   * Stop method for the plugin - {@link JavaPlugin}
   *
   * @param plugin the plugin instance
   */
  public void stop(final MobcoinsByFlares plugin) {
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
  }

  public void commandsSetup() {
    plugin.getCommand("mobcoins").setExecutor(new MobcoinsCommand());
  }

  private void startLog() {
    plugin.getLogger().log(Level.INFO, ColorUtils.process("  ___ _              __  __     _            _         "));
    plugin.getLogger().log(Level.INFO, ColorUtils.process(" | __| |__ _ _ _ ___|  \\/  |___| |__  __ ___(_)_ _  ___"));
    plugin.getLogger().log(Level.INFO, ColorUtils.process(" | _|| / _` | '_/ -_) |\\/| / _ \\ '_ \\/ _/ _ \\ | ' \\(_-<"));
    plugin.getLogger().log(Level.INFO, ColorUtils.process(" |_| |_\\__,_|_| \\___|_|  |_\\___/_.__/\\__\\___/_|_||_/__/"));
    plugin.getLogger().log(Level.INFO, ColorUtils.process("______________________________________________ By Flares.dev"));
    plugin.getLogger().log(Level.INFO, ColorUtils.process("- Loading plugin"));
    plugin.getLogger().log(Level.INFO, ColorUtils.process("  + Plugin version: v" + plugin.getDescription().getVersion()));
    plugin.getLogger().log(Level.INFO, ColorUtils.process("  + Using " + FilesManager.ACCESS.getConfig().getConfig().getString("storage.type") + " for data saving"));
    plugin.getLogger().log(Level.INFO, ColorUtils.process(""));
    plugin.getLogger().log(Level.INFO, ColorUtils.process("- Getting dependencies"));
    plugin.getLogger().log(Level.INFO, ColorUtils.process("  + PlaceholderAPI - " + (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null ? "not available" : "enabled")));
    plugin.getLogger().log(Level.INFO, ColorUtils.process("  + Vault - " + (Bukkit.getPluginManager().getPlugin("Vault") == null ? "not available" : "enabled")));
    plugin.getLogger().log(Level.INFO, ColorUtils.process(""));
    plugin.getLogger().log(Level.INFO, ColorUtils.process("- Need support or want to report a bug join our discord (discord.flares.dev)"));
  }

  private void stopLog() {
    plugin.getLogger().log(Level.INFO, ColorUtils.process("  ___ _              __  __     _            _         "));
    plugin.getLogger().log(Level.INFO, ColorUtils.process(" | __| |__ _ _ _ ___|  \\/  |___| |__  __ ___(_)_ _  ___"));
    plugin.getLogger().log(Level.INFO, ColorUtils.process(" | _|| / _` | '_/ -_) |\\/| / _ \\ '_ \\/ _/ _ \\ | ' \\(_-<"));
    plugin.getLogger().log(Level.INFO, ColorUtils.process(" |_| |_\\__,_|_| \\___|_|  |_\\___/_.__/\\__\\___/_|_||_/__/"));
    plugin.getLogger().log(Level.INFO, ColorUtils.process("______________________________________________ By Flares.dev"));
    plugin.getLogger().log(Level.INFO, ColorUtils.process("- Disabling plugin"));
    plugin.getLogger().log(Level.INFO, ColorUtils.process("  + Plugin version: v" + plugin.getDescription().getVersion()));
    plugin.getLogger().log(Level.INFO, ColorUtils.process("  + Closing " + FilesManager.ACCESS.getConfig().getConfig().getString("storage_type.type") + " connections"));
    plugin.getLogger().log(Level.INFO, ColorUtils.process(""));
    plugin.getLogger().log(Level.INFO, ColorUtils.process("- Thanks for using Flare Mobcoins."));

  }

  /**
   * Register all listener
   */
  private void registerListener() {
    final Listener[] listeners = new Listener[]{
        new DropsListener(), new ShopCommand()
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
