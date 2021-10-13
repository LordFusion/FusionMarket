package io.github.lordfusion.fusionmarket.commands;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import io.github.lordfusion.fusionmarket.DataManager;
import io.github.lordfusion.fusionmarket.FusionMarket;
import io.github.lordfusion.fusionmarket.Market;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

public class Set
{
    /**
     * /mkt set <miscellaneous arguments>
     * The Administrative command to manually change Market settings.
     * @param sender      Command sender
     * @param bloatedArgs Command arguments
     */
    public static void run(CommandSender sender, String[] bloatedArgs)
    {
        Market market = FusionMarket.getInstance().getDataManager().getMarket(bloatedArgs[0]);
        if (market == null) {
            BaseCommand.commandFailure(sender, "Invalid Market ID: '" + bloatedArgs[0] + "'.");
            return;
        }
        
        String[] args = Arrays.copyOfRange(bloatedArgs, 2, bloatedArgs.length);
        switch (bloatedArgs[1]) {
            case "duration": {// /mkt set [id] duration [number]
                if (args.length != 1) {
                    BaseCommand.argumentsReject(sender, "/mkt set [id] duration [number]", Help.CMDHELP_SET_BASIC);
                    return;
                }
                try {
                    Integer.parseInt(args[0]);
                } catch (NumberFormatException e) {
                    BaseCommand.commandFailure(sender, "Argument must be a positive integer.");
                    return;
                }
                int newDuration = Integer.parseInt(args[0]);
                if (newDuration < 1) {
                    BaseCommand.commandFailure(sender, "Argument must be a positive integer.");
                }
    
                market.setRentTime(newDuration);
                TextComponent success = new TextComponent("Set duration of '");
                success.setColor(ChatColor.BLUE);
                TextComponent marketId = new TextComponent(market.getUniqueId());
                marketId.setColor(ChatColor.AQUA);
                success.addExtra(marketId);
                success.addExtra("' to ");
                TextComponent duration = new TextComponent(newDuration + "");
                duration.setColor(ChatColor.AQUA);
                success.addExtra(duration);
                success.addExtra(".");
    
                FusionMarket.sendUserMessage(sender, success);
    
                break;
            }
            case "eviction": { // /mkt set [id] eviction [!!UNKNOWN!!] Todo: Set eviction method
                BaseCommand.commandFailure(sender, "That command isn't ready yet.");
                return;
//                break;
            }
            case "owner": { // /mkt set [id] owner [Username/UUID]
                if (args.length != 1) {
                    BaseCommand.argumentsReject(sender, "/mkt set [id] owner [Username/UUID]", Help.CMDHELP_SET_BASIC);
                    return;
                }
    
                OfflinePlayer newOwner = null;
                if (args[0].length() == 36) { // It's a UUID
                    newOwner = Bukkit.getOfflinePlayer(UUID.fromString(args[0]));
                } else {
                    for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
                        if (offlinePlayer.getName() != null && offlinePlayer.getName().equalsIgnoreCase(args[0])) {
                            newOwner = offlinePlayer;
                            break;
                        }
                    }
                }
    
                if (newOwner == null) {
                    BaseCommand.commandFailure(sender, "Argument must be a valid username or UUID.");
                    return;
                }
                
                market.setOwner(newOwner.getUniqueId());
                TextComponent success = new TextComponent("Set owner of '");
                success.setColor(ChatColor.BLUE);
                TextComponent marketId = new TextComponent(market.getUniqueId());
                marketId.setColor(ChatColor.AQUA);
                success.addExtra(marketId);
                success.addExtra("' to ");
                TextComponent ownerName = new TextComponent(newOwner.getName() + " [" +
                        newOwner.getUniqueId().toString() + "]");
                ownerName.setColor(ChatColor.AQUA);
                success.addExtra(ownerName);
                success.addExtra(".");
    
                FusionMarket.sendUserMessage(sender, success);
    
                break;
            }
            case "price": { // /mkt set [id] price [Username/UUID]
                if (args.length != 1) {
                    BaseCommand.argumentsReject(sender, "/mkt set [id] price [Username/UUID]", Help.CMDHELP_SET_BASIC);
                    return;
                }
                try {
                    Integer.parseInt(args[0]);
                } catch (NumberFormatException e) {
                    BaseCommand.commandFailure(sender, "Argument must be a positive integer.");
                    return;
                }
                int newPrice = Integer.parseInt(args[0]);
                if (newPrice < 1) {
                    BaseCommand.commandFailure(sender, "Argument must be a positive integer.");
                }
    
                market.setPrice(newPrice);
                TextComponent success = new TextComponent("Set price of '");
                success.setColor(ChatColor.BLUE);
                TextComponent marketId = new TextComponent(market.getUniqueId());
                marketId.setColor(ChatColor.AQUA);
                success.addExtra(marketId);
                success.addExtra("' to ");
                TextComponent price = new TextComponent(newPrice + "");
                price.setColor(ChatColor.AQUA);
                success.addExtra(price);
                success.addExtra(".");
    
                FusionMarket.sendUserMessage(sender, success);
    
                break;
            }
            case "region": { // /mkt set [id] region [Region ID] <world>
                if (args.length != 1 && args.length != 2) {
                    BaseCommand.argumentsReject(sender, "/mkt set [id] region [Region ID] <world>", Help.CMDHELP_SET_BASIC);
                    return;
                }
                
                World world = null;
                if (args.length == 1) {
                    if (sender instanceof Player)
                        world = ((Player) sender).getWorld();
                    else {
                        BaseCommand.argumentsReject(sender, "World argument is mandatory for non-Players.", Help.CMDHELP_SET_BASIC);
                        return;
                    }
                } else {
                    world = Bukkit.getWorld(args[1]);
                }
                if (world == null) {
                    BaseCommand.commandFailure(sender, "Invalid world.");
                    return;
                }
    
                ProtectedRegion newRegion = DataManager.getRegionManager(world).getRegion(args[0]);
                if (newRegion == null) {
                    BaseCommand.commandFailure(sender, "Invalid region.");
                    return;
                }
    
                TextComponent success = new TextComponent("Set Region for '");
                success.setColor(ChatColor.BLUE);
                TextComponent marketId = new TextComponent(market.getUniqueId());
                marketId.setColor(ChatColor.AQUA);
                success.addExtra(marketId);
                success.addExtra("' to ");
                TextComponent regionName = new TextComponent(newRegion.getId());
                regionName.setColor(ChatColor.AQUA);
                success.addExtra(regionName);
                success.addExtra(".");
    
                FusionMarket.sendUserMessage(sender, success);
                
                break;
            }
            case "trusted": {// /mkt set [id] trusted [add/remove/reset] <Username/UUID>
                if (args.length == 1 && !args[0].equalsIgnoreCase("reset") || args.length == 2 &&
                        !args[0].equalsIgnoreCase("add") &&
                        !args[0].equalsIgnoreCase("remove")) {
                    BaseCommand.argumentsReject(sender, "/mkt set [id] trusted [add/remove/reset] <Username/UUID>", Help.CMDHELP_SET_BASIC);
                    return;
                }
                
                switch (args[0]) {
                    case "reset": {
                        market.setMembers(new UUID[0]);
                        
                        TextComponent success = new TextComponent("Successfully reset Trusted members for '");
                        success.setColor(ChatColor.BLUE);
                        TextComponent marketId = new TextComponent(market.getUniqueId());
                        marketId.setColor(ChatColor.AQUA);
                        success.addExtra(marketId);
                        success.addExtra(".");
                        
                        FusionMarket.sendUserMessage(sender, success);
                        
                        break;
                    }
                    case "add": {
                        OfflinePlayer addMe;
                        if (args[1].length() == 36) { // It's a UUID
                            addMe = Bukkit.getOfflinePlayer(UUID.fromString(args[1]));
                        } else {
                            for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
                                if (offlinePlayer.getName() != null && offlinePlayer.getName().equalsIgnoreCase(args[1])) {
                                    addMe = offlinePlayer;
                                    break;
                                }
                            }
                            BaseCommand.commandFailure(sender, "Argument must be a valid username or UUID.");
                            return;
                        }
                        
                        ArrayList<UUID> allMembers = (ArrayList<UUID>)Arrays.asList(market.getMembers());
                        boolean alreadyAdded = false;
                        for (UUID uuid : allMembers) {
                            if (addMe.getUniqueId().equals(uuid)) {
                                alreadyAdded = true;
                                break;
                            }
                        }
                        
                        if (!alreadyAdded) {
                            allMembers.add(addMe.getUniqueId());
                            market.setMembers(allMembers.toArray(new UUID[0]));
    
                            TextComponent success = new TextComponent("Added new Trusted user '");
                            success.setColor(ChatColor.BLUE);
                            TextComponent name = new TextComponent(addMe.getName() + " [" + addMe.getUniqueId() + "]");
                            name.setColor(ChatColor.AQUA);
                            success.addExtra(name);
                            success.addExtra("' to ");
                            TextComponent marketId = new TextComponent(market.getUniqueId());
                            marketId.setColor(ChatColor.AQUA);
                            success.addExtra(marketId);
                            success.addExtra(".");
                            
                            FusionMarket.sendUserMessage(sender, success);
                        } else {
                            TextComponent failure = new TextComponent("User was already Trusted: '");
                            failure.setColor(ChatColor.BLUE);
                            TextComponent name = new TextComponent(addMe.getName() + " [" + addMe.getUniqueId() + "]");
                            name.setColor(ChatColor.AQUA);
                            failure.addExtra(name);
                            failure.addExtra("' in ");
                            TextComponent marketId = new TextComponent(market.getUniqueId());
                            marketId.setColor(ChatColor.AQUA);
                            failure.addExtra(marketId);
                            failure.addExtra(".");
                            
                            FusionMarket.sendUserMessage(sender, failure);
                        }
    
                        break;
                    }
                    case "remove": {
                        OfflinePlayer removeMe;
                        if (args[1].length() == 36) { // It's a UUID
                            removeMe = Bukkit.getOfflinePlayer(UUID.fromString(args[1]));
                        } else {
                            for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
                                if (offlinePlayer.getName() != null && offlinePlayer.getName().equalsIgnoreCase(args[1])) {
                                    removeMe = offlinePlayer;
                                    break;
                                }
                            }
                            BaseCommand.commandFailure(sender, "Argument must be a valid username or UUID.");
                            return;
                        }
    
                        ArrayList<UUID> allMembers = (ArrayList<UUID>)Arrays.asList(market.getMembers());
                        ArrayList<UUID> output = new ArrayList<>();
                        boolean neverExisted = true;
                        for (UUID uuid : allMembers) {
                            if (!removeMe.getUniqueId().equals(uuid))
                                output.add(uuid);
                            else
                                neverExisted = false;
                        }
                        
                        if (!neverExisted) {
                            market.setMembers(output.toArray(new UUID[0]));
        
                            TextComponent success = new TextComponent("Removed Trusted user '");
                            success.setColor(ChatColor.BLUE);
                            TextComponent name = new TextComponent(removeMe.getName() + " [" + removeMe.getUniqueId() + "]");
                            name.setColor(ChatColor.AQUA);
                            success.addExtra(name);
                            success.addExtra("' from ");
                            TextComponent marketId = new TextComponent(market.getUniqueId());
                            marketId.setColor(ChatColor.AQUA);
                            success.addExtra(marketId);
                            success.addExtra(".");
                            
                            FusionMarket.sendUserMessage(sender, success);
                        } else {
                            TextComponent failure = new TextComponent("User was NOT Trusted: '");
                            failure.setColor(ChatColor.BLUE);
                            TextComponent name = new TextComponent(removeMe.getName() + " [" + removeMe.getUniqueId() + "]");
                            name.setColor(ChatColor.AQUA);
                            failure.addExtra(name);
                            failure.addExtra("' in ");
                            TextComponent marketId = new TextComponent(market.getUniqueId());
                            marketId.setColor(ChatColor.AQUA);
                            failure.addExtra(marketId);
                            failure.addExtra(".");
    
                            FusionMarket.sendUserMessage(sender, failure);
                        }
                        
                        break;
                    }
                }
                break;
            }
            case "uniqueid":
            case "id":
            case "name": {// /mkt set [id] uniqueid [New ID]
                if (args.length != 1) {
                    BaseCommand.argumentsReject(sender, "/mkt set [id] uniqueid [New ID]", Help.CMDHELP_SET_BASIC);
                    return;
                } else if (FusionMarket.getInstance().getDataManager().getMarket(args[0]) != null) {
                    BaseCommand.commandFailure(sender, "Market already exists by ID '" + market.getUniqueId());
                    return;
                }
                
                TextComponent success = new TextComponent("Set Unique ID of '");
                success.setColor(ChatColor.BLUE);
                TextComponent marketId = new TextComponent(market.getUniqueId());
                marketId.setColor(ChatColor.AQUA);
                success.addExtra(marketId);
                market.setUniqueId(args[0]); // This is placed here so I don't have to make an "oldname" variable
                success.addExtra("' to '");
                TextComponent newId = new TextComponent(market.getUniqueId());
                newId.setColor(ChatColor.AQUA);
                success.addExtra(newId);
                success.addExtra("'.");
    
                FusionMarket.sendUserMessage(sender, success);
    
                break;
            }
            case "help":
            default:
                FusionMarket.sendUserMessage(sender, new TextComponent("Admin help coming soon?"));
                return;
        }
    
        FusionMarket.getInstance().getDataManager().saveDataFile();
        FusionMarket.getInstance().getDataManager().loadDataFile();
    }
}
