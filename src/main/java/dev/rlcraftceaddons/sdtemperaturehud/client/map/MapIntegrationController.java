package dev.rlcraftceaddons.sdtemperaturehud.client.map;

import dev.rlcraftceaddons.sdtemperaturehud.SDTemperatureHud;
import dev.rlcraftceaddons.sdtemperaturehud.config.HudConfig;
import dev.rlcraftceaddons.sdtemperaturehud.integration.BiomeColumnAccess;
import dev.rlcraftceaddons.sdtemperaturehud.integration.xaero.*;
import dev.rlcraftceaddons.sdtemperaturehud.map.*;
import java.util.*;
import java.util.concurrent.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/** All world access is on the client thread. Renderers only request/read prepared views. */
public final class MapIntegrationController {
    private static MapIntegrationController instance;
    public interface Bridge {
        void attach();
        String persistentWorld(int physicalDimension);
        boolean usable();
    }
    private final Minecraft mc;
    private final BiomeTileStore store=new BiomeTileStore();
    private final BiomeTileCollector collector=new BiomeTileCollector();
    private final BiomeColumnAccess columns=new BiomeColumnAccess();
    private final BiomeCacheIO disk;
    private final ThreadPoolExecutor worker=new ThreadPoolExecutor(1,1,30,TimeUnit.SECONDS,
        new ArrayBlockingQueue<Runnable>(16),r->{Thread t=new Thread(r,"SD HUD biome cache");t.setDaemon(true);return t;});
    private final ArrayBlockingQueue<Loaded> completed=new ArrayBlockingQueue<Loaded>(8);
    private final RegionRequests requested=new RegionRequests(worker);
    private final Set<Long> dirty=new LinkedHashSet<Long>();
    private final Bridge[] bridges=new Bridge[2];
    private final boolean[] failed=new boolean[2];
    private final View[] views=new View[2];
    private WorldClient world;
    private MapWorldKey key;
    private String connection=UUID.randomUUID().toString();
    private long generation,ticks,lastWarning;
    private int previousX=Integer.MIN_VALUE,previousZ=Integer.MIN_VALUE;

    public MapIntegrationController(Minecraft mc) {
        this.mc=mc;this.disk=new BiomeCacheIO(mc.gameDir.toPath().resolve("sdtemperaturehud/biome-cache/v1"));
        instance=this;
    }
    public static String availability(boolean mini) {
        return instance!=null && instance.failed(mini)?"failed":XaeroIntegration.status(mini);
    }
    public boolean enabled(boolean mini) {
        int index=mini?0:1;
        return bridges[index]!=null && !failed[index]
            && (mini || HudConfig.mapIntegration.worldMapEnabled);
    }
    public void disable(boolean mini,Throwable cause) {
        failed[mini?0:1]=true;warn("Xaero "+(mini?"minimap":"world map")+" integration disabled",cause);
    }
    public boolean failed(boolean mini) {return failed[mini?0:1];}

    @SubscribeEvent public void tick(TickEvent.ClientTickEvent event) {
        if(event.phase!=TickEvent.Phase.END) return;
        ticks++;
        if(ticks%20==1) attach();
        if(mc.world!=world) {
            flush();world=mc.world;key=null;generation++;connection=UUID.randomUUID().toString();
            collector.reset();store.clear();requested.clear();dirty.clear();completed.clear();
            Arrays.fill(views,null);previousX=previousZ=Integer.MIN_VALUE;
        }
        if(world==null || mc.player==null) return;
        // Keep the earlier area-cache implementation dormant without deleting user cache files.
        if(!XaeroIntegration.areaSamplingEnabled())return;
        for(int index=0;index<views.length;index++)if(!enabled(index==0))views[index]=null;
        if(!enabled(true) && !enabled(false)) {flush();collector.reset();previousX=previousZ=Integer.MIN_VALUE;return;}
        if(key==null || !mc.isSingleplayer() && ticks%20==1) {
            MapWorldKey resolved=resolveIdentity();
            if(XaeroWorldIdentity.shouldReplace(key,resolved)) {
                flush();generation++;
                key=new MapWorldKey(resolved.namespace,resolved.worldId,resolved.dimension,generation,resolved.persistent);
                store.clear();collector.reset();requested.clear();dirty.clear();completed.clear();Arrays.fill(views,null);
                previousX=previousZ=Integer.MIN_VALUE;
            }
        }
        Loaded loaded;
        while((loaded=completed.poll())!=null) {
            if(sameSession(loaded.key)) for(BiomeTile tile:loaded.tiles)
                if(wanted(loaded.key,BiomeTileStore.region(tile.chunkX),BiomeTileStore.region(tile.chunkZ))
                    && store.get(loaded.key,tile.chunkX,tile.chunkZ)==null)store.put(loaded.key,tile);
        }
        if(!mc.isGamePaused()) {
            int cx=(int)Math.floor(mc.player.posX/16),cz=(int)Math.floor(mc.player.posZ/16);
            if(cx!=previousX || cz!=previousZ) {
                if(previousX!=Integer.MIN_VALUE && (Math.abs((long)cx-previousX)>32 || Math.abs((long)cz-previousZ)>32))collector.reset();
                queueNearby(cx,cz);previousX=cx;previousZ=cz;
            }
            collector.tick(2,this::sample,tile->{if(store.put(key,tile))dirty.add(pair(BiomeTileStore.region(tile.chunkX),BiomeTileStore.region(tile.chunkZ)));},
                problem->warn("Biome column data unavailable; the affected chunk was skipped",problem));
        }
        for(int i=0;i<views.length;i++) {
            View view=views[i];if(view!=null && (view.version!=store.version(view.key) || !view.built) && ticks%5==0)
                build(view);
        }
        if(ticks%100==0) flush();
        if(ticks%20==0) {
            // Retry requests rejected/dropped under IO back-pressure, even if the view has not moved.
            for(View view:views)if(view!=null)prepareLoads(view);
            flush();prune();
        }
    }
    @SubscribeEvent public void chunkLoaded(ChunkEvent.Load event) {
        if(event.getWorld()==world && key!=null)collector.enqueue(event.getChunk().x,event.getChunk().z);
    }
    private void attach() {
        for(int i=0;i<2;i++) {
            boolean mini=i==0;if(failed[i] || !"ready".equals(XaeroIntegration.status(mini)))continue;
            try {
                if(bridges[i]==null) {
                    Class<?> type=Class.forName("dev.rlcraftceaddons.sdtemperaturehud.integration.xaero."
                        +(mini?"XaeroMinimapInfoBridge":"XaeroWorldMapBridge"));
                    bridges[i]=(Bridge)type.getConstructor(MapIntegrationController.class).newInstance(this);
                }
                bridges[i].attach();
            } catch(ReflectiveOperationException | LinkageError | RuntimeException problem) {disable(mini,problem);}
        }
    }
    private MapWorldKey resolveIdentity() {
        String local=null,source=null,id=null;int dimension=world.provider.getDimension();
        if(mc.isSingleplayer() && mc.getIntegratedServer()!=null && mc.getIntegratedServer().getWorld(0)!=null) {
            try {local=mc.getIntegratedServer().getWorld(0).getSaveHandler().getWorldDirectory().getCanonicalPath();}
            catch(java.io.IOException exception) {warn("Local map identity unavailable",exception);}
        }
        if(local==null) for(int index:new int[]{1,0}) {
            Bridge bridge=bridges[index];if(bridge==null || failed[index] || !bridge.usable())continue;
            try {id=bridge.persistentWorld(dimension);}catch(RuntimeException | LinkageError exception){disable(index==0,exception);}
            if(id!=null) {source=index==0?"minimap":"worldmap";break;}
        }
        return XaeroWorldIdentity.choose(local,source,id,dimension,generation,connection);
    }
    private void queueNearby(int cx,int cz) {
        int radius=Math.max(2,Math.min(16,mc.gameSettings.renderDistanceChunks));
        for(int r=0;r<=radius;r++)for(int dx=-r;dx<=r;dx++)for(int dz=-r;dz<=r;dz++)
            if(Math.abs(dx)==r || Math.abs(dz)==r)collector.enqueue(cx+dx,cz+dz);
    }
    private BiomeTile sample(int x,int z) {
        Chunk chunk=world.getChunkProvider().getLoadedChunk(x,z);
        if(chunk==null || !chunk.isLoaded())return null;
        int[] biomes=columns.read(chunk);
        String[] names=new String[256];Map<Integer,String> palette=new HashMap<Integer,String>();
        for(int i=0;i<256;i++) {
            int id=biomes[i];if(id<0)continue;
            if(!palette.containsKey(id)) {
                Biome biome=Biome.getBiome(id);
                palette.put(id,biome!=null && Biome.getIdForBiome(biome)==id && biome.getRegistryName()!=null
                    ?biome.getRegistryName().toString():null);
            }
            names[i]=palette.get(id);
        }
        return new BiomeTile(x,z,names);
    }
    /** A render call only records a small request; it does not sample, scan, or read files. */
    public List<BiomeRegion> regions(boolean mini,int dimension,double x,double z,int radiusChunks) {
        if(key==null || !enabled(mini))return Collections.emptyList();
        if(!key.persistent && dimension!=key.dimension)return Collections.emptyList();
        int index=mini?0:1,cx=(int)Math.floor(x/16),cz=(int)Math.floor(z/16);
        int radius=Math.max(1,Math.min(32,radiusChunks));
        View view=views[index];
        if(view==null || view.cx!=cx || view.cz!=cz || view.radius!=radius || view.key.dimension!=dimension) {
            MapWorldKey target=new MapWorldKey(key.namespace,key.worldId,dimension,generation,key.persistent);
            view=new View(target,cx,cz,radius);views[index]=view;
        }
        return view.regions;
    }
    private void build(View view) {
        List<BiomeTile> tiles=new ArrayList<BiomeTile>();
        for(int rx=BiomeTileStore.region(view.cx-view.radius);rx<=BiomeTileStore.region(view.cx+view.radius);rx++)
            for(int rz=BiomeTileStore.region(view.cz-view.radius);rz<=BiomeTileStore.region(view.cz+view.radius);rz++) {
                requestLoad(view.key,rx,rz);
                for(BiomeTile tile:store.regionTiles(view.key,rx,rz))
                    if(Math.abs((long)tile.chunkX-view.cx)<=view.radius && Math.abs((long)tile.chunkZ-view.cz)<=view.radius)tiles.add(tile);
            }
        tiles.sort(Comparator.comparingDouble(tile->distance(tile.chunkX-view.cx,tile.chunkZ-view.cz)));
        if(tiles.size()>256)tiles=new ArrayList<BiomeTile>(tiles.subList(0,256));
        view.regions=BiomeRegionCandidates.nearest(BiomeRegionBuilder.build(tiles),view.cx*16d+8,view.cz*16d+8,256);
        view.version=store.version(view.key);view.built=true;
    }
    private void prepareLoads(View view) {
        for(int rx=BiomeTileStore.region(view.cx-view.radius);rx<=BiomeTileStore.region(view.cx+view.radius);rx++)
            for(int rz=BiomeTileStore.region(view.cz-view.radius);rz<=BiomeTileStore.region(view.cz+view.radius);rz++)requestLoad(view.key,rx,rz);
    }
    private String requestId(MapWorldKey target,int rx,int rz) {
        return target.generation+":"+target.fingerprint()+":"+target.dimension+":"+rx+":"+rz;
    }
    private void requestLoad(MapWorldKey target,int rx,int rz) {
        if(!target.persistent)return;
        String request=requestId(target,rx,rz);
        requested.submit(request,()->{
                try {if(!completed.offer(new Loaded(target,disk.loadNow(target,rx,rz))))requested.forget(request);}
                catch(java.io.IOException exception){warn("Biome cache region could not be read",exception);}
            });
    }
    private void flush() {
        if(key==null || !key.persistent){dirty.clear();return;}
        Iterator<Long> iterator=dirty.iterator();
        while(iterator.hasNext()) {
            long position=iterator.next();int rx=(int)(position>>32),rz=(int)position;
            final MapWorldKey target=key;final List<BiomeTile> tiles=store.regionTiles(target,rx,rz);
            try {
                worker.execute(()->{try {disk.saveNow(target,rx,rz,tiles);}
                    catch(java.io.IOException exception){warn("Biome cache region could not be saved",exception);}});
                iterator.remove();
            }catch(RejectedExecutionException busy){break;}
        }
    }
    private boolean sameSession(MapWorldKey other) {
        return key!=null && key.generation==other.generation && key.namespace.equals(other.namespace) && key.worldId.equals(other.worldId);
    }
    private boolean wanted(MapWorldKey target,int rx,int rz) {
        if(key==null || !sameSession(target))return false;
        if(target.equals(key) && mc.player!=null && RegionWindow.contains(rx,rz,
            (int)Math.floor(mc.player.posX/16),(int)Math.floor(mc.player.posZ/16),16))return true;
        for(View view:views)if(view!=null && view.key.equals(target)
            && RegionWindow.contains(rx,rz,view.cx,view.cz,view.radius))return true;
        return false;
    }
    private void prune() {
        // Include negative cache entries: endless panning must not retain absent regions forever.
        requested.retain(id->{
            String[] parts=id.split(":");
            if(key==null || parts.length!=5 || !parts[0].equals(Long.toString(generation))
                || !parts[1].equals(key.fingerprint()))return false;
            MapWorldKey target=new MapWorldKey(key.namespace,key.worldId,Integer.parseInt(parts[2]),generation,key.persistent);
            return wanted(target,Integer.parseInt(parts[3]),Integer.parseInt(parts[4]));
        });
        for(MapWorldKey target:store.worldKeys()) {
            for(long region:store.regionPositions(target)) {
                int rx=(int)(region>>32),rz=(int)region;
                // A rejected save keeps its region alive until a later flush accepts the snapshot.
                if(!wanted(target,rx,rz) && (!target.equals(key) || !dirty.contains(region))) {
                    store.discardRegion(target,rx,rz);requested.forget(requestId(target,rx,rz));
                }
            }
            if(store.regionPositions(target).isEmpty())store.clear(target);
        }
    }
    private static long pair(int x,int z){return ((long)x<<32)^(z&0xffffffffL);}
    private static double distance(double x,double z){return x*x+z*z;}
    private synchronized void warn(String message,Throwable cause) {
        long now=System.currentTimeMillis();if(now-lastWarning<30000)return;lastWarning=now;
        SDTemperatureHud.LOGGER.warn(message,cause);
    }
    private static final class Loaded {
        final MapWorldKey key;final List<BiomeTile> tiles;
        Loaded(MapWorldKey key,List<BiomeTile> tiles){this.key=key;this.tiles=tiles;}
    }
    private static final class View {
        final MapWorldKey key;final int cx,cz,radius;long version=-1;boolean built;
        List<BiomeRegion> regions=Collections.emptyList();
        View(MapWorldKey key,int cx,int cz,int radius){this.key=key;this.cx=cx;this.cz=cz;this.radius=radius;}
    }
}
