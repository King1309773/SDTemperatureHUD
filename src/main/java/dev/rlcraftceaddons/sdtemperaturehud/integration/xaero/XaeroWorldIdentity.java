package dev.rlcraftceaddons.sdtemperaturehud.integration.xaero;

import dev.rlcraftceaddons.sdtemperaturehud.map.MapWorldKey;

/** Only confirmed identities may be used across connections. No optional map types here. */
public final class XaeroWorldIdentity {
    private XaeroWorldIdentity() {}
    public static boolean shouldReplace(MapWorldKey current,MapWorldKey next) {
        if(next==null)return false;
        return current==null || next.persistent && (!current.persistent
            || current.namespace.equals(next.namespace) && !current.worldId.equals(next.worldId));
    }
    public static MapWorldKey choose(String localSave,String confirmedSource,String confirmedId,
                                     int dimension,long generation,String connection) {
        if(localSave!=null && !localSave.isEmpty())
            return new MapWorldKey("singleplayer",localSave,dimension,generation,true);
        if(confirmedSource!=null && confirmedId!=null && !confirmedId.isEmpty())
            return new MapWorldKey(confirmedSource,confirmedId,dimension,generation,true);
        return new MapWorldKey("connection",connection,dimension,generation,false);
    }
}
