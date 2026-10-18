package dev.rlcraftceaddons.sdtemperaturehud;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;

public class RecoveryBuildIdentityTest {
    @Test
    public void releaseVersionIsDeclaredByBuildAndSourceMetadata() throws IOException {
        Path root = Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();
        String properties = new String(Files.readAllBytes(root.resolve("gradle.properties")), StandardCharsets.UTF_8);
        assertTrue(properties.contains("modVersion=1.6.1"));

        Path modClass = root.resolve("src/main/java/dev/rlcraftceaddons/sdtemperaturehud/SDTemperatureHud.java");
        assertTrue("SDTemperatureHud.java must exist", Files.isRegularFile(modClass));
        String source = new String(Files.readAllBytes(modClass), StandardCharsets.UTF_8);
        assertTrue(source.contains("version=\"1.6.1\""));
        assertTrue(source.contains("public static final String VERSION = \"1.6.1\""));
        assertTrue(source.contains("dependencies=\"required-after:simpledifficulty@[0.3.9]\""));
        assertTrue(source.contains("acceptableRemoteVersions=\"*\""));
        assertTrue(source.contains("clientSideOnly=true"));
    }

    @Test
    public void archiveTasksDisableTimestampsAndUseStableFileOrder() throws IOException {
        Path root = Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();
        Path buildScript = root.resolve("build.gradle");
        assertTrue("build.gradle must exist", Files.isRegularFile(buildScript));
        String script = new String(Files.readAllBytes(buildScript), StandardCharsets.UTF_8);

        assertTrue(script.contains("tasks.withType(org.gradle.api.tasks.bundling.AbstractArchiveTask)"));
        assertTrue(script.contains("preserveFileTimestamps = false"));
        assertTrue(script.contains("reproducibleFileOrder = true"));
    }

    @Test
    public void reobfuscatedJarIsRepackedByAReproducibleArchiveTask() throws IOException {
        Path root = Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();
        String script = new String(
                Files.readAllBytes(root.resolve("build.gradle")),
                StandardCharsets.UTF_8
        );

        assertTrue(script.contains("task stageReobfuscatedJar(type: Copy)"));
        assertTrue(script.contains("dependsOn 'reobfJar'"));
        assertTrue(script.contains("task reproducibleReobfuscatedJar(type: Zip)"));
        assertTrue(script.contains("dependsOn stageReobfuscatedJar"));
        assertTrue(script.contains("assemble.dependsOn reproducibleReobfuscatedJar"));
    }
}
