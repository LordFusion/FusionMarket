package io.github.lordfusion.fusionmarket.commands;

import io.github.lordfusion.fusionmarket.FusionMarket;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Help
{
    public static void run(CommandSender sender, String[] args)
    {
        
        if (args.length == 1) {
            switch (args[0]) {
                case "addplotsign":
                case "aps":
                    FusionMarket.sendUserMessage(sender, CMDHELP_ADDPLOTSIGN_EXTENDED);
                    break;
                case "admin":
                case "adminmode":
                    FusionMarket.sendUserMessage(sender, CMDHELP_ADMINMODE_EXTENDED);
                    break;
                case "asm":
                case "adminshop":
                case "adminshopmode":
                    FusionMarket.sendUserMessage(sender, CMDHELP_ADMINSHOPMODE_EXTENDED);
                case "buy":
                    FusionMarket.sendUserMessage(sender, CMDHELP_BUY_EXTENDED);
                case "create":
                    FusionMarket.sendUserMessage(sender, CMDHELP_CREATE_EXTENDED);
                    break;
                case "delete":
                    FusionMarket.sendUserMessage(sender, CMDHELP_DELETE_EXTENDED);
                    break;
                case "limitperplayer":
                case "lpp":
                    FusionMarket.sendUserMessage(sender, CMDHELP_LIMITPERPLAYER_EXTENDED);
                case "purge":
                    FusionMarket.sendUserMessage(sender, CMDHELP_PURGE_EXTENDED);
                case "remove":
                    FusionMarket.sendUserMessage(sender, CMDHELP_REMOVE_EXTENDED);
                    break;
                case "rent":
                    FusionMarket.sendUserMessage(sender, CMDHELP_RENT_EXTENDED);
                    break;
                case "reset":
                    FusionMarket.sendUserMessage(sender, CMDHELP_RESET_EXTENDED);
                case "sell":
                    FusionMarket.sendUserMessage(sender, CMDHELP_SELL_EXTENDED);
                case "set":
                    FusionMarket.sendUserMessage(sender, CMDHELP_SET_EXTENDED);
                    break;
                case "setlimit":
                    FusionMarket.sendUserMessage(sender, CMDHELP_SETLIMIT_EXTENDED);
                    break;
                case "setprice":
                    FusionMarket.sendUserMessage(sender, CMDHELP_SETPRICE_EXTENDED);
                    break;
                case "trust":
                case "untrust":
                    FusionMarket.sendUserMessage(sender, CMDHELP_TRUST_EXTENDED);
                    break;
                default:
                    TextComponent invalidSubcommandMsg = new TextComponent("Invalid subcommand. Try ");
                    invalidSubcommandMsg.setColor(ChatColor.RED);
                    TextComponent helpCmdText = new TextComponent("/mkt help");
                    helpCmdText.setColor(ChatColor.GOLD);
                    invalidSubcommandMsg.addExtra(helpCmdText);
                    invalidSubcommandMsg.addExtra("!");
                    
                    FusionMarket.sendUserMessage(sender, invalidSubcommandMsg);
                    break;
            }
        } else {
            if (sender instanceof Player) {
                if (sender.hasPermission("fusion.market.manage")) {
                    FusionMarket.sendUserMessage(sender, CMDHELP_ADDPLOTSIGN_FANCY);
                    FusionMarket.sendUserMessage(sender, CMDHELP_ADMINMODE_FANCY);
                }
                if (sender.hasPermission("fusion.market.adminshop")) {
                    FusionMarket.sendUserMessage(sender, CMDHELP_ADMINSHOPMODE_FANCY);
                }
                if (sender.hasPermission("fusion.market.bulkbuy"))
                    FusionMarket.sendUserMessage(sender, CMDHELP_BUY_FANCY);
                if (sender.hasPermission("fusion.market.bulksell"))
                    FusionMarket.sendUserMessage(sender, CMDHELP_SELL_FANCY);
                if (sender.hasPermission("fusion.market.manage")) {
                    FusionMarket.sendUserMessage(sender, CMDHELP_CREATE_FANCY);
                    FusionMarket.sendUserMessage(sender, CMDHELP_DELETE_FANCY);
                    FusionMarket.sendUserMessage(sender, CMDHELP_PURGE_FANCY);
                    FusionMarket.sendUserMessage(sender, CMDHELP_REMOVE_FANCY);
                }
                FusionMarket.sendUserMessage(sender, CMDHELP_RENT_FANCY);
                if (sender.hasPermission("fusion.market.manage")) {
                    FusionMarket.sendUserMessage(sender, CMDHELP_RESET_FANCY);
                    FusionMarket.sendUserMessage(sender, CMDHELP_SET_FANCY);
                }
                
                FusionMarket.sendUserMessage(sender, CMDHELP_LIMITPERPLAYER_FANCY);
                FusionMarket.sendUserMessage(sender, CMDHELP_SETLIMIT_FANCY);
                FusionMarket.sendUserMessage(sender, CMDHELP_SETPRICE_FANCY);
                FusionMarket.sendUserMessage(sender, CMDHELP_TRUST_FANCY);
            } else {
                if (sender.hasPermission("fusion.market.manage")) {
                    FusionMarket.sendUserMessage(sender, CMDHELP_ADDPLOTSIGN_BASIC);
                    FusionMarket.sendUserMessage(sender, CMDHELP_ADMINMODE_BASIC);
                }
                if (sender.hasPermission("fusion.market.adminshop")) {
                    FusionMarket.sendUserMessage(sender, CMDHELP_ADMINSHOPMODE_BASIC);
                }
                if (sender.hasPermission("fusion.market.bulkbuy"))
                    FusionMarket.sendUserMessage(sender, CMDHELP_BUY_BASIC);
                if (sender.hasPermission("fusion.market.bulksell"))
                    FusionMarket.sendUserMessage(sender, CMDHELP_SELL_BASIC);
                if (sender.hasPermission("fusion.market.manage")) {
                    FusionMarket.sendUserMessage(sender, CMDHELP_CREATE_BASIC);
                    FusionMarket.sendUserMessage(sender, CMDHELP_DELETE_BASIC);
                    FusionMarket.sendUserMessage(sender, CMDHELP_PURGE_BASIC);
                    FusionMarket.sendUserMessage(sender, CMDHELP_REMOVE_BASIC);
                }
                FusionMarket.sendUserMessage(sender, CMDHELP_RENT_BASIC);
                if (sender.hasPermission("fusion.market.manage")) {
                    FusionMarket.sendUserMessage(sender, CMDHELP_RESET_BASIC);
                    FusionMarket.sendUserMessage(sender, CMDHELP_SET_BASIC);
                }
                FusionMarket.sendUserMessage(sender, CMDHELP_SETLIMIT_BASIC);
                FusionMarket.sendUserMessage(sender, CMDHELP_SETPRICE_BASIC);
                FusionMarket.sendUserMessage(sender, CMDHELP_TRUST_BASIC);
            }
        }
    }
    
    // /mkt addplotsign <plotname>
    public static final TextComponent CMDHELP_ADDPLOTSIGN_BASIC = generateBasicHelp("/mkt addplotsign <plotname>");
    public static final TextComponent CMDHELP_ADDPLOTSIGN_EXTENDED = generateExtendedHelp(new String[]
            {"/mkt addplotsign <plotname>",
                    "Adds the sign you're looking at to the given plot's data set.",
                    "<plotname> | Existing market plot name; Omit to reset."});
    public static final TextComponent CMDHELP_ADDPLOTSIGN_FANCY = generateFancyHelp(new String[]
            {"/mkt",
                    "addplotsign", "Adds the sign you're looking at to the given plot's data set.",
                    "<plotname>", "Existing market plot name; Omit to reset."});
    
    // /mkt adminmode <true/false>
    public static final TextComponent CMDHELP_ADMINMODE_BASIC = generateBasicHelp("/mkt adminmode <true/false>");
    public static final TextComponent CMDHELP_ADMINMODE_EXTENDED = generateExtendedHelp(new String[]
            {"/mkt adminmode <true/false>",
                    "Toggles admin mode for administrators.",
                    "<true/false> | Manually set admin mode status; Omit to toggle."});
    public static final TextComponent CMDHELP_ADMINMODE_FANCY = generateFancyHelp(new String[]
            {"/mkt",
                    "adminmode", "Toggles admin mode for administrators.",
                    "<true/false>", "Manually set admin mode status; Omit to toggle."});
    
    // /mkt adminshopmode <true/false>
    public static final TextComponent CMDHELP_ADMINSHOPMODE_BASIC = generateBasicHelp("/mkt adminshopmode <true/false>");
    public static final TextComponent CMDHELP_ADMINSHOPMODE_EXTENDED = generateExtendedHelp(new String[]
            {"/mkt adminshopmode <true/false>",
                    "Toggles admin shop creation mode.",
                    "<true/false> | Manually set admin shop creation mode; Omit to toggle."});
    public static final TextComponent CMDHELP_ADMINSHOPMODE_FANCY = generateFancyHelp(new String[]
            {"/mkt",
                    "adminshopmode", "Toggles admin shop creation mode.",
                    "<true/false>", "Manually set admin shop creation mode; Omit to toggle."});
    
    // /mkt buy [ShopID] <quantity>
    public static final TextComponent CMDHELP_BUY_BASIC = generateBasicHelp("/mkt buy [ShopID] <quantity>");
    public static final TextComponent CMDHELP_BUY_EXTENDED = generateExtendedHelp(new String[]
            {"/mkt buy [ShopID] <quantity>",
                    "Allows for bulk- and distance-purchasing from shops.",
                    "[ShopID] | ID of the shop you want to buy from.",
                    "<quantity> | Number of items to buy; Default 1."});
    public static final TextComponent CMDHELP_BUY_FANCY = generateFancyHelp(new String[]
            {"/mkt",
                    "buy", "Allows for bulk- and distance-purchasing from shops.",
                    "[ShopID]", "ID of the shop you want to buy from.",
                    "<quantity>", "Number of items to buy; Default 1."});
    
    // /mkt create <name> <region>
    public static final TextComponent CMDHELP_CREATE_BASIC = generateBasicHelp("/mkt create <name> <region> <world>");
    public static final TextComponent CMDHELP_CREATE_EXTENDED = generateExtendedHelp(new String[]
            {"/mkt create <name> <region>",
                    "Creates a new Market region, protected by WorldGuard.",
                    "<name> | Optional | Specify the plot's name. Generates a procedural name if none provided.",
                    "<region> | Optional | Pre-existing WorldGuard region. Uses current WorldEdit selection if none provided.",
                    "<world> | Optional | Specify the world for the WorldGuard region. Uses your world if none provided."});
    public static final TextComponent CMDHELP_CREATE_FANCY = generateFancyHelp(new String[]
            {"/mkt",
                    "create", "Creates a new Market region, protected by WorldGuard.",
                    "<name>", "Optional | Specify the plot's name. Generates a procedural name if none provided.",
                    "<region>", "Optional | Pre-existing WorldGuard region. Uses current WorldEdit selection if none provided.",
                    "<world>", "Optional | Specify the world for the WorldGuard region. Uses your world if none provided."});
    
    // /mkt delete [plotname]
    public static final TextComponent CMDHELP_DELETE_BASIC = generateBasicHelp("/mkt delete [plotname]");
    public static final TextComponent CMDHELP_DELETE_EXTENDED = generateExtendedHelp(new String[]
            {"/mkt delete [plotname]",
                    "Deletes the given market and its underlying region.",
                    "[plotname] | Unique ID of the Market to be removed."});
    public static final TextComponent CMDHELP_DELETE_FANCY = generateFancyHelp(new String[]
            {"/mkt",
                    "delete", "Deletes the given market and its underlying region.",
                    "[plotname]", "Unique ID of the Market to be removed."});
    
    // /mkt limitPerPlayer [Shop ID] <true/false>
    public static final TextComponent CMDHELP_LIMITPERPLAYER_BASIC = generateBasicHelp("/mkt limitPerPlayer [Shop ID] <true/false>");
    public static final TextComponent CMDHELP_LIMITPERPLAYER_EXTENDED = generateExtendedHelp(new String[]
            {"/mkt limitPerPlayer [Shop ID] <true/false>",
                    "Sets the shop's limit for total sales, or per each player.",
                    "[Shop ID] | ID of the Shop.",
                    "<true/false> | New value; Default toggles."});
    public static final TextComponent CMDHELP_LIMITPERPLAYER_FANCY = generateFancyHelp(new String[]
            {"/mkt",
                    "limitPerPlayer", "Sets the shop's limit for total sales, or per each player.",
                    "[Shop ID]", "ID of the Shop.",
                    "<true/false>", "New value; Default toggles."});
    
    // /mkt purge <username>
    public static final TextComponent CMDHELP_PURGE_BASIC = generateBasicHelp("/mkt purge <username>");
    public static final TextComponent CMDHELP_PURGE_EXTENDED = generateExtendedHelp(new String[]
            {"/mkt purge <username>",
                    "Removes all shops that are missing a chest or sign.",
                    "<username> | Only purge shops from the specified user."});
    public static final TextComponent CMDHELP_PURGE_FANCY = generateFancyHelp(new String[]
            {"/mkt",
                    "purge", "Removes all shops that are missing a chest or sign.",
                    "<username>", "Only purge shops from the specified user."});
    
    // /mkt remove [plotname]
    public static final TextComponent CMDHELP_REMOVE_BASIC = generateBasicHelp("/mkt remove [plotname]");
    public static final TextComponent CMDHELP_REMOVE_EXTENDED = generateExtendedHelp(new String[]
            {"/mkt remove [plotname]",
                    "Removes a Market plot from FsnMkt, but leaves the underlying region intact.",
                    "[plotname] | Unique ID of the Market to be removed."});
    public static final TextComponent CMDHELP_REMOVE_FANCY = generateFancyHelp(new String[]
            {"/mkt",
                    "remove", "Removes a Market plot from FsnMkt, but leaves the underlying region intact.",
                    "[plotname]", "Unique ID of the Market to be removed."});
    
    // /mkt rent [plotname]
    public static final TextComponent CMDHELP_RENT_BASIC = generateBasicHelp("/mkt rent [plotname]");
    public static final TextComponent CMDHELP_RENT_EXTENDED = generateExtendedHelp(new String[]
            {"/mkt rent [plotname]",
                    "Rent the specified Market plot.",
                    "[plotname] | The Market plot you want to rent."});
    public static final TextComponent CMDHELP_RENT_FANCY = generateFancyHelp(new String[]
            {"/mkt",
                    "rent", "Rent the specified Market plot.",
                    "[plotname]", "The Market plot you want to rent."});
    
    // /mkt reset [plotname]
    public static final TextComponent CMDHELP_RESET_BASIC = generateBasicHelp("/mkt reset [plotname]");
    public static final TextComponent CMDHELP_RESET_EXTENDED = generateExtendedHelp(new String[]
            {"/mkt reset [plotname]",
                    "Resets ownership of the specified Market plot.",
                    "[plotname] | The Market plot you want to reset."});
    public static final TextComponent CMDHELP_RESET_FANCY = generateFancyHelp(new String[]
            {"/mkt",
                    "reset", "Resets ownership of the specified Market plot.",
                    "[plotname]", "The Market plot you want to reset."});
    
    // /mkt sell [ShopID] <quantity>
    public static final TextComponent CMDHELP_SELL_BASIC = generateBasicHelp("/mkt sell [ShopID] <quantity>");
    public static final TextComponent CMDHELP_SELL_EXTENDED = generateExtendedHelp(new String[]
            {"/mkt sell [ShopID] <quantity>",
                    "Allows for bulk- and distance-selling to shops.",
                    "[ShopID] | ID of the shop you want to buy from.",
                    "<quantity> | Number of items to sell; Default 1."});
    public static final TextComponent CMDHELP_SELL_FANCY = generateFancyHelp(new String[]
            {"/mkt",
                    "sell", "Allows for bulk- and distance-selling to shops.",
                    "[ShopID]", "ID of the shop you want to buy from.",
                    "<quantity>", "Number of items to sell; Default 1."});
    
    // /mkt set [various arguments]
    public static final TextComponent CMDHELP_SET_BASIC = generateBasicHelp("/mkt set help");
    public static final TextComponent CMDHELP_SET_EXTENDED = generateExtendedHelp(new String[]
            {"/mkt set help",
                    "Administrative command to manually change Market plots."});
    public static final TextComponent CMDHELP_SET_FANCY = generateFancyHelp(new String[]
            {"/mkt",
                    "set", "Administrative command to manually change Market plots.",
                    "help", "Please run this command to display the sub-commands."});
    
    // /mkt setlimit [Shop ID] [New Limit] [Limit Length]
    public static final TextComponent CMDHELP_SETLIMIT_BASIC = generateBasicHelp("/mkt setlimit [ShopID] [New Limit] [Limit Length]");
    public static final TextComponent CMDHELP_SETLIMIT_EXTENDED = generateExtendedHelp(new String[]
            {"/mkt setlimit [ShopID] [New Limit] [Limit Length]",
                    "Change the max buy/sell limit of a chest-based Shop.",
                    "[ShopID] | ID of the Shop.",
                    "[New Limit] | Limit for sales. 0 to disable.",
                    "[Limit Length] | Time period, examples: 8h, 3d, 2m, 1y, 1a"});
    public static final TextComponent CMDHELP_SETLIMIT_FANCY = generateFancyHelp(new String[]
            {"/mkt",
                    "setlimit", "Change the max buy/sell limit of a chest-based Shop.",
                    "[ShopID]", "ID of the Shop.",
                    "[New Limit]", "Limit for sales. 0 to disable.",
                    "[Limit Length]", "Time period, examples: 8h, 3d, 2m, 1y, 1a"
            });
    
    // /mkt setprice [ShopID] [New Price]
    public static final TextComponent CMDHELP_SETPRICE_BASIC = generateBasicHelp("/mkt setprice [ShopID] [New Price]");
    public static final TextComponent CMDHELP_SETPRICE_EXTENDED = generateExtendedHelp(new String[]
            {"/mkt setprice [ShopID] [New Price]",
                    "Change the price of a chest-based Shop.",
                    "[ShopID] | ID of the Shop.",
                    "[New Price] | Positive to sell, negative to buy, 0 to disable."});
    public static final TextComponent CMDHELP_SETPRICE_FANCY = generateFancyHelp(new String[]
            {"/mkt",
                    "setprice", "Change the price of a chest-based Shop.",
                    "[ShopID]", "ID of the Shop.",
                    "[New Price]", "Positive to sell, negative to buy, 0 to disable."});
    
    // /mkt trust/untrust [MarketID] [Username/UUID/RESET]
    public static final TextComponent CMDHELP_TRUST_BASIC = generateBasicHelp("/mkt [trust/untrust] [MarketID] [Username/UUID/RESET]");
    public static final TextComponent CMDHELP_TRUST_EXTENDED = generateExtendedHelp(new String[]
            {"/mkt [trust/untrust] [MarketID] [Username/UUID/RESET]",
                    "Adds or removes a player from your plot's Trusted list (allows others to build).",
                    "[MarketID] | ID of the Market.",
                    "[Username/UUID] | Player you want to (un)trust. RESET to remove all."});
    public static final TextComponent CMDHELP_TRUST_FANCY = generateFancyHelp(new String[]
            {"/mkt",
                    "[trust/untrust]", "Adds or removes a player from your plot's Trusted list (allows others to build).",
                    "[MarketID]", "ID of the Market.",
                    "[Username/UUID/RESET]", "Player you want to (un)trust. RESET to remove all."});
    
    
    /**
     * Not intended to include command info or details.
     *  Ex: "/fr debug <true/false>"
     * @param helpMsg Command usage
     * @return Formatted command usage message
     */
    private static TextComponent generateBasicHelp(String helpMsg)
    {
        TextComponent output = new TextComponent(helpMsg);
        output.setColor(ChatColor.BLUE);
        
        return output;
    }
    
    /**
     * Gives all the information that generateFancyHelp does,
     * but displays it in multiple lines instead of using text-hovering
     *
     * @param helpMsgs Command usage; First parameter should be just the command.
     * @return Formatted multi-line command usage message
     */
    private static TextComponent generateExtendedHelp(String[] helpMsgs)
    {
        TextComponent output = new TextComponent(helpMsgs[0]);
        output.setColor(ChatColor.BLUE);
        
        for (int i=1; i<helpMsgs.length; i++) {
            TextComponent nextLine = new TextComponent("\n     " + helpMsgs[i]);
            nextLine.setColor(ChatColor.GRAY);
            
            output.addExtra(nextLine);
        }
        
        return output;
    }
    
    /**
     * Give full usage info in a clean way, by letting players hover over the things they need help with.
     * @param helpMsgs Command usage. Format: "/fr","debug","(debug description)","<true/false>","(t/f description)"
     * @return Formatted usage information with hoverable text
     */
    private static TextComponent generateFancyHelp(String[] helpMsgs)
    {
        // If I fuck up, catch it before it displays something wacky.
        if ((helpMsgs.length %2) != 1) {
            TextComponent helpUnavailMsg = new TextComponent("An error occurred. This message is unavailable.");
            helpUnavailMsg.setColor(ChatColor.RED);
            return helpUnavailMsg;
        }
        
        // Assuming I didn't fuck up in a really weird way...
        TextComponent output = new TextComponent(helpMsgs[0]);
        output.setColor(ChatColor.BLUE);
        
        for (int i=1; i<helpMsgs.length; i+= 2) {
            TextComponent nextLine = new TextComponent(helpMsgs[i]);
            nextLine.setColor(ChatColor.BLUE);
            
            ComponentBuilder hoverMsg = new ComponentBuilder(helpMsgs[i+1]);
            hoverMsg.color(ChatColor.AQUA);
            
            nextLine.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverMsg.create()));
            output.addExtra(" ");
            output.addExtra(nextLine);
        }
        
        return output;
    }
}
