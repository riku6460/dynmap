package org.dynmap.bukkit.helper.v117;

import org.bukkit.*;
import org.bukkit.craftbukkit.v1_17_R1.CraftChunk;
import org.bukkit.entity.Player;
import org.dynmap.DynmapChunk;
import org.dynmap.Log;
import org.dynmap.bukkit.helper.BukkitMaterial;
import org.dynmap.bukkit.helper.BukkitVersionHelper;
import org.dynmap.bukkit.helper.BukkitVersionHelperGeneric;
import org.dynmap.bukkit.helper.BukkitWorld;
import org.dynmap.bukkit.helper.v117.MapChunkCache117;
import org.dynmap.renderer.DynmapBlockState;
import org.dynmap.utils.MapChunkCache;
import org.dynmap.utils.Polygon;

import net.minecraft.core.IRegistry;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.nbt.NBTTagShort;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.biome.BiomeBase;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BlockFluids;
import net.minecraft.world.level.block.BlockRotatable;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.material.Material;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;


/**
 * Helper for isolation of bukkit version specific issues
 */
public class BukkitVersionHelperSpigot117 extends BukkitVersionHelper {    
    public BukkitVersionHelperSpigot117() {
    }
    
    /**
     * Get block short name list
     */
    @Override
    public String[] getBlockNames() {
    	int cnt = Block.p.a();
    	String[] names = new String[cnt];
    	for (int i = 0; i < cnt; i++) {
    		IBlockData bd = Block.getByCombinedId(i);
    		names[i] = IRegistry.W.getKey(bd.getBlock()).toString();
    		Log.info(i + ": blk=" + names[i] + ", bd=" + bd.toString());
    	}
        return names;
    }

    private IRegistry<BiomeBase> reg = null;

    private IRegistry<BiomeBase> getBiomeReg() {
    	if (reg == null) {
    		reg = MinecraftServer.getServer().getCustomRegistry().b(IRegistry.aO);
    	}
    	return reg;
    }
    
    private Object[] biomelist;
    /**
     * Get list of defined biomebase objects
     */
    @Override
    public Object[] getBiomeBaseList() {
    	if (biomelist == null) {
    		biomelist = new Object[1024];
            for (int i = 0; i < 1024; i++) {
            	biomelist[i] = getBiomeReg().fromId(i);
            }
        }
        return biomelist;
    }

    /** Get ID from biomebase */
    @Override
    public int getBiomeBaseID(Object bb) {
    	return getBiomeReg().getId((BiomeBase)bb);
    }
    
    public static IdentityHashMap<IBlockData, DynmapBlockState> dataToState;
    
    /**
     * Initialize block states (org.dynmap.blockstate.DynmapBlockState)
     */
    @Override
    public void initializeBlockStates() {
    	dataToState = new IdentityHashMap<IBlockData, DynmapBlockState>();
    	HashMap<String, DynmapBlockState> lastBlockState = new HashMap<String, DynmapBlockState>();
    	
    	int cnt = Block.p.a();
    	// Loop through block data states
    	for (int i = 0; i < cnt; i++) {
    		IBlockData bd = Block.getByCombinedId(i);
    		String bname = IRegistry.W.getKey(bd.getBlock()).toString();
    		DynmapBlockState lastbs = lastBlockState.get(bname);	// See if we have seen this one
    		int idx = 0;
    		if (lastbs != null) {	// Yes
    			idx = lastbs.getStateCount();	// Get number of states so far, since this is next
    		}
    		// Build state name
    		String sb = "";
    		String fname = bd.toString();
    		int off1 = fname.indexOf('[');
    		if (off1 >= 0) {
    			int off2 = fname.indexOf(']');
    			sb = fname.substring(off1+1, off2);
    		}
    		Material mat = bd.getMaterial();
            DynmapBlockState bs = new DynmapBlockState(lastbs, idx, bname, sb, mat.toString());
            if ((!bd.getFluid().isEmpty()) && ((bd.getBlock() instanceof BlockFluids) == false)) {	// Test if fluid type for block is not empty
            	bs.setWaterlogged();
            }
            if (mat == Material.a) {	// AIR
            	bs.setAir();
            }
    		if (mat == Material.E) {	// LEAVES
    			bs.setLeaves();
    		}
    		if ((bd.getBlock() instanceof BlockRotatable) && (bd.getMaterial() == Material.z)) {	// WOOD
    			bs.setLog();
    		}
    		if (mat.isSolid()) {
    			bs.setSolid();
    		}
    		dataToState.put(bd,  bs);
    		lastBlockState.put(bname, (lastbs == null) ? bs : lastbs);
    		Log.verboseinfo(i + ": blk=" + bname + ", idx=" + idx + ", state=" + sb + ", waterlogged=" + bs.isWaterlogged());
    	}
    }
    /**
     * Create chunk cache for given chunks of given world
     * @param dw - world
     * @param chunks - chunk list
     * @return cache
     */
    @Override
    public MapChunkCache getChunkCache(BukkitWorld dw, List<DynmapChunk> chunks) {
        MapChunkCache117 c = new MapChunkCache117();
        c.setChunks(dw, chunks);
        return c;
    }
    
	/**
	 * Get biome base water multiplier
	 */
    @Override
	public int getBiomeBaseWaterMult(Object bb) {
    	BiomeBase biome = (BiomeBase)bb;
    	return biome.l().b();	// waterColor
	}

    /** Get temperature from biomebase */
    @Override
    public float getBiomeBaseTemperature(Object bb) {
    	return ((BiomeBase)bb).k();
    }

    /** Get humidity from biomebase */
    @Override
    public float getBiomeBaseHumidity(Object bb) {
    	return ((BiomeBase)bb).getHumidity();    	
    }
    
    @Override
    public Polygon getWorldBorder(World world) {
        Polygon p = null;
        WorldBorder wb = world.getWorldBorder();
        if (wb != null) {
        	Location c = wb.getCenter();
        	double size = wb.getSize();
        	if ((size > 1) && (size < 1E7)) {
        	    size = size / 2;
        		p = new Polygon();
        		p.addVertex(c.getX()-size, c.getZ()-size);
        		p.addVertex(c.getX()+size, c.getZ()-size);
        		p.addVertex(c.getX()+size, c.getZ()+size);
        		p.addVertex(c.getX()-size, c.getZ()+size);
        	}
        }
        return p;
    }
	// Send title/subtitle to user
    public void sendTitleText(Player p, String title, String subtitle, int fadeInTicks, int stayTicks, int fadeOutTIcks) {
    	if (p != null) {
    		p.sendTitle(title, subtitle, fadeInTicks, stayTicks, fadeOutTIcks);
    	}
    }
    
    /**
     * Get material map by block ID
     */
    @Override
    public BukkitMaterial[] getMaterialList() {
    	return new BukkitMaterial[4096];	// Not used
    }

	@Override
	public void unloadChunkNoSave(World w, org.bukkit.Chunk c, int cx, int cz) {
		Log.severe("unloadChunkNoSave not implemented");
	}

	private String[] biomenames;
	@Override
	public String[] getBiomeNames() {
    	if (biomenames == null) {
    		biomenames = new String[1024];
            for (int i = 0; i < 1024; i++) {
            	BiomeBase bb = getBiomeReg().fromId(i);
            	if (bb != null) {
            		biomenames[i] = bb.toString();
            	}
            }
        }
        return biomenames;
	}

	@Override
	public String getStateStringByCombinedId(int blkid, int meta) {
        Log.severe("getStateStringByCombinedId not implemented");		
		return null;
	}
	@Override
    /** Get ID string from biomebase */
    public String getBiomeBaseIDString(Object bb) {
        String s = ((BiomeBase)bb).toString();
        if (s != null) {
        	String[] ss = s.split("\\.");
        	return ss[ss.length-1];
        }
        return null;
    }

	@Override
	public Object getUnloadQueue(World world) {
		System.out.println("getUnloadQueue not implemented yet");
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isInUnloadQueue(Object unloadqueue, int x, int z) {
		System.out.println("isInUnloadQueue not implemented yet");
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Object[] getBiomeBaseFromSnapshot(ChunkSnapshot css) {
		System.out.println("getBiomeBaseFromSnapshot not implemented yet");
		// TODO Auto-generated method stub
		return new Object[256];
	}

	@Override
	public long getInhabitedTicks(Chunk c) {
		return ((CraftChunk)c).getHandle().getInhabitedTime();
	}

	@Override
	public Map<?, ?> getTileEntitiesForChunk(Chunk c) {
		return ((CraftChunk)c).getHandle().l;
	}

	@Override
	public int getTileEntityX(Object te) {
		TileEntity tileent = (TileEntity) te;
		return tileent.getPosition().getX();
	}

	@Override
	public int getTileEntityY(Object te) {
		TileEntity tileent = (TileEntity) te;
		return tileent.getPosition().getY();
	}

	@Override
	public int getTileEntityZ(Object te) {
		TileEntity tileent = (TileEntity) te;
		return tileent.getPosition().getZ();
	}

	@Override
	public Object readTileEntityNBT(Object te) {
		TileEntity tileent = (TileEntity) te;
		NBTTagCompound nbt = new NBTTagCompound();
		tileent.save(nbt);	// readNBT
        return nbt;
	}

	@Override
	public Object getFieldValue(Object nbt, String field) {
		NBTTagCompound rec = (NBTTagCompound) nbt;
		NBTBase val = rec.get(field);
        if(val == null) return null;
        if(val instanceof NBTTagByte) {
            return ((NBTTagByte)val).asByte();
        }
        else if(val instanceof NBTTagShort) {
            return ((NBTTagShort)val).asShort();
        }
        else if(val instanceof NBTTagInt) {
            return ((NBTTagInt)val).asInt();
        }
        else if(val instanceof NBTTagLong) {
            return ((NBTTagLong)val).asLong();
        }
        else if(val instanceof NBTTagFloat) {
            return ((NBTTagFloat)val).asFloat();
        }
        else if(val instanceof NBTTagDouble) {
            return ((NBTTagDouble)val).asDouble();
        }
        else if(val instanceof NBTTagByteArray) {
            return ((NBTTagByteArray)val).getBytes();
        }
        else if(val instanceof NBTTagString) {
            return ((NBTTagString)val).asString();
        }
        else if(val instanceof NBTTagIntArray) {
            return ((NBTTagIntArray)val).getInts();
        }
        return null;
	}

	@Override
	public Player[] getOnlinePlayers() {
        Collection<? extends Player> p = Bukkit.getServer().getOnlinePlayers();
        return p.toArray(new Player[0]);
	}

	@Override
	public double getHealth(Player p) {
		return p.getHealth();
	}
}
