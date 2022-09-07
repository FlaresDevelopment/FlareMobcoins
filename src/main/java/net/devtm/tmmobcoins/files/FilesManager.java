package net.devtm.tmmobcoins.files;

import lombok.Getter;
import net.devtm.tmmobcoins.files.files.*;
import net.devtm.tmmobcoins.TMMobCoinsPlugin;

@Getter
public enum FilesManager {
    ACCESS;

    private dataFile data;
    private LogsFile logs;
    private localeFile locale;
    private configFile config;
    private DropsFile drops;

    public void initialization() {
        this.data = new dataFile(TMMobCoinsPlugin.getPlugin(TMMobCoinsPlugin.class));
        this.locale = new localeFile(TMMobCoinsPlugin.getPlugin(TMMobCoinsPlugin.class));
        this.config = new configFile(TMMobCoinsPlugin.getPlugin(TMMobCoinsPlugin.class));
        this.drops = new DropsFile(TMMobCoinsPlugin.getPlugin(TMMobCoinsPlugin.class));
        this.logs = new LogsFile(TMMobCoinsPlugin.getPlugin(TMMobCoinsPlugin.class));
        loadConfig();
    }
    public void reload() {
        this.data.reloadConfig();
        this.config.reloadConfig();
        this.locale.reloadConfig();
        this.drops.reloadConfig();
    }
    private void loadConfig() {
        this.data.saveDefaultConfig();
        this.config.saveDefaultConfig();
        this.locale.saveDefaultConfig();
        this.drops.saveDefaultConfig();
    }
}
