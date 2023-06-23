package net.devtm.tmmobcoins.files;

import lombok.Getter;
import net.flares.lib.database.json.JsonFile;
import net.flares.lib.database.yaml.YamlFile;
import net.devtm.tmmobcoins.TMMobCoinsPlugin;

import java.io.File;

@Getter
public enum FilesManager {
    ACCESS;

    private YamlFile data;
    private YamlFile logs;
    private YamlFile locale;
    private YamlFile config;
    private YamlFile drops;
    private JsonFile jsonData;

    public void initialization() {
        if (!new File(TMMobCoinsPlugin.getPlugin(TMMobCoinsPlugin.class).getDataFolder(),"config.yml").exists()) {
            TMMobCoinsPlugin.getPlugin(TMMobCoinsPlugin.class).saveResource("shop/main.yml", false);
        }
        this.data = new YamlFile(TMMobCoinsPlugin.getPlugin(TMMobCoinsPlugin.class), "data/data.yml");
        this.locale = new YamlFile(TMMobCoinsPlugin.getPlugin(TMMobCoinsPlugin.class), "locale/en.yml");
        this.config = new YamlFile(TMMobCoinsPlugin.getPlugin(TMMobCoinsPlugin.class), "config.yml");
        this.drops = new YamlFile(TMMobCoinsPlugin.getPlugin(TMMobCoinsPlugin.class), "drops.yml");
        this.logs = new YamlFile(TMMobCoinsPlugin.getPlugin(TMMobCoinsPlugin.class), "data/logs.yml");
        //this.jsonData = new JsonFile(new File(TMMobCoinsPlugin.getPlugin(TMMobCoinsPlugin.class).getDataFolder(), "test.json"));
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
