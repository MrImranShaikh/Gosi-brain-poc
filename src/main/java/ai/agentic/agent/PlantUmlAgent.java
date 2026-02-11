package ai.agentic.agent;

import ai.agentic.llm.LLMClient;

public class PlantUmlAgent {

    private final LLMClient llm;

    public PlantUmlAgent(LLMClient llm) {
        this.llm = llm;
    }

    public String generateERD(String architectureJson, String modulePlanJson) {

        String prompt = """
Generate a PlantUML ER diagram.

Rules:
- Output ONLY valid PlantUML
- Use @startuml / @enduml
- Entities must match the module plan
- Show relationships if present
- No explanations
- No markdown fences

Architecture:
%s

Module Plan:
%s
""".formatted(architectureJson, modulePlanJson);

        return llm.generate(prompt);
    }
}
