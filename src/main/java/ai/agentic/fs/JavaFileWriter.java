package ai.agentic.fs;

import java.nio.file.Files;
import java.nio.file.Path;

public class JavaFileWriter {

    private final Path root;
    private final boolean dryRun;

    public JavaFileWriter(String rootDir, boolean dryRun) {
        this.root = Path.of(rootDir);
        this.dryRun = dryRun;
    }

    public void writeJavaFile(String packageName, String className, String code) {
        Path path = resolvePath(packageName, className);

        if (dryRun) {
            System.out.println("[DRY-RUN] Would write: " + path);
            return;
        }

        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, code);
            System.out.println("Written: " + path);
        } catch (Exception e) {
            throw new RuntimeException("Failed to write Java file: " + path, e);
        }
    }

    private Path resolvePath(String packageName, String className) {
        return root.resolve(
                "src/main/java/" +
                        packageName.replace('.', '/') +
                        "/" + className + ".java"
        );
    }
}
