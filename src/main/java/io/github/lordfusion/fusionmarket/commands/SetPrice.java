package io.github.lordfusion.fusionmarket.commands;

import io.github.lordfusion.fusionmarket.FusionMarket;
import io.github.lordfusion.fusionmarket.Shop;
import io.github.lordfusion.fusionmarket.utilities.SignManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetPrice
{
    /**
     * /mkt setprice [ShopID] [Price]
     * @param args
     */
    public static void run(CommandSender sender, String[] args)
    {
        Shop shop = FusionMarket.getInstance().getDataManager().getShop(args[0]);
        if (shop == null) {
            BaseCommand.commandFailure(sender, "Invalid shop name!");
            return;
        }
        
        if (shop.isAdminShop()) {
            if (!sender.hasPermission("fusion.market.adminshop")) {
                BaseCommand.permissionReject(sender, "fusion.market.adminshop");
                return;
            }
        } else {
            if (sender instanceof Player && !shop.getOwner().equals(((Player) sender).getUniqueId()) && !sender.hasPermission("fusion.market.shops.override")) {
                BaseCommand.permissionReject(sender, "fusion.market.shops.override");
                return;
            }
        }
    
        double newPrice;
        try {
            newPrice = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            BaseCommand.commandFailure(sender, "The price you entered was not valid.");
            return;
        }
        
        shop.setPrice(newPrice);
    
        TextComponent msg = new TextComponent("'" + shop.getUniqueId() + "' is now set to ");
        msg.setColor(ChatColor.BLUE);
        TextComponent msg2;
        if (newPrice == 0) {
            msg2 = new TextComponent("disabled");
            msg2.setColor(ChatColor.RED);
        } else if (newPrice > 0) {
            msg2 = new TextComponent("sell to players for $" + newPrice + " each");
            msg2.setColor(ChatColor.GREEN);
        } else {
            msg2 = new TextComponent("buy from players for $" + Math.abs(newPrice) + " each");
            msg2.setColor(ChatColor.YELLOW);
        }
        msg.addExtra(msg2);
        msg.addExtra(".");
        
        FusionMarket.sendUserMessage(sender, msg);
        FusionMarket.sendConsoleInfo(sender.getName() + " changed price of '" + shop.getUniqueId() + "' to: " + newPrice);
        
        FusionMarket.getInstance().getDataManager().saveDataFile();
        SignManager.generateShopSign(shop, shop.getSign(), false);
        
        // Sometimes the SignManager doesn't update the sign, so we're going to try doing it twice.
        shop.getSign().update(true);
    }
}
