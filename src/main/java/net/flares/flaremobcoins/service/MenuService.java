package net.flares.flaremobcoins.service;

import net.flarepowered.FlarePowered;
import net.flarepowered.core.TML.FlareScript;
import net.flarepowered.core.menus.objects.MenuInterface;
import net.flarepowered.core.menus.objects.MenuRender;
import net.flarepowered.core.menus.objects.items.FlareItem;
import net.flarepowered.other.Logger;
import net.flarepowered.other.exceptions.ItemBuilderConfigurationException;
import net.flares.flaremobcoins.files.FilesManager;
import net.flares.flaremobcoins.util.ShopStock;
import net.flares.flaremobcoins.util.StockProfile;
import net.flares.flaremobcoins.util.Utils;
import net.flares.flaremobcoins.FlareMobcoins;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;

import java.util.*;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MenuService {

    public void openMenu(Player player, String s) {
        FileConfiguration config = Utils.readConfig("shop/" + s + ".yml");
        if(config.contains("menu_permission"))
            if(!player.hasPermission(config.getString("menu_permission"))) return;
        if(config.contains("open_requirement")) {
            FlareScript flareScript = new FlareScript();
            if(!flareScript.processFull(Utils.readConfig("shops/" + s).getStringList("open_requirement"), player)) return;
        }
        MenuInterface menuInterface = new MenuInterface();
        if(config.contains("menu_type")) {
            menuInterface.inventoryType = InventoryType.valueOf(config.getString("menu_type"));
            menuInterface.menuSize = (byte) 0;
        } else {
            menuInterface.menuSize = (byte) config.getInt("size");
            menuInterface.inventoryType = InventoryType.CHEST;
        }
        menuInterface.title = config.getString("menu_title");
        for (String s1 : config.getConfigurationSection("items").getKeys(false)) {
            try {
                FlareItem item = new FlareItem().setConfig("shop/" + s + ".yml", "items." + s1).getObject();
                menuInterface.assignItem(item.page, item);
            } catch (ItemBuilderConfigurationException e) {
                Logger.error(e.getMessage());
            }
        }
        if(config.contains("rotating_shop")) createRotatingMenu(player, s, menuInterface);
        MenuRender mr = new MenuRender(player, menuInterface, true);
        if(!FlarePowered.LIB.getMenuManager().menusInRender.containsKey(player.getUniqueId()))
            FlarePowered.LIB.getMenuManager().menusInRender.put(player.getUniqueId(), mr);
        else
            FlarePowered.LIB.getMenuManager().menusInRender.replace(player.getUniqueId(), mr);
        mr.renderToPlayer();
//        menu.updateContent();
//        player.openInventory(menu.getInventory());
    }

    public void updateRotatingShop(String shopName) {
        FileConfiguration data = FilesManager.ACCESS.getData().getConfig();
        if (!data.contains("rotating_shop." + shopName)) initData(shopName);
        if(data.getLong("rotating_shop." + shopName + ".normal_last_time") <= System.currentTimeMillis()) {
            generateItems(shopName, 1);
            FileConfiguration shop = Utils.readConfig("shop/" + shopName + ".yml");
            FilesManager.ACCESS.getData().getConfig().set("rotating_shop." + shopName + ".normal_last_time", System.currentTimeMillis() + shop.getInt("rotating_shop.normal_refresh") * 1000L);
            FilesManager.ACCESS.getData().saveConfig();
            //closeAllShops();
        }
        if(data.getLong("rotating_shop." + shopName + ".premium_last_time") <= System.currentTimeMillis()) {
            generateItems(shopName, 2);
            FileConfiguration shop = Utils.readConfig("shop/" + shopName + ".yml");
            FilesManager.ACCESS.getData().getConfig().set("rotating_shop." + shopName + ".premium_last_time", System.currentTimeMillis() + shop.getInt("rotating_shop.premium_refresh") * 1000L);
            FilesManager.ACCESS.getData().saveConfig();
            //closeAllShops();
        }
    }

    private void createRotatingMenu(Player player, String shopName, MenuInterface menu) {
        updateRotatingShop(shopName);
        FileConfiguration shop = Utils.readConfig("shop/" + shopName + ".yml");
        for(String key : FilesManager.ACCESS.getData().getConfig().getConfigurationSection("rotating_shop." + shopName + ".items_normal").getKeys(false)) {
            try {
                FlareItem fi = new FlareItem().setConfig("shop/" + shopName + ".yml", "items." + key).getObject();
                fi.setSlot((byte) FilesManager.ACCESS.getData().getConfig().getInt("rotating_shop." + shopName + ".items_normal." + key + ".slot"));
                StockProfile stockProfile = ServiceHandler.SERVICE.getMenuService().getStock(player, shopName, key);
                List<String> newClickCommands = new ArrayList<>();
                if(fi.clickCommands != null)
                    for(String s : fi.clickCommands) {
                        Matcher matcher = Pattern.compile("(?i)\\[buy_from_stock\\((tokens|money|mobcoins)\\)] (\\d+\\.?\\d*)").matcher(s);
                        if(matcher.find()) {
                            newClickCommands.add("[buy_from_stock(" + matcher.group(1) + ")] " + matcher.group(2) + " rotating_shop_name=" + shopName + " rotating_shop_item=" + key);
                            continue;
                        }
                        newClickCommands.add(s);
                    }
                fi.clickCommands = newClickCommands;
                if(fi.lore != null)
                    fi.lore.replaceAll(line -> line.replace("%pl_stock%", "%pl_stock_" + shopName + "/" + key + "%")
                            .replace("%pl_max_stock%", "%pl_max_stock_" + shopName + "/" + key + "%"));
                if(fi.displayName != null) fi.displayName = fi.displayName.replace("%pl_stock%", "%pl_stock_" + shopName + "/" + key + "%")
                        .replace("%pl_max_stock%", "%pl_max_stock_" + shopName + "/" + key + "%");
                menu.assignItem(0, fi);
            } catch (ItemBuilderConfigurationException e) {
                FlareMobcoins.PLUGIN.getPlugin().getLogger().log(Level.SEVERE, e.getMessage());}
        }
        for(String key : FilesManager.ACCESS.getData().getConfig().getConfigurationSection("rotating_shop." + shopName + ".items_premium").getKeys(false)) {
            try {
                FlareItem fi = new FlareItem().setConfig("shop/" + shopName + ".yml", "items." + key).getObject();
                fi.setSlot((byte) FilesManager.ACCESS.getData().getConfig().getInt("rotating_shop." + shopName + ".items_premium." + key + ".slot"));
                StockProfile stockProfile = ServiceHandler.SERVICE.getMenuService().getStock(player, shopName, key);
                List<String> newClickCommands = new ArrayList<>();
                if(fi.clickCommands != null)
                    for(String s : fi.clickCommands) {
                        Matcher matcher = Pattern.compile("(?i)\\[buy_from_stock\\((tokens|money|mobcoins)\\)] (\\d+\\.?\\d*)").matcher(s);
                        if(matcher.find()) {
                            newClickCommands.add("[buy_from_stock(" + matcher.group(1) + ")] " + matcher.group(2) + " rotating_shop_name=" + shopName + " rotating_shop_item=" + key);
                            continue;
                        }
                        newClickCommands.add(s);
                    }
                fi.clickCommands = newClickCommands;
                if(fi.lore != null)
                    fi.lore.replaceAll(line -> line.replace("%pl_stock%", "%pl_stock_" + shopName + "/" + key + "%")
                            .replace("%pl_max_stock%", "%pl_max_stock_" + shopName + "/" + key + "%"));
                if(fi.displayName != null) fi.displayName = fi.displayName.replace("%pl_stock%", "%pl_stock_" + shopName + "/" + key + "%")
                        .replace("%pl_max_stock%", "%pl_max_stock_" + shopName + "/" + key + "%");
                menu.assignItem(0, fi);
            } catch (ItemBuilderConfigurationException e) {
                FlareMobcoins.PLUGIN.getPlugin().getLogger().log(Level.SEVERE, e.getMessage());}
        }
    }

    public StockProfile getStock(Player player, String shopName, String itemName) {
        /*
        * We will use this if statement to check if the item exists and if it's a normal or premium
        * */
        FileConfiguration data = FilesManager.ACCESS.getData().getConfig();
        if(!(FilesManager.ACCESS.getData().getConfig().contains("rotating_shop." + shopName + ".items_normal." + itemName) || FilesManager.ACCESS.getData().getConfig().contains("rotating_shop." + shopName + ".items_premium." + itemName)))
            return null;
        String stockType = FilesManager.ACCESS.getData().getConfig().contains("rotating_shop." + shopName + ".items_normal." + itemName) ? "items_normal" : "items_premium";
        switch (data.getString("rotating_shop." + shopName + "."+stockType+"." + itemName + ".stock_type")) {
            case "SERVER":
                int stockLeft = data.contains("rotating_shop." + shopName + "."+stockType+"." + itemName + ".data.server_stock") ?
                        data.getInt("rotating_shop." + shopName + "."+stockType+"." + itemName + ".data.server_stock")
                        : data.getInt("rotating_shop." + shopName + "."+stockType+"." + itemName + ".stock");
                return new StockProfile(stockLeft, ShopStock.StockType.valueOf(data.getString("rotating_shop." + shopName + "."+stockType+"." + itemName + ".stock_type")),
                        null, data.getInt("rotating_shop." + shopName + "."+stockType+"." + itemName + ".stock"));
            case "PLAYER":
                int stockLeftPlayer = data.contains("rotating_shop." + shopName + "."+stockType+"." + itemName + ".data." + player.getName()) ?
                        data.getInt("rotating_shop." + shopName + "."+stockType+"." + itemName + ".data." + player.getName())
                        : data.getInt("rotating_shop." + shopName + "."+stockType+"." + itemName + ".stock");
                return new StockProfile(stockLeftPlayer, ShopStock.StockType.valueOf(data.getString("rotating_shop." + shopName + "."+stockType+"." + itemName + ".stock_type")),
                        player, data.getInt("rotating_shop." + shopName + "."+stockType+"." + itemName + ".stock"));
        }
        return null;
    }

    /**
     * @return If this returns a true value this means there is stock and the purchase was complete, else it will return a false value because there is no stock
     */
    public boolean buyItem(Player player, String shopName, String itemName) {
        /*if(FilesManager.ACCESS.getData().getConfig().contains("rotating_shop." + shopName + ".items_normal." + itemName) || FilesManager.ACCESS.getData().getConfig().contains("rotating_shop." + shopName + ".items_premium." + itemName))
            return true;*/
        FileConfiguration data = FilesManager.ACCESS.getData().getConfig();
        StockProfile stockProfile = getStock(player, shopName, itemName);
        if(stockProfile == null) return true;

        if(stockProfile.getStock() > 0) {
            String stockType = FilesManager.ACCESS.getData().getConfig().contains("rotating_shop." + shopName + ".items_normal." + itemName) ? "items_normal" : "items_premium";
            switch (stockProfile.getType().toString()) {
                case "SERVER":
                    int stockLeft = data.contains("rotating_shop." + shopName + "."+stockType+"." + itemName + ".data.server_stock") ?
                            data.getInt("rotating_shop." + shopName + "."+stockType+"." + itemName + ".data.server_stock")
                            : data.getInt("rotating_shop." + shopName + "."+stockType+"." + itemName + ".stock");
                    data.set("rotating_shop." + shopName + "."+stockType+"." + itemName + ".data.server_stock", --stockLeft);
                    break;
                case "PLAYER":
                    int stockLeftPlayer = data.contains("rotating_shop." + shopName + "."+stockType+"." + itemName + ".data." + player.getName()) ?
                            data.getInt("rotating_shop." + shopName + "."+stockType+"." + itemName + ".data." + player.getName())
                            : data.getInt("rotating_shop." + shopName + "."+stockType+"." + itemName + ".stock");
                    data.set("rotating_shop." + shopName + "."+stockType+"." + itemName + ".data." + player.getName(), --stockLeftPlayer);
                    break;
            }
            FilesManager.ACCESS.getData().saveConfig();
        } else {
            return false;
        }
        return true;
    }

    /**
     * This method will generate the items for the shop
     * @param s the shop for witch this operation is done!
     * @param which part you want to generate (1 for normal; 2 for premium; 3 for all)
     */
    private void generateItems(String s, int which) {
        FileConfiguration shop = Utils.readConfig("shop/" + s + ".yml");
        if((which == 1 || which == 3)) FilesManager.ACCESS.getData().getConfig().set("rotating_shop." + s + ".items_normal", null);
        if((which == 2 || which == 3)) FilesManager.ACCESS.getData().getConfig().set("rotating_shop." + s + ".items_premium", null);
        // Add here a check if the items are more then the slots
        List<Integer> normalSlots = shop.getIntegerList("rotating_shop.normal_slots");
        List<Integer> premiumSlots = shop.getIntegerList("rotating_shop.premium_slots");
        List<String> normalItems = new ArrayList<>(shop.getConfigurationSection("rotating_shop.normal_items").getKeys(false));
        List<String> premiumItems = new ArrayList<>(shop.getConfigurationSection("rotating_shop.premium_items").getKeys(false));
        // Generator for normal items
        while (normalSlots.size() != 0 && (which == 1 || which == 3)) {
            int itemId = new Random().nextInt(normalItems.size());
            int slotId = new Random().nextInt(normalSlots.size());
            ShopStock stock = new ShopStock();
            Matcher matcher = Pattern.compile("stock\\((server|player)\\,(\\d+)\\)").matcher(shop.getString("rotating_shop.normal_items." + normalItems.get(itemId)));
            matcher.find();
            stock.setType(ShopStock.StockType.valueOf(matcher.group(1).toUpperCase(Locale.ROOT)));
            stock.setStock(Integer.parseInt(matcher.group(2)));
            FilesManager.ACCESS.getData().getConfig().set("rotating_shop." + s + ".items_normal." + normalItems.get(itemId) + ".slot", normalSlots.get(slotId));
            FilesManager.ACCESS.getData().getConfig().set("rotating_shop." + s + ".items_normal." + normalItems.get(itemId) + ".stock", stock.getStock());
            FilesManager.ACCESS.getData().getConfig().set("rotating_shop." + s + ".items_normal." + normalItems.get(itemId) + ".stock_type", stock.getType().toString());
            normalItems.remove(itemId);
            normalSlots.remove(slotId);
        }
        // Generator for premium items
        while(premiumSlots.size() != 0 && (which == 2 || which == 3)) {
            int itemId = new Random().nextInt(premiumItems.size());
            int slotId = new Random().nextInt(premiumSlots.size());
            ShopStock stock = new ShopStock();Matcher matcher = Pattern.compile("stock\\((server|player)\\,(\\d+)\\)").matcher(shop.getString("rotating_shop.premium_items." + premiumItems.get(itemId)));
            matcher.find();stock.setType(ShopStock.StockType.valueOf(matcher.group(1).toUpperCase(Locale.ROOT)));stock.setStock(Integer.parseInt(matcher.group(2)));
            FilesManager.ACCESS.getData().getConfig().set("rotating_shop." + s + ".items_premium." + premiumItems.get(itemId) + ".slot", premiumSlots.get(slotId));
            FilesManager.ACCESS.getData().getConfig().set("rotating_shop." + s + ".items_premium." + premiumItems.get(itemId) + ".stock", stock.getStock());
            FilesManager.ACCESS.getData().getConfig().set("rotating_shop." + s + ".items_premium." + premiumItems.get(itemId) + ".stock_type", stock.getType().toString());
            premiumItems.remove(itemId);
            premiumSlots.remove(slotId);
        }
        FilesManager.ACCESS.getData().saveConfig();
    }

    private void initData(String s) {
        FileConfiguration shop = Utils.readConfig("shop/" + s + ".yml");
        FilesManager.ACCESS.getData().getConfig().set("rotating_shop." + s + ".normal_last_time", System.currentTimeMillis() + shop.getInt("rotating_shop.normal_refresh") * 1000L);
        FilesManager.ACCESS.getData().getConfig().set("rotating_shop." + s + ".premium_last_time", System.currentTimeMillis() + shop.getInt("rotating_shop.premium_refresh") * 1000L);
        FilesManager.ACCESS.getData().saveConfig();
        generateItems(s, 3);
    }
}
