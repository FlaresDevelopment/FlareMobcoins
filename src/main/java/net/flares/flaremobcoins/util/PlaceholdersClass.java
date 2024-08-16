package net.flares.flaremobcoins.util;

import net.flarepowered.core.text.Message;
import net.flarepowered.core.text.placeholders.Placeholder;
import net.flarepowered.neo.ui.MenuArchitect;
import net.flares.flaremobcoins.API.MobcoinsPlayer;
import net.flares.flaremobcoins.service.Service;
import org.bukkit.entity.Player;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlaceholdersClass implements Placeholder {

    @Override
    public String process(String text, Object playerObj) {
        if(!(playerObj instanceof Player))
            return text;
        Player player = (Player) playerObj;
        text = text.replace("%pl_mobcoins%", MobcoinsPlayer.warpPlayer(player.getUniqueId()).getMobcoins() + "");
        text = text.replace("%pl_multiplier%", MobcoinsPlayer.warpPlayer(player.getUniqueId()).getMultiplier() + "");
        if(text.contains("time_left%")) {
            Matcher matcher = Pattern.compile("%pl_(normal|premium)_time_left%").matcher(text);
            if(matcher.find()) {
                if(Service.SERVICE.getMenuService().getInMenus().containsKey(player))
                    text = text.replace(matcher.group(), matcher.group(1).equalsIgnoreCase("normal") ?
                            Service.SERVICE.getMenuService().getMenus().get(Service.SERVICE.getMenuService().getInMenus().get(player)).getRotatingMenu().getNormalTimeLeft() :
                            Service.SERVICE.getMenuService().getMenus().get(Service.SERVICE.getMenuService().getInMenus().get(player)).getRotatingMenu().getPremiumTimeLeft());
            }
        }
//        if(text.contains("%pl_stock")) {
//            Matcher matcher = Pattern.compile("%pl_stock_(\\w+)/(\\w+)%").matcher(text);
//            if (matcher.find()) {
//                StockProfile stockProfile = Service.SERVICE.getMenuService().getStock(player, matcher.group(1), matcher.group(2));
//                if(stockProfile != null)
//                    text = text.replace(matcher.group(), stockProfile.getStock() + "");
//            }
//        }
//        if(text.contains("%pl_max_stock")) {
//            Matcher matcher = Pattern.compile("%pl_max_stock_(\\w+)/(\\w+)%").matcher(text);
//            if (matcher.find()) {
//                StockProfile stockProfile = Service.SERVICE.getMenuService().getStock(player, matcher.group(1), matcher.group(2));
//                if(stockProfile != null)
//                    text = text.replace(matcher.group(), stockProfile.getMaxStock() + "");
//            }
//        }
        return text;
    }
}
