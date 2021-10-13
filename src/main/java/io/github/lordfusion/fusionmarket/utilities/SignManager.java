package io.github.lordfusion.fusionmarket.utilities;

import com.sun.istack.internal.NotNull;
import io.github.lordfusion.fusionmarket.FusionMarket;
import io.github.lordfusion.fusionmarket.Market;
import io.github.lordfusion.fusionmarket.Shop;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Sign;

import java.text.NumberFormat;
import java.util.Locale;

public class SignManager
{
    public static void generateShopSign(Shop shop, Location location)
    {
        if (!(location.getBlock().getState() instanceof Sign)) {
            FusionMarket.sendConsoleWarn("Attempted to generate shop sign where there is no sign: " + location);
            return;
        }
        Sign sign = (Sign)location.getBlock().getState();
        
        generateShopSign(shop, sign, true);
    }
    
    public static void generateShopSign(@NotNull Shop shop, @NotNull Sign sign, boolean onStartup)
    {
        if (!onStartup)
            FusionMarket.sendConsoleInfo("Generating sign for '" + shop.getUniqueId() + "' at " + new SimpleLocation(sign.getLocation()));
        
        if (shop.isAdminShop()) {
            sign.setLine(0, ChatColor.translateAlternateColorCodes('&',"&9[Admin Shop]"));
            
            if (shop.getLimitAmt() > 0)
                sign.setLine(1, ChatColor.translateAlternateColorCodes('&', "&8Limit "
                    + shop.getReadableLimit()));
            else
                sign.setLine(1, "");
            
            if (shop.getPrice() == 0) { // Can't be bought or sold
                sign.setLine(2, ChatColor.translateAlternateColorCodes('&',"&cIncomplete"));
            } else if (shop.getPrice() > 0) { // Shop sells to the player
                sign.setLine(2, ChatColor.translateAlternateColorCodes('&', "&aBuy " + NumberFormat.getCurrencyInstance(Locale.US).format(shop.getPrice())));
            } else { // Shop buys from the player
                sign.setLine(2, ChatColor.translateAlternateColorCodes('&', "&eSell " + NumberFormat.getCurrencyInstance(Locale.US).format(Math.abs(shop.getPrice()))));
            }
            if (shop.getItem().getDurability() > 0)
                sign.setLine(3, ChatColor.translateAlternateColorCodes('&',"&f" +
                        shop.getItem().getType().getId() + ":" + shop.getItem().getDurability()));
            else
                sign.setLine(3, ChatColor.translateAlternateColorCodes('&',"&f" +
                        shop.getItem().getType().getId()));
        } else {
            sign.setLine(0,ChatColor.translateAlternateColorCodes('&',"&b[FM Shop]"));
    
            if (shop.getOwner() != null)
                sign.setLine(1, Bukkit.getOfflinePlayer(shop.getOwner()).getName());
            else
                sign.setLine(1, "Invalid Owner");
    
            if (shop.getPrice() == 0) { // Can't be bought or sold
                sign.setLine(2, ChatColor.translateAlternateColorCodes('&',"&cIncomplete"));
            } else if (shop.getPrice() > 0) { // Shop sells to the player
                sign.setLine(2, ChatColor.translateAlternateColorCodes('&', "&aBuy " + NumberFormat.getCurrencyInstance(Locale.US).format(shop.getPrice())));
            } else { // Shop buys from the player
                sign.setLine(2, ChatColor.translateAlternateColorCodes('&', "&eSell " + NumberFormat.getCurrencyInstance(Locale.US).format(Math.abs(shop.getPrice()))));
            }
    
            if (shop.getItem().getDurability() > 0)
                sign.setLine(3, ChatColor.translateAlternateColorCodes('&',"&f" +
                        shop.getItem().getType().getId() + ":" + shop.getItem().getDurability()));
            else
                sign.setLine(3, ChatColor.translateAlternateColorCodes('&',"&f" +
                        shop.getItem().getType().getId()));
        }
        
        sign.update(true, false);
    }
    
    public static void generateMarketSign(@NotNull Market market, @NotNull Location location)
    {
        if (!(location.getBlock().getState() instanceof Sign)) {
            FusionMarket.sendConsoleWarn("Attempted to generate market sign where there is no sign: " + location);
            return;
        }
        Sign sign = (Sign)location.getBlock().getState();
        
        generateMarketSign(market, sign);
    }
    
    public static void generateMarketSign(@NotNull Market market, @NotNull Sign sign)
    {
        FusionMarket.sendConsoleInfo("Generating sign for '" + market.getUniqueId() + "' at " + new SimpleLocation(sign.getLocation()));
        
        sign.setLine(0, ChatColor.translateAlternateColorCodes('&', "&d[FM Market]"));
        
        if (market.getOwner() == null) {
            if (market.getPrice() == -1 || market.getRentTime() == -1) {
                sign.setLine(1, ChatColor.translateAlternateColorCodes('&', "&4Setup"));
                sign.setLine(2, ChatColor.translateAlternateColorCodes('&', "&4Incomplete"));
            } else {
                sign.setLine(1, "For Sale");
                sign.setLine(2, ChatColor.translateAlternateColorCodes('&', "&a$" + market.getPrice() + "/" + market.getRentTime() + "days"));
            }
        } else {
            if (market.getOwner() == null)
                sign.setLine(1, ChatColor.translateAlternateColorCodes('&', "&4Unknown Owner"));
            else
                sign.setLine(1, Bukkit.getOfflinePlayer(market.getOwner()).getName());
            
            if (market.getEvictionDate() == null)
                sign.setLine(2, ChatColor.translateAlternateColorCodes('&', "&4Unknown Exp"));
            else
                sign.setLine(2, ChatColor.translateAlternateColorCodes('&', "&8" + FusionMarket.DATE_FORMAT.format(market.getEvictionDate())));
        }
        sign.setLine(3, ChatColor.translateAlternateColorCodes('&', "&f" + market.getUniqueId()));
        sign.update(true);
    }
    
    /**
     * Resets the given sign back to a blank state.
     * @param location Location that contains a sign.
     */
    public static void resetSign(@NotNull Location location)
    {
        if (!(location.getBlock().getState() instanceof Sign)) {
            FusionMarket.sendConsoleWarn("Attempted to reset sign where there is no sign: " + location);
            return;
        }
        Sign sign = (Sign)location.getBlock().getState();
        
        resetSign(sign);
    }
    
    /**
     * Resets the given sign back to a blank state.
     * @param sign Sign to be reset
     */
    public static void resetSign(@NotNull Sign sign)
    {
        sign.setLine(0, "");
        sign.setLine(1, "");
        sign.setLine(2, "");
        sign.setLine(3, "");
        sign.update(true);
    }
}
