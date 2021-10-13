package io.github.lordfusion.fusionmarket;

import io.github.lordfusion.fusionmarket.utilities.PurchaseRecord;
import io.github.lordfusion.fusionmarket.utilities.SimpleLocation;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerLoginEvent;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Date;

public class EventListener implements Listener
{
    private FusionMarket mainPlugin;
    EventListener(FusionMarket plugin)
    {
        this.mainPlugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void monitorPlayerInteractEvent(PlayerInteractEvent event)
    {
        // Should prevent console spam from Thaumcraft golems and similar non-player entities.
        if (event.getPlayer() == null || event.getClickedBlock() == null)
            return;
        
        Player player = event.getPlayer();
        // All players passing events should be online, but some modded "players" still throw events.
        if (!player.isOnline())
            return;
        DataManager dataManager = this.mainPlugin.getDataManager();
        Block block = event.getClickedBlock();
        
        /* Sign Linking ******************************************************************************** Sign Linking */
        if ((block.getType().toString().equalsIgnoreCase("wall_sign") ||
                block.getType().toString().equalsIgnoreCase("sign_post")) &&
                dataManager.getApsSelection(player) != null) {
            Market market = dataManager.getApsSelection(player);
    
            Market otherMarket = FusionMarket.getInstance().getDataManager().getMarket(block.getLocation());
            while (otherMarket != null) {
                otherMarket.removeSign(new SimpleLocation(block.getLocation()));
                
                TextComponent dupedSignMsg = new TextComponent("Removed duplicate sign from other Market: '");
                dupedSignMsg.setColor(ChatColor.GOLD);
                TextComponent otherMktMsg = new TextComponent(otherMarket.getUniqueId());
                otherMktMsg.setColor(ChatColor.YELLOW);
                dupedSignMsg.addExtra(otherMktMsg);
                dupedSignMsg.addExtra("'.");
                
                FusionMarket.sendUserMessage(player, dupedSignMsg);
                otherMarket = FusionMarket.getInstance().getDataManager().getMarket(block.getLocation());
            }
    
            market.addSign((Sign)block.getState());
            FusionMarket.sendConsoleInfo(event.getPlayer().getName() + " added sign to '" + market.getUniqueId()
                    + "' at " + new SimpleLocation(block.getLocation()));
        }
        // Sign Interaction ************************************************************************ Sign Interaction //
        else if (block.getState() instanceof Sign) {
            Shop shop = dataManager.getShop(block.getLocation());
            if (shop != null) {
                // AdminShop info
                if (shop.isAdminShop() && player.hasPermission("fusion.market.adminshop") && dataManager.isAdminMode(player)) {
                    FusionMarket.sendUserMessages(player, shop.getAdminInfo());
                }
                // Admin info
                else if (!shop.isAdminShop() && player.hasPermission("fusion.market.manage") && dataManager.isAdminMode(player)) {
                    FusionMarket.sendUserMessages(player, shop.getAdminInfo());
                }
                // Owner info
                else if (!shop.isAdminShop() && shop.getOwner().equals(player.getUniqueId())) { /* Owner Info ***/
                    FusionMarket.sendUserMessages(player, shop.getOwnerInfo());
                }
                // Not ready!
                else if (shop.getPrice() == 0) {
                    FusionMarket.sendUserMessages(player, shop.getIncompleteInfo());
                }
                // Ready!
                else {
                    shopInteraction(player, event, shop);
                }
                return;
            // Making AdminShops
            } else if (dataManager.isInAdminShopCreationMode(player)) {
                // Verify shop-creation eligibility
                if (!player.isSneaking() || event.getItem() == null || event.getItem().getType().toString().equalsIgnoreCase("sign"))
                    return;
                Sign sign = (Sign)block.getLocation().getBlock().getState();
                if (sign == null)
                    return;
                shop = dataManager.createNewAdminShop(sign, event.getItem());
    
                if (shop == null) {
                    FusionMarket.sendConsoleInfo("AdminShop creation FAILED for " + player.getName());
                    player.spigot().sendMessage(new TextComponent("AdminShop creation failed."));
                } else {
                    FusionMarket.sendConsoleInfo("Created an AdminShop to sell " + event.getItem().getType());
                    player.spigot().sendMessage(new TextComponent("AdminShop created!"));
                }
            }
            
            Market market = dataManager.getMarketByInfoSign(block.getLocation());
            if (market != null) {
                // Admin interaction
                if (player.hasPermission("fusion.market.manage") && dataManager.isAdminMode(player)) {
                    FusionMarket.sendUserMessages(player, market.getAdminInfo());
                }
                // Not Ready for Players interaction
                else if (market.getPrice() == -1 || market.getRentTime() == -1) {
                    FusionMarket.sendUserMessages(player, market.getNotReadyInfo());
                }
                // Purchase interaction
                else if (market.getOwner() == null) {
                    FusionMarket.sendUserMessages(player, market.getForSaleInfo());
                }
                // Owner interaction
                else if (market.getOwner().equals(player.getUniqueId())) {
                    FusionMarket.sendUserMessages(player, market.getOwnerInfo());
                }
                // Generic interaction
                else {
                    FusionMarket.sendUserMessages(player, market.getGenericInfo());
                }
                return;
            }
        }
        // Chest Interaction ********************************************************************** Chest Interaction //
        else if (DataManager.getChestSubstitutes().contains(block.getType())) {
            Shop shop = dataManager.getShop(block.getLocation());
            if (shop == null) { /* Shop Creation ******************************************** Shop Creation */
                // Verify shop-creation eligibility
                if (!player.isSneaking() || event.getItem() == null || event.getItem().getType().toString().equalsIgnoreCase("sign"))
                    return;
                
                Market marketPlot = dataManager.getMarket(block.getLocation());
                if (marketPlot == null) {
                    return;
                }
                if (!marketPlot.getOwner().equals(player.getUniqueId()))
                    if (marketPlot.getMembers() == null || !Arrays.asList(marketPlot.getMembers()).contains(player.getUniqueId()))
                        return;
                
                // Check to see if a shop already exists here
                if (dataManager.getShop(block.getLocation()) != null)
                    return;
                // Todo: Allow shop creation everywhere the user has building perm, using Towny/WorldGuard/etc integration
                
                Sign sign = null;
                if (block.getLocation().add(1,0,0).getBlock().getState() instanceof Sign)
                    sign = (Sign)block.getLocation().add(1,0,0).getBlock().getState();
                else if (block.getLocation().add(-1,0,0).getBlock().getState() instanceof Sign)
                    sign = (Sign)block.getLocation().add(-1,0,0).getBlock().getState();
                else if (block.getLocation().add(0,0,1).getBlock().getState() instanceof Sign)
                    sign = (Sign)block.getLocation().add(0,0,1).getBlock().getState();
                else if (block.getLocation().add(0,0,-1).getBlock().getState() instanceof Sign)
                    sign = (Sign)block.getLocation().add(0,0,-1).getBlock().getState();
                else if (block.getLocation().add(0,1,0).getBlock().getState() instanceof Sign)
                    sign = (Sign)block.getLocation().add(0,1,0).getBlock().getState();
                else if (block.getLocation().add(0,-1,0).getBlock().getState() instanceof Sign)
                    sign = (Sign)block.getLocation().add(0,-1,0).getBlock().getState();
                else
                    return;
                
                // Verify that they're not over their plot limit
                int preexistingShops = dataManager.getShopCount(player);
                if (!player.hasPermission("fusion.market.shoplimit.vip4")) {
                    if (!player.hasPermission("fusion.market.shoplimit.vip3")) {
                        if (!player.hasPermission("fusion.market.shoplimit.vip2")) {
                            if (!player.hasPermission("fusion.market.shoplimit.vip1")) {
                                if (!player.hasPermission("fusion.market.shoplimit.vip0")) {
                                    if (preexistingShops >= 5) {
                                        TextComponent tooManyShops = new TextComponent("You have exceeded your maximum number of shops.");
                                        tooManyShops.setColor(ChatColor.RED);
                                        FusionMarket.sendUserMessage(player, tooManyShops);
                                        return;
                                    }
                                } else if (preexistingShops >= 10) {
                                    TextComponent tooManyShops = new TextComponent("You have exceeded your maximum number of shops.");
                                    tooManyShops.setColor(ChatColor.RED);
                                    FusionMarket.sendUserMessage(player, tooManyShops);
                                    return;
                                }
                            } else if (preexistingShops >= 15) {
                                TextComponent tooManyShops = new TextComponent("You have exceeded your maximum number of shops.");
                                tooManyShops.setColor(ChatColor.RED);
                                FusionMarket.sendUserMessage(player, tooManyShops);
                                return;
                            }
                        } else if (preexistingShops >= 20) {
                            TextComponent tooManyShops = new TextComponent("You have exceeded your maximum number of shops.");
                            tooManyShops.setColor(ChatColor.RED);
                            FusionMarket.sendUserMessage(player, tooManyShops);
                            return;
                        }
                    } else if (preexistingShops >= 25) {
                        TextComponent tooManyShops = new TextComponent("You have exceeded your maximum number of shops.");
                        tooManyShops.setColor(ChatColor.RED);
                        FusionMarket.sendUserMessage(player, tooManyShops);
                        return;
                    }
                } else if (preexistingShops >= 30) {
                    TextComponent tooManyShops = new TextComponent("You have exceeded your maximum number of shops.");
                    tooManyShops.setColor(ChatColor.RED);
                    FusionMarket.sendUserMessage(player, tooManyShops);
                    return;
                }
                
                
                // Create the new shop!
                shop = FusionMarket.getInstance().getDataManager()
                        .createNewShop(player, block.getLocation(), event.getItem(), sign);
                if (shop == null) {
                    FusionMarket.sendConsoleInfo("Shop creation FAILED for " + player.getName());
                    player.spigot().sendMessage(new TextComponent("Shop creation failed."));
                } else {
                    FusionMarket.sendConsoleInfo("Created a shop for " + player.getName() + " to sell " + event.getItem().getType());
                    player.spigot().sendMessage(new TextComponent("Shop created!"));
                }
            }
            // Admin info
            else if (player.hasPermission("fusion.market.manage") && dataManager.isAdminMode(player)) {
                FusionMarket.sendUserMessages(player, shop.getAdminInfo());
            }
            // Owner info
            else if (shop.getOwner().equals(player.getUniqueId())) { /* Owner Info ***/
                FusionMarket.sendUserMessages(player, shop.getOwnerInfo());
            }
            // Not ready!
            else if (shop.getPrice() == 0) {
                FusionMarket.sendUserMessages(player, shop.getIncompleteInfo());
            }
            // Ready!
            else {
                shopInteraction(player, event, shop);
            }
        }
        return;
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreakEvent(BlockBreakEvent event)
    {
        if (event.getBlock() == null || event.getPlayer() == null)
            return;
        if (!(event.getBlock().getState() instanceof Sign) && !DataManager.getChestSubstitutes().contains(event.getBlock().getType()))
            return;
        
        Shop shop = FusionMarket.getInstance().getDataManager().getShop(event.getBlock().getLocation());
        if (shop == null)
            return;
        
        if (!FusionMarket.getInstance().getDataManager().canBreakShop(event.getPlayer(), shop)) {
            event.setCancelled(true);
            return;
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void monitorBlockBreakEvent(BlockBreakEvent event)
    {
        if (event.isCancelled() || event.getBlock() == null || event.getPlayer() == null)
            return;
        if (!(event.getBlock().getState() instanceof Sign) && !DataManager.getChestSubstitutes().contains(event.getBlock().getType()))
            return;
        
        DataManager dataManager = FusionMarket.getInstance().getDataManager();
        
        Shop shop = dataManager.getShop(event.getBlock().getLocation());
        if (dataManager.canBreakShop(event.getPlayer(), shop)) {
            dataManager.removeShop(shop);
            TextComponent msg = new TextComponent("Shop removed.");
            msg.setColor(ChatColor.BLUE);
            FusionMarket.sendUserMessage(event.getPlayer(), msg);
            return;
        }
        
        Market market = dataManager.getMarketByInfoSign(event.getBlock().getLocation());
        if (market != null && event.getPlayer() != null && event.getPlayer().hasPermission("fusion.market.manage")) {
            market.removeSign(new SimpleLocation(event.getBlock().getLocation()));
            TextComponent msg = new TextComponent("Market sign removed.");
            msg.setColor(ChatColor.BLUE);
            FusionMarket.sendUserMessage(event.getPlayer(), msg);
            return;
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void monitorPlayerLoginEvent(PlayerLoginEvent event)
    {
        Player player = event.getPlayer();
        if (player == null)
            return;
        
        // Check for offline purchase records
        Bukkit.getScheduler().runTaskLaterAsynchronously(this.mainPlugin,
                new LoginTransactionHistoryHandler(this.mainPlugin, player), 40);
        
        // Check for upcoming shop eviction
        Market market = FusionMarket.getInstance().getDataManager().getMarket(player);
        if (market != null) {
            FusionMarket.sendConsoleInfo("Player has market, scheduling eviction warning task");
            Bukkit.getScheduler().runTaskLaterAsynchronously(this.mainPlugin,
                    new LoginShopEvictionWarningHandler(player, market), 60);
        }
    }
    
    private void shopInteraction(Player player, PlayerInteractEvent event, Shop shop)
    {
        if (player.isSneaking() && event.getAction() == Action.RIGHT_CLICK_BLOCK && shop.getPrice() > 0) { // Buy
            FusionMarket.sendConsoleInfo("Shop " + shop.getUniqueId() + " selling to " + player.getName());
            if (shop.isAdminShop())
                shop.adminShopSellTo(player, 1);
            else
                shop.sellTo(player, 1);
        } else if (player.isSneaking() && event.getAction() == Action.LEFT_CLICK_BLOCK && shop.getPrice() < 0) { // Sell
            FusionMarket.sendConsoleInfo("Shop " + shop.getUniqueId() + " buying from " + player.getName());
            if (shop.isAdminShop())
                shop.adminShopBuyFrom(player, 1);
            else
                shop.buyFrom(player, 1);
        } else if (!player.isSneaking() && event.getAction() == Action.RIGHT_CLICK_BLOCK) { // Info
            FusionMarket.sendConsoleInfo("Shop " + shop.getUniqueId() + " showing info to " + player.getName());
            FusionMarket.sendUserMessages(player, shop.getPurchaseInfo(player));
        }
    }
    
    private class LoginTransactionHistoryHandler implements Runnable
    {
        private FusionMarket mainPlugin;
        private Player player;
        LoginTransactionHistoryHandler(FusionMarket plugin, Player player)
        {
            this.mainPlugin = plugin;
            this.player = player;
        }
        
        /**
         * When an object implementing interface <code>Runnable</code> is used
         * to create a thread, starting the thread causes the object's
         * <code>run</code> method to be called in that separately executing
         * thread.
         * <p>
         * The general contract of the method <code>run</code> is that it may
         * take any action whatsoever.
         *
         * @see Thread#run()
         */
        @Override
        public void run()
        {
            if (!player.isOnline())
                return;
            PurchaseRecord[] offlineRecords = this.mainPlugin.getDataManager().getOfflineRecords(player, true);
            if (offlineRecords == null) {
                FusionMarket.sendConsoleInfo("No offline records for " + player.getName() + ".");
                return;
            }
            TextComponent[] messages = new TextComponent[offlineRecords.length + 1];
            messages[0] = new TextComponent("While you were away, you missed:");
            messages[0].setColor(ChatColor.DARK_AQUA);
            messages[0].setItalic(true);
            for (int i=0; i<offlineRecords.length; i++) {
                messages[i+1] = offlineRecords[i].read();
            }
            
            if (player.isOnline())
                FusionMarket.sendUserMessages(player, messages);
            else {
                FusionMarket.sendConsoleWarn("Prepared transaction history for offline player: " + player.getName());
                // Go back and mark them as unread, thanks
                for (int i=0; i<offlineRecords.length; i++) {
                    offlineRecords[i].setSeen(false);
                }
                FusionMarket.getInstance().getDataManager().saveRecordsFile();
            }
        }
    }
    
    private class LoginShopEvictionWarningHandler implements Runnable
    {
        private Player player;
        private Market market;
        LoginShopEvictionWarningHandler(Player player, Market market)
        {
            this.player = player;
            this.market = market;
        }
    
        @Override
        public void run()
        {
            FusionMarket.sendConsoleInfo("Running eviction warning task!");
            if (!player.isOnline())
                return;
            if (market.getEvictionDate().before(Date.from(Instant.now().plus(3, ChronoUnit.DAYS)))) {
                FusionMarket.sendConsoleInfo("Passed if statements!");
                TextComponent message = new TextComponent("WARNING: ");
                message.setColor(ChatColor.RED);
                message.setBold(true);
                TextComponent message2 = new TextComponent("Market plot '" + market.getUniqueId() + "' will expire on " + FusionMarket.DATE_FORMAT.format(market.getEvictionDate()) + " GMT.");
                message2.setColor(ChatColor.GOLD);
                message.addExtra(message2);
                FusionMarket.sendUserMessage(player, message);
            }
        }
    }
}
