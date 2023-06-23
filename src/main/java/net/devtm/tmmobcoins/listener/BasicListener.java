package net.devtm.tmmobcoins.listener;

import net.flares.lib.TML.FlareScript;
import net.flares.lib.base.VersionCheckers;
import net.devtm.tmmobcoins.API.MobCoinReceiveEvent;
import net.devtm.tmmobcoins.API.MobcoinsPlayer;
import net.devtm.tmmobcoins.TMMobCoins;
import net.devtm.tmmobcoins.files.FilesManager;
import net.devtm.tmmobcoins.service.ServiceHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.world.LootGenerateEvent;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.logging.Level;

public class BasicListener implements Listener {

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
            l.add(miniList.replace("%pl_mobcoins%", event.getObtainedAmount() + ""));
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
        MobcoinsPlayer mobcoinsPlayer = ServiceHandler.SERVICE.getDataService().warpPlayer(player.getUniqueId());

        /* Checking if the mob is ok! */

        if(mobcoinsPlayer == null)
            TMMobCoins.PLUGIN.getPlugin().getLogger().log(Level.SEVERE, "The player profile could not be found!");

        double amount = drops.contains(configPath + ".drop_value") ?
                Double.parseDouble(String.valueOf(String.format("%.2f", ServiceHandler.SERVICE.getEventService().generateNumber(drops, configPath) * mobcoinsPlayer.getMultiplier() * FilesManager.ACCESS.getData().getConfig().getDouble("global_multiplier")))) : 0;
        MobCoinReceiveEvent eventMobcoins = new MobCoinReceiveEvent(player, mobcoinsPlayer, configPath, amount * ServiceHandler.SERVICE.getEventService().getTheStackMobs(event));
        if(drops.contains(configPath + ".requirement")) {
            FlareScript flareScript = new FlareScript();
            if (!flareScript.processFull(Collections.singletonList(drops.getString(configPath + ".requirement")), player))
                eventMobcoins.setCancelled(true);
        }
        Bukkit.getPluginManager().callEvent(eventMobcoins);
    }

    @EventHandler
    private void playerFireworkDamage(EntityDamageByEntityEvent event) {
        if(VersionCheckers.getVersion() <= 9) return;
        if (event.getDamager() instanceof Firework) {
            Firework fw = (Firework) event.getDamager();
            if (fw.hasMetadata("nodamage")) {
                event.setCancelled(true);
            }
        }
    }
}