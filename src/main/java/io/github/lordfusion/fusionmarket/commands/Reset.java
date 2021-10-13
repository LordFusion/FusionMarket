package io.github.lordfusion.fusionmarket.commands;

import io.github.lordfusion.fusionmarket.FusionMarket;
import io.github.lordfusion.fusionmarket.Market;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;

public class Reset
{
    /**
     * /mkt reset [plotname]
     * @param sender Command sender
     * @param args   Command arguments
     */
    public static void run(CommandSender sender, String[] args)
    {
        if (args.length != 1)
            return;
        
        Market market = FusionMarket.getInstance().getDataManager().getMarket(args[0]);
        if (market == null) {
            BaseCommand.commandFailure(sender, "Invalid Market ID: " + args[0]);
            return;
        }
        
        market.resetOwnership();
        
        FusionMarket.sendConsoleInfo("Market ownership reset for plot '" + market.getUniqueId() + "'.");
        TextComponent msg = new TextComponent("Successfully reset plot '");
        msg.setColor(ChatColor.BLUE);
        TextComponent msg2 = new TextComponent(market.getUniqueId());
        msg2.setColor(ChatColor.AQUA);
        msg.addExtra(msg2);
        msg.addExtra("'.");
        FusionMarket.sendUserMessage(sender, msg);
    }
}
