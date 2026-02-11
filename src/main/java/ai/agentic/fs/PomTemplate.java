package ai.agentic.fs;

public class PomTemplate {

    private PomTemplate() {
        // utility class
    }

    public static String render(
            String groupId,
            String artifactId,
            String version,
            int javaVersion,
            String springBootVersion,
            boolean web,
            boolean jpa,
            boolean validation
    ) {

        if (groupId == null || groupId.isBlank()) {
            throw new IllegalArgumentException("groupId must not be empty");
        }
        if (artifactId == null || artifactId.isBlank()) {
            throw new IllegalArgumentException("artifactId must not be empty");
        }
        if (springBootVersion == null || springBootVersion.isBlank()) {
            throw new IllegalArgumentException("springBootVersion must not be empty");
        }

        // Maven-safe artifactId
        artifactId = artifactId
                .toLowerCase()
                .replaceAll("[^a-z0-9.-]", "-");

        StringBuilder deps = new StringBuilder();

        if (web) {
            deps.append("""
                <dependency>
                    <groupId>org.springframework.boot</groupId>
                    <artifactId>spring-boot-starter-web</artifactId>
                </dependency>
            """);
        }

        if (jpa) {
            deps.append("""
                <dependency>
                    <groupId>org.springframework.boot</groupId>
                    <artifactId>spring-boot-starter-data-jpa</artifactId>
                </dependency>
            """);
        }

        if (validation) {
            deps.append("""
                <dependency>
                    <groupId>org.springframework.boot</groupId>
                    <artifactId>spring-boot-starter-validation</artifactId>
                </dependency>
            """);
        }

        // Lombok (compile-time only)
        deps.append("""
            <dependency>
                <groupId>org.projectlombok</groupId>
                <artifactId>lombok</artifactId>
                <scope>provided</scope>
            </dependency>
        """);

        // H2 database (safe default)
        deps.append("""
            <dependency>
                <groupId>com.h2database</groupId>
                <artifactId>h2</artifactId>
                <scope>runtime</scope>
            </dependency>
        """);

        return """
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="
           http://maven.apache.org/POM/4.0.0
           http://maven.apache.org/xsd/maven-4.0.0.xsd">

    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>%s</version>
        <relativePath/>
    </parent>

    <groupId>%s</groupId>
    <artifactId>%s</artifactId>
    <version>%s</version>
    <packaging>jar</packaging>

    <properties>
        <java.version>%d</java.version>
    </properties>

    <dependencies>
%s
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>

</project>
""".formatted(
                springBootVersion,
                groupId,
                artifactId,
                version,
                javaVersion,
                deps.toString().indent(8)
        );
    }
}
