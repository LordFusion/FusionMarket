package io.github.lordfusion.fusionmarket.commands;

import io.github.lordfusion.fusionmarket.FusionMarket;
import io.github.lordfusion.fusionmarket.Shop;
import io.github.lordfusion.fusionmarket.utilities.SignManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LimitPerPlayer
{
    /**
     * /mkt limitPerPlayer [Shop ID] <true/false>
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
        
        boolean newValue;
        if (args.length == 2) {
            newValue = Boolean.parseBoolean(args[1]);
        } else {
            newValue = !shop.isLimitPerPlayer();
        }
        
        shop.setLimitPerPlayer(newValue);
    
        TextComponent msg = new TextComponent("'" + shop.getUniqueId() + "' is now set to limit ");
        msg.setColor(ChatColor.BLUE);
        if (newValue)
            msg.addExtra("sales per-customer.");
        else
            msg.addExtra("total sales.");
    
        FusionMarket.sendUserMessage(sender, msg);
        FusionMarket.sendConsoleInfo(sender.getName() + " changed limit-per-customer of '" + shop.getUniqueId() + "' to: " + newValue);
    
        FusionMarket.getInstance().getDataManager().saveDataFile();
        SignManager.generateShopSign(shop, shop.getSign(), false);
    
        // Sometimes the SignManager doesn't update the sign, so we're going to try doing it twice.
        shop.getSign().update(true);
    }
}
