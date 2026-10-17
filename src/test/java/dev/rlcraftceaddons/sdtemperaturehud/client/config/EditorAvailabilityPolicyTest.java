package dev.rlcraftceaddons.sdtemperaturehud.client.config;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class EditorAvailabilityPolicyTest {
    @Test
    public void editorRequiresBothWorldAndPlayer() {
        assertFalse(EditorAvailabilityPolicy.isAvailable(false, false));
        assertFalse(EditorAvailabilityPolicy.isAvailable(true, false));
        assertFalse(EditorAvailabilityPolicy.isAvailable(false, true));
        assertTrue(EditorAvailabilityPolicy.isAvailable(true, true));
    }
}
