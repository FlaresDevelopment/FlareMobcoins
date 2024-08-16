package net.flares.flaremobcoins.files;

import lombok.Getter;
import net.flarepowered.FlarePowered;
import net.flarepowered.core.data.yaml.YamlFile;
import net.flarepowered.core.text.Message;
import net.flares.flaremobcoins.FlareMobcoins;
import net.flares.flaremobcoins.MobcoinsByFlares;

import java.io.File;

@Getter
public enum FilesManager {
    ACCESS;

    private YamlFile data;
    private YamlFile config;
    private YamlFile drops;

    public void initialization() {
        if (!new File(MobcoinsByFlares.getPlugin(MobcoinsByFlares.class).getDataFolder(),"config.yml").exists()) {
            MobcoinsByFlares.getPlugin(MobcoinsByFlares.class).saveResource("shop/main.yml", false);
        }
        this.data = new YamlFile(MobcoinsByFlares.getPlugin(MobcoinsByFlares.class), "data/data.yml");
        this.config = new YamlFile(MobcoinsByFlares.getPlugin(MobcoinsByFlares.class), "config.yml");
        this.drops = new YamlFile(MobcoinsByFlares.getPlugin(MobcoinsByFlares.class), "drops.yml");
        loadConfig();
    }
    public void reload() {
        this.data.reloadConfig();
        this.config.reloadConfig();
        this.drops.reloadConfig();
    }
    private void loadConfig() {
        this.data.saveDefaultConfig();
        this.config.saveDefaultConfig();
        this.drops.saveDefaultConfig();
    }

    public void loadLocales() {
        String locale = config.getConfig().contains("select_locale") ? config.getConfig().getString("select_locale") : "en.yml";
        if(!new File(MobcoinsByFlares.getPlugin(MobcoinsByFlares.class).getDataFolder(), "locale/" + locale).exists()) {
            if(locale.equalsIgnoreCase("en.yml")) FlareMobcoins.PLUGIN.getPlugin().saveResource("locale/en.yml", false);
        }
        Message.loadLocale(new YamlFile(FlareMobcoins.PLUGIN.getPlugin(), "locale/" + locale));
    }

}
