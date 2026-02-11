package ai.agentic.agent;

import ai.agentic.llm.LLMClient;

public class ErdAgent {

    private final LLMClient llm;

    public ErdAgent(LLMClient llm) {
        this.llm = llm;
    }

    public String generatePlantUml(String architectureJson) {
        String prompt = """
Generate a PlantUML ER diagram.

Rules:
- Use @startuml / @enduml
- Show entities and relationships
- No prose
- Output ONLY PlantUML

Architecture:
%s
""".formatted(architectureJson);

        return llm.generate(prompt);
    }
}
