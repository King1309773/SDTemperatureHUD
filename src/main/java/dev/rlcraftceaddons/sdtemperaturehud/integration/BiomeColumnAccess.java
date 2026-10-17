package dev.rlcraftceaddons.sdtemperaturehud.integration;

import java.lang.reflect.Method;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

/** Read-only adapters for the storage contracts verified in the supported instances. */
public final class BiomeColumnAccess {
    private boolean resolved;
    private String version;
    private Object api;
    private Class<?> contract;
    private Method accessor, columns;
    private RuntimeException unavailable;

    public int[] read(Chunk chunk) {
        if(!resolved)resolve();
        if(unavailable!=null)throw unavailable;
        if(version==null)return copyBytes(chunk.getBiomeArray());
        try {
            Object source=api==null?chunk:accessor.invoke(api,chunk);
            if(!contract.isInstance(source))throw new UnsupportedOperationException("Unexpected biome storage contract");
            return copyInts((int[])columns.invoke(source));
        } catch(ReflectiveOperationException | ClassCastException problem) {
            throw new UnsupportedOperationException("Extended biome columns unavailable",problem);
        }
    }
    private void resolve() {
        resolved=true;
        ModContainer mod=Loader.instance().getIndexedModList().get("jeid");
        if(mod==null)return;
        version=mod.getVersion();
        try {
            if("2.3.1".equals(version)) {
                Class<?> type=Class.forName("tff.reid.api.BiomeApi");
                api=type.getField("INSTANCE").get(null);
                accessor=type.getMethod("getBiomeAccessor",Chunk.class);
                contract=Class.forName("tff.reid.api.biome.BiomeAccessor");
                columns=contract.getMethod("getBiomes");
            } else if("2.2.3".equals(version)) {
                contract=Class.forName("org.dimdev.jeid.ducks.INewChunk");
                columns=contract.getMethod("getIntBiomeArray");
            } else throw new UnsupportedOperationException("Unverified extended biome storage version: "+version);
        } catch(ReflectiveOperationException | LinkageError | RuntimeException problem) {
            // Never fall back to the dummy vanilla byte array supplied by extended-ID mods.
            unavailable=new UnsupportedOperationException("Extended biome storage adapter unavailable",problem);
        }
    }
    static int[] copyBytes(byte[] values) {
        if(values==null || values.length!=256)throw new IllegalArgumentException("Expected 256 biome columns");
        int[] result=new int[256];
        for(int i=0;i<result.length;i++){int id=values[i]&255;result[i]=id==255?-1:id;}
        return result;
    }
    static int[] copyInts(int[] values) {
        if(values==null || values.length!=256)throw new IllegalArgumentException("Expected 256 biome columns");
        return values.clone();
    }
    static int[] readPublic(Object instance,Class<?> contract,String method) throws ReflectiveOperationException {
        if(!contract.isInstance(instance))throw new IllegalArgumentException("Unexpected biome storage contract");
        return copyInts((int[])contract.getMethod(method).invoke(instance));
    }
}
