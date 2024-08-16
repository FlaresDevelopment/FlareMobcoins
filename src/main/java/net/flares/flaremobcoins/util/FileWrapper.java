package net.flares.flaremobcoins.util;

import net.flares.flaremobcoins.FlareMobcoins;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.nio.file.Path;

public class FileWrapper {

    public static FileConfiguration wrapConfiguration(String file) {
        return YamlConfiguration.loadConfiguration(new File(FlareMobcoins.PLUGIN.getPlugin().getDataFolder(), file));
    }
    public static FileConfiguration wrapConfiguration(Path file) {
        return YamlConfiguration.loadConfiguration(new File(file.toString()));
    }

    public static FileConfiguration wrapConfiguration(File file) {
        return YamlConfiguration.loadConfiguration(file);
    }
}
