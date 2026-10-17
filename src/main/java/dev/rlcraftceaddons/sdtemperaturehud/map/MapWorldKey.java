package dev.rlcraftceaddons.sdtemperaturehud.map;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

public final class MapWorldKey {
    public final String namespace, worldId;
    public final int dimension;
    public final long generation;
    public final boolean persistent;
    private String fingerprint;

    public MapWorldKey(String namespace, String worldId, int dimension, long generation, boolean persistent) {
        if (namespace == null || namespace.isEmpty() || worldId == null || worldId.isEmpty())
            throw new IllegalArgumentException("world identity must not be empty");
        this.namespace=namespace; this.worldId=worldId; this.dimension=dimension;
        this.generation=generation; this.persistent=persistent;
    }
    /** Stable disk identity excludes the connection generation. No raw server/path is written. */
    public String fingerprint() {
        if(fingerprint!=null)return fingerprint;
        try {
            MessageDigest digest=MessageDigest.getInstance("SHA-256");
            digest.update(namespace.getBytes(StandardCharsets.UTF_8)); digest.update((byte)0);
            byte[] hash=digest.digest(worldId.getBytes(StandardCharsets.UTF_8));
            StringBuilder out=new StringBuilder(64);
            for(byte b:hash) out.append(String.format(java.util.Locale.ROOT,"%02x",b & 255));
            fingerprint=out.toString();return fingerprint;
        } catch(NoSuchAlgorithmException impossible) { throw new IllegalStateException(impossible); }
    }
    @Override public boolean equals(Object other) {
        if (!(other instanceof MapWorldKey)) return false;
        MapWorldKey key=(MapWorldKey)other;
        return dimension==key.dimension && generation==key.generation && persistent==key.persistent
            && namespace.equals(key.namespace) && worldId.equals(key.worldId);
    }
    @Override public int hashCode() { return Objects.hash(namespace,worldId,dimension,generation,persistent); }
}
