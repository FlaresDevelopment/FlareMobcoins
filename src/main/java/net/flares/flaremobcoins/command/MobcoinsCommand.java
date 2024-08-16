package net.flares.flaremobcoins.command;

import net.flarepowered.core.text.Message;
import net.flarepowered.core.text.other.Replace;
import net.flares.flaremobcoins.FlareMobcoins;
import net.flares.flaremobcoins.service.Service;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class MobcoinsCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if(args.length < 1) {
            help(sender);
        } else {
            switch (args[0].toLowerCase(Locale.ROOT)) {
                case "reload" -> SubCommands.reloadCommand(sender, args);
                case "set" -> SubCommands.setCommand(sender, args);
                case "remove" -> SubCommands.removeCommand(sender, args);
                case "give" -> SubCommands.giveCommand(sender, args);
                case "multiplier" -> SubCommands.multiplierCommand(sender, args);
                case "balance" -> SubCommands.balanceCommand(sender, args);
                case "pay" -> SubCommands.payCommand(sender, args);
                case "drops" -> SubCommands.dropsCommand(sender, args);
                case "shop" -> SubCommands.shopCommand(sender, args);
                case "help" -> help(sender);
            }
        }
        return false;
    }

    private void help(CommandSender sender) {
        if(!sender.hasPermission("flaremobcoins.command.help")) {
            Message.getLocale().getStringList("help_text.header.no_permission").forEach(s ->
                    Message.sendMessage(s, sender, new Replace("%pl_version%", "v" + FlareMobcoins.PLUGIN.getPlugin().getDescription().getVersion()))
            );
        } else {
            Message.getLocale().getStringList("help_text.header.with_permission").forEach(s -> {
                if (s.contains("%pl_commands%")) {
                    Message.getLocale().getConfigurationSection("help_text.commands").getKeys(false).forEach(cmd -> {
                        if (sender.hasPermission("flaretokens.command." + cmd))
                            Message.sendLocalizedMessage("help_text.commands." + cmd, sender);
                    });
                } else {
                    Message.sendMessage(s, sender, new Replace("%pl_version%", "v" + FlareMobcoins.PLUGIN.getPlugin().getDescription().getVersion()));
                }
            });
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        switch (args.length) {
            case 1:
                List<String> a = new ArrayList<>();
                for(String s : Arrays.asList("give", "set", "remove", "balance", "help", "multiplier", "drops", "shop"))
                    if(sender.hasPermission("tmmobcoins.command." + s)) a.add(s);
                return a;
            case 2:
                if(args[0].equalsIgnoreCase("multiplier")) {
                    return Arrays.asList("set", "reset", "global");
                }
                if(args[0].equalsIgnoreCase("shop")) {
                    return Arrays.asList("open", "list");
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
                if(args[0].equalsIgnoreCase("shop")) {
                    if(args[1].equalsIgnoreCase("open")) {
                        return new ArrayList<>(Service.SERVICE.getMenuService().getMenus().keySet());
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