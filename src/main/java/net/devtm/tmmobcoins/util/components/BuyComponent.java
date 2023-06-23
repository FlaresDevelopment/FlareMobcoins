package net.devtm.tmmobcoins.util.components;

import net.devtm.tmmobcoins.API.MobcoinsPlayer;
import net.devtm.tmmobcoins.service.ServiceHandler;
import net.flares.lib.Lib;
import net.flares.lib.TML.components.Component;
import net.flares.lib.TML.objects.TMLState;
import net.flares.lib.base.MessageHandler;
import net.flares.lib.exceptions.ComponentException;
import net.flares.lib.menu.GUI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BuyComponent implements Component {

    Pattern pattern = Pattern.compile("(?i)\\[buy\\((tokens|money|mobcoins)\\)] (\\d+\\.?\\d*)\\s?(rotating_shop_name=(\\w+))?\\s?(rotating_shop_item=(\\w+))?");

    @Override
    public TMLState run(String string, Player player) throws ComponentException {
        Matcher matcher = this.pattern.matcher(string);
        if (matcher.find()) {
            if (matcher.group(1) == null)
                throw new ComponentException("The component [buy(" + matcher.group(1) + ")] has no message. We are skipping this item.");
            if (matcher.group(2) == null)
                throw new ComponentException("The component [buy(" + matcher.group(1) + ")] has no message. We are skipping this item.");
            switch (matcher.group(1).toLowerCase(Locale.ROOT)) {
                case "mobcoins":
                    MobcoinsPlayer tp = ServiceHandler.SERVICE.getDataService().warpPlayer(player.getUniqueId());
                    if(tp.getMobcoins() >= Integer.parseInt(matcher.group(2))) {
                        if(matcher.group(6) != null)
                            if(!ServiceHandler.SERVICE.getMenuService().buyItem(player, matcher.group(4), matcher.group(6)))
                                return TMLState.FORCED_QUIT;
                        tp.removeMobcoins(Integer.parseInt(matcher.group(2)));
                        return TMLState.COMPLETED;
                    }
                    break;
                case "money":
                    if(Lib.LIB.getEcon().getBalance(player) >= Double.parseDouble(matcher.group(2))) {
                        Lib.LIB.getEcon().withdrawPlayer(player, Double.parseDouble(matcher.group(2)));
                        return TMLState.COMPLETED;
                    }
                    break;
            }
            return TMLState.FORCED_QUIT;
        } else {
            return TMLState.NOT_A_MATCH;
        }
    }
}
