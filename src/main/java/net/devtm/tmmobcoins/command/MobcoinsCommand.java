package net.devtm.tmmobcoins.command;

import net.devtm.tmmobcoins.TMMobCoins;
import net.devtm.tmmobcoins.files.FilesManager;
import net.devtm.tmmobcoins.service.LocaleService;
import net.devtm.tmmobcoins.service.ServiceHandler;
import net.devtm.tmmobcoins.util.Utils;
import net.flares.lib.base.MessageHandler;
import net.flares.lib.base.VersionCheckers;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.logging.Level;

public class MobcoinsCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if(args.length < 1) {
            help(commandSender);
        } else {
            switch (args[0].toLowerCase(Locale.ROOT)) {
                case "reload":
                    if(!commandSender.hasPermission("tmmobcoins.command.reload")) { commandSender.sendMessage(MessageHandler.message("basic.no_permission").prefix().toStringColor()); return true;}
                    FilesManager.ACCESS.reload();
                    commandSender.sendMessage(MessageHandler.message("commands.reload.success").prefix().toStringColor());
                    commandSender.sendMessage(MessageHandler.message("commands.reload.commands").prefix().toStringColor());
                    TMMobCoins.PLUGIN.startStorage();
                    commandSender.sendMessage(MessageHandler.message("commands.reload.mysql").prefix().toStringColor());
                    break;
                case "resetshopdata":
                    if(!commandSender.hasPermission("tmmobcoins.admin.resetshopdata")) { commandSender.sendMessage(MessageHandler.message("basic.no_permission").prefix().toStringColor()); return true;}
                    FilesManager.ACCESS.getData().getConfig().set("rotating_shop", null);
                    FilesManager.ACCESS.getData().saveConfig();
                    break;
                case "set":
                    if(!commandSender.hasPermission("tmmobcoins.command.set")) { commandSender.sendMessage(MessageHandler.message("basic.no_permission").prefix().toStringColor()); return true;}
                    if(args.length > 2) {
                        try {
                            double amount = Double.parseDouble(args[2]);
                            ServiceHandler.SERVICE.getDataService().setMobcoins(Utils.UTILS.getPlayerUUID(args[1]), amount);
                            commandSender.sendMessage(MessageHandler.message("commands.set.success").prefix()
                                    .replace("%pl_player%", Utils.UTILS.getPlayerName(args[1])).replace("%pl_mobcoins%", amount + "")
                                    .placeholderAPI(commandSender).toStringColor());
                            if(!args[1].equalsIgnoreCase(commandSender.getName()) && Utils.UTILS.getPlayer(args[1]) instanceof Player)
                                Bukkit.getPlayer(args[1]).sendMessage(MessageHandler.message("commands.set.received").prefix()
                                        .replace("%pl_player%", commandSender.getName()).replace("%pl_mobcoins%", amount + "")
                                        .placeholderAPI(commandSender).toStringColor());
                        } catch (Exception e) {
                            ServiceHandler.SERVICE.getLoggerService().log(Level.SEVERE, e, "Some set command faild view the logs for more info!");
                            commandSender.sendMessage(MessageHandler.message("commands.set.help").prefix().toStringColor());
                        }
                    } else commandSender.sendMessage(MessageHandler.message("commands.set.help").prefix().toStringColor());
                    break;
                case "remove":
                    if(!commandSender.hasPermission("tmmobcoins.command.remove")) { commandSender.sendMessage(MessageHandler.message("basic.no_permission").prefix().toStringColor()); return true;}
                    if(args.length > 2) {
                        try {
                            double amount = Double.parseDouble(args[2]);
                            UUID uuid = Utils.UTILS.getPlayerUUID(args[1]);
                            commandSender.sendMessage(MessageHandler.message("commands.remove.success").prefix()
                                    .replace("%pl_player%", Utils.UTILS.getPlayerName(args[1])).replace("%pl_mobcoins%", amount + "")
                                    .placeholderAPI(commandSender).toStringColor());
                            if(!args[1].equalsIgnoreCase(commandSender.getName()) && Utils.UTILS.getPlayer(args[1]) instanceof Player)
                                Bukkit.getPlayer(args[1]).sendMessage(MessageHandler.message("commands.remove.received").prefix()
                                        .replace("%pl_player%", commandSender.getName()).replace("%pl_mobcoins%", amount + "")
                                        .placeholderAPI(commandSender).toStringColor());
                            ServiceHandler.SERVICE.getDataService().warpPlayer(uuid).removeMobcoins(amount);
                        } catch (Exception e) {
                            commandSender.sendMessage(MessageHandler.message("commands.remove.help").prefix().toStringColor());
                        }
                    } else commandSender.sendMessage(MessageHandler.message("commands.remove.help").prefix().toStringColor());
                    break;
                case "give":
                    if(!commandSender.hasPermission("tmmobcoins.command.give")) { commandSender.sendMessage(MessageHandler.message("basic.no_permission").prefix().toStringColor()); return true;}
                    if(args.length > 2) {
                        try {
                            double amount = Double.parseDouble(args[2]);
                            UUID uuid = Utils.UTILS.getPlayerUUID(args[1]);
                            commandSender.sendMessage(MessageHandler.message("commands.give.success").prefix()
                                    .replace("%pl_player%", Utils.UTILS.getPlayerName(args[1])).replace("%pl_mobcoins%", amount + "")
                                    .placeholderAPI(commandSender).toStringColor());
                            if(!args[1].equalsIgnoreCase(commandSender.getName()) && Utils.UTILS.getPlayer(args[1]) instanceof Player)
                                Bukkit.getPlayer(args[1]).sendMessage(MessageHandler.message("commands.give.received").prefix()
                                        .replace("%pl_player%", commandSender.getName()).replace("%pl_mobcoins%", amount + "")
                                        .placeholderAPI(commandSender).toStringColor());
                            ServiceHandler.SERVICE.getDataService().warpPlayer(uuid).giveMobcoins(amount);
                        } catch (Exception e) {
                            e.printStackTrace();
                            ServiceHandler.SERVICE.getLoggerService().log(Level.SEVERE, e, "Give command errors");
                            commandSender.sendMessage(MessageHandler.message("commands.give.help").prefix().toStringColor());
                        }
                    } else commandSender.sendMessage(MessageHandler.message("commands.give.help").prefix().toStringColor());
                    break;
                case "multiplier":
                    /*                 0        1      2       3 */
                    /* /tmmobcoins multiplier give [player] [value] */
                    /* /tmmobcoins multiplier reset [player] */
                    if(!commandSender.hasPermission("tmmobcoins.command.multiplier")) { commandSender.sendMessage(MessageHandler.message("basic.no_permission").prefix().toStringColor()); return true;}
                    switch (args[1].toLowerCase(Locale.ROOT)) {
                        case "set":
                            try {
                                double multiplier = Double.parseDouble(args[3]);
                                UUID uuid = Utils.UTILS.getPlayerUUID(args[2]);
                                commandSender.sendMessage(MessageHandler.message("commands.multiplier.set.otherplayer").prefix().replace("%pl_player%", args[2]).replace("%pl_multiplier%", multiplier + "").placeholderAPI(commandSender).toStringColor());
                                ServiceHandler.SERVICE.getDataService().warpPlayer(uuid).setMultiplier(multiplier);
                                if(!args[2].equalsIgnoreCase(commandSender.getName()) && Utils.UTILS.getPlayer(args[2]) instanceof Player)
                                    Bukkit.getPlayer(args[2]).sendMessage(MessageHandler.message("commands.multiplier.set.player").prefix().replace("%pl_player%", args[2]).replace("%pl_multiplier%", multiplier + "").placeholderAPI(commandSender).toStringColor());
                            }catch (Exception e) {
                                commandSender.sendMessage(MessageHandler.message("commands.multiplier.set.help").prefix().toStringColor());
                            }
                            break;
                        case "reset":
                            try {
                                double multiplier = 1.0;
                                UUID uuid = Utils.UTILS.getPlayerUUID(args[1]);
                                commandSender.sendMessage(MessageHandler.message("commands.multiplier.set.player").prefix()
                                        .replace("%pl_player%", "All Server").replace("%pl_multiplier%", multiplier + "")
                                        .placeholderAPI(commandSender).toStringColor());
                                ServiceHandler.SERVICE.getDataService().warpPlayer(uuid).setMultiplier(multiplier);
                                if(!args[2].equalsIgnoreCase(commandSender.getName()) && Utils.UTILS.getPlayer(args[2]) instanceof Player)
                                    Bukkit.getPlayer(args[2]).sendMessage(MessageHandler.message("commands.multiplier.set.otherplayer").prefix()
                                            .replace("%pl_player%", "All Server").replace("%pl_multiplier%", multiplier + "")
                                            .placeholderAPI(commandSender).toStringColor());
                            }catch (Exception e) {
                                commandSender.sendMessage(MessageHandler.message("commands.multiplier.set.help").prefix().toStringColor());
                            }
                            break;
                        case "global":
                            try {
                                double multiplier = Double.parseDouble(args[2]);
                                if(multiplier != 0) {
                                    FilesManager.ACCESS.getData().getConfig().set("global_multiplier", multiplier);
                                    FilesManager.ACCESS.getData().saveConfig();
                                    commandSender.sendMessage(MessageHandler.message("commands.multiplier.global.success").prefix()
                                            .replace("%pl_player%", "All Server").replace("%pl_multiplier%", multiplier + "")
                                            .placeholderAPI(commandSender).toStringColor());
                                } else {
                                    commandSender.sendMessage(MessageHandler.message("commands.multiplier.errors.cant_be").prefix().placeholderAPI(commandSender).toStringColor());
                                }
                            }catch (Exception e) {
                                commandSender.sendMessage(MessageHandler.message("commands.multiplier.global.help").prefix().toStringColor());
                            }
                            break;
                    }
                    break;
                case "balance":
                    if(!commandSender.hasPermission("tmmobcoins.command.balance")) { commandSender.sendMessage(MessageHandler.message(LocaleService.BASIC_no_permission.get).prefix().toStringColor()); return true;}
                    try {
                        if(args.length == 1) {
                            commandSender.sendMessage(MessageHandler.message("commands.balance.player").prefix()
                                    .replace("%pl_player%", commandSender.getName()).replace("%pl_mobcoins%", "" + ServiceHandler.SERVICE.getDataService().warpPlayer(((Player) commandSender).getUniqueId()).getMobcoins())
                                    .placeholderAPI(commandSender).toStringColor());
                        } else if(commandSender.hasPermission("tmmobcoins.command.balance.others")) {
                            commandSender.sendMessage(MessageHandler.message("commands.balance.otherplayer").prefix()
                                    .replace("%pl_player%", args[1]).replace("%pl_mobcoins%", "" + ServiceHandler.SERVICE.getDataService().warpPlayer(Utils.UTILS.getPlayerUUID(args[1])).getMobcoins()).placeholderAPI(commandSender).toStringColor());
                        }
                    } catch (Exception e) {
                        commandSender.sendMessage(MessageHandler.message("commands.balance.help").prefix().toStringColor());
                    }
                    break;
                case "pay":
                    if(!commandSender.hasPermission("tmmobcoins.command.pay")) { commandSender.sendMessage(MessageHandler.message("basic.no_permission").prefix().toStringColor()); return true;}
                    try {
                        if(args.length == 3) {
                            try {
                                double amount = Double.parseDouble(args[2]);
                                if(commandSender.getName().equalsIgnoreCase(args[1])) {
                                    commandSender.sendMessage(MessageHandler.message("commands.pay.help").prefix().replace("%pl_player%", args[1]).replace("%pl_mobcoins%", args[2]).placeholderAPI(commandSender).toStringColor());
                                    return false;
                                }
                                commandSender.sendMessage(MessageHandler.message("commands.pay.success").prefix()
                                        .replace("%pl_player%", args[1]).replace("%pl_mobcoins%", amount + "").placeholderAPI(commandSender).toStringColor());
                                ServiceHandler.SERVICE.getDataService().warpPlayer(Utils.UTILS.getPlayerUUID(args[1])).giveMobcoins(amount);
                                ServiceHandler.SERVICE.getDataService().warpPlayer(((Player) commandSender).getUniqueId()).removeMobcoins(amount);
                            } catch (Exception e) {
                                commandSender.sendMessage(MessageHandler.message("commands.pay.help").prefix()
                                        .replace("%pl_player%", args[1]).replace("%pl_mobcoins%", args[2]).placeholderAPI(commandSender).toStringColor());
                            }
                        } else {
                            commandSender.sendMessage(MessageHandler.message("commands.pay.help").prefix().placeholderAPI(commandSender).toStringColor());
                        }
                    } catch (Exception e) {
                        commandSender.sendMessage(MessageHandler.message("commands.pay.help").prefix().toStringColor());
                    }
                    break;
                case "help":
                    help(commandSender);
                    break;
            }
        }
        return false;
    }

    private void help(CommandSender commandSender) {
        if(!commandSender.hasPermission("tmmobcoins.command.help")) {
            commandSender.sendMessage(MessageHandler.chat("\n  &7(v" + TMMobCoins.PLUGIN.getPlugin().getDescription().getVersion() + ")").toStringColor());
            commandSender.sendMessage(MessageHandler.message("basic.no_permission").replace("%pl_prefix%", "").toStringColor());
            commandSender.sendMessage("\n");
        } else {
            commandSender.sendMessage(MessageHandler
                    .chat("\n  <GRADIENT:#FFC837-#FFC837>Flare Mobcoins</GRADIENT> &7(v" + TMMobCoins.PLUGIN.getPlugin().getDescription().getVersion() + ")\n \n  &f&nArguments&f: &7[] Required, () Optional." +
                                "\n \n  &#f7971e▸ &7/mobcoins give [player] [amount]\n  &#f9a815▸ &7/mobcoins set [player] [amount]" +
                            "\n  &#fab011▸ &7/mobcoins remove [player] [amount]\n  &#fcb90d▸ &7/mobcoins balance (player)\n  &#fdc109▸ &7/mobcoins reload <files/database/all> " +
                            "\n")
                    .toStringColor());

            if (VersionCheckers.getVersion() >= 16) {
                ((Player) commandSender).spigot().sendMessage(ComponentSerializer.parse("[\"\",{\"text\":\"  ▸ Support: \",\"color\":\"#F72B2B\"},{\"text\":\"[Wiki]\",\"color\":\"#F72B2B\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://mobcoins.devtm.net/\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":\"Click To Open\"}},{\"text\":\" \",\"color\":\"#F72B2B\",\"hoverEvent\":{\"action\":\"show_text\",\"contents\":\"Click To Open\"}},{\"text\":\"[Discord]\",\"color\":\"#F72B2B\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://discord.com/invite/XFtV7qgajR\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":\"Click To Open\"}}]"));
                ((Player) commandSender).spigot().sendMessage(ComponentSerializer.parse("[\"\",{\"text\":\"  ▸ \",\"color\":\"red\"},{\"text\":\" Plugin: \",\"color\":\"red\"},{\"text\":\"[SpigotMC]\",\"color\":\"red\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://www.spigotmc.org/resources/authors/xmaikiyt.508656/\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":\"Click to open\"}}]"));
            } else{
                ((Player) commandSender).spigot().sendMessage(ComponentSerializer.parse("[\"\",{\"text\":\"  ▸ Support: \",\"color\":\"red\"},{\"text\":\"[Wiki]\",\"color\":\"red\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://mobcoins.devtm.net/\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"Click to open\"}},{\"text\":\" \",\"color\":\"red\",\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"Click to open\"}},{\"text\":\"[Discord]\",\"color\":\"red\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://discord.com/invite/XFtV7qgajR\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"Click to open\"}}]"));
                ((Player) commandSender).spigot().sendMessage(ComponentSerializer.parse("[\"\",{\"text\":\"  ▸\",\"color\":\"red\"},{\"text\":\" Plugin: \",\"color\":\"red\"},{\"text\":\"[SpigotMC]\",\"color\":\"red\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://www.spigotmc.org/resources/authors/xmaikiyt.508656/\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"Click to open\"}}]"));
            }
            commandSender.sendMessage(MessageHandler.chat("\n\n&7&oNote: If you need help using our plugin check our wiki or join our discord server (https://discord.com/invite/SqXCR9QvXj).").toStringColor());
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        switch (args.length) {
            case 1:
                List<String> a = new ArrayList<>();
                for(String s : Arrays.asList("give", "set", "remove", "balance", "help", "multiplier"))
                    if(sender.hasPermission("tmmobcoins.command." + s)) a.add(s);
                return a;
            case 2:
                if(args[0].equalsIgnoreCase("multiplier")) {
                    return Arrays.asList("set", "reset", "global");
                }
                if(!args[0].equalsIgnoreCase("help")) {
                    List<String> list = new ArrayList<>();
                    Bukkit.getOnlinePlayers().forEach(pl -> list.add(pl.getName()));
                    return list;
                }
            case 3:
                if(args[0].equalsIgnoreCase("multiplier")) {
                    if(args[1].equalsIgnoreCase("global")) {
                        return Collections.singletonList("[amount]");
                    } else {
                        List<String> list = new ArrayList<>();
                        Bukkit.getOnlinePlayers().forEach(pl -> list.add(pl.getName()));
                        return list;
                    }
                }
                if(!args[0].equalsIgnoreCase("help")) {
                    return Collections.singletonList("[amount]");
                }
            case 4:
                if(args[0].equalsIgnoreCase("multiplier")) {
                    return Collections.singletonList("[amount]");
                }
        }
        return null;
    }
}