package io.github.lordfusion.fusionmarket.commands;

import io.github.lordfusion.fusionmarket.DataManager;
import io.github.lordfusion.fusionmarket.FusionMarket;
import io.github.lordfusion.fusionmarket.Market;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;

public class Remove
{
    /**
     * /mkt remove [plotname]
     *
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
        
        dataManager.removeMarket(market, false);
    
        if (dataManager.getMarket(args[0]) == null) {
            TextComponent success = new TextComponent("Market '" + args[0] + "' was removed.");
            success.setColor(ChatColor.GREEN);
            FusionMarket.sendUserMessage(sender, success);
            return;
        } else {
            TextComponent failure = new TextComponent("Market '" + args[0] + "' was NOT removed.");
            failure.setColor(ChatColor.RED);
            FusionMarket.sendUserMessage(sender, failure);
            return;
        }
    }
}
