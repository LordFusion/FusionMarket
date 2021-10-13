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
        sendConsoleInfo("I've come to make an announcement:");
        sendConsoleInfo("Shadow the Hedgehog's a bitch ass motherfucker. He pissed on my fucking wife. That's right, he took his hedgehog fucking quilly dick out and he pissed on my fucking wife and he said his dick was \"this big\" and I said \"that's disgusting\".");
        sendConsoleInfo("So I'm making a callout post on my twitter dot com: Shadow the Hedgehog, you got a small dick, its the size of this walnut except way smaller. And guess what? Here's what my dong looks like. That's right baby, all point, no quills, no pillows, look at that it looks like two balls and a bong.");
        sendConsoleInfo("He fucked my wife so guess what? I'm gonna fuck the Earth. That's right this is what you get, MY SUPER LASER PISS. Except I'm not pissing on the Earth, I'm gonna go higher, I'M PISSING ON THE MOON. HOW DO YOU LIKE THAT OBAMA, I PISSED ON THE MOON YOU IDIOT!");
        sendConsoleInfo("You have twenty-three hours before the piss drop-el-ets hit the fucking Earth, now get out of my fucking sight, before I piss on you too.");
        
        instance = this;
        
        // Load configs and saved info
        this.dataManager = new DataManager(this.getDataFolder().getAbsolutePath());
        
        // Register event listeners
        Bukkit.getPluginManager().registerEvents(new EventListener(this), this);
        
        // Seize the means of production -- If this throws a NullPointer then just throw the entire plugin out fam.
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
