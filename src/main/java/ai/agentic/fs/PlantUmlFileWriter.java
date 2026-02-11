package ai.agentic.fs;

import java.nio.file.Files;
import java.nio.file.Path;

public class PlantUmlFileWriter {

    private final Path root;
    private final boolean dryRun;

    public PlantUmlFileWriter(String rootDir, boolean dryRun) {
        this.root = Path.of(rootDir);
        this.dryRun = dryRun;
    }

    public void writeDiagram(String name, String plantUml) {
        Path path = root.resolve("diagrams/" + name + ".puml");

        if (dryRun) {
            System.out.println("[DRY-RUN] Would write: " + path);
            return;
        }

        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, plantUml);
            System.out.println("Written: " + path);
        } catch (Exception e) {
            throw new RuntimeException("Failed to write PlantUML file", e);
        }
    }
}
