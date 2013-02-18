package com.github.DarkSeraphim.NPCShop;

import com.github.DarkSeraphim.NPCShop.Listener.*;
import com.github.DarkSeraphim.NPCShop.NPCs.NPC;
import com.github.DarkSeraphim.NPCShop.NPCs.NPCAction;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author DarkSeraphim
 */
public class NPCShop extends JavaPlugin
{
    
    private NPCManager manager;
    
    private String currency;
    
    private Map<String, NPCAction> playerActions;
    
    private Map<String, UUID> interactingWith;

    @Override
    public void onEnable()
    {
        if(!new File(getDataFolder(), "config.yml").exists())
        {
            saveDefaultConfig();
        }
        
        this.currency = getConfig().getString("currency", "$");
        
        File npcFolder = new File(getDataFolder(), File.separator + "npcs" + File.separator);
        
        if(!npcFolder.exists())
        {
            createDefault(npcFolder);
        }
        
        PluginManager pm = Bukkit.getPluginManager();
        
        if(Bukkit.getWorld(getWorld()) == null)
        {
            pm.disablePlugin(this);
            return;
        }
        
        Map<String, NPC> npcs = loadNPCs(npcFolder);
        
        this.manager = new NPCManager(this, npcs);
        this.playerActions = new HashMap<String, NPCAction>();
        this.interactingWith = new HashMap<String, UUID>();
        
        pm.registerEvents(new EntityListener(this), this);
        pm.registerEvents(new InventoryListener(this), this);
        pm.registerEvents(new PlayerListener(this), this);
    }
    
    @Override
    public void onDisable()
    {
        if(getManager() != null)
        {
            getManager().save();
        }
    }
    
    public NPCManager getManager()
    {
        return this.manager;
    }
    
    public String getCurrency()
    {
        return this.currency;
    }
    
    public String getWorld()
    {
        return getConfig().getString("world", "world");
    }
    
    private Map<String, NPC> loadNPCs(File dir)
    {
        Map<String, NPC> types = new HashMap<String, NPC>();
        for(File f : dir.listFiles(new YamlFilter()))
        {
            String name = ChatColor.translateAlternateColorCodes('&', f.getName().substring(0, f.getName().length() - 4));
            NPC npc = loadNPC(f);
            if(npc != null && name != null && !name.isEmpty())
            {
                types.put(ChatColor.stripColor(name), npc);
            }
        }
        return types;
    }
    
    private NPC loadNPC(File npcFile)
    {
        NPC npc;
        YamlConfiguration npcData = YamlConfiguration.loadConfiguration(npcFile);
        String etype = npcData.getString("entity", "villager").toUpperCase().replace(' ', '_');
        boolean usePerm = npcData.getBoolean("use-permission", true);
        List<String> a = npcData.getStringList("allow-messages");
        if(a.isEmpty())
        {
            a.add("<[NPC] %n> Yes %p, what can I help you with (^_^)");
        }
        List<String> d = npcData.getStringList("deny-messages");
        if(d.isEmpty())
        {
            d.add("<[NPC] %n> Sorry %p, I am a bit busy (-_-)");
        }
        EntityType type;
        if(etype.equalsIgnoreCase("player"))
        {
            type = EntityType.PLAYER;
        }
        else type = EntityType.fromName(etype);
        if(type == null)
        {
            getLogger().log(Level.WARNING, "Failed to find EntityType from {0}", npcFile.getName());
            return null;
        }
        String fileName = npcFile.getName();
        npc = new NPC(type, ChatColor.translateAlternateColorCodes('&', fileName.substring(0, fileName.length() - 4)), usePerm, a, d, getLogger());
        
        
        int helmetID = npcData.getInt("head", -1);
        int chestID = npcData.getInt("chest", -1);
        int leggingsID = npcData.getInt("leggings", -1);
        int bootsID = npcData.getInt("boots", -1);
        int handID = npcData.getInt("hand", -1);
        
        
        
        ItemStack helmet = helmetID > -1 ? new ItemStack(helmetID) : null;
        if(helmetID == Material.LEATHER_HELMET.getId())
        {
            try
            {
                String[] data = npcData.getString("head-data", "").split("\\|");
                LeatherArmorMeta meta = (LeatherArmorMeta) helmet.getItemMeta();
                meta.setColor(Color.fromRGB(Integer.parseInt(data[0]), Integer.parseInt(data[1]), Integer.parseInt(data[2])));
                helmet.setItemMeta(meta);
            }
            catch(Exception ex)
            {
                // Swallow the exception, ignore the colour
            }
        }
        ItemStack chest = chestID > -1 ? new ItemStack(chestID) : null;
        if(chestID == Material.LEATHER_CHESTPLATE.getId())
        {
            try
            {
                String[] data = npcData.getString("chest-data", "").split("\\|");
                LeatherArmorMeta meta = (LeatherArmorMeta) chest.getItemMeta();
                meta.setColor(Color.fromRGB(Integer.parseInt(data[0]), Integer.parseInt(data[1]), Integer.parseInt(data[2])));
                chest.setItemMeta(meta);
            }
            catch(Exception ex)
            {
                // Swallow the exception, ignore the colour
            }
        }
        ItemStack leggings = leggingsID > -1 ? new ItemStack(leggingsID) : null;
        if(leggingsID == Material.LEATHER_LEGGINGS.getId())
        {
            try
            {
                String[] data = npcData.getString("leggings-data", "").split("\\|");
                LeatherArmorMeta meta = (LeatherArmorMeta) leggings.getItemMeta();
                meta.setColor(Color.fromRGB(Integer.parseInt(data[0]), Integer.parseInt(data[1]), Integer.parseInt(data[2])));
                leggings.setItemMeta(meta);
                
            }
            catch(Exception ex)
            {
                // Swallow the exception, ignore the colour
            }
        }
        ItemStack boots = bootsID > -1 ? new ItemStack(bootsID) : null;
        if(bootsID == Material.LEATHER_BOOTS.getId())
        {
            try
            {
                String[] data = npcData.getString("boots-data", "").split("\\|");
                LeatherArmorMeta meta = (LeatherArmorMeta) boots.getItemMeta();
                meta.setColor(Color.fromRGB(Integer.parseInt(data[0]), Integer.parseInt(data[1]), Integer.parseInt(data[2])));
                boots.setItemMeta(meta);   
            }
            catch(Exception ex)
            {
                // Swallow the exception, ignore the colour
            }
        }
        ItemStack hand = handID > -1 ? new ItemStack(handID) : null;
        
        ItemStack[] equipment = new ItemStack[]{helmet, chest, leggings, boots, hand};
        
        npc.setEquipment(equipment);
        
        List<ItemStack> items = new ArrayList<ItemStack>();
        ConfigurationSection itemSection = npcData.getConfigurationSection("items");
        if(itemSection == null)
        {
            npcData.createSection("items");
        }
        for(String slot : itemSection.getKeys(false))
        {
            int index;
            try
            {
                index = Integer.parseInt(slot);
            }
            catch(NumberFormatException ex)
            {
                getLogger().log(Level.WARNING, "Invalid slot in {0}", fileName);
                continue;
            }
            
            int id = itemSection.getInt(slot+".id", -1);
            int data = itemSection.getInt(slot+".data", -1);
            if(id < 0 || data < 0)
            {
                continue;
            }
            
            String name = itemSection.getString(slot+".name");
            List<String> lore = itemSection.getStringList(slot+".lore");
            double buy = itemSection.getDouble(slot+".buy", -1);
            double sell = itemSection.getDouble(slot+".sell", -1);
            boolean sellable = itemSection.getBoolean(slot+".sellable", false);
            if(buy < 0 && (sell < 0 || !sellable))
            {
                continue;
            }
            
            ItemStack item = new ItemStack(id, 1);
            if(item.getType().getMaxDurability() > 0)
            {
                MaterialData Mdata = item.getData();
                Mdata.setData((byte)data);
                item.setData(Mdata);
            }
            else
            {
                item.setDurability((short)data);
            }
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(name != null ? ChatColor.RESET+ChatColor.translateAlternateColorCodes('&', name) : null);
            meta.setLore(lore);
            item.setItemMeta(meta);
            items.add(item);
            try
            {
                npc.setItem(index, item, this.getCurrency(), buy, sell, sellable);
            }
            catch(IllegalArgumentException ex)
            {
                getLogger().log(Level.WARNING, "Invalid slot: please use an index between and including 0 to 26");
            }
        }
        if(items.isEmpty())
        {
            return null;
        }
        
        return npc;
    }
    
    public NPCAction getAction(String player)
    {
        NPCAction a = this.playerActions.get(player);
        return a == null ? NPCAction.NONE : a;
    }
    
    public void setAction(String player, NPCAction action)
    {
        this.playerActions.put(player, action);
    }
    
    public UUID getInteractingWith(String player)
    {
        return this.interactingWith.get(player);    
    }
    
    public void setInteractingWith(String player, UUID uid)
    {
        this.interactingWith.put(player, uid);
    }
    
    /*****************************\
     *        - Commands -       *
    \*****************************/
    
    @Override
    public boolean onCommand(final CommandSender sender, Command cmd, String label, String[] args)
    {
        if(sender instanceof Player == false)
        {
            sender.sendMessage("NPCShop v"+getDescription().getVersion()+" by Fireblast709");
            return true;
        }
        if(!getWorld().equals(((Player)sender).getWorld().getName()))
        {
            sender.sendMessage("This command can only be used in world "+getWorld());
            return true;
        }
        if(cmd.getName().equals("npc"))
        {
            if(args.length < 1)
            {
                sender.sendMessage("NPCShop v"+getDescription().getVersion()+" by Fireblast709");
                if(sender.hasPermission("npc.spawn"))
                {
                    sender.sendMessage("/npc spawn <type> - spawns an NPC vendor at your location");
                }
                return true;
            }
            else
            {
                if(args[0].equalsIgnoreCase("spawn") && sender.hasPermission("npc.spawn"))
                {
                    if(args.length > 1)
                    {
                        StringBuilder type = new StringBuilder();
                        for(int i = 1; i < args.length; i++)
                        {
                            type.append(args[i]).append(" ");
                        }
                        getManager().spawnNPC((Player)sender, type.toString().trim());
                        return true;
                    }
                    else
                    {
                        sender.sendMessage("Please specify an entity type");
                        return false;
                    }
                }
                else if(args[0].equalsIgnoreCase("create") && sender.hasPermission("npc.create"))
                {
                    if(args.length < 1)
                    {
                        
                    }
                    sender.sendMessage("Not yet implemented");
                    return false;
                }
                else if(args[0].equalsIgnoreCase("list") && sender.hasPermission("npc.list"))
                {
                    List<String> names = getManager().getList();
                    sender.sendMessage(ChatColor.GREEN+"NPCs:");
                    for(String name : names)
                    {
                        sender.sendMessage(" * "+name);
                    }
                    return true;
                }
                else if(args[0].equalsIgnoreCase("edit") && sender.hasPermission("npc.edit"))
                {
                    this.playerActions.put(sender.getName(), NPCAction.EDIT);
                    sender.sendMessage("Not yet implemented");
                    new BukkitRunnable()
                    {
                        @Override
                        public void run()
                        {
                            if(playerActions.get(sender.getName()) == NPCAction.EDIT)
                            {
                                playerActions.put(sender.getName(), NPCAction.NONE);
                            }
                        }
                    }.runTaskLater(this, 100L);
                    return true;
                }
                else if(args[0].equalsIgnoreCase("remove") && sender.hasPermission("npc.remove"))
                {
                    this.playerActions.put(sender.getName(), NPCAction.DELETE);
                    sender.sendMessage("Right click the NPC to delete it");
                    new Cooldown(sender.getName()).runTaskLater(this, 100L);
                    return true;
                }
            }
        }
        return false;
    }
    
    private class Cooldown extends BukkitRunnable
    {
        String sender;
        
        public Cooldown(String sender)
        {
            this.sender = sender;
        }
        
        public void run()
        {
            if(playerActions.get(sender) == NPCAction.DELETE)
            {
                playerActions.put(sender, NPCAction.NONE);
            }
        }
        
    }
    
    /*****************************\
     *     - Default files -     *
    \*****************************/
    
    public void createDefault(File folder)
    {
        folder.mkdirs();
        File defaultNPC = new File(folder, "dirtyvillager.yml");
        java.io.BufferedReader reader = null;
        java.io.BufferedWriter writer = null;
        try
        {
            if(!defaultNPC.createNewFile())
            {
                throw new IOException("Failed to create default villager file! Reporting...");
            }
            java.io.InputStream defStream = getResource("dirtyvillager.yml");
            if(defStream != null)
            {
                reader = new java.io.BufferedReader(new java.io.InputStreamReader(defStream));
                writer = new java.io.BufferedWriter(new java.io.FileWriter(defaultNPC));
                String in;
                while((in = reader.readLine()) != null)
                {
                    writer.write(in+"\n");
                }
            }
        }
        catch(IOException ex)
        {
            getLogger().warning("Failed to create default files.\nIt might be hard to guess the values, so ask Fireblast709 about this");
            getLogger().info("Oh and here is the stacktrace, he might need it to solve your problems faster:");
            ex.printStackTrace();
        }
        finally
        {
            if(reader != null)
            {
                try
                {
                    reader.close();
                }
                catch(Exception ex){/* Ignore */}
            }
            if(writer != null)
            {
                try
                {
                    writer.close();
                }
                catch(Exception ex){/* Ignore */}
            }
        }
    }
    
    private class YamlFilter implements java.io.FileFilter
    {
        @Override
        public boolean accept(File pathname)
        {
            return pathname.getPath().endsWith(".yml");
        }   
    }
}
