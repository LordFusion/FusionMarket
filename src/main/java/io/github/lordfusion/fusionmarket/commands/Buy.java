package io.github.lordfusion.fusionmarket.commands;

import io.github.lordfusion.fusionmarket.FusionMarket;
import io.github.lordfusion.fusionmarket.Shop;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

public class Buy
{
    /**
     * /mkt buy [ShopID] <quantity>
     * @param player Command sender
     * @param args   Command arguments
     */
    public static void run(Player player, String[] args)
    {
        if (args.length < 1 || args.length > 2 || !player.isOnline())
            return;
        
        Shop shop = FusionMarket.getInstance().getDataManager().getShop(args[0]);
        if (shop == null) {
            BaseCommand.commandFailure(player, "Invalid ShopID.");
            return;
        }
        
        if (shop.getPrice() <= 0) {
            BaseCommand.commandFailure(player, "'" + shop.getUniqueId() + "' is not selling.");
            return;
        }
        
        if (args.length == 2) {
            int quantity = -1;
            try {
                quantity = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                BaseCommand.commandFailure(player, "Invalid quantity: " + args[1]);
            }
    
            if (quantity <= 0) {
                TextComponent msg = new TextComponent("Quantity must be greater than 0.");
                msg.setColor(ChatColor.YELLOW);
                FusionMarket.sendUserMessage(player, msg);
                return;
            }
            
            if (shop.isAdminShop())
                shop.adminShopSellTo(player, Integer.parseInt(args[1]));
            else
                shop.sellTo(player, Integer.parseInt(args[1]));
        } else
            shop.sellTo(player, 1);
    }
}
