package net.flares.flaremobcoins.service.menu;

import net.flarepowered.core.TML.FlareScript;
import net.flarepowered.core.text.Message;
import net.flarepowered.core.text.other.Replace;
import net.flarepowered.neo.ui.contents.UI;
import net.flarepowered.neo.ui.items.FlareStack;
import net.flares.flaremobcoins.FlareMobcoins;
import net.flares.flaremobcoins.files.FilesManager;
import net.flares.flaremobcoins.service.Service;
import net.flares.flaremobcoins.util.FileWrapper;
import net.flares.flaremobcoins.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class RotatingShop {

    private String id;
    private FileConfiguration config;

    private long lastNormalRefesh;
    private long lastPremiumRefesh;
    private HashMap<Integer, RotatingItem> normalLoaded = new HashMap<>();
    private HashMap<Integer, RotatingItem> premiumLoaded = new HashMap<>();

    private List<String> normalItems = new ArrayList<>();
    private List<String> premiumItems = new ArrayList<>();
    private List<Integer> defaultNormalSlots = new ArrayList<>();
    private List<Integer> defaultPremiumSlots = new ArrayList<>();
    private long normalRefreshTime;
    private long premiumRefreshTime;

    public RotatingShop(String id) {
        this.id = id;
        this.config = FileWrapper.wrapConfiguration(Service.SERVICE.getMenuService().getMenus().get(id).getFile());
        loadDataInMemory();
        loadShopInMemory();
    }

    public void injectIntoUI(UI ui) {
        // Nicest process out here :)
        // Checking and updating
        //updateData();
        validate();
        normalLoaded.forEach((slot, item) -> {
            FlareStack fs = item.getStack();
            fs.setItemReplaces(new ArrayList<>());
            fs.getItemReplaces().add(new Replace("%pl_max_stock%", item.getMaxStock() + ""));
            fs.getItemReplaces().add(new Replace("%pl_stock%", item.getStock() + ""));
            fs.getClickCommands().forEach(pair -> {
                if(pair.first.contains("[buy"))
                    pair.first = pair.first + " in_rotating_shop='" + slot + "'";
            });
            ui.getContent().get(0).addItemToDock(slot, fs);
        });
        premiumLoaded.forEach((slot, item) -> {
            FlareStack fs = item.getStack();
            fs.setItemReplaces(new ArrayList<>());
            fs.getItemReplaces().add(new Replace("%pl_max_stock%", item.getMaxStock() + ""));
            fs.getItemReplaces().add(new Replace("%pl_stock%", item.getStock() + ""));
            fs.getClickCommands().forEach(pair -> {
                if(pair.first.contains("[buy"))
                    pair.first = pair.first + " in_rotating_shop='" + slot + "'";
            });
            ui.getContent().get(0).addItemToDock(slot, fs);
        });
        // Inject items
        // Inject replaces
    }

    public boolean buyItem(int slot) {
        if(normalLoaded.containsKey(slot)) {
            if(normalLoaded.get(slot).getStock() <= 0) return false;
            normalLoaded.get(slot).updateStock(normalLoaded.get(slot).getStock() - 1, this.id, "normal");
            normalLoaded.get(slot).getStack().getItemReplaces().get(1).to = normalLoaded.get(slot).getStock() + "";
        } else if(premiumLoaded.containsKey(slot)) {
            if(premiumLoaded.get(slot).getStock() <= 0) return false;
            premiumLoaded.get(slot).updateStock(premiumLoaded.get(slot).getStock() - 1, this.id, "premium");
            premiumLoaded.get(slot).getStack().getItemReplaces().get(1).to = premiumLoaded.get(slot).getStock() + "";
        }
        return true;
    }

    public String getNormalTimeLeft() {
        validate();
        return Utils.UTILS.findDifference(lastNormalRefesh + (normalRefreshTime*1000), System.currentTimeMillis());
    }

    public String getPremiumTimeLeft() {
        validate();
        return Utils.UTILS.findDifference(lastPremiumRefesh + (premiumRefreshTime *1000), System.currentTimeMillis());
    }

    private void validate() {
        if((lastNormalRefesh + (normalRefreshTime*1000)) - System.currentTimeMillis() <= 0) {
            if(!Message.getLocale().getString("shop.rotating_shop_reset.normal").equalsIgnoreCase("disabled"))
                Bukkit.broadcastMessage(Message.formatFromLocale("shop.rotating_shop_reset.normal", null, new Replace("%pl_shop_name%", id)));
            new BukkitRunnable() {
                @Override
                public void run() {
                    for (Map.Entry<Player, String> playerStringEntry : Service.SERVICE.getMenuService().getInMenus().entrySet()) {
                        playerStringEntry.getKey().closeInventory();
                    }
                }
            }.runTask(FlareMobcoins.PLUGIN.getPlugin());
            buildNormalShop(FilesManager.ACCESS.getData().getConfig());
        }
        if((lastPremiumRefesh + (premiumRefreshTime*1000)) - System.currentTimeMillis() <= 0) {
            if(!Message.getLocale().getString("shop.rotating_shop_reset.premium").equalsIgnoreCase("disabled"))
                Bukkit.broadcastMessage(Message.formatFromLocale("shop.rotating_shop_reset.premium", null, new Replace("%pl_shop_name%", id)));
            new BukkitRunnable() {
                @Override
                public void run() {
                    for (Map.Entry<Player, String> playerStringEntry : Service.SERVICE.getMenuService().getInMenus().entrySet()) {
                        playerStringEntry.getKey().closeInventory();
                    }
                }
            }.runTask(FlareMobcoins.PLUGIN.getPlugin());
            buildPremiumShop(FilesManager.ACCESS.getData().getConfig());
        }
    }

    private void loadShopInMemory() {
        FileConfiguration data = FilesManager.ACCESS.getData().getConfig();
        if(data.contains("rotating_shop." + id)) {
            // normal
            if(data.contains("rotating_shop." + id + ".last_normal_refresh")) {
                lastNormalRefesh = data.getLong("rotating_shop." + id + ".last_normal_refresh");
                if((lastNormalRefesh + (normalRefreshTime*1000)) - System.currentTimeMillis() <= 0) buildNormalShop(data);
                else {
                    ConfigurationSection section = data.getConfigurationSection("rotating_shop." + id + ".normal_items");
                    System.out.println(section.toString());
                    for(String key : section.getKeys(false)) {
                        normalLoaded.put(section.getInt(key + ".slot"), new RotatingItem(
                                RotatingItem.StockType.valueOf(section.getString(key + ".stock_type")),
                                config.getInt("items." + key + ".rotating_shop.stock_amount"),
                                section.getInt(key + ".stock"),
                                FlareStack.wrap(config.getConfigurationSection("items." + key)),
                                key
                        ));
                    }
                }
            } else buildNormalShop(data);
            // premium
            if(data.contains("rotating_shop." + id + ".last_premium_refresh")) {
                lastPremiumRefesh = data.getLong("rotating_shop." + id + ".last_premium_refresh");
                if((lastPremiumRefesh + (premiumRefreshTime*1000)) - System.currentTimeMillis() <= 0) buildPremiumShop(data);
                else {
                    ConfigurationSection section = data.getConfigurationSection("rotating_shop." + id + ".premium_items");
                    for(String key : section.getKeys(false)) {
                        premiumLoaded.put(section.getInt(key + ".slot"), new RotatingItem(
                                RotatingItem.StockType.valueOf(section.getString(key + ".stock_type")),
                                config.getInt("items." + key + ".rotating_shop.stock_amount"),
                                section.getInt(key + ".stock"),
                                FlareStack.wrap(config.getConfigurationSection("items." + key)),
                                key
                        ));
                    }
                }
            } else buildPremiumShop(data);
        }
        // update time
    }

    private void buildNormalShop (FileConfiguration data) {
        Random random = new Random();
        data.set("rotating_shop." + id + ".last_normal_refresh", System.currentTimeMillis());
        lastNormalRefesh = System.currentTimeMillis();
        normalLoaded.clear();
        List<String> used = new ArrayList<>(normalItems);
        for(int slot : defaultNormalSlots) {
            if(used.isEmpty()) break;
            String item = used.get(random.nextInt(used.size()));
            System.out.println(item);
            RotatingItem rotatingItem = new RotatingItem(
                    RotatingItem.StockType.valueOf(config.getString("items." + item + ".rotating_shop.stock_type")),
                    config.getInt("items." + item + ".rotating_shop.stock_amount"),
                    FlareStack.wrap(config.getConfigurationSection("items." + item)),
                    item
            );
            normalLoaded.put(slot, rotatingItem);
            data.set("rotating_shop." + id + ".normal_items." + normalLoaded.get(slot).getItemID() + ".slot", slot);
            data.set("rotating_shop." + id + ".normal_items." + normalLoaded.get(slot).getItemID() + ".stock", normalLoaded.get(slot).getMaxStock());
            data.set("rotating_shop." + id + ".normal_items." + normalLoaded.get(slot).getItemID() + ".stock_type", normalLoaded.get(slot).getStockType().toString());
            used.remove(item);
        }
        FilesManager.ACCESS.getData().saveConfig();
    }

    private void buildPremiumShop (FileConfiguration data) {
        Random random = new Random();
        data.set("rotating_shop." + id + ".last_premium_refresh", System.currentTimeMillis());
        lastPremiumRefesh = System.currentTimeMillis();
        premiumLoaded.clear();
        List<String> used = new ArrayList<>(premiumItems);
        for(int slot : defaultPremiumSlots) {
            if(used.isEmpty()) break;
            String item = used.get(random.nextInt(used.size()));
            RotatingItem rotatingItem = new RotatingItem(
                    RotatingItem.StockType.valueOf(config.getString("items." + item + ".rotating_shop.stock_type")),
                    config.getInt("items." + item + ".rotating_shop.stock_amount"),
                    FlareStack.wrap(config.getConfigurationSection("items." + item)),
                    item
            );
            premiumLoaded.put(slot, rotatingItem);
            data.set("rotating_shop." + id + ".premium_items." + premiumLoaded.get(slot).getItemID() + ".slot", slot);
            data.set("rotating_shop." + id + ".premium_items." + premiumLoaded.get(slot).getItemID() + ".stock", premiumLoaded.get(slot).getMaxStock());
            data.set("rotating_shop." + id + ".premium_items." + premiumLoaded.get(slot).getItemID() + ".stock_type", premiumLoaded.get(slot).getStockType().toString());
            used.remove(item);
        }
        FilesManager.ACCESS.getData().saveConfig();
    }

    private void loadDataInMemory() {
        if(!config.contains("rotating_shop")) return;
        defaultNormalSlots = config.getIntegerList("rotating_shop.normal_slots");
        defaultPremiumSlots = config.getIntegerList("rotating_shop.premium_slots");
        normalRefreshTime = config.getInt("rotating_shop.normal_refresh");
        premiumRefreshTime = config.getInt("rotating_shop.premium_refresh");
        normalItems.clear();
        premiumItems.clear();
        ConfigurationSection section = config.getConfigurationSection("items");
        for(String key : section.getKeys(false)) {
            if(!section.contains(key + ".rotating_shop.type")) continue;
//            RotatingItem item = new RotatingItem(
//                    RotatingItem.StockType.valueOf(section.getString(key + ".rotating_shop.stock_type")),
//                    section.getInt(key + ".rotating_shop.stock_amount"),
//                    FlareStack.wrap(section.getConfigurationSection(key)),
//                    key
//            );
            switch (section.getString(key + ".rotating_shop.type").toLowerCase()) {
                case "normal" -> normalItems.add(key);
                case "premium" -> premiumItems.add(key);
            }
        }

    }
}
