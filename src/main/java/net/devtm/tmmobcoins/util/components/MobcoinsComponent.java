package net.devtm.tmmobcoins.util.components;

import net.flares.lib.TML.components.Component;
import net.flares.lib.TML.objects.TMLState;
import net.flares.lib.exceptions.ComponentException;
import org.bukkit.entity.Player;

import java.util.regex.Matcher;

public class MobcoinsComponent implements Component {

    java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(?i)\\[mobcoins_(.+)] (\\d+)");

    @Override
    public TMLState run(String string, Player player) throws ComponentException {
        Matcher matcher = pattern.matcher(string);
        if(matcher.find()) {
            if(matcher.group(1) == null)
                throw new ComponentException("The component [mobcoins] you used has no tags, use [mobcoins_remove], [mobcoins_set], [mobcoins_add]");
            if(matcher.group(2) == null)
                throw new ComponentException("The component [mobcoins_" + matcher.group(1) + "] has no value entered, please use [mobcoins_" + matcher.group(1) + "] <value>");

            return TMLState.COMPLETED;
        }
        return TMLState.NOT_A_MATCH;
    }
}
