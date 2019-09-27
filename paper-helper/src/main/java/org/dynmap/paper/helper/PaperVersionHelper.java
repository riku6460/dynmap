package org.dynmap.paper.helper;

import org.dynmap.DynmapChunk;
import org.dynmap.bukkit.helper.BukkitVersionHelperCB;
import org.dynmap.bukkit.helper.BukkitWorld;
import org.dynmap.utils.MapChunkCache;

import java.util.List;

public class PaperVersionHelper extends BukkitVersionHelperCB {

    @Override
    public MapChunkCache getChunkCache(BukkitWorld dw, List<DynmapChunk> chunks) {
        PaperMapChunkCache c = new PaperMapChunkCache();
        c.setChunks(dw, chunks);
        return c;
    }
}
