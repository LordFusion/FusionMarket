package io.github.lordfusion.fusionmarket.commands;

import io.github.lordfusion.fusionmarket.DataManager;
import io.github.lordfusion.fusionmarket.FusionMarket;
import io.github.lordfusion.fusionmarket.Shop;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;

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
        ArrayList<Shop> invalidShops = new ArrayList<>();
        OfflinePlayer player = null;
        
        if (args.length == 1) {
            player = DataManager.findPlayer(args[0]);
            if (player == null) {
                BaseCommand.commandFailure(sender, "Invalid player: " + args[0]);
                return;
            }
        }
        
        for (Shop shop : allShops) {
            // Verify username
            if (player != null && !shop.getOwner().equals(player.getUniqueId()))
                continue;
            
            // Verify chest
            if (shop.getChestLocation() == null) {
                invalidShops.add(shop);
                continue;
            } else {
                World world = Bukkit.getServer().getWorld(shop.getChestLocation().getWorldName());
                if (world == null) {
                    FusionMarket.sendConsoleWarn("Could not verify chest for shop '" + shop.getUniqueId() + "'.");
                } else {
                    if (!DataManager.getChestSubstitutes().contains(world.getBlockAt(shop.getChestLocation().asLocation()).getType())) {
                        invalidShops.add(shop);
                        continue;
                    }
                }
            }
            
            // Verify sign
            if (shop.getSign() == null) {
                invalidShops.add(shop);
                continue;
            } else {
                World world = shop.getSign().getWorld();
                if (world == null) {
                    FusionMarket.sendConsoleWarn("Could not verify sign for shop '" + shop.getUniqueId() + "'.");
                } else {
                    if (!(world.getBlockAt(shop.getSign().getLocation()).getState() instanceof Sign)) {
                        invalidShops.add(shop);
                        continue;
                    }
                }
            }
        }
        
        dataManager.purgeShops(invalidShops.toArray(new Shop[0]));
        
        if (invalidShops.size() == 0) {
            TextComponent failure = new TextComponent("0 shops were purged.");
            failure.setColor(ChatColor.RED);
            FusionMarket.sendUserMessage(sender, failure);
            return;
        } else {
            TextComponent success = new TextComponent(invalidShops.size() + " shops were purged.");
            success.setColor(ChatColor.GREEN);
            FusionMarket.sendUserMessage(sender, success);
            return;
        }
    }
}
