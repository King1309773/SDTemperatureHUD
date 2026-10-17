package dev.rlcraftceaddons.sdtemperaturehud.map;

import java.io.*;
import java.util.*;

/** Bounded, versioned data owned by this mod, not Xaero's map format. */
public final class BiomeTileCodec {
    public static final int MAX_BYTES=8*1024*1024;
    private static final int MAGIC=0x53444254, VERSION=1;
    private BiomeTileCodec() {}

    public static byte[] encode(MapWorldKey key,int rx,int rz,List<BiomeTile> tiles) throws IOException {
        if(tiles==null || tiles.size()>1024) throw new IOException("invalid tile count");
        ByteArrayOutputStream buffer=new ByteArrayOutputStream();
        DataOutputStream out=new DataOutputStream(buffer);
        out.writeInt(MAGIC); out.writeInt(VERSION);out.writeUTF(key.fingerprint());
        out.writeInt(key.dimension);out.writeInt(rx);out.writeInt(rz);out.writeInt(tiles.size());
        Set<Long> positions=new HashSet<Long>();
        for(BiomeTile tile:tiles) {
            validatePosition(tile.chunkX,tile.chunkZ,rx,rz,positions);
            out.writeInt(tile.chunkX);out.writeInt(tile.chunkZ);
            Map<String,Integer> palette=new LinkedHashMap<String,Integer>();
            for(int z=0;z<16;z++) for(int x=0;x<16;x++) {
                String name=tile.get(x,z);if(name!=null && !palette.containsKey(name)) palette.put(name,palette.size());
            }
            out.writeInt(palette.size()); for(String name:palette.keySet()) out.writeUTF(name);
            for(int z=0;z<16;z++) for(int x=0;x<16;x++) {
                String name=tile.get(x,z);out.writeShort(name==null?-1:palette.get(name));
            }
            if(buffer.size()>MAX_BYTES) throw new IOException("cache region exceeds limit");
        }
        out.flush();return buffer.toByteArray();
    }

    public static List<BiomeTile> decode(byte[] bytes,MapWorldKey key,int rx,int rz) throws IOException {
        if(bytes==null || bytes.length>MAX_BYTES) throw new IOException("invalid cache size");
        DataInputStream in=new DataInputStream(new ByteArrayInputStream(bytes));
        if(in.readInt()!=MAGIC || in.readInt()!=VERSION) throw new IOException("unsupported cache format");
        if(!key.fingerprint().equals(in.readUTF()) || key.dimension!=in.readInt()
            || rx!=in.readInt() || rz!=in.readInt()) throw new IOException("cache identity mismatch");
        int count=in.readInt();if(count<0 || count>1024) throw new IOException("invalid tile count");
        List<BiomeTile> tiles=new ArrayList<BiomeTile>(count);Set<Long> positions=new HashSet<Long>();
        for(int n=0;n<count;n++) {
            int cx=in.readInt(),cz=in.readInt();validatePosition(cx,cz,rx,rz,positions);
            int size=in.readInt();if(size<0 || size>256) throw new IOException("invalid palette size");
            String[] palette=new String[size];Set<String> names=new HashSet<String>();
            for(int i=0;i<size;i++) {
                palette[i]=in.readUTF();
                if(palette[i].length()>256 || !names.add(palette[i])) throw new IOException("invalid palette entry");
            }
            String[] cells=new String[256];
            for(int i=0;i<256;i++) {
                int index=in.readShort();if(index < -1 || index>=size) throw new IOException("invalid palette index");
                cells[i]=index<0?null:palette[index];
            }
            try {tiles.add(new BiomeTile(cx,cz,cells));}
            catch(IllegalArgumentException invalid) {throw new IOException("invalid biome identity",invalid);}
        }
        if(in.read()!=-1) throw new IOException("unexpected trailing cache data");
        return Collections.unmodifiableList(tiles);
    }

    private static void validatePosition(int x,int z,int rx,int rz,Set<Long> seen) throws IOException {
        if(BiomeTileStore.region(x)!=rx || BiomeTileStore.region(z)!=rz
            || !seen.add(((long)x << 32) ^ (z & 0xffffffffL))) throw new IOException("invalid tile position");
    }
}
