package dev.rlcraftceaddons.sdtemperaturehud;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class BaselineMetadataTest {
    @Test
    public void declaresPublishedIdentityAuthorAndDependency() throws IOException {
        InputStream metadata = getClass().getClassLoader().getResourceAsStream("mcmod.info");
        assertNotNull("mcmod.info must be packaged", metadata);

        String json = readUtf8(metadata);
        assertPublishedIdentity(json);
    }

    @Test
    public void rejectsModidNestedOutsideTheModidField() {
        String misleadingJson = "[{\n" +
                "  \"modid\": \"not-sdtemperaturehud\",\n" +
                "  \"version\": \"1.6.1\",\n" +
                "  \"authorList\": [\"KingJJ\"],\n" +
                "  \"dependencies\": [\"simpledifficulty\"],\n" +
                "  \"nested\": {\"modid\": \"sdtemperaturehud\"}\n" +
                "}]";

        assertRejectedMetadata(misleadingJson);
    }

    @Test
    public void rejectsAuthorOutsideAuthorList() {
        String misleadingJson = "[{\n" +
                "  \"modid\": \"sdtemperaturehud\",\n" +
                "  \"version\": \"1.6.1\",\n" +
                "  \"authorList\": [\"Someone Else\"],\n" +
                "  \"dependencies\": [\"simpledifficulty\"],\n" +
                "  \"credits\": \"KingJJ\"\n" +
                "}]";

        assertRejectedMetadata(misleadingJson);
    }

    @Test
    public void rejectsDependencyOutsideDependencies() {
        String misleadingJson = "[{\n" +
                "  \"modid\": \"sdtemperaturehud\",\n" +
                "  \"version\": \"1.6.1\",\n" +
                "  \"authorList\": [\"KingJJ\"],\n" +
                "  \"dependencies\": [\"anothermod\"],\n" +
                "  \"description\": \"simpledifficulty\"\n" +
                "}]";

        assertRejectedMetadata(misleadingJson);
    }

    private static void assertRejectedMetadata(String json) {
        try {
            assertPublishedIdentity(json);
        }
        catch (AssertionError expected) {
            return;
        }
        throw new AssertionError("metadata with values in the wrong fields must be rejected");
    }

    private static void assertPublishedIdentity(String json) {
        JsonArray mods = new JsonParser().parse(json).getAsJsonArray();
        assertEquals("mcmod.info must describe one mod", 1, mods.size());

        JsonObject metadata = mods.get(0).getAsJsonObject();
        assertEquals("modid must remain sdtemperaturehud",
                "sdtemperaturehud", metadata.get("modid").getAsString());
        assertEquals("release metadata must carry version 1.6.1",
                "1.6.1", metadata.get("version").getAsString());
        assertTrue("authorList must contain KingJJ",
                containsString(metadata.getAsJsonArray("authorList"), "KingJJ"));
        assertTrue("dependencies must contain simpledifficulty",
                containsString(metadata.getAsJsonArray("dependencies"), "simpledifficulty"));
    }

    private static boolean containsString(JsonArray values, String expected) {
        if (values == null) {
            return false;
        }
        for (JsonElement value : values) {
            if (value.isJsonPrimitive() && expected.equals(value.getAsString())) {
                return true;
            }
        }
        return false;
    }

    private static String readUtf8(InputStream input) throws IOException {
        try (InputStream stream = input; ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int read;
            while ((read = stream.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
            return new String(output.toByteArray(), StandardCharsets.UTF_8);
        }
    }
}
