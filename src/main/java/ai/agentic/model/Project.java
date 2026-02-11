package ai.agentic.model;

import lombok.Data;

@Data
public class Project {
    private String type;
    private int javaVersion;
    private String basePackage;
    private String buildTool;
}