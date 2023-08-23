package net.flares.flaremobcoins.drops;

import net.flarepowered.core.TML.FlareScript;
import net.flares.flaremobcoins.API.MobCoinReceiveEvent;
import net.flares.flaremobcoins.API.MobcoinsPlayer;
import net.flares.flaremobcoins.files.FilesManager;
import net.flares.flaremobcoins.FlareMobcoins;
import net.flares.flaremobcoins.service.ServiceHandler;
import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.*;
import java.util.logging.Level;

public class DropsListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL)
    public void mobcoinsReceiveEvent(MobCoinReceiveEvent event) {
        /* Checkers */
        if (event.isCancelled()) return;
        if (event.getEntity() == null) return;
        Configuration drops = FilesManager.ACCESS.getDrops().getConfig();
        /* Drop Actions */
        if (!FilesManager.ACCESS.getDrops().getConfig().contains(event.getEntity() + ".drop_action")) return;
        List<String> l = new ArrayList<>();
        for (String miniList : drops.getStringList(event.getEntity() + ".drop_action"))
            l.add(miniList.replace("%pl_mobcoins%", String.valueOf(event.getObtainedAmount())));
        FlareScript flareScript = new FlareScript();
        flareScript.processFull(l, event.getPlayer());
    }

    @EventHandler
    private void onPlayerKillEntity(EntityDeathEvent event) {
        if (event.getEntity().getKiller() == null) return;
            /* Check if the mob exists in config */
        String configPath = ServiceHandler.SERVICE.getEventService().mobVerify(event);
        if(configPath == null) return;
        Configuration drops = FilesManager.ACCESS.getDrops().getConfig();
        Player player = event.getEntity().getKiller();
        MobcoinsPlayer mobcoinsPlayer = MobcoinsPlayer.warpPlayer(player.getUniqueId());

        /* Checking if the mob is ok! */

        if(mobcoinsPlayer == null)
            FlareMobcoins.PLUGIN.getPlugin().getLogger().log(Level.SEVERE, "The player profile could not be found!");

        double amount = drops.contains(configPath + ".drop_value") ?
                Double.parseDouble(String.valueOf(String.format("%.2f", ServiceHandler.SERVICE.getEventService().generateNumber(drops, configPath) * mobcoinsPlayer.getMultiplier() * FilesManager.ACCESS.getData().getConfig().getDouble("global_multiplier")))) : 0;
        MobCoinReceiveEvent eventMobcoins = new MobCoinReceiveEvent(player, mobcoinsPlayer, configPath, amount * ServiceHandler.SERVICE.getEventService().getTheStackMobs(event));
        FlareScript flareScript = new FlareScript();
        if(drops.contains(configPath + ".requirement")) {
            if (!flareScript.processFull(Collections.singletonList(drops.getString(configPath + ".requirement")), player))
                eventMobcoins.setCancelled(true);
        } else if(drops.contains(configPath + ".requirements")) {
            if (!flareScript.processFull(Collections.singletonList(drops.getString(configPath + ".requirements")), player))
                eventMobcoins.setCancelled(true);
        }
        Bukkit.getPluginManager().callEvent(eventMobcoins);
    }
}