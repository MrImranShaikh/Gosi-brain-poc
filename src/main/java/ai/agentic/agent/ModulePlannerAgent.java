package ai.agentic.agent;

import ai.agentic.llm.LLMClient;

public class ModulePlannerAgent {

    private final LLMClient llm;

    public ModulePlannerAgent(LLMClient llm) {
        this.llm = llm;
    }

    public String planModule(String moduleJson, String architectureJson) {

        String prompt = """
            You are a STRICT code planning agent.
            
            ABSOLUTE RULES (violations are errors):
            - Output MUST be valid JSON
            - Output MUST start with '{' and end with '}'
            - Do NOT include explanations, headings, or prose
            - ALL arrays MUST be non-empty
            - Every PlannedClass MUST contain BOTH 'name' and 'package'
            - Module name MUST be a valid Java identifier (no spaces)
            
            Schema (MANDATORY):
            {
              "moduleName": "",
              "basePackage": "",
              "entities": [
                { "name": "", "package": "" }
              ],
              "repositories": [
                { "name": "", "package": "" }
              ],
              "serviceInterfaces": [
                { "name": "", "package": "" }
              ],
              "serviceImplementations": [
                { "name": "", "package": "" }
              ],
              "controllers": [
                { "name": "", "package": "" }
              ]
            }
            
            Instructions:
            - Generate a FULL Spring Boot module
            - Do NOT omit layers
            - Use conventional package names:
              - entities
              - repositories
              - services
              - services.impl
              - controllers
            
            Module requirements:
            %s
            
            Architecture:
            %s
            """.formatted(moduleJson, architectureJson);

        return llm.generate(prompt);
    }
}
