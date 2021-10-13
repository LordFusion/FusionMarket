package io.github.lordfusion.fusionmarket.commands;

import io.github.lordfusion.fusionmarket.FusionMarket;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AdminShopMode
{
    /**
     * /mkt adminshopmode <true/false>
     * @param sender Command sender
     * @param args   Command arguments
     */
    public static void run(CommandSender sender, String[] args)
    {
        // Command must be run by a player with 0-1 arguments
        if (args.length > 1 || !(sender instanceof Player))
            return;
        Player player = ((Player)sender).getPlayer();
        if (player == null)
            return;
    
        boolean newValue;
        if (args.length == 0) {
            newValue = !FusionMarket.getInstance().getDataManager().isInAdminShopCreationMode(player);
        } else {
            newValue = Boolean.parseBoolean(args[0]);
        }
    
        FusionMarket.getInstance().getDataManager().setInAdminShopCreationMode(player, newValue);
        FusionMarket.sendConsoleInfo("Admin Shop mode set to " + newValue + " for " + player.getName() + ".");
    
        TextComponent msg1 = new TextComponent("Admin Shop mode has been ");
        msg1.setColor(ChatColor.DARK_GREEN);
        TextComponent msg2;
        if (newValue) {
            msg2 = new TextComponent("enabled");
            msg2.setColor(ChatColor.GREEN);
        } else {
            msg2 = new TextComponent("disabled");
            msg2.setColor(ChatColor.RED);
        }
        msg1.addExtra(msg2);
        msg1.addExtra(".");
        FusionMarket.sendUserMessage(sender, msg1);
    }
}
