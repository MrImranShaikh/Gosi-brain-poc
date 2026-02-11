package ai.agentic.agent;

import ai.agentic.llm.LLMClient;

public class ArchitecturePlannerAgent {

    private final LLMClient llm;

    public ArchitecturePlannerAgent(LLMClient llm) {
        this.llm = llm;
    }

    public String planArchitecture(String requirementJson) {

        String prompt = """
                You are a senior Java architect.
                
                Based on the system requirements below, decide ONLY the project-level architecture.
                
                STRICT RULES:
                - Do NOT redefine entities
                - Do NOT redefine operations
                - Do NOT include domain models
                - Do NOT include business logic
                
                Return ONLY valid JSON.
                No explanations. No markdown.
                
                Schema:
                {
                  "project": {
                    "type": "spring-boot",
                    "javaVersion": 17,
                    "basePackage": "",
                    "buildTool": "maven"
                  },
                  "dependencies": [
                    {
                      "name": "",
                      "group": "",
                      "version": ""
                    }
                  ],
                  "layers": {
                    "controller": true,
                    "service": true,
                    "repository": true
                  }
                }
                
                System requirements (for context only):
                %s
                """.formatted(requirementJson);


        return llm.generate(prompt);
    }
}
