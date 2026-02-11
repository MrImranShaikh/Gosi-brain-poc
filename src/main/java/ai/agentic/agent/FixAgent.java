package ai.agentic.agent;

import ai.agentic.llm.LLMClient;

public class FixAgent {

    private final LLMClient llm;

    public FixAgent(LLMClient llm) {
        this.llm = llm;
    }

    public String fix(String code, String error) {
        String prompt = """
        Fix the following Java code.
        
        Rules:
        - Return ONLY corrected Java code
        - Preserve intent
        - No explanations
        - Return only Java code.
        - Do not include markdown.
        - Do not explain.
        Errors:
        %s
        
        Code:
        %s
        """.formatted(error, code);

        return llm.generate(prompt);
    }
}
