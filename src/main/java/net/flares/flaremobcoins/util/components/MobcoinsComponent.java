package net.flares.flaremobcoins.util.components;

import net.flarepowered.core.TML.components.Component;
import net.flarepowered.core.TML.objects.TMLState;
import net.flarepowered.other.exceptions.ComponentException;
import net.flares.flaremobcoins.API.MobcoinsPlayer;
import org.bukkit.entity.Player;

import java.util.regex.Matcher;

public class MobcoinsComponent implements Component {

    java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(?i)\\[mobcoins_(.+)] (\\d+\\.?\\d*)");

    @Override
    public TMLState run(String string, Player player) throws ComponentException {
        Matcher matcher = pattern.matcher(string);
        if(matcher.find()) {
            if(matcher.group(1) == null)
                throw new ComponentException("The component [mobcoins] you used has no tags, use [mobcoins_remove], [mobcoins_set], [mobcoins_give]");
            if(matcher.group(2) == null)
                throw new ComponentException("The component [mobcoins_" + matcher.group(1) + "] has no value entered, please use [mobcoins_" + matcher.group(1) + "] <value>");
            Double value = Double.parseDouble(matcher.group(2));
            switch (matcher.group(1).toLowerCase()) {
                case "give":
                    MobcoinsPlayer.warpPlayer(player.getUniqueId()).giveMobcoins(value);
                    break;
                case "remove":
                    MobcoinsPlayer.warpPlayer(player.getUniqueId()).removeMobcoins(value);
                    break;
                case "set":
                    MobcoinsPlayer.warpPlayer(player.getUniqueId()).setMobcoins(value);
                    break;
            }
            return TMLState.COMPLETED;
        }
        return TMLState.NOT_A_MATCH;
    }
}
