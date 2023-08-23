package net.flares.flaremobcoins.command;

import net.flarepowered.core.text.StringUtils;
import net.flarepowered.core.text.other.Replace;
import net.flares.flaremobcoins.API.MobcoinsPlayer;
import net.flares.flaremobcoins.FlareMobcoins;
import net.flares.flaremobcoins.files.FilesManager;
import net.flares.flaremobcoins.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
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
                    if(!commandSender.hasPermission("flaremobcoins.command.reload")) { commandSender.sendMessage(StringUtils.formatMessageFromLocale("basic.no_permission", null)); return true;}
                    FilesManager.ACCESS.reload();
                    commandSender.sendMessage(StringUtils.formatMessageFromLocale("commands.reload.success", null));
                    StringUtils.lang.clear();
                    StringUtils.loadLang(new File(FlareMobcoins.PLUGIN.getPlugin().getDataFolder(), "locale").toPath());
                    break;
                case "resetshopdata":
                    if(!commandSender.hasPermission("flaremobcoins.admin.resetshopdata")) { commandSender.sendMessage(StringUtils.formatMessageFromLocale("basic.no_permission", null)); return true;}
                    FilesManager.ACCESS.getData().getConfig().set("rotating_shop", null);
                    FilesManager.ACCESS.getData().saveConfig();
                    break;
                case "set":
                    if(!commandSender.hasPermission("flaremobcoins.command.set")) { commandSender.sendMessage(StringUtils.formatMessageFromLocale("basic.no_permission", null)); return true;}
                    if(args.length > 2) {
                        try {
                            double amount = Double.parseDouble(args[2]);
                            MobcoinsPlayer.warpPlayer(Utils.UTILS.getPlayerUUID(args[1])).setMobcoins(amount);
                            commandSender.sendMessage(StringUtils.formatMessageFromLocale("commands.set.success", null,
                                    new Replace("%pl_player%", Utils.UTILS.getPlayerName(args[1])), new Replace("%pl_mobcoins%", String.valueOf(amount))));
                            if(!args[1].equalsIgnoreCase(commandSender.getName()) && Bukkit.getPlayer(args[1]) != null && !Arrays.toString(args).contains("-s"))
                                commandSender.sendMessage(StringUtils.formatMessageFromLocale("commands.set.received", null,
                                        new Replace("%pl_player%", commandSender.getName()), new Replace("%pl_mobcoins%", String.valueOf(amount))));
                        } catch (Exception e) {
                            commandSender.sendMessage(StringUtils.formatMessageFromLocale("commands.set.help", null));
                        }
                    } else commandSender.sendMessage(StringUtils.formatMessageFromLocale("commands.set.help", null));
                    break;
                case "remove":
                    if(!commandSender.hasPermission("flaremobcoins.command.remove")) { commandSender.sendMessage(StringUtils.formatMessageFromLocale("basic.no_permission", null)); return true;}
                    if(args.length > 2) {
                        try {
                            double amount = Double.parseDouble(args[2]);
                            commandSender.sendMessage(StringUtils.formatMessageFromLocale("commands.remove.success", null, new Replace("%pl_player%", Utils.UTILS.getPlayerName(args[1])), new Replace("%pl_mobcoins%", String.valueOf(amount))));
                            if(!args[1].equalsIgnoreCase(commandSender.getName()) && Bukkit.getPlayer(args[1]) != null && !Arrays.toString(args).contains("-s"))
                                commandSender.sendMessage(StringUtils.formatMessageFromLocale("commands.remove.received", null,
                                        new Replace("%pl_player%", commandSender.getName()), new Replace("%pl_mobcoins%", String.valueOf(amount))));
                            MobcoinsPlayer.warpPlayer(Utils.UTILS.getPlayerUUID(args[1])).removeMobcoins(amount);
                        } catch (Exception e) {
                            commandSender.sendMessage(StringUtils.formatMessageFromLocale("commands.remove.help", null));
                        }
                    } else commandSender.sendMessage(StringUtils.formatMessageFromLocale("commands.remove.help", null));
                    break;
                case "give":
                    if(!commandSender.hasPermission("flaremobcoins.command.give")) { commandSender.sendMessage(StringUtils.formatMessageFromLocale("basic.no_permission", null)); return true;}
                    if(args.length > 2) {
                        try {
                            double amount = Double.parseDouble(args[2]);
                            commandSender.sendMessage(StringUtils.formatMessageFromLocale("commands.give.success", null, new Replace("%pl_player%", Utils.UTILS.getPlayerName(args[1])), new Replace("%pl_mobcoins%", String.valueOf(amount))));
                            if(!args[1].equalsIgnoreCase(commandSender.getName()) && Bukkit.getPlayer(args[1]) != null && !Arrays.toString(args).contains("-s"))
                                commandSender.sendMessage(StringUtils.formatMessageFromLocale("commands.give.received", null,
                                        new Replace("%pl_player%", commandSender.getName()), new Replace("%pl_mobcoins%", String.valueOf(amount))));
                            MobcoinsPlayer.warpPlayer(Utils.UTILS.getPlayerUUID(args[1])).giveMobcoins(amount);
                        } catch (Exception e) {
                            commandSender.sendMessage(StringUtils.formatMessageFromLocale("commands.give.help", null));
                        }
                    } else commandSender.sendMessage(StringUtils.formatMessageFromLocale("commands.give.help", null));
                    break;
                case "multiplier":
                    /*                 0        1      2       3 */
                    /* /tmmobcoins multiplier give [player] [value] */
                    /* /tmmobcoins multiplier reset [player] */
                    if(!commandSender.hasPermission("flaremobcoins.command.multiplier")) { commandSender.sendMessage(StringUtils.formatMessageFromLocale("basic.no_permission", null)); return true;}
                    switch (args[1].toLowerCase(Locale.ROOT)) {
                        case "set":
                            try {
                                double multiplier = Double.parseDouble(args[3]);
                                UUID uuid = Utils.UTILS.getPlayerUUID(args[2]);
                                if(args[2].equalsIgnoreCase(commandSender.getName()))
                                    commandSender.sendMessage(StringUtils.formatMessageFromLocale("commands.multiplier.set.player", null,
                                            new Replace("%pl_player%", args[2]), new Replace("%pl_multiplier%", String.valueOf(multiplier))));
                                else
                                    commandSender.sendMessage(StringUtils.formatMessageFromLocale("commands.multiplier.set.otherplayer", null,
                                            new Replace("%pl_player%", args[2]), new Replace("%pl_multiplier%", String.valueOf(multiplier))));
                                MobcoinsPlayer.warpPlayer(uuid).setMultiplier(multiplier);
                            } catch (Exception e) {
                                commandSender.sendMessage(StringUtils.formatMessageFromLocale("commands.multiplier.set.help", null));
                            }
                            break;
                        case "reset":
                            try {
                                double multiplier = 1.0;
                                UUID uuid = Utils.UTILS.getPlayerUUID(args[2]);
                                if(args[2].equalsIgnoreCase(commandSender.getName()))
                                    commandSender.sendMessage(StringUtils.formatMessageFromLocale("commands.multiplier.set.player", null,
                                            new Replace("%pl_player%", args[2]), new Replace("%pl_multiplier%", String.valueOf(multiplier))));
                                else
                                    commandSender.sendMessage(StringUtils.formatMessageFromLocale("commands.multiplier.set.otherplayer", null,
                                            new Replace("%pl_player%", args[2]), new Replace("%pl_multiplier%", String.valueOf(multiplier))));
                                MobcoinsPlayer.warpPlayer(uuid).setMultiplier(multiplier);
                            }catch (Exception e) {
                                commandSender.sendMessage(StringUtils.formatMessageFromLocale("commands.multiplier.set.help", null));
                            }
                            break;
                        case "global":
                            try {
                                double multiplier = Double.parseDouble(args[2]);
                                if(multiplier != 0) {
                                    FilesManager.ACCESS.getData().getConfig().set("global_multiplier", multiplier);
                                    FilesManager.ACCESS.getData().saveConfig();
                                    commandSender.sendMessage(StringUtils.formatMessageFromLocale("commands.multiplier.global.success", null,
                                            new Replace("%pl_multiplier%", String.valueOf(multiplier)), new Replace("%pl_player%", "GLOBAL")));
                                } else {
                                    commandSender.sendMessage(StringUtils.formatMessageFromLocale("commands.multiplier.errors.cant_be", null)); return true;

                                }
                            }catch (Exception e) {
                                commandSender.sendMessage(StringUtils.formatMessageFromLocale("commands.global.help", null));
                            }
                            break;
                    }
                    break;
                case "balance":
                    if(!commandSender.hasPermission("flaremobcoins.command.balance")) {commandSender.sendMessage(StringUtils.formatMessageFromLocale("basic.no_permission", null)); return true;}
                    try {
                        if(args.length == 1) {
                            commandSender.sendMessage(StringUtils.formatMessageFromLocale("commands.balance.player", null,
                                    new Replace("%pl_player%", commandSender.getName()), new Replace("%pl_mobcoins%", String.valueOf(MobcoinsPlayer.warpPlayer(((Player) commandSender).getUniqueId()).getMobcoins()))));
                        } else if(!commandSender.hasPermission("flaremobcoins.command.balance.other")) {
                            commandSender.sendMessage(StringUtils.formatMessageFromLocale("commands.balance.other", null,
                                    new Replace("%pl_player%", args[1]), new Replace("%pl_mobcoins%", "" + MobcoinsPlayer.warpPlayer(Utils.UTILS.getPlayerUUID(args[1])).getMobcoins())));
                        } else
                            commandSender.sendMessage(StringUtils.formatMessageFromLocale("basic.no_permission", null)); return true;
                    } catch (Exception e) {
                        commandSender.sendMessage(StringUtils.formatMessageFromLocale("commands.balance.help", null));
                    }
                    break;
                case "pay":
                    if(!commandSender.hasPermission("flaremobcoins.command.pay")) {commandSender.sendMessage(StringUtils.formatMessageFromLocale("basic.no_permission", null)); return true;}
                    try {
                        if(args.length == 3) {
                            try {
                                double amount = Double.parseDouble(args[2]);
                                if(commandSender.getName().equalsIgnoreCase(args[1])) {
                                    commandSender.sendMessage(StringUtils.formatMessageFromLocale("commands.pay.help", null, new Replace("%pl_player%", args[1]), new Replace("%pl_mobcoins%", amount + "")));
                                    return false;
                                }
                                if(MobcoinsPlayer.warpPlayer(((Player) commandSender).getUniqueId()).getMobcoins() < amount) {
                                    commandSender.sendMessage(StringUtils.formatMessageFromLocale("commands.pay.fail.no_money", null, new Replace("%pl_player%", args[1]), new Replace("%pl_tokens%", args[2])));
                                    return true;
                                }
                                commandSender.sendMessage(StringUtils.formatMessageFromLocale("commands.pay.success", null,
                                        new Replace("%pl_player%", args[1]), new Replace("%pl_mobcoins%", amount + "")));
                                MobcoinsPlayer.warpPlayer(Utils.UTILS.getPlayerUUID(args[1])).giveMobcoins(amount);
                                MobcoinsPlayer.warpPlayer(((Player) commandSender).getUniqueId()).removeMobcoins(amount);
                            } catch (Exception e) {
                                commandSender.sendMessage(StringUtils.formatMessageFromLocale("commands.pay.help", null));
                            }
                        } else {
                            commandSender.sendMessage(StringUtils.formatMessageFromLocale("commands.pay.help", null));
                        }
                    } catch (Exception e) {
                        commandSender.sendMessage(StringUtils.formatMessageFromLocale("commands.pay.help", null));
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
        if(!commandSender.hasPermission("flaremobcoins.command.help")) {
            StringUtils.getLocalConfig(null).getStringList("help_text.header.no_permission").forEach(s ->
                    commandSender.sendMessage(StringUtils.formatMessage(s, null, new Replace("%pl_version%", "v" + FlareMobcoins.PLUGIN.getPlugin().getDescription().getVersion())))
            );
        } else {
            StringUtils.getLocalConfig(null).getStringList("help_text.header.with_permission").forEach(s -> {
                if (s.contains("%pl_commands%")) {
                    StringUtils.getLocalConfig(null).getConfigurationSection("help_text.commands").getKeys(false).forEach(cmd -> {
                        if (commandSender.hasPermission("flaretokens.command." + cmd))
                            commandSender.sendMessage(StringUtils.formatMessage(StringUtils.getLocalConfig(null).getString("help_text.commands." + cmd), null));
                    });
                } else {
                    commandSender.sendMessage(StringUtils.formatMessage(s, null, new Replace("%pl_version%", "v" + FlareMobcoins.PLUGIN.getPlugin().getDescription().getVersion())));
                }
            });
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