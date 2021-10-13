package io.github.lordfusion.fusionmarket.commands;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import io.github.lordfusion.fusionmarket.DataManager;
import io.github.lordfusion.fusionmarket.FusionMarket;
import io.github.lordfusion.fusionmarket.Market;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

public class Delete
{
    /**
     * /mkt delete [plotname]
     * Deletes the given market and its underlying region.
     * @param sender Command sender
     * @param args   Command arguments
     */
    public static void run(CommandSender sender, String[] args)
    {
        DataManager dataManager = FusionMarket.getInstance().getDataManager();
        Market market = dataManager.getMarket(args[0]);
        
        if (market == null) {
            TextComponent noMarketFound = new TextComponent("Market '" + args[0] + "' does not exist.");
            noMarketFound.setColor(ChatColor.RED);
            FusionMarket.sendUserMessage(sender, noMarketFound);
            return;
        }
        
        ProtectedRegion region = market.getRegion();
        String regionId = null;
        if (region != null)
            regionId = region.getId();
        String world = dataManager.getMarket(args[0]).getWorld();
        dataManager.removeMarket(market, true);
        
        if (dataManager.getMarket(args[0]) == null) {
            TextComponent success = new TextComponent("Market '" + args[0] + "' was removed.");
            success.setColor(ChatColor.GREEN);
            FusionMarket.sendUserMessage(sender, success);
        } else {
            TextComponent failure = new TextComponent("Market '" + args[0] + "' was NOT removed.");
            failure.setColor(ChatColor.RED);
            FusionMarket.sendUserMessage(sender, failure);
        }
        
        if (regionId == null) {
            TextComponent neverRegion = new TextComponent("Region '" + regionId + "' never existed.");
            neverRegion.setColor(ChatColor.GOLD);
            FusionMarket.sendUserMessage(sender, neverRegion);
            return;
        }
        
        if (!DataManager.getRegionManager(Bukkit.getWorld(world)).hasRegion(regionId)) {
            TextComponent success = new TextComponent("Region '" + regionId + "' was removed.");
            success.setColor(ChatColor.GREEN);
            FusionMarket.sendUserMessage(sender, success);
        } else {
            TextComponent failure = new TextComponent("Region '" + regionId + "' was NOT removed.");
            failure.setColor(ChatColor.RED);
            FusionMarket.sendUserMessage(sender, failure);
        }
    }
}
