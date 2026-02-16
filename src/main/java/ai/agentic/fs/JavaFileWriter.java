package ai.agentic.fs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class JavaFileWriter {

    private final String rootDir;
    private final boolean dryRun;

    public JavaFileWriter(String rootDir, boolean dryRun) {
        this.rootDir = rootDir;
        this.dryRun = dryRun;
    }

    public void writeJavaFile(String packageName, String className, String code) {

        try {

            String normalizedClassName = normalizeClassName(className);

            Path packagePath = Path.of(rootDir,
                    "src",
                    "main",
                    "java",
                    packageName.replace(".", "/"));

            Files.createDirectories(packagePath);

            Path filePath = packagePath.resolve(normalizedClassName + ".java");

            String finalCode = ImportInjector.injectImports(code);

            if (!dryRun) {
                Files.deleteIfExists(filePath);
                Files.writeString(filePath, finalCode);
            }

            System.out.println("Written: " + filePath);

        } catch (IOException e) {
            throw new RuntimeException("Failed writing file: " + className, e);
        }
    }

    private String normalizeClassName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Class name cannot be null or blank");
        }
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }
}
