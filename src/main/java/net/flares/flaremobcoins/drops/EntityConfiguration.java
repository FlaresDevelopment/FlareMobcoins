package net.flares.flaremobcoins.drops;

import lombok.Getter;
import lombok.Setter;
import net.flarepowered.other.Logger;
import net.flares.flaremobcoins.util.Utils;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Collections;
import java.util.List;
import java.util.Random;

@Getter
@Setter
public class EntityConfiguration {

    private EntityGroup entityGroup;
    private List<String> entities;
    private List<String> requirement;
    private List<String> dropRun;
    private String dropValue;
    private Byte dropChance = 100;
    private String configPath;


    public static EntityConfiguration wrapFromConfig(ConfigurationSection section) {
        EntityConfiguration dropsConfiguration = new EntityConfiguration();
        dropsConfiguration.configPath = section.getCurrentPath();
        for(String key : section.getKeys(true)) {
            switch (key) {
                case "drop_value":
                    dropsConfiguration.dropValue = section.getString(key);
                    break;
                case "drop_action":
                    dropsConfiguration.dropRun = section.getStringList(key);
                    break;
                case "drop_chance":
                    dropsConfiguration.dropChance = (byte) section.getInt(key);
                    break;
                case "requirement":
                    dropsConfiguration.requirement = Collections.singletonList(section.getString(key));
                    break;
                case "requirement_list":
                case "requirements":
                    dropsConfiguration.requirement = section.getStringList("requirements");
                    break;
                case "entities":
                    dropsConfiguration.entities = section.getStringList(key);
                    dropsConfiguration.entityGroup = EntityGroup.LIST;
                    break;
                case "entity":
                    try {
                        dropsConfiguration.entityGroup = EntityGroup.valueOf(section.getString(key));
                    } catch (IllegalArgumentException e) {
                        dropsConfiguration.entities = Collections.singletonList(section.getString(key));
                        dropsConfiguration.entityGroup = EntityGroup.LIST;
                    }
                    break;

            }
        }
        return dropsConfiguration;
    }

    public boolean checkForChance() {
        if(dropChance >= 100) return true;
        return new Random().nextInt(100) < dropChance;
    }

    public boolean checkForMob(String type) {
        if(entityGroup == null) { Logger.error("The entities for the drop " + configPath + " is null! Please check our wiki for help!"); return false; };
        switch (entityGroup) {
            case ALL:
                return true;
            case HOSTILE:
                return Utils.hostileMobs.contains(type);
            case PASSIVE:
                return !Utils.hostileMobs.contains(type);
            case LIST:
                return entities.contains(type);
        }
        return false;
    }

    public enum EntityGroup {
        LIST,
        HOSTILE,
        PASSIVE,
        ALL
    }

}
