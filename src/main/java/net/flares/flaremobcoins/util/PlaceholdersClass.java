package net.flares.flaremobcoins.util;

import net.flarepowered.core.text.placeholders.Placeholder;
import net.flares.flaremobcoins.API.MobcoinsPlayer;
import net.flares.flaremobcoins.files.FilesManager;
import net.flares.flaremobcoins.service.ServiceHandler;
import org.bukkit.entity.Player;

import java.util.Objects;
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
        if(text.contains("%pl_rotating_shop")) {
            Matcher matcher = Pattern.compile("%pl_rotating_shop_(\\w+)_(normal|premium)%").matcher(text);
            if(matcher.find()) {
                ServiceHandler.SERVICE.getMenuService().updateRotatingShop(matcher.group(1));
                if (!FilesManager.ACCESS.getData().getConfig().contains("rotating_shop." + matcher.group(1) + "." + matcher.group(2) + "_last_time")) {
                    text = text.replace(matcher.group(), "");
                    return text;
                }
                text = text.replace(matcher.group(),
                        Utils.UTILS.findDifference(FilesManager.ACCESS.getData().getConfig().getLong("rotating_shop." + matcher.group(1) + "." + matcher.group(2) + "_last_time"), System.currentTimeMillis()));
            }
        }
        if(text.contains("%pl_stock")) {
            Matcher matcher = Pattern.compile("%pl_stock_(\\w+)/(\\w+)%").matcher(text);
            if (matcher.find()) {
                StockProfile stockProfile = ServiceHandler.SERVICE.getMenuService().getStock(player, matcher.group(1), matcher.group(2));
                if(stockProfile != null)
                    text = text.replace(matcher.group(), stockProfile.getStock() + "");
            }
        }
        if(text.contains("%pl_max_stock")) {
            Matcher matcher = Pattern.compile("%pl_max_stock_(\\w+)/(\\w+)%").matcher(text);
            if (matcher.find()) {
                StockProfile stockProfile = ServiceHandler.SERVICE.getMenuService().getStock(player, matcher.group(1), matcher.group(2));
                if(stockProfile != null)
                    text = text.replace(matcher.group(), stockProfile.getMaxStock() + "");
            }
        }
        return text;
    }
}
