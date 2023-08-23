package net.flares.flaremobcoins.service;

import lombok.Getter;
import net.flares.flaremobcoins.drops.EventService;

@Getter
public enum ServiceHandler {
    SERVICE;

    private MenuService menuService = new MenuService();
    private EventService eventService = new EventService();
    private DataService dataService = new DataService();

    public void onEnable() {
        dataService.updateTask();
    }

}
