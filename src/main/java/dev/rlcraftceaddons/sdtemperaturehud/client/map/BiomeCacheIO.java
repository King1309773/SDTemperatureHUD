package dev.rlcraftceaddons.sdtemperaturehud.client.map;

import dev.rlcraftceaddons.sdtemperaturehud.map.*;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

/** Blocking file operations: the controller invokes these only on its bounded IO worker. */
public final class BiomeCacheIO {
    private final Path root;
    private final Move move;
    interface Move {void replace(Path source,Path target) throws IOException;}
    public BiomeCacheIO(Path root) {
        this(root,(source,target)->Files.move(source,target,
            StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING));
    }
    BiomeCacheIO(Path root,Move move) {this.root=root.toAbsolutePath().normalize();this.move=move;}
    public Path fileFor(MapWorldKey key,int rx,int rz) {
        Path target=root.resolve(key.fingerprint()).resolve(Integer.toString(key.dimension))
            .resolve(rx+"_"+rz+".sdbt").normalize();
        if(!target.startsWith(root)) throw new IllegalArgumentException("cache path escaped root");
        return target;
    }
    public List<BiomeTile> loadNow(MapWorldKey key,int rx,int rz) throws IOException {
        if(!key.persistent) return Collections.emptyList();
        Path file=fileFor(key,rx,rz);
        if(!Files.isRegularFile(file)) return Collections.emptyList();
        if(Files.size(file)>BiomeTileCodec.MAX_BYTES) throw new IOException("cache file exceeds limit");
        return BiomeTileCodec.decode(Files.readAllBytes(file),key,rx,rz);
    }
    public void saveNow(MapWorldKey key,int rx,int rz,List<BiomeTile> tiles) throws IOException {
        if(!key.persistent) throw new IOException("transient world cannot be persisted");
        Path target=fileFor(key,rx,rz);
        Map<Long,BiomeTile> merged=new LinkedHashMap<Long,BiomeTile>();
        if(Files.isRegularFile(target)) {
            if(Files.size(target)>BiomeTileCodec.MAX_BYTES)throw new IOException("cache file exceeds limit");
            byte[] previous=Files.readAllBytes(target);
            try {
                for(BiomeTile tile:BiomeTileCodec.decode(previous,key,rx,rz))
                    merged.put(((long)tile.chunkX<<32)^(tile.chunkZ&0xffffffffL),tile);
            }catch(IOException invalidCache){ /* Replace only this mod's unusable region with freshly sampled data. */ }
        }
        for(BiomeTile tile:tiles)merged.put(((long)tile.chunkX<<32)^(tile.chunkZ&0xffffffffL),tile);
        byte[] bytes=BiomeTileCodec.encode(key,rx,rz,new ArrayList<BiomeTile>(merged.values()));
        Files.createDirectories(target.getParent());
        // Resolve symlinks before writing: lexical containment alone is insufficient.
        Path realRoot=root.toRealPath(),realParent=target.getParent().toRealPath();
        if(!realParent.startsWith(realRoot)) throw new IOException("cache directory resolves outside root");
        Path temporary=Files.createTempFile(realParent,"sdbt-",".tmp");
        try {Files.write(temporary,bytes);move.replace(temporary,target);}
        finally {Files.deleteIfExists(temporary);}
    }
}
