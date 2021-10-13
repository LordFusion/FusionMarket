package io.github.lordfusion.fusionmarket;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sun.istack.internal.NotNull;
import io.github.lordfusion.fusionmarket.utilities.EvictionTimer;
import io.github.lordfusion.fusionmarket.utilities.SignManager;
import io.github.lordfusion.fusionmarket.utilities.SimpleLocation;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.block.Sign;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

import java.util.*;

@SerializableAs("Market")
public class Market implements ConfigurationSerializable
{
    private String uniqueId, world;
    private UUID owner;
    private UUID[] members;
    private int price, rentTime;
    private Date evictionDate;
    private ProtectedRegion region;
    private SimpleLocation[] signs;
    
    private TimerTask deathTimer;
    
    /**
     * Creates a blank Market. I'm not doing anything with it, that's your job.
     */
    public Market() {}
    
    /**
     * Restores the Market to a for-sale state.
     */
    public void resetOwnership()
    {
        this.setOwner(null);
        this.setEvictionDate(null); // Cancels the death timer
        this.setMembers(null);
        this.setRegionFlags();
        // Todo: Restore Schematic
    
        if (this.getSigns() != null)
            for (SimpleLocation sign : this.getSigns())
                SignManager.generateMarketSign(this, sign.asLocation());
    }
    
    // Regions ********************************************************************************************** Regions //
    
    /**
     * Generate some default Region flags:
     * - Region owner
     * - Region members
     * - Greeting, based on if the plot is owned, sellable, or incomplete
     */
    public void setRegionFlags()
    {
        if (this.region == null)
            return;
        
        this.region.setPriority(1);
        
        DefaultDomain regionMembers = new DefaultDomain();
        if (this.owner != null)
            regionMembers.addPlayer(this.owner);
        if (this.members != null)
            for (UUID playerUuid : this.members)
                regionMembers.addPlayer(playerUuid);
        this.region.setMembers(regionMembers);
    
        // Greeting
        String greeting = "&7[&5Mkt&7] ";
        if (this.owner != null) {
            greeting += "&6Plot &e" + this.uniqueId + " &6owned by &e" + Bukkit.getOfflinePlayer(this.owner).getName() + "&6.";
        } else if (this.price > -1 && this.rentTime > -1)
            greeting += "&2Plot &a" + this.uniqueId + " &2available for &a$" + this.price + "/" + this.rentTime + " days&2.";
        else
            greeting += "&3Entering plot &b" + this.uniqueId + "&3.";
        this.region.setFlag(DefaultFlag.GREET_MESSAGE, greeting);
    }
    
    /**
     * Resets the Region flags as follows:
     * - Region owner:   None
     * - Region members: None
     * - Greeting:       null
     */
    public void resetRegionFlags()
    {
        if (this.region == null)
            return;
        // Owner
        this.region.setOwners(new DefaultDomain());
        // Trusted
        this.region.setMembers(new DefaultDomain());
        // Greeting
        this.region.setFlag(DefaultFlag.GREET_MESSAGE, null);
    }
    
    // Sign Time ****************************************************************************************** Sign Time //
    
    /**
     * Adds a sign to the auto-fill list for this Market, then fills it.
     * @param sign Sign to be added and auto-generated
     */
    public void addSign(Sign sign)
    {
        if (this.signs == null) {
            this.signs = new SimpleLocation[]{new SimpleLocation(sign.getLocation())};
        } else {
            this.signs = Arrays.copyOf(this.signs, this.signs.length + 1);
            this.signs[this.signs.length - 1] = new SimpleLocation(sign.getLocation());
        }
        
        SignManager.generateMarketSign(this, sign);
    }
    
    /**
     * Attempts to remove a sign at the specified location from this Market.
     * @param sign Sign location to be removed.
     */
    public void removeSign(@NotNull SimpleLocation sign)
    {
        if (this.signs == null)
            return;
        
        ArrayList<SimpleLocation> output = new ArrayList<>();
        for (SimpleLocation oldSign : this.signs)
            if (!sign.equals(oldSign))
                output.add(oldSign);
        this.signs = output.toArray(new SimpleLocation[0]);
    }
    
    // Message Generation ************************************************************************ Message Generation //
    
    /**
     * Generates a message to send to an Admin about the Market.
     * The message is "fancy," as it contains text coloration and on-hover/click events.
     * @return Fancy admin-focused message.
     */
    public TextComponent[] getAdminInfo()
    {
        TextComponent[] output = new TextComponent[5];
        
        TextComponent spacer = new TextComponent(" | ");
        spacer.setColor(ChatColor.DARK_GRAY);
        
        // Header
        output[0] = new TextComponent("==================== ");
        output[0].setColor(ChatColor.DARK_BLUE);
        TextComponent marketId = new TextComponent(this.getUniqueId());
        marketId.setColor(ChatColor.BLUE);
        output[0].addExtra(marketId);
        output[0].addExtra(" ====================");
        
        // Owner and Date info
        output[1] = (TextComponent)spacer.duplicate();
        TextComponent ownerHeader = new TextComponent("Owner: ");
        ownerHeader.setColor(ChatColor.BLUE);
        ownerHeader.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
                "/mkt set " + this.getUniqueId() + " owner [UUID/Username]"));
        TextComponent ownerName = new TextComponent("N/A");
        if (this.getOwner() != null) {
            ownerName = new TextComponent(Bukkit.getOfflinePlayer(this.getOwner()).getName());
            TextComponent hoverUuid = new TextComponent("" + this.getOwner());
            hoverUuid.setColor(ChatColor.WHITE);
            ownerName.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new BaseComponent[]{hoverUuid}));
        }
        ownerName.setColor(ChatColor.AQUA);
        ownerName.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
                "/mkt set " + this.getUniqueId() + " owner [UUID/Username]"));
        output[1].addExtra(ownerHeader);
        output[1].addExtra(ownerName);
        output[1].addExtra(spacer.duplicate());
        
        TextComponent evictionHeader = new TextComponent("Eviction: ");
        evictionHeader.setColor(ChatColor.BLUE);
        evictionHeader.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
                "/mkt set " + this.getUniqueId() + " eviction [YEAR-MONTH-DAY HR-MN]"));
        output[1].addExtra(evictionHeader);
        TextComponent evictionDate;
        if (this.getEvictionDate() == null) {
            evictionDate = new TextComponent("N/A");
            
        } else {
            evictionDate = new TextComponent(this.getEvictionDate().toString());
        }
        evictionDate.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
                "/mkt set " + this.getUniqueId() + " eviction [YEAR-MONTH-DAY HR-MN]"));
        evictionDate.setColor(ChatColor.AQUA);
        output[1].addExtra(evictionDate);
        
        // Trusted members
        output[2] = (TextComponent)spacer.duplicate();
        TextComponent trustedHeader = new TextComponent("Trusted: ");
        trustedHeader.setColor(ChatColor.BLUE);
        trustedHeader.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
                "/mkt set " + this.getUniqueId() + " trusted [add/remove/reset] [UUID/Username]"));
        output[2].addExtra(trustedHeader);
        if (this.getMembers() == null || this.getMembers().length < 1) {
            TextComponent noTrusted = new TextComponent("N/A");
            noTrusted.setColor(ChatColor.AQUA);
            noTrusted.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
                    "/mkt set " + this.getUniqueId() + " trusted [add/remove/reset] [UUID/Username]"));
            output[2].addExtra(noTrusted);
        } else {
            for (UUID uuid : this.getMembers()) {
                TextComponent member = new TextComponent(Bukkit.getOfflinePlayer(uuid).getName() + " ");
                member.setColor(ChatColor.AQUA);
                member.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
                        "/mkt set " + this.getUniqueId() + " trusted [add/remove/reset] [UUID/Username]"));
                output[2].addExtra(member);
            }
        }
        
        // Price and Rent Time
        output[3] = (TextComponent)spacer.duplicate();
        TextComponent priceHeader = new TextComponent("Price: ");
        priceHeader.setColor(ChatColor.BLUE);
        priceHeader.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
                "/mkt set " + this.getUniqueId() + " price [number]"));
        output[3].addExtra(priceHeader);
        TextComponent priceAmt;
        if (this.getPrice() == -1) {
            priceAmt = new TextComponent("NOT SET");
            priceAmt.setColor(ChatColor.RED);
            priceAmt.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
                    "/mkt set " + this.getUniqueId() + " price [number]"));
        } else {
            priceAmt = new TextComponent("$" + this.getPrice());
            priceAmt.setColor(ChatColor.AQUA);
            priceAmt.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
                    "/mkt set " + this.getUniqueId() + " price [number]"));
        }
        output[3].addExtra(priceAmt);
        TextComponent per = new TextComponent(" per ");
        per.setColor(ChatColor.BLUE);
        output[3].addExtra(per);
        
        TextComponent durationHeader = new TextComponent("Duration: ");
        durationHeader.setColor(ChatColor.BLUE);
        durationHeader.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
                "/mkt set " + this.getUniqueId() + " duration [number]"));
        output[3].addExtra(durationHeader);
        TextComponent durationAmt;
        if (this.getRentTime() == -1) {
            durationAmt = new TextComponent("NOT SET");
            durationAmt.setColor(ChatColor.RED);
            durationAmt.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
                    "/mkt set " + this.getUniqueId() + " duration [number]"));
        } else {
            durationAmt = new TextComponent(this.getRentTime() + "");
            durationAmt.setColor(ChatColor.AQUA);
            durationAmt.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
                    "/mkt set " + this.getUniqueId() + " duration [number]"));
        }
        output[3].addExtra(durationAmt);
        
        // Signs:
        output[4] = (TextComponent)spacer.duplicate();
        TextComponent signHeader = new TextComponent("Signs: ");
        signHeader.setColor(ChatColor.BLUE);
        signHeader.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
                "/mkt reloadsigns"));
        output[4].addExtra(signHeader);
        if (this.getSigns() == null || this.getSigns().length < 1) {
            TextComponent noSigns = new TextComponent("None.");
            noSigns.setColor(ChatColor.AQUA);
            noSigns.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
                    "/mkt addplotsign " + this.getUniqueId()));
            output[4].addExtra(noSigns);
        } else {
            for (SimpleLocation sign : this.getSigns()) {
                TextComponent singleSign = new TextComponent("# ");
                singleSign.setColor(ChatColor.AQUA);
                TextComponent position = new TextComponent(sign.toString());
                position.setColor(ChatColor.WHITE);
                singleSign.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new TextComponent[]{position}));
                singleSign.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
                        "/mkt removesign " + sign.toString().substring(1, sign.toString().length()-1)));
                output[4].addExtra(singleSign);
            }
        }
        
        return output;
    }
    
    /**
     * Generates a message to send to a player about the Market, when the Market isn't ready to be sold yet.
     * The message is "fancy," as it contains text coloration and on-hover/click events.
     * @return Fancy peasant-focused message.
     */
    public TextComponent[] getNotReadyInfo()
    {
        TextComponent[] output = new TextComponent[2];
    
        TextComponent spacer = new TextComponent(" | ");
        spacer.setColor(ChatColor.DARK_GRAY);
        
        // Header
        output[0] = new TextComponent("==================== ");
        output[0].setColor(ChatColor.DARK_RED);
        TextComponent marketId = new TextComponent(this.getUniqueId());
        marketId.setColor(ChatColor.RED);
        output[0].addExtra(marketId);
        output[0].addExtra(" ====================");
        
        // pls leave me alone
        output[1] = (TextComponent)spacer.duplicate();
        TextComponent notReadywarning = new TextComponent("This plot is not yet ready for sale.");
        notReadywarning.setColor(ChatColor.YELLOW);
        output[1].addExtra(notReadywarning);
        
        return output;
    }
    
    /**
     * Generates a message to send to a Potential Buyer about the Market.
     * The message is "fancy," as it contains text coloration and on-hover/click events.
     * @return Fancy renting-focused message.
     */
    public TextComponent[] getForSaleInfo()
    {
        TextComponent[] output = new TextComponent[3];
    
        TextComponent spacer = new TextComponent(" | ");
        spacer.setColor(ChatColor.DARK_GRAY);
    
        // Header
        output[0] = new TextComponent("==================== ");
        output[0].setColor(ChatColor.DARK_GREEN);
        TextComponent marketId = new TextComponent(this.getUniqueId());
        marketId.setColor(ChatColor.DARK_AQUA);
        output[0].addExtra(marketId);
        output[0].addExtra(" ====================");
        
        // Size, price, and duration
        output[1] = (TextComponent)spacer.duplicate();
        TextComponent sizeHeader = new TextComponent("Size: ");
        sizeHeader.setColor(ChatColor.DARK_AQUA);
        output[1].addExtra(sizeHeader);
        TextComponent sizeValue = new TextComponent(this.calculateSize());
        sizeValue.setColor(ChatColor.GREEN);
        BlockVector min = this.getRegion().getMinimumPoint();
        BlockVector max = this.getRegion().getMaximumPoint();
        TextComponent corners = new TextComponent(min.toString() + " -> " + max.toString());
        corners.setColor(ChatColor.WHITE);
        sizeValue.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[]{corners}));
        output[1].addExtra(sizeValue);
        
        output[1].addExtra(spacer.duplicate());
        
        TextComponent priceHeader = new TextComponent("Price: ");
        priceHeader.setColor(ChatColor.DARK_GREEN);
        output[1].addExtra(priceHeader);
        TextComponent priceValue = new TextComponent("$" + this.getPrice());
        priceValue.setColor(ChatColor.GREEN);
        output[1].addExtra(priceValue);
        TextComponent per = new TextComponent(" per ");
        per.setColor(ChatColor.DARK_GREEN);
        output[1].addExtra(per);
        TextComponent durationValue = new TextComponent(this.getRentTime() + "");
        durationValue.setColor(ChatColor.GREEN);
        output[1].addExtra(durationValue);
        TextComponent days = new TextComponent(" days");
        days.setColor(ChatColor.DARK_GREEN);
        output[1].addExtra(days);
        
        // Click here to purchase!
        output[2] = (TextComponent)spacer.duplicate();
        TextComponent clickToPurchase = new TextComponent("Click me to rent this plot!");
        clickToPurchase.setColor(ChatColor.GREEN);
        TextComponent buyMe = new TextComponent("Click me!");
        buyMe.setColor(ChatColor.WHITE);
        clickToPurchase.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[]{buyMe}));
        clickToPurchase.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
                "/mkt rent " + this.getUniqueId()));
        output[2].addExtra(clickToPurchase);
        
        return output;
    }
    
    /**
     * Generates a message to send to a plot-Owner about their Market.
     * The message is "fancy," as it contains text coloration and on-hover/click events.
     * @return Fancy owner-focused message.
     */
    public TextComponent[] getOwnerInfo()
    {
        TextComponent[] output = new TextComponent[3];
    
        TextComponent spacer = new TextComponent(" | ");
        spacer.setColor(ChatColor.DARK_GRAY);
    
        // Header
        output[0] = new TextComponent("==================== ");
        output[0].setColor(ChatColor.DARK_GRAY);
        TextComponent marketId = new TextComponent(this.getUniqueId());
        marketId.setColor(ChatColor.DARK_PURPLE);
        output[0].addExtra(marketId);
        output[0].addExtra(" ====================");
    
        // Size, price, and duration
        output[1] = (TextComponent)spacer.duplicate();
        TextComponent sizeHeader = new TextComponent("Size: ");
        sizeHeader.setColor(ChatColor.DARK_PURPLE);
        output[1].addExtra(sizeHeader);
        TextComponent sizeValue = new TextComponent(this.calculateSize());
        sizeValue.setColor(ChatColor.LIGHT_PURPLE);
        BlockVector min = this.getRegion().getMinimumPoint();
        BlockVector max = this.getRegion().getMaximumPoint();
        TextComponent corners = new TextComponent(min.toString() + " -> " + max.toString());
        corners.setColor(ChatColor.WHITE);
        sizeValue.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[]{corners}));
        output[1].addExtra(sizeValue);
    
        output[1].addExtra(spacer.duplicate());
        
        TextComponent clickToExtend = new TextComponent("Click to extend your rent!");
        clickToExtend.setColor(ChatColor.WHITE);
        
        TextComponent priceHeader = new TextComponent("Price: ");
        priceHeader.setColor(ChatColor.DARK_PURPLE);
        priceHeader.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[]{clickToExtend}));
        priceHeader.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
                "/mkt rent " + this.getUniqueId()));
        output[1].addExtra(priceHeader);
        TextComponent priceValue = new TextComponent("$" + this.getPrice());
        priceValue.setColor(ChatColor.LIGHT_PURPLE);
        priceValue.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[]{clickToExtend}));
        priceValue.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
                "/mkt rent " + this.getUniqueId()));
        output[1].addExtra(priceValue);
        TextComponent per = new TextComponent(" per ");
        per.setColor(ChatColor.DARK_PURPLE);
        per.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[]{clickToExtend}));
        per.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
                "/mkt rent " + this.getUniqueId()));
        output[1].addExtra(per);
        TextComponent durationValue = new TextComponent(this.getRentTime() + "");
        durationValue.setColor(ChatColor.LIGHT_PURPLE);
        durationValue.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[]{clickToExtend}));
        durationValue.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
                "/mkt rent " + this.getUniqueId()));
        output[1].addExtra(durationValue);
        TextComponent days = new TextComponent(" days");
        days.setColor(ChatColor.DARK_PURPLE);
        days.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[]{clickToExtend}));
        days.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
                "/mkt rent " + this.getUniqueId()));
        output[1].addExtra(days);
        
        // Trusted Members
        output[2] = (TextComponent)spacer.duplicate();
        TextComponent trustedHeader = new TextComponent("Trusted: ");
        trustedHeader.setColor(ChatColor.DARK_PURPLE);
        trustedHeader.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
                // Todo: Change this command to /mkt trusted [plotid] [add/remove/reset] <UUID/Username>
                "/mkt set " + this.getUniqueId() + " trusted [add/remove/reset] [UUID/Username]"));
        output[2].addExtra(trustedHeader);
        if (this.getMembers() == null || this.getMembers().length < 1) {
            TextComponent noTrusted = new TextComponent("N/A");
            noTrusted.setColor(ChatColor.LIGHT_PURPLE);
            noTrusted.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
                    // Todo: Change this command to /mkt trusted [plotid] [add/remove/reset] <UUID/Username>
                    "/mkt set " + this.getUniqueId() + " trusted [add/remove/reset] [UUID/Username]"));
            output[2].addExtra(noTrusted);
        } else {
            for (UUID uuid : this.getMembers()) {
                TextComponent member = new TextComponent(Bukkit.getOfflinePlayer(uuid).getName() + " ");
                member.setColor(ChatColor.LIGHT_PURPLE);
                member.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
                        // Todo: Change this command to /mkt trusted [plotid] [add/remove/reset] <UUID/Username>
                        "/mkt set " + this.getUniqueId() + " trusted [add/remove/reset] [UUID/Username]"));
                output[2].addExtra(member);
            }
        }
        
        return output;
    }
    
    /**
     * Generates a message to send to a player about the Market.
     * The message is "fancy," as it contains text coloration and on-hover/click events.
     * @return Fancy peasant-focused message.
     */
    public TextComponent[] getGenericInfo()
    {
        TextComponent[] output = new TextComponent[2];
    
        TextComponent spacer = new TextComponent(" | ");
        spacer.setColor(ChatColor.GRAY);
    
        // Header
        output[0] = new TextComponent("==================== ");
        output[0].setColor(ChatColor.GRAY);
        TextComponent marketId = new TextComponent(this.getUniqueId());
        marketId.setColor(ChatColor.GOLD);
        output[0].addExtra(marketId);
        output[0].addExtra(" ====================");
    
        // Owner and Date info
        output[1] = (TextComponent)spacer.duplicate();
        TextComponent ownerHeader = new TextComponent("Owner: ");
        ownerHeader.setColor(ChatColor.GOLD);
        TextComponent ownerName = new TextComponent("N/A");
        if (this.getOwner() != null) {
            ownerName = new TextComponent(Bukkit.getOfflinePlayer(this.getOwner()).getName());
            TextComponent hoverUuid = new TextComponent("" + this.getOwner());
            hoverUuid.setColor(ChatColor.WHITE);
            ownerName.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new BaseComponent[]{hoverUuid}));
        }
        ownerName.setColor(ChatColor.YELLOW);
        output[1].addExtra(ownerHeader);
        output[1].addExtra(ownerName);
        output[1].addExtra(spacer.duplicate());
    
        TextComponent evictionHeader = new TextComponent("Eviction: ");
        evictionHeader.setColor(ChatColor.GOLD);
        output[1].addExtra(evictionHeader);
        TextComponent evictionDate;
        if (this.getEvictionDate() == null) {
            evictionDate = new TextComponent("N/A");
        
        } else {
            evictionDate = new TextComponent(this.getEvictionDate().toString());
        }
        evictionDate.setColor(ChatColor.YELLOW);
        output[1].addExtra(evictionDate);
        
        return output;
    }
    
    /**
     * Creates a printable string that contains the dimensions of this Market's area.
     * @return String in format [x*y*z]
     */
    public String calculateSize()
    {
        BlockVector min = this.getRegion().getMinimumPoint();
        BlockVector max = this.getRegion().getMaximumPoint();
        int x,y,z;
        
        if (min.getX() > max.getX())
            x = (int)Math.abs(min.getX() - max.getX());
        else
            x = (int)Math.abs(max.getX() - min.getX());
    
        if (min.getY() > max.getY())
            y = (int)Math.abs(min.getY() - max.getY());
        else
            y = (int)Math.abs(max.getY() - min.getY());
    
        if (min.getZ() > max.getZ())
            z = (int)Math.abs(min.getZ() - max.getZ());
        else
            z = (int)Math.abs(max.getZ() - min.getZ());
        
        return "[" + (x+1) + "*" + (y+1) + "*" + (z+1) + "]";
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
    
    public UUID[] getMembers()
    {
        return members;
    }
    
    public void setMembers(UUID[] members)
    {
        this.members = members;
    }
    
    public int getPrice()
    {
        return price;
    }
    
    public void setPrice(int price)
    {
        this.price = price;
    }
    
    public int getRentTime()
    {
        return rentTime;
    }
    
    public void setRentTime(int rentTime)
    {
        this.rentTime = rentTime;
    }
    
    public Date getEvictionDate()
    {
        return evictionDate;
    }
    
    public void setEvictionDate(Date evictionDate)
    {
        this.evictionDate = evictionDate;
        
        if (this.deathTimer != null)
            this.deathTimer.cancel();
        if (this.evictionDate == null) {
            this.deathTimer = null;
        } else {
            this.deathTimer = new EvictionTimer(this);
            // Setting the timer is run as a separate task, because at plugin load the DataManager isn't initialized yet
            Bukkit.getScheduler().runTask(FusionMarket.getInstance(), () -> {
                FusionMarket.getInstance().getDataManager().setTimer(this.deathTimer, this.getEvictionDate());
            });
        }
    }
    
    public ProtectedRegion getRegion()
    {
        return region;
    }
    
    public void setRegion(ProtectedRegion region)
    {
        this.region = region;
    }
    
    public SimpleLocation[] getSigns()
    {
        return signs;
    }
    
    public void setSigns(SimpleLocation[] signs)
    {
        this.signs = signs;
    }
    
    public String getWorld()
    {
        return world;
    }
    
    public void setWorld(String world)
    {
        this.world = world;
    }
    
    public TimerTask getDeathTimer()
    {
        return deathTimer;
    }
    
    public void setDeathTimer(TimerTask deathTimer)
    {
        this.deathTimer = deathTimer;
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
        
        output.put("Unique ID", this.uniqueId);
        output.put("World", this.world);
        if (this.owner == null)
            output.put("Owner", null);
        else
            output.put("Owner", this.owner.toString());
        
        if (this.members == null)
            output.put("Members", null);
        else {
            ArrayList<String> newMemberList = new ArrayList<>();
            for (UUID uuid : this.members)
                newMemberList.add(uuid.toString());
            output.put("Members", newMemberList);
        }
        
        output.put("Price", this.price);
        output.put("Rent Time", this.rentTime);
        output.put("Eviction Date", this.evictionDate);
        if (this.signs == null)
            output.put("Signs", null);
        else
            output.put("Signs", Arrays.asList(this.signs));
    
        if (this.region == null)
            output.put("Region", null);
        else
            output.put("Region", this.region.getId());
    
        return output;
    }
    
    /**
     * Attempted deserialization
     * @param input
     * @return
     */
    public static Market deserialize(Map<String, Object> input)
    {
        Market output = new Market();
        
        // Sensitive things
        if (input.containsKey("Unique ID"))
            output.setUniqueId((String)input.get("Unique ID"));
        else
            return null;
        if (input.containsKey("Price"))
            output.setPrice((int)input.get("Price"));
        else
            output.setPrice(-1);
        if (input.containsKey("Rent Time"))
            output.setRentTime((int)input.get("Rent Time"));
        else
            output.setRentTime(-1);
        if (input.containsKey("World"))
            output.setWorld((String)input.get("World"));
        if (Bukkit.getWorld(output.getWorld()) != null && input.containsKey("Region")) {
            output.setRegion(WorldGuardPlugin.inst().getRegionManager(Bukkit.getWorld(output.getWorld()))
                    .getRegion((String)input.get("Region")));
        } else {
            output.setRegion(null);
        }
        
        // Stuff that can be null without backfiring on me, probably
        if (input.containsKey("Owner") && input.get("Owner") != null)
            output.setOwner(UUID.fromString((String)input.get("Owner")));
        else
            output.setOwner(null);
        if (input.containsKey("Members") && input.get("Members") != null) {
            ArrayList<String> memberInput = (ArrayList<String>)input.get("Members");
            UUID[] memberOutput = new UUID[memberInput.size()];
            int i=0;
            for (String stringUUID : memberInput) {
                memberOutput[i] = UUID.fromString(stringUUID);
                i++;
            }
            output.setMembers(memberOutput);
        }
        else
            output.setMembers(null);
        if (input.containsKey("Eviction Date") && input.get("Eviction Date") != null)
            output.setEvictionDate((Date)input.get("Eviction Date"));
        else
            output.setEvictionDate(null);
        
        
        // WIP
        if (input.containsKey("Signs") && input.get("Signs") != null) {
            output.setSigns(((List<SimpleLocation>)input.get("Signs")).toArray(new SimpleLocation[0]));
        }
        return output;
    }
}
