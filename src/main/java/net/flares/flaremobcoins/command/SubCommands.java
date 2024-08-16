package net.flares.flaremobcoins.command;

import net.flarepowered.core.text.Message;
import net.flarepowered.core.text.other.Replace;
import net.flares.flaremobcoins.API.MobcoinsPlayer;
import net.flares.flaremobcoins.exceptions.MenuOpenException;
import net.flares.flaremobcoins.files.FilesManager;
import net.flares.flaremobcoins.service.Service;
import net.flares.flaremobcoins.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Locale;
import java.util.UUID;

public class SubCommands {

    public static void reloadCommand(@NotNull CommandSender sender, @NotNull String[] ignoredArgs) {
        if(!sender.hasPermission("flaremobcoins.command.reload")) { Message.sendLocalizedMessage("basic.no_permission", sender); return;}
        FilesManager.ACCESS.reload();
        Service.SERVICE.onReload();
        Message.sendLocalizedMessage("commands.reload.success", sender);
    }

    public static void setCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if(!sender.hasPermission("flaremobcoins.command.set")) { Message.sendLocalizedMessage("basic.no_permission", sender); return;}
        if(args.length > 2) {
            try {
                double amount = Double.parseDouble(args[2]);
                if(amount < 0) { Message.sendLocalizedMessage("basic.number_cannot_be_negative", sender); return; }
                MobcoinsPlayer.warpPlayer(Utils.UTILS.getPlayerUUID(args[1])).setMobcoins(amount);
                Message.sendLocalizedMessage("commands.set.success", sender, new Replace("%pl_player%", Utils.UTILS.getPlayerName(args[1])), new Replace("%pl_mobcoins%", String.valueOf(amount)));
                if(!args[1].equalsIgnoreCase(sender.getName()) && Bukkit.getPlayer(args[1]) != null && !Arrays.toString(args).contains("-s"))
                    Message.sendLocalizedMessage("commands.set.received", sender, new Replace("%pl_player%", sender.getName()), new Replace("%pl_mobcoins%", String.valueOf(amount)));
            } catch (Exception e) {
                Message.sendLocalizedMessage("commands.set.help", sender);
            }
        } else Message.sendLocalizedMessage("commands.set.help", null);
    }

    public static void removeCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if(!sender.hasPermission("flaremobcoins.command.remove")) { Message.sendLocalizedMessage("basic.no_permission", sender); return;}
        if(args.length > 2) {
            try {
                double amount = Double.parseDouble(args[2]);
                if(amount < 0) { Message.sendLocalizedMessage("basic.number_cannot_be_negative", sender); return; }
                Message.sendLocalizedMessage("commands.remove.success", sender, new Replace("%pl_player%", Utils.UTILS.getPlayerName(args[1])), new Replace("%pl_mobcoins%", String.valueOf(amount)));
                if(!args[1].equalsIgnoreCase(sender.getName()) && Bukkit.getPlayer(args[1]) != null && !Arrays.toString(args).contains("-s"))
                    Message.sendLocalizedMessage("commands.remove.received", sender, new Replace("%pl_player%", sender.getName()), new Replace("%pl_mobcoins%", String.valueOf(amount)));
                MobcoinsPlayer.warpPlayer(Utils.UTILS.getPlayerUUID(args[1])).removeMobcoins(amount);
            } catch (Exception e) {
                Message.sendLocalizedMessage("commands.remove.help", sender);
            }
        } else Message.sendLocalizedMessage("commands.remove.help", sender);
    }

    public static void giveCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if(!sender.hasPermission("flaremobcoins.command.give")) { Message.sendLocalizedMessage("basic.no_permission", sender); return;}
        if(args.length > 2) {
            try {
                double amount = Double.parseDouble(args[2]);
                if(amount < 0) { Message.sendLocalizedMessage("basic.number_cannot_be_negative", sender); return; }
                Message.sendLocalizedMessage("commands.give.success", sender, new Replace("%pl_player%", Utils.UTILS.getPlayerName(args[1])), new Replace("%pl_mobcoins%", String.valueOf(amount)));
                if(!args[1].equalsIgnoreCase(sender.getName()) && Bukkit.getPlayer(args[1]) != null && !Arrays.toString(args).contains("-s"))
                    Message.sendLocalizedMessage("commands.give.received", sender, new Replace("%pl_player%", sender.getName()), new Replace("%pl_mobcoins%", String.valueOf(amount)));
                MobcoinsPlayer.warpPlayer(Utils.UTILS.getPlayerUUID(args[1])).giveMobcoins(amount);
            } catch (Exception e) {
                Message.sendLocalizedMessage("commands.give.help", sender);
            }
        } else Message.sendLocalizedMessage("commands.give.help", sender);
    }

    public static void balanceCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if(!sender.hasPermission("flaremobcoins.command.balance")) { Message.sendLocalizedMessage("basic.no_permission", sender); return;}
        try {
            if(args.length == 1) {
                Message.sendLocalizedMessage("commands.balance.player", sender,
                        new Replace("%pl_player%", sender.getName()), new Replace("%pl_mobcoins%", String.valueOf(MobcoinsPlayer.warpPlayer(((Player) sender).getUniqueId()).getMobcoins())));
            } else if(sender.hasPermission("flaremobcoins.command.balance.other")) {
                Message.sendLocalizedMessage("commands.balance.other", sender,
                        new Replace("%pl_player%", args[1]), new Replace("%pl_mobcoins%", "" + MobcoinsPlayer.warpPlayer(Utils.UTILS.getPlayerUUID(args[1])).getMobcoins()));
            } else Message.sendLocalizedMessage("basic.no_permission", sender);
        } catch (Exception e) {
            Message.sendLocalizedMessage("commands.balance.help", sender);
        }
    }

    public static void payCommand (@NotNull CommandSender sender, @NotNull String[] args) {
        if(!sender.hasPermission("flaremobcoins.command.pay")) { Message.sendLocalizedMessage("basic.no_permission", sender); return;}
        try {
            if(args.length == 3) {
                try {
                    double amount = Double.parseDouble(args[2]);
                    if(amount < 0) { Message.sendLocalizedMessage("basic.number_cannot_be_negative", sender); return; }
                    if(sender.getName().equalsIgnoreCase(args[1])) {
                        Message.sendLocalizedMessage("commands.pay.help", sender, new Replace("%pl_player%", args[1]), new Replace("%pl_mobcoins%", amount + ""));
                        return;
                    }
                    if(MobcoinsPlayer.warpPlayer(((Player) sender).getUniqueId()).getMobcoins() < amount) {
                        Message.sendLocalizedMessage("commands.pay.fail.no_money", sender, new Replace("%pl_player%", args[1]), new Replace("%pl_tokens%", args[2]));
                        return;
                    }
                    Message.sendLocalizedMessage("commands.pay.success", sender, new Replace("%pl_player%", args[1]), new Replace("%pl_mobcoins%", amount + ""));
                    MobcoinsPlayer.warpPlayer(Utils.UTILS.getPlayerUUID(args[1])).giveMobcoins(amount);
                    MobcoinsPlayer.warpPlayer(((Player) sender).getUniqueId()).removeMobcoins(amount);
                } catch (Exception e) {
                    Message.sendLocalizedMessage("commands.pay.help", sender);
                }
            } else {
                Message.sendLocalizedMessage("commands.pay.help", sender);
            }
        } catch (Exception e) {
            Message.sendLocalizedMessage("commands.pay.help", sender);
        }
    }

    public static void multiplierCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if(!sender.hasPermission("flaremobcoins.command.multiplier")) { Message.sendLocalizedMessage("basic.no_permission", sender); return;}
        switch (args[1].toLowerCase(Locale.ROOT)) {
            case "set":
                try {
                    double multiplier = Double.parseDouble(args[3]);
                    UUID uuid = Utils.UTILS.getPlayerUUID(args[2]);
                    if(args[2].equalsIgnoreCase(sender.getName()))
                        Message.sendLocalizedMessage("commands.multiplier.set.player", sender, new Replace("%pl_player%", args[2]), new Replace("%pl_multiplier%", String.valueOf(multiplier)));
                    else
                        Message.sendLocalizedMessage("commands.multiplier.set.otherplayer", sender, new Replace("%pl_player%", args[2]), new Replace("%pl_multiplier%", String.valueOf(multiplier)));
                    MobcoinsPlayer.warpPlayer(uuid).setMultiplier(multiplier);
                } catch (Exception e) {
                    Message.sendLocalizedMessage("commands.multiplier.set.help", sender);
                }
                break;
            case "reset":
                try {
                    double multiplier = 1.0;
                    UUID uuid = Utils.UTILS.getPlayerUUID(args[2]);
                    if(args[2].equalsIgnoreCase(sender.getName()))
                        Message.sendLocalizedMessage("commands.multiplier.set.player", sender, new Replace("%pl_player%", args[2]), new Replace("%pl_multiplier%", String.valueOf(multiplier)));
                    else
                        Message.sendLocalizedMessage("commands.multiplier.set.otherplayer", sender, new Replace("%pl_player%", args[2]), new Replace("%pl_multiplier%", String.valueOf(multiplier)));
                    MobcoinsPlayer.warpPlayer(uuid).setMultiplier(multiplier);
                } catch (Exception e) {
                    Message.sendLocalizedMessage("commands.multiplier.set.help", sender);
                }
                break;
            case "global":
                try {
                    double multiplier = Double.parseDouble(args[2]);
                    if(multiplier != 0) {
                        FilesManager.ACCESS.getData().getConfig().set("global_multiplier", multiplier);
                        FilesManager.ACCESS.getData().saveConfig();
                        Message.sendLocalizedMessage("commands.multiplier.global.success", sender,
                                new Replace("%pl_multiplier%", String.valueOf(multiplier)), new Replace("%pl_player%", "GLOBAL"));
                    } else
                        Message.sendLocalizedMessage("commands.multiplier.errors.cant_be", sender); return;
                }catch (Exception e) {
                    Message.sendLocalizedMessage("commands.global.help", sender);
                }
                break;
        }
    }

    public static void dropsCommand(CommandSender sender, String[] args) {
        if(!sender.hasPermission("flaremobcoins.command.drops")) { Message.sendLocalizedMessage("basic.no_permission", sender); return;}
        try {
            if(args.length == 1) {
                Service.SERVICE.getEntityService().openDropsList((Player) sender);
            } else if(sender.hasPermission("flaremobcoins.command.drops.other")) {
                Service.SERVICE.getEntityService().openDropsList(Bukkit.getPlayer(args[1]));
            } else Message.sendLocalizedMessage("basic.no_permission", sender);
        } catch (Exception e) {
            Message.sendLocalizedMessage("commands.drops.help", sender);
        }
    }

    public static void shopCommand(CommandSender sender, String[] args) {
        if(!sender.hasPermission("flaremobcoins.command.shop")) { Message.sendLocalizedMessage("basic.no_permission", sender); return;}
        try {
            if(args.length <= 1) { Message.sendLocalizedMessage("commands.shop.help", sender); return; }
            switch (args[1].toLowerCase()) {
                case "open" -> {
                    if(args.length == 4)
                        Service.SERVICE.getMenuService().openMenu(Bukkit.getPlayer(args[3]), args[2]);
                    else Service.SERVICE.getMenuService().openMenu((Player) sender, args[2]);

                }
                case "list" -> {
                    Message.sendLocalizedMessage("commands.shop.list.list_header", sender, new Replace("%pl_number%", Service.SERVICE.getMenuService().getMenus().size() + ""));
                    Service.SERVICE.getMenuService().getMenus().keySet().forEach(s -> {
                        Message.sendLocalizedMessage("commands.shop.list.list_element", sender, new Replace("%pl_panel_name%", s));
                    });
                }
                case "reset" -> {
                    //TODO implement
                }
            }
        } catch (MenuOpenException me) {
            switch (me.getMessage()) {
                case "player_is_null" -> Message.sendLocalizedMessage("error.player_not_found", sender);
                case "no_such_menu" -> Message.sendLocalizedMessage("error.no_menu_by_this_name", sender);
                case "no_permission" ->  Message.sendLocalizedMessage("error.no_permission", sender);
            }
        } catch (Exception e) {
            Message.sendLocalizedMessage("commands.balance.help", sender);
            e.printStackTrace();
        }
    }
}
