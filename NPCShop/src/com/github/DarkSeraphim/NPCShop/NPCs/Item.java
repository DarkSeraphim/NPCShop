package com.github.DarkSeraphim.NPCShop.NPCs;

import org.bukkit.inventory.ItemStack;

/**
 *
 * @author DarkSeraphim
 */
public class Item 
{
        
    private double buy;
    
    private double sell;
    
    private boolean sellable;
    
    protected Item(double buy, double sell, boolean sellable)
    {
        this.buy = buy;
        this.sell = sell;
        this.sellable = sellable;
    }
    
    public double getBuyPrice()
    {
        return this.buy;
    }
    
    public double getSellPrice()
    {
        return this.sell;
    }
    
    public boolean isSellable()
    {
        return this.sellable;
    }

}
