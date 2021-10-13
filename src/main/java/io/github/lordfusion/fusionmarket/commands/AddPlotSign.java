package io.github.lordfusion.fusionmarket.commands;

import io.github.lordfusion.fusionmarket.FusionMarket;
import io.github.lordfusion.fusionmarket.Market;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AddPlotSign
{
    /**
     * /mkt addplotsign <plotname>
     * Allows the user to add new auto-filled signs to a market plot.
     * @param sender Command sender
     * @param args   Command arguments
     */
    public static void run(CommandSender sender, String[] args)
    {
        // Command must be run by a player with exactly 1 argument
        if (args.length > 1 || !(sender instanceof Player))
            return;
        Player player = ((Player)sender).getPlayer();
        if (player == null)
            return;
        
        if (args.length == 0) { // Reset the metadata if needed
            if (FusionMarket.getInstance().getDataManager().getApsSelection(player) == null) {
                FusionMarket.sendUserMessage(sender, Help.CMDHELP_ADDPLOTSIGN_BASIC);
            } else {
                FusionMarket.getInstance().getDataManager().setApsSelection(player, null);
                FusionMarket.getInstance().getDataManager().saveDataFile();
                
                TextComponent msg = new TextComponent("Plot reset. No further signs will be added.");
                msg.setColor(ChatColor.DARK_GREEN);
                FusionMarket.sendUserMessage(sender, msg);
            }
            return;
        }
        
        // Find the applicable Market
        Market market = FusionMarket.getInstance().getDataManager().getMarket(args[0]);
        if (market == null) {
            BaseCommand.commandFailure(player, "Invalid Market plot name.");
            return;
        }
        // Set the necessary metadata
        FusionMarket.getInstance().getDataManager().setApsSelection(player, market);
        
        TextComponent msg1 = new TextComponent("Plot set! Click any sign to add it to ");
        msg1.setColor(ChatColor.DARK_GREEN);
        TextComponent msg2 = new TextComponent(market.getUniqueId());
        msg2.setColor(ChatColor.GREEN);
        msg1.addExtra(msg2);
        msg1.addExtra(".");
        FusionMarket.sendUserMessage(sender, msg1);
    }
}
