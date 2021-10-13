package io.github.lordfusion.fusionmarket;

import io.github.lordfusion.fusionmarket.commands.BaseCommand;
import io.github.lordfusion.fusionmarket.utilities.PurchaseRecord;
import io.github.lordfusion.fusionmarket.utilities.SimpleLocation;
import net.md_5.bungee.api.chat.TextComponent;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public final class FusionMarket extends JavaPlugin
{
    static final String consolePrefix = "[Fusion Market] ";
    
    private DataManager dataManager;
    private static FusionMarket instance;
    private Economy vaultEco;
    
    public static final DateFormat DATE_FORMAT = new SimpleDateFormat("MMMdd HH:mm");
    
    static {
        ConfigurationSerialization.registerClass(Market.class, "Market");
        ConfigurationSerialization.registerClass(Shop.class, "Shop");
        ConfigurationSerialization.registerClass(SimpleLocation.class, "SimpleLocation");
        ConfigurationSerialization.registerClass(PurchaseRecord.class, "PurchaseRecord");
    }
    
    @Override
    public void onEnable()
    {
        sendConsoleInfo("Customers, remember to pay your taxes! Shop owners, remember to hide from the IRS!");
        
        instance = this;
        
        // Load configs and saved info
        this.dataManager = new DataManager(this.getDataFolder().getAbsolutePath());
        
        // Register event listeners
        Bukkit.getPluginManager().registerEvents(new EventListener(this), this);
        
        // Seize the means of production -- If this throws a NullPointer then just throw the entire plugin out fam.
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            sendConsoleWarn("Warning! Missing plugin: Vault");
        }
        this.vaultEco = Bukkit.getServer().getServicesManager().getRegistration(Economy.class).getProvider();
        
        // Get that shit-tier command garbage outta my main class
        getCommand("fusionmarket").setExecutor(new BaseCommand());
    }
    
    @Override
    public void onDisable()
    {
        this.dataManager.saveDataFile();
        this.dataManager.saveRecordsFile();
    }
    
    /**
     * Sends a message to the server console, with the Info priority level.
     * @param message Message for console
     */
    public static void sendConsoleInfo(String message)
    {
        Bukkit.getServer().getLogger().info(consolePrefix + message);
    }
    
    /**
     * Sends a message to the server console, with the Warning priority level.
     * @param message Message for console
     */
    public static void sendConsoleWarn(String message)
    {
        Bukkit.getServer().getLogger().warning(consolePrefix + message);
    }
    
    public static void sendUserMessage(CommandSender sender, TextComponent msg)
    {
        if (sender instanceof Player)
            ((Player)sender).spigot().sendMessage(msg);
        else
            sender.sendMessage(msg.getText());
    }
    
    public static void sendUserMessages(CommandSender sender, TextComponent[] msgs)
    {
        for (TextComponent msg : msgs)
            sendUserMessage(sender, msg);
    }
    
    public static FusionMarket getInstance()
    {
        return instance;
    }
    
    public DataManager getDataManager()
    {
        return this.dataManager;
    }
    
    public Economy getEconomy()
    {
        return this.vaultEco;
    }
    
}
