package io.github.lordfusion.fusionmarket.utilities;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@SerializableAs("PurchaseRecord")
public class PurchaseRecord implements ConfigurationSerializable
{
    private UUID shopOwner, customer;
    private Date time;
    private int quantity;
    private double totalPrice;
    private String shopName;
    private boolean seen = false;
    
    /**
     * Translates this PurchaseRecord into a readable format for when a shop owner logs in.
     * @return A formatted message for a shop owner to read.
     */
    public TextComponent read()
    {
        ChatColor primaryColor, secondaryColor;
        if (quantity > 0) {
            primaryColor = ChatColor.DARK_GREEN;
            secondaryColor = ChatColor.GREEN;
        } else {
            primaryColor = ChatColor.GOLD;
            secondaryColor = ChatColor.YELLOW;
        }
        
        TextComponent output = new TextComponent(" | ");
        output.setColor(ChatColor.DARK_AQUA);
        
        TextComponent msgUsername = new TextComponent(Bukkit.getOfflinePlayer(getCustomer()).getName());
        msgUsername.setColor(secondaryColor);
        output.addExtra(msgUsername);
        
        TextComponent msgBuySell;
        if (quantity > 0)
            msgBuySell = new TextComponent(" bought from ");
        else
            msgBuySell = new TextComponent(" sold to ");
        msgBuySell.setColor(primaryColor);
        output.addExtra(msgBuySell);
        
        TextComponent msgShopId = new TextComponent(getShopName());
        msgShopId.setColor(secondaryColor);
        output.addExtra(msgShopId);
        
        TextComponent msgQuantity = new TextComponent(" x" + getQuantity() + " (");
        msgQuantity.setColor(primaryColor);
        output.addExtra(msgQuantity);
        
        TextComponent msgPrice;
        if (quantity > 0)
            msgPrice = new TextComponent("+$" + getTotalPrice());
        else
            msgPrice = new TextComponent("-$" + getTotalPrice());
        msgPrice.setColor(secondaryColor);
        output.addExtra(msgPrice);
        
        TextComponent msgFinalParenthesis = new TextComponent(")");
        msgFinalParenthesis.setColor(primaryColor);
        output.addExtra(msgFinalParenthesis);
        
        return output;
    }
    
    // Generic Getters & Setters ************************************************************************************ //
    
    public UUID getShopOwner()
    {
        return shopOwner;
    }
    
    public void setShopOwner(UUID shopOwner)
    {
        this.shopOwner = shopOwner;
    }
    
    public UUID getCustomer()
    {
        return customer;
    }
    
    public void setCustomer(UUID customer)
    {
        this.customer = customer;
    }
    
    public Date getTime()
    {
        return time;
    }
    
    public void setTime(Date time)
    {
        this.time = time;
    }
    
    public int getQuantity()
    {
        return quantity;
    }
    
    public void setQuantity(int quantity)
    {
        this.quantity = quantity;
    }
    
    public double getTotalPrice()
    {
        return totalPrice;
    }
    
    public void setTotalPrice(double totalPrice)
    {
        this.totalPrice = totalPrice;
    }
    
    public String getShopName()
    {
        return shopName;
    }
    
    public void setShopName(String shopName)
    {
        this.shopName = shopName;
    }
    
    public boolean isSeen()
    {
        return seen;
    }
    
    public void setSeen(boolean seen)
    {
        this.seen = seen;
    }
    
    // Serialization ********************************************************************************** Serialization //
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
        
        output.put("Shop UniqueID", this.shopName);
        if (shopOwner == null)
            output.put("Shop Owner", null);
        else
            output.put("Shop Owner", this.shopOwner.toString());
        output.put("Customer", this.customer.toString());
        output.put("Time of Sale", this.time); // todo: THIS MIGHT NOT WORK
        output.put("Quantity", this.quantity);
        output.put("Total Price", this.totalPrice);
        output.put("Seen", this.seen);
        
        return output;
    }
    
    /**
     * Attempted deserialization
     * @param input input uwu
     * @return output uwu
     */
    public static PurchaseRecord deserialize(Map<String, Object> input)
    {
        PurchaseRecord output = new PurchaseRecord();
        
        if (input.containsKey("Shop UniqueID"))
            output.setShopName((String)input.get("Shop UniqueID"));
        else
            output.setShopName(null);
        if (input.containsKey("Shop Owner") && input.get("Shop Owner") != null)
            output.setShopOwner(UUID.fromString((String)input.get("Shop Owner")));
        else
            output.setShopOwner(null);
        if (input.containsKey("Customer"))
            output.setCustomer(UUID.fromString((String)input.get("Customer")));
        else
            output.setCustomer(null);
        if (input.containsKey("Time of Sale"))
            output.setTime((Date)input.get("Time of Sale"));
        else
            output.setTime(null);
        if (input.containsKey("Quantity"))
            output.setQuantity((int)input.get("Quantity"));
        else
            output.setQuantity(0);
        if (input.containsKey("Total Price"))
            output.setTotalPrice((double)input.get("Total Price"));
        else
            output.setTotalPrice(0);
        output.setSeen((boolean)input.getOrDefault("Seen", false));
        
        return output;
    }
}
