package ai.agentic.fs;

public class PomTemplate {

    public static String render(
            String groupId,
            String artifactId,
            String version,
            int javaVersion,
            String bootVersion,
            boolean web,
            boolean jpa,
            boolean validation,
            String database,
            boolean swaggerEnabled
    ) {

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

        switch (database) {
            case "mysql" -> deps.append("""
                    <dependency>
                        <groupId>com.mysql</groupId>
                        <artifactId>mysql-connector-j</artifactId>
                        <scope>runtime</scope>
                    </dependency>
                    """);
            case "postgres" -> deps.append("""
                    <dependency>
                        <groupId>org.postgresql</groupId>
                        <artifactId>postgresql</artifactId>
                        <scope>runtime</scope>
                    </dependency>
                    """);
            case "h2" -> deps.append("""
                    <dependency>
                        <groupId>com.h2database</groupId>
                        <artifactId>h2</artifactId>
                        <scope>runtime</scope>
                    </dependency>
                    """);
        }

        if (swaggerEnabled) {
            deps.append("""
                    <dependency>
                        <groupId>org.springdoc</groupId>
                        <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
                        <version>2.5.0</version>
                    </dependency>
                    """);
        }

        // Lombok – always added if generator uses it
        deps.append("""
                <dependency>
                    <groupId>org.projectlombok</groupId>
                    <artifactId>lombok</artifactId>
                    <version>1.18.32</version>
                    <scope>provided</scope>
                </dependency>
                """);

        return """
                <project xmlns="http://maven.apache.org/POM/4.0.0"
                         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
                         http://maven.apache.org/xsd/maven-4.0.0.xsd">

                  <modelVersion>4.0.0</modelVersion>

                  <groupId>%s</groupId>
                  <artifactId>%s</artifactId>
                  <version>%s</version>

                  <parent>
                    <groupId>org.springframework.boot</groupId>
                    <artifactId>spring-boot-starter-parent</artifactId>
                    <version>%s</version>
                  </parent>

                  <properties>
                    <java.version>%s</java.version>
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

                      <plugin>
                        <groupId>org.apache.maven.plugins</groupId>
                        <artifactId>maven-compiler-plugin</artifactId>
                        <version>3.11.0</version>
                        <configuration>
                          <source>${java.version}</source>
                          <target>${java.version}</target>
                          <annotationProcessorPaths>
                            <path>
                              <groupId>org.projectlombok</groupId>
                              <artifactId>lombok</artifactId>
                              <version>1.18.32</version>
                            </path>
                          </annotationProcessorPaths>
                        </configuration>
                      </plugin>

                    </plugins>
                  </build>

                </project>
                """.formatted(
                groupId,
                artifactId,
                version,
                bootVersion,
                javaVersion,
                deps.toString()
        );
    }
}
