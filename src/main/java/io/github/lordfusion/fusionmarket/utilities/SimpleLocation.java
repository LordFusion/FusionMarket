package io.github.lordfusion.fusionmarket.utilities;

import com.sun.istack.internal.NotNull;
import io.github.lordfusion.fusionmarket.FusionMarket;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

import java.util.HashMap;
import java.util.Map;

/**
 * A location that stores data only about the world and x, y, z position.
 * This allows me to set and get a location directly from the Yaml file, using ~1/4 of the file size.
 */
@SerializableAs("SimpleLocation")
public class SimpleLocation implements ConfigurationSerializable
{
    private String worldName;
    private int x,y,z;
    
    public SimpleLocation() {}
    
    public SimpleLocation(Location location)
    {
        this.worldName = location.getWorld().getName();
        this.x = location.getBlockX();
        this.y = location.getBlockY();
        this.z = location.getBlockZ();
    }
    
    public Location asLocation()
    {
        World world = Bukkit.getWorld(this.worldName);
        if (world == null)
            return null;
        return new Location(world, x, y, z);
    }
    
    public String getWorldName()
    {
        return worldName;
    }
    
    public void setWorldName(String worldName)
    {
        this.worldName = worldName;
    }
    
    public int getX()
    {
        return x;
    }
    
    public void setX(int x)
    {
        this.x = x;
    }
    
    public int getY()
    {
        return y;
    }
    
    public void setY(int y)
    {
        this.y = y;
    }
    
    public int getZ()
    {
        return z;
    }
    
    public void setZ(int z)
    {
        this.z = z;
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
        
        output.put("World", this.worldName);
        output.put("x", this.x);
        output.put("y", this.y);
        output.put("z", this.z);
        
        return output;
    }
    
    public static SimpleLocation deserialize(Map<String, Object> input)
    {
        SimpleLocation output = new SimpleLocation();
        if (!input.containsKey("World") || !input.containsKey("x") || !input.containsKey("y") || !input.containsKey("z"))
        {
            FusionMarket.sendConsoleWarn("Attempted to deserialize invalid sign: " + input.toString());
            return null;
        }
        
        output.setWorldName((String)input.get("World"));
        output.setX((int)input.get("x"));
        output.setY((int)input.get("y"));
        output.setZ((int)input.get("z"));
        
        return output;
    }
    
    @Override
    public String toString()
    {
        return '(' + this.worldName + ';' + this.x + ',' + this.y + ',' + this.z + ')';
    }
    
    /**
     * Lets you print out a generic Location as if it were one of mine.
     * Because frankly, the default toString of Location is complete fucking garbage.
     * @param loc Location
     * @return String in the style of (world,x,y,z)
     */
    public static String prettify(@NotNull Location loc)
    {
        return '(' + loc.getWorld().getName() + ';' + loc.getX() + ',' + loc.getBlockY() + ',' + loc.getZ() + ')';
    }
    
    public boolean equals(@NotNull SimpleLocation loc)
    {
        return this.getWorldName().equalsIgnoreCase(loc.getWorldName()) &&
                this.getX() == loc.getX() &&
                this.getY() == loc.getY() &&
                this.getZ() == loc.getZ();
    }
    
    public boolean equals(@NotNull Location loc)
    {
        return this.getWorldName().equalsIgnoreCase(loc.getWorld().getName()) &&
                this.getX() == loc.getX() &&
                this.getY() == loc.getY() &&
                this.getZ() == loc.getZ();
    }
}
