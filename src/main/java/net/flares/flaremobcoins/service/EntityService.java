package net.flares.flaremobcoins.service;

import lombok.Getter;
import net.flarepowered.core.TML.FlareScript;
import net.flarepowered.core.text.Message;
import net.flarepowered.neo.ui.MenuArchitect;
import net.flarepowered.neo.ui.contents.InventoryScreen;
import net.flarepowered.neo.ui.contents.UI;
import net.flarepowered.neo.ui.contents.helper.UITemplate;
import net.flarepowered.neo.ui.items.FlareMaterial;
import net.flarepowered.neo.ui.items.FlareStack;
import net.flarepowered.other.Logger;
import net.flarepowered.utils.objects.Pair;
import net.flares.flaremobcoins.API.MobCoinReceiveEvent;
import net.flares.flaremobcoins.API.MobcoinsPlayer;
import net.flares.flaremobcoins.drops.EntityConfiguration;
import net.flares.flaremobcoins.files.FilesManager;
import net.flares.flaremobcoins.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class EntityService implements Listener {

    @Getter
    private List<EntityConfiguration> entityList;

    public void onReload() {
        entityList = new ArrayList<>();
        ConfigurationSection section = FilesManager.ACCESS.getDrops().getConfig().getConfigurationSection("entity");
        for(String value : section.getKeys(false)) {
            entityList.add(EntityConfiguration.wrapFromConfig(section.getConfigurationSection(value)));
        }
        dropsTemplate = null;
    }

    public EntityConfiguration getConfiguration (String type) {
        for (EntityConfiguration obj : entityList) {
            if(obj.checkForMob(type))
                return obj;
        }
        return null;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void mobcoinsReceiveEvent(MobCoinReceiveEvent event) {
        /* Checks */
        if (event.isCancelled()) return;
        if (event.getEntity() == null) return;
        /* Drop Actions */
        new FlareScript().processFull((List<String>) event.getEntityConfiguration().getDropRun().stream().map(s -> s.replace("%pl_mobcoins%", event.getDroppedAmount() + ""))
                .collect(Collectors.toCollection(ArrayList::new)), event.getPlayer());
//        Configuration drops = FilesManager.ACCESS.getDrops().getConfig();
//        if (!FilesManager.ACCESS.getDrops().getConfig().contains(event.getEntity() + ".drop_action")) return;
//        List<String> l = new ArrayList<>();
//        for (String miniList : drops.getStringList(event.getEntity() + ".drop_action"))
//            l.add(miniList.replace("%pl_mobcoins%", String.valueOf(event.getObtainedAmount())));
//        FlareScript flareScript = new FlareScript();
//        flareScript.processFull(l, event.getPlayer());
    }

    @EventHandler
    private void onPlayerKillEntity(EntityDeathEvent event) {
        if (event.getEntity().getKiller() == null) return;
        /* Getting profiles and entity */
        EntityConfiguration entity = getConfiguration(event.getEntity().getType().toString());
        if(entity == null) return;
        if(!entity.checkForChance()) return;
        Player player = event.getEntity().getKiller();
        MobcoinsPlayer mobcoinsPlayer = MobcoinsPlayer.warpPlayer(player.getUniqueId());
        if(mobcoinsPlayer == null) Logger.error("The player profile could not be found!");
        /* Creating values */
        double dropped = Double.parseDouble(String.valueOf(String.format("%.2f", Utils.UTILS.generateNumbers(entity.getDropValue())
                * mobcoinsPlayer.getMultiplier() * FilesManager.ACCESS.getData().getConfig().getDouble("global_multiplier"))));
        if(Service.SERVICE.getDisplayCoinService().isEnabled) {
            Service.SERVICE.getDisplayCoinService().dropItemToGround(dropped * Utils.getTheStackMobs(event), player, event.getEntity().getLocation());
        } else {
            MobCoinReceiveEvent eventMobcoins = new MobCoinReceiveEvent(player, mobcoinsPlayer, event.getEntity().getType().toString(), entity, dropped * Utils.getTheStackMobs(event));
            /* Requirement Checks */
            if(entity.getRequirement() != null)
                if(new FlareScript().processFull(entity.getRequirement(), player))
                    eventMobcoins.setCancelled(true);
            /* Sending event! */
            Bukkit.getPluginManager().callEvent(eventMobcoins);
        }
    }

    UITemplate dropsTemplate;

    public UITemplate getDropMenuTemplate() {
        if(dropsTemplate != null) return dropsTemplate;
        dropsTemplate = new UITemplate();
        dropsTemplate.setContent(new HashMap<>());
        dropsTemplate.getContent().put(0, new InventoryScreen(null));
        AtomicInteger i = new AtomicInteger();
        getEntityList().forEach(entity -> {
            FlareStack stack = new FlareStack();
            switch (entity.getEntityGroup()) {
                case LIST -> stack.setMaterial(mobsHeads.containsKey(entity.getEntities().get(0).toLowerCase()) ? mobsHeads.get(entity.getEntities().get(0).toLowerCase()) : new FlareMaterial().setMaterial("turtle_egg"));
                case HOSTILE -> stack.setMaterial(mobsHeads.get("HOSTILE"));
                case PASSIVE -> stack.setMaterial(mobsHeads.get("PASSIVE"));
                case ALL -> stack.setMaterial(mobsHeads.get("ALL"));
            }
            stack.setLore(processLore(entity));
            stack.setDisplayName(Message.getLocale().getString("drops_menu.item.display_name"));
            dropsTemplate.getContent().get(0).addItemToDock(i.getAndIncrement(), stack);
        });
        dropsTemplate.setTitle(Message.getLocale().getString("drops_menu.title"));
        dropsTemplate.setSize(45);
        return dropsTemplate;
    }

    private List<String> processLore(EntityConfiguration entity) {
        List<String> lore = new ArrayList<>();
        for(String s : Message.getLocale().getStringList("drops_menu.item.lore")) {
            if(s.contains("%pl_entities%")) {
                switch (entity.getEntityGroup()) {
                    case ALL -> lore.add(s.replace("%pl_entities%", "ALL"));
                    case PASSIVE -> lore.add(s.replace("%pl_entities%", "PASSIVE"));
                    case HOSTILE -> lore.add(s.replace("%pl_entities%", "HOSTILE"));
                    case LIST -> {
                        int pass = 0;
                        StringBuilder smallList = new StringBuilder();
                        for(String name : entity.getEntities()) {
                            if(pass >= 3) {
                                lore.add(s.replace("%pl_entities%", smallList.toString()));
                                pass = 0;
                                smallList = new StringBuilder();
                            }
                            if(pass == 0) { smallList.append(name); pass++; continue;}
                            smallList.append(", ").append(name);
                            pass++;
                        }
                        lore.add(s.replace("%pl_entities%", smallList.toString()));
                    }
                }
            } else if(s.contains("%pl_drop_value%")) {
                Pair<String, String> pair = Utils.UTILS.getMinMax(entity.getDropValue());
                lore.add(s.replace("%pl_drop_value%", pair == null ? entity.getDropValue() :
                        Message.getLocale().getString("drops_menu.drop_value_format").replace("%pl_min%", pair.first).replace("%pl_max%", pair.second)));
            } else if(s.contains("%pl_drop_chance%")) {
                lore.add(s.replace("%pl_drop_chance%", entity.getDropChance() + "%"));
            } else lore.add(s);
        }
        return lore;
    }

    public void openDropsList(Player player) {
        UI ui = UI.wrapFromTemplate(player, getDropMenuTemplate(), true);
        MenuArchitect.MENU.openInventory(player, ui);
    }

    private static final Map<String, FlareMaterial> mobsHeads = new HashMap<>() {{
        put("zombie", new FlareMaterial().setBase64Head("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzgzYWFhZWUyMjg2OGNhZmRhYTFmNmY0YTBlNTZiMGZkYjY0Y2QwYWVhYWJkNmU4MzgxOGMzMTJlYmU2NjQzNyJ9fX0="));
        put("skeleton", new FlareMaterial().setBase64Head(""));
        put("ALL", new FlareMaterial().setBase64Head("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjRiNTRmM2U2YTcwMTZhMTRlNzZlZWRjNTMwNDBmNDk2MDU4MWJhY2FjODE0ZmZiNTgxNmZhZTFhMTRiMDE4ZCJ9fX0"));
        put("HOSTILE", new FlareMaterial().setBase64Head("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzQwZWJhZjUxMTE3NzAwZWU5NjM3NGUxYjUzZjRkNDJmMjRiYmQyNzkwNTllNjUxMzk5YTBkNGVhYzdlOTczMiJ9fX0"));
        put("PASSIVE", new FlareMaterial().setBase64Head("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmU2NGYzNDEyZmYwMzkxMWY4N2RiMGJkZjBkN2Q5YjZkYmUwMjgzNGFhMjM2MjQ5NGMyOTVlZGJkODUwIn19fQ"));
    }};

}
