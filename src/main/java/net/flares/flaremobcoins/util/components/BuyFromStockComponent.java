package net.flares.flaremobcoins.util.components;

import net.flarepowered.core.TML.components.Component;
import net.flarepowered.core.TML.objects.TMLState;
import net.flarepowered.core.text.StringUtils;
import net.flarepowered.core.text.other.Replace;
import net.flarepowered.other.exceptions.ComponentException;
import net.flarepowered.utils.DependencyManager;
import net.flares.flaremobcoins.API.MobcoinsPlayer;
import net.flares.flaremobcoins.FlareMobcoins;
import net.flares.flaremobcoins.service.ServiceHandler;
import org.bukkit.entity.Player;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BuyFromStockComponent implements Component {

    Pattern pattern = Pattern.compile("(?i)\\[buy_from_stock\\((tokens|money|mobcoins)\\)] (\\d+\\.?\\d*)\\s?(rotating_shop_name=(\\w+))?\\s?(rotating_shop_item=(\\w+))?");

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
                    MobcoinsPlayer tp = MobcoinsPlayer.warpPlayer(player.getUniqueId());
                    if(tp.getMobcoins() >= Integer.parseInt(matcher.group(2))) {
                        if(matcher.group(6) != null)
                            if(!ServiceHandler.SERVICE.getMenuService().buyItem(player, matcher.group(4), matcher.group(6))) {
                                player.sendMessage(StringUtils.formatMessageFromLocale("shop.no_stock", player));
                                return TMLState.FORCED_QUIT;
                            }
                        tp.removeMobcoins(Integer.parseInt(matcher.group(2)));
                        return TMLState.COMPLETED;
                    }
                    player.sendMessage(StringUtils.formatMessageFromLocale("shop.buy_failed", player, new Replace("%pl_amount%", matcher.group(2))));
                    break;
                case "money":
                    if(!DependencyManager.GET.isPluginLoaded(DependencyManager.Dependency.Vault))
                        throw new ComponentException("This server has no economy, so we cant use " + matcher.group());
                    if(DependencyManager.GET.getVaultEconomy().getBalance(player) >= Double.parseDouble(matcher.group(2))) {
                        if(matcher.group(6) != null)
                            if(!ServiceHandler.SERVICE.getMenuService().buyItem(player, matcher.group(4), matcher.group(6))) {
                                player.sendMessage(StringUtils.formatMessageFromLocale("shop.no_stock", player));
                                return TMLState.FORCED_QUIT;
                            }
                        DependencyManager.GET.getVaultEconomy().withdrawPlayer(player, Double.parseDouble(matcher.group(2)));
                        return TMLState.COMPLETED;
                    }
                    player.sendMessage(StringUtils.formatMessageFromLocale("shop.buy_failed", player, new Replace("%pl_amount%", matcher.group(2))));
                    break;
            }
            return TMLState.FORCED_QUIT;
        } else {
            return TMLState.NOT_A_MATCH;
        }
    }
}
