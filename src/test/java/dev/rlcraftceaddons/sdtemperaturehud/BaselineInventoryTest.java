package dev.rlcraftceaddons.sdtemperaturehud;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BaselineInventoryTest {
    @Test
    public void recoveredSourcesMatchEveryPublishedTopLevelClass() throws IOException {
        Path root = projectRoot();
        Set<String> publishedClasses = new TreeSet<String>();
        Set<String> expectedSources = new TreeSet<String>();

        try (ZipFile published = new ZipFile(root.resolve("reference/sdtemperaturehud-1.2.1.jar").toFile())) {
            for (ZipEntry entry : java.util.Collections.list(published.entries())) {
                String name = entry.getName();
                if (name.startsWith("dev/rlcraftceaddons/sdtemperaturehud/") && name.endsWith(".class")) {
                    publishedClasses.add(name);
                    if (!name.contains("$")) {
                        expectedSources.add(name.substring(0, name.length() - ".class".length()) + ".java");
                    }
                }
            }
        }

        Set<String> actualSources = relativeFiles(root.resolve("src/main/java"), ".java");
        assertEquals("published class inventory changed", 28, publishedClasses.size());
        assertEquals("published top-level class inventory changed", 24, expectedSources.size());
        assertTrue("recovered sources must retain every published top-level class",
                actualSources.containsAll(expectedSources));
    }

    @Test
    public void recoveredResourcesMatchEveryPublishedResource() throws IOException {
        Path root = projectRoot();
        Set<String> expectedResources = new TreeSet<String>();
        try (ZipFile published = new ZipFile(root.resolve("reference/sdtemperaturehud-1.2.1.jar").toFile())) {
            for (ZipEntry entry : java.util.Collections.list(published.entries())) {
                String name = entry.getName();
                if (!entry.isDirectory() && (name.startsWith("assets/") ||
                        name.equals("mcmod.info") || name.equals("pack.mcmeta"))) {
                    expectedResources.add(name);
                }
            }
        }

        assertTrue("recovered resources must retain every published resource",
                relativeFiles(root.resolve("src/main/resources"), null).containsAll(expectedResources));
        assertTrue(expectedResources.contains("assets/sdtemperaturehud/lang/zh_cn.lang"));
        assertEquals(3, expectedResources.size());
    }

    private static Path projectRoot() {
        return Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();
    }

    private static Set<String> relativeFiles(Path root, String suffix) throws IOException {
        Set<String> files = new TreeSet<String>();
        try (Stream<Path> paths = Files.walk(root)) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> suffix == null || path.getFileName().toString().endsWith(suffix))
                    .forEach(path -> files.add(root.relativize(path).toString().replace('\\', '/')));
        }
        return files;
    }
}
