package io.github.lordfusion.fusionmarket;

import com.comphenix.protocol.wrappers.nbt.NbtFactory;
import com.comphenix.protocol.wrappers.nbt.NbtWrapper;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import io.github.lordfusion.fusionmarket.utilities.PurchaseRecord;
import io.github.lordfusion.fusionmarket.utilities.SignManager;
import io.github.lordfusion.fusionmarket.utilities.SimpleLocation;
import org.bukkit.*;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import static org.bukkit.Bukkit.getServer;

public class DataManager
{
    // Files
    static final String DATA_FILE_NAME = "/FusionMarket Data.yml";
    static final String RECORDS_FILE_NAME = "/Purchase Records.yml";
    static final String METADATA_FOLDER = "/Shop Metadata/";
    static String dataFolderPath;
    private YamlConfiguration dataFile;
    private YamlConfiguration recordsFile;
    // Player data
    private HashMap<Player, Market> apsData;
    private HashMap<Player, Boolean> adminMode;
    private HashMap<Player, Boolean> adminShopCreationMode;
    // Working data
    private ArrayList<Market> markets;
    private ArrayList<Shop> shops;
    private Timer timer;
    private RecordSaver recordSaver;
    
    // Purchase data
    private HashMap<UUID, ArrayList<PurchaseRecord>> records;
    
    /**
     * The DataManager... Manages data. All of it. Any file-related data this plugin needs, here it is. Right here.
     * @param pluginDataFolder Folder where all data is/should be stored.
     */
    DataManager(@NotNull String pluginDataFolder)
    {
        this.dataFolderPath = pluginDataFolder;
        
        // Set up the metadata folder
        File metadataFolder = new File(this.dataFolderPath + METADATA_FOLDER);
        if (!metadataFolder.exists() && !metadataFolder.mkdir())
            FusionMarket.sendConsoleWarn("Failed to create Metadata directory. This might cause problems later!");
        
        // Set up the fake-player-metadata
        this.apsData = new HashMap<>();
        this.adminMode = new HashMap<>();
        this.adminShopCreationMode = new HashMap<>();
        
        // Parse data
        this.reloadConfigs();
        
        // Create the death timer
        this.timer = new Timer();
    }
    
    /**
     * Uses the config to determine a set of user-specified chest-types, to be fair to all modded chests.
     * Todo: Add config option
     * @return A collection containing the Materials representing all valid chest substitutes
     */
    @NotNull
    public static Collection<Material> getChestSubstitutes()
    {
        return Arrays.asList(Material.getMaterial("CHEST"),
                Material.getMaterial("TRAPPED_CHEST"),
                Material.getMaterial("APPLIEDENERGISTICS2_TILEBLOCKSKYCHEST"));
    }
    
    public void setTimer(TimerTask task, Date date)
    {
        this.timer.schedule(task, date);
    }
    
    // Shops ************************************************************************************************** Shops //
    
    /**
     * Uses the provided location to find a Shop with a matching chest or sign.
     * @param location Position of a Chest or Sign.
     */
    @Nullable
    public Shop getShop(@NotNull Location location)
    {
        if (location == null || this.shops == null || this.shops.isEmpty())
            return null;
        
        for (Shop shop : this.shops) {
            if (shop.getSign() == null ||
                    (!shop.isAdminShop() && !location.getWorld().getName().equalsIgnoreCase(shop.getChestLocation().getWorldName())) ||
                    (shop.isAdminShop() && !location.getWorld().getName().equalsIgnoreCase(shop.getSign().getWorld().getName()))
                )
                continue;
            if ((!shop.isAdminShop() && shop.getChestLocation().asLocation().distance(location) == 0) ||
                    shop.getSign().getLocation().distance(location) == 0)
                return shop;
        }
        return null;
    }
    
    /**
     * Uses the provided name to find a Shop a matching unique ID.
     * @param uniqueId Supposed Shop ID
     */
    @Nullable
    public Shop getShop(@NotNull String uniqueId)
    {
        if (this.shops == null || this.shops.isEmpty())
            return null;
        
        for (Shop shop : this.shops)
            if (shop.getUniqueId().equalsIgnoreCase(uniqueId))
                return shop;
        return null;
    }
    
    /**
     * Officially creates a new FusionMarket Shop.
     * @param owner    Player creating the shop.
     * @param location Chest location, to contain the shop's wares.
     * @param item     The item to be sold.
     * @param sign     Sign location, to display the shop's information.
     * @return A newly-created Shop, or null if there was a problem.
     */
    public Shop createNewShop(@NotNull Player owner, @NotNull Location location, @NotNull ItemStack item, @NotNull Sign sign)
    {
        Shop shop = new Shop();
        
        // Easy, boring variables.
        shop.setUniqueId(this.generateShopName(owner.getName()));
        shop.setOwner(owner.getUniqueId());
        shop.setPrice(0);
        shop.setChestLocation(new SimpleLocation(location));
        shop.setSign(sign);
        shop.setAdminShop(false);
        
        // Save the item's metadata, so I can figure out what to do with it in the future.
        shop.setItem(item.clone());
        this.saveMetadata(item.clone(), shop.getUniqueId());
        
        this.shops.add(shop);
        this.saveDataFile();
        
        SignManager.generateShopSign(shop, sign, false);
        return shop;
    }
    
    public Shop createNewAdminShop(@NotNull Sign sign, @NotNull ItemStack item)
    {
        Shop shop = new Shop();
        
        // Easy, boring variables.
        shop.setUniqueId(this.generateShopName("AdminShop"));
        shop.setOwner(null);
        shop.setPrice(0);
        shop.setChestLocation(null);
        shop.setSign(sign);
        shop.setAdminShop(true);
    
        // Save the item's metadata, so I can figure out what to do with it in the future.
        shop.setItem(item.clone());
        this.saveMetadata(item.clone(), shop.getUniqueId());
        
        this.shops.add(shop);
        this.saveDataFile();
        
        SignManager.generateShopSign(shop, sign, false);
        return shop;
    }
    
    /**
     * Generates a new unique shop name, based on the format "username-xx" where xx is a generated number.
     * @param username Shop owner's username
     * @return Unique Shop name
     */
    public String generateShopName(@NotNull String username)
    {
        if (this.shops == null || this.shops.size() == 0)
            return username + "-" + 1;
        
        int i=1;
        while (i<200) {
            boolean idUnique = true;
            for (Shop shop : this.shops)
                if (shop.getUniqueId().equalsIgnoreCase(username + "-" + i)) {
                    idUnique = false;
                    break;
                }
            if (idUnique)
                return username + "-" + i;
            i++;
        }
        return null;
    }
    
    /**
     * Uses ProtocolLib to save the item's entire metadata to an independent file.
     * @param item     Item whose data is to be saved.
     * @param fileName Name of the file that will contain the saved data.
     */
    private void saveMetadata(@NotNull ItemStack item, @NotNull String fileName)
    {
        String filePath = this.dataFolderPath + METADATA_FOLDER + fileName + ".mkt";
        try {
            NbtWrapper nbtWrapper = NbtFactory.fromItemTag(item);
            NbtFactory.toFile(NbtFactory.asCompound(nbtWrapper), filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * A new way to save metadata!
     * @param item     Item whose data is to be saved.
     * @param fileName Name of the file that will contain the saved data.
     */
    private void newSaveMetadata(@NotNull ItemStack item, @NotNull String fileName)
    {
    
    }
    
    /**
     * Removes the provided Shop from the data set. Breaks the associated sign if necessary.
     * @param shop Shop to be deleted.
     */
    public void removeShop(@NotNull Shop shop)
    {
        this.shops.remove(shop);
        if (shop != null && shop.getSign() != null && shop.getSign().getLocation().getBlock() != null &&
                shop.getSign().getLocation().getBlock().getState() instanceof Sign)
            shop.getSign().getLocation().getBlock().breakNaturally();
        this.saveDataFile();
    }
    
    /**
     * Removes multiple shops at once, then saves to file when finished.
     * @param invalidShops Array of shops to be deleted.
     */
    public void purgeShops(Shop[] invalidShops)
    {
        for (Shop shop : invalidShops) {
            FusionMarket.sendConsoleInfo("Purging invalid shop: '" + shop.getUniqueId() + "'.");
            this.shops.remove(shop);
            if (shop.getSign() != null && shop.getSign().getLocation().getBlock() != null &&
                    shop.getSign().getLocation().getBlock().getState() instanceof Sign)
                shop.getSign().getLocation().getBlock().breakNaturally();
        }
        this.saveDataFile();
    }
    
    /**
     * Gets the total number of existing shops made by the given player
     * @param player Player who would theoretically own a shop
     * @return Number of shops owned by the given player
     */
    public int getShopCount(Player player)
    {
        int output = 0;
        for (Shop shop : this.shops)
            if (shop.getOwner() != null && shop.getOwner().equals(player.getUniqueId()))
                output++;
        return output;
    }
    
    /**
     * Checks a bunch of conditions to determine if a player should be able to break a shop.
     * @param player Player attempting to break a sign or chest.
     * @param shop   Shop being broken
     * @return True if the player can break the shop, false if they don't have permission.
     */
    public boolean canBreakShop(Player player, Shop shop) {
        if (player == null || shop == null)
            return false;
        
        // Special rules for admin shops
        if (shop.isAdminShop()) {
            return player.hasPermission("fusion.market.adminshop");
        }
        
        // Override permission
        if (player.hasPermission("fusion.market.shops.override"))
            return true;
        
        // Shop owner can always break their own shop
        if (shop.getOwner().equals(player.getUniqueId()))
            return true;
        
        // Market plot owners should be able to break all shops on their own plot
        Market market = getMarket(shop.getChestLocation().asLocation());
        if (market == null)
            return false;
        if (market.getOwner().equals(player.getUniqueId()))
            return true;
        
        // People allowed on the plot can break any shop not owned by the plot owner
        if (Arrays.asList(market.getMembers()).contains(player.getUniqueId())) {
            return !shop.getOwner().equals(market.getOwner());
        }
        
        return false;
    }
    
    /**
     * todo: documentation
     * @return
     */
    public Shop[] getAllShops()
    {
        return this.shops.toArray(new Shop[0]);
    }
    
    // Markets ********************************************************************************************** Markets //
    
    /**
     * Uses the provided location to find a Market with a matching sign, or containing the given location.
     * @param location Position of a Chest or Sign.
     * @return Market that contains the provided location, or has a matching sign.
     */
    @Nullable
    public Market getMarket(@NotNull Location location)
    {
        if (this.markets == null || this.markets.isEmpty())
            return null;
        
        SimpleLocation sloc = new SimpleLocation(location);
        for (Market market : this.markets) {
            if (market.getRegion().contains(sloc.getX(), sloc.getY(), sloc.getZ()))
                return market;
            if (market.getSigns() == null || market.getSigns().length < 1)
                continue;
            for (SimpleLocation sign : market.getSigns()) {
                if (sloc.equals(sign))
                    return market;
            }
        }
        return null;
    }
    
    /**
     * Uses the provided name to find a Market with a matching unique ID.
     * @param uniqueId Supposed Market ID
     * @return Market that matches the given Unique ID, if it exists.
     */
    @Nullable
    public Market getMarket(@NotNull String uniqueId)
    {
        if (this.markets == null || this.markets.isEmpty())
            return null;
    
        for (Market market : this.markets)
            if (market.getUniqueId().equalsIgnoreCase(uniqueId))
                return market;
        return null;
    }
    
    /**
     * Uses the provided Location to find a Market with a matching sign.
     * @param location Position of a Sign.
     * @return Market that has a sign that matches the location.
     */
    public Market getMarketByInfoSign(Location location)
    {
        if (this.markets == null || this.markets.isEmpty())
            return null;
    
        SimpleLocation sloc = new SimpleLocation(location);
        for (Market market : this.markets) {
            if (market.getSigns() == null || market.getSigns().length < 1)
                continue;
            for (SimpleLocation sign : market.getSigns()) {
                if (sloc.equals(sign))
                    return market;
            }
        }
        return null;
    }
    
    /**
     * Officially creates a new FusionMarket Market.
     * @param world    World containing the FusionMarket region.
     * @param region   Previously unused WorldGuard region
     * @param uniqueId A unique name.
     * @return
     */
    public Market createNewMarket(@NotNull World world, @NotNull ProtectedRegion region, @NotNull String uniqueId)
    {
        Market market = new Market();
        
        market.setUniqueId(uniqueId);
        market.setWorld(world.getName());
        market.setOwner(null);
        market.setMembers(null);
        market.setPrice(-1);
        market.setRentTime(-1);
        market.setEvictionDate(null);
        market.setRegion(region);
        market.setSigns(null);
        
        market.setRegionFlags();
        
        this.markets.add(market);
        this.saveDataFile();
        return market;
    }
    
    /**
     * Generates a new unique shop name, based on the format "market-xx" where xx is a generated number.
     * @return Unique Market name
     */
    @NotNull
    public String generateMarketName()
    {
        int i=0;
        for (Market market : this.markets) {
            if (market.getUniqueId().equalsIgnoreCase("market-" + i))
                break;
            i++;
        }
        return "market-" + i;
    }
    
    /**
     * Removes the provided Market from the data set. Optionally deletes the associated region.
     * @param market       Market to be deleted.
     * @param deleteRegion True to delete the Region, false to only reset the region flags.
     */
    public void removeMarket(@NotNull Market market, boolean deleteRegion)
    {
        this.markets.remove(market);
        if (deleteRegion)
            if (WorldGuardPlugin.inst().getRegionManager(Bukkit.getWorld(market.getWorld())).hasRegion(market.getRegion().getId()))
                WorldGuardPlugin.inst().getRegionManager(Bukkit.getWorld(market.getWorld())).removeRegion(market.getRegion().getId());
        else
            market.resetRegionFlags();
        
        market.getDeathTimer().cancel();
        
        if (market.getSigns() != null && market.getSigns().length > 0)
            for (SimpleLocation sign : market.getSigns()) {
                Location loc = sign.asLocation();
                if (loc != null)
                    SignManager.resetSign(loc);
            }
        this.saveDataFile();
    }
    
    public Market getMarket(Player player)
    {
        for (Market market : this.markets) {
            if (market.getOwner() != null && market.getOwner().equals(player.getUniqueId()))
                return market;
            if (market.getMembers() != null && market.getMembers().length > 0 &&
                    Arrays.asList(market.getMembers()).contains(player.getUniqueId()))
                return market;
        }
        return null;
    }
    
    // Player "Metadata" ************************************************************************** Player "Metadata" //
    
    /**
     * For the "AddPlotSign" system.
     * If the player is actively selecting Market plot signs, this will return their chosen Market.
     * @param player Player who might be adding plot signs.
     * @return Market if the player is doing APS, null otherwise.
     */
    @Nullable
    public Market getApsSelection(@NotNull Player player)
    {
        return this.apsData.get(player);
    }
    
    /**
     * For the "AddPlotSign" system.
     * Sets the Market that a player wants to add signs to.
     * @param player Player who wants to add plot signs.
     * @param market Market to add signs for, or null to reset.
     */
    public void setApsSelection(@NotNull Player player, Market market)
    {
        if (market == null)
            this.apsData.remove(player);
        else
            this.apsData.put(player, market);
    }
    
    /**
     * Checks to see if the specified player desires to be in "Admin Mode"
     * Admin mode shows admin-themed messages with admin-level command offerings.
     * Normie mode shows the same thing that normal players see.
     * @param player Interacting player
     * @return True if admin mode, false if normie mode.
     */
    public boolean isAdminMode(@NotNull Player player)
    {
        if (this.adminMode == null || this.adminMode.get(player) == null)
            return false;
        return this.adminMode.get(player);
    }
    
    /**
     * Sets the player's Admin Mode status.
     * @param player    Player to change status of
     * @param adminMode True to activate Admin Mode, false to deactivate.
     */
    public void setAdminMode(@NotNull Player player, boolean adminMode)
    {
        if (adminMode)
            this.adminMode.put(player, true);
        else
            this.adminMode.remove(player);
    }
    
    /**
     * Checks to see if the specified player is in the mode to create admin shops.
     * @param player The player attempting to make an admin shop
     * @return True if the player is set to make admin shops, false otherwise.
     */
    public boolean isInAdminShopCreationMode(Player player)
    {
        if (this.adminShopCreationMode == null || this.adminShopCreationMode.get(player) == null)
            return false;
        return this.adminShopCreationMode.get(player);
    }
    
    public void setInAdminShopCreationMode(@NotNull Player player, boolean iascm)
    {
        if (iascm)
            this.adminShopCreationMode.put(player, true);
        else
            this.adminShopCreationMode.remove(player);
    }
    
    // Shopping Records **************************************************************************** Shopping Records //
    
    /**
     * Takes the provided purchase record and commits it to the memory file
     * @param newRecord Pre-completed purchase record
     */
    public void createRecord(PurchaseRecord newRecord)
    {
        ArrayList<PurchaseRecord> playerRecords;
        if (this.records.containsKey(newRecord.getCustomer()))
            playerRecords = this.records.get(newRecord.getCustomer());
        else
            playerRecords = new ArrayList<>();
        playerRecords.add(newRecord);
        
        this.records.put(newRecord.getCustomer(), playerRecords);
        if (this.recordSaver != null)
            this.recordSaver.cancel();
        this.recordSaver = new RecordSaver();
        this.timer.schedule(this.recordSaver, 600);
    }
    
    /**
     * Get all known records for the specified player. Does not include sales from shops they own.
     * @param player Customer to find records for.
     * @return Any records found. Null if no records exist.
     */
    public PurchaseRecord[] getPlayerRecords(UUID player)
    {
        if (this.records.containsKey(player) && !this.records.get(player).isEmpty())
            return this.records.get(player).toArray(new PurchaseRecord[0]);
        else
            return null;
    }
    
//    /**
//     * Count the number of times a player has purchased from this shop, since the given time frame.
//     * @param player    Customer UUID
//     * @param shopId    AdminShop unique ID
//     * @param sinceTime Time to search from
//     * @return read the fucking description
//     */
//    public int countAdminPurchases(UUID player, String shopId, Date sinceTime)
//    {
//        if (!this.records.containsKey(player) || this.records.get(player).isEmpty())
//            return 0;
//
//        int counter = 0;
//        for (PurchaseRecord record : this.records.get(player))
//            if (record.getShopName().equalsIgnoreCase(shopId)
//                    && record.getTime().after(sinceTime)
//                    && record.getQuantity() > 0)
//                counter++;
//        return counter;
//    }
//
//    /**
//     * Count the number of times a player has sold to this shop, since the given time frame.
//     * @param player    Customer UUID
//     * @param shopId    AdminShop unique ID
//     * @param sinceTime Time to search from
//     * @return read the fucking description
//     */
//    public int countAdminSales(UUID player, String shopId, Date sinceTime)
//    {
//        if (!this.records.containsKey(player) || this.records.get(player).isEmpty())
//            return 0;
//
//        int counter = 0;
//        for (PurchaseRecord record : this.records.get(player))
//            if (record.getShopName().equalsIgnoreCase(shopId)
//                    && record.getTime().after(sinceTime)
//                    && record.getQuantity() < 0)
//                counter++;
//        return counter;
//    }
//
    /**
     * Count the total number of items a player has transacted with this shop, since the given time frame.
     * @param player    Customer UUID
     * @param shop      Shop to check for transactions
     * @param sinceTime Time to search from
     * @return Gives number of items bought if shop is set to sell, or the number of items sold if the shop is set to buy.
     */
    public int countTransactions(UUID player, Shop shop, Date sinceTime)
    {
        if (shop == null || !this.records.containsKey(player) || this.records.get(player).isEmpty())
            return 0;
        
        int counter = 0;
        boolean buyFromPlayer = shop.getPrice() < 0;
        for (PurchaseRecord record : this.records.get(player))
            if (record.getShopName().equalsIgnoreCase(shop.getUniqueId())
                    && record.getTime().after(sinceTime)
                    && ((record.getQuantity() < 0 && buyFromPlayer) ||
                        (record.getQuantity() > 0 && !buyFromPlayer)))
                counter += Math.abs(record.getQuantity());
        return counter;
    }
    
    /**
     * Count the total number of items all players have transacted with this shop, since the given time frame.
     * @param shop      Shop to check for transactions
     * @param sinceTime Time to search from
     * @return Gives number of items bought if shop is set to sell, or the number of items sold if the shop is set to buy.
     */
    public int countTotalTransactions(Shop shop, Date sinceTime)
    {
        if (shop == null)
            return 0;
    
        int counter = 0;
        boolean buyFromPlayer = shop.getPrice() < 0;
        for (UUID player : this.records.keySet())
            for (PurchaseRecord record : this.records.get(player))
                if (record.getShopName().equalsIgnoreCase(shop.getUniqueId())
                        && record.getTime().after(sinceTime)
                        && ((record.getQuantity() < 0 && buyFromPlayer) ||
                        (record.getQuantity() > 0 && !buyFromPlayer)))
                    counter += Math.abs(record.getQuantity());
        return counter;
    }
    
    private class RecordSaver extends TimerTask
    {
        /**
         * The action to be performed by this timer task.
         */
        @Override
        public void run()
        {
            FusionMarket.getInstance().getDataManager().saveRecordsFile();
        }
    }
    
    // Data Files **************************************************************************************** Data Files //
    
    /**
     * Reloads all configs from file.
     * If the config is not found, it will be regenerated.
     */
    public void reloadConfigs()
    {
        this.dataFile = loadConfig(DATA_FILE_NAME);
        this.loadDataFile();
        this.recordsFile = loadConfig(RECORDS_FILE_NAME);
        this.loadRecordsFile();
        
        FusionMarket.sendConsoleInfo("All configs have been reloaded.");
    }
    
    /**
     * Attempts to load the given config from the plugin's data folder.
     * If the config file does not already exist, it will be generated.
     * @param configName Name of the config to load.
     */
    private YamlConfiguration loadConfig(@NotNull String configName)
    {
        YamlConfiguration output;
        File configFile = new File(this.dataFolderPath + configName);
        
        if (!configFile.exists()) {
            FusionMarket.sendConsoleWarn("Config '" + configName + "' not found. It will be generated.");
            configFile = new File(this.dataFolderPath + configName);
            output = YamlConfiguration.loadConfiguration(configFile);
            
            output.options().copyDefaults(false).indent(3);
            if (configName.equalsIgnoreCase(DATA_FILE_NAME)) {
                output.options().header("FusionMarket Data Storage File \n" +
                        "Please don't edit this file unless you know what you're doing. Which you don't. \n" +
                        "Even I don't know what I'm doing, and I wrote this thing.");
            } else if (configName.equalsIgnoreCase(RECORDS_FILE_NAME)) {
                output.options().header("FusionMarket Purchase Records File \n" +
                        "Please don't edit this file unless you know what you're doing. Which you don't. \n" +
                        "Even I don't know what I'm doing, and I wrote this thing.");
            }
            
            try {
                output.save(this.dataFolderPath + configName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            output = YamlConfiguration.loadConfiguration(configFile);
        }
        
        return output;
    }
    
    /**
     * Uses the data from the data file to populate the Markets and Shops in memory.
     */
    public void loadDataFile()
    {
        FusionMarket.sendConsoleInfo("Loading Market and Shop data from file...");
        
        // Market Data
        this.markets = new ArrayList<>();
        if (this.dataFile.contains("Markets")) {
            ArrayList<Market> parsedMarkets = (ArrayList<Market>) this.dataFile.get("Markets");
            if (parsedMarkets != null && parsedMarkets.size() > 0) {
                for (Market market : parsedMarkets) {
                    if (market == null || market.getUniqueId() == null)
                        FusionMarket.sendConsoleWarn("Invalid market found in file, it will removed on next file save.");
                    else {
                        this.markets.add(market);
                        if (market.getRegion() != null)
                            market.setRegionFlags();
                        if (market.getSigns() != null && market.getSigns().length > 0)
                            for (SimpleLocation sign : market.getSigns())
                                SignManager.generateMarketSign(market, sign.asLocation());
                    }
                }
            } else
                FusionMarket.sendConsoleInfo("  'Markets' data is empty on file, and will be skipped.");
        } else
            FusionMarket.sendConsoleInfo("  'Markets' data is missing from file, and will be skipped.");
        
        // Shop Data
        int loadedSigns = 0;
        this.shops = new ArrayList<>();
        if (this.dataFile.contains("Shops")) {
            ArrayList<Shop> parsedShops = (ArrayList<Shop>) this.dataFile.get("Shops");
            if (parsedShops != null && parsedShops.size() > 0) {
                for (Shop shop : parsedShops) {
                    if (shop == null || shop.getUniqueId() == null)
                        FusionMarket.sendConsoleWarn("Invalid shop found in file, it will be removed on next file save.");
                    else {
                        this.shops.add(shop);
                        if (shop.getSign() != null) {
                            SignManager.generateShopSign(shop, shop.getSign(), true);
                            loadedSigns++;
                        } else {
                            FusionMarket.sendConsoleWarn("Shop '" + shop.getUniqueId() + "' does not have a sign!");
                        }
                    }
                }
            } else
                FusionMarket.sendConsoleInfo("  'Shops' data is missing from file, and will be skipped.");
            FusionMarket.sendConsoleInfo("  Loaded " + this.shops.size() + " shops with " + loadedSigns + " signs.");
        } else
            FusionMarket.sendConsoleInfo("  'Shops' data is missing from file, and will be skipped.");
        
        
        if (this.markets == null)
            this.markets = new ArrayList<>();
        if (this.shops == null)
            this.shops = new ArrayList<>();
        FusionMarket.sendConsoleInfo("Finished loading working data.");
    }
    
    /**
     * Saves all Market and Shop data to a YAML file.
     */
    public void saveDataFile()
    {
        FusionMarket.sendConsoleInfo("Saving Market and Shop data to file...");
        
        this.dataFile.set("Markets", this.markets);
        this.dataFile.set("Shops", this.shops);
        
        try {
            this.dataFile.save(this.dataFolderPath + DATA_FILE_NAME);
        } catch (IOException e) {
            e.printStackTrace();
            FusionMarket.sendConsoleWarn(" -!- DATA SAVE FAILED -!-");
        }
        
        FusionMarket.sendConsoleInfo("Finished saving!");
    }
    
    /**
     * Uses the data from the records file to populate purchase records in memory.
     */
    public void loadRecordsFile()
    {
        FusionMarket.sendConsoleInfo("Loading Purchase Records data from file...");
        
        this.records = new HashMap<>();
        for (String key : this.recordsFile.getKeys(false)) {
            if (this.recordsFile.get(key) != null) {
                ArrayList<PurchaseRecord> parsedPurchases = (ArrayList<PurchaseRecord>) this.recordsFile.get(key);
                
            
//                ArrayList<PurchaseRecord> userPurchases = new ArrayList<>(Arrays.asList((PurchaseRecord[]) this.recordsFile.get(key)));
                this.records.put(UUID.fromString(key), parsedPurchases);
            }
        }
        
        FusionMarket.sendConsoleInfo("Finished loading purchase data.");
    }
    
    /**
     * Saves all records data to a YAML file.
     */
    public void saveRecordsFile()
    {
        FusionMarket.sendConsoleInfo("Saving Purchase Records to file...");
        
        for (UUID uuid : this.records.keySet())
            this.recordsFile.set(uuid.toString(), this.records.get(uuid).toArray(new PurchaseRecord[0]));
    
        try {
            this.recordsFile.save(this.dataFolderPath + RECORDS_FILE_NAME);
        } catch (IOException e) {
            e.printStackTrace();
            FusionMarket.sendConsoleWarn(" -!- DATA SAVE FAILED -!-");
        }
    
        FusionMarket.sendConsoleInfo("Finished saving!");
    }
    
    /**
     * Finds any records for shops owned by the player, for sales made while the player was offline.
     * @param player     Shop owner
     * @param markAsRead True: Mark messages as read (they will not be shown again)
     *                   False: Do not mark messages as read (usually for admins/testing)
     * @return Any purchase records found (array could be empty); If player is not found, null is returned.
     */
    public PurchaseRecord[] getOfflineRecords(OfflinePlayer player, boolean markAsRead)
    {
        ArrayList<PurchaseRecord> output = new ArrayList<>();
        for (UUID key : this.records.keySet()) {
            for (PurchaseRecord purchase : this.records.get(key)) {
                if (purchase.getShopOwner().equals(player.getUniqueId()) && !purchase.isSeen()) {
                    output.add(purchase);
                    purchase.setSeen(markAsRead);
                }
            }
        }
        
        if (output.isEmpty())
            return null;
        if (markAsRead)
            this.saveRecordsFile();
        return output.toArray(new PurchaseRecord[0]);
    }
    
    public static Class<?> getNMSClass(String nmsClassString) throws ClassNotFoundException {
        String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + ".";
        String name = "net.minecraft.server." + version + nmsClassString;
        Class<?> nmsClass = Class.forName(name);

        return nmsClass;
    }
    public static Object getConnection(Player player) throws SecurityException, NoSuchMethodException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException, InvocationTargetException
    {
        Method getHandle = player.getClass().getMethod("getHandle");
        Object nmsPlayer = getHandle.invoke(player);
        Field conField = nmsPlayer.getClass().getField("playerConnection");
        Object con = conField.get(nmsPlayer);
        return con;
    }
    
    // Other ************************************************************************************************** Other //
    
    /**
     * Find a player by their username or UUID.
     * @param username - Partial or full username to be found, or UUID
     * @return A player matching the given search term, null if not found.
     */
    public static OfflinePlayer findPlayer(String username)
    {
        if (username.length() == 32 || username.length() == 36) {
            UUID uuid = UUID.fromString(username);
            return Bukkit.getPlayer(uuid);
        }
        
        for (Player onPlayer : getServer().getOnlinePlayers())
            if (onPlayer.getName().toLowerCase().contains(username.toLowerCase()))
                return onPlayer;
        for (OfflinePlayer offPlayer : getServer().getOfflinePlayers())
            if (offPlayer.getName().toLowerCase().contains(username.toLowerCase()))
                return offPlayer;
        return null;
    }
}