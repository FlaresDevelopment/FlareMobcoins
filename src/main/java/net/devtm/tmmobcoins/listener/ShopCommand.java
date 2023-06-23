package net.devtm.tmmobcoins.listener;

import net.devtm.tmmobcoins.files.FilesManager;
import net.devtm.tmmobcoins.service.ServiceHandler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
public class ShopCommand implements Listener {

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        String[] array = event.getMessage().replaceFirst("/", "").split(" ");
            if(FilesManager.ACCESS.getConfig().getConfig().getString("shop.open_command").equalsIgnoreCase(array[0])) {
                ServiceHandler.SERVICE.getMenuService()
                        .openMenu(event.getPlayer(), FilesManager.ACCESS.getConfig().getConfig().getString("shop.main_shop"));
                event.setCancelled(true);
            }
    }
}
