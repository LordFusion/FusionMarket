package io.github.lordfusion.fusionmarket;

import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;
import io.github.lordfusion.fusionmarket.utilities.PurchaseRecord;
import io.github.lordfusion.fusionmarket.utilities.SimpleLocation;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * A Shop represents a chest and sign, where a player may purchase or sell a specific item to/from the automated Shop.
 */
@SerializableAs("Shop")
public class Shop implements ConfigurationSerializable
{
    private String uniqueId;
    private UUID owner;
    private ItemStack item;
    private double price;
    private SimpleLocation chestLocation;
    private Sign sign;
    
    private boolean adminShop;
    private boolean limitPerPlayer;
    private int limit;
    private int limitLength;
    private LimitPeriod limitPeriod = LimitPeriod.ALLTIME;
    
    public enum LimitPeriod
    {
        HOUR('H'), DAY('D'), WEEK('W'), MONTH('M'), ALLTIME('A');
        private char translation;
        
        LimitPeriod(char t) {
            this.translation = t;
        }
        
        public static LimitPeriod translate(char t) {
            switch (t) {
                case 'H':
                case 'h':
                    return HOUR;
                case 'D':
                case 'd':
                    return DAY;
                case 'W':
                case 'w':
                    return WEEK;
                case 'M':
                case 'm':
                    return MONTH;
                case 'A':
                case 'a':
                default:
                    return ALLTIME;
            }
        }
        
        public static List<Character> getValidValues()
        {
            List<Character> output = new ArrayList<Character>();
            output.add('H');
            output.add('D');
            output.add('W');
            output.add('M');
            output.add('A');
            return output;
        }
        
        @Override
        public String toString()
        {
            switch (translation) {
                case 'H':
                    return "hour";
                case 'D':
                    return "day";
                case 'W':
                    return "week";
                case 'M':
                    return "month";
                case 'A':
                default:
                    return "all time";
            }
        }
    }
    
    /**
     * Creates a blank Shop. I'm not doing anything with it, that's your job.
     */
    public Shop() {}
    
    // Message Generation ************************************************************************ Message Generation //
    
    /**
     * Generates a message to send to an Admin about the Shop.
     * The message is "fancy," as it contains text coloration and on-hover/click events.
     * @return Fancy admin-focused message.
     */
    public TextComponent[] getAdminInfo()
    {
        TextComponent[] output = new TextComponent[4];
    
        TextComponent spacer = new TextComponent(" | ");
        spacer.setColor(ChatColor.GRAY);
        
        // Header
        output[0] = new TextComponent("-*-*-*-{ ");
        output[0].setColor(ChatColor.DARK_BLUE);
        TextComponent shopId = new TextComponent(this.getUniqueId());
        shopId.setColor(ChatColor.BLUE);
        TextComponent location;
        if (isAdminShop())
            location = new TextComponent(this.getSign().getLocation().toString());
        else
            location = new TextComponent(this.getChestLocation().toString());
        location.setColor(ChatColor.WHITE);
        shopId.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[]{location}));
        output[0].addExtra(shopId);
        output[0].addExtra(" }-*-*-*-");
        
        // Owner and Price info
        output[1] = (TextComponent)spacer.duplicate();
        TextComponent ownerHeader = new TextComponent("Owner: ");
        ownerHeader.setColor(ChatColor.BLUE);
        TextComponent ownerName;
        if (this.getOwner() != null) {
            ownerName = new TextComponent(Bukkit.getOfflinePlayer(this.getOwner()).getName());
            TextComponent hoverUuid = new TextComponent("" + this.getOwner());
            hoverUuid.setColor(ChatColor.WHITE);
            ownerName.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new BaseComponent[]{hoverUuid}));
        } else if (this.isAdminShop()) {
            ownerName = new TextComponent("ADMINSHOP");
        } else {
            ownerName = new TextComponent("N/A");
        }
        ownerName.setColor(ChatColor.AQUA);
        output[1].addExtra(ownerHeader);
        output[1].addExtra(ownerName);
        output[1].addExtra(spacer.duplicate());
        
        TextComponent priceHeader = new TextComponent("Price: $");
        priceHeader.setColor(ChatColor.BLUE);
        priceHeader.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/mkt setprice " +
                this.getUniqueId() + " [price]"));
        TextComponent priceMsg = new TextComponent(this.getPrice() + "");
        priceMsg.setColor(ChatColor.AQUA);
        priceMsg.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/mkt setprice " +
                this.getUniqueId() + " [price]"));
        output[1].addExtra(priceHeader);
        output[1].addExtra(priceMsg);
    
        // Limits?
        output[2] = (TextComponent)spacer.duplicate();
        TextComponent limitText;
        if (getLimitAmt() > 0) {
            limitText = new TextComponent("Limit set as ");
            limitText.setColor(ChatColor.BLUE);
        
            TextComponent limitAmtText = new TextComponent("" + this.getLimitAmt());
            limitAmtText.setColor(ChatColor.AQUA);
            limitText.addExtra(limitAmtText);
        
            limitText.addExtra(" per ");
        
            TextComponent timePeriodText = new TextComponent(this.getLimitLength() + " " + this.getLimitPeriod());
            if (this.getLimitLength() > 1 && this.limitPeriod != LimitPeriod.ALLTIME)
                timePeriodText.addExtra("s");
            timePeriodText.setColor(ChatColor.BLUE);
            limitText.addExtra(timePeriodText);
        
            limitText.addExtra(".");
        } else {
            limitText = new TextComponent("No transaction limit.");
            limitText.setColor(ChatColor.BLUE);
        }
    
        TextComponent limitHoverText = new TextComponent("Click me for the setlimit command!");
        limitHoverText.setColor(ChatColor.WHITE);
        BaseComponent[] hover = new BaseComponent[]{limitHoverText};
        limitText.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover));
        limitText.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/mkt setlimit " + this.uniqueId
                + " " + this.limit + " " + this.limitLength + this.limitPeriod.translation));
        output[2].addExtra(limitText);
        
        // Item info
        output[3] = (TextComponent)spacer.duplicate();
        TextComponent itemHeader = new TextComponent("Item: ");
        itemHeader.setColor(ChatColor.BLUE);
        TextComponent itemName = new TextComponent(this.getItem().getType().name());
        itemName.setColor(ChatColor.AQUA);
        output[3].addExtra(itemHeader);
        output[3].addExtra(itemName);
        
        return output;
    }
    
    /**
     * Generates a message to send to an Owner about their Shop.
     * The message is "fancy," as it contains text coloration and on-hover/click events.
     * @return Fancy owner-focused message.
     */
    public TextComponent[] getOwnerInfo()
    {
        TextComponent[] output = new TextComponent[4];
    
        TextComponent spacer = new TextComponent(" | ");
        spacer.setColor(ChatColor.GRAY);
    
        // Header
        output[0] = new TextComponent("-*-*-*-{ ");
        output[0].setColor(ChatColor.DARK_GRAY);
        TextComponent shopId = new TextComponent(this.getUniqueId());
        shopId.setColor(ChatColor.DARK_PURPLE);
        TextComponent location;
        if (isAdminShop())
            location = new TextComponent(this.getSign().getLocation().toString());
        else
            location = new TextComponent(this.getChestLocation().toString());
        location.setColor(ChatColor.WHITE);
        shopId.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[]{location}));
        output[0].addExtra(shopId);
        output[0].addExtra(" }-*-*-*-");
        
        // Price Info
        output[1] = (TextComponent)spacer.duplicate();
        if (this.getPrice() < 0) { // Buy from player
            TextComponent priceHeader = new TextComponent("Shop is ");
            priceHeader.setColor(ChatColor.DARK_PURPLE);
            TextComponent buyingText = new TextComponent("BUYING");
            buyingText.setColor(ChatColor.LIGHT_PURPLE);
            priceHeader.addExtra(buyingText);
            priceHeader.addExtra(" from players at $");
            TextComponent priceText = new TextComponent(Math.abs(this.getPrice()) + "");
            priceText.setColor(ChatColor.LIGHT_PURPLE);
            priceHeader.addExtra(priceText);
            priceHeader.addExtra(" each.");
            TextComponent hoverText = new TextComponent("Price < 0: Buy from players | Price > 0 Sell to players");
            hoverText.setColor(ChatColor.WHITE);
            priceHeader.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[]{hoverText}));
            priceHeader.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/mkt setprice " +
                    this.getUniqueId() + " [Price]"));
            output[1].addExtra(priceHeader);
        } else if (this.getPrice() > 0) { // Sell to player
            TextComponent priceHeader = new TextComponent("Shop is ");
            priceHeader.setColor(ChatColor.DARK_PURPLE);
            TextComponent buyingText = new TextComponent("SELLING");
            buyingText.setColor(ChatColor.LIGHT_PURPLE);
            priceHeader.addExtra(buyingText);
            priceHeader.addExtra(" to players at $");
            TextComponent priceText = new TextComponent(Math.abs(this.getPrice()) + "");
            priceText.setColor(ChatColor.LIGHT_PURPLE);
            priceHeader.addExtra(priceText);
            priceHeader.addExtra(" each.");
            TextComponent hoverText = new TextComponent("Price < 0: Buy from players | Price > 0 Sell to players");
            hoverText.setColor(ChatColor.WHITE);
            priceHeader.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[]{hoverText}));
            priceHeader.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/mkt setprice " +
                    this.getUniqueId() + " [Price]"));
            output[1].addExtra(priceHeader);
        } else {
            TextComponent priceNotSet = new TextComponent("Shop price is not set! ");
            priceNotSet.setColor(ChatColor.DARK_RED);
            TextComponent clickable = new TextComponent("Click here to set price.");
            clickable.setColor(ChatColor.RED);
            TextComponent hoverText = new TextComponent("Price < 0: Buy from players | Price > 0 Sell to players");
            hoverText.setColor(ChatColor.WHITE);
            clickable.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[]{hoverText}));
            clickable.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/mkt setprice " +
                    this.getUniqueId() + " [Price]"));
            priceNotSet.addExtra(clickable);
            output[1].addExtra(priceNotSet);
        }
    
        // Limits?
        output[2] = (TextComponent)spacer.duplicate();
        TextComponent limitText;
        if (getLimitAmt() > 0) {
            limitText = new TextComponent("Limit set as ");
            limitText.setColor(ChatColor.DARK_PURPLE);
            
            TextComponent limitAmtText = new TextComponent("" + this.getLimitAmt());
            limitAmtText.setColor(ChatColor.LIGHT_PURPLE);
            limitText.addExtra(limitAmtText);
            
            limitText.addExtra(" per ");
            
            TextComponent timePeriodText = new TextComponent(this.getLimitLength() + " " + this.getLimitPeriod());
            if (this.getLimitLength() > 1 && this.limitPeriod != LimitPeriod.ALLTIME)
                timePeriodText.addExtra("s");
            timePeriodText.setColor(ChatColor.LIGHT_PURPLE);
            limitText.addExtra(timePeriodText);
            
            limitText.addExtra(".");
        } else {
            limitText = new TextComponent("No transaction limit.");
            limitText.setColor(ChatColor.DARK_PURPLE);
        }
        
        TextComponent limitHoverText = new TextComponent("Click me for the setlimit command!");
        limitHoverText.setColor(ChatColor.WHITE);
        BaseComponent[] hover = new BaseComponent[]{limitHoverText};
        limitText.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover));
        limitText.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/mkt setlimit " + this.uniqueId
                + " " + this.limit + " " + this.limitLength + this.limitPeriod.translation));
        output[2].addExtra(limitText);
        
        // Item info
        output[3] = (TextComponent)spacer.duplicate();
        TextComponent itemHeader = new TextComponent("Item: ");
        itemHeader.setColor(ChatColor.DARK_PURPLE);
        TextComponent itemName = new TextComponent(this.getItem().getType().name());
        itemName.setColor(ChatColor.LIGHT_PURPLE);
        output[3].addExtra(itemHeader);
        output[3].addExtra(itemName);
    
        return output;
    }
    
    /**
     * The shop isn't ready, and this message is gonna tell people that.
     * The message is "fancy," as it contains text coloration and on-hover/click events.
     * @return Fancy peasant-focused message.
     */
    public TextComponent[] getIncompleteInfo()
    {
        TextComponent[] output = new TextComponent[2];
    
        TextComponent spacer = new TextComponent(" | ");
        spacer.setColor(ChatColor.GRAY);
    
        // Header
        output[0] = new TextComponent("-*-*-*-{ ");
        output[0].setColor(ChatColor.DARK_RED);
        TextComponent shopId = new TextComponent(this.getUniqueId());
        shopId.setColor(ChatColor.RED);
        TextComponent location;
        if (isAdminShop())
            location = new TextComponent(this.getSign().getLocation().toString());
        else
            location = new TextComponent(this.getChestLocation().toString());
        location.setColor(ChatColor.WHITE);
        shopId.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[]{location}));
        output[0].addExtra(shopId);
        output[0].addExtra(" }-*-*-*-");
        
        // pls leave me alone
        output[1] = (TextComponent)spacer.duplicate();
        TextComponent notReadyWarning = new TextComponent("This shop is not yet ready for transactions.");
        notReadyWarning.setColor(ChatColor.YELLOW);
        output[1].addExtra(notReadyWarning);
        
        return output;
    }
    
    /**
     * The shop is ready, and this message is gonna give people the option to multi-purchase easily.
     * The message is "fancy," as it contains text coloration and on-hover/click events.
     * @return Fancy peasant-focused message.
     */
    public TextComponent[] getPurchaseInfo(Player player)
    {
        TextComponent[] output = new TextComponent[5];
    
        TextComponent spacer = new TextComponent(" | ");
        spacer.setColor(ChatColor.GRAY);
    
        // Header
        output[0] = new TextComponent("-*-*-*-{ ");
        output[0].setColor(ChatColor.DARK_GREEN);
        TextComponent shopId = new TextComponent(this.getUniqueId());
        shopId.setColor(ChatColor.DARK_AQUA);
        TextComponent location;
        if (isAdminShop())
            location = new TextComponent(this.getSign().getLocation().toString());
        else
            location = new TextComponent(this.getChestLocation().toString());
        location.setColor(ChatColor.WHITE);
        shopId.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[]{location}));
        output[0].addExtra(shopId);
        output[0].addExtra(" }-*-*-*-");
        
        // Owner and Price Info
        output[1] = (TextComponent)spacer.duplicate();
        TextComponent oneLiner = new TextComponent("");
        oneLiner.setColor(ChatColor.DARK_AQUA);
        TextComponent ownerName;
        if (this.getOwner() != null) {
            ownerName = new TextComponent(Bukkit.getOfflinePlayer(this.getOwner()).getName());
            TextComponent hoverUuid = new TextComponent("" + this.getOwner());
            hoverUuid.setColor(ChatColor.WHITE);
            ownerName.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new BaseComponent[]{hoverUuid}));
        } else if (this.isAdminShop()) {
            ownerName = new TextComponent("ADMINSHOP");
        } else {
            ownerName = new TextComponent("N/A");
        }
        ownerName.setColor(ChatColor.AQUA);
        oneLiner.addExtra(ownerName);
        oneLiner.addExtra(" is ");
        TextComponent buyOrSell;
        if (this.getPrice() > 0)
            buyOrSell = new TextComponent("SELLING");
        else if (this.getPrice() < 0)
            buyOrSell = new TextComponent("BUYING");
        else
            buyOrSell = new TextComponent("ERRORING");
        if (this.getPrice() != 0)
            buyOrSell.setColor(ChatColor.AQUA);
        else
            buyOrSell.setColor(ChatColor.RED);
        oneLiner.addExtra(buyOrSell);
        oneLiner.addExtra(" for $");
        TextComponent priceText = new TextComponent(Math.abs(this.getPrice()) + "");
        priceText.setColor(ChatColor.AQUA);
        oneLiner.addExtra(priceText);
        oneLiner.addExtra(" each. ");
        output[1].addExtra(oneLiner);
        
        // Limits?
        output[2] = (TextComponent)spacer.duplicate();
        TextComponent limitText;
        if (getLimitAmt() > 0) {
            int transactionCount = this.getTransactionCount(player);
            limitText = new TextComponent("Limit ");
            limitText.addExtra(transactionCount + " of ");
            limitText.addExtra(this.getLimitAmt() + " per ");
            limitText.addExtra(this.getLimitLength() + " " + this.getLimitPeriod());
            if (this.getLimitLength() > 1 && this.limitPeriod != LimitPeriod.ALLTIME)
                limitText.addExtra("s");
            limitText.addExtra(".");
            if (this.isLimitPerPlayer())
                limitText.addExtra(" (per player)");
            else
                limitText.addExtra(" (total)");
        
            if (transactionCount < this.getLimitAmt())
                limitText.setColor(ChatColor.DARK_AQUA);
            else if (transactionCount > this.getLimitAmt())
                limitText.setColor(ChatColor.DARK_RED);
            else
                limitText.setColor(ChatColor.RED);
        } else {
            limitText = new TextComponent("No transaction limit.");
            limitText.setColor(ChatColor.DARK_AQUA);
        }
        output[2].addExtra(limitText);
        
        // Bulk purchase clickable
        output[3] = (TextComponent)spacer.duplicate();
        TextComponent bulkOperation = new TextComponent("");
        bulkOperation.setColor(ChatColor.DARK_AQUA);
        if (this.getPrice() != 0) {
            TextComponent clickable = new TextComponent("Click ME");
            clickable.setColor(ChatColor.AQUA);
            if (this.getPrice() > 0) {
                clickable.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/mkt buy " + this.getUniqueId() + " [Quantity]"));
                bulkOperation.addExtra(clickable);
                bulkOperation.addExtra(" to get the bulk buying command!");
            } else {
                clickable.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/mkt sell " + this.getUniqueId() + " [Quantity]"));
                bulkOperation.addExtra(clickable);
                bulkOperation.addExtra(" to get the bulk selling command!");
            }
        } else {
            bulkOperation.addExtra("Bulk operation not available at this time.");
        }
        output[3].addExtra(bulkOperation);
        
        // Item info
        output[4] = (TextComponent)spacer.duplicate();
        TextComponent itemHeader = new TextComponent("Item: ");
        itemHeader.setColor(ChatColor.DARK_AQUA);
        TextComponent itemName = new TextComponent(this.getItem().getType().name());
        itemName.setColor(ChatColor.AQUA);
        output[4].addExtra(itemHeader);
        output[4].addExtra(itemName);
        
        return output;
    }
    
    /**
     * Creates a string representing this shop's set purchase limit.
     * Ex: "32/d" (32 per day), "1024/m" (1024 per month), "8" (8 for all time), "" (no limit)
     * @return String representation of this shop's purchase limit.
     */
    public String getReadableLimit()
    {
        if (this.limit <= 0)
            return "";
        
        String output = "" + limit + "/" + limitLength + Character.toLowerCase(limitPeriod.translation);
        return output;
    }
    
    // SALES ************************************************************************************************** SALES //
    
    /**
     * Take an item out of this shop. Give it to the player. Take money from the player. Give it to the shop owner.
     * @param player   The player tryna buy
     * @param quantity Number of items the player wants to buy
     */
    public void sellTo(Player player, int quantity)
    {
        // Check for shop limits
        if (this.limit > 0) {
            int sales = this.getTransactionCount(player);
            if (sales >= getLimitAmt()) {
                TextComponent msg = new TextComponent("You have reached your sale limit for this shop.");
                msg.setColor(ChatColor.YELLOW);
                FusionMarket.sendUserMessage(player, msg);
                return;
            }
        
            int availableSales = getLimitAmt() - sales;
            if (quantity > availableSales) {
                TextComponent msg = new TextComponent("Limiting quantity to " + availableSales + " due to shop limit.");
                msg.setColor(ChatColor.YELLOW);
                FusionMarket.sendUserMessage(player, msg);
                quantity = availableSales;
            }
        }
        
        if (!(this.chestLocation.asLocation().getBlock().getState() instanceof Chest)) {
            TextComponent msg = new TextComponent("Internal error. Transaction cancelled.");
            msg.setColor(ChatColor.YELLOW);
            FusionMarket.sendUserMessage(player, msg);
            return;
        }
        Inventory chestInventory = ((Chest)this.chestLocation.asLocation().getBlock().getState()).getInventory();
        if (!chestInventory.containsAtLeast(this.getItem(), quantity)) {
            TextComponent msg = new TextComponent("The shop doesn't have enough items.");
            msg.setColor(ChatColor.YELLOW);
            FusionMarket.sendUserMessage(player, msg);
            return;
        }
        else if (FusionMarket.getInstance().getEconomy().getBalance(player) < (Math.abs(this.getPrice()) * quantity)) {
            TextComponent msg = new TextComponent("You can't afford that.");
            msg.setColor(ChatColor.YELLOW);
            FusionMarket.sendUserMessage(player, msg);
            return;
        }
        
        // Check if there is enough space in the Player's inventory to hold the items.
        int openSpace = 0;
        for (ItemStack slot : player.getInventory().getContents()) {
            if (slot == null) // Empty slot
                openSpace += this.item.getMaxStackSize();
            else if (slot.isSimilar(item) && slot.getAmount() < slot.getMaxStackSize()) // Same item, slot not full
                openSpace += slot.getMaxStackSize() - slot.getAmount();
            if (openSpace >= quantity)
                break;
        }
        if (openSpace < quantity) {
            TextComponent msg = new TextComponent("Not enough space! Transaction cancelled.");
            msg.setColor(ChatColor.YELLOW);
            FusionMarket.sendUserMessage(player, msg);
            return;
        }
        
        // Sale time
        FusionMarket.sendConsoleInfo("Attempting to sell " + quantity + " of '" + this.getItem().getType() + "' to " +
                player.getName() + " for Shop '" + this.getUniqueId() + "'.");
    
        ItemStack transactionItemStack = MinecraftReflection.getBukkitItemStack(new ItemStack(this.item));
        try {
            NbtCompound nbtData = NbtFactory.fromFile(DataManager.dataFolderPath + DataManager.METADATA_FOLDER +
                    this.getUniqueId() + ".mkt");
//            if (nbtData.getKeys().size() == 0)
            if (nbtData == null)
                transactionItemStack.setData(null);
            else
                NbtFactory.setItemTag(transactionItemStack, nbtData);
        } catch (IOException e) {
            e.printStackTrace();
    
            TextComponent msg = new TextComponent("Internal error. Transaction cancelled.");
            msg.setColor(ChatColor.YELLOW);
            FusionMarket.sendUserMessage(player, msg);
            return;
        }
        transactionItemStack.setAmount(quantity);
        
        HashMap<Integer, ItemStack> notTaken = chestInventory.removeItem(transactionItemStack);
        if (notTaken.keySet().size() != 0) { // Taking the items failed
            FusionMarket.sendConsoleWarn("Transaction failed!");
            for (Map.Entry<Integer, ItemStack> entry : notTaken.entrySet()) { // Try to put the items back
                Integer i = entry.getKey();
                ItemStack is = entry.getValue();
                FusionMarket.sendConsoleWarn(i + " | " + is.toString());
                is.setAmount(i);
                if (chestInventory.addItem(is).keySet().size() != 0) {
                    FusionMarket.sendConsoleWarn("  Failed to readd to inventory!");
                }
            }
            TextComponent msg = new TextComponent("Internal error. Transaction cancelled.");
            msg.setColor(ChatColor.YELLOW);
            FusionMarket.sendUserMessage(player, msg);
            return;
        }
        
        HashMap<Integer, ItemStack> notGiven = player.getInventory().addItem(transactionItemStack);
        int finalQuantity = quantity;
        if (notGiven.keySet().size() != 0) { // At least partial failure
            for (Map.Entry<Integer, ItemStack> entry : notGiven.entrySet()) {
                Integer i = entry.getKey();
                ItemStack is = entry.getValue();
                is.setAmount(i);
                chestInventory.addItem(is);
                finalQuantity -= i;
            }
        }
    
        Economy economy = FusionMarket.getInstance().getEconomy();
        economy.depositPlayer(Bukkit.getOfflinePlayer(this.getOwner()), finalQuantity * Math.abs(this.getPrice()));
        economy.withdrawPlayer(player, finalQuantity * Math.abs(this.getPrice()));
        
        // Console notification
        FusionMarket.sendConsoleInfo("Transaction completed for qty" + finalQuantity + " at $" + finalQuantity *
                Math.abs(this.getPrice()));
        // Customer notification
        TextComponent msg = new TextComponent("Successfully bought " + finalQuantity + "/" + quantity + " for $" +
                finalQuantity * Math.abs(this.getPrice()));
        msg.setColor(ChatColor.GREEN);
        FusionMarket.sendUserMessage(player, msg);
        // Owner notification
        if (Bukkit.getOfflinePlayer(this.getOwner()).isOnline()) {
            Player owner = Bukkit.getOfflinePlayer(this.getOwner()).getPlayer();
            
            TextComponent alertMsg1 = new TextComponent(player.getName());
            alertMsg1.setColor(ChatColor.GREEN);
            TextComponent alertMsg2 = new TextComponent(" purchased " + this.getItem().getType().getId());
            alertMsg2.setColor(ChatColor.DARK_GREEN);
            if (this.getItem().getDurability() > 0)
                alertMsg2.addExtra(":" + this.getItem().getDurability());
            alertMsg2.addExtra(" x" + quantity + " for ");
            alertMsg1.addExtra(alertMsg2);
            alertMsg1.addExtra("$" + finalQuantity * Math.abs(this.getPrice()));
            
            FusionMarket.sendUserMessage(owner, alertMsg1);
        }
        
        // Purchase Record
        PurchaseRecord purchaseRecord = new PurchaseRecord();
        purchaseRecord.setShopOwner(this.getOwner());
        purchaseRecord.setCustomer(player.getUniqueId());
        purchaseRecord.setTime(new Date());
        purchaseRecord.setQuantity(-finalQuantity);
        purchaseRecord.setTotalPrice(finalQuantity * Math.abs(this.getPrice()));
        purchaseRecord.setShopName(this.uniqueId);
        if (Bukkit.getOfflinePlayer(this.getOwner()).isOnline())
            purchaseRecord.setSeen(true);
        FusionMarket.getInstance().getDataManager().createRecord(purchaseRecord);
        
        player.updateInventory();
    }
    
    /**
     * Take money from the shop owner. Give it to the player. Take an item from the player. Put it into this shop.
     * @param player   The player tryna sell
     * @param quantity Number of times the player wants to sell their stuff
     */
    public void buyFrom(Player player, int quantity)
    {
        // Check for shop limits
        if (this.limit > 0) {
            int sales = this.getTransactionCount(player);
            if (sales >= getLimitAmt()) {
                TextComponent msg = new TextComponent("You have reached your sale limit for this shop.");
                msg.setColor(ChatColor.YELLOW);
                FusionMarket.sendUserMessage(player, msg);
                return;
            }
    
            int availableSales = getLimitAmt() - sales;
            if (quantity > availableSales) {
                TextComponent msg = new TextComponent("Limiting quantity to " + availableSales + " due to shop limit.");
                msg.setColor(ChatColor.YELLOW);
                FusionMarket.sendUserMessage(player, msg);
                quantity = availableSales;
            }
        }
        
        if (!player.getInventory().containsAtLeast(this.getItem(), quantity)) {
            TextComponent msg = new TextComponent("You don't have enough matching items in your inventory.");
            msg.setColor(ChatColor.YELLOW);
            FusionMarket.sendUserMessage(player, msg);
            return;
        }
        else if (FusionMarket.getInstance().getEconomy().getBalance(Bukkit.getOfflinePlayer(this.getOwner())) <
                (Math.abs(this.getPrice()) * quantity)) {
            TextComponent msg = new TextComponent("The shop owner can't afford the transaction.");
            msg.setColor(ChatColor.YELLOW);
            FusionMarket.sendUserMessage(player, msg);
            return;
        }
        
        // Check if there is enough space in the chest to hold the items.
        if (!(this.chestLocation.asLocation().getBlock().getState() instanceof Chest)) {
            TextComponent msg = new TextComponent("Internal error. Transaction cancelled.");
            msg.setColor(ChatColor.YELLOW);
            FusionMarket.sendUserMessage(player, msg);
            return;
        }
        Inventory chestInventory = ((Chest)this.chestLocation.asLocation().getBlock().getState()).getInventory();
        int openSpace = 0;
        for (ItemStack slot : chestInventory.getContents()) {
            if (slot == null) // Empty slot
                openSpace += this.item.getMaxStackSize();
            else if (slot.isSimilar(item) && slot.getAmount() < slot.getMaxStackSize()) // Same item, slot not full
                openSpace += slot.getMaxStackSize() - slot.getAmount();
            
            if (openSpace >= quantity)
                break;
        }
        if (openSpace < quantity) {
            TextComponent msg = new TextComponent("Not enough space! Transaction cancelled.");
            msg.setColor(ChatColor.YELLOW);
            FusionMarket.sendUserMessage(player, msg);
            return;
        }
        
        // Sale time
        FusionMarket.sendConsoleInfo("Attempting to buy " + quantity + " of '" + this.getItem().getType() + "' from " +
                player.getName() + " for Shop '" + this.getUniqueId() + "'.");
    
        ItemStack transactionItemStack = MinecraftReflection.getBukkitItemStack(new ItemStack(this.item));
        try {
            NbtCompound nbtData = NbtFactory.fromFile(DataManager.dataFolderPath + DataManager.METADATA_FOLDER +
                    this.getUniqueId() + ".mkt");
//            if (nbtData.getKeys().size() == 0)
            if (nbtData == null)
                transactionItemStack.setData(null);
            else
                NbtFactory.setItemTag(transactionItemStack, nbtData);
        } catch (IOException e) {
            e.printStackTrace();
        
            TextComponent msg = new TextComponent("Internal error. Transaction cancelled.");
            msg.setColor(ChatColor.YELLOW);
            FusionMarket.sendUserMessage(player, msg);
            return;
        }
        transactionItemStack.setAmount(quantity);
        
        HashMap<Integer, ItemStack> notTaken = player.getInventory().removeItem(transactionItemStack);
        if (notTaken.keySet().size() != 0) { // Taking the items failed
            FusionMarket.sendConsoleWarn("Transaction failed!");
            for (Map.Entry<Integer, ItemStack> entry : notTaken.entrySet()) { // Try to put the items back
                Integer i = entry.getKey();
                ItemStack is = entry.getValue();
                FusionMarket.sendConsoleWarn(i + " | " + is.toString());
                is.setAmount(i);
                if (player.getInventory().addItem(is).keySet().size() != 0) {
                    FusionMarket.sendConsoleWarn("  Failed to readd to inventory!");
                }
            }
            TextComponent msg = new TextComponent("Internal error. Transaction cancelled.");
            msg.setColor(ChatColor.YELLOW);
            FusionMarket.sendUserMessage(player, msg);
            return;
        }
    
        HashMap<Integer, ItemStack> notGiven = chestInventory.addItem(transactionItemStack);
        int finalQuantity = quantity;
        if (notGiven.keySet().size() != 0) { // At least partial failure
            for (Map.Entry<Integer, ItemStack> entry : notGiven.entrySet()) {
                Integer i = entry.getKey();
                ItemStack is = entry.getValue();
                is.setAmount(i);
                player.getInventory().addItem(is);
                finalQuantity -= i;
            }
        }
    
        Economy economy = FusionMarket.getInstance().getEconomy();
        economy.withdrawPlayer(Bukkit.getOfflinePlayer(this.getOwner()), finalQuantity * Math.abs(this.getPrice()));
        economy.depositPlayer(player, finalQuantity * Math.abs(this.getPrice()));
        
        // Console notification
        FusionMarket.sendConsoleInfo("Transaction completed for qty" + finalQuantity + " at $" + finalQuantity *
                Math.abs(this.getPrice()));
        // Customer notification
        TextComponent msg = new TextComponent("Successfully sold " + finalQuantity + "/" + quantity + " for $" +
                finalQuantity * Math.abs(this.getPrice()));
        msg.setColor(ChatColor.GREEN);
        FusionMarket.sendUserMessage(player, msg);
        // Owner notification
        if (Bukkit.getOfflinePlayer(this.getOwner()).isOnline()) {
            Player owner = Bukkit.getOfflinePlayer(this.getOwner()).getPlayer();
        
            TextComponent alertMsg1 = new TextComponent(player.getName());
            alertMsg1.setColor(ChatColor.GREEN);
            TextComponent alertMsg2 = new TextComponent(" sold " + this.getItem().getType().getId());
            alertMsg2.setColor(ChatColor.DARK_GREEN);
            if (this.getItem().getDurability() > 0)
                alertMsg2.addExtra(":" + this.getItem().getDurability());
            alertMsg2.addExtra(" x" + quantity + " for ");
            alertMsg1.addExtra(alertMsg2);
            alertMsg1.addExtra("$" + finalQuantity * Math.abs(this.getPrice()));
        
            FusionMarket.sendUserMessage(owner, alertMsg1);
        }
    
        // Purchase Record
        PurchaseRecord purchaseRecord = new PurchaseRecord();
        purchaseRecord.setShopOwner(this.getOwner());
        purchaseRecord.setCustomer(player.getUniqueId());
        purchaseRecord.setTime(new Date());
        purchaseRecord.setQuantity(-finalQuantity);
        purchaseRecord.setTotalPrice(finalQuantity * Math.abs(this.getPrice()));
        purchaseRecord.setShopName(this.uniqueId);
        if (Bukkit.getOfflinePlayer(this.getOwner()).isOnline())
            purchaseRecord.setSeen(true);
        FusionMarket.getInstance().getDataManager().createRecord(purchaseRecord);
        
        player.updateInventory();
    }
    
    /**
     * Magically generate items into existence. Give it to the player. Take money from the player. Don't give it away.
     * @param player   The player tryna buy
     * @param quantity Number of items the player wants to buy
     */
    public void adminShopSellTo(Player player, int quantity)
    {
        // Verify quantity is positive
        if (quantity <= 0) {
            TextComponent msg = new TextComponent("You can't purchase a negative number of items!");
            msg.setColor(ChatColor.YELLOW);
            FusionMarket.sendUserMessage(player, msg);
            return;
        }
        
        // Check for shop limits
        if (this.limit > 0) {
            int sales = this.getTransactionCount(player);
            if (sales >= getLimitAmt()) {
                TextComponent msg = new TextComponent("You have reached your purchase limit for this shop.");
                msg.setColor(ChatColor.YELLOW);
                FusionMarket.sendUserMessage(player, msg);
                return;
            }
        
            int availableSales = getLimitAmt() - sales;
            if (quantity > availableSales) {
                TextComponent msg = new TextComponent("Limiting quantity to " + availableSales + " due to shop limit.");
                msg.setColor(ChatColor.YELLOW);
                FusionMarket.sendUserMessage(player, msg);
                quantity = availableSales;
            }
        }
        
        if (FusionMarket.getInstance().getEconomy().getBalance(player) < (Math.abs(this.getPrice()) * quantity)) {
            TextComponent msg = new TextComponent("You can't afford that.");
            msg.setColor(ChatColor.YELLOW);
            FusionMarket.sendUserMessage(player, msg);
            return;
        }
    
        // Check if there is enough space in the Player's inventory to hold the items.
        int openSpace = 0;
        for (ItemStack slot : player.getInventory().getContents()) {
            if (slot == null) // Empty slot
                openSpace += this.item.getMaxStackSize();
            else if (slot.isSimilar(item) && slot.getAmount() < slot.getMaxStackSize()) // Same item, slot not full
                openSpace += slot.getMaxStackSize() - slot.getAmount();
            if (openSpace >= quantity)
                break;
        }
        if (openSpace < quantity) {
            TextComponent msg = new TextComponent("Not enough space! Transaction cancelled.");
            msg.setColor(ChatColor.YELLOW);
            FusionMarket.sendUserMessage(player, msg);
            return;
        }
    
        // Sale time
        FusionMarket.sendConsoleInfo("Attempting to sell " + quantity + " of '" + this.getItem().getType() + "' to " +
                player.getName() + " for Shop '" + this.getUniqueId() + "'.");
        
        ItemStack transactionItemStack = MinecraftReflection.getBukkitItemStack(new ItemStack(this.item));
        try {
            NbtCompound nbtData = NbtFactory.fromFile(DataManager.dataFolderPath + DataManager.METADATA_FOLDER +
                    this.getUniqueId() + ".mkt");
////            if ((nbtData == null) || nbtData.getKeys().size() == 0)
            if (nbtData == null)
            if (nbtData == null)
                transactionItemStack.setData(null);
            else
                NbtFactory.setItemTag(transactionItemStack, nbtData);
        } catch (IOException e) {
            e.printStackTrace();
        
            TextComponent msg = new TextComponent("Internal error. Transaction cancelled.");
            msg.setColor(ChatColor.YELLOW);
            FusionMarket.sendUserMessage(player, msg);
            return;
        }
        transactionItemStack.setAmount(quantity);
    
        HashMap<Integer, ItemStack> notGiven = player.getInventory().addItem(transactionItemStack);
        int finalQuantity = quantity;
        if (notGiven.keySet().size() != 0) { // At least partial failure
            for (Map.Entry<Integer, ItemStack> entry : notGiven.entrySet()) {
                Integer i = entry.getKey();
                ItemStack is = entry.getValue();
                is.setAmount(i);
                finalQuantity -= i;
            }
        }
        
        Economy economy = FusionMarket.getInstance().getEconomy();
        economy.withdrawPlayer(player, finalQuantity * Math.abs(this.getPrice()));
    
        // Console notification
        FusionMarket.sendConsoleInfo("Transaction completed for qty" + finalQuantity + " at $" + finalQuantity *
                Math.abs(this.getPrice()));
        // Customer notification
        TextComponent msg = new TextComponent("Successfully bought " + finalQuantity + "/" + quantity + " for $" +
                finalQuantity * Math.abs(this.getPrice()));
        msg.setColor(ChatColor.GREEN);
        FusionMarket.sendUserMessage(player, msg);
    
        // Purchase Record
        PurchaseRecord purchaseRecord = new PurchaseRecord();
        purchaseRecord.setShopOwner(this.getOwner());
        purchaseRecord.setCustomer(player.getUniqueId());
        purchaseRecord.setTime(new Date());
        purchaseRecord.setQuantity(finalQuantity);
        purchaseRecord.setTotalPrice(finalQuantity * Math.abs(this.getPrice()));
        purchaseRecord.setShopName(this.uniqueId);
        purchaseRecord.setSeen(true);
        FusionMarket.getInstance().getDataManager().createRecord(purchaseRecord);
    
        player.updateInventory();
    }
    
    /**
     * Give the player money that came from the void. Take items from the player, make them disappear into the void.
     * @param player   The player tryna sell
     * @param quantity Number of times the player wants to sell their stuff
     */
    public void adminShopBuyFrom(Player player, int quantity)
    {
        // Verify quantity is positive
        // todo: check if this check exists elsewhere, because it does for player shops
        if (quantity <= 0) {
            TextComponent msg = new TextComponent("You can't sell a negative number of items!");
            msg.setColor(ChatColor.YELLOW);
            FusionMarket.sendUserMessage(player, msg);
            return;
        }
        
        // Check for shop limits
        if (this.limit > 0) {
            int sales = this.getTransactionCount(player);
            if (sales >= getLimitAmt()) {
                TextComponent msg = new TextComponent("You have reached your sale limit for this shop.");
                msg.setColor(ChatColor.YELLOW);
                FusionMarket.sendUserMessage(player, msg);
                return;
            }
    
            int availableSales = getLimitAmt() - sales;
            if (quantity > availableSales) {
                TextComponent msg = new TextComponent("Limiting quantity to " + availableSales + " due to shop limit.");
                msg.setColor(ChatColor.YELLOW);
                FusionMarket.sendUserMessage(player, msg);
                quantity = availableSales;
            }
        }
        
        if (!player.getInventory().containsAtLeast(this.getItem(), quantity)) {
            TextComponent msg = new TextComponent("You don't have enough matching items in your inventory.");
            msg.setColor(ChatColor.YELLOW);
            FusionMarket.sendUserMessage(player, msg);
            return;
        }
        
        // Sale time
        FusionMarket.sendConsoleInfo("Attempting to buy " + quantity + " of '" + this.getItem().getType() + "' from " +
                player.getName() + " for Shop '" + this.getUniqueId() + "'.");
    
        ItemStack transactionItemStack = MinecraftReflection.getBukkitItemStack(new ItemStack(this.item));
        try {
            NbtCompound nbtData = NbtFactory.fromFile(DataManager.dataFolderPath + DataManager.METADATA_FOLDER +
                    this.getUniqueId() + ".mkt");
//            if ((nbtData == null) || nbtData.getKeys().size() == 0)
            if (nbtData == null)
                transactionItemStack.setData(null);
            else
                NbtFactory.setItemTag(transactionItemStack, nbtData);
        } catch (IOException e) {
            e.printStackTrace();
        
            TextComponent msg = new TextComponent("Internal error. Transaction cancelled.");
            msg.setColor(ChatColor.YELLOW);
            FusionMarket.sendUserMessage(player, msg);
            return;
        }
        transactionItemStack.setAmount(quantity);
    
        HashMap<Integer, ItemStack> notTaken = player.getInventory().removeItem(transactionItemStack);
        if (notTaken.keySet().size() != 0) { // Taking the items failed
            FusionMarket.sendConsoleWarn("Transaction failed!");
            for (Map.Entry<Integer, ItemStack> entry : notTaken.entrySet()) { // Try to put the items back
                Integer i = entry.getKey();
                ItemStack is = entry.getValue();
                FusionMarket.sendConsoleWarn(i + " | " + is.toString());
                is.setAmount(i);
                if (player.getInventory().addItem(is).keySet().size() != 0) {
                    FusionMarket.sendConsoleWarn("  Failed to readd to inventory!");
                }
            }
            TextComponent msg = new TextComponent("Internal error. Transaction cancelled.");
            msg.setColor(ChatColor.YELLOW);
            FusionMarket.sendUserMessage(player, msg);
            return;
        }
        
        int finalQuantity = quantity;
        Economy economy = FusionMarket.getInstance().getEconomy();
        economy.depositPlayer(player, finalQuantity * Math.abs(this.getPrice()));
    
        // Console notification
        FusionMarket.sendConsoleInfo("Transaction completed for qty" + finalQuantity + " at $" + finalQuantity *
                Math.abs(this.getPrice()));
        // Customer notification
        TextComponent msg = new TextComponent("Successfully sold " + finalQuantity + "/" + quantity + " for $" +
                finalQuantity * Math.abs(this.getPrice()));
        msg.setColor(ChatColor.GREEN);
        FusionMarket.sendUserMessage(player, msg);
    
        // Purchase Record
        PurchaseRecord purchaseRecord = new PurchaseRecord();
        purchaseRecord.setShopOwner(this.getOwner());
        purchaseRecord.setCustomer(player.getUniqueId());
        purchaseRecord.setTime(new Date());
        purchaseRecord.setQuantity(-finalQuantity);
        purchaseRecord.setTotalPrice(finalQuantity * Math.abs(this.getPrice()));
        purchaseRecord.setShopName(this.uniqueId);
        purchaseRecord.setSeen(true);
        FusionMarket.getInstance().getDataManager().createRecord(purchaseRecord);
    
        player.updateInventory();
    }
    
    // DATA **************************************************************************************************** DATA //
    
    private int getTransactionCount(Player player)
    {
        Date limitDate = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(limitDate);
        switch (this.getLimitPeriod().translation) {
            case 'H':
            case 'h':
                calendar.add(Calendar.HOUR, -this.getLimitLength());
                break;
            case 'D':
            case 'd':
                calendar.add(Calendar.DATE, -this.getLimitLength());
                break;
            case 'W':
            case 'w':
                calendar.add(Calendar.DATE, -this.getLimitLength()*7);
                break;
            case 'M':
            case 'm':
                calendar.add(Calendar.MONTH, -this.getLimitLength());
                break;
            case 'A':
            case 'a':
            default:
                calendar.add(Calendar.YEAR, -20);
                break;
        }
        limitDate = calendar.getTime();
    
        if (this.isLimitPerPlayer())
            return FusionMarket.getInstance().getDataManager().countTransactions(player.getUniqueId(), this, limitDate);
        else
            return FusionMarket.getInstance().getDataManager().countTotalTransactions(this, limitDate);
        
//        if (price > 0)
//            return FusionMarket.getInstance().getDataManager().countAdminPurchases(player.getUniqueId(), this.uniqueId, limitDate);
//        else if (price < 0)
//            return FusionMarket.getInstance().getDataManager().countAdminSales(player.getUniqueId(), this.uniqueId, limitDate);
//        else
//            return 0;
    }
    
    // Generic Getters & Setters ************************************************************************************ //
    public String getUniqueId()
    {
        return uniqueId;
    }
    
    public void setUniqueId(String uniqueId)
    {
        this.uniqueId = uniqueId;
    }
    
    public UUID getOwner()
    {
        return owner;
    }
    
    public void setOwner(UUID owner)
    {
        this.owner = owner;
    }
    
    public boolean isAdminShop()
    {
        return this.adminShop;
    }
    
    public void setAdminShop(boolean input)
    {
        this.adminShop = input;
    }
    
    public boolean isLimitPerPlayer()
    {
        return this.limitPerPlayer;
    }
    
    public void setLimitPerPlayer(boolean input)
    {
        this.limitPerPlayer = input;
    }
    
    public int getLimitAmt()
    {
        return this.limit;
    }
    
    public void setLimitAmt(int amt)
    {
        if (amt < 0)
            this.limit = 0;
        else
            this.limit = amt;
    }
    
    public int getLimitLength()
    {
        return this.limitLength;
    }
    
    public void setLimitLength(int amt)
    {
        if (amt < 0)
            this.limitLength = 0;
        else
            this.limitLength = amt;
    }
    
    public LimitPeriod getLimitPeriod()
    {
        return this.limitPeriod;
    }
    
    public void setLimitPeriod(LimitPeriod input)
    {
        this.limitPeriod = input;
    }
    
    public ItemStack getItem()
    {
        return item;
    }
    
    public void setItem(ItemStack item)
    {
        this.item = item;
    }
    
    public double getPrice()
    {
        return price;
    }
    
    public void setPrice(double buyFromPlayerPrice)
    {
        this.price = buyFromPlayerPrice;
    }
    
    public SimpleLocation getChestLocation()
    {
        return chestLocation;
    }
    
    public void setChestLocation(SimpleLocation chestLocation)
    {
        this.chestLocation = chestLocation;
    }
    
    public Sign getSign()
    {
        return sign;
    }
    
    public void setSign(Sign sign)
    {
        this.sign = sign;
    }
    
    /**
     * Creates a Map representation of this class.
     * <p>
     * This class must provide a method to restore this class, as defined in
     * the {@link ConfigurationSerializable} interface javadocs.
     *
     * @return Map containing the current state of this class
     */
    @Override
    public Map<String, Object> serialize()
    {
        Map<String, Object> output = new HashMap<>();
        
        output.put("Unique ID", this.uniqueId);
        if (this.owner != null)
            output.put("Owner", this.owner.toString());
        else
            output.put("Owner", null);
        
        output.put("AdminShop", this.adminShop);
        output.put("Limit Per-Player", this.limitPerPlayer);
        output.put("Limit", this.limit);
        output.put("Limit Length", this.limitLength);
        output.put("Limit Period", this.limitPeriod.translation);
        
        if (this.item != null) {
            output.put("Item Type", this.item.getType().toString());
            output.put("Item Damage", this.item.getDurability());
        } else {
            output.put("Item Type", null);
            output.put("Item Damage", -1);
        }
        
        output.put("Price", this.price);
        
        if (chestLocation != null)
            output.put("Chest Location", this.chestLocation);
        else
            output.put("Chest Location", null);
        
        if (this.sign != null)
            output.put("Sign Location", new SimpleLocation(this.sign.getLocation()));
        else
            output.put("Sign Location", null);
        
        return output;
    }
    
    public static Shop deserialize(Map<String, Object> input)
    {
        Shop output = new Shop();
    
        if (input.containsKey("Unique ID") && input.get("Unique ID") != null)
            output.setUniqueId((String) input.get("Unique ID"));
        else
            return null;
    
        if (input.containsKey("Owner") && input.get("Owner") != null)
            output.setOwner(UUID.fromString((String)input.get("Owner")));
        else
            output.setOwner(null);
        
        output.setAdminShop((boolean)(input.getOrDefault("AdminShop", false)));
        output.setLimitPerPlayer((boolean)(input.getOrDefault("Limit Per-Player", output.isAdminShop())));
        output.setLimitAmt((Integer)(input.getOrDefault("Limit", 0)));
        output.setLimitLength((Integer)input.getOrDefault("Limit Length", 0 ));
        output.setLimitPeriod(LimitPeriod.translate(((String)input.getOrDefault("Limit Period", "A")).charAt(0)));
        
        if (input.containsKey("Item Type") && input.get("Item Type") != null) {
            Material material = Material.getMaterial((String)input.get("Item Type"));
            if (material != null) {
                ItemStack parsedItem;
                if (input.containsKey("Item Damage") && ((Integer)input.get("Item Damage")).shortValue() >= 0) {
                    parsedItem = new ItemStack(material, 1, ((Integer)input.get("Item Damage")).shortValue());
                } else
                    parsedItem = new ItemStack(material);
                
                if (new File(DataManager.dataFolderPath + DataManager.METADATA_FOLDER + output.getUniqueId() + ".mkt").exists()) {
                    try {
                        parsedItem = MinecraftReflection.getBukkitItemStack(parsedItem);
                        NbtCompound nbtData = NbtFactory.fromFile(DataManager.dataFolderPath + DataManager.METADATA_FOLDER +
                                output.getUniqueId() + ".mkt");
//                        if ((nbtData == null) || nbtData.getKeys().size() == 0)
                        if (nbtData == null)
                            parsedItem.setData(null);
                        else
                            NbtFactory.setItemTag(parsedItem, nbtData);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                output.setItem(parsedItem);
            }
        } else {
            FusionMarket.sendConsoleWarn("Invalid Item Type for shop '" + output.getUniqueId() + "'.");
        }
    
        if (input.containsKey("Price") && input.get("Price") != null) {
            if (input.get("Price") instanceof Integer) {
                output.setPrice((Integer) input.get("Price"));
            } else {
                output.setPrice((Double) input.get("Price"));
            }
        } else
            output.setPrice(0);
    
        if (input.containsKey("Chest Location") && input.get("Chest Location") != null)
            output.setChestLocation((SimpleLocation)input.get("Chest Location"));
        else
            output.setChestLocation(null);
        if (input.containsKey("Sign Location") && input.get("Sign Location") != null) {
            Block potentialSign = ((SimpleLocation)input.get("Sign Location")).asLocation().getBlock();
            if (potentialSign.getState() instanceof Sign)
                output.setSign((Sign)potentialSign.getState());
            else
                output.setSign(null);
        } else
            output.setSign(null);
        
        return output;
    }
}
