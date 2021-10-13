package io.github.lordfusion.fusionmarket.commands;

import io.github.lordfusion.fusionmarket.FusionMarket;
import io.github.lordfusion.fusionmarket.Market;
import io.github.lordfusion.fusionmarket.utilities.SignManager;
import io.github.lordfusion.fusionmarket.utilities.SimpleLocation;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.entity.Player;

import java.util.Calendar;

public class Rent
{
    /**
     * /mkt rent [Market ID]
     * @param player   Player that wants to rent the market plot.
     * @param marketId Market plot to be rented.
     */
    public static void run(Player player, String marketId)
    {
        Market market = FusionMarket.getInstance().getDataManager().getMarket(marketId);
        Economy economy = FusionMarket.getInstance().getEconomy();
        
        if (market == null) {
            TextComponent msg = new TextComponent("Market ID invalid: '" + marketId + "'.");
            msg.setColor(ChatColor.RED);
            FusionMarket.sendUserMessage(player, msg);
            return;
        } else if (market.getOwner() != null) {
            if (market.getOwner().equals(player.getUniqueId())) { // EXTEND RENT
                EconomyResponse playerChargeResult = economy.withdrawPlayer(player, market.getPrice());
    
                if (playerChargeResult.type == EconomyResponse.ResponseType.FAILURE ||
                        playerChargeResult.type == EconomyResponse.ResponseType.NOT_IMPLEMENTED) {
                    FusionMarket.sendConsoleWarn(playerChargeResult.errorMessage);
                    TextComponent msg = new TextComponent(playerChargeResult.errorMessage);
                    msg.setColor(ChatColor.DARK_RED);
                    FusionMarket.sendUserMessage(player, msg);
                    return;
                }
                
                FusionMarket.sendConsoleInfo(player.getName() + " extended rent on plot '" + market.getUniqueId() + "'.");
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(market.getEvictionDate());
                calendar.add(Calendar.DATE, market.getRentTime());
                market.setEvictionDate(calendar.getTime());
    
                if (market.getSigns() != null && market.getSigns().length > 0) {
                    for (SimpleLocation sign : market.getSigns()) {
                        SignManager.generateMarketSign(market, sign.asLocation());
                    }
                }
                
                TextComponent msg = new TextComponent("Successfully extended rent until ");
                msg.setColor(ChatColor.BLUE);
                TextComponent msg2 = new TextComponent(FusionMarket.DATE_FORMAT.format(market.getEvictionDate()));
                msg2.setColor(ChatColor.AQUA);
                msg.addExtra(msg2);
                msg.addExtra(".");
                FusionMarket.sendUserMessage(player, msg);
                
                return;
            } else {
                TextComponent msg = new TextComponent("Market is already rented.");
                msg.setColor(ChatColor.RED);
                FusionMarket.sendUserMessage(player, msg);
                return;
            }
        } else if (market.getPrice() <= 0 || market.getRentTime() <= 0) {
            TextComponent msg = new TextComponent("Plot not for sale due to invalid pricing.");
            msg.setColor(ChatColor.RED);
            FusionMarket.sendUserMessage(player, msg);
            return;
        } else if (FusionMarket.getInstance().getEconomy().getBalance(player) < market.getPrice()) {
            TextComponent msg = new TextComponent("You do not have enough money to rent this plot.");
            msg.setColor(ChatColor.RED);
            FusionMarket.sendUserMessage(player, msg);
            return;
        }
    
        EconomyResponse playerChargeResult = economy.withdrawPlayer(player, market.getPrice());
        
        if (playerChargeResult.type == EconomyResponse.ResponseType.FAILURE ||
                playerChargeResult.type == EconomyResponse.ResponseType.NOT_IMPLEMENTED) {
            FusionMarket.sendConsoleWarn(playerChargeResult.errorMessage);
            TextComponent msg = new TextComponent(playerChargeResult.errorMessage);
            msg.setColor(ChatColor.DARK_RED);
            FusionMarket.sendUserMessage(player, msg);
            return;
        }
        
        FusionMarket.sendConsoleInfo(player.getName() + " purchasing Market plot '" + market.getUniqueId() + "'.");
        
        market.setOwner(player.getUniqueId());
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, market.getRentTime());
        market.setEvictionDate(calendar.getTime());
        market.setRegionFlags();
        
        FusionMarket.getInstance().getDataManager().saveDataFile();
        if (market.getSigns() != null && market.getSigns().length > 0) {
            for (SimpleLocation sign : market.getSigns()) {
                SignManager.generateMarketSign(market, sign.asLocation());
            }
        }
        
        TextComponent msg = new TextComponent("Successfully rented '" + market.getUniqueId() + "'! Rent expires " +
                FusionMarket.DATE_FORMAT.format(market.getEvictionDate()) + ".");
        msg.setColor(ChatColor.GREEN);
        FusionMarket.sendUserMessage(player, msg);
    }
}
