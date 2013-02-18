package com.github.DarkSeraphim.NPCShop.NPCs;

import com.earth2me.essentials.api.Economy;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;


/**
 *
 * @author DarkSeraphim
 */
public class NPC 
{
    
    private EntityType shopkeeper;
        
    private String name;
    
    private boolean usePerm;
    
    private List<String> allowMsgs;
    
    private List<String> denyMsgs;
    
    private ItemStack[] wares;
    
    private ItemStack[] display;
    
    // Prices array for fast reference
    private Item[] items;
    
    private Logger log;
    
    private final ItemMeta emptyMeta;
    
    private ItemStack[] equipment;
    
    private Random rand;
    
    public NPC(EntityType shopkeeperType, String name, boolean usePerm, List<String> allowMsgs, List<String> denyMsgs, Logger log)
    {
        this.shopkeeper = shopkeeperType;
        this.name = name;
        this.usePerm = usePerm;
        this.allowMsgs = allowMsgs;
        this.denyMsgs = denyMsgs;
        this.wares = new ItemStack[27];
        this.display = new ItemStack[27];
        this.items = new Item[27];
        this.log = log;
        this.emptyMeta = new ItemStack(Material.AIR).getItemMeta();
        this.equipment = new ItemStack[5];
        this.rand = new Random();
    }
    
    public EntityType getShopKeeperType()
    {
        return this.shopkeeper;
    }
    
    public String getName()
    {
        return this.name;
    }
    
    public boolean playerIsAllowedToInteract(Player player)
    {
        if(!this.usePerm) return true;
        return player.hasPermission("npc.use.*") || player.hasPermission("npc.use."+getName());
    }
    
    public String getAllowMessage()
    {
        int index = rand.nextInt(this.allowMsgs.size());
        return ChatColor.translateAlternateColorCodes('&', this.allowMsgs.get(index).replace("%n", getName()+ChatColor.RESET));
    }
    
    public String getDenyMessage()
    {
        int index = rand.nextInt(this.denyMsgs.size());
        return ChatColor.translateAlternateColorCodes('&', this.denyMsgs.get(index).replace("%n", getName()));
    }
            
    public void setItem(int index, ItemStack item, String currency, double buy, double sell, boolean sellable) throws IllegalArgumentException
    {
        if(index < 0 || index > 26)
        {
            throw new IllegalArgumentException("index should be between and including 0 and 26");
        }
        this.wares[index] = item;
        this.items[index] = new Item(buy, sell, sellable);
        ItemStack it = new ItemStack(item);
        ItemMeta meta = it.getItemMeta();
        List<String> lore = meta.getLore();
        if(lore == null) lore = new ArrayList<String>();
        lore.add("");
        if(this.items[index].getBuyPrice() > 0)
        lore.add(ChatColor.RESET.toString()+ChatColor.GRAY+"Buy: "+currency+this.items[index].getBuyPrice());
        if(this.items[index].isSellable())
        lore.add(ChatColor.RESET.toString()+ChatColor.GRAY+"Sell: "+currency+this.items[index].getSellPrice());
        meta.setLore(lore);
        it.setItemMeta(meta);
        this.display[index] = it;    
    }
    
    public void setEquipment(ItemStack[] equip)
    {
        for(int i = 0; i < equip.length; i++)
        {
            this.equipment[i] = equip[i];
        }
    }
    
    public ItemStack[] getEquipment()
    {
        return this.equipment;
    }
    
    public void setHelmet(ItemStack i)
    {
        this.equipment[0] = i;
    }
    
    public ItemStack getHelmet()
    {
        return this.equipment[0];
    }
    
    public void setChestplate(ItemStack i)
    {
        this.equipment[1] = i;
    }
    
    public ItemStack getChestplate()
    {
        return this.equipment[1];
    }
    
    public void setLeggings(ItemStack i)
    {
        this.equipment[2] = i;
    }
    
    public ItemStack getLeggings()
    {
        return this.equipment[2];
    }
    
    public void setBoots(ItemStack i)
    {
        this.equipment[3] = i;
    }
    
    public ItemStack getBoots()
    {
        return this.equipment[3];
    }
    
    public void setHand(ItemStack i)
    {
        this.equipment[4] = i;
    }
    
    public ItemStack getHand()
    {
        return this.equipment[4];
    }
    
    public Inventory getInventory(Player holder)
    {
        Inventory inv = Bukkit.createInventory(holder, 27, this.name);
        inv.setContents(this.display);
        return inv;
    }
    
    public ItemStack getItem(int index)
    {
        log.info(this.wares[index] != null ? this.wares[index].toString() : "null");
        return this.wares[index];
    }
    
    public boolean onBuy(Player player, ItemStack buying)
    {
        if(buying == null || buying.getType() == Material.AIR) return false;
        Item item;
        for(int i = 0; i < this.wares.length; i++)
        {
            if(this.wares[i] == null) continue;        
            if(buying.isSimilar(this.wares[i]))
            {
                item = this.items[i];
                if(item == null) continue;
                if(item.getBuyPrice() < 0) continue;
                double price = item.getBuyPrice()*buying.getAmount();
                try
                {
                    if(Economy.hasEnough(player.getName(), price))
                    {
                        Economy.subtract(player.getName(), price);
                        player.playSound(player.getLocation(), Sound.ORB_PICKUP, 10f, 63f);
                        String name = buying.getItemMeta().getDisplayName();
                        name = (name != null ? name : buying.getType().name().replace('_', ' ').toLowerCase());
                        name = name.substring(0, 1).toUpperCase()+name.substring(1);
                        player.sendMessage(ChatColor.GREEN+"You have purchased "+buying.getAmount()+" of "+name);
                        return true;
                    }
                    
                }
                catch(com.earth2me.essentials.api.UserDoesNotExistException ex)
                {
                    log.log(Level.INFO, "{0} does not exist. Is this an Essentials bug?", player.getName());
                }
                catch(com.earth2me.essentials.api.NoLoanPermittedException ex)
                {
                    log.log(Level.INFO, "{0} tried to loan money, while this was not allowed", player.getName());
                }
            }
        }
        player.playSound(player.getLocation(), Sound.NOTE_BASS_DRUM, 10f, 63f);
        player.sendMessage(ChatColor.RED+"Not enough money");
        return false;
    }
    
    public boolean onSell(Player player, ItemStack selling)
    {
        if(selling == null || selling.getType() == Material.AIR) return false;
        ItemStack ware;
        Item item;
        for(int i = 0; i < this.wares.length; i++)
        {
            if(this.wares[i] == null) continue;
            ware = this.wares[i].clone();
            //ware.setItemMeta(this.emptyMeta);
            if(selling.isSimilar(ware))
            {
                item = this.items[i];
                if(item == null) continue;
                if(!item.isSellable()) continue;
                double price = item.getSellPrice()*selling.getAmount();
                try
                {
                    Economy.add(player.getName(), price);
                    player.playSound(player.getLocation(), Sound.ORB_PICKUP, 10f, 63f);
                    String name = selling.getItemMeta().getDisplayName();
                    name = (name != null ? name : selling.getType().name().replace('_', ' ').toLowerCase());
                    name = name.substring(0, 1).toUpperCase()+name.substring(1);
                    player.sendMessage(ChatColor.GREEN+"You have sold "+name);
                    return true;
                }
                catch(com.earth2me.essentials.api.UserDoesNotExistException ex)
                {
                    log.log(Level.INFO, "{0} does not exist. Is this an Essentials bug?", player.getName());
                }
                catch(com.earth2me.essentials.api.NoLoanPermittedException ex)
                {
                    log.log(Level.INFO, "{0} tried to loan money, while this was not allowed", player.getName());
                }
            }
        }
        player.playSound(player.getLocation(), Sound.NOTE_BASS_DRUM, 10f, 63f);
        player.sendMessage(ChatColor.RED+"You cannot sell this here");
        return false;
    }

}
