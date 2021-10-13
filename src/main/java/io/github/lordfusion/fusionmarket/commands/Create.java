package io.github.lordfusion.fusionmarket.commands;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import io.github.lordfusion.fusionmarket.DataManager;
import io.github.lordfusion.fusionmarket.FusionMarket;
import io.github.lordfusion.fusionmarket.Market;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class Create
{
    /**
     * /mkt create <name> <region> <world>
     * @param sender Command sender
     * @param args   Command arguments
     */
    public static void run(CommandSender sender, String[] args)
    {
        ProtectedRegion worldGuardRegion = null;
        String marketName = null;
        World world = null;
        
        // Argument Parsing
        if (args.length == 3) { // /mkt create <name> <region> <world>
            if (FusionMarket.getInstance().getDataManager().getMarket(args[0]) != null) {
                BaseCommand.argumentsReject(sender, "Market '" + args[0] + "' already exists!", Help.CMDHELP_CREATE_BASIC);
                return;
            }
            marketName = args[0];
            world = Bukkit.getWorld(args[2]);
            if (world == null) {
                BaseCommand.argumentsReject(sender, "Invalid world.", Help.CMDHELP_CREATE_BASIC);
                return;
            }

            worldGuardRegion = DataManager.findRegion(world, args[1]);
            
            if (worldGuardRegion == null) {
                BaseCommand.argumentsReject(sender, "Invalid region.", Help.CMDHELP_CREATE_BASIC);
                return;
            }
        } else if (args.length == 2) { // /mkt create <name> <region>
            if (FusionMarket.getInstance().getDataManager().getMarket(args[0]) != null) {
                BaseCommand.argumentsReject(sender, "Market '" + args[0] + "' already exists!", Help.CMDHELP_CREATE_BASIC);
                return;
            }
            marketName = args[0];
            worldGuardRegion = DataManager.findRegion(((Player)sender).getWorld(), args[1]);
            if (worldGuardRegion == null) {
                BaseCommand.argumentsReject(sender, "Invalid region.", Help.CMDHELP_CREATE_BASIC);
                return;
            }
            world = ((Player)sender).getWorld();
        } else if (args.length == 1) { // /mkt create <name>
            worldGuardRegion = DataManager.findRegion(((Player)sender).getWorld(), args[0]);
            if (worldGuardRegion == null) {
                if (FusionMarket.getInstance().getDataManager().getMarket(args[0]) != null) {
                    BaseCommand.argumentsReject(sender, "Market '" + args[0] + "' already exists!", Help.CMDHELP_CREATE_BASIC);
                    return;
                }
                marketName = args[0];
                Player player = ((Player)sender).getPlayer();
                if (player == null) {// Not possible but make IntelliJ stop yelling at me
                    BaseCommand.commandFailure(sender, "Missing Player");
                    return;
                }
                
                BukkitWorld selectionWorld = DataManager.getWorldEditWorld(player.getWorld());
                // Find their WorldEdit selection
                Region selection = null;
                try {
                    selection = WorldEdit.getInstance().getSessionManager().findByName(player.getName()).getSelection(selectionWorld);
                } catch (IncompleteRegionException e) {
                    e.printStackTrace();
                }
                if (selection == null) {
                    BaseCommand.commandFailure(sender, "No region selected.");
                    return;
                }
                // Create the new WorldGuard region
                worldGuardRegion = new ProtectedCuboidRegion(marketName, selection.getMinimumPoint(),
                        selection.getMaximumPoint());
                DataManager.getRegionManager(player.getWorld()).addRegion(worldGuardRegion);
                FusionMarket.sendConsoleInfo("Created region for new Market: " + marketName + " @ " + worldGuardRegion.getId());
                world = player.getWorld();
            } else {
                marketName = worldGuardRegion.getId();
                world = ((Player)sender).getWorld();
            }
        } else if (args.length == 0) { // /mkt create
            marketName = FusionMarket.getInstance().getDataManager().generateMarketName();
            
            Player player = ((Player)sender).getPlayer();
            
            if (player == null) {// Not possible but make IntelliJ stop yelling at me
                BaseCommand.commandFailure(sender, "Missing Player");
                return;
            }
            BukkitWorld selectionWorld = DataManager.getWorldEditWorld(player.getWorld());
            // Find their WorldEdit selection
            Region selection = null;
            try {
                selection = WorldEdit.getInstance().getSessionManager().findByName(player.getName()).getSelection(selectionWorld);
            } catch (IncompleteRegionException e) {
                e.printStackTrace();
            }
            if (selection == null) {
                BaseCommand.commandFailure(sender, "No region selected.");
                return;
            }
            // Create the new WorldGuard region
            worldGuardRegion = new ProtectedCuboidRegion(marketName, selection.getMinimumPoint(),
                    selection.getMaximumPoint());
            
            DataManager.getRegionManager(player.getWorld()).addRegion(worldGuardRegion);
            FusionMarket.sendConsoleInfo("Created region for new Market: " + marketName + " @ " + worldGuardRegion.getId());
            world = player.getWorld();
        }
        
        if (worldGuardRegion == null) {
            BaseCommand.commandFailure(sender, "Null region.");
            return;
        } else if (marketName == null) {
            BaseCommand.commandFailure(sender, "Null name.");
            return;
        } else if (world == null) {
            BaseCommand.commandFailure(sender, "Null world.");
            return;
        }
        
        Market market = FusionMarket.getInstance().getDataManager().createNewMarket(world, worldGuardRegion,
                marketName);
        if (market == null) {
            BaseCommand.commandFailure(sender, "Failed to create Market.");
        } else {
            if (sender instanceof Player) {
                TextComponent successMsg = new TextComponent("Market created!");
                successMsg.setColor(ChatColor.GREEN);
                ((Player)sender).spigot().sendMessage(successMsg);
            } else {
                sender.sendMessage("Market created!");
            }
        }
    }
}
