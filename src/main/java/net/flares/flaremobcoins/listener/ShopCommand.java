package net.flares.flaremobcoins.listener;

import net.flares.flaremobcoins.exceptions.MenuOpenException;
import net.flares.flaremobcoins.files.FilesManager;
import net.flares.flaremobcoins.service.Service;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
public class ShopCommand implements Listener {

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        String[] array = event.getMessage().replaceFirst("/", "").split(" ");
            if(FilesManager.ACCESS.getConfig().getConfig().getString("shop.open_command").equalsIgnoreCase(array[0])) {
                try {
                    Service.SERVICE.getMenuService()
                            .openMenu(event.getPlayer(), FilesManager.ACCESS.getConfig().getConfig().getString("shop.main_shop"));
                } catch (MenuOpenException e) {
                    throw new RuntimeException(e);
                }
                event.setCancelled(true);
            }
    }
}
