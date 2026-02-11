package ai.agentic.agent;

import ai.agentic.llm.LLMClient;
//import lombok.AllArgsConstructor;

//@AllArgsConstructor
public class RequirementAnalyzerAgent {

    private final LLMClient llm;

    public RequirementAnalyzerAgent(LLMClient llm) {
        this.llm = llm;
    }

    public String analyze(String brd) {
        String prompt = """
            You are a senior software architect.
            
            Analyze the BRD below and return ONLY valid JSON.
            No explanations. No markdown. No text outside JSON.
            
            Schema:
            {
              "modules": [
                {
                  "name": "",
                  "description": "",
                  "entities": [],
                  "operations": []
                }
              ]
            }
            
            BRD:
            %s
            """.formatted(brd);

        return llm.generate(prompt);
    }
}
