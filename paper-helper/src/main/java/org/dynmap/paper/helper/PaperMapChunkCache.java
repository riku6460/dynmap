package org.dynmap.paper.helper;

import org.bukkit.ChunkSnapshot;
import org.dynmap.DynmapChunk;
import org.dynmap.DynmapCore;
import org.dynmap.bukkit.helper.AbstractMapChunkCache;
import org.dynmap.bukkit.helper.BukkitVersionHelper;
import org.dynmap.bukkit.helper.MapChunkCacheClassic;
import org.dynmap.bukkit.helper.SnapshotCache;
import org.dynmap.hdmap.HDBlockModels;
import org.dynmap.utils.DynIntHashMap;
import org.dynmap.utils.VisibilityLimit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class PaperMapChunkCache extends MapChunkCacheClassic {

    private AtomicInteger count = new AtomicInteger();

    @Override
    public int loadChunks(int max_to_load) {
        if(dw.isLoaded() == false)
            return 0;
        Object queue = BukkitVersionHelper.helper.getUnloadQueue(w);

        int cnt = 0;
        if(iterator == null)
            iterator = chunks.listIterator();

        DynmapCore.setIgnoreChunkLoads(true);
        //boolean isnormral = w.getEnvironment() == Environment.NORMAL;
        // Load the required chunks.
        while((cnt < max_to_load) && iterator.hasNext()) {
            long startTime = System.nanoTime();
            DynmapChunk chunk = iterator.next();
            boolean vis = true;
            if(visible_limits != null) {
                vis = false;
                for(VisibilityLimit limit : visible_limits) {
                    if (limit.doIntersectChunk(chunk.x, chunk.z)) {
                        vis = true;
                        break;
                    }
                }
            }
            if(vis && (hidden_limits != null)) {
                for(VisibilityLimit limit : hidden_limits) {
                    if (limit.doIntersectChunk(chunk.x, chunk.z)) {
                        vis = false;
                        break;
                    }
                }
            }
            boolean finalVis = vis;
            /* Check if cached chunk snapshot found */
            if (!w.isChunkGenerated(chunk.x, chunk.z)) {
                endChunkLoad(startTime, ChunkStats.UNGENERATED_CHUNKS);
                cnt++;
                continue;
            }

            count.incrementAndGet();
            w.getChunkAtAsync(chunk.x, chunk.z, c -> {
                long inhabited_ticks = 0;
                DynIntHashMap tileData = null;
                Snapshot ss = null;
                SnapshotCache.SnapshotRec ssr = SnapshotCache.sscache.getSnapshot(dw.getName(), chunk.x, chunk.z, blockdata, biome, biomeraw, highesty);
                if(ssr != null) {
                    inhabited_ticks = ssr.inhabitedTicks;
                    if(!finalVis) {
                        if(hidestyle == HiddenChunkStyle.FILL_STONE_PLAIN)
                            ss = STONE;
                        else if(hidestyle == HiddenChunkStyle.FILL_OCEAN)
                            ss = OCEAN;
                        else
                            ss = EMPTY;
                    }
                    else {
                        ss = ssr.ss;
                    }
                    int idx = (chunk.x-x_min) + (chunk.z - z_min)*x_dim;
                    snaparray[idx] = ss;
                    snaptile[idx] = ssr.tileData;
                    inhabitedTicks[idx] = inhabited_ticks;

                    endChunkLoad(startTime, ChunkStats.CACHED_SNAPSHOT_HIT);
                    done();
                    return;
                }
                boolean wasLoaded = w.isChunkLoaded(chunk.x, chunk.z);
                boolean isunloadpending = false;
                if (queue != null) {
                    isunloadpending = BukkitVersionHelper.helper.isInUnloadQueue(queue, chunk.x, chunk.z);
                }
                if (isunloadpending) {  /* Workaround: can't be pending if not loaded */
                    wasLoaded = true;
                }
                /* If it did load, make cache of it */
                tileData = new DynIntHashMap();
                /* Get inhabited ticks count */

                inhabited_ticks = BukkitVersionHelper.helper.getInhabitedTicks(c);
                if(!finalVis) {
                    if(hidestyle == HiddenChunkStyle.FILL_STONE_PLAIN)
                        ss = STONE;
                    else if(hidestyle == HiddenChunkStyle.FILL_OCEAN)
                        ss = OCEAN;
                    else
                        ss = EMPTY;
                }
                else {
                    ChunkSnapshot css;
                    if(blockdata || highesty) {
                        css = c.getChunkSnapshot(highesty, biome, biomeraw);
                        ss = wrapChunkSnapshot(css);
                        /* Get tile entity data */
                        List<Object> vals = new ArrayList<Object>();
                        Map<?,?> tileents = BukkitVersionHelper.helper.getTileEntitiesForChunk(c);
                        for(Object t : tileents.values()) {
                            int te_x = BukkitVersionHelper.helper.getTileEntityX(t);
                            int te_y = BukkitVersionHelper.helper.getTileEntityY(t);
                            int te_z = BukkitVersionHelper.helper.getTileEntityZ(t);
                            int cx = te_x & 0xF;
                            int cz = te_z & 0xF;
                            String[] te_fields = HDBlockModels.getTileEntityFieldsNeeded(ss.getBlockType(cx, te_y, cz));
                            if(te_fields != null) {
                                Object nbtcompound = BukkitVersionHelper.helper.readTileEntityNBT(t);

                                vals.clear();
                                for(String id: te_fields) {
                                    Object val = BukkitVersionHelper.helper.getFieldValue(nbtcompound, id);
                                    if(val != null) {
                                        vals.add(id);
                                        vals.add(val);
                                    }
                                }
                                if(vals.size() > 0) {
                                    Object[] vlist = vals.toArray(new Object[vals.size()]);
                                    tileData.put(getIndexInChunk(cx,te_y,cz), vlist);
                                }
                            }
                        }
                    }
                    else {
                        css = w.getEmptyChunkSnapshot(chunk.x, chunk.z, biome, biomeraw);
                        ss = wrapChunkSnapshot(css);
                    }
                    if(ss != null) {
                        ssr = new SnapshotCache.SnapshotRec();
                        ssr.ss = ss;
                        ssr.inhabitedTicks = inhabited_ticks;
                        ssr.tileData = tileData;
                        SnapshotCache.sscache.putSnapshot(dw.getName(), chunk.x, chunk.z, ssr, blockdata, biome, biomeraw, highesty);
                    }
                }
                int chunkIndex = (chunk.x-x_min) + (chunk.z - z_min)*x_dim;
                snaparray[chunkIndex] = ss;
                snaptile[chunkIndex] = tileData;
                inhabitedTicks[chunkIndex] = inhabited_ticks;

                if (!wasLoaded) {
                    if (w.isChunkInUse(chunk.x, chunk.z) == false) {
                        if (BukkitVersionHelper.helper.isUnloadChunkBroken()) {
                            // Give up on broken unloadChunk API - lets see if this works
                            w.unloadChunkRequest(chunk.x, chunk.z);
                        }
                        else {
                            BukkitVersionHelper.helper.unloadChunkNoSave(w, c, chunk.x, chunk.z);
                        }
                    }
                    endChunkLoad(startTime, ChunkStats.UNLOADED_CHUNKS);
                }
                else if (isunloadpending) { /* Else, if loaded and unload is pending */
                    if (w.isChunkInUse(chunk.x, chunk.z) == false) {
                        w.unloadChunkRequest(chunk.x, chunk.z); /* Request new unload */
                    }
                    endChunkLoad(startTime, ChunkStats.LOADED_CHUNKS);
                }
                else {
                    endChunkLoad(startTime, ChunkStats.LOADED_CHUNKS);
                }
                done();
            });
            cnt++;
        }

        return cnt;
    }

    @Override
    public boolean isDoneLoading() {
        if (count.get() != 0) {
            return false;
        }
        return super.isDoneLoading();
    }

    @Override
    public void unloadChunks() {
        super.unloadChunks();
        count.set(0);
    }

    private void done() {
        if (count.decrementAndGet() != 0) return;
        DynmapCore.setIgnoreChunkLoads(false);

        if(iterator.hasNext() == false) {   /* If we're done */
            isempty = true;
            /* Fill missing chunks with empty dummy chunk */
            for(int i = 0; i < snaparray.length; i++) {
                if(snaparray[i] == null)
                    snaparray[i] = EMPTY;
                else if(snaparray[i] != EMPTY)
                    isempty = false;
            }
        }
    }
}
