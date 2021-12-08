package io.github.lordfusion.fusionmarket.commands;

import io.github.lordfusion.fusionmarket.DataManager;
import io.github.lordfusion.fusionmarket.FusionMarket;
import io.github.lordfusion.fusionmarket.Shop;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;

public class Purge
{
    /**
     * /mkt purge <username>
     *
     * @param sender Command sender
     * @param args   Command arguments
     */
    public static void run(CommandSender sender, String[] args)
    {
        DataManager dataManager = FusionMarket.getInstance().getDataManager();
        Shop[] allShops = dataManager.getAllShops();
        ArrayList<Shop> shopsToVerify = new ArrayList<>();
        
        if (args.length == 1) {
            // Check if username is valid
            OfflinePlayer player = DataManager.findPlayer(args[0]);
            if (player == null || player.getUniqueId() == null) {
                BaseCommand.commandFailure(sender, "Invalid player: " + args[0]);
                return;
            }
            
            // Get all shops owned by the username
            for (Shop shop : allShops)
                if (shop.getOwner() != null && shop.getOwner().equals(player.getUniqueId()))
                    shopsToVerify.add(shop);
        } else {
            // No username provided; verify all shops
            shopsToVerify.addAll(Arrays.asList(allShops));
        }
        
        // Verify specified shops
        ArrayList<Shop> shopsToPurge = new ArrayList<>();
        for (Shop shop : shopsToVerify) {
            // Verify Unique-ID; all shops must have a UID
            if (shop.getUniqueId() == null) {
                FusionMarket.sendConsoleInfo("Purging shop for null UID.");
                shopsToPurge.add(shop);
                continue;
            }
            
            // Verify owner; All shops must have an owner, unless it's an AdminShop.
            if (shop.getOwner() == null && !shop.isAdminShop()) {
                FusionMarket.sendConsoleInfo("Purging shop for null owner non-admin.");
                shopsToPurge.add(shop);
                continue;
            }
            
            // Verify sign location; All shops must have a sign.
            if (shop.getSign() == null || shop.getSign().getLocation() == null) {
                // todo: verify sign exists at location
                FusionMarket.sendConsoleInfo("Purging shop for missing sign.");
                shopsToPurge.add(shop);
                continue;
            }
            
            // Verify chest location; All non-admin shops must have a chest.
            if (!shop.isAdminShop() && shop.getChestLocation() == null) {
                // todo: verify chest exists at location
                FusionMarket.sendConsoleInfo("Purging shop for missing chest.");
                shopsToPurge.add(shop);
                continue;
            }
        }
        
        dataManager.purgeShops(shopsToPurge.toArray(new Shop[0]));
    
        if (shopsToPurge.size() == 0) {
            TextComponent failure = new TextComponent("0 shops were purged.");
            failure.setColor(ChatColor.RED);
            FusionMarket.sendUserMessage(sender, failure);
            return;
        } else {
            TextComponent success = new TextComponent(shopsToPurge.size() + " shops were purged.");
            success.setColor(ChatColor.GREEN);
            FusionMarket.sendUserMessage(sender, success);
            return;
        }
    }
}
