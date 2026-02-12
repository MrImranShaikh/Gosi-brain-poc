package ai.agentic.agent;

import ai.agentic.llm.LLMClient;

public class PomGeneratorAgent {

    private final LLMClient llm;

    public PomGeneratorAgent(LLMClient llm) {
        this.llm = llm;
    }

    public String generatePom(String architectureJson) {

        String prompt = """
            You are a build engineer generating a Maven pom.xml.
            
            STRICT RULES (violations are errors):
            - Output ONLY valid pom.xml
            - Do NOT invent tags
            - Do NOT include any tags other than standard Maven POM tags
            - Do NOT include architecture metadata like layers
            - Use ONLY: groupId, artifactId, version, packaging, properties, dependencies, build
            
            Constraints:
            - Spring Boot 3.x
            - Java 17
            - Packaging: jar
            - Parent: spring-boot-starter-parent
            
            Dependencies to include (based on architecture):
            - spring-boot-starter-web (if controllers exist)
            - spring-boot-starter-data-jpa (if repositories exist)
            - spring-boot-starter-validation
            - lombok
            - H2 or PostgreSQL (choose one)
            
            Return ONLY valid XML. No explanations.
            
            Architecture (for dependency decisions ONLY):
            %s
            """.formatted(architectureJson);

        return llm.generate(prompt);
    }
}
