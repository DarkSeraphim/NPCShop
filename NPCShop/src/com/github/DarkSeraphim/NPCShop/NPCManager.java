package com.github.DarkSeraphim.NPCShop;

import com.github.DarkSeraphim.NPCShop.NPCs.NPC;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.server.v1_4_R1.Connection;
import net.minecraft.server.v1_4_R1.EntityPlayer;
import net.minecraft.server.v1_4_R1.MinecraftServer;
import net.minecraft.server.v1_4_R1.NetworkManager;
import net.minecraft.server.v1_4_R1.Packet;
import net.minecraft.server.v1_4_R1.Packet102WindowClick;
import net.minecraft.server.v1_4_R1.Packet106Transaction;
import net.minecraft.server.v1_4_R1.Packet10Flying;
import net.minecraft.server.v1_4_R1.Packet130UpdateSign;
import net.minecraft.server.v1_4_R1.Packet14BlockDig;
import net.minecraft.server.v1_4_R1.Packet15Place;
import net.minecraft.server.v1_4_R1.Packet16BlockItemSwitch;
import net.minecraft.server.v1_4_R1.Packet255KickDisconnect;
import net.minecraft.server.v1_4_R1.Packet28EntityVelocity;
import net.minecraft.server.v1_4_R1.Packet3Chat;
import net.minecraft.server.v1_4_R1.Packet51MapChunk;
import net.minecraft.server.v1_4_R1.PlayerConnection;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author DarkSeraphim
 */
public class NPCManager
{

    private NPCShop p;
    private Map<UUID, String> npcs;
    private Map<String, NPC> types;
    private YamlConfiguration npcsCfg;
    private File npcsFile;

    NPCManager(NPCShop p, Map<String, NPC> types)
    {
        this.p = p;
        this.types = types;
        this.npcs = new HashMap<UUID, String>();
        // Spawn NPCs

        this.npcsFile = new File(p.getDataFolder(), "npcs.yml");
        if (!this.npcsFile.exists())
        {
            try
            {
                if (!this.npcsFile.createNewFile())
                {
                    throw new IOException("Failed to create file");
                }
            }
            catch (IOException ex)
            {
                this.npcsFile = null;
                ex.printStackTrace();
            }
        }

        load();

        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                NPCManager.this.save();
            }
        }.runTaskTimer(p, 300 * 20L, p.getConfig().getLong("save-interval", 300) * 20L);

    }

    public Entity spawnNPC(Player spawner, String type)
    {
        return spawnNPC(spawner, type, spawner.getLocation());
    }
    
    public Entity spawnNPC(Player spawner, String type, Location at)
    {
        if (this.types.containsKey(ChatColor.stripColor(type)))
        {
            final NPC npc = this.types.get(ChatColor.stripColor(type));
            EntityType skType = npc.getShopKeeperType();
            final Entity sk;
            String nloc = "";
            if (skType == EntityType.PLAYER)
            {
                net.minecraft.server.v1_4_R1.MinecraftServer server = ((org.bukkit.craftbukkit.v1_4_R1.CraftServer) p.getServer()).getServer();
                net.minecraft.server.v1_4_R1.World world = ((org.bukkit.craftbukkit.v1_4_R1.CraftWorld) at.getWorld()).getHandle();
                net.minecraft.server.v1_4_R1.EntityPlayer ep = new net.minecraft.server.v1_4_R1.EntityPlayer(server, world, npc.getName().length() <= 16 ? npc.getName() : npc.getName().substring(0,16), new net.minecraft.server.v1_4_R1.PlayerInteractManager(world));
                ep.setPositionRotation(at.getX(), at.getY(), at.getZ(), at.getYaw(), at.getPitch());

                Socket socket = new EmptySocket();
                NetworkManager conn = null;
                try
                {
                    conn = new EmptyNetworkManager(socket, "npc mgr", new Connection()
                    {
                        @Override
                        public boolean a()
                        {
                            return false;
                        }
                    }, server.F().getPrivate());
                    ep.playerConnection = new EmptyNetHandler(server, conn, ep);
                    conn.a(ep.playerConnection);
                }
                catch (IOException e)
                {
                    // swallow
                }
                // Check the EntityPlayer constructor for the new name.
                try
                {
                    socket.close();
                }
                catch (IOException ex)
                {
                    // swallow
                }


                world.addEntity(ep);
                sk = ep.getBukkitEntity();
                
                StringBuilder loc = new StringBuilder(":");
                loc.append(at.getX());
                loc.append(",").append(at.getY());
                loc.append(",").append(at.getZ());
                loc.append(",").append(at.getYaw());
                loc.append(",").append(at.getPitch());
                nloc = loc.toString();
            }
            else
            {
                sk = at.getWorld().spawnEntity(at, skType);
            }

            if (sk instanceof LivingEntity)
            {
                final LivingEntity livingSk = (LivingEntity) sk;
                livingSk.setCanPickupItems(false);
                livingSk.setMaxHealth(Integer.MAX_VALUE);
                livingSk.setHealth(Integer.MAX_VALUE);
                livingSk.setMaximumNoDamageTicks(Integer.MAX_VALUE);
                livingSk.setNoDamageTicks(Integer.MAX_VALUE);
                livingSk.setMaximumAir(Integer.MAX_VALUE);
                livingSk.setRemainingAir(Integer.MAX_VALUE);
                livingSk.setRemoveWhenFarAway(false);
                livingSk.getEquipment().setHelmet(npc.getHelmet());
                livingSk.getEquipment().setChestplate(npc.getChestplate());
                livingSk.getEquipment().setLeggings(npc.getLeggings());
                livingSk.getEquipment().setBoots(npc.getBoots());
                livingSk.getEquipment().setItemInHand(npc.getHand());
                if(sk instanceof Player)
                {
                    /*new BukkitRunnable()
                    {
                        @Override
                        public void run()
                        {*/
                            Player playerSk = (Player) livingSk;
                            playerSk.getInventory().setArmorContents(Arrays.copyOfRange(npc.getEquipment(), 0, 4));
                            playerSk.setItemInHand(npc.getHand());
                            playerSk.updateInventory();
                       /* }
                    }.runTaskLater(this.p, 1L);*/
                }
            }
            this.npcs.put(sk.getUniqueId(), type);
            this.npcsCfg.set(sk.getUniqueId().toString(), type+nloc);
            return sk;
        }
        else
        {
            if(spawner != null)
            spawner.sendMessage(ChatColor.RED + "Invalid NPC type");
        }
        return null;
    }

    public NPC getNPC(UUID uid)
    {
        String type = this.npcs.get(uid);
        return this.types.get(ChatColor.stripColor(type));
    }

    public boolean removeNPC(UUID uid)
    {
        if (!this.npcs.containsKey(uid))
        {
            return false;
        }
        this.npcsCfg.set(uid.toString(), null);
        this.npcs.remove(uid);
        return true;
    }

    public boolean isNPC(UUID uid)
    {
        return npcs.get(uid) != null && !npcs.get(uid).isEmpty();
    }

    public void save()
    {
        if (this.npcsFile == null)
        {
            p.getLogger().warning("File npcs.yml not found: failed to save");
            return;
        }
        try
        {
            this.npcsCfg.save(this.npcsFile);
        }
        catch (IOException ex)
        {
            this.p.getLogger().severe("Failed to save npcs");
        }
    }

    public void load()
    {
        if (this.npcsFile == null)
        {
            p.getLogger().warning("File npcs.yml not found: failed to load");
            return;
        }
        
        Set<UUID> uids = getUUIDs();

        World w = Bukkit.getWorld(p.getWorld());
        
        this.npcsCfg = YamlConfiguration.loadConfiguration(this.npcsFile);
        for (String npc : this.npcsCfg.getKeys(false))
        {
            String[] typeloc = this.npcsCfg.getString(npc, "").split(":", 2);
            String type = typeloc[0];
            if (this.types.containsKey(ChatColor.stripColor(type)))
            {
                NPC n = this.types.get(ChatColor.stripColor(type));
                UUID uid = UUID.fromString(npc);
                if(uids.contains(uid))
                {
                    this.npcs.put(UUID.fromString(npc), type);
                }
                else if(n.getShopKeeperType() == EntityType.PLAYER)
                {
                    if(typeloc.length == 2)
                    {
                        String[] pos = typeloc[1].split(",");
                        try
                        {
                            Location loc = new Location(w, 
                                                        Double.parseDouble(pos[0]),
                                                        Double.parseDouble(pos[1]),
                                                        Double.parseDouble(pos[2]),
                                                        Float.parseFloat(pos[3]),
                                                        Float.parseFloat(pos[4]));
                            Entity e = spawnNPC(null, type, loc);
                            if(e == null)
                            {
                                this.npcsCfg.set(npc, null);
                            }
                        }
                        catch(Exception ex)
                        {
                            // Swallow the exception
                        }
                    }
                    
                }
                else
                {
                    this.npcsCfg.set(npc, null);
                }
            }
        }
    }
    
    public Set<UUID> getUUIDs()
    {
        Set<UUID> uids = new HashSet<UUID>();
        for(Entity e : Bukkit.getWorld(p.getWorld()).getEntities())
        {
            uids.add(e.getUniqueId());
        }
        return uids;
    }
    
    public List<String> getList()
    {
        List<String> list = new ArrayList<String>();
        for(String type : this.types.keySet())
        {
            list.add(ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', type)));
        }
        return list;
    }

    /*
     * Temporary class needed
     * Remove when 1.5 is released
     */
    private class EmptySocket extends Socket
    {

        @Override
        public java.io.InputStream getInputStream()
        {
            return new java.io.ByteArrayInputStream(EMPTY);
        }

        @Override
        public java.io.OutputStream getOutputStream()
        {
            return new java.io.ByteArrayOutputStream(1);
        }
        private final byte[] EMPTY = new byte[0];
    }

    public class EmptyNetworkManager extends NetworkManager
    {

        public EmptyNetworkManager(Socket socket, String string, Connection conn, PrivateKey key) throws IOException
        {
            super(socket, string, conn, key);

            try
            {
                java.lang.reflect.Field nm = NetworkManager.class.getDeclaredField("m");
                nm.setAccessible(true);
                nm.set(this, false);
            }
            catch (Exception ex)
            {
                // Swallow the exception
            }
        }

        @Override
        public void a()
        {
        }

        @Override
        public void a(Connection conn)
        {
        }

        @Override
        public void a(String s, Object... objects)
        {
        }

        @Override
        public void b()
        {
        }

        @Override
        public void d()
        {
        }

        @Override
        public int e()
        {
            return 0;
        }

        @Override
        public void queue(Packet packet)
        {
        }
    }

    private class EmptyNetHandler extends PlayerConnection
    {

        public EmptyNetHandler(MinecraftServer minecraftServer, NetworkManager networkManager, EntityPlayer entityPlayer)
        {
            super(minecraftServer, networkManager, entityPlayer);
        }

        @Override
        public void a(Packet102WindowClick packet)
        {
        }

        @Override
        public void a(Packet106Transaction packet)
        {
        }

        @Override
        public void a(Packet10Flying packet)
        {
        }

        @Override
        public void a(Packet130UpdateSign packet)
        {
        }

        @Override
        public void a(Packet14BlockDig packet)
        {
        }

        @Override
        public void a(Packet15Place packet)
        {
        }

        @Override
        public void a(Packet16BlockItemSwitch packet)
        {
        }

        @Override
        public void a(Packet255KickDisconnect packet)
        {
        }

        @Override
        public void a(Packet28EntityVelocity packet)
        {
        }

        @Override
        public void a(Packet3Chat packet)
        {
        }

        @Override
        public void a(Packet51MapChunk packet)
        {
        }

        @Override
        public void a(String string, Object[] objects)
        {
        }

        @Override
        public void sendPacket(Packet packet)
        {
        }
    }
}
