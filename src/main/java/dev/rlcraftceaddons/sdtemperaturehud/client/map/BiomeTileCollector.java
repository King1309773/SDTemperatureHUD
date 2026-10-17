package dev.rlcraftceaddons.sdtemperaturehud.client.map;

import dev.rlcraftceaddons.sdtemperaturehud.map.BiomeTile;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/** Bounded client-thread queue; the source may only return already-loaded data. */
public final class BiomeTileCollector {
    private final Set<Long> queued=new LinkedHashSet<Long>();
    public void enqueue(int x,int z) {
        if(queued.size()<8192) queued.add(((long)x << 32) ^ (z & 0xffffffffL));
    }
    public int pending() {return queued.size();}
    public void reset() {queued.clear();}
    public void tick(int budget,BiFunction<Integer,Integer,BiomeTile> source,Consumer<BiomeTile> sink) {
        tick(budget,source,sink,problem->{throw problem;});
    }
    public void tick(int budget,BiFunction<Integer,Integer,BiomeTile> source,Consumer<BiomeTile> sink,
                     Consumer<RuntimeException> errors) {
        for(int i=0;i<Math.max(0,Math.min(2,budget)) && !queued.isEmpty();i++) {
            Iterator<Long> iterator=queued.iterator();long pos=iterator.next();iterator.remove();
            BiomeTile tile;
            try {tile=source.apply((int)(pos >> 32),(int)pos);}
            catch(RuntimeException unsupported){errors.accept(unsupported);continue;}
            if(tile!=null && tile.isKnown()) sink.accept(tile);
        }
    }
}
