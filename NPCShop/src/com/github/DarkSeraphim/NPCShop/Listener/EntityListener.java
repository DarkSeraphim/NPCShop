package com.github.DarkSeraphim.NPCShop.Listener;

import com.github.DarkSeraphim.NPCShop.NPCShop;
import java.util.UUID;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.CreeperPowerEvent;
import org.bukkit.event.entity.EntityBreakDoorEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.entity.PigZapEvent;
import org.bukkit.event.entity.SheepDyeWoolEvent;
import org.bukkit.event.entity.SlimeSplitEvent;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author DarkSeraphim
 */
public class EntityListener implements Listener
{

    NPCShop p;
    
    public EntityListener(NPCShop p)
    {
        this.p = p;
    }
    
    @EventHandler
    public void onDamage(EntityDamageEvent event)
    {
        UUID uid = event.getEntity().getUniqueId();
        if(p.getManager().isNPC(uid))
        {
            event.setCancelled(true);
            event.setDamage(0);
        }
    }
    
    @EventHandler
    public void onSpawn(CreatureSpawnEvent event)
    {
        SpawnReason reason = event.getSpawnReason();
        String name = event.getLocation().getWorld().getName();
        if(reason == SpawnReason.CUSTOM && name.equals(p.getWorld()))
        {
            new NPCValidator(event.getEntity()).runTaskLater(p, 1L);
        }
        else
        {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onBreakDoor(EntityBreakDoorEvent event)
    {
        if(p.getManager().isNPC(event.getEntity().getUniqueId()))
        {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onChangeBlock(EntityChangeBlockEvent event)
    {
        if(p.getManager().isNPC(event.getEntity().getUniqueId()))
        {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onCombust(EntityCombustEvent event)
    {
        if(p.getManager().isNPC(event.getEntity().getUniqueId()))
        {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onExplode(EntityExplodeEvent event)
    {
        if(p.getManager().isNPC(event.getEntity().getUniqueId()))
        {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onPortalTP(EntityPortalEvent event)
    {
        if(p.getManager().isNPC(event.getEntity().getUniqueId()))
        {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onShootBow(EntityShootBowEvent event)
    {
        if(p.getManager().isNPC(event.getEntity().getUniqueId()))
        {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onPigZap(PigZapEvent event)
    {
        
    }
    
    @EventHandler
    public void onDye(SheepDyeWoolEvent event)
    {
        if(p.getManager().isNPC(event.getEntity().getUniqueId()))
        {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onSlimeSplit(SlimeSplitEvent event)
    {
        if(p.getManager().isNPC(event.getEntity().getUniqueId()))
        {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onCreeperPowering(CreeperPowerEvent event)
    {
        if(p.getManager().isNPC(event.getEntity().getUniqueId()))
        {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onTeleport(EntityTeleportEvent event)
    {
        if(p.getManager().isNPC(event.getEntity().getUniqueId()))
        {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onTame(EntityTameEvent event)
    {
        if(p.getManager().isNPC(event.getEntity().getUniqueId()))
        {
            event.setCancelled(true);
        }
    }
    
    private class NPCValidator extends BukkitRunnable
    {
        private Entity e;
        
        protected NPCValidator(Entity e)
        {
            this.e = e;
        }
        public void run()
        {
            if(!p.getManager().isNPC(e.getUniqueId()))
            {
                e.remove();
            }
        }
    }
}
