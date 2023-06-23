package net.devtm.tmmobcoins.util;

import lombok.Getter;
import net.devtm.tmmobcoins.API.MobcoinsPlayer;
import net.devtm.tmmobcoins.TMMobCoins;
import net.devtm.tmmobcoins.files.FilesManager;
import net.devtm.tmmobcoins.service.ServiceHandler;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.UUID;

@Getter
public enum Utils {
  UTILS;

  private NumberFormat doubleFormat = new DecimalFormat("#0.00");
  long delayFactor = 0;
  HashMap<UUID, FlarePlayer> delayPlayers = new HashMap<>();

  public void reloadUtils() {
    delayFactor = FilesManager.ACCESS.getConfig().getConfig().getInt("storage_type.read_delay");
  }

  public Object getPlayer(String s) {
    if(Bukkit.getPlayer(s) != null) return Bukkit.getPlayer(s); else return Bukkit.getOfflinePlayer(s);
  }
  public UUID getPlayerUUID(String s) {
    if(Bukkit.getPlayer(s) != null) return Bukkit.getPlayer(s).getUniqueId(); else return Bukkit.getOfflinePlayer(s).getUniqueId();
  }
  public String getPlayerName(String s) {
    if(Bukkit.getPlayer(s) != null) return Bukkit.getPlayer(s).getName(); else return Bukkit.getOfflinePlayer(s).getName();
  }
  public String getPlayerName(UUID s) {
    if(Bukkit.getPlayer(s) != null) return Bukkit.getPlayer(s).getName(); else return Bukkit.getOfflinePlayer(s).getName();
  }
  public String findDifference(long start_date, long end_date) {
    long difference_In_Time = start_date - end_date;
    long difference_In_Seconds = (difference_In_Time / 1000) % 60;
    long difference_In_Minutes = (difference_In_Time / (1000 * 60)) % 60;
    long difference_In_Hours = (difference_In_Time / (1000 * 60 * 60)) % 24;
    long difference_In_Years = (difference_In_Time / (1000l * 60 * 60 * 24 * 365));
    long difference_In_Days = (difference_In_Time  / (1000 * 60 * 60 * 24))  % 365;
    return FilesManager.ACCESS.getConfig().getConfig().getString("rotating_shop_time_format").replace("%years%", difference_In_Years + "")
            .replace("%days%", difference_In_Days + "")
            .replace("%hours%", difference_In_Hours + "")
            .replace("%min%", difference_In_Minutes + "")
            .replace("%sec%", difference_In_Seconds + "");
  }

  public static FileConfiguration readConfig(String file) {
    return YamlConfiguration.loadConfiguration(new File(TMMobCoins.PLUGIN.getPlugin().getDataFolder(), file));
  }
  public static FileConfiguration readConfig(Path file) {
    return YamlConfiguration.loadConfiguration(new File(file.toString()));
  }
}
