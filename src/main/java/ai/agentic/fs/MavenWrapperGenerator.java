package ai.agentic.fs;

import java.nio.file.Files;
import java.nio.file.Path;

public class MavenWrapperGenerator {

    public static void generate(Path projectRoot) {
        try {
            Path mvnDir = projectRoot.resolve(".mvn/wrapper");
            Files.createDirectories(mvnDir);

            // mvnw (unix)
            Files.writeString(
                    projectRoot.resolve("mvnw"),
                    MVNW
            );

            // mvnw.cmd (windows)
            Files.writeString(
                    projectRoot.resolve("mvnw.cmd"),
                    MVNW_CMD
            );

            // wrapper properties
            Files.writeString(
                    mvnDir.resolve("maven-wrapper.properties"),
                    WRAPPER_PROPERTIES
            );

            System.out.println("Maven Wrapper generated");

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate Maven Wrapper", e);
        }
    }

    private static final String WRAPPER_PROPERTIES = """
distributionUrl=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.6/apache-maven-3.9.6-bin.zip
""";

    private static final String MVNW = """
#!/bin/sh
mvn "$@"
""";

    private static final String MVNW_CMD = """
@echo off
mvn %*
""";
}
