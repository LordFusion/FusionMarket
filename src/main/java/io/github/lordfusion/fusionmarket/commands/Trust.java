package io.github.lordfusion.fusionmarket.commands;

import io.github.lordfusion.fusionmarket.FusionMarket;
import io.github.lordfusion.fusionmarket.Market;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

public class Trust
{
    /**
     * /mkt trust/untrust [MarketID] [Username/UUID/RESET]
     * @param player   Player that wants to edit their Market's trusted members
     * @param args     Command arguments
     * @param trust    True to trust, false to untrust
     */
    public static void run(Player player, String[] args, boolean trust)
    {
        if (player == null || args.length != 2)
            return;
        
        Market market = FusionMarket.getInstance().getDataManager().getMarket(args[0]);
        if (market == null) {
            BaseCommand.commandFailure(player, "Invalid Market: '" + args[0] + "'.");
            return;
        }
        if (!market.getOwner().equals(player.getUniqueId())) {
            BaseCommand.commandFailure(player, "Only the Market owner may run this command.");
            return;
        }
        
        if (args[1].equalsIgnoreCase("reset")) {
            market.setMembers(new UUID[0]);
        } else {
            if (trust) {
                OfflinePlayer addMe = null;
                if (args[1].length() == 36) { // It's a UUID
                    addMe = Bukkit.getOfflinePlayer(UUID.fromString(args[1]));
                } else {
                    for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
                        if (offlinePlayer.getName() != null && offlinePlayer.getName().equalsIgnoreCase(args[1])) {
                            addMe = offlinePlayer;
                            break;
                        }
                    }
                }
                if (addMe == null) {
                    BaseCommand.commandFailure(player, "Argument must be a valid username or UUID.");
                    return;
                }
    
    
                boolean alreadyAdded = false;
                ArrayList<UUID> allMembers = null;
                if (market.getMembers() != null && market.getMembers().length > 0) {
                    allMembers = new ArrayList<>(Arrays.asList(market.getMembers()));
    
                    for (UUID uuid : allMembers) {
                        if (addMe.getUniqueId().equals(uuid)) {
                            alreadyAdded = true;
                            break;
                        }
                    }
                } else {
                    allMembers = new ArrayList<>();
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
    
                    FusionMarket.sendUserMessage(player, success);
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
    
                    FusionMarket.sendUserMessage(player, failure);
                }
            } else {
                OfflinePlayer removeMe = null;
                if (args[1].length() == 36) { // It's a UUID
                    removeMe = Bukkit.getOfflinePlayer(UUID.fromString(args[1]));
                } else {
                    for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
                        if (offlinePlayer.getName() != null && offlinePlayer.getName().equalsIgnoreCase(args[1])) {
                            removeMe = offlinePlayer;
                            break;
                        }
                    }
                }
                if (removeMe == null) {
                    BaseCommand.commandFailure(player, "Argument must be a valid username or UUID.");
                    return;
                }
                
                ArrayList<UUID> output = new ArrayList<>();
                boolean neverExisted = true;
                if (market.getMembers() != null && market.getMembers().length > 0) {
                    ArrayList<UUID> allMembers = new ArrayList<>(Arrays.asList(market.getMembers()));
                    for (UUID uuid : allMembers) {
                        if (!removeMe.getUniqueId().equals(uuid))
                            output.add(uuid);
                        else
                            neverExisted = false;
                    }
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
                    
                    FusionMarket.sendUserMessage(player, success);
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
    
                    FusionMarket.sendUserMessage(player, failure);
                }
            }
        }
        market.setRegionFlags();
        FusionMarket.getInstance().getDataManager().saveDataFile();
    }
}
