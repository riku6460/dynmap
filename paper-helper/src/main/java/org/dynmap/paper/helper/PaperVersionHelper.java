package org.dynmap.paper.helper;

import org.bukkit.World;
import org.dynmap.DynmapChunk;
import org.dynmap.bukkit.helper.BukkitVersionHelperCB;
import org.dynmap.bukkit.helper.BukkitWorld;
import org.dynmap.utils.MapChunkCache;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class PaperVersionHelper extends BukkitVersionHelperCB {

    private final Map<World, Object> unloadQueues = new WeakHashMap<>();

    @Override
    public MapChunkCache getChunkCache(BukkitWorld dw, List<DynmapChunk> chunks) {
        PaperMapChunkCache c = new PaperMapChunkCache();
        c.setChunks(dw, chunks);
        return c;
    }

    @Override
    public Object getUnloadQueue(World world) {
        if (unloadQueues.containsKey(world)) {
            return unloadQueues.get(world);
        }
        return unloadQueues.put(world, super.getUnloadQueue(world));
    }
}
