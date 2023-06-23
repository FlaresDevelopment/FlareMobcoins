package net.devtm.tmmobcoins.service;

import lombok.Getter;

@Getter
public enum ServiceHandler {
    SERVICE;

    private MenuService menuService = new MenuService();
    private EventService eventService = new EventService();
    private LoggerService loggerService = new LoggerService();
    private DataService dataService = new DataService();

    public void onEnable() {
        dataService.updateTask();
    }

}
