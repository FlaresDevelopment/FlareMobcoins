package net.flares.flaremobcoins.service.menu;

import lombok.Getter;
import net.flarepowered.core.text.other.Replace;
import net.flares.flaremobcoins.FlareMobcoins;
import net.flares.flaremobcoins.exceptions.MenuOpenException;
import net.flares.flaremobcoins.util.FileWrapper;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.stream.Stream;

@Getter

public class MenuService implements Listener {

    private HashMap<String, MemoryMenu> menus;
    private HashMap<Player, String> inMenus = new HashMap<>();

    public void onEnable() {
        menus = new HashMap<>();
        Bukkit.getPluginManager().registerEvents(this, FlareMobcoins.PLUGIN.getPlugin());
        regen();
    }

    public void onReload() {
        regen();
    }

    public void openMenu(Player player, String s, Replace... replaces) throws MenuOpenException {
        if(player == null) throw new MenuOpenException("player_is_null");
        if(!menus.containsKey(s)) throw new MenuOpenException("no_such_menu");
        MemoryMenu menu = menus.get(s);
        if(!menu.checkRequirements(player, replaces)) throw new MenuOpenException("not_meet_requirements");
        if(!menu.checkPermission(player, replaces)) throw new MenuOpenException("no_permission");
        menu.showMenu(player, replaces);
    }

    private void regen() {
        menus.clear();
        try (Stream<Path> paths = Files.walk(Paths.get(FlareMobcoins.PLUGIN.getPlugin().getDataFolder().getPath() + "/shop"))) {
            paths.filter(Files::isRegularFile).forEach(file -> {
                if(file.getFileName().toString().contains("yml")) {
                    MemoryMenu menu = new MemoryMenu();
                    File menuFile = file.toFile();
                    FileConfiguration config = FileWrapper.wrapConfiguration(menuFile);
                    String menuName = file.getFileName().toString().replace(".yml", "");
                    menu.setFile(menuFile);
                    menu.setId(menuName);
                    menus.put(menuName, menu);
                    if(config.contains("rotating_shop")) menu.setRotatingMenu(new RotatingShop(menuName));
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    private void onInventoryClose(InventoryCloseEvent event) {
        inMenus.remove((Player) event.getPlayer());
    }

}
