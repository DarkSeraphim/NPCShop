package com.github.DarkSeraphim.NPCShop.Listener;

import com.earth2me.essentials.api.Economy;
import com.earth2me.essentials.api.UserDoesNotExistException;
import com.github.DarkSeraphim.NPCShop.NPCShop;
import com.github.DarkSeraphim.NPCShop.NPCs.NPC;
import com.github.DarkSeraphim.NPCShop.NPCs.NPCAction;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author DarkSeraphim
 */
public class InventoryListener implements Listener
{
    
    private NPCShop p;
    
    private Map<String, Integer> clickTracker = new HashMap<String, Integer>();
    
    private Map<String, Boolean> buying = new HashMap<String, Boolean>();
    
    public InventoryListener(NPCShop p)
    {
        this.p = p;
    }
    
    @EventHandler
    public void onClose(InventoryCloseEvent event)
    {
        if(event.getPlayer() instanceof Player == false) return;
        Player player = (Player) event.getPlayer();
        NPCAction action = p.getAction(player.getName());
        if(action == NPCAction.TRADING)
        {
            p.setAction(player.getName(), NPCAction.NONE);
            this.clickTracker.put(player.getName(), -1);
            if(this.buying.get(player.getName()) == Boolean.TRUE)
            {
                event.getView().setCursor(null);
            }
            this.buying.remove(player.getName());
        }
    }
    
    @EventHandler
    public void onClick(final InventoryClickEvent event)
    {
        if(event.getWhoClicked() instanceof Player == false) return;
        final Player player = (Player) event.getWhoClicked();
        if(p.getAction(player.getName()) != NPCAction.TRADING) return;
        int lastSlot = (this.clickTracker.get(player.getName()) != null ? this.clickTracker.get(player.getName()) : -1);
        int slot = event.getRawSlot();
        
        UUID uid = this.p.getInteractingWith(player.getName());
        
        if(uid == null) return;
        
        NPC npc = this.p.getManager().getNPC(uid);
        
        // Player inventory -> Player inventory
        if((slot > 26 || (slot < 0 && slot > -2)) && (lastSlot > 26 || (lastSlot < 0 && lastSlot > -2)) && !event.isShiftClick())
        {
            this.clickTracker.put(player.getName(), slot);
            return;
        }
        
        if((((slot > -1 && slot < 27) && (lastSlot > 26 || lastSlot < 0)) || (event.isShiftClick() && slot > 26))
              && (event.getCursor() != null && event.getCursor().getType() != Material.AIR && !event.isShiftClick()
                 || (event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR && event.isShiftClick())))
        {
            ItemStack selling;
            if(event.isShiftClick())
            {
                selling = event.getCurrentItem();
            }
            else
            {
                selling = event.getCursor().clone();
                if(event.isRightClick())
                {
                    selling.setAmount(1);
                }
            }
            if(npc.onSell(player, selling))
            {
                if(event.isShiftClick() && event.isLeftClick())
                {
                    event.setCurrentItem(null);
                }
                else
                {
                    ItemStack cursor = null;
                    if(event.isRightClick())
                    {
                        cursor = event.getCursor();
                        cursor.setAmount(cursor.getAmount() - selling.getAmount());
                        if(cursor.getAmount() < 1)
                        {
                            cursor = null;
                        }
                    } 
                    event.setCursor(cursor);
                }
                this.clickTracker.put(player.getName(), -1);
            }
            else
            {
            }
            this.buying.put(player.getName(), Boolean.TRUE);
            // Sell, possibly
        }
        
        else if((slot > 26 || slot < -1) && (lastSlot > -1 && lastSlot < 27))
        {
            
            if(npc.onBuy(player, event.getCursor()))
            {
                this.clickTracker.put(player.getName(), slot);
                this.buying.put(player.getName(), Boolean.FALSE);
                return;
            }
            else
            {
                this.buying.put(player.getName(), Boolean.TRUE);
            }
            // Buy, possibly
        }
         
        else if((slot > -1 && slot < 27) && (lastSlot < 27))
        {
            ItemStack cursor = event.getCursor();
            ItemStack item = npc.getItem(slot);
            if(cursor == null || cursor.getType() == Material.AIR)
            {
                event.setCursor(item);
            }
            else if(!cursor.isSimilar(item))
            {
                event.setCursor(item);
            }
            else
            {
                if(event.isLeftClick())
                {
                    if(event.isShiftClick())
                    {
                        cursor.setAmount(cursor.getMaxStackSize());
                    }
                    else
                    {
                        cursor.setAmount(cursor.getAmount() + 1);
                        if(cursor.getAmount() > cursor.getMaxStackSize())
                        {
                            cursor.setAmount(cursor.getMaxStackSize());
                        }
                    }
                }
                else
                {
                    int amount = cursor.getAmount() - 1;
                    if(amount < 0) amount = 0;
                    cursor.setAmount(amount);
                }
                event.setCursor(cursor);
            }
            // Another click in the merchant inv. Switch or stack
            this.clickTracker.put(player.getName(), (event.getCursor() == null || event.getCursor().getType() == Material.AIR) ? -1 : slot);
            this.buying.put(player.getName(), Boolean.TRUE);
        }
                
        event.setCancelled(true);
        event.setResult(Event.Result.DENY);
        
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                player.updateInventory();
                if(event.getCursor() == null || event.getCursor().getType() == Material.AIR)
                {
                    clickTracker.put(player.getName(), -1);
                }
            }
        }.runTaskLater(p, 1L);
    }

}
