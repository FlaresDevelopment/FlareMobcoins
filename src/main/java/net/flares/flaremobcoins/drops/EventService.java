package net.flares.flaremobcoins.drops;

import com.bgsoftware.wildstacker.api.WildStackerAPI;
import net.flares.flaremobcoins.FlareMobcoins;
import net.flares.flaremobcoins.files.FilesManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;
import org.bukkit.event.entity.EntityDeathEvent;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EventService {



    public int getTheStackMobs(EntityDeathEvent event) {
        try {
//            if (Bukkit.getPluginManager().isPluginEnabled("RoseStacker")) {
//                return RoseStackerAPI.getInstance().getStackedEntity(event.getEntity()).getStackSize();
//            } else
            if (Bukkit.getPluginManager().isPluginEnabled("WildStacker")) {
                return WildStackerAPI.getStackedEntity(event.getEntity()).getStackAmount();
            }
        } catch (NullPointerException e) {
            FlareMobcoins.PLUGIN.getPlugin().getLogger().log(Level.SEVERE, "Something happened when a player killed a stacked entity");
            FlareMobcoins.PLUGIN.getPlugin().getLogger().log(Level.SEVERE, "Try turning on the debug mode for logs");
            FlareMobcoins.PLUGIN.getPlugin().getLogger().log(Level.SEVERE, "Error message: " + e.getMessage());
            FlareMobcoins.PLUGIN.getPlugin().getLogger().log(Level.SEVERE, "Error code (for support): 012002");
        }
        return 1;
    }

    /**
     * This is used to determinte if we have any mobs that the player can kill!
     * @param event The EntityDeathEvent
     * @return the string to the drop path
     */
    public String mobVerify(EntityDeathEvent event) {
        if(FilesManager.ACCESS.getDrops().getConfig().contains("entity." + event.getEntity().getType())) {
            return "entity." + event.getEntity().getType();
        } else if(FilesManager.ACCESS.getDrops().getConfig().contains("entity.HOSTILE") && hostileMobs.contains(event.getEntity().getType().toString())) {
            return "entity.HOSTILE";
        } else if(FilesManager.ACCESS.getDrops().getConfig().contains("entity.PASSIVE") && !hostileMobs.contains(event.getEntity().getType().toString())) {
            return "entity.PASSIVE";
        } else {
            return null;
        }
    }

    public double generateNumber(@NotNull Configuration config, @NotNull String entityName) {
        final double defaultNumber = 0;
        // Get the type of value
        String dropValue = config.getString(entityName + ".drop_value");
        assert dropValue != null;
        Random rand = new Random();
        if (dropValue.toLowerCase(Locale.ROOT).contains("random_number")) {
            Matcher matcher = Pattern.compile("random_number\\((.+),(.+)\\)").matcher(dropValue);
            if (matcher.find()) {
                int num1 = Integer.parseInt(matcher.group(1));
                int num2 = Integer.parseInt(matcher.group(2));
                return rand.nextInt((num2 - num1) + 1) + num1;
            }
        } else if (dropValue.toLowerCase(Locale.ROOT).contains("random_decimal")) {
            Matcher matcher = Pattern.compile("random_decimal\\((.+),(.+)\\)").matcher(dropValue);
            if (matcher.find()) {
                double num1 = Double.parseDouble(matcher.group(1));
                double num2 = Double.parseDouble(matcher.group(2));
                return (rand.nextDouble() * (num2 - num1) + 1) + num1;
            }
        } else {
            return Double.parseDouble(dropValue);
        }
        return defaultNumber;
    }

    List<String> hostileMobs = Arrays.asList("BLAZE","CREEPER","DROWNED","ELDER_GUARDIAN","ENDERMITE","EVOKER","GHAST",
            "GIANT","GUARDIAN","HOGLIN","HUSK","ILLUSIONER","MAGMA_CUBE","PHANTOM","PIGLIN_BRUTE",
            "PIGLIN","PILLAGER","RAVAGER","SHULKER","SILVERFISH","SKELETON","SLIME","SPIDER",
            "STRAY","VEX","VINDICATOR","WITCH","WITHER","WITHER_SKELETON","ZOGLIN","ZOMBIE", "ZOMBIE_VILLAGER");
}
