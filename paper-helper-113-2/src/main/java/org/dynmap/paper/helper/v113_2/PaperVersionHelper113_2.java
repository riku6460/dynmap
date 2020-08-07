package org.dynmap.paper.helper.v113_2;

import org.bukkit.World;
import org.dynmap.DynmapChunk;
import org.dynmap.bukkit.helper.BukkitWorld;
import org.dynmap.bukkit.helper.v113_2.BukkitVersionHelperSpigot113_2;
import org.dynmap.utils.MapChunkCache;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class PaperVersionHelper113_2 extends BukkitVersionHelperSpigot113_2 {

    private final Map<World, Object> unloadQueues = new WeakHashMap<>();

    @Override
    public MapChunkCache getChunkCache(BukkitWorld dw, List<DynmapChunk> chunks) {
        PaperMapChunkCache113_2 c = new PaperMapChunkCache113_2();
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
