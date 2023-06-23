package net.devtm.tmmobcoins.service;

import net.devtm.tmmobcoins.TMMobCoins;
import net.devtm.tmmobcoins.files.FilesManager;
import net.devtm.tmmobcoins.util.Utils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.entity.EntityDeathEvent;

import java.io.File;
import java.util.Arrays;
import java.util.logging.Level;

public class LoggerService {

    public void fileSetup() {
        File f = new File(TMMobCoins.PLUGIN.getPlugin().getDataFolder(), "data/logs.yml");
        f.delete();
        FilesManager.ACCESS.getLogs().saveDefaultConfig();
    }

    public void log(Level level, Exception e, String s) {
        TMMobCoins.PLUGIN.getPlugin().getLogger().log(level,s);
        FilesManager.ACCESS.getLogs().getConfig().set(s.replace(" ", "_") + "TIME:" + System.currentTimeMillis(), Arrays.toString(e.getStackTrace()));
        FilesManager.ACCESS.getLogs().getConfig().set(s.replace(" ", "_") + "TIME:" + System.currentTimeMillis(), e.getCause());

        FilesManager.ACCESS.getLogs().saveConfig();
    }

    public void log(Level level, String s) {
        TMMobCoins.PLUGIN.getPlugin().getLogger().log(level,s);
        FilesManager.ACCESS.getLogs().getConfig().set(s.replace(" ", "_") + "TIME:" + System.currentTimeMillis(), s);
        FilesManager.ACCESS.getLogs().saveConfig();
    }

}
