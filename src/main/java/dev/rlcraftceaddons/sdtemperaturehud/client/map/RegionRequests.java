package dev.rlcraftceaddons.sdtemperaturehud.client.map;

import java.util.Set;
import java.util.concurrent.*;

/** Reserve before dispatch: a fast IO worker can complete before execute() returns. */
public final class RegionRequests {
    private final Set<String> pending=ConcurrentHashMap.newKeySet();
    private final Executor executor;
    public RegionRequests(Executor executor){this.executor=executor;}
    public boolean submit(String id,Runnable action) {
        if(!pending.add(id))return false;
        try {executor.execute(action);return true;}
        catch(RejectedExecutionException busy){pending.remove(id);return false;}
    }
    public void forget(String id){pending.remove(id);}
    public void retain(java.util.function.Predicate<String> wanted){pending.removeIf(id->!wanted.test(id));}
    public void clear(){pending.clear();}
}
