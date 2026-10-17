package dev.rlcraftceaddons.sdtemperaturehud.client.config;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ConfigChangeNotifierTest {
    @Test
    public void changedConfigPostsOnThenPostBeforeExplicitSyncAndRefresh() {
        RecordingOperations operations = new RecordingOperations(true, true);

        ConfigChangeNotifier.finish(true, false, false, operations);

        assertEquals(
            Arrays.asList("loaded", "on", "post", "sync", "refresh"),
            operations.calls
        );
    }

    @Test
    public void deniedOnEventSkipsPostButStillSyncsAndRefreshes() {
        RecordingOperations operations = new RecordingOperations(true, false);

        ConfigChangeNotifier.finish(true, true, false, operations);

        assertEquals(
            Arrays.asList("loaded", "on", "sync", "refresh"),
            operations.calls
        );
    }

    @Test
    public void unchangedConfigOnlyPerformsExplicitSyncAndRefresh() {
        RecordingOperations operations = new RecordingOperations(true, true);

        ConfigChangeNotifier.finish(false, true, false, operations);

        assertEquals(Arrays.asList("sync", "refresh"), operations.calls);
    }

    private static final class RecordingOperations implements ConfigChangeNotifier.Operations {
        private final boolean loaded;
        private final boolean allowPost;
        private final List<String> calls = new ArrayList<String>();

        private RecordingOperations(boolean loaded, boolean allowPost) {
            this.loaded = loaded;
            this.allowPost = allowPost;
        }

        @Override
        public boolean isModLoaded() {
            this.calls.add("loaded");
            return this.loaded;
        }

        @Override
        public boolean postOnChanged(boolean worldRunning, boolean requiresMcRestart) {
            this.calls.add("on");
            return this.allowPost;
        }

        @Override
        public void postPostChanged(boolean worldRunning, boolean requiresMcRestart) {
            this.calls.add("post");
        }

        @Override
        public void sync() {
            this.calls.add("sync");
        }

        @Override
        public void refresh() {
            this.calls.add("refresh");
        }
    }
}
