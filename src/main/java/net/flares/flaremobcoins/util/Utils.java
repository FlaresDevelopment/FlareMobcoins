package net.flares.flaremobcoins.util;

import com.bgsoftware.wildstacker.api.WildStackerAPI;
import dev.rosewood.rosestacker.api.RoseStackerAPI;
import lombok.Getter;
import net.flarepowered.other.Logger;
import net.flarepowered.utils.objects.Pair;
import net.flares.flaremobcoins.FlareMobcoins;
import net.flares.flaremobcoins.files.FilesManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.entity.EntityDeathEvent;

import java.io.File;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
public enum Utils {
  UTILS;

  private final DecimalFormat twoDecimals = new DecimalFormat("#.00");

  public static final List<String> hostileMobs = Arrays.asList("BLAZE","CREEPER","DROWNED","ELDER_GUARDIAN","ENDERMITE","EVOKER","GHAST",
          "GIANT","GUARDIAN","HOGLIN","HUSK","ILLUSIONER","MAGMA_CUBE","PHANTOM","PIGLIN_BRUTE",
          "PIGLIN","PILLAGER","RAVAGER","SHULKER","SILVERFISH","SKELETON","SLIME","SPIDER",
          "STRAY","VEX","VINDICATOR","WITCH","WITHER","WITHER_SKELETON","ZOGLIN","ZOMBIE", "ZOMBIE_VILLAGER");

  public static int getTheStackMobs(EntityDeathEvent event) {
    if(FilesManager.ACCESS.getConfig().getConfig().getBoolean("enable_stackers")) return 1;
    try {
        if (Bukkit.getPluginManager().isPluginEnabled("RoseStacker")) {
            return RoseStackerAPI.getInstance().getStackedEntity(event.getEntity()).getStackSize();
        } else if (Bukkit.getPluginManager().isPluginEnabled("WildStacker")) {
        return WildStackerAPI.getStackedEntity(event.getEntity()).getStackAmount();
      }
    } catch (NullPointerException e) {
      Logger.warn("Failed to get a stacked entity the default of one will be used.");
    }
    return 1;
  }

  long delayFactor = 0;

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
    return FilesManager.ACCESS.getConfig().getConfig().getString("rotating_shop_time_format")
            .replace("%years%", String.valueOf((difference_In_Time / (1000l * 60 * 60 * 24 * 365))))
            .replace("%days%", String.valueOf((difference_In_Time  / (1000 * 60 * 60 * 24))  % 365))
            .replace("%hours%", String.valueOf((difference_In_Time / (1000 * 60 * 60)) % 24))
            .replace("%min%", String.valueOf((difference_In_Time / (1000 * 60)) % 60))
            .replace("%sec%", String.valueOf((difference_In_Time / 1000) % 60));
  }

  public double generateNumbers(String s) {
    Random rand = new Random();
    try {
      if(s.matches("(\\d+\\.?\\d*)")) {
        return Double.parseDouble(s);
      } else if (s.toLowerCase(Locale.ROOT).contains("random_number")) {
        Matcher matcher = Pattern.compile("random_number\\((.+),(.+)\\)").matcher(s);
        if (matcher.find()) {
          int num1 = Integer.parseInt(matcher.group(1));
          int num2 = Integer.parseInt(matcher.group(2));
          return rand.nextInt((num2 - num1) + 1) + num1;
        }
      } else if (s.toLowerCase(Locale.ROOT).contains("random_decimal")) {
        Matcher matcher = Pattern.compile("random_decimal\\((.+),(.+)\\)").matcher(s);
        if (matcher.find()) {
          double num1 = Double.parseDouble(matcher.group(1));
          double num2 = Double.parseDouble(matcher.group(2));
          return (rand.nextDouble() * (num2 - num1) + 1) + num1;
        }
      }
    } catch (Exception e) {
      Logger.error("We could not process the drop amount: '" + s + "', we are returning 0!");
      return 0;
    }
    return 0;
  }

  public Pair<String, String> getMinMax(String s) {
    if (s.toLowerCase(Locale.ROOT).contains("random_number")) {
      Matcher matcher = Pattern.compile("random_number\\((.+),(.+)\\)").matcher(s);
      if (matcher.find()) {
        return new Pair<>(matcher.group(1), matcher.group(2));
      }
    } else if (s.toLowerCase(Locale.ROOT).contains("random_decimal")) {
      Matcher matcher = Pattern.compile("random_decimal\\((.+),(.+)\\)").matcher(s);
      if (matcher.find()) {
        return new Pair<>(matcher.group(1), matcher.group(2));
      }
    }
    return null;
  }

  public static FileConfiguration readConfig(String file) {
    return YamlConfiguration.loadConfiguration(new File(FlareMobcoins.PLUGIN.getPlugin().getDataFolder(), file));
  }
  public static FileConfiguration readConfig(Path file) {
    return YamlConfiguration.loadConfiguration(new File(file.toString()));
  }

}
