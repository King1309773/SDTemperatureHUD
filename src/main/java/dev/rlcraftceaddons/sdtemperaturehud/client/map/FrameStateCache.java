package dev.rlcraftceaddons.sdtemperaturehud.client.map;

import java.util.Objects;
import java.util.function.Supplier;

/** Render-thread confined; expensive GL state is captured only when the first label is painted. */
final class FrameStateCache<T> {
    private T state;
    void beginFrame(){state=null;}
    T get(Supplier<T> capture){if(state==null)state=Objects.requireNonNull(capture.get());return state;}
}
