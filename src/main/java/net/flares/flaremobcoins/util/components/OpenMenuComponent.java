package net.flares.flaremobcoins.util.components;

import net.flarepowered.core.TML.components.Component;
import net.flarepowered.core.TML.objects.TMLState;
import net.flarepowered.other.exceptions.ComponentException;
import net.flares.flaremobcoins.service.ServiceHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.regex.Matcher;

public class OpenMenuComponent implements Component {

    java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(?i)\\[open_menu] (.+)");

    @Override
    public TMLState run(String string, Player player) throws ComponentException {
        Matcher matcher = pattern.matcher(string);
        if(matcher.find()) {
            if(matcher.group(1) == null)
                throw new ComponentException("The component [open_menu] has no console command. We are skipping this item.");
            ServiceHandler.SERVICE.getMenuService().openMenu(player, matcher.group(1));
            return TMLState.COMPLETED;
        }
        return TMLState.NOT_A_MATCH;
    }
}
