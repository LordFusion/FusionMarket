package io.github.lordfusion.fusionmarket.commands;

import io.github.lordfusion.fusionmarket.FusionMarket;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class BaseCommand implements CommandExecutor
{
    /**
     * Executes the given command, returning its success
     *
     * @param sender  Source of the command
     * @param command Command which was executed
     * @param label   Alias of the command which was used
     * @param args    Passed command arguments
     * @return true if a valid command, otherwise false
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if (args.length == 0) { // Display plugin info
            TextComponent[] output = new TextComponent[4];
            
            TextComponent pluginHeader = new TextComponent(" - * - * - ");
            pluginHeader.setColor(ChatColor.GRAY);
            TextComponent pluginName = new TextComponent("Fusion Market");
            pluginName.setColor(ChatColor.DARK_PURPLE);
            pluginHeader.addExtra(pluginName);
            pluginHeader.addExtra(" - * - * - ");
            if (!(sender instanceof Player)) {
                FusionMarket.sendUserMessage(sender, pluginHeader);
                Help.run(sender, new String[0]);
                return true;
            }
            TextComponent developerText = new TextComponent("Developed by Lord_Fusion");
            developerText.setColor(ChatColor.DARK_AQUA);
            pluginHeader.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[]{developerText}));
            pluginHeader.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/LordFusion/FusionMarket"));
            output[0] = pluginHeader;
            
            TextComponent mktTutorialMsg = new TextComponent("For a tutorial for Market renting, ");
            mktTutorialMsg.setColor(ChatColor.DARK_AQUA);
            TextComponent mktTutorialClickable = new TextComponent("click me!");
            mktTutorialClickable.setColor(ChatColor.AQUA);
            mktTutorialClickable.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/mkt tutorial renting"));
            TextComponent temp1 = new TextComponent("Coming soon!");
            temp1.setColor(ChatColor.WHITE);
            mktTutorialClickable.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[]{temp1}));
            mktTutorialMsg.addExtra(mktTutorialClickable);
            output[1] = mktTutorialMsg;
            
            TextComponent shopTutorialMsg = new TextComponent("For a tutorial for Shop making, ");
            shopTutorialMsg.setColor(ChatColor.DARK_AQUA);
            TextComponent shopTutorialClickable = new TextComponent("click me!");
            shopTutorialClickable.setColor(ChatColor.AQUA);
            shopTutorialClickable.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/mkt tutorial selling"));
            TextComponent temp2 = new TextComponent("Coming soon!");
            temp2.setColor(ChatColor.WHITE);
            shopTutorialClickable.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[]{temp2}));
            shopTutorialMsg.addExtra(shopTutorialClickable);
            output[2] = shopTutorialMsg;
            
            TextComponent helpMsg = new TextComponent("To view available commands, ");
            helpMsg.setColor(ChatColor.DARK_AQUA);
            TextComponent helpClickable = new TextComponent("click me!");
            helpClickable.setColor(ChatColor.AQUA);
            helpClickable.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/mkt help"));
            helpMsg.addExtra(helpClickable);
            output[3] = helpMsg;
            
            FusionMarket.sendUserMessages(sender, output);
            
            return true;
        }
        
        String[] newArgs = Arrays.copyOfRange(args, 1, args.length);
        
        switch (args[0].toLowerCase()) {
            case "addplotsign":
            case "aps":
                if (!(sender instanceof Player)) {
                    commandFailure(sender, "This command is only available for players.");
                    return true;
                }
                if (!sender.hasPermission("fusion.market.manage")) {
                    permissionReject(sender, "fusion.market.manage");
                    return true;
                }
                if (newArgs.length > 1) {
                    argumentsReject(sender, "Too many arguments!", Help.CMDHELP_ADDPLOTSIGN_BASIC);
                    return true;
                }
                AddPlotSign.run(sender, newArgs);
                break;
            case "admin":
            case "adminmode":
                if (!(sender instanceof Player)) {
                    commandFailure(sender, "This command is only available for players.");
                    return true;
                }
                if (!sender.hasPermission("fusion.market.manage")) {
                    permissionReject(sender, "fusion.market.manage");
                    return true;
                }
                if (newArgs.length > 1) {
                    argumentsReject(sender, "Too many arguments!", Help.CMDHELP_ADMINMODE_BASIC);
                    return true;
                }
                AdminMode.run(sender, newArgs);
                break;
            case "asm":
            case "adminshop":
            case "adminshopmode":
                if (!(sender instanceof Player)) {
                    commandFailure(sender, "This command is only available for players.");
                    return true;
                }
                if (!sender.hasPermission("fusion.market.adminshop")) {
                    permissionReject(sender, "fusion.market.adminshop");
                    return true;
                }
                if (newArgs.length > 1) {
                    argumentsReject(sender, "Too many arguments!", Help.CMDHELP_ADMINSHOPMODE_BASIC);
                    return true;
                }
                AdminShopMode.run(sender, newArgs);
                break;
            case "buy":
                if (!(sender instanceof Player)) {
                    commandFailure(sender, "This command is only available for players.");
                    return true;
                }
                if (!sender.hasPermission("fusion.market.bulkbuy")) {
                    permissionReject(sender, "fusion.market.bulkbuy");
                    return true;
                }
                if (newArgs.length > 2) {
                    argumentsReject(sender, "Too many arguments!", Help.CMDHELP_BUY_BASIC);
                    return true;
                } else if (newArgs.length < 1) {
                    argumentsReject(sender, "Not enough arguments!", Help.CMDHELP_BUY_BASIC);
                    return true;
                }
    
                Buy.run(((Player) sender).getPlayer(), newArgs);
                break;
            case "create":
                if (!sender.hasPermission("fusion.market.create")) {
                    permissionReject(sender, "fusion.market.create");
                    return true;
                }
                if (newArgs.length != 3 && !(sender instanceof Player)) {
                    argumentsReject(sender, new TextComponent("All arguments are required for non-players."), Help.CMDHELP_CREATE_BASIC);
                    return true;
                }
                
                Create.run(sender, newArgs);
                break;
            case "delete":
                if (!sender.hasPermission("fusion.market.manage")) {
                    permissionReject(sender, "fusion.market.manage");
                    return true;
                }
                if (newArgs.length != 1) {
                    argumentsReject(sender, "Incorrect arguments!", Help.CMDHELP_DELETE_BASIC);
                    return true;
                }
        
                Delete.run(sender, newArgs);
                break;
            case "limitperplayer":
            case "lpp":
                if (!sender.hasPermission("fusion.market.shops")) {
                    permissionReject(sender, "fusion.market.shops");
                    return true;
                }
                if (newArgs.length != 2 && newArgs.length != 1) {
                    argumentsReject(sender, "Incorrect arguments!", Help.CMDHELP_LIMITPERPLAYER_BASIC);
                    return true;
                }
    
                LimitPerPlayer.run(sender, newArgs);
                break;
            case "purge":
                if (!sender.hasPermission("fusion.market.manage")) {
                    permissionReject(sender, "fusion.market.manage");
                    return true;
                }
                if (newArgs.length > 1) {
                    argumentsReject(sender, "Incorrect arguments!", Help.CMDHELP_PURGE_BASIC);
                    return true;
                }
                
                Purge.run(sender, newArgs);
                break;
            case "remove":
                if (!sender.hasPermission("fusion.market.manage")) {
                    permissionReject(sender, "fusion.market.manage");
                    return true;
                }
                if (newArgs.length != 1) {
                    argumentsReject(sender, "Incorrect arguments!", Help.CMDHELP_REMOVE_BASIC);
                    return true;
                }
        
                Remove.run(sender, newArgs);
                break;
            case "rent":
                if (!sender.hasPermission("fusion.market.rent")) {
                    permissionReject(sender, "fusion.market.rent");
                    return true;
                }
                if (newArgs.length != 1) {
                    argumentsReject(sender, "Incorrect arguments!", Help.CMDHELP_RENT_BASIC);
                    return true;
                }
                if (!(sender instanceof Player)) {
                    commandFailure(sender, "This command is only available for players.");
                    return true;
                }
                Rent.run(((Player)sender).getPlayer(), newArgs[0]);
                break;
            case "reset":
                if (!sender.hasPermission("fusion.market.manage")) {
                    permissionReject(sender, "fusion.market.manage");
                    return true;
                }
                if (newArgs.length != 1) {
                    argumentsReject(sender, "Incorrect arguments!", Help.CMDHELP_RESET_BASIC);
                    return true;
                }
                
                Reset.run(sender, newArgs);
                break;
            case "sell":
                if (!(sender instanceof Player)) {
                    commandFailure(sender, "This command is only available for players.");
                    return true;
                }
                if (!sender.hasPermission("fusion.market.bulksell")) {
                    permissionReject(sender, "fusion.market.bulksell");
                    return true;
                }
                if (newArgs.length > 2) {
                    argumentsReject(sender, "Too many arguments!", Help.CMDHELP_SELL_BASIC);
                    return true;
                } else if (newArgs.length < 1) {
                    argumentsReject(sender, "Not enough arguments!", Help.CMDHELP_SELL_BASIC);
                    return true;
                }
        
                Sell.run(((Player) sender).getPlayer(), newArgs);
                break;
            case "set":
                if (!sender.hasPermission("fusion.market.manage")) {
                    permissionReject(sender, "fusion.market.manage");
                    return true;
                }
                if (newArgs.length < 2 && !(newArgs.length == 1 && newArgs[0].equalsIgnoreCase("help")))
                    argumentsReject(sender, "Not enough arguments!", Help.CMDHELP_SET_BASIC);
                Set.run(sender, newArgs);
                break;
            case "setlimit":
                if (!sender.hasPermission("fusion.market.shops")) {
                    permissionReject(sender, "fusion.market.shops");
                    return true;
                }
                if (newArgs.length != 3) {
                    argumentsReject(sender, "Incorrect arguments!", Help.CMDHELP_SETLIMIT_BASIC);
                    return true;
                }
                SetLimit.run(sender, newArgs);
                break;
            case "setprice":
                if (!sender.hasPermission("fusion.market.shops")) {
                    permissionReject(sender, "fusion.market.shops");
                    return true;
                }
                if (newArgs.length != 2) {
                    argumentsReject(sender, "Incorrect arguments!", Help.CMDHELP_SETPRICE_BASIC);
                    return true;
                }
                
                SetPrice.run(sender, newArgs);
                break;
            case "trust":
            case "untrust":
                if (!sender.hasPermission("fusion.market.rent")) {
                    permissionReject(sender, "fusion.market.rent");
                    return true;
                }
                if (newArgs.length < 2) {
                    argumentsReject(sender, "Too few arguments!", Help.CMDHELP_TRUST_BASIC);
                    return true;
                }
                if (newArgs.length > 2) {
                    argumentsReject(sender, "Too many arguments!", Help.CMDHELP_TRUST_BASIC);
                    return true;
                }
                if (!(sender instanceof Player)) {
                    commandFailure(sender, "This command is only available for players.");
                    return true;
                }
                if (args[0].equalsIgnoreCase("trust"))
                    Trust.run(((Player) sender).getPlayer(), newArgs, true);
                else if (args[0].equalsIgnoreCase("untrust"))
                    Trust.run(((Player) sender).getPlayer(), newArgs, false);
                break;
            case "help":
            default:
                Help.run(sender, newArgs);
                break;
        }
        
        return true;
    }
    
    /**
     * Sends the user a message, informing them that they don't have permission for the command.
     * @param sender User sending the command who does not have permissions.
     * @param permissionNode Missing permission node.
     */
    static void permissionReject(CommandSender sender, String permissionNode)
    {
        TextComponent mainMsg = new TextComponent("You don't have permission for that command.");
        mainMsg.setColor(ChatColor.RED);
        
        ComponentBuilder nodeMsg = new ComponentBuilder(permissionNode);
        nodeMsg.color(ChatColor.GOLD);
        
        mainMsg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, nodeMsg.create()));
        FusionMarket.sendUserMessage(sender, mainMsg);
        
        FusionMarket.sendConsoleInfo("Rejected command from '" + sender.getName() + "': Lacks permission '" +
                permissionNode + "'.");
    }
    
    /**
     * Sends the user a message, informing them that their arguments weren't good enough.
     * @param sender       User sending the malformed command.
     * @param msg          Details about the missing arguments.
     * @param basicHelpMsg Command's basic help message, generated from Help class.
     */
    static void argumentsReject(CommandSender sender, TextComponent msg, TextComponent basicHelpMsg)
    {
        FusionMarket.sendUserMessage(sender, msg);
        FusionMarket.sendUserMessage(sender, basicHelpMsg);
    }
    
    /**
     * Sends the user a message, informing them that their arguments weren't good enough.
     * @param sender       User sending the malformed command.
     * @param msg          Details about the missing arguments.
     * @param basicHelpMsg Command's basic help message, generated from Help class.
     */
    static void argumentsReject(CommandSender sender, String msg, TextComponent basicHelpMsg)
    {
        argumentsReject(sender, new TextComponent(msg), basicHelpMsg);
    }
    
    /**
     * Sends the user a message, informing them that the command failed.
     * @param sender User attempting the failed command.
     * @param msg    Details about the failure.
     */
    static void commandFailure(CommandSender sender, String msg) {
        if (sender instanceof Player)
            ((Player)sender).spigot().sendMessage(new TextComponent(msg));
        else
            sender.sendMessage(msg);
    }
}
