package io.github.lordfusion.fusionmarket.utilities;

import io.github.lordfusion.fusionmarket.FusionMarket;
import io.github.lordfusion.fusionmarket.Market;

import java.util.TimerTask;

public class EvictionTimer extends TimerTask
{
    private Market market;
    
    public EvictionTimer(Market market)
    {
        this.market = market;
    }
    
    /**
     * The action to be performed by this timer task.
     */
    @Override
    public void run()
    {
        FusionMarket.sendConsoleInfo("MARKET PLOT HAS EXPIRED: " + this.market.getUniqueId());
        this.market.resetOwnership();
    }
}
