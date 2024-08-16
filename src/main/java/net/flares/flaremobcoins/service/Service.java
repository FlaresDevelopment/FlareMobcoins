package net.flares.flaremobcoins.service;

import lombok.Getter;
import net.flares.flaremobcoins.FlareMobcoins;
import net.flares.flaremobcoins.listener.DisplayCoinService;
import net.flares.flaremobcoins.service.menu.MenuService;
import org.bukkit.Bukkit;

@Getter
public enum Service {
    SERVICE;

    private final MenuService menuService = new MenuService();
    private final EntityService entityService = new EntityService();
    private final DataService dataService = new DataService();
    private final DisplayCoinService displayCoinService = new DisplayCoinService();

    public void onEnable() {
        dataService.updateTask();
        displayCoinService.onEnable();
        entityService.onReload();
        menuService.onEnable();
        Bukkit.getServer().getPluginManager().registerEvents(entityService, FlareMobcoins.PLUGIN.getPlugin());
    }

    public void onReload() {
        menuService.onReload();
        dataService.reloadDataService();
        entityService.onReload();
        displayCoinService.onEnable();
    }

}
