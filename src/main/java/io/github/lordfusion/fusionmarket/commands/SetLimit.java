package io.github.lordfusion.fusionmarket.commands;

import io.github.lordfusion.fusionmarket.FusionMarket;
import io.github.lordfusion.fusionmarket.Shop;
import io.github.lordfusion.fusionmarket.utilities.SignManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetLimit
{
    /**
     * /mkt setlimit <ShopID> <Limit> <Period>
     * @param sender
     * @param args
     */
    public static void run(CommandSender sender, String[] args)
    {
        Shop shop = FusionMarket.getInstance().getDataManager().getShop(args[0]);
        if (shop == null) {
            BaseCommand.commandFailure(sender, "Invalid shop name!");
            return;
        }
        
        if (shop.isAdminShop() && !sender.hasPermission("fusion.market.adminshop")) {
            BaseCommand.permissionReject(sender, "fusion.market.adminshop");
            return;
        } else if (sender instanceof Player && !shop.isAdminShop() && !shop.getOwner().equals(((Player) sender).getUniqueId())
                && !sender.hasPermission("fusion.market.shops.override")) {
            BaseCommand.permissionReject(sender, "fusion.market.shops.override");
            return;
        }
        
        // Verify limit count
        int newLimit = -1;
        try {
            newLimit = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            BaseCommand.commandFailure(sender, "Invalid limit number: " + args[1]);
            return;
        }
        if (newLimit < 0) {
            BaseCommand.commandFailure(sender, "Limit number cannot be less than 0.");
            return;
        }
    
        int limitLength = -1;
        Shop.LimitPeriod limitPeriod = Shop.LimitPeriod.ALLTIME;
        if (newLimit > 0) {
            // Verify limit period
            if (!Shop.LimitPeriod.getValidValues().contains(Character.toUpperCase(args[2].charAt(args[2].length() - 1)))) {
                BaseCommand.commandFailure(sender, "Invalid time unit. Possible values: " + Shop.LimitPeriod.getValidValues());
                return;
            }
            limitPeriod = Shop.LimitPeriod.translate(args[2].charAt(args[2].length() - 1));
            // Verify limit length
            try {
                limitLength = Integer.parseInt(args[2].substring(0, args[2].length() - 1));
            } catch (NumberFormatException e) {
                BaseCommand.commandFailure(sender, "Invalid limit length: " + args[2].substring(0, args[2].length() - 1));
                return;
            }
            if (limitLength < 0) {
                BaseCommand.commandFailure(sender, "Limit length cannot be less than 0.");
                return;
            }
            if (limitPeriod == Shop.LimitPeriod.ALLTIME)
                shop.setLimitLength(1);
            else
                shop.setLimitLength(limitLength);
            shop.setLimitPeriod(Shop.LimitPeriod.translate(args[2].charAt(args[2].length()-1)));
        } else {
            shop.setLimitLength(0);
            shop.setLimitPeriod(Shop.LimitPeriod.ALLTIME);
        }
        
        shop.setLimitAmt(newLimit);
    
        TextComponent msg = new TextComponent("Limit for '" + shop.getUniqueId() + "' is now set to ");
        msg.setColor(ChatColor.BLUE);
        TextComponent msg2;
        if (newLimit == 0) {
            msg2 = new TextComponent("disabled");
            msg2.setColor(ChatColor.RED);
        } else {
            msg2 = new TextComponent(newLimit + " per " + limitLength + " " + limitPeriod);
            msg2.setColor(ChatColor.AQUA);
        }
        msg.addExtra(msg2);
        msg.addExtra(".");
    
        FusionMarket.sendUserMessage(sender, msg);
        FusionMarket.sendConsoleInfo(sender.getName() + " changed limit of '" + shop.getUniqueId() + "' to: " +
                newLimit + "/" + limitLength + " " + limitPeriod);
    
        FusionMarket.getInstance().getDataManager().saveDataFile();
        SignManager.generateShopSign(shop, shop.getSign(), false);
    
        // Sometimes the SignManager doesn't update the sign, so we're going to try doing it twice.
        shop.getSign().update(true);
    }
}
