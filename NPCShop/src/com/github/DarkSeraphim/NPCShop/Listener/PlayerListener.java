package com.github.DarkSeraphim.NPCShop.Listener;

import com.github.DarkSeraphim.NPCShop.NPCShop;
import com.github.DarkSeraphim.NPCShop.NPCs.NPC;
import com.github.DarkSeraphim.NPCShop.NPCs.NPCAction;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author DarkSeraphim
 */
public class PlayerListener implements Listener
{
    NPCShop p; 
    
    public PlayerListener(NPCShop p)
    {
        this.p = p;
    }
    
    @EventHandler
    public void onInteract(final PlayerInteractEntityEvent event)
    {
        final Entity e = event.getRightClicked();
        final Player player = event.getPlayer();
        if(p.getManager().isNPC(e.getUniqueId()))
        {
            final NPC npc = p.getManager().getNPC(e.getUniqueId());
            event.setCancelled(true);
            NPCAction action = p.getAction(player.getName());
            if(action == NPCAction.NONE && npc.playerIsAllowedToInteract(player))
            {
                player.sendMessage(npc.getAllowMessage().replace("%p", player.getName()+ChatColor.RESET));
                new BukkitRunnable()
                {
                    @Override
                    public void run()
                    {
                        if(player == null) return;
                        Inventory inv = npc.getInventory(player);
                        player.openInventory(inv);
                        p.setAction(event.getPlayer().getName(), NPCAction.TRADING);
                        p.setInteractingWith(event.getPlayer().getName(), e.getUniqueId());
                        player.playSound(player.getLocation(), Sound.CHICKEN_EGG_POP, 10f, 63f);
                    }
                }.runTaskLater(p, 5L);
               
            }
            else if(action == NPCAction.NONE)
            {
                event.getPlayer().sendMessage(npc.getDenyMessage().replace("%p", event.getPlayer().getName()));
            }
            else if(action == NPCAction.DELETE)
            {
                if(p.getManager().removeNPC(e.getUniqueId()))
                {
                    event.getPlayer().sendMessage("Removed NPC");
                    e.remove();
                }
                else
                {
                    event.getPlayer().sendMessage("Failed to remove NPC");
                }
                this.p.setAction(event.getPlayer().getName(), NPCAction.NONE);
            }
        }
    }
}
